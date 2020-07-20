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
 * Elevator电梯类，封装电梯相关的属性，由与其连接的控制器（ElevatorController类）进行调度
 * <p>
 * 由于图形化显示的需要，Elevator类继承自JPanel类，作为组件添加到JFrame中
 * <p>
 * 可以将Elevator看作一个线程，线程调度的逻辑封装再在controller中
 * 
 * @author Brian
 *
 */
@SuppressWarnings("serial")
public class Elevator extends JPanel {

	/* 电梯所属系统 */
	private ElevatorGUI system;

	/* 电梯控制器 封装控制电梯运行相关的属性及逻辑 */
	private ElevatorController controller;

	/* 电梯编号 */
	Integer ID;

	/* 电梯当前所在楼层 */
	Integer floor;

	/**
	 * 电梯当前状态，共有四种状态，分别为静止，上升，下降，故障
	 */
	private int state = STATE_STILL;

	/**
	 * 电梯当前处于静止状态
	 */
	public static final int STATE_STILL = 0;

	/**
	 * 电梯当前处于上升状态
	 */
	public static final int STATE_UP = 1;

	/**
	 * 电梯当前处于下降状态
	 */
	public static final int STATE_DOWN = 2;

	/**
	 * 电梯当前处于故障状态
	 */
	public static final int STATE_ERROR = 3;

	/**
	 * 电梯到达目标楼层后的期望状态
	 */
	int expectedState = STATE_STILL;

	/**
	 * 内部类FloorButton专门服务于电梯内部楼层按钮
	 * <p>
	 * 继承自JToggleButton，并封装有按钮对应的楼层
	 */
	class FloorButton extends JToggleButton {

		/* 按钮对应的楼层 */
		public int floor;

		/**
		 * @param floor 按钮对应的楼层
		 * @param text  按钮显示的文字（即楼层）
		 */
		public FloorButton(int floor, String text) {
			super(text);

			this.floor = floor;
		}
	}

	/* 电梯内部楼层按钮 */
	FloorButton[] floorJb;

	/* 表示电梯楼层按钮是否被按下，与floorJb一一对应 */
	boolean[] bfloorJb;

	/* 开门关门按钮、报警按钮 */
	private JButton openJb, closeJb, alarmJb;

	/* 表示开门关门按钮、报警按钮是否被按下，与openJb,closeJb,alarmJb对应 */
	boolean bopenJb, bcloseJb, balarmJb;

	/* 存放电梯门、表示电梯运行方向的图片 */
	JLabel doorJl, upJl, downJl;

	/* 电梯编号显示 */
	private JLabel IDJl;

	/* 电梯楼层显示 */
	JLabel floorJl;

	/* 电梯信息显示 */
	JLabel infoJl;

