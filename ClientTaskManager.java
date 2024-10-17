package RemoteDesktopClient;

import java.net.*;
import java.awt.BorderLayout;
import java.io.*;
import java.util.*;

import javax.swing.JFrame;
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
	public ClientTaskManager(Socket socket) {
		setSize(400, 300);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());
		
		String[] columnNames = {"Name App", "ID App"};
		
		tableModel = new DefaultTableModel(columnNames, 0);
        processTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(processTable);
        add(scrollPane, BorderLayout.CENTER);
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
