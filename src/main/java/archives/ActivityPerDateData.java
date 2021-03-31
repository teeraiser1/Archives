package main.java.archives;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Vector;

public class ActivityPerDateData {
	public Vector<ActivityTimePerDate> activityTimePerDateVector = new Vector<ActivityTimePerDate>();
	public String status;
	public String activity;
	
	public ActivityPerDateData(String status, String activity) {
		this.status = status;
		this.activity = activity;
	} 
	
	public void addActivityTimePerDate(LocalDate date, long totalMillisec) {
		activityTimePerDateVector.add(new ActivityTimePerDate(date, totalMillisec));
	}
	
	public class ActivityTimePerDate {
		public LocalDate date;
		public long totalMillisec;

		public ActivityTimePerDate(LocalDate date, long totalMillisec) {
			this.date = date;
			this.totalMillisec = totalMillisec;
		} 
	}
}
