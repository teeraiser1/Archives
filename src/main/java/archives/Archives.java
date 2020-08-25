package main.java.archives;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.User;

import javax.security.auth.login.LoginException;

import com.sedmelluq.discord.lavaplayer.demo.BotApplicationManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

import java.sql.*;
import java.text.SimpleDateFormat;

/* This project is based on JDA example "MessageListenerExample.java" */

public class Archives extends ListenerAdapter
{
	private static JDA jda;
	private boolean isAttending = false;
	private Vector<String> participants = new Vector<String>();
	private static Vector<MemeCmd> Memecmds = new Vector<MemeCmd>();
	
	
	private static int MEME_ADD = 0;
	private static int MEME_MODIFY = 1;
	
	
	
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
            jda = JDABuilder.createDefault(PrivateData.TOKEN) // The token of the account that is logging in.
                    .addEventListeners(new Archives())   // An instance of a class that will handle events.
                    .addEventListeners(new BotApplicationManager())
                    .build();
            jda.awaitReady(); // Blocking guarantees that JDA will be completely loaded.

            Class.forName("com.mysql.cj.jdbc.Driver");
			System.out.println("Loaded JDBC Driver");

	        InitDir();
	        loadMemeCmd();
	        ExpiredMemeCheckThread checker = new ExpiredMemeCheckThread();
	        checker.start();
            
