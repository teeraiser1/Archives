package Archives;

public class MemeCmd {
		private String command;
    	private String fileName;
    	
    	public MemeCmd(String command, String fileName) {
    		this.command = command;
    		this.fileName = fileName;
		}
    	public MemeCmd(String[] memeCmd) {
    		this.command = memeCmd[0];
    		this.fileName = memeCmd[1];
		}
		public String getCommand() {
    		return command;
    	}
    	public String getFileName() {
    		return fileName;
    	}
}
