package main.java.archives;

public final class Constants {
	public static final class Files {
		public static final String ROOT_PATH = System.getProperty("user.dir") + "//";
		public static final String MEME_PATH = ROOT_PATH + "resources//meme//";
		public static final String MUSIC_PATH = ROOT_PATH + "resources//music//";
		public static final String DATA_PATH = ROOT_PATH + "data//";
		public static final String LOG_PATH = ROOT_PATH + "log//";
		public static final String MEME_DATAFILE = DATA_PATH + "meme.txt";
	}
	public static final class Extensions {
		public static String[] IMG = {"jpg", "jpeg", "png", "gif"};
		public static String[] TRACK = {"mp3", "flac", "wav"};
	}
	
	public static final class Max {
		public static int SERVER_ID_LENGTH = 10;
		public static int TRIAL = 5;
	}
}
