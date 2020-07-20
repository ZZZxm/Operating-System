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
 * @title 2019-2020学年 同济大学软件工程 操作系统 进程管理项目---电梯调度系统
 * 
 * @time 项目开发时间：2020.4.28 - 2020.5.17
 * 
 * @ID 1850250
 * @author 赵浠明 Brian
 * @mail 2440720776@qq.com
 * 
 */

/**
 * ElevatorGUI类，实现电梯调度系统与用户之间的交互
 */
@SuppressWarnings("serial")
public class ElevatorGUI extends JFrame {

	private JPanel contentPane;

	/* 系统控制器 封装系统调度逻辑部分 */
	SystemController controller;

	/* 系统内的电梯组 */
	Elevator[] elevator;

	/* 大楼的楼层1-20 */
	Integer[] listFloor;

	/* 楼层选项框 */
	JComboBox<Integer> comboBoxFloor;

	/* 上下选择按钮 */
	JButton upJb, downJb;

	/* 各楼层的上下按钮状态 */
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
	 * 创建ElevatorGUI框架
	 * 
	 * @throws Exception
	 */
	public ElevatorGUI(String title) throws Exception {

		super(title);
		setResizable(false);

		/* 初始化系统控制器 */
		controller = new SystemController();
		controller.setSystem(this);

		/* 设置程序小图标 */
		Image image = ImageIO.read(this.getClass().getResource("/img/icon.png"));
		this.setIconImage(image); 

		/* 初始化contentPane */
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1203, 669);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		/* 初始化楼层选择标签 */
		JLabel label = new JLabel("请选择所在楼层：");
		label.setFont(ID_FONT);
		label.setBounds(400, 58, 200, 60);
		contentPane.add(label);

		/* 初始化上下楼按钮 */
		upJb = new JButton("上");
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

		downJb = new JButton("下");
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

		/* 可选择的楼层1-20 */
		listFloor = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };

		/* 初始化楼层选项框 */
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
				// 只处理选中的状态
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

		/* 初始化系统内的电梯组 */
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
	 * 从ID获取对应电梯
	 * 
	 * @param ID 电梯编号
	 * @return ID所对应的电梯
	 */
	Elevator getFromID(int ID) {
		return elevator[ID - 1];
	}

	/* 字体设置 */
	public static final Font BTN_FONT = new Font("微软雅黑", Font.BOLD, 14);

	public static final Font BIG_BTN_FONT = new Font("微软雅黑", Font.BOLD, 20);

	public static final Font ID_FONT = new Font(Font.SERIF, Font.BOLD, 24);

	public static final Font FLOOR_FONT = new Font("DISPLAY FREE TFB", Font.BOLD, 22);
}

/**
 * 电梯请求类，封装乘客请求的具体信息 乘客按下按钮后，创建一个ElevatorRequest对象
 */
class ElevatorRequest {

	/* 请求种类（电梯内/电梯外） */
	boolean type;

	/* 电梯外部请求 */
	public static final boolean REQUEST_INSIDE = true;

	/* 电梯内部请求 */
	public static final boolean REQUEST_OUTSIDE = false;

	/**
	 * 仅用于电梯内的请求<BR/>
	 * 表示执行此请求的电梯编号
	 */
	int ID;

	/* 目标楼层 */
	int target;

	/**
	 * 仅用于电梯外的请求<BR/>
	 * true代表向上，false代表向下
	 */
	boolean direction;

	/**
	 * 电梯内部请求的构造函数
	 * 
	 * @param type   请求种类
	 * @param ID     对应电梯的编号
	 * @param target 此请求的目标楼层
	 */
	public ElevatorRequest(boolean type, int ID, int target) {
		this.type = type;
		this.ID = ID;
		this.target = target;
	}

	/**
	 * 电梯外部请求的构造函数
	 * 
	 * @param type      请求种类
	 * @param target    此请求的目标楼层
	 * @param direction 请求搭乘电梯的方向
	 */
	public ElevatorRequest(boolean type, int target, boolean direction) {
		this.type = type;
		this.target = target;
		this.direction = direction;
	}

}

/**
 * SystemController电梯系统控制器类
 * <p>
 * 封装系统请求的调度部分，与ElevatorGUI为一对一关系
 */
class SystemController {

	/* 控制的系统对象 */
	ElevatorGUI system;

	/* 系统调度请求队列 */
	CopyOnWriteArrayList<ElevatorRequest> requests;

	public SystemController() {
		requests = new CopyOnWriteArrayList<ElevatorRequest>();
	}

	/**
	 * 连接系统
	 * 
	 * @param sys 电梯调度系统
	 */
	void setSystem(ElevatorGUI sys) {
		system = sys;
	}

	/**
	 * 添加调度请求
	 * 
	 * @param eleReq 需添加的调度请求
	 * @throws Exception
	 */
	void addRequest(ElevatorRequest eleReq) throws Exception {
		dispatch(eleReq);
		requests.add(eleReq);
	}

	/**
	 * 电梯完成调度后，需更新SystemController中总的请求队列
	 * 
	 * @param ele   完成调度的电梯
	 * @param floor 调度请求对应楼层
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
	 * 调度算法，将新的调度请求调度给指定电梯
	 * 
	 * @param eleReq 新的调度请求
	 * @throws Exception
	 */
	void dispatch(ElevatorRequest eleReq) throws Exception {
		/* 内部请求不用算法 */
		if (eleReq.type == ElevatorRequest.REQUEST_INSIDE) {
			system.getFromID(eleReq.ID).addTarget(eleReq);
			return;
		}

		Elevator e = system.elevator[1];
		int value = Integer.MAX_VALUE;
		int cur;

		/* 外部请求根据caclValue()权衡各电梯的调度 */
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
	 * 权值计算函数，返回值越小，代表该电梯执行此调度请求越好
	 * 
	 * @param ele   电梯
	 * @param floor 楼层
	 * @return 此电梯执行此调度请求的权值
	 */
	private int calcValue(Elevator ele, ElevatorRequest eleReq) {

		/* 故障状态电梯无法调度 */
		if (ele.getState() == Elevator.STATE_ERROR)
			return Integer.MAX_VALUE;

		/* 静止状态电梯直接计算目标距离 */
		if (ele.getState() == Elevator.STATE_STILL)
			return Math.abs(ele.getFloor() - eleReq.target);

		int ret = 0;
		/* 电梯可以顺路接第floor层的乘客，需额外加上途中停靠楼层的时间开销 */
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
