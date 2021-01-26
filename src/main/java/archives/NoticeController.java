package main.java.archives;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;

public class NoticeController {

    public static JDA jda = null;
    
    public NoticeController(JDA jda) {
    	this.jda = jda;
    }
    
    public static TextChannel getNoticeChannel(Guild guild) {
        List<TextChannel> noticeChannelList = guild.getTextChannelsByName(Constants.Name.BOT_NOTICE_CHANNEL, false);
        TextChannel noticeChannel = null;
        if (noticeChannelList.isEmpty())
        	noticeChannel = guild.createTextChannel(Constants.Name.BOT_NOTICE_CHANNEL).complete();
        else
        	noticeChannel = noticeChannelList.get(0);
        
        if (noticeChannel == null)
        	Logger.writeLog("log_" + guild.getId(), "공지방을 찾을 수 없음");
		
        return noticeChannel;
    }
    
    public static void NotifyArchivesConnected() {
		for(Guild guild : jda.getGuilds()) {
			MessageChannel noticeChannel = getNoticeChannel(guild);
			if (noticeChannel != null) {
				File targetFile = new File(Constants.Files.MEME_COMMON_PATH + "matrix.gif");
				if (targetFile.exists()) {
					noticeChannel.sendFile(targetFile).queue();
					noticeChannel.sendMessage("기록보관소 가동 ").queue();
				}
					
				NotifyUpdatedFunction();
			}
		}
	}
	
    public static void NotifyArchivesDisconnected() {
		for(Guild guild : jda.getGuilds()) {
			MessageChannel noticeChannel = getNoticeChannel(guild);
			if (noticeChannel != null) {
				File targetFile = new File(Constants.Files.MEME_COMMON_PATH + "terminated.gif");
				if (targetFile.exists()) {
					noticeChannel.sendFile(targetFile).queue();
					noticeChannel.sendMessage("기록보관소 중지").queue();
				}
					
			}
		}
	}
    
    public static void NotifyUpdatedFunction() {
		for(Guild guild : jda.getGuilds()) {
			MessageChannel noticeChannel = getNoticeChannel(guild);
			if (noticeChannel != null)
		    	ArchivesCommandController.ShowUpdateFunction(noticeChannel);
		}
    }
}
