package Archives;
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
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

/* This project is based on JDA example "MessageListenerExample.java" */

public class Archives extends ListenerAdapter
{
	private boolean isAttending = false;
	private Vector<String> participants = new Vector<String>();
	private static Vector<MemeCmd> Memecmds = new Vector<MemeCmd>();
	
	private static String[] Extensions = {"jpg", "jpeg", "png", "gif"};
	
	private static int MEME_ADD = 0;
	private static int MEME_MODIFY = 1;
	private static int MUSIC_PLAY = 2;
	
	
	
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
            JDA jda = JDABuilder.createDefault(PrivateData.TOKEN) // The token of the account that is logging in.
                    .addEventListeners(new Archives())   // An instance of a class that will handle events.
                    .addEventListeners(new BotApplicationManager())
                    .build();
            jda.awaitReady(); // Blocking guarantees that JDA will be completely loaded.
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
        
        InitDir();
        ReadMemeData();
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
	        					+ "���� ��� : " + ArgstoLine(participants, ""))
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
		        	
		        	channel.sendMessage("'" + ArgstoLine(argList, "") + "' �� \n" + argList[index] + "\n��/�� �������ϴ�.")
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
        		channel.sendMessage("���� ���� ��� : " + ArgstoLine(participants, ""))
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
	        	
	        	channel.sendMessage("'" + ArgstoLine(participants, "") + "' �� \n" + participants.get(index) + "\n��/�� �������ϴ�.")
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
        
        /************* © ���� ��� ***************/
        else if (msg.startsWith("!©"))
        {
        	String[] args = null;
        	if ((args = extractArgs(msg)) != null) {
	        	if (args[0].equals("������Ʈ")) {
	        		Memecmds.clear();
	        		ReadMemeData();
		        	channel.sendMessage("������Ʈ �Ǿ����ϴ�")
		    			.queue();
	        	}
	        	else if (args[0].equals("����")) {
	            	String command = args[1];
	            	 boolean isCommandExist = false;
	            	
	            	for (MemeCmd cmd : Memecmds) {
	            		if (cmd.getCommand().equals(command)) {
	            			if (new File(cmd.getFileName()).delete()) {
				            	isCommandExist = true;
				            	Memecmds.remove(cmd);
				            	RemoveMemeData(cmd.getCommand());
	            				channel.sendMessage("�̹����� ���������� �����Ǿ����ϴ�.")
	            						.queue();
	            				System.out.println("Removed image " + cmd.getFileName());
	            			}
	            			break;
	            		}
	            	}
	            	if (!isCommandExist)
        				channel.sendMessage("�ش� �̹����� �������� �ʽ��ϴ�." + command)
						.queue();
	        	}
        	}
        	else {
	        	String str = new String("");
	        	for (int i = 0; i < Memecmds.size(); i++)
	        		str += Memecmds.get(i).getCommand() + "\n";
	        	
	        	channel.sendMessage(str)
	    			.queue();
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
	        else if (comment.equals("!���"))
	        	commandType = MUSIC_PLAY;
    		
        	if (commandType == MEME_ADD || commandType == MEME_MODIFY){
            	boolean isExtentionValid = false;
        		boolean isCommandExist = false;
        		
	        	for (String extention : Extensions)
	        		if (extention.equals(attachment.getFileExtension())) {
	        			isExtentionValid = true;
			        	if (commandType == MEME_ADD) {
			            	String command = comment.substring(comment.indexOf(' ') + 1);
			        		String fileName = attachment.getFileName();
			            	isCommandExist = false;
			            	
			        		for (MemeCmd cmd : Memecmds) {
			            		if (cmd.getCommand().equals(command)) {
			            			isCommandExist = true;
			            			break;
			            		}
			            	}
			        		if (!isCommandExist) {
				        		Memecmds.add(new MemeCmd(command, fileName));
				        		AddMemeData(command, fileName);
				            	attachment.downloadToFile(Constants.Files.MEME_PATH + fileName)
								.thenAccept(file -> System.out.println("Saved attachment to " + file.getName()));
			        			channel.sendMessage("���������� �̹����� ��ϵǾ����ϴ�")
	        							.queue();
			        		}
			        		else
			        			channel.sendMessage("�̹� �ش� ��ɾ��� �̹����� �����մϴ�")
			        					.queue();
			        			
			        	}
			        	else if (commandType == MEME_MODIFY) {
			            	String command = comment.substring(comment.indexOf(' ') + 1);
			        		String fileName = attachment.getFileName();
			            	
			            	for (MemeCmd cmd : Memecmds) {
			            		if (cmd.getCommand().equals(command)) {
			            			if (new File(Constants.Files.MEME_PATH + cmd.getFileName()).delete()) {
			    		            	attachment.downloadToFile(Constants.Files.MEME_PATH + fileName)
			    						.thenAccept(file -> System.out.println("Saved attachment to " + file.getName()));
			            				
			    		        		Memecmds.add(new MemeCmd(command, fileName));
			            				Memecmds.remove(cmd);
			            				ModifyMemeData(command, fileName);
			            				
			            				channel.sendMessage("�̹����� ���������� ����Ǿ����ϴ�.")
			            						.queue();
			            				System.out.println("Changed image from \"" + cmd.getFileName() + "\" to \"" + fileName + "\"");
			            			}
			            			break;
			            		}
			            	}
			        		
			        	}
			        	break;
	        		}
	        	if (!isExtentionValid)
	        		channel.sendMessage("�ش� Ȯ���ڴ� ��ȿ���� �ʽ��ϴ�.\n��ȿ�� Ȯ���� : " + ArgstoLine(Extensions, ","))
	        				.queue();
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
        	channel.sendMessage("!�̱� a b c ...			a b c ... �� �ϳ� ����\n")
						.append("!�̱� ���				������ �̱�. !����! �� ���� �����ϰ� !��������! �� ������ �� �Ѹ� ����\n")
						.append("\n")
						.append("!���					���� ��� ǥ�� (� �������� �� �𸣰�...\n")
						.append("!�˻� <�˻���>				���� �˻�\n")
						.append("\n")
						.append("!©						���� ��ϵ� © ����Ʈ ���\n")
						.append("!© ������Ʈ				���� ��ϵ� © ������Ʈ. �̹��� �־��µ� ���� �� ���� ������ �ϱ�\n")
						.append("!© ���� <© �̸�>			�ش� © ����\n")
						.append("\n")
						.append("© ��� : ©�� �߰��ϰ���� ���. ��ȭâ�� �̹����� �ø��� '�ø���' ��ư�� ������ ���� ��۷� '!�߰� <© �̸�>' �Է� �� �ø���\n")
						.append("\n")
						.append("© ���� : © �̸��� �ش��ϴ� �̹����� �����ϰ� ���� ���. ��ȭâ�� �̹����� �ø��� '�ø���' ��ư�� ������ ���� ��۷� '!���� <© �̸�>' �Է� �� �ø���\n")
						.queue();
        	System.out.println(Constants.Files.ROOT_PATH);
        }
        else if (msg.equals("!���̹���")) {
        	File targetFile = new File(Constants.Files.MEME_PATH + "terminated.gif");
        	channel.sendFile(targetFile).queue();
        	jda.getPresence().setStatus(OnlineStatus.OFFLINE);
        	try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	System.exit(0);
        }
        else {
        	for (MemeCmd cmd : Memecmds) {
        		if (cmd.getCommand().equals(msg)) {
        			File targetFile = new File(Constants.Files.MEME_PATH + cmd.getFileName());
        			if (targetFile.exists()) {
	                	channel.purgeMessagesById(channel.getLatestMessageId());
	                	channel.sendFile(targetFile)
	                			.queue();
        			}
        			else
        				channel.sendMessage("�ش� �̹����� �������� �ʽ��ϴ� " + targetFile.toString())
        						.queue();
        		}
        	}
        }
    }
    
 
    private String[] extractArgs(String message) {
    	int arg_index = message.indexOf(" ") + 1;
    	if (arg_index < 1)
    		return null;
    	else
    		return message.substring(arg_index).split(" ");
    }
    private String ArgstoLine(String[] args, String partition) {
    	if (args.length > 0) {
    		String argsLine = new String(args[0]);
    		for (int i = 1; i < args.length; i++)
    			argsLine += partition + " " + args[i];
    		
    		return argsLine;
    	}
    	else
    		return null;
    }
    private String ArgstoLine(Vector<String> args, String partition) {
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
    }
            
    private static void ReadMemeData() {
    	try {
    		File meme_path = new File(Constants.Files.MEME_PATH);
            if (!meme_path.exists()) {
            	meme_path.mkdir();
            }
            
    		File data_path = new File(Constants.Files.DATA_PATH);
            if (!data_path.exists()) {
            	data_path.mkdir();
            }
            
    		File meme_DataFile = new File(Constants.Files.MEME_DATAFILE);
            if (!meme_DataFile.exists()) {
            	meme_DataFile.createNewFile();
            }
            else {
            	BufferedReader bufferedReader = new BufferedReader(new FileReader(meme_DataFile));
            	String tmp = null;
            	while ((tmp = bufferedReader.readLine()) != null) {
            		if (!tmp.isEmpty())
            			Memecmds.add(new MemeCmd(tmp.substring(0, tmp.indexOf('\t')), tmp.substring(tmp.lastIndexOf('\t') + 1, tmp.length())));
            	}
            
            	bufferedReader.close();
            }
    	}
    	catch(FileNotFoundException e) {
    		System.out.println("FileNotFoundException occur : " + e.toString());
    	}
    	catch(IOException e) {
    		System.out.println("IOException occur : " + e.toString());
    	}
    }
    private static void AddMemeData(String command, String fileName) {
    	try {
        	BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(Constants.Files.MEME_DATAFILE), true));
        	bufferedWriter.write(MakeMemeCmdLine(command, fileName));
        	bufferedWriter.flush();
        	bufferedWriter.close();
    	}
    	catch(FileNotFoundException e) {
    		System.out.println("FileNotFoundException occur : " + e.toString());
    	}
    	catch(IOException e) {
    		System.out.println("IOException occur : " + e.toString());
    	}
    }
    private static void ModifyMemeData(String command, String fileName) {
    	try {
        	File originFile = new File(Constants.Files.MEME_DATAFILE);
    		File tmpFile = File.createTempFile("meme", "", new File(Constants.Files.DATA_PATH));
    		
			BufferedReader bufferedReader = new BufferedReader(new FileReader(originFile));
        	BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tmpFile, true));
        	String tmp = null;
        	while ((tmp = bufferedReader.readLine()) != null) {
        		if (!tmp.isEmpty()) {
	        		if (!tmp.subSequence(0,  tmp.indexOf('\t')).equals(command))
	            		bufferedWriter.write(tmp + "\r\n");
	        		else
	                	bufferedWriter.write(MakeMemeCmdLine(command, fileName));
        		}
        		
        	}
        	
        	bufferedReader.close();
        	bufferedWriter.flush();
        	bufferedWriter.close();
        	
        	originFile.delete();
        	tmpFile.renameTo(new File(Constants.Files.MEME_DATAFILE));
    	}
    	catch(FileNotFoundException e) {
    		System.out.println("FileNotFoundException occur : " + e.toString());
    	}
    	catch(IOException e) {
    		System.out.println("IOException occur : " + e.toString());
    	}
    }
    private static void RemoveMemeData(String command) {
    	try {
        	File originFile = new File(Constants.Files.MEME_DATAFILE);
    		File tmpFile = File.createTempFile("meme", "", new File(Constants.Files.DATA_PATH));
    		
			BufferedReader bufferedReader = new BufferedReader(new FileReader(originFile));
        	BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tmpFile, true));
        	String tmp = null;
        	while ((tmp = bufferedReader.readLine()) != null) {
        		if (!tmp.isEmpty() && !tmp.subSequence(0,  tmp.indexOf('\t')).equals(command))
            		bufferedWriter.write(tmp + "\r\n");
        	}
        	
        	bufferedReader.close();
        	bufferedWriter.flush();
        	bufferedWriter.close();
        	
        	originFile.delete();
        	tmpFile.renameTo(new File(Constants.Files.DATA_PATH + "meme.txt"));
    	}
    	catch(FileNotFoundException e) {
    		System.out.println("FileNotFoundException occur : " + e.toString());
    	}
    	catch(IOException e) {
    		System.out.println("IOException occur : " + e.toString());
    	}
    }
    private static String MakeMemeCmdLine(String command, String fileName) {
    	String buffer = command + "\t";
    	if (command.length() < 5)
    		buffer += "\t" ;
    	buffer += fileName + "\n";
    	
    	return buffer;
    }

      
    
}
