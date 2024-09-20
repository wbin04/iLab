package Client;

import javax.swing.JButton;
import javax.swing.JTextField;

import Server.ServerForm;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;

public class ClientChatForm {

    private JFrame frame;
    private JTextField tfIP;
    private JTextField tfName;
    private JTextField tfPort;
    private JTextArea chatArea;
    private JComboBox comboBox;
    private Socket socket;
    private PrintWriter out;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ClientChatForm window = new ClientChatForm();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

        JTextField textField = new JTextField();
        textField.setColumns(10);
        textField.setBounds(10, 397, 254, 19);
        frame.getContentPane().add(textField);

        JButton btnSend = new JButton("Send");
        btnSend.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
//        		sendMessage();
        	}
        });
        btnSend.setBounds(315, 396, 85, 21);
        frame.getContentPane().add(btnSend);

        JLabel lb2 = new JLabel("Nhap IP:");
        lb2.setBounds(10, 47, 91, 13);
        frame.getContentPane().add(lb2);

        tfIP = new JTextField();
        tfIP.setColumns(10);
        tfIP.setBounds(111, 45, 96, 19);
        frame.getContentPane().add(tfIP);

        JLabel lblNhapTen = new JLabel("Nhap ten: ");
        lblNhapTen.setBounds(225, 47, 91, 13);
        frame.getContentPane().add(lblNhapTen);

        tfName = new JTextField();
        tfName.setText("nvbbb");
        tfName.setColumns(10);
        tfName.setBounds(315, 44, 96, 19);
        frame.getContentPane().add(tfName);

        JButton btnConnect = new JButton("Connect");
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
        tfPort.setText("99");
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
        
//        System.out.println(Server.ServerForm.getMsg());
    }
    public void connectServer() {
        try {
            int port = Integer.parseInt(tfPort.getText());
            InetAddress ip = InetAddress.getByName(tfIP.getText());
            socket = new Socket(ip, port);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String machineList = in.readLine();
            if (machineList != null) {
                String[] machines = machineList.split(",");

                comboBox.removeAllItems();
                for (String machine : machines) {
                    comboBox.addItem(machine);
                }
            }

            chatArea.append("Kết nối thành công với server.\n");
        } catch (Exception e) {
            e.printStackTrace();
            chatArea.append("Kết nối thất bại\n");
            JOptionPane.showMessageDialog(frame, "Kết nối thất bại. Vui lòng kiểm tra lại IP và Port.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void joinServer() {
    	try {
    		String clientName = tfName.getText();
            String selectedMachine = comboBox.getSelectedItem().toString();
            out.println(selectedMachine + ":" + clientName);  
            
            chatArea.append("Connected to server as " + clientName + " on machine " + selectedMachine + "\n");
            frame.setTitle(clientName);
		} catch (Exception e) {
			e.printStackTrace();
            chatArea.append("Vào phòng thất bại\n");
		}
    }
}