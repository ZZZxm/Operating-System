package elevator;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Elevator�����࣬��װ������ص����ԣ����������ӵĿ�������ElevatorController�ࣩ���е���
 * <p>
 * ����ͼ�λ���ʾ����Ҫ��Elevator��̳���JPanel�࣬��Ϊ�����ӵ�JFrame��
 * <p>
 * ���Խ�Elevator����һ���̣߳��̵߳��ȵ��߼���װ����controller��
 * 
 * @author Brian
 *
 */
@SuppressWarnings("serial")
public class Elevator extends JPanel {

	/* ��������ϵͳ */
	private ElevatorGUI system;

	/* ���ݿ����� ��װ���Ƶ���������ص����Լ��߼� */
	private ElevatorController controller;

	/* ���ݱ�� */
	Integer ID;

	/* ���ݵ�ǰ����¥�� */
	Integer floor;

	/**
	 * ���ݵ�ǰ״̬����������״̬���ֱ�Ϊ��ֹ���������½�������
	 */
	private int state = STATE_STILL;

	/**
	 * ���ݵ�ǰ���ھ�ֹ״̬
	 */
	public static final int STATE_STILL = 0;

	/**
	 * ���ݵ�ǰ��������״̬
	 */
	public static final int STATE_UP = 1;

	/**
	 * ���ݵ�ǰ�����½�״̬
	 */
	public static final int STATE_DOWN = 2;

	/**
	 * ���ݵ�ǰ���ڹ���״̬
	 */
	public static final int STATE_ERROR = 3;

	/**
	 * ���ݵ���Ŀ��¥��������״̬
	 */
	int expectedState = STATE_STILL;

	/**
	 * �ڲ���FloorButtonר�ŷ����ڵ����ڲ�¥�㰴ť
	 * <p>
	 * �̳���JToggleButton������װ�а�ť��Ӧ��¥��
	 */
	class FloorButton extends JToggleButton {

		/* ��ť��Ӧ��¥�� */
		public int floor;

		/**
		 * @param floor ��ť��Ӧ��¥��
		 * @param text  ��ť��ʾ�����֣���¥�㣩
		 */
		public FloorButton(int floor, String text) {
			super(text);

			this.floor = floor;
		}
	}

	/* �����ڲ�¥�㰴ť */
	FloorButton[] floorJb;

	/* ��ʾ����¥�㰴ť�Ƿ񱻰��£���floorJbһһ��Ӧ */
	boolean[] bfloorJb;

	/* ���Ź��Ű�ť��������ť */
	private JButton openJb, closeJb, alarmJb;

	/* ��ʾ���Ź��Ű�ť��������ť�Ƿ񱻰��£���openJb,closeJb,alarmJb��Ӧ */
	boolean bopenJb, bcloseJb, balarmJb;

	/* ��ŵ����š���ʾ�������з����ͼƬ */
	JLabel doorJl, upJl, downJl;

	/* ���ݱ����ʾ */
	private JLabel IDJl;

	/* ����¥����ʾ */
	JLabel floorJl;

	/* ������Ϣ��ʾ */
	JLabel infoJl;

