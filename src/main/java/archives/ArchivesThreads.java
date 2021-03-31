package main.java.archives;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.managers.AudioManager;

public class ArchivesThreads {    
	
	public static JDA jda;
	
    static class ExpiredMemeCheckThread extends Thread {    
    	
    	public static ExpiredMemeCheckThread getInstance() {
    		return new ExpiredMemeCheckThread();
    	}
    	
    	public void run() {
    		while(true) {
    			LocalTime now = LocalTime.now();
    			long sleepMillisec;
	    		try {
	    			if (!(now.getHour() == 0 && now.getMinute() == 0 && now.getSecond() == 0)) {
		    			sleepMillisec = 24*60*60*1000 - now.getHour()*60*60*1000 - now.getMinute()*60*1000 - now.getSecond()*1000 - now.getNano()/1000000;
						Thread.sleep(sleepMillisec);
	    			}
				} catch (InterruptedException e) {
					System.out.println("InterruptedException : " + e.toString());
					e.printStackTrace();
				}
	    		
	    		for(Guild guild : jda.getGuilds()) {
	    			MessageChannel noticeChannel = NoticeController.getNoticeChannel(guild);
	    			if (noticeChannel != null)
	    				MemeCmdController.checkExpiredMeme(noticeChannel, guild.getId());
	    		}
    		}
    		
    	}
    	
    }
    
    public static void RunExpiredMemeCheckThread() {
    	ExpiredMemeCheckThread.getInstance().start();
    }

    static class AfkVoiceChannelCheckThread extends Thread {    	
    	
    	public static AfkVoiceChannelCheckThread getInstance() {
    		return new AfkVoiceChannelCheckThread();
    	}
    	
    	public void run() {
    		while (true) {
	    		AudioManager audioManager;
	    		
				try {
		    		for(Guild guild : jda.getGuilds()) {
		    			audioManager = guild.getAudioManager();
		    			if (ArchivesCommandController.checkAudioManagerAlone(audioManager))
		        				audioManager.closeAudioConnection();
		    		}
		    		
					Thread.sleep(Constants.Max.AFK_VOICE_SEC * 1000);
				} catch (InterruptedException e) {
					System.out.println("InterruptedException : " + e.toString());
					e.printStackTrace();
				}
    		}
    	}
    	
    }
    
    public static void RunAfkVoiceChannelCheckThread() {
    	AfkVoiceChannelCheckThread.getInstance().start();
    }
}
