package main.java.archives;

public class HelpCommand {
	static String version = Archives.ArchivesVersion;
	
	public static String getArchiveHelp() {
		StringBuilder builder = new StringBuilder();
		String prefix = Constants.Prefix.Archives;
		builder.append("��Ϻ����� ���� : " + version + "\n")
			.append(prefix + "���� ��Ϻ�����\n")
			.append("    : ��Ϻ����� �⺻ ��� ����\n")
			.append(prefix + "���� ��\n")
			.append("    : �� ��� ����\n")
			.append(prefix + "���� ����\n")
			.append("    : ���� ��� ����\n")
			.append(prefix + "���� ���\n")
			.append("    : ��� ��� ����\n")
			.append(prefix + "������Ʈ ����\n")
			.append("    : �ֽ� ������Ʈ ���� ���\n");
			

		return builder.toString();
	}

	public static String getArchiveCommandHelp() {
		StringBuilder builder = new StringBuilder();
		String prefix = Constants.Prefix.Archives;
		builder.append(prefix + "�̱� a b c ...\n")
	    	.append("    : a b c ... �� �ϳ� ����\n")
			.append(prefix + "�̱� ���\n")
			.append("    : ������ �̱�. !����! �� ���� �����ϰ� !��������! �� ������ �� �Ѹ� ����\n")
			.append("\n")
			.append(prefix + "���\n")
			.append("    : ��� ǥ�� (� �������� �� �𸣰�...\n")
			.append(prefix + "�˻�\n")
			.append("    : ���� �˻�\n");
		
		return builder.toString();
	}
	
	public static String getMemeHelp() {
		StringBuilder builder = new StringBuilder();
		String prefix = Constants.Prefix.Archives;
		builder.append(prefix + "��\n")
			.append("    : ���� ��ϵ� �� ����Ʈ ���\n")
			.append(prefix + "�� ���� ���̸�	\n")
			.append("    : �ش� �� ����\n")
			.append("���� �ø��� ��ۿ� !��� ���̸�\n")
			.append("    : �� ���\n")
			.append("���� �ø��� ��ۿ� !���� ���̸�\n")
			.append("    : �� ����\n");
		
		return builder.toString();
	}
	
	public static String getMusicHelp() {
		StringBuilder builder = new StringBuilder();
		String prefix = Constants.Prefix.LAVA_PLAYER;
		builder.append(".\n")
			.append(prefix + "p ��ũ\n")
			.append("    : ���� �߰�(��Ʃ��)\n")
			.append("���� �ø��� ��ۿ� !@p m\n")
			.append("    : ���� �߰�(���ε� ����)\n")
			.append(prefix + "s\n")
			.append("    : ���� ��ŵ\n")
			.append(prefix + "list\n")
			.append("    : ���� ������ ����Ʈ\n")
			.append(prefix + "now ��ũ\n")
			.append("    : ���� �ٷ� ���\n")
			.append(prefix + "pause\n")
			.append("    : ���� �Ͻ�����\n")
			.append(prefix + "resume\n")
			.append("    : ���� ���\n")
			.append(prefix + "v ����\n")
			.append("    : ���� ����\n");
		
		return builder.toString();
	}
	
	public static String getStatisticHelp() {
		StringBuilder builder = new StringBuilder();
		String prefix = Constants.Prefix.Archives;
		builder.append(prefix + "Ȱ����� ����\n")
			.append("    : Ȱ�� ��� ��� ����\n")
			.append(prefix + "Ȱ����� �Ͻ�����\n")
			.append("    : Ȱ�� ��� ��� �Ͻ�����(DB�� �����ʹ� ���������� ����� ��� X)\n")
			.append(prefix + "Ȱ����� �����\n")
			.append("    : Ȱ����� ��� �����(��� �Ͻ������� ����)\n")
			.append(prefix + "Ȱ����� �ı�\n")
			.append("    : DB�� �����ϴ� ������� ��� ������ �ı�. 'Ȱ����� �ı�'�� �ٽ� �ѹ� �Է��ϸ� ������ �ı� \n")
			.append("\n")
			.append(prefix + "Ȱ����� '####-##-##' '����'\n")
			.append("    : �ش� ��¥�� �ش� Ȱ�� �ð� ���.\n")
			.append("        ex) !Ȱ����� 2021-01-01 �¶���\n")
			.append(prefix + "Ȱ����� '####-##-##' '####-##-##' '����'\n")
			.append("    : �ش� �Ⱓ�� �ش� �� Ȱ�� �ð� ���.\n")
			.append("        ex) !Ȱ����� 2021-01-01 2021-01-31 �ڸ����\n")
			.append(prefix + "Ȱ����� ��ü '����'\n")
			.append("        ex) !Ȱ����� ��ü �ٸ��빫��\n")
			.append(prefix + "Ȱ����� ��ü '����' xlsx\n")
			.append("    : ��ü ��� ��Ʈ ��� + DB ������ �������Ϸ� ���\n")
			.append("        ex) !Ȱ����� ��ü �¶��� xlsx\n")
			.append("*�ش� '���� Ȱ�� ��� ���' ����� ������ ���ϴ� �������� ����, ��� ����, ������ �ıⰡ �����ϸ� ������(���޸��)���� ������ Ȯ���� ��û�ϸ� �̸� ������ �ż��ϰ� ȭ�� ���� ���� ���� ����ڿ��� ������ �ǹ��� ������ ������ �˸��ϴ�.\n");

		return builder.toString();
	}	
}