            System.out.println("Finished Building JDA!");
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
    public void onMessageReceived(MessageReceivedEvent event)
    {
        //These are provided with every event in JDA
        JDA jda = event.getJDA();                       //JDA, the core of the api.
        long responseNumber = event.getResponseNumber();//The amount of discord events that JDA has received since the last reconnect.

        //Event specific information
        User author = event.getAuthor();                //The user that sent the message
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

            Guild guild = event.getGuild();             //The Guild that this message was sent in. (note, in the API, Guilds are Servers)
            TextChannel textChannel = event.getTextChannel(); //The TextChannel that this message was sent to.
            Member member = event.getMember();          //This Member that sent the message. Contains Guild specific information about the User!

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
        
        if (bot) {}
        else if (msg.equals("����"))
        {
            channel.sendMessage("��-��")
                   .queue();
        }
        
        /************* �̱� ���� ��� ***************/
        else if (msg.startsWith("!�̱�"))
        {
        	String[] argList = extractArgs(msg);
        	if (argList != null) {
	        	if (argList.length == 1 && argList[0].equals("���")) {
	        		if (isAttending)
	        			channel.sendMessage("���� ����̱Ⱑ �������Դϴ�\n"
	        					+ "���� ��� : " + argstoLine(participants, ""))
	        					.queue();
	        		else {
		        		isAttending = true;
		        		channel.sendMessage("�̱� ������û�� �޽��ϴ�. \" !����! \" �� �Է����ּ���.\n"
		        				+ "�̱⸦ �����Ϸ��� \" !��������! \" �� �Է����ּ���")
		        				.queue();
	        		}
	        	}
	        	else {
		        	Random rand = ThreadLocalRandom.current();
		        	int index = rand.nextInt(argList.length-1) + 1;
		        	
		        	channel.sendMessage("'" + argstoLine(argList, "") + "' �� \n" + argList[index] + "\n��/�� �������ϴ�.")
        			.queue();
        	
	        	}
        	}
        	else
	        	channel.sendMessage("���ڸ� �Է��� �ּ���")
    					.queue();
        }
        else if (msg.equals("!����!")) {
        	if (isAttending) {
        		participants.add(message.getAuthor().getName());
        		channel.sendMessage(message.getAuthor().getName() + "���� �����ϼ̽��ϴ�")
        				.queue();
        		channel.sendMessage("���� ���� ��� : " + argstoLine(participants, ""))
				.queue();
     
        	}
        	
        }
        else if (msg.equals("!��������!")) {
        	if (isAttending) {
	        	Random rand = ThreadLocalRandom.current();
	        	int index;
	        	if (participants.size() != 1)
	        		index = rand.nextInt(participants.size()-1) + 1;
	        	else
	        		index = 0;
	        	
	        	channel.sendMessage("'" + argstoLine(participants, "") + "' �� \n" + participants.get(index) + "\n��/�� �������ϴ�.")
	        			.queue();
	        	
	        	isAttending = false;
	        	participants.clear();
        	}
        }
        else if (msg.startsWith("!���"))
        {
        	List<Member> members = event.getGuild().getMembers();
        	for (Member mb : members)
        	{
        		channel.sendMessage(mb.getEffectiveName())
        			.queue();
        	}
        }
        
        /******************** © ���� ��� **********************/
        else if (msg.startsWith("!©"))
        {
        	String[] args = null;
        	if ((args = extractArgs(msg)) != null) {
	        	if (args[0].equals("������Ʈ")) {
	        		Memecmds.clear();
	        		loadMemeCmd();
		        	channel.sendMessage("������Ʈ �Ǿ����ϴ�").queue();
	        	}
	        	else if (args[0].equals("����")) {
	            	String command = args[1];
	            	deleteMemeCmd(command, channel);
	        	}
        	}
        	else {
        		showMemeList(channel);
        	}
        }
        else if (msg.matches(".*�Ҷ����.*")) {
        	channel.sendFile(new File(Constants.Files.MEME_PATH + "soragodong_qst.jpeg"))
        			.queue();
        	channel.sendFile(new File(Constants.Files.MEME_PATH + "soragodong_ans.jpeg"))
        			.queue();

        	Random rand = ThreadLocalRandom.current();
        	if (rand.nextInt(2) == 0)
	        	channel.sendMessage("��                                        �ƤѴ�                                       ��")
	        			.queue();
        	else
	        	channel.sendMessage("��                                           ��.                                           ��")
    			.queue();
        		
        }
        ///////////////////// © ���� ����� �� �ڵ�ȭ/////////////////////////////////
        else if (!message.getAttachments().isEmpty() && !message.getContentRaw().isEmpty()){
        	Attachment attachment = message.getAttachments().get(0);
    		
    		int commandType = -1;
			String comment = message.getContentRaw();
        	if (comment.startsWith("!�߰� ") && comment.length() > 4)
        		commandType = MEME_ADD;
	        else if (comment.startsWith("!���� ") && comment.length() > 4)
	        	commandType = MEME_MODIFY;
    		
        	if (commandType == MEME_ADD || commandType == MEME_MODIFY){
            	boolean isExtentionValid = false;
            	
	        	for (String extention : Constants.Extensions.IMG)
	        		if (extention.equals(attachment.getFileExtension()))
	        			isExtentionValid = true;
	        	
	        	if (isExtentionValid) {
            	String command = comment.substring(comment.indexOf(' ') + 1);
        		String fileName = attachment.getFileName();
        		
		        	if (commandType == MEME_ADD) {
		        		registerMemeCmd(command, fileName, channel, attachment);			        			
		        	}
		        	else if (commandType == MEME_MODIFY) {			        		
	    				modifyMemeCmd(command, fileName, channel, attachment);
		        	}
	        	}
	        	else
	        		channel.sendMessage("�ش� Ȯ���ڴ� ��ȿ���� �ʽ��ϴ�.\n��ȿ�� Ȯ���� : " + argstoLine(Constants.Extensions.IMG, ",")).queue();
        	}
        }
        
        else if (msg.startsWith("!�˻� ")) {
        	String searchUrl = "https://www.google.com/search?q=";
        	String keyword = msg.substring(msg.indexOf(' ') + 1);
        	keyword = keyword.replaceAll(" ", "+");
        	channel.sendMessage(searchUrl + keyword)
        			.queue();
        }
        else if (msg.equals("!����")) {
        	showHelp(channel);
        }
        else if (msg.equals("!���̹���")) {
        	shutdown(channel);
        }
        else {
        	executeMemeCmd(channel, msg);
        }
    }
 
    
    
