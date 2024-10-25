package Server;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import Client.ClientListener;

public class ServerClientPanel extends JFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel clientPanel;
    private JLabel lbNum; 
    private JLabel lbIP;
    private JLabel lbName;
    private JLabel lbStatus;
    private JButton btnView;
    private JButton btnChat;
    private JButton btnFile;
    private Socket socket = null;
    private Socket socketChat = null;
    private Socket socketRemote = null;
    private DataOutputStream dosChat;
    ServerChatForm serverChatForm;
    ClientListener clientListener;
	
    public ServerClientPanel(int stt) {
        initialize(stt);
    }

    
    private void initialize(int stt) {
        clientPanel = new JPanel();
        clientPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
        clientPanel.setBounds(28, 10, 250, 150);
        clientPanel.setLayout(null);
        clientPanel.setName("Client: " + stt);

        btnView = new JButton("View");
        btnView.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		try {
        			DataOutputStream dosRemote = new DataOutputStream(socketRemote.getOutputStream());
        			//dosRemote.writeUTF("REMOTE_DESKTOP");
            		
            		clientListener.setVisible(true);
				} catch (Exception e2) {
					// TODO: handle exception
				} 
        	}
        });
        btnView.setBounds(10, 119, 85, 21);
        clientPanel.add(btnView);

        btnFile = new JButton("File");
        btnFile.setBounds(149, 119, 85, 21);
        clientPanel.add(btnFile);

        lbNum = new JLabel("Máy số 0");
        lbNum.setBounds(10, 10, 69, 13);
        clientPanel.add(lbNum);

        lbName = new JLabel("Họ tên:");
        lbName.setBounds(10, 56, 224, 13);
        clientPanel.add(lbName);

        lbIP = new JLabel("IP:");
        lbIP.setBounds(10, 33, 224, 13);
        clientPanel.add(lbIP);
        
        btnChat = new JButton("Chat");
        btnChat.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		try {
        			dosChat = new DataOutputStream(socketChat.getOutputStream());
//        			dosChat.writeUTF("CHAT");
        			serverChatForm.setVisible(true);        			
				} catch (Exception e2) {
					// TODO: handle exception
					e2.printStackTrace();
				}
        	}
        });
        btnChat.setBounds(10, 88, 85, 21);
        clientPanel.add(btnChat);
        
        lbStatus = new JLabel("Chưa kết nối");
        lbStatus.setBounds(110, 10, 124, 13);
        clientPanel.add(lbStatus);
        
        btnChat.setVisible(false);
		btnView.setVisible(false);
		btnFile.setVisible(false);
    }

    public JPanel getClientPanel() {
        return this.clientPanel;
    }
    
    public void updateStt(int stt) {
        lbNum.setText("Máy số " + stt);
    }
    
    public void updateIP(String ip) {
        lbIP.setText(ip);
    }
    
    public void updateClientName(String clientName) {
        lbName.setText(clientName);
    }
	
	public void setStatus() {
		lbStatus.setText("Đã kết nối");
		btnChat.setVisible(true);
		btnView.setVisible(true);
		btnFile.setVisible(true);
	}

	public Socket getSocket() {
		return socket;
	}


	public void setSocket(Socket socket) {
		this.socket = socket;
	}


	public Socket getSocketChat() {
		return socketChat;
	}


	public void setSocketChat(Socket socketChat) {
		this.socketChat = socketChat;
		serverChatForm = new ServerChatForm(socketChat, lbNum.getText());
    	new Thread(serverChatForm).start();
	}


	public Socket getSocketRemote() {
		return socketRemote;
	}


	public void setSocketRemote(Socket socketRemote) {
		this.socketRemote = socketRemote;
		clientListener = new ClientListener(socketRemote, lbNum.getText());
		clientListener.startListening();
	}
}