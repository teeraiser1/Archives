package main.java.archives;

import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import discord4j.core.object.entity.TextChannel;
import main.java.archives.MemeCmdController.MemeCmd;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;

public class MemeCmdController {
//	private static List<MemeCmd> Memecmds = new Vector<MemeCmd>();  -- �� ��ɾ��� ���� ��� ����(�޸� -> DB)���� �̻��
	
	public static class MemeCmd {
		private String command;
    	private String path;
    	private Date atime;
    	
    	public MemeCmd(String command, String path, Date atime) {
    		this.command = command;
    		this.path = path;
    		this.atime = atime;
		}
    	public MemeCmd(String[] memeCmd) {
    		this.command = memeCmd[0];
    		this.path = memeCmd[1];
		}
		public String getCommand() {
    		return command;
    	}
    	public String getPath() {
    		return path;
    	}
    	public String setPath(String path) {
    		return this.path = path;
    	}
    	public Date getAtime() {
    		return atime;
    	}
    	public boolean isCommandExpired() {		// ������ ��� �ð����κ��� 3�ְ� ������ �ʾҴٸ� false ����
    		int days = 1000*60*60*24;
    		Date curtime = new Date(System.currentTimeMillis() - Constants.Max.EXPIRATION_DATE*days);
    		return atime.before(curtime);
    	}
    	public void updateAtime() {
    		atime = new Date(System.currentTimeMillis());
    	}
	}
	
	public static class ActionResult {
		String type_log = null;
		String reason_log = null;
		String type_channel = null;
		String reason_channel = null;
	}
/*	// �� �ε�  -- ���� ���� ��� ����(�޸� -> DB)���� �̻��
	// DB�� Meme ���̺��� ��� ������ MemeCmd ��ü�� ���� �� Memecmds ����Ʈ�� �߰�
    public static void loadMemeCmd() {
        Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
    	try {            
            conn = DriverManager.getConnection(PrivateData.DB.url_meme, PrivateData.DB.root, PrivateData.DB.pass);
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
    }*/
    
