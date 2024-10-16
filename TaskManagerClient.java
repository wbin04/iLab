package Client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TaskManagerClient extends JFrame{
	private JTable processTable;
	private DefaultTableModel tableModel;
	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	
	public TaskManagerClient() {
		setTitle("Remote Task Manager");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        try {
			socket = new Socket("localhost", 2222);
			this.dis = new DataInputStream(socket.getInputStream());
			this.dos = new DataOutputStream(socket.getOutputStream());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
        
        tableModel = new DefaultTableModel(new Object[]{"PID", "Command", "User"}, 0);
        processTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(processTable);
        add(scrollPane);
        
        JButton killButton = new JButton("Kill Process");
        add(killButton, BorderLayout.SOUTH);
        
        killButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = processTable.getSelectedRow();
                if (selectedRow != -1) {
                    long pid = (long) tableModel.getValueAt(selectedRow, 0);
                    killProcess(pid);
                }
            }
        });
	}
	// Lấy list process từ server
	private void loadProcessList() {
		try {
			dos.writeUTF("LIST_PROCESSES");
			dos.flush();
			
			int processCount = dis.readInt();
			for(int i = 0; i < processCount; i++) {
				long processId = dis.readLong();
				String command = dis.readUTF();
				String user = dis.readUTF();
				tableModel.addRow(new Object[] {processId,command, user});
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	// Tắt ứng dụng
	private void killProcess(long processId) {
		try {
			dos.writeUTF("KILL_PROCESS");
			dos.writeLong(processId);
			dos.flush();
			JOptionPane.showMessageDialog(this, "Process " + processId + " terminated.");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TaskManagerClient().setVisible(true));
    }
}
