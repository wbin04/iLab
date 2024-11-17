package Server;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

import java.net.*;
import java.io.*;

public class ClientHandler implements Runnable{
	private String id;
	private Socket socketRemote;
	private Socket socketFile;
	private Socket socketTM;
	
	private DataInputStream disRemote;
	private DataOutputStream dosRemote;
	
	private DataInputStream disFile;
	private DataOutputStream dosFile;
	
	private DataInputStream disTM;
	private DataOutputStream dosTM;
	
	private boolean isRunning;
	
	public ClientHandler(Socket socket, Socket socketFile, Socket socketTM) {
		this.socketRemote = socket;
		this.socketFile = socketFile;
		this.socketTM = socketTM;
		this.id = "1";
		this.isRunning = true;
		try {
			this.disRemote = new DataInputStream(socket.getInputStream());
			this.dosRemote = new DataOutputStream(socket.getOutputStream());
			
			this.disFile = new DataInputStream(socketFile.getInputStream());
			this.dosFile = new DataOutputStream(socketFile.getOutputStream());
			
			this.disTM = new DataInputStream(socketTM.getInputStream());
			this.dosTM = new DataOutputStream(socketTM.getOutputStream());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		 Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         try {
			dosRemote.writeInt(screenSize.width);
			dosRemote.writeInt(screenSize.height);
			dosRemote.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         // Them luong chat vao day
         new Thread(this::handleClientEvents).start();
         new Thread(this::handleRemoteDesktop).start();
         new Thread(this::handleFileTransfer).start();
         new Thread(this::sendRunningApps).start();
         new Thread(this::killApp).start(); 
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
//	    while(isRunning) {
	    	try {
//		    	 synchronized(dosTM) {
		    		 dosRemote.writeUTF("TASK_MANAGER");  
		             int preSize = 0;
		             while(isRunning) {
		            	 List<String[]> apps = getRunningApps();
			             if(apps.size() != preSize) {
			            	 dosTM.writeInt(apps.size());  
				             for (String[] app : apps) {
				            	 dosTM.writeUTF(app[0]);  
				            	 dosTM.writeUTF(app[1]);  
				             }
				             dosTM.flush();
				             preSize = apps.size();
			             }
		             }
//		         }
		        
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
//	    }
	}
	
	private void sendScreenShot() {
		try {
			synchronized(dosRemote) {
				Robot r = new Robot();
				Rectangle rectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
				BufferedImage img = r.createScreenCapture(rectangle);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(img, "png", baos);
				byte[] imageBytes = baos.toByteArray();
				
				dosRemote.writeUTF("SCREENSHOT");
				dosRemote.writeInt(imageBytes.length);
				dosRemote.write(imageBytes);
				dosRemote.flush();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public void sendMessage(String message) {
		try {
			synchronized(dosRemote) {
				dosRemote.writeUTF(message);
				dosRemote.flush();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private void commandShutDown() {
		try {
			Runtime.getRuntime().exec("shutdown -s -t 3600");
			synchronized (dosRemote) {
				dosRemote.writeUTF("SHUTDOWN");
				dosRemote.writeUTF("Máy tính sẽ được tắt sau 60 phút");
				dosRemote.flush();
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	private void handleRemoteDesktop() {
        while (isRunning) {
            try {
            	
                Robot r = new Robot();
                Rectangle rectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                BufferedImage img = r.createScreenCapture(rectangle);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img, "png", baos);
                byte[] imageBytes = baos.toByteArray();
                
                if (socketRemote != null && !socketRemote.isClosed()) {
                    synchronized (dosRemote) {
                    	dosRemote.writeUTF("REMOTE_DESKTOP");  
                    	dosRemote.writeInt(imageBytes.length);
                    	dosRemote.write(imageBytes);
                    	dosRemote.flush();
                    }
                }


                Thread.sleep(50);
            
            } catch (SocketException e) {
//                System.out.println("handleRemoteDesktop ClientHandler Socket closed: " + e.getMessage());
            	e.printStackTrace();
                break;
            } catch (Exception e) {
//                e.printStackTrace();
            	System.out.println("Lỗi handleRemoteDesktop ClientHandler");
            	break;
            }
        }
    }
	private void killApp() {
		System.out.println(111);
		while(isRunning) {
			try {
				System.out.println(111);
				String appId = disTM.readUTF();
		        System.out.println("Received kill request for app ID: " + appId);
		        Process process = Runtime.getRuntime().exec("taskkill /F /PID " + appId);

		        // Chờ lệnh taskkill hoàn thành
		        int exitCode = process.waitFor();
		        if (exitCode == 0) {
		            System.out.println("App with ID " + appId + " was killed successfully.");
		            
		        } else {
		            System.out.println("Failed to kill app with ID " + appId + ". Exit code: " + exitCode);
		        }
			} catch (SocketException e) {
	            System.out.println("err killapp");
	            closeAllConnections();
	        } catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		
	}
    private void handleClientEvents() {
        try {
        	Robot robot = new Robot();
//            robot.setAutoDelay(50);
//            robot.setAutoWaitForIdle(true);

            while (isRunning) {
                if(disRemote.available() > 0) {
                	String eventType = disRemote.readUTF();
                    System.out.println("input: " + eventType);
                    if(eventType.equals("REQUEST_RUNNING_APPS")) System.out.println("eventType la : " + eventType);
                    switch (eventType) {
                        case "MOUSE_PRESS":
                        case "MOUSE_RELEASE":
                            int x = disRemote.readInt();
                            int y = disRemote.readInt();
                            int button = disRemote.readInt();
                            System.out.println("MOUSE_PRESS to: (" + x + ", " + y + ") with button " + button);
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
                            x = disRemote.readInt();
                            y = disRemote.readInt();
                            System.out.println("MOUSE_MOVE to: (" + x + ", " + y + ")");
                            robot.mouseMove(x, y);
                            break;
                            
                        case "MOUSE_DRAGGED":
                            x = disRemote.readInt();
                            y = disRemote.readInt();
                            System.out.println("MOUSE_DRAGGED to: (" + x + ", " + y + ")");
                            robot.mouseMove(x, y);
                            break;    
                        
                        case "MOUSE_WHEEL":
                            int wheelAmt = disRemote.readInt();
                            robot.mouseWheel(wheelAmt);       
                            break;    	
                            
                        case "KEY_PRESS":
                        case "KEY_RELEASE":
                            int keyCode = disRemote.readInt();
                            if (eventType.equals("KEY_PRESS")) {
                                robot.keyPress(keyCode);
                            } else {
                                robot.keyRelease(keyCode);
                            }
                            break;
                            
                        case "KEY_TYPED":
                            String text = disRemote.readUTF();
                            setClipboardContents(text);
                            robot.delay(50);
                            pasteFromClipboard(robot);
                            break;


//                        case "TRANSFER_FILE":
//                        	handleFileTransfer();
//                        	break;
//                        case "REQUEST_RUNNING_APPS":
//                        	new Thread(this::sendRunningApps).start();  
//                        	new Thread(this::killApp).start();   
//                        	killApp();
//                        	break;
//                        case "KILL_APP":
//                        	new Thread(this::killApp).start(); 
//                        	killApp();
//                        	sendRunningApps();
//                        	break;
                        case "SCREEN_SHOT":
                        	sendScreenShot();
                        	break;
                        case "SHUT_DOWN":
                        	commandShutDown();
                        	break;
                        case "SERVER_CLOSED":
                        	isRunning = false;
                            closeAllConnections();
                            break;
                    }
                    
                }
            }
        } catch (SocketException e) {
            System.out.println("SocketException");
            isRunning = false;
            closeAllConnections();
        }  catch (EOFException e) {
            System.out.println("EOFException");
            isRunning = false;
            closeAllConnections();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void handleFileTransfer() {
    	while(isRunning) {
    		try {
//        		String eventType = inputFile.readUTF();
//        		System.out.println("handleFile " + eventType);
    			String fileName = disFile.readUTF();
    			
    			if(fileName.equals("SERVER_CLOSED")) {
    				isRunning = false;
    			    closeAllConnections();
    				break;
    			}
    			long fileSize = disFile.readLong();
    			
    			System.out.println("Receiving file: " + fileName + ", size: " + fileSize + " bytes");
    			
    			String path = "D:/Remote/File";
//    			String path = "C:/Users/Administrator/Downloads/Remote/File";
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
    			
    			File file = new File(path + "/receive_" + fileName);
    	        int count = 1;
    	        String newFileName = fileName;

    	        while (file.exists()) {
    	            int dotIndex = fileName.lastIndexOf(".");
    	            if (dotIndex != -1) {
    	                newFileName = fileName.substring(0, dotIndex) + "(" + count + ")" + fileName.substring(dotIndex);
    	            } else {
    	                newFileName = fileName + "(" + count + ")";
    	            }
    	            file = new File(path + "/receive_" + newFileName);
    	            count++;
    	        }
    			
    			FileOutputStream fileOut = new FileOutputStream(file);
    			
    			
    			byte[] buffer = new byte[8192];
    			int bytesRead;
    			long totalBytesRead = 0;
    			
    			while (totalBytesRead < fileSize && (bytesRead = disFile.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
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
    
    private void setClipboardContents(String text) {
        StringSelection selection = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }

    
    private void pasteFromClipboard(Robot robot) {
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.delay(50); 
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.delay(50); 
    }
    
    private void closeAllConnections() {
    	isRunning = false;
        try {
            if (disRemote != null) disRemote.close();
            if (dosRemote != null) dosRemote.close();
            if (socketRemote != null && !socketRemote.isClosed()) socketRemote.close();
            
            if (disFile != null) disFile.close();
            if (dosFile != null) dosFile.close();
            if (socketFile != null && !socketFile.isClosed()) socketFile.close();
            
            if (disTM != null) disTM.close();
            if (dosTM != null) dosTM.close();
            if (socketTM != null && !socketTM.isClosed()) socketTM.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