    // �� ��ɾ� ���
    // DB�� ��ɾ�, ���ϸ� �ߺ� üũ �� DB�� �߰�
    public static void registerMemeCmd(String command, String fileName, MessageChannel channel, String guildID, Attachment image) {		// ©�� �߰��ϴ�
        Connection conn = null;
        Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int ret;
		
		String tableName = "meme." + guildID;
		ActionResult ar = new ActionResult();
    	boolean isDownloadSuccess = false;
		
		try {
            conn = DriverManager.getConnection(PrivateData.DB.url_meme, PrivateData.DB.root, PrivateData.DB.pass);

            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT command FROM " + tableName + " WHERE command = '" + command + "'");
            if (rs.next()) {
            	ar.type_log = "Register meme fail : " + command;
            	ar.reason_log = "meme command exist";
            	ar.type_channel = "�� ��ɾ� ��Ͽ� �����Ͽ����ϴ�.";
            	ar.reason_channel = "�ش� ��ɾ �̹� �����մϴ�.";
            	
            	throw new SQLException();
            }

            rs = stmt.executeQuery("SELECT path FROM " + tableName + " WHERE path = '" + fileName + "'");
            if (rs.next()) {
            	ar.type_log = "Register meme fail : " + command;
            	ar.reason_log = "meme file name exist";
            	ar.type_channel = "�� ��ɾ� ��Ͽ� �����Ͽ����ϴ�.";
            	ar.reason_channel = "�ش� �̹��� ���� �̸��� ������ �̹����� �̹� �����մϴ�. �̹������� �ٲ��ּ���.";
            	
            	throw new SQLException();
            }
    		
			String atime_s = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
            pstmt = conn.prepareStatement("INSERT INTO " + tableName + " VALUES (?,?,?)");

            pstmt.setString(1, command);
            pstmt.setString(2, fileName);
            pstmt.setString(3, atime_s);
            
            ret = pstmt.executeUpdate();
            if(ret == 1){
            	CompletableFuture<Void> downloadResult = image.downloadToFile(Constants.Files.MEME_PATH + guildID + "//" + fileName).thenAccept(file -> System.out.println("Saved attachment to " + file.getName()));
            	downloadResult.get();
    	    	
    	    	if (downloadResult.isDone()) {
                	ar.type_log = "Register meme success : " + command;
                	ar.reason_log = null;
                	ar.type_channel = "�� ��ɾ� ��Ͽ� �����Ͽ����ϴ�.";
                	ar.reason_channel = null;
    	    	}
    	    	else {
    	    		System.out.println("Register meme fail");
                	ar.type_log = "Register meme fail : " + command;
                	ar.reason_log = "image download fail";
                	ar.type_channel = "�� ��ɾ� ��Ͽ� �����Ͽ����ϴ�.";
                	ar.reason_channel = "�̹��� �ٿ�ε忡 �����Ͽ����ϴ�.";
                	
                    stmt = conn.createStatement();
                    ret = stmt.executeUpdate("DELETE FROM " + tableName + " WHERE command = '" + command + "'");
                    if (ret != 1) {
                		ActionResult ar2 = new ActionResult();
                    	ar2.type_log = "Delete errored meme command in registerMemeCmd fail : " + command;
                    	ar2.reason_log = "execute query fail";
                    	ar2.type_channel = "������ �߻��� �� ��ɾ� ������ �����Ͽ����ϴ�.";
                    	ar2.reason_channel = "DB ���� ����";

                    	recordActionResult(guildID, channel, ar2);
                    	throw new SQLException();
                    }
    	    	}
                
                stmt = conn.createStatement();
                rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
                if (rs.next()) {
                	if (rs.getInt(1) > Constants.Max.MEME_COUNT)
            			channel.sendMessage("*���*\n ��ϵ� ��ɾ��� ������ �ִ�ġ(" + Constants.Max.MEME_COUNT + ")�� �ʰ��Ͽ����ϴ�. ���������� ������ " + Constants.Max.EXPIRATION_DATE + "���� ���� ��ɾ ���� �����˴ϴ�.").queue();
                }
            }
            else{
            	ar.type_log = "Register meme fail : " + command;
            	ar.reason_log = "execute update fail";
            	ar.type_channel = "�� ��ɾ� ��Ͽ� �����Ͽ����ϴ�.";
            	ar.reason_channel = "DB ��� ����";
            }
            
		} 
		catch (SQLException e) {
			if (ar.type_log == null) {
	        	ar.type_log = "Register meme fail : " + command;
	        	ar.reason_log = "SQLException occur";
	        	ar.type_channel = "�� ��ɾ� ��Ͽ� �����Ͽ����ϴ�.";
	        	ar.reason_channel = "DB ��� ����";
			}
			e.printStackTrace();
		} catch (InterruptedException e) {
			if (ar.type_log == null) {
	        	ar.type_log = "Register meme fail : " + command;
	        	ar.reason_log = "InterruptedException occur";
	        	ar.type_channel = "�� ��ɾ� ��Ͽ� �����Ͽ����ϴ�.";
	        	ar.reason_channel = "InterruptedException";			////////////////////////
			}
			
			e.printStackTrace();
		} catch (ExecutionException e) {
			if (ar.type_log == null) {
	        	ar.type_log = "Register meme fail : " + command;
	        	ar.reason_log = "ExecutionException occur";
	        	ar.type_channel = "�� ��ɾ� ��Ͽ� �����Ͽ����ϴ�.";
	        	ar.reason_channel = "ExecutionException";			///////////////////////
			}
			
			e.printStackTrace();
		}
		finally {
        	recordActionResult(guildID, channel, ar);
        	
			try {
				if (conn != null && !conn.isClosed())
					conn.close();
			} catch (SQLException e) {
	        	ar.type_log = "Connection close fail";
	        	ar.reason_log = e.toString();
	        	ar.type_channel = null;
	        	ar.reason_channel = null;

	        	recordActionResult(guildID, channel, ar);
				e.printStackTrace();
			}
		}
    }
    
