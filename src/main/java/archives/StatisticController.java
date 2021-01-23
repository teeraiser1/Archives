package main.java.archives;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class StatisticController {
/*	public static String getServerID(String serverName) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String serverID = null;
		boolean isDuplicatedID;
		int count;
		int trial = 0;

		try {
			conn = DriverManager.getConnection(PrivateData.DB.url, PrivateData.DB.root, PrivateData.DB.pass);
			pstmt = conn.prepareStatement("SELECT * FROM serverid WHERE name = ?");
			pstmt.setString(1, serverName);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				serverID = rs.getString("id");
			}
			else {
				do {
					serverID = generateID();
					pstmt = conn.prepareStatement("INSERT IGNORE INTO serverid VALUES(?, ?)");
					pstmt.setString(1, serverName);
					pstmt.setString(2, serverID);
					count = pstmt.executeUpdate();
					if (count == 0)
						isDuplicatedID = true;
					else
						isDuplicatedID = false;
					trial++;
				} while (isDuplicatedID && trial < Constants.Max.TRIAL);
				if (trial >= Constants.Max.TRIAL) {
					serverID = null;
					throw new SQLException("generateID generate duplicated ID repeatly");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (conn != null && !conn.isClosed())
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return serverID;
	}
	
	public static String generateID() {
		System.out.println("ID generated");
		String alphabet = "abcdefghijklmnopqrstuvwxyz0123456789";
		char c;
		StringBuilder builder = new StringBuilder();
		Random rand = new Random(System.currentTimeMillis());
		while (builder.length() < Constants.Max.SERVER_ID_LENGTH) {
			c = alphabet.charAt(rand.nextInt(alphabet.length() - 1));
			builder.append(c);
		}
		return builder.toString();
	}
	
	public static String makeStatisticTable(String tableName, List<Member> members) {
		Connection conn = null;
		Statement stmt = null;
		
		try {
			conn = DriverManager.getConnection(PrivateData.DB.url, PrivateData.DB.root, PrivateData.DB.pass);
			
			StringBuilder builder = new StringBuilder();
            builder.append("CREATE TABLE if not exists {0}(")
                    .append("date date primary key not null unique");
            for (Member member : members)
            	builder.append("," + member.getEffectiveName() + " varchar(20)");
            builder.append(")");
            
			stmt = conn.createStatement();
			boolean isExecute = stmt.execute(MessageFormat.format(builder.toString(), tableName));
			if (isExecute == true)
				System.out.println("table create success : " + tableName);
			else
				System.out.println("table create fail : " + tableName);
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (conn != null && !conn.isClosed())
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return tableName;
	}
	
	public static boolean updateUserData(String tableType, Guild guild, String userName, String data) {
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String stmt_str;
		
		String serverID = getServerID(guild.getName());
		String tableName = tableType + "_" + serverID;
		if (serverID == null)
			return false;
		
		try {
			conn = DriverManager.getConnection(PrivateData.DB.url, PrivateData.DB.root, PrivateData.DB.pass);

        	List<Member> members = new ArrayList<Member>();
        	Iterator<Member> iter = guild.getMembers().iterator();
        	Member member;
        	while (iter.hasNext()) {
        		member = iter.next();
        		if (!member.getUser().isBot())
        			members.add(member);
        	}
			makeStatisticTable(tableName, members);
			
			Date today = new Date(System.currentTimeMillis());
			
			stmt_str = "SELECT * FROM {0} WHERE date = ?";
			pstmt = conn.prepareStatement(MessageFormat.format(stmt_str, tableName));
			pstmt.setDate(1,  today);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				stmt_str = "UPDATE {0} SET {1} = ? WHERE date = ?";
				pstmt = conn.prepareStatement(MessageFormat.format(stmt_str, tableName, userName));
				
				pstmt.setString(1, CumulativeTime.valueOf(rs.getString(userName)).add(CumulativeTime.valueOf(data)).toString()); // 추후 각 데이터 별 데이터 삽입에 해당하는 코드로 변경
				pstmt.setDate(2,  today);
			}
			else {
				stmt_str = "INSERT INTO {0} SET date = ?, {1} = ?";
				pstmt = conn.prepareStatement(MessageFormat.format(stmt_str, tableName, userName));
				pstmt.setDate(1, today);
				pstmt.setString(2,  data);
			}
			int count = pstmt.executeUpdate();
			if (count == 0)
				System.out.println("data INSERT success : " + tableName);
			else
				System.out.println("data INSERT fail : " + tableName);
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (conn != null && !conn.isClosed())
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return true;
	}*/
	
}
