package Client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class TaskManagerClient extends JFrame {
    private JTable appTable;
    private DefaultTableModel tableModel;

    public TaskManagerClient() {
        setTitle("Task Manager Client");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Tạo bảng với các cột "App Name" và "PID"
        String[] columnNames = {"App Name", "PID"};
        tableModel = new DefaultTableModel(columnNames, 0);
        appTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(appTable);
        
        add(scrollPane, BorderLayout.CENTER);

        // Kết nối và tải dữ liệu từ server
        connectToServer();
    }

    private void connectToServer() {
        try (Socket socket = new Socket("localhost", 2222);
             DataInputStream input = new DataInputStream(socket.getInputStream())) {

            int appCount = input.readInt(); // Đọc số lượng ứng dụng

            // Xóa bảng hiện tại
            tableModel.setRowCount(0);

            // Đọc và thêm dữ liệu ứng dụng vào bảng
            for (int i = 0; i < appCount; i++) {
                String appName = input.readUTF();
                String appId = input.readUTF();
                tableModel.addRow(new String[]{appName, appId});
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TaskManagerClient client = new TaskManagerClient();
            client.setVisible(true);
        });
    }
}

