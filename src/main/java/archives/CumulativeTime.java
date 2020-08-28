package main.java.archives;

public class CumulativeTime extends Object{
	private long millisec = 0;

	public CumulativeTime(long time) {
		this.millisec = time;
	}
	public CumulativeTime() {
		this.millisec = 0;
	}
	
	@Override
	public String toString() {
		long tmp = millisec / 1000;
		long hour = tmp/(60*60);
		long min = tmp%(60*60) / 60; 
		long sec = tmp%(60*60) % 60;
		StringBuilder builder = new StringBuilder();
		
		if (hour < 10)
			builder.append("0");
		builder.append(hour);
		builder.append(":");

		if (min < 10)
			builder.append("0");
		builder.append(min);
		builder.append(":");

		if (sec < 10)
			builder.append("0");
		builder.append(sec);
		return builder.toString();
	}
	
	public CumulativeTime add(CumulativeTime ct) {
		this.millisec += ct.millisec;
		return this;
	}
	
	public static CumulativeTime valueOf(String s) {
		CumulativeTime ct = new CumulativeTime();
		String[] time_s = s.split(":");
		ct.millisec += Long.parseLong(time_s[0])*(60*60*1000);
		ct.millisec += Long.parseLong(time_s[1])*(60*1000);
		ct.millisec += Long.parseLong(time_s[2])*(1000);
		
		return ct;
	}
}
