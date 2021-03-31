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
    @DebugCommandHandler
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
        
		File statistic_path = new File(Constants.Files.STATISTIC_PATH);
        if (!statistic_path.exists()) {
        	statistic_path.mkdir();
        }

		System.out.println("Essensial dir init success");
    }
    @DebugCommandHandler
    static void initDir(String guildID) {
		File meme_guild_path = new File(Constants.Files.MEME_PATH + guildID + "//");
        if (!meme_guild_path.exists()) {
        	meme_guild_path.mkdir();
        }

		System.out.println("guild dir init success");
    }


    @DebugCommandHandler
    static void requestSoragodong(MessageChannel channel) {
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
    @DebugCommandHandler
    static void showMembers(MessageChannel channel, Guild guild) {
    	List<Member> members = guild.getMembers();
    	for (Member mb : members)
    	{
    		channel.sendMessage(mb.getEffectiveName())
    			.queue();
    	}
    }
    
    @DebugCommandHandler
    static void showUpdatedFunction(MessageChannel channel) {
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

    @DebugCommandHandler
    static void showHelp(MessageChannel channel, String helpType) {
    	String helpString = null;
    	System.out.println("QWER " + helpType);
    	if (helpType == null)
    		helpString = HelpCommand.getArchiveHelp();
    	else if (helpType.matches("기록보관소"))
    		helpString = HelpCommand.getArchiveCommandHelp();
    	else if (helpType.matches("밈"))
    		helpString = HelpCommand.getMemeHelp();
    	else if (helpType.matches("음악"))
    		helpString = HelpCommand.getMusicHelp();
    	else if (helpType.matches("통계"))
    		helpString = HelpCommand.getStatisticHelp();
    	
    	if (helpType != null)
    		channel.sendMessage(helpString).queue();
    }
    

    /************************ Power *************************/
    @DebugCommandHandler
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
				NoticeController.notifyArchivesDisconnected();
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
    @DebugCommandHandler
    static void shutdown(MessageChannel channel) {
    	NoticeController.notifyArchivesDisconnected();
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