    private String[] extractArgs(String message) {
    	int arg_index = message.indexOf(" ") + 1;
    	if (arg_index < 1)
    		return null;
    	else
    		return message.substring(arg_index).split(" ");
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

    private static void InitDir() {
		File meme_path = new File(Constants.Files.MEME_PATH);
        if (!meme_path.exists()) {
        	meme_path.mkdir();
        }
        
		File data_path = new File(Constants.Files.DATA_PATH);
        if (!data_path.exists()) {
        	data_path.mkdir();
        }

		System.out.println("Essensial file init success");
    }
            
    private static void loadMemeCmd() {
        Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
    	try {            
            conn = DriverManager.getConnection(PrivateData.DB.url, PrivateData.DB.root, PrivateData.DB.pass);
    		stmt = conn.createStatement();
    		rs = stmt.executeQuery("SELECT command, path, date_format(atime, '%Y-%m-%d') FROM meme");

            while (rs.next()) {
            	String command = rs.getString(1);
            	String path = rs.getString(2);
            	String atime_s = rs.getString(3);
            	Date atime = Date.valueOf(atime_s);
    			Memecmds.add(new MemeCmd(command, path, atime));
            }
			if (conn != null && !conn.isClosed())
				conn.close();
			System.out.println("Meme data load success");
    	}
    	catch (SQLException e) {
    		System.out.println("Meme load fail : " + e.toString());
			e.printStackTrace();
		}
    }
    private static void registerMemeCmd(String command, String fileName, MessageChannel channel, Attachment image) {
        Connection conn = null;
        Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
            conn = DriverManager.getConnection(PrivateData.DB.url, PrivateData.DB.root, PrivateData.DB.pass);

            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT command FROM meme WHERE command = '" + command + "'");
            if (rs.next()) {
    			channel.sendMessage("�ش� ��ɾ �̹� �����մϴ�").queue();
            	throw new SQLException();
            }

            rs = stmt.executeQuery("SELECT path FROM meme WHERE path = '" + fileName + "'");
            if (rs.next()) {
    			channel.sendMessage("�ش� �̸��� ������ �̹����� �̹� �����մϴ�. �̹������� �ٲ��ּ���.").queue();
            	throw new SQLException();
            }
    		
			String atime_s = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
            pstmt = conn.prepareStatement("INSERT INTO meme VALUES (?,?,?)");

            pstmt.setString(1, command);
            pstmt.setString(2, fileName);
            pstmt.setString(3, atime_s);
            
            int count = pstmt.executeUpdate();
            if(count == 1){
    			System.out.println("Meme register success : " + command);

    			Memecmds.add(new MemeCmd(command, fileName, new Date(System.currentTimeMillis())));
    	    	image.downloadToFile(Constants.Files.MEME_PATH + fileName)
    			.thenAccept(file -> System.out.println("Saved attachment to " + file.getName()));
    	    	
    			channel.sendMessage("���������� �̹����� ��ϵǾ����ϴ�").queue();
            }
            else{
    			System.out.println("Meme register error : " + command);
            }
		} 
		catch (SQLException e) {
			Logger.writeLog("log.txt", "�̹��� �߰��� �����Ͽ����ϴ�. -> " + command);
    		channel.sendMessage("�̹��� �߰��� �����Ͽ����ϴ�.").queue();
			System.out.println("Meme register fail : " + e.toString());
			e.printStackTrace();
		}
		finally {
			try {
				if (conn != null && !conn.isClosed())
					conn.close();
			} catch (SQLException e) {
				System.out.println("connection close fail : " + e.toString());
				e.printStackTrace();
			}
		}
    }
    private static void modifyMemeCmd(String command, String fileName, MessageChannel channel, Attachment image) {
        Connection conn = null;
        Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean isModified = false;
		
		try {
            conn = DriverManager.getConnection(PrivateData.DB.url, PrivateData.DB.root, PrivateData.DB.pass);
            
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT path FROM meme WHERE path = '" + fileName + "'");
            if (rs.next()) {
    			channel.sendMessage("�ش� �̸��� ������ �̹����� �̹� �����մϴ�. �̹������� �ٲ��ּ���.").queue();
            	throw new SQLException();
            }
            
            pstmt = conn.prepareStatement("UPDATE meme SET path = ? WHERE command = ?");
            pstmt.setString(1, fileName);
            pstmt.setString(2, command);
            
            int count = pstmt.executeUpdate();
            if (count == 0) {
        		channel.sendMessage("�ش� Ŀ�ǵ尡 �������� �ʽ��ϴ�.").queue();
        		throw new SQLException();
            }
            else if(count == 1){
    			System.out.println("Meme modify success : " + command);
    			isModified = true;
            }
            else{
    			Logger.writeLog("log.txt", "�̹��� ���� �� ������ �߻��߽��ϴ�. -> " + command);
        		channel.sendMessage("�̹��� ���� �� ������ �߻��߽��ϴ�.").queue();
        		System.out.println("Meme modify SQL fail : " + command);
        		throw new SQLException();
            }
		} 
		catch (SQLException e) {
    		Logger.writeLog("log.txt", "�̹��� ������ �����Ͽ����ϴ�. -> " + command);
    		channel.sendMessage("�̹��� ������ �����Ͽ����ϴ�.").queue();
			System.out.println("Meme modify fail : " + e.toString());
			e.printStackTrace();
		}
		finally {
			try {
				if (conn != null && !conn.isClosed())
					conn.close();
			} catch (SQLException e) {
				System.out.println("connection close fail : " + e.toString());
				e.printStackTrace();
			}
		}

		if (isModified) {
			MemeCmd targetCmd = findMemeCmd(command);
			if (new File(Constants.Files.MEME_PATH + targetCmd.getPath()).delete()) {
				image.downloadToFile(Constants.Files.MEME_PATH + fileName)
				.thenAccept(file -> System.out.println("Saved attachment to " + file.getName()));
				
				targetCmd.setPath(fileName);
			}
			
			updateMemeCmdAtime(targetCmd);
		}
		channel.sendMessage("�̹����� ���������� ����Ǿ����ϴ�.").queue();
    }
    private static void deleteMemeCmd(String command, MessageChannel channel) {
	    Connection conn = null;
		PreparedStatement pstmt = null;
		boolean isDeleted = false;
		
    	try {
            conn = DriverManager.getConnection(PrivateData.DB.url, PrivateData.DB.root, PrivateData.DB.pass);
            pstmt = conn.prepareStatement("DELETE FROM meme WHERE command = ?");
            pstmt.setString(1, command);
            
            int count = pstmt.executeUpdate();
            if (count == 0) {
        		Logger.writeLog("log.txt", "�ش� ��ɾ �������� �ʽ��ϴ�. -> " + command);
    			System.out.println("Delete Meme success : " + command);
            	if (channel != null)
	    			channel.sendMessage("�ش� ��ɾ �������� �ʽ��ϴ�. -> " + command).queue();
            	
    			throw new SQLException();
            }
            else if(count == 1){
        		Logger.writeLog("log.txt", "������ �ʴ� ©�� �����Ǿ����ϴ�. -> " + command);
	    		System.out.println("Delete Unused meme command success : " + command);
            	if (channel != null)
    	    		channel.sendMessage("������ �ʴ� ©�� �����Ǿ����ϴ�. -> " + command).queue();
            	
    			isDeleted = true;
            }
            else{
        		Logger.writeLog("log.txt", "�̹��� ���� �� ������ �߻��߽��ϴ�. -> " + command);
    			System.out.println("Delete Unused meme command fail : " + command);
        		if (channel != null)
            		channel.sendMessage("�̹��� ���� �� ������ �߻��߽��ϴ�.").queue();
        		
    			throw new SQLException();
            }            
    	}
    	catch (SQLException e) {
    		Logger.writeLog("log.txt", "�̹��� ������ �����Ͽ����ϴ�. -> " + command);
    		channel.sendMessage("�̹��� ������ �����Ͽ����ϴ�.").queue();
			System.out.println("Delete meme fail : " + e.toString());
			e.printStackTrace();
		}
		finally {
			try {
				if (conn != null && !conn.isClosed())
					conn.close();
			}
			catch (SQLException e) {
				System.out.println("connection close fail : " + e.toString());
				e.printStackTrace();
			}
		}
    	

    	if (isDeleted) {
    		MemeCmd targetCmd = findMemeCmd(command);
    		
   			if (new File(Constants.Files.MEME_PATH + targetCmd.getPath()).delete()) {
	            Memecmds.remove(targetCmd);
	    		Logger.writeLog("log.txt", "�̹����� ���������� �����Ǿ����ϴ�. -> " + command);
   				channel.sendMessage("�̹����� ���������� �����Ǿ����ϴ�.").queue();
   				System.out.println("Removed image " + targetCmd.getPath());
   			}
   			else {
	    		Logger.writeLog("log.txt", "�̹��� ���� �� ������ �߻��߽��ϴ�.(�̹��� ���� ���� ����) -> " + command);
   				channel.sendMessage("�̹��� ���� �� ������ �߻��߽��ϴ�.(�̹��� ���� ���� ����)").queue();
   				System.out.println("Delete image error : " + targetCmd.getPath());
   			}
       				
    	}
    }
    private void executeMemeCmd(MessageChannel channel, String command) {
    	MemeCmd targetCmd = findMemeCmd(command);
    	if (targetCmd == null)
    		return;
    	
		File targetFile = new File(Constants.Files.MEME_PATH + targetCmd.getPath());
		
		if (targetFile.exists()) {
        	channel.purgeMessagesById(channel.getLatestMessageId());
        	channel.sendFile(targetFile)
        			.queue();
        	updateMemeCmdAtime(targetCmd);
		}
		else
			channel.sendMessage("�ش� �̹����� �������� �ʽ��ϴ� " + targetFile.toString())
					.queue();
    }
    private static void updateMemeCmdAtime(MemeCmd mcmd) {
    	Connection conn = null;
    	PreparedStatement pstmt = null;
    	boolean isUpdated = false;
    	try {
			conn = DriverManager.getConnection(PrivateData.DB.url, PrivateData.DB.root, PrivateData.DB.pass);

			String atime_s = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
	    	pstmt = conn.prepareStatement("UPDATE meme SET atime = ? WHERE command = ?");
	    	pstmt.setString(1, atime_s);
	    	pstmt.setString(2, mcmd.getCommand());

	    	int count = pstmt.executeUpdate();
	    	if (count == 0) {
	    		System.out.println("Update atime error occur : Cannot find target command " + mcmd.getCommand());
	    	}
	    	if (count == 1) {
	    		System.out.println("Updated success : " + mcmd.getCommand());
	    		isUpdated = true;
	    	}
	    	else {
	    		System.out.println("Update atime error occur : sql error" + mcmd.getCommand());
	    	}
	    	
		} catch (SQLException e) {
			System.out.println("SQLException occur : " + e.toString());
			e.printStackTrace();
		}
    	finally {
    		try {
				if (conn != null && !conn.isClosed())
					conn.close();
			} catch (SQLException e) {
				System.out.println("SQLException occur : " + e.toString());
				e.printStackTrace();
			}
    	}
    	
    	if (isUpdated) {
	    	MemeCmd targetCmd = findMemeCmd(mcmd.getCommand());
	    	targetCmd.updateAtime();
    	}
    }
    private static MemeCmd findMemeCmd(String command) {
    	for (MemeCmd cmd : Memecmds)
    		if (cmd.getCommand().equals(command))
    			return cmd;
    	return null;
    }
    
