package Client;

import java.net.*;
import java.awt.BorderLayout;
import java.io.*;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ClientTaskManager extends JFrame implements Runnable {
	private JTable processTable;
	private DefaultTableModel tableModel;
	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	public ClientTaskManager(Socket socket) {
		setSize(400, 300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
			this.dos = new DataOutputStream(socket.getOutputStream());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			receiveRunningApps();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void receiveRunningApps() {
		try {
			int appCount = dis.readInt();
			List<String[]> apps = new ArrayList<>();
			for(int i = 0; i < appCount; i++) {
				String appName = dis.readUTF();
				String appId = dis.readUTF();
				apps.add(new String[] {appName, appId});
			}
			
			for(String[] app: apps) {
				tableModel.addRow(app);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
