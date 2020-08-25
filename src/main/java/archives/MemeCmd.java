package main.java.archives;

import java.sql.Date;

public class MemeCmd {
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
