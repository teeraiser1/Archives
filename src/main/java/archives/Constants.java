package main.java.archives;

public final class Constants {
	static final class Files {
		static final String ROOT_PATH = System.getProperty("user.dir") + "//";
		static final String MEME_PATH = ROOT_PATH + "resources//meme//";
		static final String MUSIC_PATH = ROOT_PATH + "resources//music//";
		static final String DATA_PATH = ROOT_PATH + "data//";
		static final String MEME_DATAFILE = DATA_PATH + "meme.txt";
	}
	static final class Extensions {
		static String[] IMG = {"jpg", "jpeg", "png", "gif"};
		static String[] TRACK = {"mp3", "flac", "wav"};
	}
}
