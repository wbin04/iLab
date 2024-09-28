package Client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class ClientChatForm {

    private JFrame frame;
    private JTextField tfIP;
    private JTextField tfName;
    private JTextField tfPort;
    private JButton btnConnect;
    private JButton btnDisconnect;
    private JTextArea chatArea;
    private JTextField chatField;
    private JComboBox<String> comboBox;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isConnected = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                ClientChatForm window = new ClientChatForm();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public ClientChatForm() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 450, 464);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        chatArea = new JTextArea();
        chatArea.setBounds(10, 203, 416, 172);
        frame.getContentPane().add(chatArea);

        chatField = new JTextField();
        chatField.setColumns(10);
        chatField.setBounds(10, 397, 254, 19);
        frame.getContentPane().add(chatField);

        JButton btnSend = new JButton("Send");
        btnSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        btnSend.setBounds(315, 396, 85, 21);
        frame.getContentPane().add(btnSend);

        JLabel lb2 = new JLabel("Nhap IP:");
        lb2.setBounds(10, 47, 91, 13);
        frame.getContentPane().add(lb2);

        tfIP = new JTextField();
        tfIP.setText("192.168.1.5");
        tfIP.setColumns(10);
        tfIP.setBounds(111, 45, 96, 19);
        frame.getContentPane().add(tfIP);

        JLabel lblNhapTen = new JLabel("Nhap ten: ");
        lblNhapTen.setBounds(225, 47, 91, 13);
        frame.getContentPane().add(lblNhapTen);

        tfName = new JTextField();
        tfName.setText("aaa");
        tfName.setColumns(10);
        tfName.setBounds(315, 44, 96, 19);
        frame.getContentPane().add(tfName);

        btnConnect = new JButton("Connect");
        btnConnect.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		connectServer();
        	}
        });
        btnConnect.setBounds(68, 132, 85, 21);
        frame.getContentPane().add(btnConnect);

        JLabel lblNhapPort = new JLabel("Nhap port:");
        lblNhapPort.setBounds(10, 93, 91, 13);
        frame.getContentPane().add(lblNhapPort);

        tfPort = new JTextField();
        tfPort.setText("1234");
        tfPort.setColumns(10);
        tfPort.setBounds(111, 90, 96, 19);
        frame.getContentPane().add(tfPort);
        
        JLabel lblChonMay = new JLabel("Chon may:");
        lblChonMay.setBounds(225, 93, 91, 13);
        frame.getContentPane().add(lblChonMay);
        
        comboBox = new JComboBox();
        comboBox.setBounds(315, 89, 107, 21);
        frame.getContentPane().add(comboBox);
        
        JButton btnJoin = new JButton("Join");
        btnJoin.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		joinServer();
        	}
        });
        btnJoin.setBounds(282, 132, 85, 21);
        frame.getContentPane().add(btnJoin);
        
        btnDisconnect = new JButton("Disconnect");
        btnDisconnect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                disconnectFromServer();
            }
        });
        btnDisconnect.setBounds(68, 132, 85, 21);
        btnDisconnect.setVisible(false);
        frame.getContentPane().add(btnDisconnect);
        
    }

    private void connectServer() {
        new Thread(() -> {
            try {
            	chatArea.setText("");
                int port = Integer.parseInt(tfPort.getText());
                InetAddress ip = InetAddress.getByName(tfIP.getText());
                socket = new Socket(ip, port);

                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String machineList = in.readLine();
                System.out.println("List các máy trống: " + machineList);

                if (machineList == null || machineList.isEmpty()) {
                    SwingUtilities.invokeLater(() -> chatArea.append("Không có máy nào sẵn sàng.\n"));
                } else {
                	isConnected = true;
                    String[] machines = machineList.split(",");
                    SwingUtilities.invokeLater(() -> {
                        comboBox.removeAllItems();
                        for (String machine : machines) {
                            comboBox.addItem(machine);
                        }
                        chatArea.append("Kết nối thành công và nhận danh sách máy.\n");
                        btnDisconnect.setVisible(true);
                        btnConnect.setVisible(false);
                    });
                }

            } catch (IOException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    chatArea.append("Kết nối thất bại\n");
                    JOptionPane.showMessageDialog(frame, "Kết nối thất bại. Vui lòng kiểm tra lại IP và Port.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    private void joinServer() {
        new Thread(() -> {
            try {
                String clientName = tfName.getText();
                String selectedMachine = comboBox.getSelectedItem().toString();
                System.out.println("Máy " + selectedMachine + " được chọn");
                if (selectedMachine != null && !selectedMachine.isEmpty()) {
                    out.println(selectedMachine + ":" + clientName);

                    SwingUtilities.invokeLater(() -> {
                    	String noticeMessage = clientName + " ở máy số " + selectedMachine + " kết nối thành công\n";
                        chatArea.append(noticeMessage);
                        frame.setTitle(clientName);
                    });
                    
                    startMessageListener();
                } else {
                    SwingUtilities.invokeLater(() -> chatArea.append("Vui lòng chọn máy.\n"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    chatArea.append("Vào phòng thất bại\n");
                    JOptionPane.showMessageDialog(frame, "Lỗi khi tham gia phòng. Vui lòng thử lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    private void startMessageListener() {
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equals("SERVER_CLOSING")) {
                        handleServerClosing();
                        break;
                    } else {
                        final String finalMessage = message;
                        SwingUtilities.invokeLater(() -> chatArea.append(finalMessage + "\n"));
                    }
                }
            } catch (IOException e) {
            	if (isConnected) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        chatArea.append("Mất kết nối với server.\n");
                        handleDisconnect();
                    });
                }
            }
        }).start();
    }

    private void sendMessage() {
    	String message = chatField.getText();
        if (out != null && !message.isEmpty()) {
            out.println(tfName.getText() + ": " + message); 
            chatArea.append(tfName.getText() + ": " + message + "\n"); 
            chatField.setText(""); 
        } else {
            chatArea.append("Lỗi: Không thể gửi tin nhắn.\n");
        }
    }
    
    private void handleServerClosing() {
        SwingUtilities.invokeLater(() -> {
            chatArea.append("Server đã đóng. Kết nối bị ngắt.\n");
            JOptionPane.showMessageDialog(frame, "Server đã đóng. Kết nối bị ngắt.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            handleDisconnect();
        });
    }
    
    private void handleDisconnect() {
        isConnected = false;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        btnDisconnect.setVisible(false);
        btnConnect.setVisible(true);
        comboBox.removeAllItems();
        frame.setTitle("Client Chat");
    }
    
    private void disconnectFromServer() {
        if (isConnected) {
            try {
                if (out != null) {
                    out.println("DISCONNECT:" + tfName.getText());
                    out.close();
                }
                if (in != null) in.close();
                if (socket != null) socket.close();

                handleDisconnect();
                chatArea.append("Đã ngắt kết nối từ server.\n");
            } catch (IOException e) {
                e.printStackTrace();
                chatArea.append("Lỗi khi ngắt kết nối: " + e.getMessage() + "\n");
            }
        } else {
            chatArea.append("Không có kết nối đến server nào.\n");
        }
    }
}