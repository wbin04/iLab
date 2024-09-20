package Server;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JTextField;

public class ServerChatForm {

	private JFrame frame;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerChatForm window = new ServerChatForm();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public ServerChatForm() {
		initialize();
	}

	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JTextArea chatArea = new JTextArea();
		chatArea.setBounds(10, 26, 416, 172);
		frame.getContentPane().add(chatArea);
		
		JButton btnSend = new JButton("Send");
		btnSend.setBounds(315, 219, 85, 21);
		frame.getContentPane().add(btnSend);
		
		JTextField textField = new JTextField();
		textField.setBounds(10, 220, 254, 19);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
	}

	public boolean isVisible() {
		return frame.isVisible();
	}

	public void setVisible(boolean visible) {
		frame.setVisible(visible);
	}
}
