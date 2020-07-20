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
 * �ļ�Ŀ¼�࣬����ļ�ϵͳ��Ŀ¼��Ϣ
 * 
 * @author brian
 * 
 */
public class MyCatelog {

	/** Ŀ¼���ڵ� */
	Node root;

	/** ��ǰĿ¼ */
	Node cur;

	/** ���� */
	MyDisk disk;

	public MyCatelog() {
		root = new Node(new FCB(), null);
		root.fcb.setPath("DATA(D:)");
		cur = root;
	}

	/** ��ӽڵ� */
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

	/** ɾ���ڵ� */
	public void delete(FCB file) {
		Node p1 = cur.leftChild;
		Node p2 = p1.nextSibling;

		/* �޸�ǰ������ָ�� */
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

	/** �ݹ�ɾ����Ŀ¼ */
	private void erase(Node p, int depth) {

		if (p.leftChild != null) {
			/* ��ǰ������ļ��� */
			erase(p.leftChild, depth + 1);
			if (p.nextSibling != null && depth > 0)
				erase(p.nextSibling, depth + 1);
		} else {
			if (p.nextSibling != null && depth > 0)
				erase(p.nextSibling, depth + 1);
		}

		/* �ͷſռ� */
		if (p.fcb.type == FCB.TXT) {
			disk.release(p.fcb);
		}
		p = null;
	}

	private void erase(Node p) {
		erase(p, 0);
	}

	/** ������Ů�ڵ�ı�����õ�ǰ�ڵ� */
	public void setCurrentCatelog(int index) {
		Node p = cur.leftChild;
		while (index > 0) {
			index--;
			p = p.nextSibling;
		}
		cur = p;
	}

	/** �ڵ�ǰĿ¼�²����ļ� */
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

	/** �����ϲ�Ŀ¼ */
	public boolean returnToUpper() {
		if (cur.parent == null) {
			System.out.println("�������ϲ�Ŀ¼");
			return false;
		} else {
			cur = cur.parent;
			return true;
		}
	}

	/** ��ȡ��ǰĿ¼·�� */
	public String getCurrentPath() {
		return cur.fcb.path;
	}

	/** ���Ŀ¼ */
	public void clear() {
		root = new Node(new FCB(), null);
		root.fcb.setPath("DATA(D:)");
		cur = root;
	}

	/** ��Ŀ¼д���ļ� */
	public boolean writeToFile() throws IOException {
		OutputStream os = new FileOutputStream("info/catelogInfo.txt");
		PrintWriter pw = new PrintWriter(os);

		write(pw, root);
		pw.close();
		os.close();
		return true;
	}

	/** �ݹ�д�� */
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
	 * ���ļ���ȡĿ¼
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
	
	/* �ݹ��ȡ */
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

/** ����Ŀ¼�ڵ� */
class Node {

	/** �ļ���Ϣ */
	FCB fcb = null;

	/** ����Ů���ֵܱ�ʾ�� */
	Node leftChild = null;
	Node nextSibling = null;

	/** ���ڵ� */
	Node parent;

	Node(FCB fcb) {
		this(fcb, null);
	}

	Node(FCB fcb, Node parent) {
		this.fcb = fcb;
		this.parent = parent;
	}
}
