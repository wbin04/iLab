package Server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ServerChatForm extends JFrame implements Runnable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea chatArea;
    private JTextField chatField;
	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	boolean isChatFormOn = false;
	public ServerChatForm(Socket socketChat, String stt) {
		showChatForm(stt);
		try {
			this.socket = socketChat;
			this.dis = new DataInputStream(socket.getInputStream());
			this.dos = new DataOutputStream(socket.getOutputStream());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}
	
	public void showChatForm(String stt) {
		setTitle("Tin nhắn với: " + stt);
        setBounds(100, 100, 450, 332);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBounds(10, 35, 416, 201);
        add(chatArea);

        chatField = new JTextField();
        chatField.setColumns(10);
        chatField.setBounds(10, 246, 254, 19);
        chatField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });
        add(chatField);

        JButton btnSend = new JButton("Gửi");
        btnSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        btnSend.setBounds(322, 246, 85, 21);
        add(btnSend);
        this.setVisible(false);

    }
	
	private void sendMessage() {
		String message = chatField.getText();
        if(!message.equals("")){
        	try {
            	dos.writeUTF(message); 
                dos.flush();  
                System.out.println("Send successfully");
                chatField.setText(""); 
                chatArea.append("Bạn: " + message + "\n");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
	}
	
	 private void receiveMessage() {
       	 try {
             while (true) {
                String message = dis.readUTF(); 
                chatArea.append(message + "\n"); // ten client + message
             }
            } catch (Exception e) {
                e.printStackTrace();
            }
       }
	 @Override
	public void run() {
		// TODO Auto-generated method stub
		receiveMessage();
	}
}
