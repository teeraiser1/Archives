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
	        MemeCmdController.loadMemeCmd();
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
	        		MemeCmdController.getMemeCmdList().clear();
	        		MemeCmdController.loadMemeCmd();
		        	channel.sendMessage("������Ʈ �Ǿ����ϴ�").queue();
	        	}
	        	else if (args[0].equals("����")) {
	            	String command = args[1];
	            	MemeCmdController.deleteMemeCmd(command, channel);
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
		        		MemeCmdController.registerMemeCmd(command, fileName, channel, attachment);			        			
		        	}
		        	else if (commandType == MEME_MODIFY) {			        		
		        		MemeCmdController.modifyMemeCmd(command, fileName, channel, attachment);
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
        	MemeCmdController.executeMemeCmd(channel, msg);
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
            

    
    private void showMemeList(MessageChannel channel) {
    	String str = new String("");
    	for (int i = 0; i < MemeCmdController.getMemeCmdList().size(); i++)
    		str += MemeCmdController.getMemeCmdList().get(i).getCommand() + "\n";
    	
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

        	for (MemeCmd mcmd : MemeCmdController.getMemeCmdList())
        		if (mcmd.isCommandExpired())
        			MemeCmdController.deleteMemeCmd(mcmd.getCommand(), null);
    		this.run();
    		
    	}
    	
    }
    
}
