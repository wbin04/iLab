package Client;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.util.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.*;
import java.net.*;

public class ClientListener extends JFrame{
	int off = 50;
	Socket soc;
	ImagePanel imagePanel;
	Dimension serverScreenSize;
	DataOutputStream dos;
	DataInputStream dis;

	public ClientListener(Socket socket, String stt) {
		try {
			soc = socket;
			dis = new DataInputStream(soc.getInputStream());
			dos = new DataOutputStream(soc.getOutputStream());
			int serverWidth = dis.readInt();
			int serverHeight = dis.readInt();
			serverScreenSize = new Dimension(serverWidth, serverHeight);
		} catch (Exception e) {
			// TODO: handle exception
		}
		this.setTitle("Màn hình của: " + stt);
		this.setSize(1000, 800);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		imagePanel = new ImagePanel(soc, serverScreenSize);
		getContentPane().add(imagePanel);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnNewMenu = new JMenu("File");
		menuBar.add(mnNewMenu);
		
		JMenuItem TransferFileMenu = new JMenuItem("Truyền file");
		mnNewMenu.add(TransferFileMenu);
		
		
		JMenu mnNewMenu_1 = new JMenu("Tools");
		menuBar.add(mnNewMenu_1);
		
		JMenuItem TaskManagerMenu = new JMenuItem("Task Manager");
		mnNewMenu_1.add(TaskManagerMenu);
		
		JMenuItem ScreenshotMenu = new JMenuItem("Screenshot");
		mnNewMenu_1.add(ScreenshotMenu);
		
		JMenuItem ShutDownMenu = new JMenuItem("Shut down");
		mnNewMenu_1.add(ShutDownMenu);
		this.setVisible(true);
		
		TransferFileMenu.addActionListener(e -> {
			try {
				transferFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		
		TaskManagerMenu.addActionListener(e -> {
			try {
				taskManager();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		
		ScreenshotMenu.addActionListener(e -> {
			try {
				dos.writeUTF("SCREEN_SHOT");
				dos.flush();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		
		ShutDownMenu.addActionListener(e -> {
			try {
				dos.writeUTF("SHUT_DOWN");
				dos.flush();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		this.setVisible(false);
	}
	
	  private void transferFile() throws IOException {
	        // Hiển thị JFileChooser để người dùng chọn file
	        JFileChooser fileChooser = new JFileChooser();
	        int result = fileChooser.showOpenDialog(this);
	        if (result == JFileChooser.APPROVE_OPTION) {
	            File file = fileChooser.getSelectedFile();
	            // Khởi tạo ClientFileSender và truyền file đã chọn	     
	            new Thread(new ClientFileSender(soc, file)).start();
	        }
	    }

	  private void taskManager() throws IOException{
		  dos.writeUTF("REQUEST_RUNNING_APPS");		
		  dos.flush();
	  }
	
	  private void takeScreenShot() {
		  
		try {
			int len = dis.readInt();
			byte tmp[] = new byte[len];
			dis.readFully(tmp);
			ByteArrayInputStream bais = new ByteArrayInputStream(tmp);
			BufferedImage img2 = ImageIO.read(bais);
			
			String path = "D:/Remote/Screenshot";
			File directory = new File(path);
			if (!directory.exists()) {
	            if (directory.mkdirs()) {
	                System.out.println("Thư mục đã được tạo thành công: " + path);
	            } else {
	                System.out.println("Không thể tạo thư mục: " + path);
	            }
	        } else {
	            System.out.println("Thư mục đã tồn tại: " + path);
	        }
			
			LocalDateTime now = LocalDateTime.now();
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
	        String datetime = now.format(formatter);
			
			File outputFile = new File(path + "/screenshot_" + datetime + ".png");
			ImageIO.write(img2, "png", outputFile);
			System.out.println("Screenshot sent");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
	public void startListening() {
		new Thread(()->{
			try {
				while(true) {
					String messageType = dis.readUTF();
					switch(messageType) {
						case "REMOTE_DESKTOP": 
							int len = dis.readInt();
							byte tmp[] = new byte[len];
							dis.readFully(tmp);
							ByteArrayInputStream bais = new ByteArrayInputStream(tmp);
							BufferedImage img2 = ImageIO.read(bais);
							
							imagePanel.updateImage(img2);
							Thread.sleep(50);
							break;
						case "TASK_MANAGER":
							new ClientTaskManager(soc);
							break;
						case "SCREENSHOT":
							takeScreenShot();
							break;
						}			
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}).start();
	}
}
