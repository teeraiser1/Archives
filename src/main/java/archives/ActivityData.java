package main.java.archives;

import java.time.LocalDateTime;
import java.util.Date;

public class ActivityData {
	public LocalDateTime time;
	public String status;
	public String activity;
	
	public ActivityData(LocalDateTime time, String status, String activity) {
		this.time = time;
		this.status = status;
		this.activity = activity;
	}
}