	/**
	 * Elevator�๹�캯��
	 * 
	 * @param ID ���ݱ��
	 */
	public Elevator(int ID) throws Exception {

		super(null, false);

		/* ����״̬��ʼ������ */
		this.ID = ID;
		floor = 1;

		/* ���õ��ݿ����� */
		controller = new ElevatorController();
		controller.setElevator(this);
		controller.start();
		controller.pauseThread();

		/* �����ڲ�¥�㰴ť���� */
		floorJb = new FloorButton[21];
		int x = 185, y = 433;
		for (Integer i = 1; i <= 20; i++) {

			/* ����¥�㰴ť��λ�ã����¼��Ϊ10��40-30������������Ϊ145��185 */
			if (i % 2 == 1)
				y -= 40;
			x = (x == 145) ? 185 : 145;

			floorJb[i] = new FloorButton(i, i.toString());
			floorJb[i].setFocusPainted(false);
			floorJb[i].setMargin(new Insets(0, 0, 0, 0));
			floorJb[i].setBounds(x, y, 30, 30);
			floorJb[i].setFont(ElevatorGUI.BTN_FONT);

			floorJb[i].addActionListener(new ActionListener() {

				/**
				 * ���ӵ����ڰ�ť״̬<BR/>
				 * ��ť���º�ϵͳӦ����������Ӧ��<BR/>
				 * 1. ��Ӧ��ť��Ϊ�����¡�״̬<BR/>
				 * 2. ��Ӷ�Ӧ�ĵ�������<BR/>
				 */
				@Override
				public void actionPerformed(ActionEvent e) {

					FloorButton fb = (FloorButton) e.getSource();
					fb.setSelected(true);
					fb.setEnabled(false);
					Elevator ele = (Elevator) fb.getParent();
					if (ele.getState() == Elevator.STATE_ERROR) {
						fb.setSelected(false);
						return;
					}
					ele.bfloorJb[fb.floor] = true;

					/* �����ϵͳ��ӵ������� */
					try {
						ele.system.addRequest(new ElevatorRequest(ElevatorRequest.REQUEST_INSIDE, ele.ID, fb.floor));
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
			this.add(floorJb[i]);
		}
		bfloorJb = new boolean[21];

		/* �����ڿ��ذ�ť���� */
		openJb = new JButton("��");
		openJb.setFocusPainted(false);
		openJb.setMargin(new Insets(0, 0, 0, 0));
		openJb.setBounds(145, 433, 30, 30);
		openJb.setFont(ElevatorGUI.BTN_FONT);
		this.add(openJb);
		openJb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO �Զ����ɵķ������
				JButton fb = (JButton) e.getSource();
				Elevator ele = (Elevator) fb.getParent();
				if (ele.getState() == Elevator.STATE_ERROR)
					return;
				controller.count = 6;
			}
		});

		closeJb = new JButton("��");
		closeJb.setFocusPainted(false);
		closeJb.setMargin(new Insets(0, 0, 0, 0));
		closeJb.setBounds(185, 433, 30, 30);
		closeJb.setFont(ElevatorGUI.BTN_FONT);
		this.add(closeJb);
		closeJb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO �Զ����ɵķ������
				JButton fb = (JButton) e.getSource();
				Elevator ele = (Elevator) fb.getParent();
				if (ele.getState() == Elevator.STATE_ERROR)
					return;
				controller.count = 0;
			}
		});

		/* ������ť���ã����º���ݱ�Ϊ�����ϡ�״̬ */
		alarmJb = new JButton("����");
		alarmJb.setBackground(Color.yellow);
		alarmJb.setFocusPainted(false);
		alarmJb.setMargin(new Insets(0, 0, 0, 0));
		alarmJb.setFont(ElevatorGUI.BTN_FONT);
		alarmJb.setBounds(92, y, 38, 38);
		this.add(alarmJb);
		alarmJb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO �Զ����ɵķ������
				setState(Elevator.STATE_ERROR);
				for (int i= 1;i<=20;i++) {
					floorJb[i].setEnabled(false);
				}
				openJb.setEnabled(false);
				closeJb.setEnabled(false);
			}
		});

		/* ���õ�����ͼƬ */
		doorJl = new JLabel();
		doorJl.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("img/door_close.png")));
		doorJl.setBounds(10, 256, 120, 207);
		this.add(doorJl);

		/* ���õ�������״̬��ʾͼƬ */
		upJl = new JLabel();
		ImageIcon upImg = new ImageIcon(this.getClass().getClassLoader().getResource("img/elevator_not_up.png"));
		upImg.setImage(upImg.getImage().getScaledInstance((int) (608 * 0.05), (int) (508 * 0.05), Image.SCALE_DEFAULT));
		upJl.setIcon(upImg);
		upJl.setBounds(10, 221, (int) (608 * 0.05), (int) (508 * 0.05));
		this.add(upJl);

		downJl = new JLabel();
		ImageIcon downImg = new ImageIcon(this.getClass().getClassLoader().getResource("img/elevator_not_down.png"));
		downImg.setImage(
				downImg.getImage().getScaledInstance((int) (608 * 0.05), (int) (508 * 0.05), Image.SCALE_DEFAULT));
		downJl.setIcon(downImg);
		downJl.setBounds(100, 221, (int) (608 * 0.05), (int) (508 * 0.05));
		this.add(downJl);

		/* ���õ��ݱ����ʾ */
		IDJl = new JLabel();
		IDJl.setLocation(25, 167);
		IDJl.setSize(96, 44);
		IDJl.setHorizontalAlignment(SwingConstants.CENTER);
		IDJl.setFont(ElevatorGUI.ID_FONT);
		IDJl.setText(this.ID + "�ŵ���");
		this.add(IDJl);

		/* ���õ�������¥����ʾ */
		floorJl = new JLabel();
		floorJl.setHorizontalAlignment(SwingConstants.CENTER);
		floorJl.setLocation(45, 221);
		floorJl.setSize(50, 30);
		floorJl.setFont(ElevatorGUI.FLOOR_FONT);
		floorJl.setText(floor.toString());
		this.add(floorJl);

		/* ���õ�����Ϣ��ʾ */
		infoJl = new JLabel();
		infoJl.setHorizontalAlignment(SwingConstants.CENTER);
		infoJl.setLocation(10, 80);
		infoJl.setSize(126, 38);
		infoJl.setText("����������");
		infoJl.setFont(new Font("΢���ź�", Font.BOLD, 16));
		this.add(infoJl);
	}

	/**
	 * ����������ĵ���ϵͳ����
	 * 
	 * @param eleSys ����ϵͳ
	 */
	public void setSystem(ElevatorGUI eleSys) {
		system = eleSys;
	}

	public ElevatorGUI getSystem() {
		return system;
	}

	/**
	 * ���õ��ݵĿ�����
	 * 
	 * @param eleCont ���ݿ�����
	 */
	public void setController(ElevatorController eleCont) {
		controller = eleCont;
	}

	public ElevatorController getController() {
		return controller;
	}

	/**
	 * ���õ���״̬
	 * <p>
	 * �˷���Ϊ��Ȩ�ޣ�һ��ֻ��controller���е���
	 * 
	 * @param state ����״̬
	 */
	@SuppressWarnings("deprecation")
	void setState(int state) {
		this.state = state;
		ImageIcon upImg, downImg;

		/* ���ĵ������з���ͼ�� */
		if (state == Elevator.STATE_UP) {
			upImg = new ImageIcon(this.getClass().getClassLoader().getResource("img/elevator_up.png"));
			upImg.setImage(
					upImg.getImage().getScaledInstance((int) (608 * 0.05), (int) (508 * 0.05), Image.SCALE_DEFAULT));
			upJl.setIcon(upImg);
			downImg = new ImageIcon(this.getClass().getClassLoader().getResource("img/elevator_not_down.png"));
			downImg.setImage(
					downImg.getImage().getScaledInstance((int) (608 * 0.05), (int) (508 * 0.05), Image.SCALE_DEFAULT));
			downJl.setIcon(downImg);
		}

		if (state == Elevator.STATE_DOWN) {
			upImg = new ImageIcon(this.getClass().getClassLoader().getResource("img/elevator_not_up.png"));
			upImg.setImage(
					upImg.getImage().getScaledInstance((int) (608 * 0.05), (int) (508 * 0.05), Image.SCALE_DEFAULT));
			upJl.setIcon(upImg);
			downImg = new ImageIcon(this.getClass().getClassLoader().getResource("img/elevator_down.png"));
			downImg.setImage(
					downImg.getImage().getScaledInstance((int) (608 * 0.05), (int) (508 * 0.05), Image.SCALE_DEFAULT));
			downJl.setIcon(downImg);
		}

		if (state == Elevator.STATE_STILL) {
			upImg = new ImageIcon(this.getClass().getClassLoader().getResource("img/elevator_not_up.png"));
			upImg.setImage(
					upImg.getImage().getScaledInstance((int) (608 * 0.05), (int) (508 * 0.05), Image.SCALE_DEFAULT));
			upJl.setIcon(upImg);
			downImg = new ImageIcon(this.getClass().getClassLoader().getResource("img/elevator_not_down.png"));
			downImg.setImage(
					downImg.getImage().getScaledInstance((int) (608 * 0.05), (int) (508 * 0.05), Image.SCALE_DEFAULT));
			downJl.setIcon(downImg);
		}

		if (state == Elevator.STATE_ERROR) {
			/* ������ʾ */
			infoJl.setText("���ݹ���");
			infoJl.setForeground(Color.RED);
			controller.stop();

			upImg = new ImageIcon(this.getClass().getClassLoader().getResource("img/elevator_up.png"));
			upImg.setImage(
					upImg.getImage().getScaledInstance((int) (608 * 0.05), (int) (508 * 0.05), Image.SCALE_DEFAULT));
			upJl.setIcon(upImg);
			downImg = new ImageIcon(this.getClass().getClassLoader().getResource("img/elevator_down.png"));
			downImg.setImage(
					downImg.getImage().getScaledInstance((int) (608 * 0.05), (int) (508 * 0.05), Image.SCALE_DEFAULT));
			downJl.setIcon(downImg);

			/*
			 * ���ݱ�Ϊ����״̬����Ҫ��Outside���͵ĵ����������µ��ȸ���������
			 */
			for (ElevatorRequest e : controller.target) {
				if (e.type == ElevatorRequest.REQUEST_INSIDE) {
					controller.removeTarget(e);
				} else {
					try {
						system.controller.dispatch(e);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					controller.removeTarget(e);
				}
			}
		}
	}

	/**
	 * @return ¥�㵱ǰ״̬
	 */
	public int getState() {
		return state;
	}

	public void setExpectedState(int state) {
		expectedState = state;
	}

	public int getExpectedState() {
		return expectedState;
	}

	/**
	 * ��ӵ��ݵ������������ͨ���������ô��͸�������
	 * 
	 * @param eleReq ��ӵĵ�������
	 */
	public void addTarget(ElevatorRequest eleReq) {
		this.controller.addTarget(eleReq);
	}

	/**
	 * @return ���ݵ�ǰ����¥��
	 */
	public int getFloor() {
		return floor;
	}
}

