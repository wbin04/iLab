package Server;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.lang.ProcessHandle;
import java.lang.ProcessHandle.Info;
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
  
         new Thread(this::handleClientEvents).start();
         new Thread(this::handleRemoteDesktop).start();

		//List apps
		
	}
	
	private List<String[]> getRunningApps() {
        List<String[]> apps = new ArrayList<>();
        try {
            String command = "powershell.exe gps | where {$_.mainwindowhandle -ne 0} | select ProcessName, Id";
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("ProcessName") || line.startsWith("--")) {
                    continue;
                }

                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 2) {
                    String appName = parts[0];
                    String appId = parts[1];
                    apps.add(new String[]{appName, appId});
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return apps;
    }
	
	private void sendRunningApps() {
	    try {
	    	 synchronized(output) {
	             output.writeUTF("TASK_MANAGER");  // Gửi mã định danh trước
	             
	             List<String[]> apps = getRunningApps();
	             output.writeInt(apps.size());  // Gửi số lượng ứng dụng
	             for (String[] app : apps) {
	                 output.writeUTF(app[0]);  // Gửi tên ứng dụng
	                 output.writeUTF(app[1]);  // Gửi ID ứng dụng
	             }
	             output.flush();
	         }
	        
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	private void sendScreenShot() {
		try {
			synchronized(output) {
				Robot r = new Robot();
				Rectangle rectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
				BufferedImage img = r.createScreenCapture(rectangle);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(img, "png", baos);
				byte[] imageBytes = baos.toByteArray();
				
				output.writeUTF("SCREENSHOT");
				output.writeInt(imageBytes.length);
				output.write(imageBytes);
				output.flush();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	private void commandShutDown() {
		try {
			Runtime.getRuntime().exec("shutdown -s -t 3600");
			synchronized (output) {
				output.writeUTF("SHUTDOWN");
				output.writeUTF("Máy tính sẽ được tắt sau 60 phút");
				output.flush();
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	private void handleRemoteDesktop() {
        while (true) {
            try {
            	
                Robot r = new Robot();
                Rectangle rectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                BufferedImage img = r.createScreenCapture(rectangle);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img, "png", baos);
                byte[] imageBytes = baos.toByteArray();
                
                synchronized(output) {
                    output.writeUTF("REMOTE_DESKTOP");  
                    output.writeInt(imageBytes.length);
                    output.write(imageBytes);
                    output.flush();
                }

                Thread.sleep(50);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
	private void killApp() {
		try {
			String appId = input.readUTF();
	        System.out.println("Received kill request for app ID: " + appId);
	        Process process = Runtime.getRuntime().exec("taskkill /F /PID " + appId);

	        // Chờ lệnh taskkill hoàn thành
	        int exitCode = process.waitFor();
	        if (exitCode == 0) {
	            System.out.println("App with ID " + appId + " was killed successfully.");
	            
	        } else {
	            System.out.println("Failed to kill app with ID " + appId + ". Exit code: " + exitCode);
	        }
//	        synchronized (output) {
//	        	List<String[]> apps = getRunningApps();
//	             output.writeInt(apps.size());
//	             for (String[] app : apps) {
//	                 output.writeUTF(app[0]);  
//	                 output.writeUTF(app[1]);  
//	             }
//	             output.flush();
//			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}
    private void handleClientEvents() {
        try {
            Robot robot = new Robot();

            while (true) {
                // Nhận loại sự kiện từ client
                String eventType = input.readUTF();
                if(eventType.equals("REQUEST_RUNNING_APPS")) System.out.println("eventType la : " + eventType);
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
                    case "TRANSFER_FILE":
                    	handleFileTransfer();
                    	break;
                    case "REQUEST_RUNNING_APPS":
                    	sendRunningApps();                
                    	break;
                    case "KILL_APP":
                    	killApp();
//                    	sendRunningApps();
                    	break;
                    case "SCREEN_SHOT":
                    	sendScreenShot();
                    	break;
                    case "SHUT_DOWN":
                    	commandShutDown();
                    	break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void handleFileTransfer() {
    	try {
			String fileName = input.readUTF();
			long fileSize = input.readLong();
			
			System.out.println("Receiving file: " + fileName + ", size: " + fileSize + " bytes");
			
			FileOutputStream fileOut = new FileOutputStream("D:/PBL4/received_" + fileName);
			
			
			byte[] buffer = new byte[8192];
			int bytesRead;
			long totalBytesRead = 0;
			// ở đây vòng while sẽ kiểm tra các bytesRead chạy tới khi đủ số bytes nhận vào, nếu dùng input.read(buffer) nó sẽ tiếp tục chạy tới 8192 bytes
			while (totalBytesRead < fileSize && (bytesRead = input.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
			    fileOut.write(buffer, 0, bytesRead);
			    totalBytesRead += bytesRead;
			}

			fileOut.close();
			System.out.println("File " + fileName + " đã nhận được thành công");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
    }
    
    
}