    private void showMemeList(MessageChannel channel) {
    	String str = new String("");
    	for (int i = 0; i < Memecmds.size(); i++)
    		str += Memecmds.get(i).getCommand() + "\n";
    	
    	channel.sendMessage(str).queue();
    }
    private void showHelp(MessageChannel channel) {
    	StringBuilder builder = new StringBuilder();
    	builder.append("!�̱� a b c ...\n")
		    	.append("    : a b c ... �� �ϳ� ����\n")
				.append("!�̱� ���\n")
				.append("    : ������ �̱�. !����! �� ���� �����ϰ� !��������! �� ������ �� �Ѹ� ����\n")
				.append("\n")
				.append("!���\n")
				.append("    : ��� ǥ�� (� �������� �� �𸣰�...\n")
				.append("!�˻�\n")
				.append("    : ���� �˻�\n")
				.append("\n")
				.append("!©\n")
				.append("    : ���� ��ϵ� © ����Ʈ ���\n")
				.append("!© ������Ʈ\n")
				.append("    : ���� ��ϵ� © ������Ʈ. �̹��� �־��µ� ���� �� ���� ������ �ϱ�\n")
				.append("!© ���� ©�̸�	\n")
				.append("    : �ش� © ����\n")
				.append("���� �ø��� ��ۿ� !�߰� ©�̸�\n")
				.append("    : © ���\n")
				.append("���� �ø��� ��ۿ� !���� ©�̸�\n")
				.append("    : © ����\n")
				.append("\n")
				.append("!@p ��ũ\n")
				.append("    : ���� �߰�(��Ʃ��)\n")
				.append("���� �ø��� ��ۿ� !@p m\n")
				.append("    : ���� �߰�(���ε� ����)\n")
				.append("!@s\n")
				.append("    : ���� ��ŵ\n")
				.append("!@list\n")
				.append("    : ���� ������ ����Ʈ\n")
				.append("!@now ��ũ\n")
				.append("    : ���� �ٷ� ���\n")
				.append("!@pause\n")
				.append("    : ���� �Ͻ�����\n")
				.append("!@resume\n")
				.append("    : ���� ���\n")
				.append("!@v ����\n")
				.append("    : ���� ����\n");
		channel.sendMessage(builder).queue();
    }
    private void shutdown(MessageChannel channel) {
    	File targetFile = new File(Constants.Files.MEME_PATH + "terminated.gif");
    	channel.sendFile(targetFile).queue();
    	jda.getPresence().setStatus(OnlineStatus.OFFLINE);
    	
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			System.out.println("sleep fail : " + e.toString());
			e.printStackTrace();
		}
    	System.exit(0);
    }


    static class ExpiredMemeCheckThread extends Thread {    	
    	
    	public void run() {
    		Date now_d = new Date(System.currentTimeMillis());
    		
    		Calendar now_c = Calendar.getInstance();
    		now_c.setTime(now_d);
    		Calendar midnight = Calendar.getInstance();
    	
    		midnight.set(now_c.get(Calendar.YEAR), now_c.get(Calendar.MONTH), now_c.get(Calendar.DAY_OF_MONTH), now_c.get(Calendar.HOUR_OF_DAY) + 1, 0, 0);
    		try {
				Thread.sleep(midnight.getTime().getTime() - now_d.getTime());
			} catch (InterruptedException e) {
				System.out.println("InterruptedException : " + e.toString());
				e.printStackTrace();
			}

        	for (MemeCmd mcmd : Memecmds)
        		if (mcmd.isCommandExpired())
        			deleteMemeCmd(mcmd.getCommand(), null);
    		this.run();
    		
    	}
    	
    }
    
}
