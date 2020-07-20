package elevator;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @title 2019-2020ѧ�� ͬ�ô�ѧ������� ����ϵͳ ���̹�����Ŀ---���ݵ���ϵͳ
 * 
 * @time ��Ŀ����ʱ�䣺2020.4.28 - 2020.5.17
 * 
 * @ID 1850250
 * @author ����� Brian
 * @mail 2440720776@qq.com
 * 
 */

/**
 * ElevatorGUI�࣬ʵ�ֵ��ݵ���ϵͳ���û�֮��Ľ���
 */
@SuppressWarnings("serial")
public class ElevatorGUI extends JFrame {

	private JPanel contentPane;

	/* ϵͳ������ ��װϵͳ�����߼����� */
	SystemController controller;

	/* ϵͳ�ڵĵ����� */
	Elevator[] elevator;

	/* ��¥��¥��1-20 */
	Integer[] listFloor;

	/* ¥��ѡ��� */
	JComboBox<Integer> comboBoxFloor;

	/* ����ѡ��ť */
	JButton upJb, downJb;

	/* ��¥������°�ť״̬ */
	boolean upSelect[], downSelect[];

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ElevatorGUI frame = new ElevatorGUI("Elevator Dispatch");

					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * ����ElevatorGUI���
	 * 
	 * @throws Exception
	 */
	public ElevatorGUI(String title) throws Exception {

		super(title);
		setResizable(false);

		/* ��ʼ��ϵͳ������ */
		controller = new SystemController();
		controller.setSystem(this);

		/* ���ó���Сͼ�� */
		Image image = ImageIO.read(this.getClass().getResource("/img/icon.png"));
		this.setIconImage(image); 

		/* ��ʼ��contentPane */
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1203, 669);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		/* ��ʼ��¥��ѡ���ǩ */
		JLabel label = new JLabel("��ѡ������¥�㣺");
		label.setFont(ID_FONT);
		label.setBounds(400, 58, 200, 60);
		contentPane.add(label);

		/* ��ʼ������¥��ť */
		upJb = new JButton("��");
		upJb.setMargin(new Insets(0, 0, 0, 0));
		upJb.setFont(BIG_BTN_FONT);
		upJb.setLocation(684, 63);
		upJb.setSize(53, 53);
		upJb.setFocusPainted(false);
		upJb.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Integer floor = listFloor[comboBoxFloor.getSelectedIndex()];
				try {
					addRequest(new ElevatorRequest(ElevatorRequest.REQUEST_OUTSIDE, floor, true));
					upSelect[comboBoxFloor.getSelectedIndex()] = true;
					upJb.setEnabled(false);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}

		});
		contentPane.add(upJb);

		downJb = new JButton("��");
		downJb.setMargin(new Insets(0, 0, 0, 0));
		downJb.setFont(BIG_BTN_FONT);
		downJb.setLocation(750, 63);
		downJb.setSize(53, 53);
		downJb.setFocusPainted(false);
		downJb.setEnabled(false);
		downJb.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Integer floor = listFloor[comboBoxFloor.getSelectedIndex()];
				try {
					addRequest(new ElevatorRequest(ElevatorRequest.REQUEST_OUTSIDE, floor, false));
					downSelect[comboBoxFloor.getSelectedIndex()] = true;
					downJb.setEnabled(false);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		contentPane.add(downJb);

		/* ��ѡ���¥��1-20 */
		listFloor = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };

		/* ��ʼ��¥��ѡ��� */
		downSelect = new boolean[21];
		upSelect = new boolean[21];
		comboBoxFloor = new JComboBox<Integer>(listFloor);
		comboBoxFloor.setSelectedIndex(0);
		comboBoxFloor.setFont(new Font(Font.SERIF, Font.BOLD, 18));
		comboBoxFloor.setFocusable(false);
		comboBoxFloor.setBounds(600, 67, 60, 40);
		contentPane.add(comboBoxFloor);
		comboBoxFloor.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				// ֻ����ѡ�е�״̬
				if (comboBoxFloor.getSelectedIndex() == 0 || downSelect[comboBoxFloor.getSelectedIndex()])
					downJb.setEnabled(false);
				else
					downJb.setEnabled(true);
				if (comboBoxFloor.getSelectedIndex() == 19 || upSelect[comboBoxFloor.getSelectedIndex()])
					upJb.setEnabled(false);
				else
					upJb.setEnabled(true);
			}

		});

		/* ��ʼ��ϵͳ�ڵĵ����� */
		elevator = new Elevator[5];
		int x = 10;
		for (int i = 0; i < 5; i++) {
			elevator[i] = new Elevator(i + 1);
			elevator[i].setLocation(x, 149);
			elevator[i].setSize(225, 473);
			elevator[i].setSystem(this);
			contentPane.add(elevator[i]);
			x += 238;
		}
	}

	void addRequest(ElevatorRequest eleReq) throws Exception {
		controller.addRequest(eleReq);
	}

	/**
	 * ��ID��ȡ��Ӧ����
	 * 
	 * @param ID ���ݱ��
	 * @return ID����Ӧ�ĵ���
	 */
	Elevator getFromID(int ID) {
		return elevator[ID - 1];
	}

	/* �������� */
	public static final Font BTN_FONT = new Font("΢���ź�", Font.BOLD, 14);

	public static final Font BIG_BTN_FONT = new Font("΢���ź�", Font.BOLD, 20);

	public static final Font ID_FONT = new Font(Font.SERIF, Font.BOLD, 24);

	public static final Font FLOOR_FONT = new Font("DISPLAY FREE TFB", Font.BOLD, 22);
}

/**
 * ���������࣬��װ�˿�����ľ�����Ϣ �˿Ͱ��°�ť�󣬴���һ��ElevatorRequest����
 */
