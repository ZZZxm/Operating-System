package file;

import java.util.Date;

/**
 * 文件控制块，用于管理文件
 * 
 * @author brian
 *
 */
public class FCB {

	/** 文件名 */
	public String name;

	/** 文件类型 */
	int type;

	/** 两种文件类型：文本文件、文件夹 */
	public static final int TXT = 1;

	public static final int FOLDER = 2;

	/** 文件索引表地址 */
	int start;

	/** 文件当前长度 */
	int curLength = 0;

	/** 文件最大允许长度 */
	int maxLength;

	/** 文件创建时间 */
	Date dateCreated;

	/** 文件修改时间 */
	Date dateUpdated;

	/** 文件路径 */
	String path;

	/** 父文件 */
	FCB parent;

	/** 文件读写权限 */
	public boolean protection = false;

	/** 文件所在磁盘 */
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
	
	/** 文件恢复使用 */
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
	
	
	/** 获取文件文本内容 */
	public String getContent() {
		String content = new String();
		char[] ch = disk.memory[start].toCharArray();

		for (int i = 0; i < curLength; i++) {
			String cur = disk.memory[(int) ch[i]];
			content += cur;
		}

		/* 取掉字符串尾部的空格 */
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

	/** 设置文件路径 */
	void setPath(String path) {
		this.path = path;
	}
}