    // �� �̹��� ���� - ��ɾ ���� �̹��� ����
    // DB���� ���ϸ� �ߺ� üũ , �ش� ��ɾ �����ϴ��� üũ �� �̹��� ���� 
    public static void modifyMemeCmd(String command, String fileName, MessageChannel channel, String guildID, Attachment image) {
        Connection conn = null;
        Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int ret;
		
		String tableName = "meme." + guildID;
		MemeCmd targetCmd = findMemeCmd(command, guildID);
		ActionResult ar = new ActionResult();
		
		try {
            conn = DriverManager.getConnection(PrivateData.DB.url_meme, PrivateData.DB.root, PrivateData.DB.pass);
            
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT path FROM " + tableName + " WHERE path = '" + fileName + "'");
            if (rs.next()) {
            	ar.type_log = "Modify meme fail : " + command;
            	ar.reason_log = "image file name exist";
            	ar.type_channel = "�� ��ɾ� ������ �����Ͽ����ϴ�.";
            	ar.reason_channel = "�ش� �̸��� ������ �̹����� �̹� �����մϴ�. �̹������� �ٲ��ּ���.";
    			
            	throw new SQLException();
            }
            
            pstmt = conn.prepareStatement("UPDATE " + tableName + " SET path = ? WHERE command = ?");
            pstmt.setString(1, fileName);
            pstmt.setString(2, command);
            
            ret = pstmt.executeUpdate();
            if (ret == 0) {
            	ar.type_log = "Modify meme fail : " + command;
            	ar.reason_log = "no meme command exist";
            	ar.type_channel = "�� ��ɾ� ������ �����Ͽ����ϴ�.";
            	ar.reason_channel = "�ش� ��ɾ �������� �ʽ��ϴ�.";
            	
        		throw new SQLException();
            }
            else if(ret == 1){
        		ActionResult ar2 = new ActionResult();
        		
            	CompletableFuture<Void> downloadResult = image.downloadToFile(Constants.Files.MEME_PATH + guildID + "//" + fileName).thenAccept(file -> System.out.println("Saved attachment to " + file.getName()));
            	downloadResult.get();
    	    	
    	    	if (downloadResult.isDone()) {
                	ar2.type_log = "Download image success in modifyMemeCmd : " + command;
                	ar2.reason_log = targetCmd.getPath();
                	ar2.type_channel = null;
                	ar2.reason_channel = null;
                	
                	recordActionResult(guildID, channel, ar2);
    	    	}
    	    	else {
                	ar.type_log = "Modify meme fail : " + command;
                	ar.reason_log = "image download fail";
                	ar.type_channel = "�� ��ɾ� ������ �����Ͽ����ϴ�.";
                	ar.reason_channel = "�̹��� �ٿ�ε忡 �����Ͽ����ϴ�.";

                    pstmt = conn.prepareStatement("UPDATE " + tableName + " SET path = ?, atime = ? WHERE command = ?");
                    
                    pstmt.setString(1, targetCmd.getPath());
        			String atime_s = new SimpleDateFormat("yyyy-MM-dd").format(targetCmd.getAtime());
                    pstmt.setString(2, atime_s);
                    pstmt.setString(3, targetCmd.getCommand());

                    ret = pstmt.executeUpdate();
                    if (ret != 1) {
                    	ar2.type_log = "Revert errored meme command in modifyMemeCmd fail : " + command;
                    	ar2.reason_log = "execute update fail";
                    	ar2.type_channel = "������ �߻��� �� ��ɾ� �ѹ鿡 �����Ͽ����ϴ�.";
                    	ar2.reason_channel = "DB ���� ����";

                    	recordActionResult(guildID, channel, ar2);
                    }

                	throw new SQLException();
    	    	}

            	File targetFile = new File(Constants.Files.MEME_PATH + guildID + "//" + targetCmd.getPath());
    	    	if (targetFile.delete()) {
                	ar2.type_log = "Delete image success in modifyMemeCmd : " + command;
                	ar2.reason_log = targetCmd.getPath();
                	ar2.type_channel = null;
                	ar2.reason_channel = null;
                	
                	recordActionResult(guildID, channel, ar2);
    	    	}
    	    	else {
                	ar2.type_log = "Delete image fail in modifyMemeCmd : " + command;
                	ar2.reason_log = "image delete fail : " + targetCmd.getPath();
                	ar2.type_channel = null;
                	ar2.reason_channel = null;
                	
                	recordActionResult(guildID, channel, ar2);
                	throw new SQLException();
    	    	}
    	    	
            	ar.type_log = "Modify meme success : " + command;
            	ar.reason_log = null;
            	ar.type_channel = "�� ��ɾ� ������ �����Ͽ����ϴ�.";
            	ar.reason_channel = null;

    			updateMemeCmdAtime(targetCmd, guildID);
            }
            else{
            	ar.type_log = "Modify meme fail : " + command;
            	ar.reason_log = "execute update fail";
            	ar.type_channel = "�� ��ɾ� ������ �����Ͽ����ϴ�.";
            	ar.reason_channel = "DB ���� ����";

        		throw new SQLException();
            }
		} 
		catch (SQLException e) {
			if (ar.type_log == null) {
	        	ar.type_log = "Modify meme fail : " + command;
	        	ar.reason_log = "SQLException occur";
	        	ar.type_channel = "�� ��ɾ� ������ �����Ͽ����ϴ�.";
	        	ar.reason_channel = "DB ���� ����";
			}
        	
			e.printStackTrace();
		} catch (InterruptedException e) {
			if (ar.type_log == null) {
	        	ar.type_log = "Register meme fail : " + command;
	        	ar.reason_log = "InterruptedException occur";
	        	ar.type_channel = "�� ��ɾ� ������ �����Ͽ����ϴ�.";
	        	ar.reason_channel = "InterruptedException";			////////////////////////
			}
			
			e.printStackTrace();
		} catch (ExecutionException e) {
			if (ar.type_log == null) {
	        	ar.type_log = "Register meme fail : " + command;
	        	ar.reason_log = "ExecutionException occur";
	        	ar.type_channel = "�� ��ɾ� ��Ͽ� �����Ͽ����ϴ�.";
	        	ar.reason_channel = "ExecutionException";			///////////////////////
			}
			
			e.printStackTrace();
		}
		finally {
        	recordActionResult(guildID, channel, ar);
        	
			try {
				if (conn != null && !conn.isClosed())
					conn.close();
			} catch (SQLException e) {
	        	ar.type_log = "Connection close fail";
	        	ar.reason_log = e.toString();
	        	ar.type_channel = null;
	        	ar.reason_channel = null;

	        	recordActionResult(guildID, channel, ar);
				e.printStackTrace();
			}
		}
    }
    