class ElevatorRequest {

	/* �������ࣨ������/�����⣩ */
	boolean type;

	/* �����ⲿ���� */
	public static final boolean REQUEST_INSIDE = true;

	/* �����ڲ����� */
	public static final boolean REQUEST_OUTSIDE = false;

	/**
	 * �����ڵ����ڵ�����<BR/>
	 * ��ʾִ�д�����ĵ��ݱ��
	 */
	int ID;

	/* Ŀ��¥�� */
	int target;

	/**
	 * �����ڵ����������<BR/>
	 * true�������ϣ�false��������
	 */
	boolean direction;

	/**
	 * �����ڲ�����Ĺ��캯��
	 * 
	 * @param type   ��������
	 * @param ID     ��Ӧ���ݵı��
	 * @param target �������Ŀ��¥��
	 */
	public ElevatorRequest(boolean type, int ID, int target) {
		this.type = type;
		this.ID = ID;
		this.target = target;
	}

	/**
	 * �����ⲿ����Ĺ��캯��
	 * 
	 * @param type      ��������
	 * @param target    �������Ŀ��¥��
	 * @param direction �����˵��ݵķ���
	 */
	public ElevatorRequest(boolean type, int target, boolean direction) {
		this.type = type;
		this.target = target;
		this.direction = direction;
	}

}

/**
 * SystemController����ϵͳ��������
 * <p>
 * ��װϵͳ����ĵ��Ȳ��֣���ElevatorGUIΪһ��һ��ϵ
 */
class SystemController {

	/* ���Ƶ�ϵͳ���� */
	ElevatorGUI system;

	/* ϵͳ����������� */
	CopyOnWriteArrayList<ElevatorRequest> requests;

	public SystemController() {
		requests = new CopyOnWriteArrayList<ElevatorRequest>();
	}

	/**
	 * ����ϵͳ
	 * 
	 * @param sys ���ݵ���ϵͳ
	 */
	void setSystem(ElevatorGUI sys) {
		system = sys;
	}

	/**
	 * ��ӵ�������
	 * 
	 * @param eleReq ����ӵĵ�������
	 * @throws Exception
	 */
	void addRequest(ElevatorRequest eleReq) throws Exception {
		dispatch(eleReq);
		requests.add(eleReq);
	}

	/**
	 * ������ɵ��Ⱥ������SystemController���ܵ��������
	 * 
	 * @param ele   ��ɵ��ȵĵ���
	 * @param floor ���������Ӧ¥��
	 */
	void removeRequest(ElevatorRequest eleReq) {
		if (eleReq.type == ElevatorRequest.REQUEST_OUTSIDE) {
			if (eleReq.direction == true) {
				system.upSelect[eleReq.target - 1] = false;
				if (system.comboBoxFloor.getSelectedIndex() == eleReq.target - 1)
					system.upJb.setEnabled(true);
			} else {
				system.downSelect[eleReq.target - 1] = false;
				if (system.comboBoxFloor.getSelectedIndex() == eleReq.target - 1)
					system.downJb.setEnabled(true);
			}
		}
		requests.remove(eleReq);
	}

	/**
	 * �����㷨�����µĵ���������ȸ�ָ������
	 * 
	 * @param eleReq �µĵ�������
	 * @throws Exception
	 */
	void dispatch(ElevatorRequest eleReq) throws Exception {
		/* �ڲ��������㷨 */
		if (eleReq.type == ElevatorRequest.REQUEST_INSIDE) {
			system.getFromID(eleReq.ID).addTarget(eleReq);
			return;
		}

		Elevator e = system.elevator[1];
		int value = Integer.MAX_VALUE;
		int cur;

		/* �ⲿ�������caclValue()Ȩ������ݵĵ��� */
		for (Elevator e1 : system.elevator) {
			cur = calcValue(e1, eleReq);
			if (cur < value) {
				value = cur;
				e = e1;
			}
		}
		eleReq.ID = e.ID;
		e.addTarget(eleReq);
	}

	/**
	 * Ȩֵ���㺯��������ֵԽС������õ���ִ�д˵�������Խ��
	 * 
	 * @param ele   ����
	 * @param floor ¥��
	 * @return �˵���ִ�д˵��������Ȩֵ
	 */
	private int calcValue(Elevator ele, ElevatorRequest eleReq) {

		/* ����״̬�����޷����� */
		if (ele.getState() == Elevator.STATE_ERROR)
			return Integer.MAX_VALUE;

		/* ��ֹ״̬����ֱ�Ӽ���Ŀ����� */
		if (ele.getState() == Elevator.STATE_STILL)
			return Math.abs(ele.getFloor() - eleReq.target);

		int ret = 0;
		/* ���ݿ���˳·�ӵ�floor��ĳ˿ͣ���������;��ͣ��¥���ʱ�俪�� */
		if (eleReq.target >= ele.getFloor() && ele.getState() == Elevator.STATE_UP
				&& ele.getExpectedState() == Elevator.STATE_STILL && (eleReq.direction || eleReq.target == 20)) {
			for (ElevatorRequest e : ele.getController().target) {
				if (e.target > ele.getFloor() && e.target < eleReq.target)
					ret += 6;
			}
			return eleReq.target - ele.getFloor() + ret;
		}
		if (eleReq.target <= ele.getFloor() && ele.getState() == Elevator.STATE_DOWN
				&& ele.getExpectedState() == Elevator.STATE_STILL && (!eleReq.direction || eleReq.target == 1)) {
			for (ElevatorRequest e : ele.getController().target) {
				if (e.target < ele.getFloor() && e.target > eleReq.target)
					ret += 6;
			}
			return ele.getFloor() - eleReq.target + ret;
		}
		return Integer.MAX_VALUE - 1;
	}
}
