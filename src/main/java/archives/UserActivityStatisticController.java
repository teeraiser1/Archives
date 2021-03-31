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
	private static Vector<Long> users = new Vector<Long>();	// 트래킹중인 유저의 ID 벡터

	public UserActivityStatisticController(JDA jda) {
		this.jda = jda;
	}
	// 모니터링중인 유저 리스트 로드
    @DebugCommandHandler
	public static void loadMonitoringUsers() {
		ActionResult actionResult = new ActionResult();
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
            
			// 모니터링 유저 테이블에 해당 유저 아이디, 모니터 상태 저장
            pstmt = conn.prepareStatement("INSERT INTO user_activity.user_monitoring_status VALUES (?,?)");
            pstmt.setLong(1,  userId);
            pstmt.setBoolean(2, true);
            pstmt.executeUpdate();
			
            // 유저 활동 데이터 테이블 생성
			stmt = conn.createStatement();
			ret = stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `user_activity`.`" + userId + "` (`timestamp` timestamp primary key, `status` text, `activity` text)");
			
			conn.close();
			
			actionResult.type_log = "Join User Activity Statistic Success : " + userName;
			actionResult.reason_log = null;
			actionResult.type_channel = userName + "님의 유저 활동 통계 기록이 시작되었습니다.";
			actionResult.reason_channel = null;
        	
		} catch (SQLException e) {
			actionResult.type_log = "Join User Activity Statistic fail : " + userName;
			actionResult.reason_log = e.toString();
			actionResult.type_channel = userName + "님의 유저 활동 통계 시작에 실패하였습니다.";
			actionResult.reason_channel = "DB 통신 실패";
		}
    	MemeCmdController.recordActionResult(guild.getId().toString(), channel, actionResult);
    	
		users.add(userId);
	}

    // 유저 데이터 수집 일시중지
    @DebugCommandHandler
	public static void pauseMonitoring(Member member, Guild guild, MessageChannel channel) {
    	long userId = member.getIdLong();
    	String userName = member.getEffectiveName();
		if (!users.contains(userId))
			return ;
		
		ActionResult actionResult = new ActionResult();

		// 기록 중단을 표기
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
			actionResult.type_channel = userName + "님의 유저 활동 통계 기록이 일시중지되었습니다.";
			actionResult.reason_channel = null;
			
		} catch (SQLException e) {
			actionResult.type_log = "Pause Monitoring fail : " + userName;
			actionResult.reason_log = e.toString();
			actionResult.type_channel = userName + "님의 유저 활동 통계 기록 일시정지에 실패하였습니다.";
			actionResult.reason_channel = "DB 통신 실패";
		}
    	MemeCmdController.recordActionResult(guild.getId().toString(), channel, actionResult);
	}	

    // 유저 데이터 수집 재시작
    @DebugCommandHandler
	public static void resumeMonitoring(Member member, Guild guild, MessageChannel channel) {
    	long userId = member.getIdLong();
    	String userName = member.getEffectiveName();
		if (users.contains(userId))
			return ;
		
		ActionResult actionResult = new ActionResult();

		// 기록 중단을 표기
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
			actionResult.type_channel = userName + "님의 유저 활동 통계 기록이 재시작되었습니다.";
			actionResult.reason_channel = null;
        	
		} catch (SQLException e) {
			actionResult.type_log = "Resume Monitoring fail : " + userName;
			actionResult.reason_log = e.toString();
			actionResult.type_channel = userName + "님의 유저 활동 통계 기록 재시작에 실패하였습니다.";
			actionResult.reason_channel = "DB 통신 실패";
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
			actionResult.reason_log = e.toString();
			actionResult.type_channel = userName + "님의 유저 활동 통계 데이터 파기에 실패하였습니다.";
			actionResult.reason_channel = "DB 통신 실패";
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
			
			// 해당 유저의 테이블 체크
			stmt = conn.createStatement();
			ret = stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `user_activity`.`" + userId + "` (`timestamp` timestamp, `status` text, `activity` text)");
			
			// 데이터 저장
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
			
			// 해당 유저의 테이블 체크
			stmt = conn.createStatement();
			ret = stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `user_activity`.`" + userId + "` (`timestamp` timestamp, `status` text, `activity` text)");
			
			// 데이터 저장
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
			
			pstmt = conn.prepareStatement("SELECT * FROM user_activity." + member.getIdLong() + " WHERE ( datetime BETWEEN ? AND ?)");	// 다음날 00:00:00의 데이터까지 고려하여 정확한 데이터 추출을 위해 BETWEEN 사용
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
			actionResult.type_channel = "날짜 유저 활동 데이터 추출 실패";
			actionResult.reason_channel = "DB 통신 실패";
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
			actionResult.type_channel = "범위 유저 활동 데이터 추출 실패";
			actionResult.reason_channel = "DB 통신 실패";
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
				if (!localDate.equals(targetLocalDate)) { 					// localDate와 다른 날짜일 경우
					activityPerDateData.addActivityTimePerDate(targetLocalDate, totalMillisec);	// 현재까지 해당 날짜에 합계된 활동 시간 정리
					targetLocalDate = LocalDate.of(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());	// 다음 날짜로 변경
					totalMillisec = 0;
				}
				if (activityDataVector.get(i).status.equals(dataType.toUpperCase())) {
					if (i + 1 < activityDataVector.size())
						totalMillisec += Duration.between(activityDataVector.get(i).time, activityDataVector.get(i + 1).time).toMillis();
					else
						totalMillisec += Duration.between(activityDataVector.get(i).time, LocalDateTime.now()).toMillis();
				}
			}

			activityPerDateData.addActivityTimePerDate(targetLocalDate, totalMillisec);	// 현재까지 해당 날짜에 합계된 활동 시간 정리
			conn.close();

			actionResult.type_log = "Extract User Activity All Term Data Success : " + member.getEffectiveName();
			actionResult.reason_log = null;
			actionResult.type_channel = null;
			actionResult.reason_channel = null;
		} catch (SQLException e) {
			actionResult.type_log = "Extract User Activity All Term Data fail : " + member.getEffectiveName();
			actionResult.reason_log = e.toString();
			actionResult.type_channel = "전체 유저 활동 데이터 추출 실패";
			actionResult.reason_channel = "DB 통신 실패";
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
		getCell(row, 0).setCellValue("날짜");
		getCell(row, 1).setCellValue("시간");
		
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
			actionResult.type_channel = "Xlsx 파일 생성 실패";
			actionResult.reason_channel = "데이터 파일 출력 실패";
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
		
		// 주, 월, 연의 일 평균 활동 시간 계산
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
		
		// dataset에 데이터 삽입
		for (ActivityTimePerDate activityTimePerDate : dayActivityData.activityTimePerDateVector) {
			if ( (activityTimePerDate.date.isAfter(week_before)) 
					&& (activityTimePerDate.date.isEqual(date_to) || activityTimePerDate.date.isBefore(date_to)) ) {
				prevWeekAvg_dataset.addValue(weekMillisec, "최근 7일 일 평균 활동 시간", activityTimePerDate.date.toString());
				prevMonthAvg_dataset.addValue(monthMillisec, "최근 30일 일 평균 활동 시간", activityTimePerDate.date.toString());
				prevYearAvg_dataset.addValue(yearMillisec, "최근 365일 일 평균 활동 시간", activityTimePerDate.date.toString());
				day_dataset.addValue(activityTimePerDate.totalMillisec, "일 평균 활동 시간", activityTimePerDate.date.toString());
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
        chart.setTitle(dayActivityData.status + " 활동 통계"); // 차트 타이틀
        chart.getTitle().setFont(new Font("돋움", Font.BOLD, 20));
        chart.getLegend().setItemFont(new Font("돋움", Font.BOLD, 20));
        
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
			actionResult.type_channel = "차트 이미지 생성에 실패하였습니다.";
			actionResult.reason_channel = "차트를 이미지로 저장 실패 ";
			e.printStackTrace();
		}
    	MemeCmdController.recordActionResult(guild.getId().toString(), channel, actionResult);
        
		return targetFile;
	}
	
	private static void initDefaultRendererOption(LineAndShapeRenderer renderer) {
        CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING, new CumulativeTime());
        ItemLabelPosition p_top = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER);
        ItemLabelPosition p_below = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE6, TextAnchor.TOP_LEFT);
        Font f = new Font("돋움", Font.BOLD, 14);
        
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
        Font f = new Font("돋움", Font.BOLD, 14);
        
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
        
        plot.setOrientation(PlotOrientation.VERTICAL);             // 그래프 표시 방향
        plot.setRangeGridlinesVisible(true);                       // X축 가이드 라인 표시여부
        plot.setRangeGridlineStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 3.0f));
        plot.setDomainGridlinesVisible(true);                      // Y축 가이드 라인 표시여부
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.STANDARD);       // 카테고리 라벨 위치 조정
	}
	
	// 자정 1초 전에 유저들의 활동통계를 기록하는 쓰레드. 데이터를 날짜별로 구분하기 위한 작업
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
						targetSleepMillis += 24*60*60*1000; // 하루 추가
					Thread.sleep(targetSleepMillis);
				} catch (InterruptedException e) {
	
					e.printStackTrace();
				}
			}
		}
	}
	
	// 자정에 유저들의 활동통계를 기록하는 쓰레드. 데이터를 날짜별로 구분하기 위한 작업
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
						targetSleepMillis += 24*60*60*1000; // 하루 추가
					Thread.sleep(targetSleepMillis);
				} catch (InterruptedException e) {
	
					e.printStackTrace();
				}
			}
		}
	}
}
