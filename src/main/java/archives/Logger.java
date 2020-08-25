package main.java.archives;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

	public static void writeLog(String logFileName, String log) {
		
		File logPath = new File(Constants.Files.LOG_PATH);
		File logFile = new File(Constants.Files.LOG_PATH + logFileName);
		SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		StringBuilder builder = new StringBuilder();;
		Date now = new Date(System.currentTimeMillis());

		try {
			if (!logPath.exists() || logPath.isDirectory()) {
				logPath.mkdir();
				System.out.println(logPath.getPath());
			}
			if (!logFile.exists() || logPath.isFile()) {
				logFile.createNewFile();
				System.out.println(logFile.getPath());
			}
			
			builder.append("[" + time.format(now).toString() + "] ")
					.append("[" + new Throwable().getStackTrace()[1].getMethodName() + "] ")
					.append(log);
			FileWriter fw = new FileWriter(logFile, true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(builder.toString());
			bw.newLine();
			bw.flush();
			bw.close();
			fw.close();
		}
		catch (IOException e) {
			System.out.println("IOException occur : " + e.toString());
			e.printStackTrace();
		}
	}
}