	/**
	 * Elevator类构造函数
	 * 
	 * @param ID 电梯编号
	 */
	public Elevator(int ID) throws Exception {

		super(null, false);

		/* 电梯状态初始化设置 */
		this.ID = ID;
		floor = 1;

		/* 设置电梯控制器 */
		controller = new ElevatorController();
		controller.setElevator(this);
		controller.start();
		controller.pauseThread();

		/* 电梯内部楼层按钮设置 */
		floorJb = new FloorButton[21];
		int x = 185, y = 433;
		for (Integer i = 1; i <= 20; i++) {

			/* 设置楼层按钮的位置，上下间隔为10（40-30），左右坐标为145和185 */
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
				 * 监视电梯内按钮状态<BR/>
				 * 按钮按下后，系统应作出下列响应：<BR/>
				 * 1. 对应按钮变为“按下”状态<BR/>
				 * 2. 添加对应的调度请求<BR/>
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

					/* 向电梯系统添加调度请求 */
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

		/* 电梯内开关按钮设置 */
		openJb = new JButton("开");
		openJb.setFocusPainted(false);
		openJb.setMargin(new Insets(0, 0, 0, 0));
		openJb.setBounds(145, 433, 30, 30);
		openJb.setFont(ElevatorGUI.BTN_FONT);
		this.add(openJb);
		openJb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO 自动生成的方法存根
				JButton fb = (JButton) e.getSource();
				Elevator ele = (Elevator) fb.getParent();
				if (ele.getState() == Elevator.STATE_ERROR)
					return;
				controller.count = 6;
			}
		});

		closeJb = new JButton("关");
		closeJb.setFocusPainted(false);
		closeJb.setMargin(new Insets(0, 0, 0, 0));
		closeJb.setBounds(185, 433, 30, 30);
		closeJb.setFont(ElevatorGUI.BTN_FONT);
		this.add(closeJb);
		closeJb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO 自动生成的方法存根
				JButton fb = (JButton) e.getSource();
				Elevator ele = (Elevator) fb.getParent();
				if (ele.getState() == Elevator.STATE_ERROR)
					return;
				controller.count = 0;
			}
		});

		/* 警报按钮设置，按下后电梯变为“故障”状态 */
		alarmJb = new JButton("警报");
		alarmJb.setBackground(Color.yellow);
		alarmJb.setFocusPainted(false);
		alarmJb.setMargin(new Insets(0, 0, 0, 0));
		alarmJb.setFont(ElevatorGUI.BTN_FONT);
		alarmJb.setBounds(92, y, 38, 38);
		this.add(alarmJb);
		alarmJb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO 自动生成的方法存根
				setState(Elevator.STATE_ERROR);
				for (int i= 1;i<=20;i++) {
					floorJb[i].setEnabled(false);
				}
				openJb.setEnabled(false);
				closeJb.setEnabled(false);
			}
		});

		/* 设置电梯门图片 */
		doorJl = new JLabel();
		doorJl.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("img/door_close.png")));
		doorJl.setBounds(10, 256, 120, 207);
		this.add(doorJl);

		/* 设置电梯上下状态显示图片 */
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

		/* 设置电梯编号显示 */
		IDJl = new JLabel();
		IDJl.setLocation(25, 167);
		IDJl.setSize(96, 44);
		IDJl.setHorizontalAlignment(SwingConstants.CENTER);
		IDJl.setFont(ElevatorGUI.ID_FONT);
		IDJl.setText(this.ID + "号电梯");
		this.add(IDJl);

		/* 设置电梯所在楼层显示 */
		floorJl = new JLabel();
		floorJl.setHorizontalAlignment(SwingConstants.CENTER);
		floorJl.setLocation(45, 221);
		floorJl.setSize(50, 30);
		floorJl.setFont(ElevatorGUI.FLOOR_FONT);
		floorJl.setText(floor.toString());
		this.add(floorJl);

		/* 设置电梯信息显示 */
		infoJl = new JLabel();
		infoJl.setHorizontalAlignment(SwingConstants.CENTER);
		infoJl.setLocation(10, 80);
		infoJl.setSize(126, 38);
		infoJl.setText("电梯运行中");
		infoJl.setFont(new Font("微软雅黑", Font.BOLD, 16));
		this.add(infoJl);
	}

	/**
	 * 与电梯所属的电梯系统连接
	 * 
	 * @param eleSys 电梯系统
	 */
	public void setSystem(ElevatorGUI eleSys) {
		system = eleSys;
	}

	public ElevatorGUI getSystem() {
		return system;
	}

	/**
	 * 设置电梯的控制器
	 * 
	 * @param eleCont 电梯控制器
	 */
	public void setController(ElevatorController eleCont) {
		controller = eleCont;
	}

	public ElevatorController getController() {
		return controller;
	}

	/**
	 * 设置电梯状态
	 * <p>
	 * 此方法为包权限，一般只有controller进行调用
	 * 
	 * @param state 电梯状态
	 */
	@SuppressWarnings("deprecation")
	void setState(int state) {
		this.state = state;
		ImageIcon upImg, downImg;

		/* 更改电梯运行方向图标 */
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
			/* 界面显示 */
			infoJl.setText("电梯故障");
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
			 * 电梯变为故障状态后，需要将Outside类型的调度请求重新调度给其他电梯
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
	 * @return 楼层当前状态
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
	 * 添加电梯调度请求，请求会通过方法调用传送给控制器
	 * 
	 * @param eleReq 添加的调度请求
	 */
	public void addTarget(ElevatorRequest eleReq) {
		this.controller.addTarget(eleReq);
	}

	/**
	 * @return 电梯当前所在楼层
	 */
	public int getFloor() {
		return floor;
	}
}

/**
 * ElevatorController电梯控制器类，封装对电梯的调度逻辑
 * <p>
 * 继承自Thread类，为电梯增添了线程属性，可以对电梯进行线程的调度，包括暂停、运行等
 */