/**
 * ElevatorController���ݿ������࣬��װ�Ե��ݵĵ����߼�
 * <p>
 * �̳���Thread�࣬Ϊ�����������߳����ԣ����ԶԵ��ݽ����̵߳ĵ��ȣ�������ͣ�����е�
 */
class ElevatorController extends Thread {

	/* �����������Ƶĵ��� */
	private Elevator elevator;

	/* ���ݵ�Ŀ��¥�� */
	CopyOnWriteArrayList<ElevatorRequest> target;

	/* ���ݵ�ǰ���з������ԶĿ��¥�� */
	int curTarget = 1;

	/* ���ݹ��ŵ���ʱ */
	int count = 0;

	/**
	 * @param ele ���ô˿��������ӵĵ���
	 */
	void setElevator(Elevator ele) {
		elevator = ele;
	}

	/**
	 * @param eleReq ��ӵ�������
	 */
	void addTarget(ElevatorRequest eleReq) {
		/* �������������Ϊ��ʱ���¼��������ΪĿ��¥�� */

		target.add(eleReq);

		/*
		 * ����ִ�е��ݵ�ǰ���з����ϵĵ�������
		 */
		if (eleReq.target >= curTarget && elevator.getState() == Elevator.STATE_UP
				&& elevator.getExpectedState() == Elevator.STATE_STILL) {
			curTarget = eleReq.target;
			/* ����expectedState */
			if (eleReq.type == ElevatorRequest.REQUEST_OUTSIDE)
				elevator.setExpectedState(eleReq.direction == true ? Elevator.STATE_UP : Elevator.STATE_DOWN);
			else
				elevator.setExpectedState(Elevator.STATE_STILL);
		}
		if (eleReq.target <= curTarget && elevator.getState() == Elevator.STATE_DOWN
				&& elevator.getExpectedState() == Elevator.STATE_STILL) {
			curTarget = eleReq.target;
			/* ����expectedState */
			if (eleReq.type == ElevatorRequest.REQUEST_OUTSIDE)
				elevator.setExpectedState(eleReq.direction == true ? Elevator.STATE_UP : Elevator.STATE_DOWN);
			else
				elevator.setExpectedState(Elevator.STATE_STILL);
		}
		resumeThread();
	}

