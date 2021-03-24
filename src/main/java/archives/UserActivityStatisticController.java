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
	
	private static Vector<Long> users = new Vector<Long>();	// 트래킹중인 유저의 ID 벡터
	

	// 모니터링중인 유저 리스트 로드
    @DebugCommandHandler
	public static void loadMonitoringUsers() {
		try {
			Connection conn = null;
			Statement stmt = null;
			ResultSet rs = null;

			conn = DriverManager.getConnection(PrivateData.DB.url_user_activity, PrivateData.DB.root, PrivateData.DB.pass);
			
			// 모니터링중인 유저 ID를 로드
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
	
	// 유저 데이터 수집 시작
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
            
			// 모니터링 유저 테이블에 해당 유저 아이디, 모니터 상태 저장
            pstmt = conn.prepareStatement("INSERT INTO user_activity.user_monitoring_status VALUES (?,?)");
            pstmt.setLong(1,  userId);
            pstmt.setBoolean(2, true);
            pstmt.executeUpdate();
			
            // 유저 활동 데이터 테이블 생성
			stmt = conn.createStatement();
			ret = stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `user_activity`.`" + userId + "` (`timestamp` timestamp, `status` text, `activity` text)");
			
			conn.close();
			
			actionResult.type_log = "Join User Activity Statistic Success : " + userName;
			actionResult.reason_log = null;
			actionResult.type_channel = userName + "님의 유저 활동 통계 기록이 시작되었습니다.";
			actionResult.reason_channel = null;
        	
		} catch (SQLException e) {
			actionResult.type_log = "Join User Activity Statistic fail : " + userName;
			actionResult.reason_log = "SQLException occur";
			actionResult.type_channel = userName + "님의 유저 활동 통계 시작에 실패하였습니다.";
			actionResult.reason_channel = "SQLException";
		}

    	MemeCmdController.recordActionResult(guild.getId().toString(), channel, actionResult);
		users.add(userId);
	}

    // 유저 데이터 수집 일시중지
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
			actionResult.type_channel = userName + "님의 유저 활동 통계 기록이 일시중지되었습니다.";
			actionResult.reason_channel = null;
			
		} catch (SQLException e) {
			actionResult.type_log = "Pause Monitoring fail : " + userName;
			actionResult.reason_log = "SQLException occur";
			actionResult.type_channel = userName + "님의 유저 활동 통계 기록 일시정지에 실패하였습니다.";
			actionResult.reason_channel = "SQLException";
		}
    	MemeCmdController.recordActionResult(guild.getId().toString(), channel, actionResult);
	}	

    // 유저 데이터 수집 재시작
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
			actionResult.type_channel = userName + "님의 유저 활동 통계 기록이 재시작되었습니다.";
			actionResult.reason_channel = null;
        	
		} catch (SQLException e) {
			actionResult.type_log = "Resume Monitoring fail : " + userName;
			actionResult.reason_log = "SQLException occur";
			actionResult.type_channel = userName + "님의 유저 활동 통계 기록 재시작에 실패하였습니다.";
			actionResult.reason_channel = "SQLException";
		}
    	MemeCmdController.recordActionResult(guild.getId().toString(), channel, actionResult);
	}

    // 유저 데이터 파기
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
			actionResult.type_channel = userName + "님의 모든 유저 활동 통계 데이터가 파기되었습니다.";
			actionResult.reason_channel = null;
        	
		} catch (SQLException e) {
			actionResult.type_log = "Delete User Activity Data fail : " + userName;
			actionResult.reason_log = "SQLException occur";
			actionResult.type_channel = userName + "님의 유저 활동 통계 데이터 파기에 실패하였습니다.";
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
			
			// 해당 유저의 테이블 체크
			stmt = conn.createStatement();
			ret = stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `user_activity`.`" + userId + "` (`timestamp` timestamp, `status` text, `activity` text)");
			
			// 데이터 저장
			pstmt = conn.prepareStatement("INSERT INTO user_activity." + userId + " VALUES(?, ?, ?)");
			pstmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));	// 현재 UTC 시간을 저장
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
	
	// 유저 상태를 체크하는 부모 쓰레드. 내부 쓰레드를 1분마다 호출
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
	// 유저 상태를 체크하는 내부 쓰레드. 
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
