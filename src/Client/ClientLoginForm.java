package Client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientLoginForm extends JFrame{

   
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField tfIP;
    private JTextField tfName;
    private JTextField tfPort;
    private JButton btnConnect;
    private JComboBox<String> comboBox;
    
    public ClientLoginForm() {
        initialize();
    }

    private void initialize() {
     
        setBounds(100, 100, 450, 224);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JLabel lb2 = new JLabel("Nhập server IP:");
        lb2.setBounds(10, 47, 91, 13);
        add(lb2);

        tfIP = new JTextField();
        tfIP.setText("192.168.1.5");
        tfIP.setColumns(10);
        tfIP.setBounds(111, 45, 96, 19);
        add(tfIP);

        JLabel lblNhapTen = new JLabel("Nhập tên: ");
        lblNhapTen.setBounds(225, 47, 91, 13);
        add(lblNhapTen);

        tfName = new JTextField();
        tfName.setText("aaa");
        tfName.setColumns(10);
        tfName.setBounds(315, 44, 107, 19);
        add(tfName);

       

        JLabel lblNhapPort = new JLabel("Nhập cổng:");
        lblNhapPort.setBounds(10, 93, 91, 13);
        add(lblNhapPort);

        tfPort = new JTextField();
        tfPort.setText("2222");
        tfPort.setColumns(10);
        tfPort.setBounds(111, 90, 96, 19);
        add(tfPort);
        
        JLabel lblChonMay = new JLabel("Chọn máy:");
        lblChonMay.setBounds(225, 93, 91, 13);
        add(lblChonMay);
        
        comboBox = new JComboBox<>();
        comboBox.setBounds(315, 89, 107, 21);
        add(comboBox);
        for(int i=1; i<=10; i++) {
        	comboBox.addItem("" + i);
        }
        
        btnConnect = new JButton("Kết nối");
        btnConnect.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		String ip = tfIP.getText();
        		int port = Integer.parseInt(tfPort.getText());
        		String name = tfName.getText();
        		String stt = (String)comboBox.getSelectedItem();
        		
        		try {
					ClientListenerMain clientListenerMain = new ClientListenerMain(ip, port, name, stt);
					setVisible(false);
					
				} catch (Exception e2) {
					// TODO: handle exception
					System.out.println("Loi btnConnect ClientLoginForm");
				}
        	}
        });
        btnConnect.setBounds(179, 147, 85, 21);
        add(btnConnect);
        this.setVisible(true);
    }
}
