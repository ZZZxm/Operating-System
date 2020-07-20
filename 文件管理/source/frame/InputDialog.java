package frame;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import file.FCB;

import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class InputDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();

	private InputDialog self;

	/** ������ */
	MainFrame parent;

	/** ��Ӧ�����ı���Ϣ���ļ� */
	FCB file;

	/**
	 * Create the dialog.
	 */
	public InputDialog(FCB file, MainFrame parent) {

		setTitle(file.name);
		this.parent = parent;
		self = this;
		setResizable(false);
		setBounds(600, 300, 451, 321);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		JTextArea textArea = new JTextArea();
		textArea.setBounds(0, 0, 449, 225);
		textArea.setFont(new Font("΢���ź�",Font.PLAIN,24));
		/* ���ļ���������д���ı������� */
		textArea.setText(file.getContent());
		contentPanel.add(textArea);

		JButton saveJb = new JButton("����");
		saveJb.setBounds(159, 239, 108, 23);
		saveJb.setFocusable(false);
		saveJb.setFont(new Font("΢���ź�", Font.BOLD, 16));
		saveJb.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				parent.disk.updateFile(file, textArea.getText());
				parent.disk.refreshCatelog();
				self.setVisible(false);
			}
		});
		contentPanel.add(saveJb);

		setVisible(true);
	}
}
