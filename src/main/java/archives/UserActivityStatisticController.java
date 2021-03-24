package main.java.archives;

import java.sql.*;
import java.util.Calendar;
import java.util.Vector;

import main.java.archives.MemeCmdController.ActionResult;
import net.dv8tion.jda.api.entities.ClientType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;;

public class UserActivityStatisticController {
	
	private static Vector<Long> users = new Vector<Long>();	// Ʈ��ŷ���� ������ ID ����
	

	// ����͸����� ���� ����Ʈ �ε�
    @DebugCommandHandler
	public static void loadMonitoringUsers() {
		try {
			Connection conn = null;
			Statement stmt = null;
			ResultSet rs = null;

			conn = DriverManager.getConnection(PrivateData.DB.url_user_activity, PrivateData.DB.root, PrivateData.DB.pass);
			
			// ����͸����� ���� ID�� �ε�
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM user_activity.user_monitoring_status");
			while (rs.next()) {
				if (rs.getBoolean(2))
					users.add(rs.getLong(1));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// ���� ������ ���� ����
    @DebugCommandHandler
	public static void joinUserActivityStatistic(User user, Guild guild, MessageChannel channel) {
    	long userId = user.getIdLong();
    	String userName = user.getName();
    	
		if (users.contains(userId))
			return ;

		ActionResult actionResult = new ActionResult();
		
		try {
			System.out.println("QWER " + "joinUserActivityStatistic");
			Connection conn = null;
			Statement stmt = null;
			PreparedStatement pstmt = null;
			int ret;
			ResultSet rs = null;
			
			conn = DriverManager.getConnection(PrivateData.DB.url_user_activity, PrivateData.DB.root, PrivateData.DB.pass);

			// 
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT user_id FROM user_activity.user_monitoring_status WHERE user_id = " + userId);
            if (rs.next()) {
    			System.out.println("QWER " + "exists");
            	return ;
            }
            
			// ����͸� ���� ���̺� �ش� ���� ���̵�, ����� ���� ����
            pstmt = conn.prepareStatement("INSERT INTO user_activity.user_monitoring_status VALUES (?,?)");
            pstmt.setLong(1,  userId);
            pstmt.setBoolean(2, true);
            pstmt.executeUpdate();
			
            // ���� Ȱ�� ������ ���̺� ����
			stmt = conn.createStatement();
			ret = stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `user_activity`.`" + userId + "` (`timestamp` timestamp, `status` text, `activity` text)");
			
			conn.close();
			
			actionResult.type_log = "Join User Activity Statistic Success : " + userName;
			actionResult.reason_log = null;
			actionResult.type_channel = userName + "���� ���� Ȱ�� ��� ����� ���۵Ǿ����ϴ�.";
			actionResult.reason_channel = null;
        	
		} catch (SQLException e) {
			actionResult.type_log = "Join User Activity Statistic fail : " + userName;
			actionResult.reason_log = "SQLException occur";
			actionResult.type_channel = userName + "���� ���� Ȱ�� ��� ���ۿ� �����Ͽ����ϴ�.";
			actionResult.reason_channel = "SQLException";
		}

    	MemeCmdController.recordActionResult(guild.getId().toString(), channel, actionResult);
		users.add(userId);
	}

    // ���� ������ ���� �Ͻ�����
    @DebugCommandHandler
	public static void pauseMonitoring(User user, Guild guild, MessageChannel channel) {
    	long userId = user.getIdLong();
    	String userName = user.getName();
		if (!users.contains(userId))
			return ;
		
		ActionResult actionResult = new ActionResult();
		
		try {
			System.out.println("QWER " + "pauseMonitoring");
			Connection conn = null;
			PreparedStatement pstmt = null;
			int ret;
			ResultSet rs = null;
			
			conn = DriverManager.getConnection(PrivateData.DB.url_user_activity, PrivateData.DB.root, PrivateData.DB.pass);
			
			pstmt = conn.prepareStatement("UPDATE user_activity.user_monitoring_status" + " SET status = ? WHERE user_id = ?");
			pstmt.setBoolean(1, false);
			pstmt.setLong(2, userId);
			ret = pstmt.executeUpdate();
			
			users.remove(userId);
			
			conn.close();
			
			actionResult.type_log = "Pause Monitoring Success : " + userName;
			actionResult.reason_log = null;
			actionResult.type_channel = userName + "���� ���� Ȱ�� ��� ����� �Ͻ������Ǿ����ϴ�.";
			actionResult.reason_channel = null;
			
		} catch (SQLException e) {
			actionResult.type_log = "Pause Monitoring fail : " + userName;
			actionResult.reason_log = "SQLException occur";
			actionResult.type_channel = userName + "���� ���� Ȱ�� ��� ��� �Ͻ������� �����Ͽ����ϴ�.";
			actionResult.reason_channel = "SQLException";
		}
    	MemeCmdController.recordActionResult(guild.getId().toString(), channel, actionResult);
	}	

    // ���� ������ ���� �����
    @DebugCommandHandler
	public static void resumeMonitoring(User user, Guild guild, MessageChannel channel) {
    	long userId = user.getIdLong();
    	String userName = user.getName();
		if (users.contains(userId))
			return ;
		
		ActionResult actionResult = new ActionResult();
		
		try {
			System.out.println("QWER " + "resumeMonitoring");
			Connection conn = null;
			PreparedStatement pstmt = null;
			int ret;
			ResultSet rs = null;
			
			conn = DriverManager.getConnection(PrivateData.DB.url_user_activity, PrivateData.DB.root, PrivateData.DB.pass);
			
			pstmt = conn.prepareStatement("UPDATE user_activity.user_monitoring_status" + " SET status = ? WHERE user_id = ?");
			pstmt.setBoolean(1, true);
			pstmt.setLong(2, userId);
			ret = pstmt.executeUpdate();
			
			users.add(userId);
			
			conn.close();
			
			actionResult.type_log = "Resume Monitoring Success : " + userName;
			actionResult.reason_log = null;
			actionResult.type_channel = userName + "���� ���� Ȱ�� ��� ����� ����۵Ǿ����ϴ�.";
			actionResult.reason_channel = null;
        	
		} catch (SQLException e) {
			actionResult.type_log = "Resume Monitoring fail : " + userName;
			actionResult.reason_log = "SQLException occur";
			actionResult.type_channel = userName + "���� ���� Ȱ�� ��� ��� ����ۿ� �����Ͽ����ϴ�.";
			actionResult.reason_channel = "SQLException";
		}
    	MemeCmdController.recordActionResult(guild.getId().toString(), channel, actionResult);
	}

    // ���� ������ �ı�
    @DebugCommandHandler
	public static void deleteUserActivityData(User user, Guild guild, MessageChannel channel) {
    	long userId = user.getIdLong();
    	String userName = user.getName();
		if (users.contains(userId))
			users.remove(userId);
		
		ActionResult actionResult = new ActionResult();
		
		try {
			System.out.println("QWER " + "deleteUserActivityData");
			Connection conn = null;
			Statement stmt = null;
			PreparedStatement pstmt = null;
			int ret;
			ResultSet rs = null;
			
			conn = DriverManager.getConnection(PrivateData.DB.url_user_activity, PrivateData.DB.root, PrivateData.DB.pass);
			
			pstmt = conn.prepareStatement("DELETE FROM user_activity.user_monitoring_status" + " WHERE user_id = ?");
			pstmt.setLong(1, userId);
			ret = pstmt.executeUpdate();
			
			stmt = conn.createStatement();
			ret = stmt.executeUpdate("DROP TABLE user_activity." + userId);
			
			conn.close();
			
			actionResult.type_log = "Delete User Activity Data Success : " + userName;
			actionResult.reason_log = null;
			actionResult.type_channel = userName + "���� ��� ���� Ȱ�� ��� �����Ͱ� �ı�Ǿ����ϴ�.";
			actionResult.reason_channel = null;
        	
		} catch (SQLException e) {
			actionResult.type_log = "Delete User Activity Data fail : " + userName;
			actionResult.reason_log = "SQLException occur";
			actionResult.type_channel = userName + "���� ���� Ȱ�� ��� ������ �ı⿡ �����Ͽ����ϴ�.";
			actionResult.reason_channel = "SQLException";
		}
    	MemeCmdController.recordActionResult(guild.getId().toString(), channel, actionResult);
	}

    @DebugCommandHandler
	public static void recordUserStatus(Member targetMember) {
		long userId = targetMember.getIdLong();
		
		if (!users.contains(userId))
			return ;
		
		try {
			System.out.println("QWER " + "recordUserStatus");
			Connection conn = null;
			Statement stmt = null;
			PreparedStatement pstmt = null;
			int ret;
			
			conn = DriverManager.getConnection(PrivateData.DB.url_user_activity, PrivateData.DB.root, PrivateData.DB.pass);
			
			// �ش� ������ ���̺� üũ
			stmt = conn.createStatement();
			ret = stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `user_activity`.`" + userId + "` (`timestamp` timestamp, `status` text, `activity` text)");
			
			// ������ ����
			pstmt = conn.prepareStatement("INSERT INTO user_activity." + userId + " VALUES(?, ?, ?)");
			pstmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));	// ���� UTC �ð��� ����
			pstmt.setString(2, targetMember.getOnlineStatus(ClientType.DESKTOP).toString());
			if (!targetMember.getActivities().isEmpty())
				pstmt.setString(3, targetMember.getActivities().get(0).getName());
			else
				pstmt.setString(3, null);
				
			ret = pstmt.executeUpdate();
			
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// ���� ���¸� üũ�ϴ� �θ� ������. ���� �����带 1�и��� ȣ��
	public static class RecorderThread extends Thread {
		public static RecorderThread getInstance() {
			return new RecorderThread();
		}
		
		public void run() {
			while (true) {
				Calendar now_c = Calendar.getInstance();
				now_c.setTimeInMillis(System.currentTimeMillis());
				
				try {
					if (now_c.get(Calendar.SECOND) == 0)
						RecorderThread_inner.getInstance().run();

					now_c.setTimeInMillis(System.currentTimeMillis());
					Thread.sleep((60 - now_c.get(Calendar.SECOND)) * 1000 - now_c.get(Calendar.MILLISECOND));
				} catch (InterruptedException e) {
	
					e.printStackTrace();
				}
			}
		}
	}
	// ���� ���¸� üũ�ϴ� ���� ������. 
	public static class RecorderThread_inner extends Thread {
		public static RecorderThread_inner getInstance() {
			return new RecorderThread_inner();
		}
		
		public void run() {
			Calendar now_c = Calendar.getInstance();
			now_c.setTimeInMillis(System.currentTimeMillis());
			
			try {
				Connection conn = null;
				Statement stmt = null;
				PreparedStatement pstmt = null;
				ResultSet rs = null;
				
				conn = DriverManager.getConnection(PrivateData.DB.url_user_activity, PrivateData.DB.root, PrivateData.DB.pass);
				
				for(long userId : users)
				{
					System.out.println();
				}
//				if (users.size() == 0) {
//					stmt = conn.createStatement();
//					rs = stmt.executeQuery(("SHOW TABLES from user_activity"));
//					
//					while(rs.next()) {
//						users.add(rs.getLong(1));
//					}
//				}
				
				
				
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