    // �� ��ɾ� ����
    // DB���� ��ɾ� üũ �� ���� �� �̹��� ���� ����
    @DebugCommandHandler
    public static void deleteMemeCmd(String command, MessageChannel channel, String guildID) {
	    Connection conn = null;
		PreparedStatement pstmt = null;
		int ret;
		
		String tableName = "meme." + guildID;
		MemeCmd targetCmd = findMemeCmd(command, guildID);
		ActionResult ar = new ActionResult();
		
    	try {
            conn = DriverManager.getConnection(PrivateData.DB.url_meme, PrivateData.DB.root, PrivateData.DB.pass);
            pstmt = conn.prepareStatement("DELETE FROM " + tableName + " WHERE command = ?");
            pstmt.setString(1, command);
            
            ret = pstmt.executeUpdate();
            if (ret == 0) {
            	ar.type_log = "Delete meme fail : " + command;
            	ar.reason_log = "no meme command exist";
            	ar.type_channel = "�� ��ɾ� ������ �����Ͽ����ϴ�.";
            	ar.reason_channel = "�ش� ��ɾ �������� �ʽ��ϴ�.";
            	
    			throw new SQLException();
            }
            else if(ret == 1){
            	File targetFile = new File(Constants.Files.MEME_PATH + guildID + "//" + targetCmd.getPath());
       			if (targetFile.delete()) {
                	if (channel.getName().equals(Constants.Name.BOT_NOTICE_CHANNEL)) {
                    	ar.type_log = "Delete unused meme success : " + command;
                    	ar.reason_log = null;
                    	ar.type_channel = "������ �ʴ� �� ��ɾ� �����Ǿ����ϴ�.";
                    	ar.reason_channel = null;
                	}
                	else {
                    	ar.type_log = "Delete meme success : " + command;
                    	ar.reason_log = null;
                    	ar.type_channel = "�� ��ɾ� ������ �����Ͽ����ϴ�.";
                    	ar.reason_channel = null;
                	}
       			}
       			else {
                	ar.type_log = "Delete meme fail : " + command;
                	ar.reason_log = "delete image file fail : " + targetCmd.getPath();
                	ar.type_channel = "�� ��ɾ� ������ �����Ͽ����ϴ�.";
                	ar.reason_channel = "�̹��� ���� ������ �����Ͽ����ϴ�.";
                	
                    pstmt = conn.prepareStatement("INSERT INTO " + tableName + " VALUES (?,?,?)");

                    pstmt.setString(1, targetCmd.getCommand());
                    pstmt.setString(2, targetCmd.getPath());

        			String atime_s = new SimpleDateFormat("yyyy-MM-dd").format(targetCmd.getAtime());
                    pstmt.setString(3, atime_s);
                    
                    ret = pstmt.executeUpdate();
                    if (ret != 1) {
                    	ActionResult ar2 = new ActionResult();
                    	ar2.type_log = "Insert errored meme command in deleteMemeCmd fail : " + command;
                    	ar2.reason_log = "execute update fail";
                    	ar2.type_channel = "������ �߻��� �� ��ɾ� ���Կ� �����Ͽ����ϴ�.";
                    	ar2.reason_channel = "DB ���� ����";

                    	recordActionResult(guildID, channel, ar2);
                    	throw new SQLException();
                    }
       			}
            }
            else{
            	ar.type_log = "Delete meme fail : " + command;
            	ar.reason_log = "execute update fail";
            	ar.type_channel = "�� ��ɾ� ������ �����Ͽ����ϴ�.";
            	ar.reason_channel = "DB ���� ����";
        		
    			throw new SQLException();
            }            
    	}
    	catch (SQLException e) {
        	ar.type_log = "Delete meme fail : " + command;
        	ar.reason_log = "SQLException occur";
        	ar.type_channel = "�� ��ɾ� ������ �����Ͽ����ϴ�.";
        	ar.reason_channel = "DB ���� ����";
        	
			e.printStackTrace();
		}
		finally {
        	recordActionResult(guildID, channel, ar);
        	
			try {
				if (conn != null && !conn.isClosed())
					conn.close();
			}
			catch (SQLException e) {
	        	ar.type_log = "Connection close fail";
	        	ar.reason_log = e.toString();
	        	ar.type_channel = null;
	        	ar.reason_channel = null;

	        	recordActionResult(guildID, channel, ar);
				e.printStackTrace();
			}
		}
    }
    
