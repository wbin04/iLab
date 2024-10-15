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
	
	public ClientListener() {
		try {
			soc = new Socket("192.168.2.246", 2222);
			DataInputStream dis = new DataInputStream(soc.getInputStream());
			
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
		this.add(imagePanel);
		this.setVisible(true);
	}
	
//	@Override
//	public void paint(Graphics g) {
//		// TODO Auto-generated method stub
//		try {
//			DataInputStream dis = new DataInputStream(soc.getInputStream());
//			int len = dis.readInt();
//			byte tmp[] = new byte[len];
//			dis.readFully(tmp);
//			ByteArrayInputStream bais = new ByteArrayInputStream(tmp);
//			BufferedImage img2 = ImageIO.read(bais);
//			
//			g.drawImage(img2, off, off, this.getWidth()-off, this.getHeight()-off, 
//					0, 0, img2.getWidth(), img2.getHeight(), null);
//			Thread.sleep(10);
//			this.repaint();
//		} catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//		}
//		
//	}
	
	public void startListening() {
		new Thread(()->{
			try {
				DataInputStream dis = new DataInputStream(soc.getInputStream());
				while(true) {
					int len = dis.readInt();
					byte tmp[] = new byte[len];
					dis.readFully(tmp);
					ByteArrayInputStream bais = new ByteArrayInputStream(tmp);
					BufferedImage img2 = ImageIO.read(bais);
					
					imagePanel.updateImage(img2);
					Thread.sleep(50);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}).start();
	}
}
