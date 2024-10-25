package Client;

import java.net.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class ClientTaskManager extends JFrame {
	private JTable processTable;
	private DefaultTableModel tableModel;
	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	private JButton killButton;
	public ClientTaskManager(Socket socket) {
		setSize(400, 300);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());
		
		String[] columnNames = {"Name App", "ID App"};
		
		tableModel = new DefaultTableModel(columnNames, 0);
        processTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(processTable);
        add(scrollPane, BorderLayout.CENTER);
        
        killButton = new JButton("Kill App");
        add(killButton, BorderLayout.SOUTH);
        setVisible(true);
		
        try {
			this.socket = socket;
			this.dis = new DataInputStream(socket.getInputStream());
			System.out.println(socket.getInetAddress());
			this.dos = new DataOutputStream(socket.getOutputStream());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        receiveRunningApps();
        
        killButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                killSelectedApp();
            }
        });
	}
	
	public void killSelectedApp() {
		int selectedRow = processTable.getSelectedRow();

        if (selectedRow != -1) {         
            String appId = (String) tableModel.getValueAt(selectedRow, 1);
            try {     
                dos.writeUTF("KILL_APP");
                dos.writeUTF(appId);
                dos.flush();
                System.out.println("Kill request sent for app ID: " + appId);
                //receiveRunningApps();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {           
            JOptionPane.showMessageDialog(this, "Please select an app to kill.");
        }
	}
	public void receiveRunningApps() {
		try {
			synchronized(dis) {
				int appCount = dis.readInt();
				
				System.out.println("appCount la : " + appCount);

				List<String[]> apps = new ArrayList<>();
				tableModel.setRowCount(0);
				for(int i = 0; i < appCount; i++) {
					System.out.println("Round " + i +": ");
					String appName = dis.readUTF();
					String appId = dis.readUTF();
					System.out.println(appName + " ------ " + appId);
					apps.add(new String[] {appName, appId});
				}
				
				
			    for (String[] app : apps) {
			        tableModel.addRow(app);
			    }		
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
