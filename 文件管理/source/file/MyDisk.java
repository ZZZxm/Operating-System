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
 * ��������࣬���ڴ��п���һ���ռ���Ϊ�ļ��洢����ģ������ϵ��ļ�����
 * 
 * @author brian
 *
 */
public class MyDisk {

	/** ���̿��ӻ� */
	MainFrame frame;

	/** ���̿�Ĵ�С������Ϊ8���ֽ� */
	int blockSize = 8;

	/** ���̴�С���������е���������Ŀ */
	public int size;

	/** ���̿��п����Ŀ */
	public int free;

	/** λʾͼ����Ӧ���̿ռ��ʹ����� */
	public boolean[] bitMap;

	/** �������ݣ���һ��String������ģ����̵Ŀ� */
	public String[] memory;

	/** �ļ�Ŀ¼ */
	public MyCatelog catelog;

	/** ���ݲ�������Ҫ�Ĵ��̴�С�����ڴ��п�����Ӧ�ռ� 
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

	/** �����ļ���Ϣ��ʼ�� 
	 * @throws IOException 
	 * @throws ParseException */
	public void initial() throws IOException, ParseException {
		/* ��ȡλʾͼ */
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
		
		/* ��ȡ�ڴ���Ϣ */
		BufferedReader memorybr = new BufferedReader(new FileReader("info/memoryInfo.txt"));
		line = null;
		i = 0;
		while((line = memorybr.readLine()) != null) {
			memory[i] = line;
			i++;
		}
		memorybr.close();
		
		/* ��ȡĿ¼��Ϣ */
		catelog.readFromFile();
		refreshCatelog();
	}
	
	
	/**
	 * �����ļ����ļ���
	 * 
	 * @param name �´������ļ���
	 * @param type TXT��ʾ�ı��ļ���FOLDER��ʾ�ļ���
	 */
	public boolean createFile(String name, int type) {
		/* �����Ƿ��п��пռ� */
		if (free == 0) {
			return false;
		}

		/* ������������ļ� */
		String temp = name;
		int count = 1;
		while (!isLegal(temp, type)) {
			temp = name;
			temp += ("(" + count + ")");
			count++;
		}
		name = temp;
		
		/* ��Ŀ¼�������Ӧ�Ϊ�ļ�����һ���� */
		if (type == FCB.TXT)
			catelog.add(new FCB(name, type, this, findFreeBlock(), catelog.cur.fcb));
		else
			catelog.add(new FCB(name, type, this, -1, catelog.cur.fcb));
		setFreeBlockNum();
		return true;
	}

	/** ��鵱ǰĿ¼�Ƿ�����ͬ���ֵ��ļ����ļ��� */
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
	
	/** �����ļ����ļ��е����� */
	public boolean setFileName(FCB file, String name) {
		if(!isLegal(name, file.type)) {
			return false;
		}
		file.name = name;
		refreshCatelog();
		return true;
	}

	/** �����ļ� */
	public boolean updateFile(FCB file, String content) {
		char[] ch = content.toCharArray();
		int block = blockNum(ch.length);

		/* ���ռ��Ƿ��㹻 */
		if (block > free) {
			System.out.println("���̿ռ䲻��");
			return false;
		}

		/* �ͷŴ��̿� */
		char[] index = memory[file.start].toCharArray();
		for (int i = 0; i < file.curLength; i++) {
			releaseBlock((int) index[i]);
		}

		/* ��¼��ǰ���ȡ������ */
		int i = 0;
		file.curLength = 0;
		index = new char[blockSize];
		char[] temp = new char[blockSize];

		while (block > 0) {
			int j = findFreeBlock();

			/* �������� */
			index[file.curLength++] = (char) j;
			/* ������д����� */
			Arrays.fill(temp, ' ');
			System.arraycopy(ch, i, temp, 0, ch.length - i >= blockSize ? blockSize : ch.length - i);
			memory[j] = String.valueOf(temp);
			i += blockSize;
			block--;
		}
		memory[file.start] = String.valueOf(index);

		/* �����ļ��޸�ʱ�� */
		file.dateUpdated = new Date(System.currentTimeMillis());

		setFreeBlockNum();
		return true;
	}

	/** �����ļ�������̿��� */
	private int blockNum(int byteNum) {
		return byteNum / blockSize + (byteNum % blockSize == 0 ? 0 : 1);
	}

	/** ˳����ҵ�һ�����п�ĵ�ַ */
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

	/** �ͷ�һ�����̿� */
	private void releaseBlock(int index) {
		bitMap[index] = false;
		free++;
	}

	/** ɾ���ļ� */
	public void deleteFile(FCB file) {
		/* ��Ŀ¼��ɾ����ӦĿ¼�� */
		catelog.delete(file);
		setFreeBlockNum();
	}

	/** �ͷ��ļ��ռ� */
	public void release(FCB file) {
		char[] index = memory[file.start].toCharArray();
		for (int i = 0; i < file.curLength; i++) {
			releaseBlock(index[i]);
		}
		releaseBlock(file.start);
	}

	/** ����ҳ������ʾ��Ŀ¼ */
	public void refreshCatelog() {
		SimpleDateFormat myFmt = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		
		/* ����Ŀ¼��ʾ */
		frame.fileTableModel.setRowCount(0);
		Node p = catelog.cur.leftChild;
		while (p != null) {
			Object[] temp = { p.fcb.name, myFmt.format(p.fcb.dateUpdated), p.fcb.type == FCB.TXT ? "�ı��ļ�(.TXT)" : "�ļ���",
					p.fcb.type == FCB.TXT ? p.fcb.curLength * 8 + "B" : "" };
			frame.fileTableModel.addRow(temp);
			p = p.nextSibling;
		}

		/* ����·����ʾ */
		frame.setPath(catelog.getCurrentPath());
		setFreeBlockNum();
	}
	
	/** ��ʽ�� */
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

	/** ���µ�ǰ���пռ� */
	public void setFreeBlockNum() {
		frame.freeJl.setText("��ǰʣ��ռ䣺" + free * 8 + " B");
	}

	/** ���ÿ��ӻ����� */
	public void setFrame(MainFrame frame) {
		this.frame = frame;
	}
}
