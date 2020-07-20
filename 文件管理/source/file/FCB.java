package file;

import java.util.Date;

/**
 * �ļ����ƿ飬���ڹ����ļ�
 * 
 * @author brian
 *
 */
public class FCB {

	/** �ļ��� */
	public String name;

	/** �ļ����� */
	int type;

	/** �����ļ����ͣ��ı��ļ����ļ��� */
	public static final int TXT = 1;

	public static final int FOLDER = 2;

	/** �ļ��������ַ */
	int start;

	/** �ļ���ǰ���� */
	int curLength = 0;

	/** �ļ���������� */
	int maxLength;

	/** �ļ�����ʱ�� */
	Date dateCreated;

	/** �ļ��޸�ʱ�� */
	Date dateUpdated;

	/** �ļ�·�� */
	String path;

	/** ���ļ� */
	FCB parent;

	/** �ļ���дȨ�� */
	public boolean protection = false;

	/** �ļ����ڴ��� */
	MyDisk disk;

	FCB() {

	}

	FCB(String name, int type, MyDisk disk, int start, FCB parent) {
		this.name = name;
		this.type = type;
		this.start = start;
		this.disk = disk;
		this.parent = parent;
		path = parent.path + " > " + name;

		dateCreated = new Date(System.currentTimeMillis());
		dateUpdated = dateCreated;
	}
	
	/** �ļ��ָ�ʹ�� */
	FCB(String name, int type, MyDisk disk, int start, FCB parent, Date created, Date updated, int len){
		this.name = name;
		this.type = type;
		this.start = start;
		this.disk = disk;
		this.parent = parent;
		dateCreated = created;
		dateUpdated = updated;
		curLength = len;
		path = parent.path + " > " + name;
	}
	
	
	/** ��ȡ�ļ��ı����� */
	public String getContent() {
		String content = new String();
		char[] ch = disk.memory[start].toCharArray();

		for (int i = 0; i < curLength; i++) {
			String cur = disk.memory[(int) ch[i]];
			content += cur;
		}

		/* ȡ���ַ���β���Ŀո� */
		int i = content.length() - 1;
		if (i < 0) {
			return content;
		}
		
		while (content.charAt(i) == ' ') {
			i--;
		}
		content = content.substring(0, i + 1);
		return content;
	}

	/** �����ļ�·�� */
	void setPath(String path) {
		this.path = path;
	}
}
