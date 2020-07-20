package frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JDialog;

import file.*;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {

	/** 虚拟磁盘 */
	MyDisk disk;

	private JPanel contentPane;

	MainFrame self;

	/** 文件表格 */
	public DefaultTableModel fileTableModel;

	private JTable fileTable;

	/** 路径标签 */
	private JLabel pathJl;
	
	/** 剩余空间标签 */
	public JLabel freeJl;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame("文件管理模拟系统");
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public MainFrame(String title) throws ParseException, IOException {
		super(title);

		disk = new MyDisk(2000);
		disk.setFrame(this);
		self = this;
		setResizable(false);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1033, 724);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		pathJl = new JLabel("路径");
		pathJl.setBounds(21, 104, 800, 34);
		pathJl.setBorder(BorderFactory.createEtchedBorder());
		pathJl.setFont(new Font("微软雅黑", Font.PLAIN, 18));
		pathJl.setText(disk.catelog.getCurrentPath());
		contentPane.add(pathJl);

		/** 设置当前目录显示 */
		Object[] column = { "名称", "修改日期", "文件类型", "大小" };
		fileTableModel = new DefaultTableModel(null, column);
		fileTable = new JTable(fileTableModel) {
			public boolean isCellEditable(int row, int column) {
				return false;// 表格不允许被编辑
			}
		};
		JScrollPane pageJsp = new JScrollPane(fileTable);
		fileTable.setBorder(BorderFactory.createEtchedBorder());
		fileTable.setShowGrid(false);
		fileTable.setRowHeight(40);
		fileTable.getTableHeader().setFont(new Font("微软雅黑", Font.PLAIN, 18));
		fileTable.setFont(new Font("微软雅黑", Font.PLAIN, 18));
		fileTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {

			@Override
			public void setHorizontalAlignment(int alignment) {
				// TODO 自动生成的方法存根
				super.setHorizontalAlignment(JTextField.RIGHT);
			}
		});
		pageJsp.setLocation(21, 161);
		pageJsp.setSize(800, 516);
		contentPane.add(pageJsp);
		fileTable.addMouseListener(new MouseListener() {

			@Override
			public void mousePressed(MouseEvent e) {

			}

			@Override
			public void mouseClicked(MouseEvent e) {
				JTable p = (JTable) e.getSource();
				int index = p.rowAtPoint(e.getPoint());
				FCB file = self.disk.catelog.findFile((String) p.getValueAt(index, 0));
				int type = "文件夹".equals(p.getValueAt(index, 2)) ? FCB.FOLDER : FCB.TXT;

				/* 鼠标左键点击 */
				if (e.getButton() == MouseEvent.BUTTON1) {
					if (type == FCB.FOLDER) {

						/* 点击文件夹，进入下一场目录 */
						self.disk.catelog.setCurrentCatelog(index);
						self.disk.refreshCatelog();
					} else {

						/* 点击文件，进入编辑界面 */
						InputDialog d = new InputDialog(file, self);
						d.setVisible(true);
					}
				}

				/* 鼠标右键点击 */
				if (e.getButton() == MouseEvent.BUTTON3) {

					JPopupMenu m_popupMenu = new JPopupMenu();
					JMenuItem delMenItem = new JMenuItem();
					delMenItem.setText("删除");
					delMenItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							self.disk.deleteFile(file);
							self.disk.refreshCatelog();
						}
					});
					JMenuItem renameMenItem = new JMenuItem();
					renameMenItem.setText("重命名");
					renameMenItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							RenameDialog d = new RenameDialog(self, file);
							d.setVisible(true);
							self.disk.refreshCatelog();
						}
					});
					m_popupMenu.add(delMenItem);
					m_popupMenu.add(renameMenItem);
					m_popupMenu.show(p, e.getX(), e.getY());
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {

			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseExited(MouseEvent e) {

			}
		});

		/** 设置按钮 */
		JButton createJb = new JButton("创建文件");
		createJb.setBounds(856, 180, 137, 34);
		createJb.setFocusPainted(false);
		createJb.setFont(new Font("微软雅黑",Font.BOLD,16));
		createJb.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				CreateDialog d = new CreateDialog(FCB.TXT, self);
				d.setVisible(true);
			}
		});
		contentPane.add(createJb);

		JButton createFolderJb = new JButton("创建文件夹");
		createFolderJb.setBounds(856, 254, 137, 34);
		createFolderJb.setFocusable(false);
		createFolderJb.setFont(new Font("微软雅黑",Font.BOLD,16));
		createFolderJb.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				CreateDialog d = new CreateDialog(FCB.FOLDER, self);
				d.setVisible(true);
			}
		});
		contentPane.add(createFolderJb);

		JButton returnJb = new JButton("返回上层目录");
		returnJb.setBounds(856, 105, 137, 34);
		returnJb.setFocusable(false);
		returnJb.setFont(new Font("微软雅黑",Font.BOLD,16));
		returnJb.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (disk.catelog.returnToUpper())
					disk.refreshCatelog();
			}
		});
		contentPane.add(returnJb);

		JButton clearJb = new JButton("格式化");
		clearJb.setBounds(856, 329, 137, 34);
		clearJb.setFocusable(false);
		clearJb.setBackground(Color.pink);
		clearJb.setFont(new Font("微软雅黑",Font.BOLD,16));
		clearJb.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				disk.clearAll();
			}
		});
		contentPane.add(clearJb);

		freeJl = new JLabel("当前剩余空间：" + disk.free * 8 + " B");
		freeJl.setHorizontalAlignment(SwingConstants.CENTER);
		freeJl.setBounds(831, 494, 178, 44);
		freeJl.setForeground(Color.red);
		freeJl.setFont(new Font("微软雅黑", Font.PLAIN, 16));
		contentPane.add(freeJl);

		JLabel titleJl = new JLabel("文件管理模拟系统");
		titleJl.setHorizontalAlignment(SwingConstants.CENTER);
		titleJl.setFont(new Font("微软雅黑", Font.BOLD, 36));
		titleJl.setBounds(10, 10, 999, 58);
		contentPane.add(titleJl);

		/* 实现文件管理的可持久化 */
		addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO 自动生成的方法存根

			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO 自动生成的方法存根

			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO 自动生成的方法存根

			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO 自动生成的方法存根

			}

			@Override
			public void windowClosing(WindowEvent e) {
				// TODO 自动生成的方法存根
				try {
					/* 保存位示图信息 */
					OutputStream bitos = new FileOutputStream("info/bitmapInfo.txt");
					PrintWriter bitpw = new PrintWriter(bitos);
					for (int i = 0; i < disk.size; i++) {
						bitpw.println(disk.bitMap[i]);
					}
					bitpw.close();
					bitos.close();

					/* 保存内存信息 */
					OutputStream memoryos = new FileOutputStream("info/memoryInfo.txt");
					PrintWriter memorypw = new PrintWriter(memoryos);
					for (int i = 0; i < disk.size; i++) {
						memorypw.println(disk.memory[i]);
					}
					memorypw.close();
					memoryos.close();

					/* 保存目录信息 */
					disk.catelog.writeToFile();

				} catch (IOException e1) {
					// TODO 自动生成的 catch 块
					e1.printStackTrace();
				}

			}
			
			@Override
			public void windowClosed(WindowEvent e) {
				// TODO 自动生成的方法存根

			}

			@Override
			public void windowActivated(WindowEvent e) {
				// TODO 自动生成的方法存根

			}
		});
		
		disk.initial();
	}

	/** 设置窗口显示当前路径 */
	public void setPath(String path) {
		pathJl.setText(path);
	}
}

