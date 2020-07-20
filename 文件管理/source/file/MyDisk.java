package file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import frame.MainFrame;

/**
 * 虚拟磁盘类，在内存中开辟一个空间作为文件存储器，模拟磁盘上的文件管理
 * 
 * @author brian
 *
 */
public class MyDisk {

	/** 磁盘可视化 */
	MainFrame frame;

	/** 磁盘块的大小，设置为8个字节 */
	int blockSize = 8;

	/** 磁盘大小，即磁盘中的物理块的数目 */
	public int size;

	/** 磁盘空闲块的数目 */
	public int free;

	/** 位示图，反应磁盘空间的使用情况 */
	public boolean[] bitMap;

	/** 磁盘内容，用一个String数组来模拟磁盘的块 */
	public String[] memory;

	/** 文件目录 */
	public MyCatelog catelog;

	/** 根据参数中需要的磁盘大小，在内存中开辟相应空间 
	 * @throws ParseException */
	@SuppressWarnings("unused")
	public MyDisk(int sz) throws ParseException {
		this.size = sz / blockSize;
		this.free = this.size;
		bitMap = new boolean[size];
		for (boolean b : bitMap) {
			b = false;
		}
		memory = new String[size];
		Arrays.fill(memory, "");

		catelog = new MyCatelog();
		catelog.disk = this;
	}

	/** 根据文件信息初始化 
	 * @throws IOException 
	 * @throws ParseException */
	public void initial() throws IOException, ParseException {
		/* 读取位示图 */
		BufferedReader bitbr = new BufferedReader(new FileReader("info/bitmapInfo.txt"));
		String line = null;
		int i = 0;
		while((line = bitbr.readLine()) != null) {
			bitMap[i] = ("true".equals(line));
			if(bitMap[i] == true)
				free--;
			i++;
		}
		bitbr.close();
		
		/* 读取内存信息 */
		BufferedReader memorybr = new BufferedReader(new FileReader("info/memoryInfo.txt"));
		line = null;
		i = 0;
		while((line = memorybr.readLine()) != null) {
			memory[i] = line;
			i++;
		}
		memorybr.close();
		
		/* 读取目录信息 */
		catelog.readFromFile();
		refreshCatelog();
	}
	
	
	/**
	 * 创建文件或文件夹
	 * 
	 * @param name 新创建的文件名
	 * @param type TXT表示文本文件，FOLDER表示文件夹
	 */
	public boolean createFile(String name, int type) {
		/* 磁盘是否有空闲空间 */
		if (free == 0) {
			return false;
		}

		/* 检查有无重名文件 */
		String temp = name;
		int count = 1;
		while (!isLegal(temp, type)) {
			temp = name;
			temp += ("(" + count + ")");
			count++;
		}
		name = temp;
		
		/* 在目录中添加相应项，为文件分配一个块 */
		if (type == FCB.TXT)
			catelog.add(new FCB(name, type, this, findFreeBlock(), catelog.cur.fcb));
		else
			catelog.add(new FCB(name, type, this, -1, catelog.cur.fcb));
		setFreeBlockNum();
		return true;
	}

	/** 检查当前目录是否有相同名字的文件或文件夹 */
	private boolean isLegal(String name, int type) {
		try {
			Node p = catelog.cur.leftChild;
			while (p != null) {
				if (p.fcb.name.equals(name) && p.fcb.type == type) {
					return false;
				}
				p = p.nextSibling;
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	/** 设置文件或文件夹的名称 */
	public boolean setFileName(FCB file, String name) {
		if(!isLegal(name, file.type)) {
			return false;
		}
		file.name = name;
		refreshCatelog();
		return true;
	}

	/** 更新文件 */
	public boolean updateFile(FCB file, String content) {
		char[] ch = content.toCharArray();
		int block = blockNum(ch.length);

		/* 检查空间是否足够 */
		if (block > free) {
			System.out.println("磁盘空间不足");
			return false;
		}

		/* 释放磁盘块 */
		char[] index = memory[file.start].toCharArray();
		for (int i = 0; i < file.curLength; i++) {
			releaseBlock((int) index[i]);
		}

		/* 记录当前块存取的数据 */
		int i = 0;
		file.curLength = 0;
		index = new char[blockSize];
		char[] temp = new char[blockSize];

		while (block > 0) {
			int j = findFreeBlock();

			/* 更新索引 */
			index[file.curLength++] = (char) j;
			/* 将内容写入块内 */
			Arrays.fill(temp, ' ');
			System.arraycopy(ch, i, temp, 0, ch.length - i >= blockSize ? blockSize : ch.length - i);
			memory[j] = String.valueOf(temp);
			i += blockSize;
			block--;
		}
		memory[file.start] = String.valueOf(index);

		/* 更新文件修改时间 */
		file.dateUpdated = new Date(System.currentTimeMillis());

		setFreeBlockNum();
		return true;
	}

	/** 计算文件所需磁盘块数 */
	private int blockNum(int byteNum) {
		return byteNum / blockSize + (byteNum % blockSize == 0 ? 0 : 1);
	}

	/** 顺序查找第一个空闲块的地址 */
	private int findFreeBlock() {
		for (int i = 0; i < size; i++) {
			if (bitMap[i] == false) {
				bitMap[i] = true;
				free--;
				return i;
			}
		}
		return -1;
	}

	/** 释放一个磁盘块 */
	private void releaseBlock(int index) {
		bitMap[index] = false;
		free++;
	}

	/** 删除文件 */
	public void deleteFile(FCB file) {
		/* 在目录中删除对应目录项 */
		catelog.delete(file);
		setFreeBlockNum();
	}

	/** 释放文件空间 */
	public void release(FCB file) {
		char[] index = memory[file.start].toCharArray();
		for (int i = 0; i < file.curLength; i++) {
			releaseBlock(index[i]);
		}
		releaseBlock(file.start);
	}

	/** 更新页面上显示的目录 */
	public void refreshCatelog() {
		SimpleDateFormat myFmt = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		
		/* 更新目录显示 */
		frame.fileTableModel.setRowCount(0);
		Node p = catelog.cur.leftChild;
		while (p != null) {
			Object[] temp = { p.fcb.name, myFmt.format(p.fcb.dateUpdated), p.fcb.type == FCB.TXT ? "文本文件(.TXT)" : "文件夹",
					p.fcb.type == FCB.TXT ? p.fcb.curLength * 8 + "B" : "" };
			frame.fileTableModel.addRow(temp);
			p = p.nextSibling;
		}

		/* 更新路径显示 */
		frame.setPath(catelog.getCurrentPath());
		setFreeBlockNum();
	}
	
	/** 格式化 */
	@SuppressWarnings("unused")
	public void clearAll() {
		free = size;
		bitMap = new boolean[size];
		for (boolean b : bitMap) {
			b = false;
		}
		memory = new String[size];
		Arrays.fill(memory, "");
		
		catelog.clear();
		refreshCatelog();
		setFreeBlockNum();
	}

	/** 更新当前空闲空间 */
	public void setFreeBlockNum() {
		frame.freeJl.setText("当前剩余空间：" + free * 8 + " B");
	}

	/** 设置可视化窗口 */
	public void setFrame(MainFrame frame) {
		this.frame = frame;
	}
}
