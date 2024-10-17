package RemoteDesktopClient;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.*;
import java.net.*;

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
	public ClientListener() {
		try {
			soc = new Socket("192.168.2.246", 2222);
			dis = new DataInputStream(soc.getInputStream());
			dos = new DataOutputStream(soc.getOutputStream());
			int serverWidth = dis.readInt();
			int serverHeight = dis.readInt();
			serverScreenSize = new Dimension(serverWidth, serverHeight);
		} catch (Exception e) {
			// TODO: handle exception
		}
		this.setTitle("Remote Desktop");
		this.setSize(1000, 800);
		this.setDefaultCloseOperation(3);
		
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
						}
					
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}).start();
	}
}
