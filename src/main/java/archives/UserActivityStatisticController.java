package main.java.archives;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.*;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.text.TextAnchor;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;

import main.java.archives.ActivityPerDateData.ActivityTimePerDate;
import main.java.archives.ArchivesThreads.ExpiredMemeCheckThread;
import main.java.archives.MemeCmdController.ActionResult;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ClientType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;;

public class UserActivityStatisticController {

	public static JDA jda;
	private static Vector<Long> users = new Vector<Long>();	// Ʈ��ŷ���� ������ ID ����

	public UserActivityStatisticController(JDA jda) {
		this.jda = jda;
	}
	// ����͸����� ���� ����Ʈ �ε�
    @DebugCommandHandler
	public static void loadMonitoringUsers() {
		ActionResult actionResult = new ActionResult();
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
			Connection conn = null;
			Statement stmt = null;
			PreparedStatement pstmt = null;
			int ret;
			ResultSet rs = null;
			
			conn = DriverManager.getConnection(PrivateData.DB.url_user_activity, PrivateData.DB.root, PrivateData.DB.pass);

			// 
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT user_id FROM user_activity.user_monitoring_status WHERE user_id = " + userId);
            if (rs.next())
            	return ;
            
			// ����͸� ���� ���̺� �ش� ���� ���̵�, ����� ���� ����
            pstmt = conn.prepareStatement("INSERT INTO user_activity.user_monitoring_status VALUES (?,?)");
            pstmt.setLong(1,  userId);
            pstmt.setBoolean(2, true);
            pstmt.executeUpdate();
			
            // ���� Ȱ�� ������ ���̺� ����
			stmt = conn.createStatement();
			ret = stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `user_activity`.`" + userId + "` (`timestamp` timestamp primary key, `status` text, `activity` text)");
			
			conn.close();
			
			actionResult.type_log = "Join User Activity Statistic Success : " + userName;
			actionResult.reason_log = null;
			actionResult.type_channel = userName + "���� ���� Ȱ�� ��� ����� ���۵Ǿ����ϴ�.";
			actionResult.reason_channel = null;
        	
		} catch (SQLException e) {
			actionResult.type_log = "Join User Activity Statistic fail : " + userName;
			actionResult.reason_log = e.toString();
			actionResult.type_channel = userName + "���� ���� Ȱ�� ��� ���ۿ� �����Ͽ����ϴ�.";
			actionResult.reason_channel = "DB ��� ����";
		}
    	MemeCmdController.recordActionResult(guild.getId().toString(), channel, actionResult);
    	
