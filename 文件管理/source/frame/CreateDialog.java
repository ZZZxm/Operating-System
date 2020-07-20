package frame;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import file.*;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class CreateDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();

	private CreateDialog self;

	MainFrame parent;
	private JTextField nameText;
	private final JButton enterJb = new JButton("");

	/**
	 * Create the dialog.
	 */
	public CreateDialog(int type, MainFrame parent) {

		this.parent = parent;
		self = this;
		setResizable(false);
		setTitle(type == FCB.TXT ? "�������ļ�" : "�������ļ���");

		setBounds(600, 300, 502, 237);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		JLabel label1 = new JLabel();
		label1.setHorizontalAlignment(SwingConstants.CENTER);
		label1.setBounds(10, 59, 246, 41);
		label1.setFont(new Font("΢���ź�", Font.BOLD, 22));
		label1.setText(type == FCB.TXT ? "���������ļ������ƣ�" : "���������ļ��е����ƣ�");
		contentPanel.add(label1);
		
		nameText = new JTextField();
		nameText.setBounds(263, 59, 215, 41);
		nameText.setFont(new Font("΢���ź�", Font.BOLD, 22));
		contentPanel.add(nameText);
		nameText.setColumns(10);
		
		enterJb.setBounds(171, 127, 128, 32);
		enterJb.setFont(new Font("΢���ź�", Font.BOLD, 18));
		enterJb.setText("ȷ��");
		enterJb.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.disk.createFile(nameText.getText(), type);
				parent.disk.refreshCatelog();
				self.setVisible(false);
			}
		});
		contentPanel.add(enterJb);

	}
}