@SuppressWarnings("serial")
class RenameDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();

	private RenameDialog self;

	MainFrame parent;

	FCB file;

	private JTextField nameText;

	private final JButton enterJb = new JButton("");

	/**
	 * Create the dialog.
	 */
	public RenameDialog(MainFrame parent, FCB file) {

		this.parent = parent;
		this.file = file;
		self = this;

		setTitle("重命名");

		setBounds(600, 300, 502, 237);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		JLabel label1 = new JLabel();
		label1.setHorizontalAlignment(SwingConstants.CENTER);
		label1.setBounds(10, 59, 246, 41);
		label1.setFont(new Font("微软雅黑", Font.BOLD, 22));
		label1.setText("请输入更改后的名称：");
		contentPanel.add(label1);

		nameText = new JTextField();
		nameText.setBounds(263, 59, 215, 41);
		nameText.setFont(new Font("微软雅黑", Font.BOLD, 22));
		contentPanel.add(nameText);
		nameText.setColumns(10);

		enterJb.setBounds(171, 127, 128, 32);
		enterJb.setFont(new Font("微软雅黑", Font.BOLD, 18));
		enterJb.setText("确认");
		enterJb.setFocusable(false);
		enterJb.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				parent.disk.setFileName(file, nameText.getText());
				self.setVisible(false);
			}
		});
		contentPanel.add(enterJb);

	}
}