	/**
	 * ���ݵ���ĳһĿ��¥��󣬼����һ�����������轫�˵�������ɾ��
	 * 
	 * @param eleReq ��ɵĵ�������
	 */
	void removeTarget(ElevatorRequest eleReq) {
		target.remove(eleReq);
		elevator.bfloorJb[eleReq.target] = false;
		elevator.floorJb[eleReq.target].setSelected(false);
		elevator.floorJb[eleReq.target].setEnabled(true);

		/* ����ϵͳ�������еĵ���������� */
		elevator.getSystem().controller.removeRequest(eleReq);

		if (elevator.getState() == Elevator.STATE_ERROR)
			return;

		if (curTarget == eleReq.target) {
			elevator.setState(elevator.expectedState);

			elevator.setExpectedState(Elevator.STATE_STILL);
			return;
		}

		if (target.isEmpty()) {
			elevator.setState(Elevator.STATE_STILL);
		}
	}

	/**
	 * ��������һ��
	 * 
	 * @throws InterruptedException
	 */
	void upFloor() throws InterruptedException {
		sleep(1000);
		elevator.floor++;
		elevator.floorJl.setText(elevator.floor + "");
		for (ElevatorRequest e : target) {
			if (e.target == elevator.floor) {
				removeTarget(e);
				openDoor(e.target);
				closeDoor(e.target);
			}
		}
	}

