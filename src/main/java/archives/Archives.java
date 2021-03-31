package main.java.archives;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.user.UserActivityEndEvent;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import net.dv8tion.jda.api.entities.User;

import javax.security.auth.login.LoginException;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sedmelluq.discord.lavaplayer.demo.BotApplicationManager;

import main.java.archives.ActivityPerDateData.ActivityTimePerDate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
/* This project is based on JDA example "MessageListenerExample.java" */

@SuppressWarnings("unused")
public class Archives extends ListenerAdapter
{
	public static String ArchivesVersion = "6.0";
	public static Archives archives;
	
	private static JDA jda;
	private static DebugCommandManager debugCommandManager;
	private boolean isAttending = false;
	private Vector<String> participants = new Vector<String>();
	HashMap<String, Timestamp> studyTime = new HashMap<String, Timestamp>();
	
	private static long userActivityStatisticDeleteChecker_id;
	
	private static int MEME_ADD = 0;
	private static int MEME_MODIFY = 1;
	
	private static boolean noticeFlag = false;


    /**
     * This is the method where the program starts.
     *
     */
    public static void main(String[] args) throws FileNotFoundException
    {
        //We construct a builder for a BOT account. If we wanted to use a CLIENT account
        // we would use AccountType.CLIENT
        try
        {
        	checkOption(args);
            jda = JDABuilder.createDefault(PrivateData.TOKEN) // The token of the account that is logging in.
                    .addEventListeners(new Archives())   // An instance of a class that will handle events.
                    .addEventListeners(new BotApplicationManager())
                    .enableIntents(GatewayIntent.GUILD_PRESENCES)	// onUserUpdateOnlineStatus를 사용하기 위해 설정 - 봇의 네트워크 부하 심함 but 해당 기능을 위해 필요함
                    .enableCache(CacheFlag.CLIENT_STATUS)	// 유저 활동 기록에서 기기에 따른 유저 스테이터스를 얻기 위해 설정
                    .enableCache(CacheFlag.ACTIVITY)	// 유저 활동 기록에서 유저의 활동을 얻기 위해 설정
                    .build();
            jda.awaitReady(); // Blocking guarantees that JDA will be completely loaded.

            Class.forName("com.mysql.cj.jdbc.Driver");
			System.out.println("Loaded JDBC Driver");

	        /************* 직접 구현한 기능들 *************/
	        initControllers();
	        initShutdownHook();
	        ArchivesCommandController.initDir();	 // 필수 디렉토리 체크 및 생성
	        
	        UserActivityStatisticController.loadMonitoringUsers();
	        UserActivityStatisticController.AutoMidnightRecordThread_beforeMidnight.getInstance().start();
	        UserActivityStatisticController.AutoMidnightRecordThread_midnight.getInstance().start();
	        
	        /*** 사용한지 오래된 밈 명령어 제거  ***/
	        ArchivesThreads.ExpiredMemeCheckThread.getInstance().start();
	        /*** 대화 채널에 아무도 없을 경우 대화방 퇴장  ***/
	        ArchivesThreads.AfkVoiceChannelCheckThread.getInstance().start();
	        
            System.out.println("Finished Building JDA!");

            UserActivityStatisticController.RecordSystemOffStatus();
            UserActivityStatisticController.RecordEveryUsersCurrentStatus();
            
            if (noticeFlag)
            	NoticeController.notifyArchivesConnected();
        }
        catch (LoginException e)
        {
            //If anything goes wrong in terms of authentication, this is the exception that will represent it
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            //Due to the fact that awaitReady is a blocking method, one which waits until JDA is fully loaded,
            // the waiting can be interrupted. This is the exception that would fire in that situation.
            //As a note: in this extremely simplified example this will never occur. In fact, this will never occur unless
            // you use awaitReady in a thread that has the possibility of being interrupted (async thread usage and interrupts)
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
			System.out.println("Driver load fail : " + e.toString());
			e.printStackTrace();
		}
    }

    /**
     * NOTE THE @Override!
     * This method is actually overriding a method in the ListenerAdapter class! We place an @Override annotation
     *  right before any method that is overriding another to guarantee to ourselves that it is actually overriding
     *  a method from a super class properly. You should do this every time you override a method!
     *
     * As stated above, this method is overriding a hook method in the
     * {@link net.dv8tion.jda.api.hooks.ListenerAdapter ListenerAdapter} class. It has convenience methods for all JDA events!
     * Consider looking through the events it offers if you plan to use the ListenerAdapter.
     *
     * In this example, when a message is received it is printed to the console.
     *
     * @param event
     *          An event containing information about a {@link net.dv8tion.jda.api.entities.Message Message} that was
     *          sent in a channel.
     */

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
    	Guild guild = event.getGuild();
    	