		users.add(userId);
	}

    // ���� ������ ���� �Ͻ�����
    @DebugCommandHandler
	public static void pauseMonitoring(Member member, Guild guild, MessageChannel channel) {
    	long userId = member.getIdLong();
    	String userName = member.getEffectiveName();
		if (!users.contains(userId))
			return ;
		
		ActionResult actionResult = new ActionResult();

		// ��� �ߴ��� ǥ��
		recordUserStatus(member, guild);
		recordSystemStatus(member, "PAUSE_RECORD", guild);
		
		try {
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
			actionResult.reason_log = e.toString();
			actionResult.type_channel = userName + "���� ���� Ȱ�� ��� ��� �Ͻ������� �����Ͽ����ϴ�.";
			actionResult.reason_channel = "DB ��� ����";
		}
    	MemeCmdController.recordActionResult(guild.getId().toString(), channel, actionResult);
	}	

    // ���� ������ ���� �����
    @DebugCommandHandler
	public static void resumeMonitoring(Member member, Guild guild, MessageChannel channel) {
    	long userId = member.getIdLong();
    	String userName = member.getEffectiveName();
		if (users.contains(userId))
			return ;
		
		ActionResult actionResult = new ActionResult();

		// ��� �ߴ��� ǥ��
		recordSystemStatus(member, "RESUME_RECORD", guild);
		recordUserStatus(member, guild);
		
		try {
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
			actionResult.reason_log = e.toString();
			actionResult.type_channel = userName + "���� ���� Ȱ�� ��� ��� ����ۿ� �����Ͽ����ϴ�.";
			actionResult.reason_channel = "DB ��� ����";
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
			actionResult.reason_log = e.toString();
			actionResult.type_channel = userName + "���� ���� Ȱ�� ��� ������ �ı⿡ �����Ͽ����ϴ�.";
			actionResult.reason_channel = "DB ��� ����";
		}
    	MemeCmdController.recordActionResult(guild.getId().toString(), channel, actionResult);
	}

    @DebugCommandHandler
	public static void recordUserStatus(Member targetMember, Guild guild) {
		long userId = targetMember.getIdLong();
		
		if (!users.contains(userId))
			return ;

		ActionResult actionResult = new ActionResult();
		try {
			Connection conn = null;
			Statement stmt = null;
			PreparedStatement pstmt = null;
			int ret;
			
			conn = DriverManager.getConnection(PrivateData.DB.url_user_activity, PrivateData.DB.root, PrivateData.DB.pass);
			
			// �ش� ������ ���̺� üũ
			stmt = conn.createStatement();
			ret = stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `user_activity`.`" + userId + "` (`timestamp` timestamp, `status` text, `activity` text)");
			
			// ������ ����
			pstmt = conn.prepareStatement("INSERT INTO user_activity." + userId + " VALUES(NOW(), ?, ?)");
			pstmt.setString(1, targetMember.getOnlineStatus(ClientType.DESKTOP).toString());
			if (!targetMember.getActivities().isEmpty())
				pstmt.setString(2, targetMember.getActivities().get(0).getName());
			else
				pstmt.setString(2, null);
				
			ret = pstmt.executeUpdate();
			
			conn.close();

			actionResult.type_log = "Record User Activity Data Success : " + targetMember.getEffectiveName();
			actionResult.reason_log = null;
			actionResult.type_channel = null;
			actionResult.reason_channel = null;
			
		} catch (SQLException e) {
			actionResult.type_log = "Record User Activity Data fail : " + targetMember.getEffectiveName();
			actionResult.reason_log = e.toString();
			actionResult.type_channel = null;
			actionResult.reason_channel = null;
			e.printStackTrace();
		}
    	MemeCmdController.recordActionResult(guild.getId().toString(), null, actionResult);
	}
    
    @DebugCommandHandler
	public static void recordSystemStatus(Member targetMember, String status, Guild guild) {
		long userId = targetMember.getIdLong();
		
		if (!users.contains(userId))
			return ;

		ActionResult actionResult = new ActionResult();
		try {
			Connection conn = null;
			Statement stmt = null;
			PreparedStatement pstmt = null;
			int ret;
			
			conn = DriverManager.getConnection(PrivateData.DB.url_user_activity, PrivateData.DB.root, PrivateData.DB.pass);
			
			// �ش� ������ ���̺� üũ
			stmt = conn.createStatement();
			ret = stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `user_activity`.`" + userId + "` (`timestamp` timestamp, `status` text, `activity` text)");
			
			// ������ ����
			pstmt = conn.prepareStatement("INSERT INTO user_activity." + userId + " VALUES(NOW(), ?, ?)");
			pstmt.setString(1, status);
				pstmt.setString(2, null);
				
			ret = pstmt.executeUpdate();
			
			conn.close();

			actionResult.type_log = "Record System Activity Data Success : " + targetMember.getEffectiveName();
			actionResult.reason_log = null;
			actionResult.type_channel = null;
			actionResult.reason_channel = null;
		} catch (SQLException e) {
			actionResult.type_log = "Record System Activity Data fail : " + targetMember.getEffectiveName();
			actionResult.reason_log = e.toString();
			actionResult.type_channel = null;
			actionResult.reason_channel = null;
			e.printStackTrace();
		}
    	MemeCmdController.recordActionResult(guild.getId().toString(), null, actionResult);
	}
    
    public static long extractDateDatatoMillis(Member member, LocalDate date, String dataType, Guild guild, MessageChannel channel) {
    	Vector<ActivityData> activityDataVector = new Vector<ActivityData>();
		long totalMillisec = 0;

		ActionResult actionResult = new ActionResult();
		try {
	    	Connection conn = null;
	    	PreparedStatement pstmt = null;
	    	Statement stmt = null;
	    	int ret;
	    	ResultSet rs = null;

			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	    	
			conn = DriverManager.getConnection(PrivateData.DB.url_user_activity, PrivateData.DB.root, PrivateData.DB.pass);
			
			pstmt = conn.prepareStatement("SELECT * FROM user_activity." + member.getIdLong() + " WHERE ( datetime BETWEEN ? AND ?)");	// ������ 00:00:00�� �����ͱ��� ����Ͽ� ��Ȯ�� ������ ������ ���� BETWEEN ���
			pstmt.setString(1, dateFormat.format(date));
			pstmt.setString(2, dateFormat.format(date.plusDays(1)));
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				ActivityData activityData = new ActivityData(rs.getTimestamp(1).toLocalDateTime(), rs.getString(2), rs.getString(3));
				activityDataVector.add(activityData);
			}

			for (int i = 0; i < activityDataVector.size() ; i++) {
				if (activityDataVector.get(i).status.equals(dataType.toUpperCase())) {
					if (i + 1 < activityDataVector.size())
						totalMillisec += Duration.between(activityDataVector.get(i).time, activityDataVector.get(i + 1).time).toMillis();
					else if (date.equals(LocalDate.now()))
						totalMillisec += Duration.between(activityDataVector.get(i).time, LocalDateTime.now()).toMillis();
				}
			}
			conn.close();

			actionResult.type_log = "Extract User Activity Date Data Success : " + member.getEffectiveName();
			actionResult.reason_log = null;
			actionResult.type_channel = null;
			actionResult.reason_channel = null;
		} catch (SQLException e) {
			actionResult.type_log = "Extract User Activity Date Data fail : " + member.getEffectiveName();
			actionResult.reason_log = e.toString();
			actionResult.type_channel = "��¥ ���� Ȱ�� ������ ���� ����";
			actionResult.reason_channel = "DB ��� ����";
			e.printStackTrace();
		}
    	MemeCmdController.recordActionResult(guild.getId().toString(), channel, actionResult);

    	if (actionResult.reason_log != null)
    		return -1;
    	
		return totalMillisec;
    }
    public static long extractTermDatatoMillis(Member member, LocalDate date_s, LocalDate date_e, String dataType, Guild guild, MessageChannel channel) {
    	Vector<ActivityData> activityDataVector = new Vector<ActivityData>();
		long totalMillisec = 0;

		ActionResult actionResult = new ActionResult();
		try {
	    	Connection conn = null;
	    	PreparedStatement pstmt = null;
	    	Statement stmt = null;
	    	int ret;
	    	ResultSet rs = null;

			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	    	
			conn = DriverManager.getConnection(PrivateData.DB.url_user_activity, PrivateData.DB.root, PrivateData.DB.pass);
			
			pstmt = conn.prepareStatement("SELECT * FROM user_activity." + member.getIdLong() + " WHERE ( datetime BETWEEN ? AND ?)");
			pstmt.setString(1, dateFormat.format(date_s));
			pstmt.setString(2, dateFormat.format(date_e.plusDays(1)));
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				ActivityData activityData = new ActivityData(rs.getTimestamp(1).toLocalDateTime(), rs.getString(2), rs.getString(3));
				activityDataVector.add(activityData);
			}

			for (int i = 0; i < activityDataVector.size() ; i++) {
				if (activityDataVector.get(i).status.equals(dataType.toUpperCase())) {
					if (i + 1 < activityDataVector.size())
						totalMillisec += Duration.between(activityDataVector.get(i).time, activityDataVector.get(i + 1).time).toMillis();
					else if (date_e.equals(LocalDate.now()))
						totalMillisec += Duration.between(activityDataVector.get(i).time, LocalDateTime.now()).toMillis();
				}
			}
			conn.close();

			actionResult.type_log = "Extract User Activity Term Data Success : " + member.getEffectiveName();
			actionResult.reason_log = null;
			actionResult.type_channel = null;
			actionResult.reason_channel = null;
		} catch (SQLException e) {
			actionResult.type_log = "Extract User Activity Term Data fail : " + member.getEffectiveName();
			actionResult.reason_log = e.toString();
			actionResult.type_channel = "���� ���� Ȱ�� ������ ���� ����";
			actionResult.reason_channel = "DB ��� ����";
			e.printStackTrace();
		}
    	MemeCmdController.recordActionResult(guild.getId().toString(), channel, actionResult);
    	
    	if (actionResult.reason_log != null)
    		return -1;
    	
		return totalMillisec;
    }
    public static ActivityPerDateData extractAllTermDatatoMillis(Member member, String dataType, Guild guild, MessageChannel channel) {
    	Vector<ActivityData> activityDataVector = new Vector<ActivityData>();
		long totalMillisec = 0;
		
		ActivityPerDateData activityPerDateData = new ActivityPerDateData(dataType, null);

		ActionResult actionResult = new ActionResult();
		try {
	    	Connection conn = null;
	    	PreparedStatement pstmt = null;
	    	Statement stmt = null;
	    	int ret;
	    	ResultSet rs = null;

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    	
			conn = DriverManager.getConnection(PrivateData.DB.url_user_activity, PrivateData.DB.root, PrivateData.DB.pass);
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM user_activity." + member.getIdLong());

			while(rs.next()) {
				ActivityData activityData = new ActivityData(rs.getTimestamp(1).toLocalDateTime(), rs.getString(2), rs.getString(3));
				activityDataVector.add(activityData);
			}

			LocalDate localDate;
			LocalDate targetLocalDate = LocalDate.now();

			if (!activityDataVector.isEmpty()) {
				localDate = activityDataVector.get(0).time.toLocalDate();
				targetLocalDate = LocalDate.of(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
			}
			
			for (int i = 0; i < activityDataVector.size() ; i++) {
				localDate = activityDataVector.get(i).time.toLocalDate();
				if (!localDate.equals(targetLocalDate)) { 					// localDate�� �ٸ� ��¥�� ���
					activityPerDateData.addActivityTimePerDate(targetLocalDate, totalMillisec);	// ������� �ش� ��¥�� �հ�� Ȱ�� �ð� ����
					targetLocalDate = LocalDate.of(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());	// ���� ��¥�� ����
					totalMillisec = 0;
				}
				if (activityDataVector.get(i).status.equals(dataType.toUpperCase())) {
					if (i + 1 < activityDataVector.size())
						totalMillisec += Duration.between(activityDataVector.get(i).time, activityDataVector.get(i + 1).time).toMillis();
					else
						totalMillisec += Duration.between(activityDataVector.get(i).time, LocalDateTime.now()).toMillis();
				}
			}

			activityPerDateData.addActivityTimePerDate(targetLocalDate, totalMillisec);	// ������� �ش� ��¥�� �հ�� Ȱ�� �ð� ����
			conn.close();

			actionResult.type_log = "Extract User Activity All Term Data Success : " + member.getEffectiveName();
			actionResult.reason_log = null;
			actionResult.type_channel = null;
			actionResult.reason_channel = null;
		} catch (SQLException e) {
			actionResult.type_log = "Extract User Activity All Term Data fail : " + member.getEffectiveName();
			actionResult.reason_log = e.toString();
			actionResult.type_channel = "��ü ���� Ȱ�� ������ ���� ����";
			actionResult.reason_channel = "DB ��� ����";
			e.printStackTrace();
		}
    	MemeCmdController.recordActionResult(guild.getId().toString(), channel, actionResult);

		return activityPerDateData;
    }
    
	public static void RecordEveryUsersCurrentStatus() {
		Vector<Long> checkedList = new Vector<Long>();
		for (Guild guild : jda.getGuilds()) {
			for (long userId : users) {
				if (!checkedList.contains(userId)) {
					recordUserStatus(guild.getMember(jda.getUserById(userId)), guild);
					checkedList.add(userId);
				}
			}
		}
	}	
	
	public static void RecordSystemOffStatus() {
		Vector<Long> checkedList = new Vector<Long>();
		for (Guild guild : jda.getGuilds()) {
			for (long userId : users) {
				if (!checkedList.contains(userId)) {
					recordSystemStatus(guild.getMember(jda.getUserById(userId)), "SYSTEM_OFF", guild);
					checkedList.add(userId);
				}
			}
		}
	}
	
	public static File exportActivityDataToXlsx(Member member, ActivityPerDateData activityPerDateData, Guild guild, MessageChannel channel) {
		SXSSFWorkbook workbook = new SXSSFWorkbook();
		String sheetName = new String(activityPerDateData.status);
		if (activityPerDateData.activity != null)
			sheetName = sheetName.concat(" - " + activityPerDateData.activity);
		SXSSFSheet sheet = workbook.createSheet(sheetName);

		Row row = getRow(sheet, 0);
		getCell(row, 0).setCellValue("��¥");
		getCell(row, 1).setCellValue("�ð�");
		
		for (int i = 0; i < activityPerDateData.activityTimePerDateVector.size(); i++) {
			ActivityTimePerDate  activityTimePerDate = activityPerDateData.activityTimePerDateVector.get(i);
			
			row = getRow(sheet, i + 1);
			getCell(row, 0).setCellValue(activityTimePerDate.date.toString());
			getCell(row, 1).setCellValue(activityTimePerDate.date.toString());
		}
		sheet.trackColumnForAutoSizing(0);
		sheet.trackColumnForAutoSizing(1);
		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);
		
		String fileName = new String(Constants.Files.STATISTIC_PATH + member.getIdLong() + "_" + activityPerDateData.status);
		if (activityPerDateData.activity != null)
			fileName = fileName.concat("-" + activityPerDateData.activity);
		fileName = fileName.concat(".xlsx");
		
		FileOutputStream stream = null;
		ActionResult actionResult = new ActionResult();
		try {
			stream = new FileOutputStream(fileName);
			if (stream != null)
				workbook.write(stream);
			stream.close();
			workbook.close();

			actionResult.type_log = "Export User Activity Data to Xlsx Success : " + member.getEffectiveName();
			actionResult.reason_log = null;
			actionResult.type_channel = null;
			actionResult.reason_channel = null;
		} catch (IOException e) {
			actionResult.type_log = "Export User Activity Data to Xlsx fail : " + member.getEffectiveName();
			actionResult.reason_log = e.toString();
			actionResult.type_channel = "Xlsx ���� ���� ����";
			actionResult.reason_channel = "������ ���� ��� ����";
			e.printStackTrace();
		}
    	MemeCmdController.recordActionResult(guild.getId().toString(), channel, actionResult);
		
		return new File(fileName);
	}
	
	private static Row getRow(Sheet sheet, int rowNum) {
		if (sheet == null)
			return null;

		Row row = sheet.getRow(rowNum);
		if (row == null)
			row = sheet.createRow(rowNum);
		
		return row;
	}	
	private static Cell getCell(Row row, int coulumNum) {
		if (row == null)
			return null;
		
		Cell cell = row.getCell(coulumNum);
		if (cell == null)
			cell = row.createCell(coulumNum);
		
		return cell;
	}
	static File makeDayUserActivityChartPng(LocalDate date_to, ActivityPerDateData dayActivityData, Member member, Guild guild, MessageChannel channel) {
		DefaultCategoryDataset<String, String> prevWeekAvg_dataset = new DefaultCategoryDataset<String, String>();
		DefaultCategoryDataset<String, String> prevMonthAvg_dataset = new DefaultCategoryDataset<String, String>();
		DefaultCategoryDataset<String, String> prevYearAvg_dataset = new DefaultCategoryDataset<String, String>();
		DefaultCategoryDataset<String, String> day_dataset = new DefaultCategoryDataset<String, String>();

		LineAndShapeRenderer day_renderer = new LineAndShapeRenderer();
		LineAndShapeRenderer week_renderer = new LineAndShapeRenderer();
		LineAndShapeRenderer month_renderer = new LineAndShapeRenderer();
		LineAndShapeRenderer year_renderer = new LineAndShapeRenderer();

		LocalDate week_before = date_to.minusDays(Constants.Number.DAYS_OF_WEEK);
		LocalDate month_before = date_to.minusDays(Constants.Number.DAYS_OF_MONTH);
		LocalDate year_before = date_to.minusDays(Constants.Number.DAYS_OF_YEAR);

		long weekMillisec = 0;
		long monthMillisec = 0;
		long yearMillisec = 0;

		int countedWeekDay = 0;
		int countedMonthDay = 0; 
		int countedYearDay = 0;
		
		// ��, ��, ���� �� ��� Ȱ�� �ð� ���
		for (ActivityTimePerDate activityTimePerDate : dayActivityData.activityTimePerDateVector) {

			if ( (activityTimePerDate.date.isAfter(week_before)) 
					&& (activityTimePerDate.date.isEqual(date_to) || activityTimePerDate.date.isBefore(date_to)) ) {
				weekMillisec += activityTimePerDate.totalMillisec;
				countedWeekDay++;
			}
			if ( (activityTimePerDate.date.isAfter(month_before)) 
					&& (activityTimePerDate.date.isEqual(date_to) || activityTimePerDate.date.isBefore(date_to)) ) {
				monthMillisec += activityTimePerDate.totalMillisec;
				countedMonthDay++;
			}
			if ( (activityTimePerDate.date.isAfter(year_before)) 
					&& (activityTimePerDate.date.isEqual(date_to) || activityTimePerDate.date.isBefore(date_to)) ) {
				yearMillisec += activityTimePerDate.totalMillisec;
				countedYearDay++;
			}
		}
		weekMillisec /= countedWeekDay;
		monthMillisec /= countedMonthDay;
		yearMillisec /= countedYearDay;
		
		// dataset�� ������ ����
		for (ActivityTimePerDate activityTimePerDate : dayActivityData.activityTimePerDateVector) {
			if ( (activityTimePerDate.date.isAfter(week_before)) 
					&& (activityTimePerDate.date.isEqual(date_to) || activityTimePerDate.date.isBefore(date_to)) ) {
				prevWeekAvg_dataset.addValue(weekMillisec, "�ֱ� 7�� �� ��� Ȱ�� �ð�", activityTimePerDate.date.toString());
				prevMonthAvg_dataset.addValue(monthMillisec, "�ֱ� 30�� �� ��� Ȱ�� �ð�", activityTimePerDate.date.toString());
				prevYearAvg_dataset.addValue(yearMillisec, "�ֱ� 365�� �� ��� Ȱ�� �ð�", activityTimePerDate.date.toString());
				day_dataset.addValue(activityTimePerDate.totalMillisec, "�� ��� Ȱ�� �ð�", activityTimePerDate.date.toString());
			}
		}
		
        initDefaultRendererOption(day_renderer);
        initDefaultRendererOption(week_renderer);
        initDefaultRendererOption(month_renderer);
        initDefaultRendererOption(year_renderer);

        day_renderer.setSeriesPaint(0, Color.BLACK);
        week_renderer.setSeriesPaint(0, Color.RED);
        month_renderer.setSeriesPaint(0, Color.ORANGE);
        year_renderer.setSeriesPaint(0, new Color(180, 85, 162));

        CategoryPlot<String, String> plot = new CategoryPlot<String, String>();
        initDefaultCategoryPlotOption(plot);
        
        plot.setDataset(0, day_dataset);
        plot.setRenderer(0,  day_renderer);
        plot.setDataset(1, prevWeekAvg_dataset);
        plot.setRenderer(1,  week_renderer);
        plot.setDataset(2, prevMonthAvg_dataset);
        plot.setRenderer(2,  month_renderer);
        plot.setDataset(3, prevYearAvg_dataset);
        plot.setRenderer(3,  year_renderer);     

        JFreeChart chart = new JFreeChart(plot);
        chart.setTitle(dayActivityData.status + " Ȱ�� ���"); // ��Ʈ Ÿ��Ʋ
        chart.getTitle().setFont(new Font("����", Font.BOLD, 20));
        chart.getLegend().setItemFont(new Font("����", Font.BOLD, 20));
        
        File targetFile = new File(Constants.Files.STATISTIC_PATH + "chart.png");
		ActionResult actionResult = new ActionResult();
        try {
			ChartUtils.saveChartAsPNG(targetFile, chart, 1000, 600);

			actionResult.type_log = "Make User Activity Data to Chart Success : " + member.getEffectiveName();
			actionResult.reason_log = null;
			actionResult.type_channel = null;
			actionResult.reason_channel = null;
		} catch (IOException e) {
			actionResult.type_log = "Make User Activity Data to Chart fail : " + member.getEffectiveName();
			actionResult.reason_log = e.toString();
			actionResult.type_channel = "��Ʈ �̹��� ������ �����Ͽ����ϴ�.";
			actionResult.reason_channel = "��Ʈ�� �̹����� ���� ���� ";
			e.printStackTrace();
		}
    	MemeCmdController.recordActionResult(guild.getId().toString(), channel, actionResult);
        
		return targetFile;
	}
	
	private static void initDefaultRendererOption(LineAndShapeRenderer renderer) {
        CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING, new CumulativeTime());
        ItemLabelPosition p_top = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER);
        ItemLabelPosition p_below = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE6, TextAnchor.TOP_LEFT);
        Font f = new Font("����", Font.BOLD, 14);
        
		renderer.setDefaultItemLabelGenerator(generator);
		renderer.setDefaultItemLabelsVisible(true);
		renderer.setDefaultShapesVisible(true);   
		renderer.setDrawOutlines(true);
		renderer.setUseFillPaint(true);
		renderer.setDefaultFillPaint(Color.WHITE);
        renderer.setDefaultPositiveItemLabelPosition(p_top);
        renderer.setDefaultItemLabelFont(f);     
        renderer.setSeriesStroke(0, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 3.0f));
	}
	
	private static void initDefaultCategoryPlotOption(CategoryPlot<String, String> plot) {
        Font f = new Font("����", Font.BOLD, 14);
        
        NumberAxis rangeAxis = new NumberAxis();
        rangeAxis.setNumberFormatOverride(new CumulativeTime());
        rangeAxis.setLabelFont(f);
        rangeAxis.setTickLabelFont(f);
        rangeAxis.setTickLabelsVisible(true);
        plot.setRangeAxis(rangeAxis);
        
        CategoryAxis domainAxis = new CategoryAxis();
        domainAxis.setLabelFont(f);
        domainAxis.setTickLabelFont(f);
        domainAxis.setTickLabelsVisible(true);
        plot.setDomainAxis(domainAxis);
        
        plot.setOrientation(PlotOrientation.VERTICAL);             // �׷��� ǥ�� ����
        plot.setRangeGridlinesVisible(true);                       // X�� ���̵� ���� ǥ�ÿ���
        plot.setRangeGridlineStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 3.0f));
        plot.setDomainGridlinesVisible(true);                      // Y�� ���̵� ���� ǥ�ÿ���
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.STANDARD);       // ī�װ� �� ��ġ ����
	}
	
	// ���� 1�� ���� �������� Ȱ����踦 ����ϴ� ������. �����͸� ��¥���� �����ϱ� ���� �۾�
	public static class AutoMidnightRecordThread_beforeMidnight extends Thread {
		public static AutoMidnightRecordThread_beforeMidnight getInstance() {
			return new AutoMidnightRecordThread_beforeMidnight();
		}
		
		public void run() {			
			while (true) {
				Calendar now_c = Calendar.getInstance();
				now_c.setTimeInMillis(System.currentTimeMillis());
				
	    		Calendar beforeMidnight = Calendar.getInstance();
	    		beforeMidnight.set(now_c.get(Calendar.YEAR), now_c.get(Calendar.MONTH), now_c.get(Calendar.DAY_OF_MONTH), 23, 59, 0);
				
				try {
					if (now_c.equals(beforeMidnight))
						RecordEveryUsersCurrentStatus();
					
					now_c.setTimeInMillis(System.currentTimeMillis());
					long targetSleepMillis = beforeMidnight.getTimeInMillis() - now_c.getTimeInMillis();
					if (targetSleepMillis <= 0)
						targetSleepMillis += 24*60*60*1000; // �Ϸ� �߰�
					Thread.sleep(targetSleepMillis);
				} catch (InterruptedException e) {
	
					e.printStackTrace();
				}
			}
		}
	}
	
	// ������ �������� Ȱ����踦 ����ϴ� ������. �����͸� ��¥���� �����ϱ� ���� �۾�
	public static class AutoMidnightRecordThread_midnight extends Thread {
		public static AutoMidnightRecordThread_midnight getInstance() {
			return new AutoMidnightRecordThread_midnight();
		}
		
		public void run() {			
			while (true) {
				Calendar now_c = Calendar.getInstance();
				now_c.setTimeInMillis(System.currentTimeMillis());
				
	    		Calendar midnight = Calendar.getInstance();
	    		midnight.set(now_c.get(Calendar.YEAR), now_c.get(Calendar.MONTH), now_c.get(Calendar.DAY_OF_MONTH) + 1, 0, 0, 0);
				
				try {
					if (now_c.equals(midnight))
						RecordEveryUsersCurrentStatus();
					
					now_c.setTimeInMillis(System.currentTimeMillis());
					long targetSleepMillis = midnight.getTimeInMillis() - now_c.getTimeInMillis();
					if (targetSleepMillis <= 0)
						targetSleepMillis += 24*60*60*1000; // �Ϸ� �߰�
					Thread.sleep(targetSleepMillis);
				} catch (InterruptedException e) {
	
					e.printStackTrace();
				}
			}
		}
	}
}
