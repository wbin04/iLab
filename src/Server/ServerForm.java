package Server;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPanel;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.awt.event.ActionEvent;
import javax.swing.border.LineBorder;

import Client.ClientPanel;
import java.awt.Color;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

public class ServerForm {

    private JFrame frame;
    private JTextField tfNum;
    private JTextField tfIP;
    private JTextField tfSM;
    private JTextField tfDG;
    private JPanel serverPanel;
    private JScrollPane scrollPane;
    private JTextField textField;
    private JLabel lblConTrong;
    private JTextField textField_1;
    private JButton btnChat;
    private ExecutorService clientThreadPool;
    private ServerSocket serverSocket;
    private Map<Integer, ClientPanel> clientFormsMap;
    private Map<Integer, Boolean> isOpen;
    private JTextArea chatArea;
    private JTextField chatField;
    private PrintWriter out;
    private List<MyProcess> clientProcesses = new ArrayList<>();
    private List<PrintWriter> clientWriters = new ArrayList<>();


    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ServerForm window = new ServerForm();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public ServerForm() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 1203, 535);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        JButton btnOpen = new JButton("Open Server");
        btnOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openClientInServerForm();
            }
        });
        btnOpen.setBounds(728, 49, 111, 21);
        frame.getContentPane().add(btnOpen);

        JButton btnClose = new JButton("Close Server");
        btnClose.setBounds(728, 93, 111, 21);
        frame.getContentPane().add(btnClose);

        JLabel lb1 = new JLabel("Nhap so may:");
        lb1.setBounds(342, 52, 91, 13);
        frame.getContentPane().add(lb1);

        JLabel lb2 = new JLabel("Nhap IP:");
        lb2.setBounds(46, 52, 91, 13);
        frame.getContentPane().add(lb2);

        JLabel lb3 = new JLabel("Nhap SM:");
        lb3.setBounds(46, 96, 91, 13);
        frame.getContentPane().add(lb3);

        JLabel lb4 = new JLabel("Nhap DG:");
        lb4.setBounds(46, 141, 91, 13);
        frame.getContentPane().add(lb4);

        tfNum = new JTextField();
        tfNum.setText("10");
        tfNum.setBounds(447, 49, 96, 19);
        frame.getContentPane().add(tfNum);
        tfNum.setColumns(10);

        tfIP = new JTextField();
        tfIP.setText("192.168.1.5");
        tfIP.setColumns(10);
        tfIP.setBounds(147, 50, 96, 19);
        frame.getContentPane().add(tfIP);

        tfSM = new JTextField();
        tfSM.setColumns(10);
        tfSM.setBounds(147, 93, 96, 19);
        frame.getContentPane().add(tfSM);

        tfDG = new JTextField();
        tfDG.setColumns(10);
        tfDG.setBounds(147, 138, 96, 19);
        frame.getContentPane().add(tfDG);

        serverPanel = new JPanel();
        serverPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
        serverPanel.setLayout(null);

        scrollPane = new JScrollPane(serverPanel);
        scrollPane.setBounds(46, 185, 817, 287);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        frame.getContentPane().add(scrollPane);

        JLabel lb1_1 = new JLabel("Da dung:");
        lb1_1.setBounds(342, 96, 91, 13);
        frame.getContentPane().add(lb1_1);

        textField = new JTextField();
        textField.setColumns(10);
        textField.setBounds(447, 93, 96, 19);
        frame.getContentPane().add(textField);

        lblConTrong = new JLabel("Con trong:");
        lblConTrong.setBounds(342, 141, 91, 13);
        frame.getContentPane().add(lblConTrong);

        textField_1 = new JTextField();
        textField_1.setColumns(10);
        textField_1.setBounds(447, 138, 96, 19);
        frame.getContentPane().add(textField_1);

        btnChat = new JButton("Chat");
        btnChat.setBounds(728, 137, 111, 21);
        frame.getContentPane().add(btnChat);
        
        chatArea = new JTextArea();
        chatArea.setBounds(897, 174, 271, 249);
        frame.getContentPane().add(chatArea);
        
        chatField = new JTextField();
        chatField.setBounds(897, 453, 151, 19);
        frame.getContentPane().add(chatField);
        chatField.setColumns(10);
        
        JButton btnSend = new JButton("Send");
        btnSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	sendMessageToAllClients();
            }
        });

        btnSend.setBounds(1073, 451, 85, 21);
        frame.getContentPane().add(btnSend);
    }
    
    private void sendMessageToAllClients() {
        String message = chatField.getText();
        if (!message.isEmpty()) {
            String serverMessage = "Server: " + message;
//            chatArea.append(serverMessage + "\n");
            broadcastMessage(serverMessage);
            chatField.setText("");
        }
    }

    public void broadcastMessage(String message) {
        synchronized (clientWriters) {
        	System.out.println(clientWriters.size());
            for (PrintWriter writer : clientWriters) {
            	System.out.println(writer.toString());
                writer.println(message);
                writer.flush();
            }
        }
    }

    public void addClientWriter(PrintWriter writer) {
        synchronized (clientWriters) {
            clientWriters.add(writer);
        }
    }

    public void removeClientWriter(PrintWriter writer) {
        synchronized (clientWriters) {
            clientWriters.remove(writer);
        }
    }

    private void openClientInServerForm() {
        try {
            int numClients = Integer.parseInt(tfNum.getText());
            String serverIP = tfIP.getText();
            int port = 99;

            serverPanel.removeAll();

            clientThreadPool = Executors.newFixedThreadPool(numClients);
            clientFormsMap = new HashMap<>();
            isOpen = new HashMap<>();
            
            int x = 10;
            int y = 10;
            int clientPanelWidth = 250;
            int clientPanelHeight = 150;
            int gap = 10;

            for (int i = 1; i <= numClients; i++) {
                ClientPanel clientForm = new ClientPanel(i); 
                JPanel clientPanel = clientForm.getClientPanel();

                clientPanel.setBounds(x, y, clientPanelWidth, clientPanelHeight);
                serverPanel.add(clientPanel);
                
                clientFormsMap.put(i, clientForm);
                isOpen.put(i, false);

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

            new Thread(() -> {
                try {
                    serverSocket = new ServerSocket(port, 10, InetAddress.getByName(serverIP));
                    chatArea.append("Server started on IP: " + serverIP + " and port: " + port + "\n");

                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        chatArea.append("Client đã kết nối!\n");

                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        addClientWriter(out);

                        MyProcess clientHandler = new MyProcess(clientSocket, clientProcesses.size() + 1, serverPanel, clientFormsMap, isOpen, clientProcesses, this);
                        clientProcesses.add(clientHandler);
                        clientThreadPool.submit(clientHandler);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            
//            new Thread(() -> startServer(port, serverIP)).start();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Vui lòng nhập số lượng client hợp lệ!", "Lỗi: Nhập sai số lượng client", JOptionPane.ERROR_MESSAGE);
        } 
    }
    
    public void appendToChatArea(String message) {
        chatArea.append(message);
    }
}  