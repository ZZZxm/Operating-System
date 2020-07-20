package file;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 文件目录类，存放文件系统的目录信息
 * 
 * @author brian
 * 
 */
public class MyCatelog {

	/** 目录根节点 */
	Node root;

	/** 当前目录 */
	Node cur;

	/** 磁盘 */
	MyDisk disk;

	public MyCatelog() {
		root = new Node(new FCB(), null);
		root.fcb.setPath("DATA(D:)");
		cur = root;
	}

	/** 添加节点 */
	public void add(FCB fcb) {

		Node n = cur.leftChild;
		if (n == null) {
			cur.leftChild = new Node(fcb, cur);
		} else {
			while (n.nextSibling != null) {
				n = n.nextSibling;
			}
			n.nextSibling = new Node(fcb, cur);
		}
	}

	/** 删除节点 */
	public void delete(FCB file) {
		Node p1 = cur.leftChild;
		Node p2 = p1.nextSibling;

		/* 修改前驱结点的指针 */
		if (p1.fcb == file) {
			cur.leftChild = p2;
			erase(p1);
		} else {
			while (p2 != null) {
				if (p2.fcb == file) {
					p1.nextSibling = p2.nextSibling;
					erase(p2);
					break;
				}
				p1 = p2;
				p2 = p1.nextSibling;
			}
		}

	}

	/** 递归删除子目录 */
	private void erase(Node p, int depth) {

		if (p.leftChild != null) {
			/* 当前结点是文件夹 */
			erase(p.leftChild, depth + 1);
			if (p.nextSibling != null && depth > 0)
				erase(p.nextSibling, depth + 1);
		} else {
			if (p.nextSibling != null && depth > 0)
				erase(p.nextSibling, depth + 1);
		}

		/* 释放空间 */
		if (p.fcb.type == FCB.TXT) {
			disk.release(p.fcb);
		}
		p = null;
	}

	private void erase(Node p) {
		erase(p, 0);
	}

	/** 根据子女节点的编号设置当前节点 */
	public void setCurrentCatelog(int index) {
		Node p = cur.leftChild;
		while (index > 0) {
			index--;
			p = p.nextSibling;
		}
		cur = p;
	}

	/** 在当前目录下查找文件 */
	public FCB findFile(String name) {
		Node p = cur.leftChild;
		while (p != null) {
			if (name.equals(p.fcb.name)) {
				return p.fcb;
			}
			p = p.nextSibling;
		}
		return null;
	}

	/** 返回上层目录 */
	public boolean returnToUpper() {
		if (cur.parent == null) {
			System.out.println("已是最上层目录");
			return false;
		} else {
			cur = cur.parent;
			return true;
		}
	}

	/** 获取当前目录路径 */
	public String getCurrentPath() {
		return cur.fcb.path;
	}

	/** 清空目录 */
	public void clear() {
		root = new Node(new FCB(), null);
		root.fcb.setPath("DATA(D:)");
		cur = root;
	}

	/** 将目录写入文件 */
	public boolean writeToFile() throws IOException {
		OutputStream os = new FileOutputStream("info/catelogInfo.txt");
		PrintWriter pw = new PrintWriter(os);

		write(pw, root);
		pw.close();
		os.close();
		return true;
	}

	/** 递归写入 */
	private void write(PrintWriter pw, Node p) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		FCB fcb = p.fcb;
		pw.println(fcb.name);
		pw.println(fcb.type);
		pw.println(fcb.start);
		pw.println(fcb.dateCreated == null ? null : dateFormat.format(fcb.dateCreated));
		pw.println(fcb.dateUpdated == null ? null : dateFormat.format(fcb.dateUpdated));
		pw.println(fcb.curLength);
		pw.println(p.leftChild == null ? 0 : 1);
		pw.println(p.nextSibling == null ? 0 : 1);

		if (p.leftChild != null) {
			write(pw, p.leftChild);
		}
		if (p.nextSibling != null) {
			write(pw, p.nextSibling);
		}
	}

	/**
	 * 从文件读取目录
	 * 
	 * @throws IOException
	 * @throws ParseException
	 */
	public void readFromFile() throws IOException, ParseException {
		BufferedReader bitbr = new BufferedReader(new FileReader("info/catelogInfo.txt"));
		
		
		root = read(bitbr, null);
		cur = root;
		bitbr.close();
	}
	
	/* 递归读取 */
	private Node read(BufferedReader br, Node parent) throws IOException, ParseException {
		Node p = null;		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		String name = br.readLine();
		int type = "1".equals(br.readLine()) ? FCB.TXT : FCB.FOLDER;
		int start = Integer.parseInt(br.readLine());
		String temp = br.readLine();
		Date dateCreated = "null".equals(temp) ? null : dateFormat.parse(temp);
		temp = br.readLine();
		Date dateUpdated = "null".equals(temp) ? null : dateFormat.parse(temp);
		int curLen = Integer.parseInt(br.readLine());
		int left = Integer.parseInt(br.readLine()), right = Integer.parseInt(br.readLine());
		
		if (parent == null) {
			p = new Node(new FCB(), null);
			p.fcb.setPath("DATA(D:)");
		} else {
			p = new Node(new FCB(name, type, disk, start, parent.fcb, dateCreated, dateUpdated, curLen), parent);
		}
		if (left == 1) {
			p.leftChild = read(br, p);
		}
		if (right == 1) {
			p.nextSibling = read(br, parent);
		}
		return p;
	}
}

/** 树形目录节点 */
class Node {

	/** 文件信息 */
	FCB fcb = null;

	/** 左子女右兄弟表示法 */
	Node leftChild = null;
	Node nextSibling = null;

	/** 父节点 */
	Node parent;

	Node(FCB fcb) {
		this(fcb, null);
	}

	Node(FCB fcb, Node parent) {
		this.fcb = fcb;
		this.parent = parent;
	}
}
