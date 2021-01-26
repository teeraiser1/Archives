package main.java.archives;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.managers.AudioManager;

public class ArchivesCommandController {
    public static JDA jda;

    
    /************************ Initiation *************************/
    static void initDir() {
		File meme_path = new File(Constants.Files.MEME_PATH);
        if (!meme_path.exists()) {
        	meme_path.mkdir();
        }
        
		File meme_common_path = new File(Constants.Files.MEME_COMMON_PATH);
        if (!meme_common_path.exists()) {
        	meme_common_path.mkdir();
        }

		File log_path = new File(Constants.Files.LOG_PATH);
        if (!log_path.exists()) {
        	log_path.mkdir();
        }
        
		File data_path = new File(Constants.Files.DATA_PATH);
        if (!data_path.exists()) {
        	data_path.mkdir();
        }

		System.out.println("Essensial dir init success");
    }
    static void initDir(String guildID) {
		File meme_guild_path = new File(Constants.Files.MEME_PATH + guildID + "//");
        if (!meme_guild_path.exists()) {
        	meme_guild_path.mkdir();
        }

		System.out.println("guild dir init success");
    }

    
    static void RequestSoragodong(MessageChannel channel) {
    	channel.sendFile(new File(Constants.Files.MEME_COMMON_PATH + "soragodong_qst.jpeg"))
    			.queue();
    	channel.sendFile(new File(Constants.Files.MEME_COMMON_PATH + "soragodong_ans.jpeg"))
    			.queue();

    	Random rand = ThreadLocalRandom.current();
    	if (rand.nextInt(2) == 0)
        	channel.sendMessage("ㅡ                                        아ㅡ니                                       ㅡ")
        			.queue();
    	else
        	channel.sendMessage("ㅡ                                           응.                                           ㅡ")
			.queue();
    		
    }
    

    /************************ Show *************************/
    static void showMembers(MessageChannel channel, Guild guild) {
    	List<Member> members = guild.getMembers();
    	for (Member mb : members)
    	{
    		channel.sendMessage(mb.getEffectiveName())
    			.queue();
    	}
    }
    static void showMemeList(MessageChannel channel, String guildID) {
    	String str = MemeCmdController.showMemeCmd(guildID);
    	channel.sendMessage(str).queue();
    }
    
    static void ShowUpdateFunction(MessageChannel channel) {
    	File updateFile = new File(Constants.Files.ROOT_PATH + "update.txt");
    	
    	if (!updateFile.exists())
    		return ;
    	
    	try {
    		FileReader reader = new FileReader(updateFile);
    		BufferedReader bufReader = new BufferedReader(reader);
			StringBuilder builder = new StringBuilder();
			
			builder.append("업데이트 내용\n");
			
			String line = null;
			if ((line = bufReader.readLine()) != null)
				builder.append(line)
						.append("\n");
			
			while ((line = bufReader.readLine()) != null && !line.startsWith("#ver")) {
				builder.append(line)
						.append("\n");
			}
			bufReader.close();
			reader.close();
			
			channel.sendMessage(builder).queue();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }
    
    static void showHelp(MessageChannel channel, String version) {
    	StringBuilder builder = new StringBuilder();
    	builder.append("기록보관소 버전 : " + version + "\n")
    			.append("!뽑기 a b c ...\n")
		    	.append("    : a b c ... 중 하나 뽑음\n")
				.append("!뽑기 멤버\n")
				.append("    : 참가형 뽑기. !참가! 를 통해 참가하고 !참가종료! 시 참가자 중 한명 뽑음\n")
				.append("\n")
				.append("!멤버\n")
				.append("    : 멤버 표시 (어떤 기준인지 잘 모르겠...\n")
				.append("!검색\n")
				.append("    : 구글 검색\n")
				.append("\n")
				.append("!밈\n")
				.append("    : 현재 등록된 밈 리스트 출력\n")
				.append("!밈 삭제 밈이름	\n")
				.append("    : 해당 밈 삭제\n")
				.append("파일 올리며 댓글에 !등록 밈이름\n")
				.append("    : 밈 등록\n")
				.append("파일 올리며 댓글에 !수정 밈이름\n")
				.append("    : 밈 수정\n")
				.append("\n")
				.append("!@p 링크\n")
				.append("    : 음악 추가(유튜브)\n")
				.append("파일 올리며 댓글에 !@p m\n")
				.append("    : 음악 추가(업로드 파일)\n")
				.append("!@s\n")
				.append("    : 음악 스킵\n")
				.append("!@list\n")
				.append("    : 음악 재생목록 리스트\n")
				.append("!@now 링크\n")
				.append("    : 음악 바로 재생\n")
				.append("!@pause\n")
				.append("    : 음악 일시정지\n")
				.append("!@resume\n")
				.append("    : 음악 재생\n")
				.append("!@v 숫자\n")
				.append("    : 음악 볼륨\n")
				.append("!업데이트 내용\n")
				.append("    : 최신 업데이트 내용 출력\n");
		channel.sendMessage(builder).queue();
    }
    

    /************************ Power *************************/
    static void reboot(MessageChannel channel) {
		try {
			//Process archives = Runtime.getRuntime().exec(Constants.Files.ROOT_PATH + "Archives.jar");
/*			System.out.println("java -jar " + System.getProperty("user.dir") + "\\Archives.jar");
			Process archives = Runtime.getRuntime().exec("java -jar " + System.getProperty("user.dir") + "\\Archives.jar");
			System.out.println(archives);*/
			ProcessBuilder pb = new ProcessBuilder(System.getProperty("java.home"), "-jar", "Archives.jar");
			//ProcessBuilder pb = new ProcessBuilder("P:\\Program Files\\Java\\jdk-15.0.1\\bin\\java.exe", "-jar", "Archives.jar");
			pb.directory(new File(System.getProperty("user.dir")));
			Process archives = pb.start();
			
			if (archives != null) {		    	
				NoticeController.NotifyArchivesDisconnected();
		    	try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					System.out.println("sleep fail : " + e.toString());
					e.printStackTrace();
				}
		    	
		    	System.exit(0);
			}
		} catch (IOException e) {
			channel.sendMessage("재부팅에 실패하였습니다.");
			System.out.println("Reboot fail : " + e.toString());
			e.printStackTrace();
		}
    	
    }
    static void shutdown(MessageChannel channel) {
    	NoticeController.NotifyArchivesDisconnected();
    	jda.getPresence().setStatus(OnlineStatus.OFFLINE);
    	
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			System.out.println("sleep fail : " + e.toString());
			e.printStackTrace();
		}
    	System.exit(0);
    }
    
    
    
	static boolean checkAudioManagerAlone(AudioManager audioManager) {
		return audioManager.isConnected() && audioManager.getConnectedChannel().getMembers().size() == 1;
	}
    
    
}
