package Server;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPanel;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.event.ActionEvent;
import javax.swing.border.LineBorder;

import java.awt.Color;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ServerForm2 extends JFrame{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField tfNum;
    private JPanel serverPanel;
    private JScrollPane scrollPane;
    private JLabel lbEn;
    private JLabel lblConTrong;
    private JLabel lbDis;
    private Map<Integer, ServerClientPanel> clientFormsMap;
    private JTextField tfPort;
    private JTextArea textArea;
    private List<Socket> listSocket = new ArrayList<>();
    private List<Socket> listSocketChat = new ArrayList<>();
    private List<Socket> listSocketRemote = new ArrayList<>();
    private ServerSocket serverSocket;
    private ServerSocket serverSocketChat;
    private ServerSocket serverSocketRemote;
    private JTextField textField;
    public ServerForm2() {
    	initialize();
    	try {
			ServerSocket serverSocket = new ServerSocket(2222);
			ServerSocket serverSocketChat = new ServerSocket(5000);
			ServerSocket serverSocketRemote = new ServerSocket(6000);

//			textArea.append("Server đang chờ kết nối...\n");
			while(true) {
				Socket soc = serverSocket.accept();
				Socket socChat = serverSocketChat.accept();
				Socket socRemote = serverSocketRemote.accept();
				listSocket.add(soc);
				listSocketChat.add(socChat);
				listSocketRemote.add(socRemote);
//				System.out.println("Client mới vừa kết nối vào server");
				refreshServerForm(soc, socChat, socRemote);
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Loi initialize ServerForm");
		}   	
    }

    private void initialize() {
        
        setBounds(100, 100, 931, 535);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);

        JButton btnOpen = new JButton("Mở Server");
        btnOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	textArea.setText("");
            	textArea.append("Server đang chờ kết nối...\n");
                openClientInServerForm();
                lbEn.setText("" + 10);
            }
        });
        btnOpen.setBounds(91, 137, 111, 21);
        getContentPane().add(btnOpen);

        JButton btnClose = new JButton("Đóng Server");
        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                closeServer();
            }
        });
        btnClose.setBounds(333, 137, 111, 21);
        getContentPane().add(btnClose);

        JLabel lb1 = new JLabel("Nhập số máy:");
        lb1.setBounds(46, 96, 91, 13);
        getContentPane().add(lb1);

        tfNum = new JTextField();
        tfNum.setText("10");
        tfNum.setBounds(147, 93, 96, 19);
        getContentPane().add(tfNum);
        tfNum.setColumns(10);

        serverPanel = new JPanel();
        serverPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
        serverPanel.setLayout(null);

        scrollPane = new JScrollPane(serverPanel);
        scrollPane.setBounds(46, 185, 817, 287);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        getContentPane().add(scrollPane);

        JLabel lb1_1 = new JLabel("Số máy đã sử dụng:");
        lb1_1.setBounds(285, 53, 111, 13);
        getContentPane().add(lb1_1);

        lbEn = new JLabel();
        lbEn.setText("0");
        lbEn.setBounds(447, 50, 38, 19);
        getContentPane().add(lbEn);

        lblConTrong = new JLabel("Số máy còn trống:");
        lblConTrong.setBounds(285, 98, 111, 13);
        getContentPane().add(lblConTrong);

        lbDis = new JLabel();
        lbDis.setText("0");
        lbDis.setBounds(447, 93, 38, 19);
        getContentPane().add(lbDis);
        
        JLabel lblNhapPort = new JLabel("Nhập cổng:");
        lblNhapPort.setBounds(46, 51, 91, 13);
        getContentPane().add(lblNhapPort);
        
        tfPort = new JTextField();
        tfPort.setText("2222");
        tfPort.setColumns(10);
        tfPort.setBounds(147, 49, 96, 19);
        getContentPane().add(tfPort);
        
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBounds(520, 31, 343, 90);
        getContentPane().add(textArea);
        
        textField = new JTextField();
        textField.setBounds(520, 138, 238, 19);
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });
        getContentPane().add(textField);
        textField.setColumns(10);
        
        JButton btnSend = new JButton("Gửi");
        btnSend.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		sendMessage();
        	}
        });
        btnSend.setBounds(778, 137, 85, 21);
        getContentPane().add(btnSend);
        
        JScrollPane scrollPane_1 = new JScrollPane(textArea);
        scrollPane_1.setBounds(520, 31, 343, 90);
        getContentPane().add(scrollPane_1);
        this.setVisible(true);
    }
    private void refreshServerForm(Socket soc, Socket socketChat, Socket socketRemote) {
    	DataInputStream dis;
		try {
			dis = new DataInputStream(soc.getInputStream());
			String msg = dis.readUTF();
			String[] parts = msg.split(",");
	        int stt = Integer.parseInt(parts[0]);
	        String name = parts[1];
	        
			ServerClientPanel clientPanel = clientFormsMap.get(stt); // tìm ra được clientPanel, cấp cho nó 1 socket, ban đầu khởi tạo bằng NULL
			clientPanel.updateIP("IP: "+soc.getInetAddress().getLocalHost().getHostAddress());
			clientPanel.updateClientName("Họ tên: " + name);
			clientPanel.setStatus();
			clientPanel.setSocket(soc);
			clientPanel.setSocketChat(socketChat);
			clientPanel.setSocketRemote(socketRemote);
			
			lbDis.setText("" + listSocket.size());
			lbEn.setText("" + (10-listSocket.size()));
			textArea.append(name + " ở máy số " + stt + " mới vừa kết nối vào server\n");
			
			serverPanel.revalidate();
			serverPanel.repaint();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }
    private void openClientInServerForm() {

            serverPanel.removeAll();

            clientFormsMap = new HashMap<>();
            
            int x = 10;
            int y = 10;
            int clientPanelWidth = 250;
            int clientPanelHeight = 150;
            int gap = 10;

            for (int i = 1; i <= 10; i++) {
                ServerClientPanel clientForm = new ServerClientPanel(i); 
    			clientForm.updateStt(i);
                JPanel clientPanel = clientForm.getClientPanel();

                clientPanel.setBounds(x, y, clientPanelWidth, clientPanelHeight);

                serverPanel.add(clientPanel);
                
                clientFormsMap.put(i, clientForm);

                x += clientPanelWidth + gap;
                if (x + clientPanelWidth > serverPanel.getWidth()) {
                    x = 10;
                    y += clientPanelHeight + gap;
                }
            }

            int totalHeight = y + clientPanelHeight + gap;
            serverPanel.setPreferredSize(new java.awt.Dimension(scrollPane.getWidth(), totalHeight));

            serverPanel.revalidate();
            serverPanel.repaint();
            scrollPane.revalidate();
    }
    
    private void sendMessage() {
        String msg = textField.getText();
        if (!msg.equals("")) {
            for (Socket socket : listSocketChat) {
                try {
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    dos.writeUTF(msg);
                    dos.flush();

                    textField.setText("");
                    textArea.append("Server: " + msg + "\n");
                } catch (IOException e1) {
                    e1.printStackTrace();
                    System.out.println("Lỗi gửi tin nhắn tổng ServerForm");
                    textArea.append("Lỗi gửi tin nhắn tổng ServerForm\n");
                }
            }
        }
    }
    
    private void closeServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (serverSocketChat != null && !serverSocketChat.isClosed()) {
                serverSocketChat.close();
            }
            if (serverSocketRemote != null && !serverSocketRemote.isClosed()) {
                serverSocketRemote.close();
            }

            for (Socket socket : listSocket) {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            }
            for (Socket socketChat : listSocketChat) {
                if (socketChat != null && !socketChat.isClosed()) {
                	try {
                        DataOutputStream dos = new DataOutputStream(socketChat.getOutputStream());
                        dos.writeUTF("Server đã đóng");
                        dos.flush();

                        socketChat.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        System.out.println("Lỗi đóng ServerForm");
                        textArea.append("Lỗi đóng ServerForm\n");
                    }
                }
            }
            for (Socket socketRemote : listSocketRemote) {
                if (socketRemote != null && !socketRemote.isClosed()) {
                    socketRemote.close();
                }
            }

            textArea.append("Server đã được đóng.\n");
            listSocket.clear();
            listSocketChat.clear();
            listSocketRemote.clear();
            lbEn.setText("0");
            lbDis.setText("0");
            serverPanel.removeAll();
            serverPanel.revalidate();
            serverPanel.repaint();
        } catch (IOException e) {
            e.printStackTrace();
            textArea.append("Lỗi khi đóng server: " + e.getMessage() + "\n");
        }
    }
}  