class ElevatorController extends Thread {

	/* 控制器所控制的电梯 */
	private Elevator elevator;

	/* 电梯的目标楼层 */
	CopyOnWriteArrayList<ElevatorRequest> target;

	/* 电梯当前运行方向的最远目标楼层 */
	int curTarget = 1;

	/* 电梯关门倒计时 */
	int count = 0;

	/**
	 * @param ele 设置此控制器连接的电梯
	 */
	void setElevator(Elevator ele) {
		elevator = ele;
	}

	/**
	 * @param eleReq 添加调度请求
	 */
	void addTarget(ElevatorRequest eleReq) {
		/* 当调度请求队列为空时，新加入的请求即为目标楼层 */

		target.add(eleReq);

		/*
		 * 优先执行电梯当前运行方向上的调度请求
		 */
		if (eleReq.target >= curTarget && elevator.getState() == Elevator.STATE_UP
				&& elevator.getExpectedState() == Elevator.STATE_STILL) {
			curTarget = eleReq.target;
			/* 设置expectedState */
			if (eleReq.type == ElevatorRequest.REQUEST_OUTSIDE)
				elevator.setExpectedState(eleReq.direction == true ? Elevator.STATE_UP : Elevator.STATE_DOWN);
			else
				elevator.setExpectedState(Elevator.STATE_STILL);
		}
		if (eleReq.target <= curTarget && elevator.getState() == Elevator.STATE_DOWN
				&& elevator.getExpectedState() == Elevator.STATE_STILL) {
			curTarget = eleReq.target;
			/* 设置expectedState */
			if (eleReq.type == ElevatorRequest.REQUEST_OUTSIDE)
				elevator.setExpectedState(eleReq.direction == true ? Elevator.STATE_UP : Elevator.STATE_DOWN);
			else
				elevator.setExpectedState(Elevator.STATE_STILL);
		}
		resumeThread();
	}

	/**
	 * 电梯到达某一目标楼层后，即完成一个调度请求，需将此调度请求删除
	 * 
	 * @param eleReq 完成的调度请求
	 */
	void removeTarget(ElevatorRequest eleReq) {
		target.remove(eleReq);
		elevator.bfloorJb[eleReq.target] = false;
		elevator.floorJb[eleReq.target].setSelected(false);
		elevator.floorJb[eleReq.target].setEnabled(true);

		/* 更新系统控制器中的调度请求队列 */
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
	 * 电梯上升一层
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
	 * 电梯下降一层
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
	 * 开门
	 * 
	 * @param floor 当前楼层
	 * @throws InterruptedException
	 */
	private void openDoor(int floor) throws InterruptedException {

		elevator.infoJl.setText("电梯到达" + floor + "层");
		sleep(500);

		elevator.infoJl.setText("开门中...");
		sleep(1000);
	}

	/**
	 * 关门
	 * 
	 * @param floor 当前楼层
	 * @throws InterruptedException
	 */
	private void closeDoor(int floor) throws InterruptedException {

		/* 设置关门时间 */
		count = 5;

		while (count > 0) {
			elevator.infoJl.setText("电梯" + count + "s后关门");
			sleep(1000);
			count--;
		}

		elevator.infoJl.setText("电梯运行中");
	}

	/*****************************/
	/********** 线程部分 **********/
	/*****************************/

	/**
	 * 开始线程
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

	/* 利用两个变量实现线程的暂停及恢复，防止电梯线程忙等待 */
	private final Object lock = new Object();

	private boolean pause = false;

	/**
	 * 暂停线程
	 */
	public void pauseThread() {
		pause = true;
	}

	/**
	 * 恢复线程运行
	 */
	public void resumeThread() {
		pause = false;
		synchronized (lock) {
			lock.notify();
		}
	}

	/**
	 * 线程暂停时执行
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
	 * 运行线程
	 */
	@Override
	public void run() {
		super.run();
		while (true) {
			/* 防止暂停时忙等待 */
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
					/* 检测电梯是否突然故障 */
					if (elevator.getState() == Elevator.STATE_ERROR)
						break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			/* 无调度请求时，暂停进程 */
			if (target.isEmpty()) {
				pauseThread();
			}
		}
	}
}
