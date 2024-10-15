package Server;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.util.*;

import javax.imageio.ImageIO;

import java.net.*;
import java.io.*;
public class ClientHandler implements Runnable{
	private String id;
	private Socket socket;
	
	private DataInputStream input;
	private DataOutputStream output;
	
	
	
	public ClientHandler(Socket socket, String id) {
		this.socket = socket;
		this.id = id;
		try {
			this.input = new DataInputStream(socket.getInputStream());
			this.output = new DataOutputStream(socket.getOutputStream());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		 Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         try {
			output.writeInt(screenSize.width);
			output.writeInt(screenSize.height);
	        output.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
		
		new Thread(() -> handleClientEvents()).start();
		
		while(true) {
			try {
				Robot r = new Robot();
				Rectangle rectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
				BufferedImage img = r.createScreenCapture(rectangle);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(img, "png", baos);
				byte[] imageBytes = baos.toByteArray();
				
				output.writeInt(imageBytes.length);
				output.write(imageBytes);
				output.flush();
				Thread.sleep(50);	
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}
	
	   private void handleClientEvents() {
	        try {
	            Robot robot = new Robot();

	            while (true) {
	                // Nhận loại sự kiện từ client
	                String eventType = input.readUTF();

	                switch (eventType) {
	                    case "MOUSE_PRESS":
	                    case "MOUSE_RELEASE":
	                        int x = input.readInt();
	                        int y = input.readInt();
	                        int button = input.readInt();

	                        int mask = button == 1 ? InputEvent.BUTTON1_DOWN_MASK :
	                                   button == 2 ? InputEvent.BUTTON2_DOWN_MASK :
	                                   InputEvent.BUTTON3_DOWN_MASK;

	                        robot.mouseMove(x, y);
	                        if (eventType.equals("MOUSE_PRESS")) {
	                            robot.mousePress(mask);
	                        } else {
	                            robot.mouseRelease(mask);
	                        }
	                        break;

	                    case "MOUSE_MOVE":
	                        x = input.readInt();
	                        y = input.readInt();
	                        robot.mouseMove(x, y);
	                        break;
	                        
	                    case "MOUSE_DRAGGED":
	                        x = input.readInt();
	                        y = input.readInt();
	                        robot.mouseMove(x, y);
	                        break;    
	                    
	                    case "MOUSE_WHEEL":
	                        int wheelAmt = input.readInt();
	                        robot.mouseWheel(wheelAmt);       
	                        break;    	
	                        
	                    case "KEY_PRESS":
	                    case "KEY_RELEASE":
	                        int keyCode = input.readInt();
	                        if (eventType.equals("KEY_PRESS")) {
	                            robot.keyPress(keyCode);
	                        } else {
	                            robot.keyRelease(keyCode);
	                        }
	                        break;
	                }
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
}