    // �� ���
    // DB���� �ش� ��ɾ� �˻� �� ��� & ������ ���ð� ����
    @DebugCommandHandler
    public static void executeMemeCmd(MessageChannel channel, Message message, String guildID) {
    	MemeCmd targetCmd = findMemeCmd(message.getContentDisplay(), guildID);
		ActionResult ar = new ActionResult();
		
    	if (targetCmd == null)
    		return;
    	
		File targetFile = new File(Constants.Files.MEME_PATH + guildID + "//" + targetCmd.getPath());
		
		if (targetFile.exists()) {
	    	channel.purgeMessages(message);
        	channel.sendFile(targetFile)
        			.queue();
        	channel.sendMessage("by " + message.getAuthor().getName())
					.queue();
        	updateMemeCmdAtime(targetCmd, guildID);
		}
		else {
        	ar.type_log = "no image exist";
        	ar.reason_log = targetFile.toString();
        	ar.type_channel = "�ش� �̹����� �������� �ʽ��ϴ�.";
        	ar.reason_channel = targetFile.toString();

        	recordActionResult(guildID, channel, ar);
		}
    }
    
    // �� ��ɾ� ������ ��� �ð� ����
    public static void updateMemeCmdAtime(MemeCmd mcmd, String guildID) {
    	Connection conn = null;
    	PreparedStatement pstmt = null;
    	
    	String tableName = "meme." + guildID;
    	try {
			conn = DriverManager.getConnection(PrivateData.DB.url_meme, PrivateData.DB.root, PrivateData.DB.pass);

			String atime_s = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
	    	pstmt = conn.prepareStatement("UPDATE " + tableName + " SET atime = ? WHERE command = ?");
	    	pstmt.setString(1, atime_s);
	    	pstmt.setString(2, mcmd.getCommand());

	    	int count = pstmt.executeUpdate();
	    	if (count == 0) {
	    		System.out.println("Update atime error occur : Cannot find target command " + mcmd.getCommand());
	    	}
	    	if (count == 1) {
	    		System.out.println("Updated success : " + mcmd.getCommand());
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
    }
    
    // ��ɾ �ش��ϴ� ������ ����
    private static MemeCmd findMemeCmd(String command, String guildID) {
    	Connection conn = null;
    	PreparedStatement pstmt = null;
    	ResultSet rs = null;
    	MemeCmd result = null;
    	String tableName = "meme." + guildID;
    	
    	try {
    		conn = DriverManager.getConnection(PrivateData.DB.url_meme, PrivateData.DB.root, PrivateData.DB.pass);
    		
    		pstmt = conn.prepareStatement("SELECT command, path, atime FROM " + tableName + " WHERE command = ?");
    		pstmt.setString(1, command);
    		rs = pstmt.executeQuery();
    		if (rs.next()) {
	    		String cmd = rs.getString(1);
	    		String path = rs.getString(2);
	    		Date atime =  Date.valueOf(rs.getString(3));
	    		
	    		result = new MemeCmd(cmd, path, atime);
    		}
    	}
    	catch (SQLException e) {
			System.out.println("SQLException occur : " + e.toString());
			e.printStackTrace();
    	}
    	finally {
    		try {
    			conn.close();
    		}
    		catch (SQLException e) {
				System.out.println("SQLException occur : " + e.toString());
				e.printStackTrace();
    		}
    	}
    	
    	return result;
    }

    @DebugCommandHandler
    public static void showMemeCmd(MessageChannel channel, String guildID) {
    	Connection conn = null;
    	Statement stmt = null;
    	ResultSet rs = null;
    	String tableName = "meme." + guildID;
    	String result = new String("");
    	
    	try {
    		conn = DriverManager.getConnection(PrivateData.DB.url_meme, PrivateData.DB.root, PrivateData.DB.pass);

    		stmt = conn.createStatement();
    		rs = stmt.executeQuery("SELECT command FROM " + tableName + " ORDER BY command");
    		while (rs.next()) {
    			result = result.concat(rs.getString(1) + "\n");
    		}
    	}
    	catch (SQLException e) {
			System.out.println("SQLException occur : " + e.toString());
			e.printStackTrace();
    	}
    	finally {
    		try {
    			conn.close();
    		}
    		catch (SQLException e) {
				System.out.println("SQLException occur : " + e.toString());
				e.printStackTrace();
    		}
    	}
    	
    	channel.sendMessage(result).queue();
    	//return result;
    }
    
    // ����� �� üũ
    // ���� �ð����� ������ ���� �� ��ɾ ���Ͽ� ���� ���� ����
    @DebugCommandHandler
    public static void checkExpiredMeme(MessageChannel noticeChannel, String guildID) {
    	Connection conn = null;
    	Statement stmt = null;
    	ResultSet rs = null;
    	String tableName = "meme." + guildID;
    	MemeCmd targetCmd = null;
    	int cnt = 0;
    	
    	try {
    		conn = DriverManager.getConnection(PrivateData.DB.url_meme, PrivateData.DB.root, PrivateData.DB.pass);

    		stmt = conn.createStatement();
    		rs = stmt.executeQuery("SELECT * FROM " + tableName + " ORDER BY atime DESC");
    		while (rs.next()) {
    			if (++cnt > Constants.Max.MEME_COUNT) {
    				targetCmd = new MemeCmd(rs.getString(1), rs.getString(2), rs.getDate(3));
    				
    				if (targetCmd.isCommandExpired())
    					MemeCmdController.deleteMemeCmd(targetCmd.getCommand(), noticeChannel, guildID);
    			}
    		}
    	}
    	catch (SQLException e) {
			System.out.println("SQLException occur : " + e.toString());
			e.printStackTrace();
    	}
    	finally {
    		try {
    			conn.close();
    		}
    		catch (SQLException e) {
				System.out.println("SQLException occur : " + e.toString());
				e.printStackTrace();
    		}
    	}
    }

/*  --  ������ ���� ��� �������� ���� �̻��    
	public static List<MemeCmd> getMemeCmdList() {
		return Memecmds;
	}*/

    
    static void recordActionResult(String guildID, MessageChannel channel, ActionResult actionResult) {
    	if (guildID != null && actionResult.type_log != null) {
    		String logMessage = actionResult.type_log + " -> " + actionResult.reason_log;
			Logger.writeLog("log_" + guildID, logMessage);
			System.out.println(logMessage);
    	}
    	
    	if (channel != null && actionResult.type_channel != null) {
    		String textChannelMessage = actionResult.type_channel + " -> " + actionResult.reason_channel;
    		channel.sendMessage(textChannelMessage).queue();
    	}
    }
    
    

    @DebugCommandHandler
    static void createMemeTable(Guild guild) {
        Connection conn = null;
        Statement stmt = null;
        int ret;
        MessageChannel noticeChannel = NoticeController.getNoticeChannel(guild);

		try {
            conn = DriverManager.getConnection(PrivateData.DB.url_meme, PrivateData.DB.root, PrivateData.DB.pass);

            stmt = conn.createStatement();
            ret = stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `meme`.`" + guild.getId() + "` (`command` text, `path` text, `atime` text)");

            if (ret != 0) {
    			Logger.writeLog("log_" + guild.getId(), "�� ��ɾ� ���̺� ������ �����Ͽ����ϴ�.");
    			noticeChannel.sendMessage("�� ��ɾ� ���̺� ������ �����Ͽ����ϴ�.").queue();
    			System.out.println("Create meme table fail");
            }
		} 
		catch (SQLException e) {
			Logger.writeLog("log_" + guild.getId(), "�� ��ɾ� ���̺� ������ �����Ͽ����ϴ�.");
			noticeChannel.sendMessage("�� ��ɾ� ���̺� ������ �����Ͽ����ϴ�.").queue();
			System.out.println("Create meme table fail  : " + e.toString());
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
}
