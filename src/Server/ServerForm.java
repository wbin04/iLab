package Server;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPanel;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
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

public class ServerForm extends JFrame{

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
    private List<Socket> listSocket = new ArrayList<>();
    private List<Socket> listSocketChat = new ArrayList<>();
    private List<Socket> listSocketRemote = new ArrayList<>();
    private ServerSocket serverSocket;
    private ServerSocket serverSocketChat;
    private ServerSocket serverSocketRemote;
    public ServerForm() {
    	initialize();
    	try {
			ServerSocket serverSocket = new ServerSocket(2222);
			ServerSocket serverSocketChat = new ServerSocket(5000);
			ServerSocket serverSocketRemote = new ServerSocket(6000);

			System.out.println("Server is waiting for connection");
			while(true) {
				Socket soc = serverSocket.accept();
				Socket socChat = serverSocketChat.accept();
				Socket socRemote = serverSocketRemote.accept();
				listSocket.add(soc);
				listSocketChat.add(socChat);
				listSocketRemote.add(socRemote);
				System.out.println("Client mới vừa kết nối vào server");
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
        setLayout(null);

        JButton btnOpen = new JButton("Open Server");
        btnOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openClientInServerForm();
                lbEn.setText("" + 10);
            }
        });	
        btnOpen.setBounds(91, 137, 111, 21);
        add(btnOpen);

        JButton btnClose = new JButton("Close Server");
        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
            }
        });
        btnClose.setBounds(386, 137, 111, 21);
        add(btnClose);

        JLabel lb1 = new JLabel("Nhap so may:");
        lb1.setBounds(46, 96, 91, 13);
        getContentPane().add(lb1);

        tfNum = new JTextField();
        tfNum.setText("10");
        tfNum.setBounds(151, 93, 96, 19);
        add(tfNum);
        tfNum.setColumns(10);

        serverPanel = new JPanel();
        serverPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
        serverPanel.setLayout(null);

        scrollPane = new JScrollPane(serverPanel);
        scrollPane.setBounds(46, 185, 817, 287);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane);

        JLabel lb1_1 = new JLabel("Da dung:");
        lb1_1.setBounds(338, 53, 91, 13);
        add(lb1_1);

        lbEn = new JLabel();
        lbEn.setText("lbEn");
        lbEn.setBounds(443, 50, 96, 19);
        add(lbEn);

        lblConTrong = new JLabel("Con trong:");
        lblConTrong.setBounds(338, 98, 91, 13);
        add(lblConTrong);

        lbDis = new JLabel();
        lbDis.setText("lbDis");
        lbDis.setBounds(443, 95, 96, 19);
        add(lbDis);
        
        JLabel lblNhapPort = new JLabel("Nhap Port:");
        lblNhapPort.setBounds(46, 51, 91, 13);
        add(lblNhapPort);
        
        tfPort = new JTextField();
        tfPort.setText("2222");
        tfPort.setColumns(10);
        tfPort.setBounds(147, 49, 96, 19);
        add(tfPort);
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
			clientPanel.setSocket(soc);
			clientPanel.setSocketChat(socketChat);
			clientPanel.setSocketRemote(socketRemote);
			System.out.println(""+soc.getInetAddress().getLocalHost());
			clientPanel.updateIP("IP: "+soc.getInetAddress().getLocalHost().getHostAddress());
			clientPanel.updateClientName("Họ tên: " + name);
			clientPanel.setStatus();
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
}  