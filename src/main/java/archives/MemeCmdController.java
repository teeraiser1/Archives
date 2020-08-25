package main.java.archives;

import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Vector;

import main.java.archives.MemeCmdController.MemeCmd;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Message.Attachment;

public class MemeCmdController {
	private static Vector<MemeCmd> Memecmds = new Vector<MemeCmd>();
	
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
    	public boolean isCommandExpired() {
    		int days = 1000*60*60*24;
    		Date curtime = new Date(System.currentTimeMillis() - 21*days);
    		return atime.before(curtime);
    	}
    	public void updateAtime() {
    		atime = new Date(System.currentTimeMillis());
    	}
}

    public static void loadMemeCmd() {
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
    public static void registerMemeCmd(String command, String fileName, MessageChannel channel, Attachment image) {
        Connection conn = null;
        Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
            conn = DriverManager.getConnection(PrivateData.DB.url, PrivateData.DB.root, PrivateData.DB.pass);

            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT command FROM meme WHERE command = '" + command + "'");
            if (rs.next()) {
    			channel.sendMessage("해당 명령어가 이미 존재합니다").queue();
            	throw new SQLException();
            }

            rs = stmt.executeQuery("SELECT path FROM meme WHERE path = '" + fileName + "'");
            if (rs.next()) {
    			channel.sendMessage("해당 이름과 동일한 이미지가 이미 존재합니다. 이미지명을 바꿔주세요.").queue();
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
    	    	
    			channel.sendMessage("성공적으로 이미지가 등록되었습니다").queue();
            }
            else{
    			System.out.println("Meme register error : " + command);
            }
		} 
		catch (SQLException e) {
			Logger.writeLog("log.txt", "이미지 추가에 실패하였습니다. -> " + command);
    		channel.sendMessage("이미지 추가에 실패하였습니다.").queue();
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
    public static void modifyMemeCmd(String command, String fileName, MessageChannel channel, Attachment image) {
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
    			channel.sendMessage("해당 이름과 동일한 이미지가 이미 존재합니다. 이미지명을 바꿔주세요.").queue();
            	throw new SQLException();
            }
            
            pstmt = conn.prepareStatement("UPDATE meme SET path = ? WHERE command = ?");
            pstmt.setString(1, fileName);
            pstmt.setString(2, command);
            
            int count = pstmt.executeUpdate();
            if (count == 0) {
        		channel.sendMessage("해당 커맨드가 존재하지 않습니다.").queue();
        		throw new SQLException();
            }
            else if(count == 1){
    			System.out.println("Meme modify success : " + command);
    			isModified = true;
            }
            else{
    			Logger.writeLog("log.txt", "이미지 수정 중 오류가 발생했습니다. -> " + command);
        		channel.sendMessage("이미지 수정 중 오류가 발생했습니다.").queue();
        		System.out.println("Meme modify SQL fail : " + command);
        		throw new SQLException();
            }
		} 
		catch (SQLException e) {
    		Logger.writeLog("log.txt", "이미지 수정에 실패하였습니다. -> " + command);
    		channel.sendMessage("이미지 수정에 실패하였습니다.").queue();
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
		channel.sendMessage("이미지가 성공적으로 변경되었습니다.").queue();
    }
    public static void deleteMemeCmd(String command, MessageChannel channel) {
	    Connection conn = null;
		PreparedStatement pstmt = null;
		boolean isDeleted = false;
		
    	try {
            conn = DriverManager.getConnection(PrivateData.DB.url, PrivateData.DB.root, PrivateData.DB.pass);
            pstmt = conn.prepareStatement("DELETE FROM meme WHERE command = ?");
            pstmt.setString(1, command);
            
            int count = pstmt.executeUpdate();
            if (count == 0) {
        		Logger.writeLog("log.txt", "해당 명령어가 존재하지 않습니다. -> " + command);
    			System.out.println("Delete Meme success : " + command);
            	if (channel != null)
	    			channel.sendMessage("해당 명령어가 존재하지 않습니다. -> " + command).queue();
            	
    			throw new SQLException();
            }
            else if(count == 1){
        		Logger.writeLog("log.txt", "사용되지 않는 짤이 삭제되었습니다. -> " + command);
	    		System.out.println("Delete Unused meme command success : " + command);
            	if (channel != null)
    	    		channel.sendMessage("사용되지 않는 짤이 삭제되었습니다. -> " + command).queue();
            	
    			isDeleted = true;
            }
            else{
        		Logger.writeLog("log.txt", "이미지 삭제 중 오류가 발생했습니다. -> " + command);
    			System.out.println("Delete Unused meme command fail : " + command);
        		if (channel != null)
            		channel.sendMessage("이미지 삭제 중 오류가 발생했습니다.").queue();
        		
    			throw new SQLException();
            }            
    	}
    	catch (SQLException e) {
    		Logger.writeLog("log.txt", "이미지 삭제에 실패하였습니다. -> " + command);
    		channel.sendMessage("이미지 삭제에 실패하였습니다.").queue();
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
	    		Logger.writeLog("log.txt", "이미지가 성공적으로 삭제되었습니다. -> " + command);
   				channel.sendMessage("이미지가 성공적으로 삭제되었습니다.").queue();
   				System.out.println("Removed image " + targetCmd.getPath());
   			}
   			else {
	    		Logger.writeLog("log.txt", "이미지 삭제 중 에러가 발생했습니다.(이미지 파일 제거 실패) -> " + command);
   				channel.sendMessage("이미지 삭제 중 에러가 발생했습니다.(이미지 파일 제거 실패)").queue();
   				System.out.println("Delete image error : " + targetCmd.getPath());
   			}
       				
    	}
    }
    public static void executeMemeCmd(MessageChannel channel, String command) {
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
			channel.sendMessage("해당 이미지가 존재하지 않습니다 " + targetFile.toString())
					.queue();
    }
    public static void updateMemeCmdAtime(MemeCmd mcmd) {
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
    
    public static void checkExpiredMeme() {
    	for (MemeCmd mcmd : MemeCmdController.getMemeCmdList())
    		if (mcmd.isCommandExpired())
    			MemeCmdController.deleteMemeCmd(mcmd.getCommand(), null);
    }
    
	public static Vector<MemeCmd> getMemeCmdList() {
		return Memecmds;
	}
}