	/**
	 * �����½�һ��
	 * 
	 * @throws InterruptedException
	 */
	void downFloor() throws InterruptedException {
		sleep(1000);
		elevator.floor--;
		elevator.floorJl.setText(elevator.floor + "");

		for (ElevatorRequest e : target) {
			if (e.target == elevator.floor) {
				removeTarget(e);
				openDoor(e.target);
				closeDoor(e.target);
			}
		}
	}

	/**
	 * ����
	 * 
	 * @param floor ��ǰ¥��
	 * @throws InterruptedException
	 */
	private void openDoor(int floor) throws InterruptedException {

		elevator.infoJl.setText("���ݵ���" + floor + "��");
		sleep(500);

		elevator.infoJl.setText("������...");
		sleep(1000);
	}

	/**
	 * ����
	 * 
	 * @param floor ��ǰ¥��
	 * @throws InterruptedException
	 */
	private void closeDoor(int floor) throws InterruptedException {

		/* ���ù���ʱ�� */
		count = 5;

		while (count > 0) {
			elevator.infoJl.setText("����" + count + "s�����");
			sleep(1000);
			count--;
		}

		elevator.infoJl.setText("����������");
	}

	/*****************************/
	/********** �̲߳��� **********/
	/*****************************/

	/**
	 * ��ʼ�߳�
	 */
	@Override
	public synchronized void start() {
		super.start();

		target = new CopyOnWriteArrayList<ElevatorRequest>();

		if (elevator == null) {
			try {
				elevator = new Elevator(0);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/* ������������ʵ���̵߳���ͣ���ָ�����ֹ�����߳�æ�ȴ� */
	private final Object lock = new Object();

	private boolean pause = false;

	/**
	 * ��ͣ�߳�
	 */
	public void pauseThread() {
		pause = true;
	}

	/**
	 * �ָ��߳�����
	 */
	public void resumeThread() {
		pause = false;
		synchronized (lock) {
			lock.notify();
		}
	}

	/**
	 * �߳���ͣʱִ��
	 */
	private void onPause() {
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * �����߳�
	 */
	@Override
	public void run() {
		super.run();
		while (true) {
			/* ��ֹ��ͣʱæ�ȴ� */
			while (pause)
				onPause();

			curTarget = target.get(0).target;
			if (curTarget > elevator.floor)
				elevator.setState(Elevator.STATE_UP);
			if (curTarget < elevator.floor)
				elevator.setState(Elevator.STATE_DOWN);
			if (target.get(0).type == ElevatorRequest.REQUEST_OUTSIDE)
				elevator.setExpectedState(target.get(0).direction == true ? Elevator.STATE_UP : Elevator.STATE_DOWN);
			if (curTarget == elevator.floor) {
				elevator.setState(Elevator.STATE_STILL);
				removeTarget(target.get(0));
			}
			try {
				while (elevator.floor != curTarget) {
					if (curTarget > elevator.floor)
						upFloor();
					else
						downFloor();
					/* �������Ƿ�ͻȻ���� */
					if (elevator.getState() == Elevator.STATE_ERROR)
						break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			/* �޵�������ʱ����ͣ���� */
			if (target.isEmpty()) {
				pauseThread();
			}
		}
	}
}
