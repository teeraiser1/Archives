package main.java.archives;

import java.sql.Date;
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
    		Date now_d = new Date(System.currentTimeMillis());
    		
    		Calendar now_c = Calendar.getInstance();
    		now_c.setTime(now_d);
    		Calendar midnight = Calendar.getInstance();
    	
    		midnight.set(now_c.get(Calendar.YEAR), now_c.get(Calendar.MONTH), now_c.get(Calendar.DAY_OF_MONTH), now_c.get(Calendar.HOUR_OF_DAY) + 1, 0, 0);
    		try {
				Thread.sleep(midnight.getTime().getTime() - now_d.getTime());
			} catch (InterruptedException e) {
				System.out.println("InterruptedException : " + e.toString());
				e.printStackTrace();
			}
    		
    		for(Guild guild : jda.getGuilds()) {
    			MessageChannel noticeChannel = NoticeController.getNoticeChannel(guild);
    			if (noticeChannel != null)
    				MemeCmdController.checkExpiredMeme(noticeChannel, guild.getId());
    		}
    		
    		this.run();
    		
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
			
    		this.run();
    		
    	}
    	
    }
    
    public static void RunAfkVoiceChannelCheckThread() {
    	AfkVoiceChannelCheckThread.getInstance().start();
    }
}