    	NoticeController.notifyArchivesConnected();
    	MemeCmdController.createMemeTable(guild);
    	ArchivesCommandController.initDir(guild.getId());
    }
    
    @Override	
	public void onUserUpdateOnlineStatus(UserUpdateOnlineStatusEvent event)	{
    	UserActivityStatisticController.recordUserStatus(event.getMember(), event.getGuild());
	}
    @Override	
	public void onUserActivityStart(UserActivityStartEvent event) {
    	UserActivityStatisticController.recordUserStatus(event.getMember(), event.getGuild());
    }
    @Override	
	public void onUserActivityEnd(UserActivityEndEvent event) {
    	UserActivityStatisticController.recordUserStatus(event.getMember(), event.getGuild());
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        //These are provided with every event in JDA
        JDA jda = event.getJDA();                       //JDA, the core of the api.
        long responseNumber = event.getResponseNumber();//The amount of discord events that JDA has received since the last reconnect.

        //Event specific information
        Guild guild = event.getGuild();             //The Guild that this message was sent in. (note, in the API, Guilds are Servers)
        User author = event.getAuthor();                //The user that sent the message
        Member member = event.getMember();
        Message message = event.getMessage();           //The message that was received.
        MessageChannel channel = event.getChannel();    //This is the MessageChannel that the message was sent to.
                                                        //  This could be a TextChannel, PrivateChannel, or Group!        
        String msg = message.getContentDisplay();              //This returns a human readable version of the Message. Similar to
                                                        // what you would see in the client.

        boolean bot = author.isBot();                    //This boolean is useful to determine if the User that
                                                        // sent the Message is a BOT or not!

        if (event.isFromType(ChannelType.TEXT))         //If this message was sent to a Guild TextChannel
        {
            //Because we now know that this message was sent in a Guild, we can do guild specific things
            // Note, if you don't check the ChannelType before using these methods, they might return null due
            // the message possibly not being from a Guild!

            TextChannel textChannel = event.getTextChannel(); //The TextChannel that this message was sent to.

            String name;
            if (message.isWebhookMessage())
            {
                name = author.getName();                //If this is a Webhook message, then there is no Member associated
            }                                           // with the User, thus we default to the author for name.
            else
            {
                name = member.getEffectiveName();       //This will either use the Member's nickname if they have one,
            }                                           // otherwise it will default to their username. (User#getName())

            System.out.printf("(%s)[%s]<%s>: %s\n", guild.getName(), textChannel.getName(), name, msg);
        }
        else if (event.isFromType(ChannelType.PRIVATE)) //If this message was sent to a PrivateChannel
        {
            //The message was sent in a PrivateChannel.
            //In this example we don't directly use the privateChannel, however, be sure, there are uses for it!
            PrivateChannel privateChannel = event.getPrivateChannel();

            System.out.printf("[PRIV]<%s>: %s\n", author.getName(), msg);
        }

        //Now that you have a grasp on the things that you might see in an event, specifically MessageReceivedEvent,
        // we will look at sending / responding to messages!
        //This will be an extremely simplified example of command processing.

        //Remember, in all of these .equals checks it is actually comparing
        // message.getContentDisplay().equals, which is comparing a string to a string.
        // If you did message.equals() it will fail because you would be comparing a Message to a String!
        
        
        /************* 직접 구현한 기능들 *************/
        
        
        if (bot)
        	return ;
        
        // 유저 활동 통계 데이터 파기 체크를 위한 체크. 연달아서 같은 사람이 특정 메세지를 보내지 않는경우 초기화
        if (userActivityStatisticDeleteChecker_id != 0) {
        	if (member.getIdLong() == userActivityStatisticDeleteChecker_id && msg.equals("활동통계 파기")) {
        		UserActivityStatisticController.deleteUserActivityData(member.getUser(), guild, channel);
        	}
        	else
                channel.sendMessage("활통 통계 데이터 파기 절차가 취소되었습니다")
                .queue();
    		userActivityStatisticDeleteChecker_id = 0;
        }
        	
        
        if (msg.equals("하이"))	
        {
            channel.sendMessage("해-위")
                   .queue();
        }
        
        /************* 뽑기 관련 기능 ***************/
        else if (msg.startsWith("!뽑기"))
        {
        	String[] argList = extractArgs(msg);
        	if (argList != null) {
	        	if (argList.length == 1 && argList[0].equals("멤버")) {
	        		if (isAttending)
	        			channel.sendMessage("현재 멤버뽑기가 진행중입니다\n"
	        					+ "참가 멤버 : " + argstoLine(participants, ""))
	        					.queue();
	        		else {
		        		isAttending = true;
		        		channel.sendMessage("뽑기 참가신청을 받습니다. \" !참가! \" 를 입력해주세요.\n"
		        				+ "뽑기를 시작하려면 \" !참가종료! \" 를 입력해주세요")
		        				.queue();
	        		}
	        	}
	        	else {
		        	Random rand = ThreadLocalRandom.current();
		        	int index = rand.nextInt(argList.length-1) + 1;
		        	
		        	channel.sendMessage("'" + argstoLine(argList, "") + "' 중 \n" + argList[index] + "\n이/가 뽑혔습니다.")
        			.queue();
        	
	        	}
        	}
        	else
	        	channel.sendMessage("인자를 입력해 주세요")
    					.queue();
        }
        else if (msg.equals("!참가!")) {
        	if (isAttending) {
        		participants.add(message.getAuthor().getName());
        		channel.sendMessage(message.getAuthor().getName() + "님이 참가하셨습니다")
        				.queue();
        		channel.sendMessage("현재 참가 멤버 : " + argstoLine(participants, ""))
				.queue();
     
        	}
        	
        }
        else if (msg.equals("!참가종료!")) {
        	if (isAttending) {
	        	Random rand = ThreadLocalRandom.current();
	        	int index;
	        	if (participants.size() != 1)
	        		index = rand.nextInt(participants.size()-1) + 1;
	        	else
	        		index = 0;
	        	
	        	channel.sendMessage("'" + argstoLine(participants, "") + "' 중 \n" + participants.get(index) + "\n이/가 뽑혔습니다.")
	        			.queue();
	        	
	        	isAttending = false;
	        	participants.clear();
        	}
        }
        else if (msg.startsWith("!멤버"))
        {
        	ArchivesCommandController.showMembers(channel, guild);
        }
        
        /******************** 밈 명령어 관련 기능 **********************/
        else if (msg.matches(".*소라고동님.*")) {
        	ArchivesCommandController.requestSoragodong(channel);
        }
        else if (msg.startsWith("!밈"))
        {
        	String[] args = null;
        	if ((args = extractArgs(msg)) != null) {
    			if (args[0].equals("삭제")) {
	            	String command = args[1];
	            	MemeCmdController.deleteMemeCmd(command, channel, guild.getId());
	        	}
        	}
        	else {

            	MemeCmdController.showMemeCmd(channel, guild.getId());
        	}
        }
        ///////////////////// 밈 파일 입출력 및 자동화/////////////////////////////////
        else if (!message.getAttachments().isEmpty() && !message.getContentRaw().isEmpty()){
        	Attachment attachment = message.getAttachments().get(0);
    		
    		int commandType = -1;
			String comment = message.getContentRaw();
        	if (comment.startsWith("!등록 ") && comment.length() > 4)
        		commandType = MEME_ADD;
	        else if (comment.startsWith("!수정 ") && comment.length() > 4)
	        	commandType = MEME_MODIFY;
    		
        	if (commandType == MEME_ADD || commandType == MEME_MODIFY){
        		
        		// 이미지 확장자 체크
            	boolean isExtentionValid = false;
	        	for (String extention : Constants.Extensions.IMG)
	        		if (extention.equals(attachment.getFileExtension()))
	        			isExtentionValid = true;
	        	
	        	if (!isExtentionValid) {
	        		channel.sendMessage("해당 확장자는 유효하지 않습니다.\n유효한 확장자 : " + argstoLine(Constants.Extensions.IMG, ",")).queue();
	        		return ;
	        	}
	        	
	        	// 명령어 타입에 따라 동작
            	String command = comment.substring(comment.indexOf(' ') + 1);
        		String fileName = attachment.getFileName();
        		
	        	if (commandType == MEME_ADD) {
	        		MemeCmdController.registerMemeCmd(command, fileName, channel, guild.getId(), attachment);		// 밈 명령어 등록	        			
	        	}
	        	else if (commandType == MEME_MODIFY) {			        		
	        		MemeCmdController.modifyMemeCmd(command, fileName, channel, guild.getId(), attachment);			// 밈 명령어 수정
	        	}
        	}
        }

        /******************** 구글 검색 링크 출력 **********************/
        else if (msg.startsWith("!검색 ")) {
        	String[] args = null;
        	if ((args = extractArgs(msg)) != null) {
    			if (args[0].equals("구글")) {
    	        	String searchUrl = "https://www.google.com/search?q=";
	            	String keyword = args[1];
		        	channel.sendMessage(searchUrl + keyword)
		        			.queue();
	        	}
            	else if (args[0].equals("유튜브")) {
    	        	String searchUrl = "https://www.youtube.com/results?search_query=";
                	String keyword = args[1];
    	        	channel.sendMessage(searchUrl + keyword)
    	        			.queue();
            	}
        	}
        }

        /******************** 유저 활동 통계 **********************/
        else if (msg.startsWith("!활동통계 ")) {
        	String[] args = null;
        	if ((args = extractArgs(msg)) != null) {
    			if (args[0].equals("시작"))
    	        	UserActivityStatisticController.joinUserActivityStatistic(member.getUser(), guild, channel);
    			else if (args[0].equals("일시중지"))
    	        	UserActivityStatisticController.pauseMonitoring(member, guild, channel);
    			else if (args[0].equals("재시작")) 
    	        	UserActivityStatisticController.resumeMonitoring(member, guild, channel);
    			else if (args[0].equals("파기")) {
    	        	channel.sendMessage("데이터를 정말로 파기하시려면 '활동통계 파기' 메세지를 바로 보내주세요. 다른 내용의 메세지를 보내거나 다른 사람이 메세지를 보내면 절차가 취소됩니다. ")
        			.queue();
    				userActivityStatisticDeleteChecker_id = member.getIdLong();
    			}
    			else  {
    				DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    				LocalDate date_from, date_to;
    				String status;
    				long totalMillisec;
    				ActivityPerDateData activityPerDateData;
    				File userActivityXlsx, chartPng;
    				boolean xlsxExportingFlag = false;
    				boolean chartExportingFlag = false;
    				
    				if (args[args.length - 1].toLowerCase().matches("xlsx"))
    					xlsxExportingFlag = true;
    				
    				if (args[0].matches("\\d{4}-\\d{2}-\\d{2}")) {
						
	    				if (args[1].matches("\\d{4}-\\d{2}-\\d{2}")) {	// 두 날짜 사이의 총 활동 시간 출력
	    					date_from = LocalDate.parse(args[0], dateFormat);
	    					date_to = LocalDate.parse(args[1], dateFormat);
	    					args[2] = translateArgFromKorToEng(args[2]);
	        				status = args[2];
	    					
	    					totalMillisec = UserActivityStatisticController.extractTermDatatoMillis(member, date_from, date_to, status, guild, channel);
	    					if (totalMillisec >= 0)
		        	        	channel.sendMessage(author.getName() + "님의 " + args[0] + "~" + args[1] + "의  " + args[2] + " 총 활동 시간은 '" + new CumulativeTime(totalMillisec).toString()  + "' 입니다.")
		            			.queue();
	    				}
	    				else {	// 해당 날짜 사이의 총 활동 시간 출력
	    					date_to = LocalDate.parse(args[0], dateFormat);
	    					args[1] = translateArgFromKorToEng(args[1]);
	    					status = args[1];
	    					
	    					totalMillisec = UserActivityStatisticController.extractDateDatatoMillis(member, date_to, status, guild, channel);
	    					if (totalMillisec >= 0)
	    	    	        	channel.sendMessage(author.getName() + "님의 " + args[0] + "의  " + args[1] + " 총 활동 시간은 '" + new CumulativeTime(totalMillisec).toString()  + "' 입니다.")
	    	        			.queue();
	    				}
    				}
	    			else if (args[0].matches("all") || args[0].matches("전체")) {
						args[1] = translateArgFromKorToEng(args[1]);
						status = args[1];
	    				activityPerDateData = UserActivityStatisticController.extractAllTermDatatoMillis(member, status, guild, channel);
	    				date_to = activityPerDateData.activityTimePerDateVector.lastElement().date;
	    				
	    				chartPng = UserActivityStatisticController.makeDayUserActivityChartPng(date_to, activityPerDateData, member, guild, channel);
	    				if (chartPng.exists())
	    					channel.sendFile(chartPng).queue();
	    				
	    				if (xlsxExportingFlag) {
		    				userActivityXlsx = UserActivityStatisticController.exportActivityDataToXlsx(member, activityPerDateData, guild, channel);
		    				if (userActivityXlsx.exists())
		    					channel.sendFile(userActivityXlsx).queue();
	    				}
    				}
    			}
        	}
        }

        
        else if (msg.equals("!업데이트 내용")) {
        	ArchivesCommandController.showUpdatedFunction(channel);
        }

        /******************** help **********************/
        else if (msg.startsWith("!도움")) {
        	String arg = null;
        	if (msg.contains(" "))
        		arg = msg.split(" ")[1];
        	ArchivesCommandController.showHelp(channel, arg);
        }
        /******************** 봇 재시작 및 종료 **********************/
        else if (msg.equals("!재부팅")) {
        	ArchivesCommandController.reboot(channel);
        }
        else if (msg.equals("!바이바이")) {
        	ArchivesCommandController.shutdown(channel);
        }
        
        
/*        	-- 사용되지 않는 기능
 			else if (msg.equals("!시작!")) {
        	String userName = event.getAuthor().getName();
        	if (!studyTime.containsKey(userName)) {
        		Timestamp now = new Timestamp(System.currentTimeMillis());
        		channel.sendMessage("공부 시작 : " + userName + " / " + now.toString());
        		studyTime.put(userName, now);
        	}
        }
        else if (msg.equals("!끝!")) {
        	String userName = event.getAuthor().getName();
        	if (studyTime.containsKey(userName)) {
        		Timestamp start = studyTime.get(userName);
        		Timestamp end = new Timestamp(System.currentTimeMillis());
        		CumulativeTime ct = new CumulativeTime(end.getTime() - start.getTime());
        		
        		StatisticController.updateUserData("test", event.getGuild(), event.getAuthor().getName(), ct.toString());
        		studyTime.remove(userName);
        	}
        }*/
        
        else if (msg.startsWith("!debug")) {
        	debugCommandManager.dispatchDebugCommand("!debug", message, channel, guild, member);
        	
/*        	String[] args = null;
        	if ((args = extractArgs(msg)) != null) {
    			if (args[0].equals("createMemeTable")) {
    				MemeCmdController.createMemeTable(guild);
	        	}
    			else if (args[0].equals("initDir")) {
    				ArchivesCommandController.initDir(guild.getId());
	        	}
        	}*/
        }

        /******************** 밈 명령어 모니터링 **********************/
        else {
        	MemeCmdController.executeMemeCmd(channel, message, guild.getId());
        }
    }


    private String translateArgFromKorToEng(String arg) {
		if (arg.matches("온라인"))
			return "online";
		else if (arg == "자리비움")
			return "idle";
		else if (arg == "다른용무중")
			return "do_not_disturb";
		
		return null;
    }
    
    private String[] extractArgs(String message) {
    	int arg_index = message.indexOf(" ") + 1;
    	if (arg_index < 1)
    		return null;
    	else
    		return message.substring(arg_index).split("\\s+");
    }
    private String argstoLine(String[] args, String partition) {
    	if (args.length > 0) {
    		String argsLine = new String(args[0]);
    		for (int i = 1; i < args.length; i++)
    			argsLine += partition + " " + args[i];
    		
    		return argsLine;
    	}
    	else
    		return null;
    }
    private String argstoLine(Vector<String> args, String partition) {
    	if (args.size() > 0) {
    		String argsLine = new String(args.elementAt(0));
    		for (int i = 1; i < args.size(); i++)
    			argsLine += partition + " " + args.elementAt(i);
    		
    		return argsLine;
    	}
    	else
    		return null;
    }
    private static void checkOption(String[] arg) {
    	for (int i = 0; i < arg.length; i++)
    	{
    		if (arg[i].charAt(0) != '-')
    			continue;
    		
    		switch(arg[i].substring(1)) {
    			case "n" :
    				noticeFlag = true;
    				System.out.println("noticeFlag On");
    				break;
    		}
    	}
    }
    
    
    static void initControllers() {
        NoticeController.jda = jda;
        ArchivesCommandController.jda = jda;
        ArchivesThreads.jda = jda;
        UserActivityStatisticController.jda = jda;
        
        List<Class<?>> controllers = new ArrayList<>();
        controllers.add(NoticeController.class);
        controllers.add(ArchivesCommandController.class);
        controllers.add(MemeCmdController.class);
        controllers.add(ArchivesThreads.class);
        controllers.add(UserActivityStatisticController.class);
        
        debugCommandManager = new DebugCommandManager(controllers);
    }
    
    static void initShutdownHook() {
        Runtime rt = Runtime.getRuntime();
        rt.addShutdownHook(
            new Thread() {
                public void run() {
                	UserActivityStatisticController.RecordEveryUsersCurrentStatus();
                	UserActivityStatisticController.RecordSystemOffStatus();
            }
        } );
    }
    
}
