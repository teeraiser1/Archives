package main.java.archives;

public class HelpCommand {
	static String version = Archives.ArchivesVersion;
	
	public static String getArchiveHelp() {
		StringBuilder builder = new StringBuilder();
		String prefix = Constants.Prefix.Archives;
		builder.append("기록보관소 버전 : " + version + "\n")
			.append(prefix + "도움 기록보관소\n")
			.append("    : 기록보관소 기본 기능 도움말\n")
			.append(prefix + "도움 밈\n")
			.append("    : 밈 기능 도움말\n")
			.append(prefix + "도움 음악\n")
			.append("    : 음악 기능 도움말\n")
			.append(prefix + "도움 통계\n")
			.append("    : 통계 기능 도움말\n")
			.append(prefix + "업데이트 내용\n")
			.append("    : 최신 업데이트 내용 출력\n");
			

		return builder.toString();
	}

	public static String getArchiveCommandHelp() {
		StringBuilder builder = new StringBuilder();
		String prefix = Constants.Prefix.Archives;
		builder.append(prefix + "뽑기 a b c ...\n")
	    	.append("    : a b c ... 중 하나 뽑음\n")
			.append(prefix + "뽑기 멤버\n")
			.append("    : 참가형 뽑기. !참가! 를 통해 참가하고 !참가종료! 시 참가자 중 한명 뽑음\n")
			.append("\n")
			.append(prefix + "멤버\n")
			.append("    : 멤버 표시 (어떤 기준인지 잘 모르겠...\n")
			.append(prefix + "검색\n")
			.append("    : 구글 검색\n");
		
		return builder.toString();
	}
	
	public static String getMemeHelp() {
		StringBuilder builder = new StringBuilder();
		String prefix = Constants.Prefix.Archives;
		builder.append(prefix + "밈\n")
			.append("    : 현재 등록된 밈 리스트 출력\n")
			.append(prefix + "밈 삭제 밈이름	\n")
			.append("    : 해당 밈 삭제\n")
			.append("파일 올리며 댓글에 !등록 밈이름\n")
			.append("    : 밈 등록\n")
			.append("파일 올리며 댓글에 !수정 밈이름\n")
			.append("    : 밈 수정\n");
		
		return builder.toString();
	}
	
	public static String getMusicHelp() {
		StringBuilder builder = new StringBuilder();
		String prefix = Constants.Prefix.LAVA_PLAYER;
		builder.append(".\n")
			.append(prefix + "p 링크\n")
			.append("    : 음악 추가(유튜브)\n")
			.append("파일 올리며 댓글에 !@p m\n")
			.append("    : 음악 추가(업로드 파일)\n")
			.append(prefix + "s\n")
			.append("    : 음악 스킵\n")
			.append(prefix + "list\n")
			.append("    : 음악 재생목록 리스트\n")
			.append(prefix + "now 링크\n")
			.append("    : 음악 바로 재생\n")
			.append(prefix + "pause\n")
			.append("    : 음악 일시정지\n")
			.append(prefix + "resume\n")
			.append("    : 음악 재생\n")
			.append(prefix + "v 숫자\n")
			.append("    : 음악 볼륨\n");
		
		return builder.toString();
	}
	
	public static String getStatisticHelp() {
		StringBuilder builder = new StringBuilder();
		String prefix = Constants.Prefix.Archives;
		builder.append(prefix + "활동통계 시작\n")
			.append("    : 활동 통계 기록 시작\n")
			.append(prefix + "활동통계 일시중지\n")
			.append("    : 활동 통계 기록 일시중지(DB에 데이터는 남아있으나 현재는 기록 X)\n")
			.append(prefix + "활동통계 재시작\n")
			.append("    : 활동통계 기록 재시작(기록 일시중지를 해제)\n")
			.append(prefix + "활동통계 파기\n")
			.append("    : DB에 존재하는 사용자의 모든 데이터 파기. '활동통계 파기'를 다시 한번 입력하면 완전히 파기 \n")
			.append("\n")
			.append(prefix + "활동통계 '####-##-##' '상태'\n")
			.append("    : 해당 날짜의 해당 활동 시간 출력.\n")
			.append("        ex) !활동통계 2021-01-01 온라인\n")
			.append(prefix + "활동통계 '####-##-##' '####-##-##' '상태'\n")
			.append("    : 해당 기간의 해당 총 활동 시간 출력.\n")
			.append("        ex) !활동통계 2021-01-01 2021-01-31 자리비움\n")
			.append(prefix + "활동통계 전체 '상태'\n")
			.append("        ex) !활동통계 전체 다른용무중\n")
			.append(prefix + "활동통계 전체 '상태' xlsx\n")
			.append("    : 전체 통계 차트 출력 + DB 내용을 엑셀파일로 출력\n")
			.append("        ex) !활동통계 전체 온라인 xlsx\n")
			.append("*해당 '유저 활동 통계 기록' 기능은 유저가 원하는 언제든지 시작, 기록 중지, 데이터 파기가 가능하며 개발자(삼등급목심)에게 데이터 확인을 요청하면 이를 가능한 신속하게 화면 공유 등을 통해 사용자에게 보여줄 의무를 가지고 있음을 알립니다.\n");

		return builder.toString();
	}	
}
