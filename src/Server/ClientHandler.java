package Server;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

public class ClientHandler implements Runnable{
	private String id;
	private Socket socketImage;
	private Socket socketRemote;
	private Socket socketMouse;
	private Socket socketKeyboard;
	private Socket socketFile;
	private Socket socketTaskManager;
	private Socket socketStream;
//	private Socket socketBD;
	
	private DataInputStream disImage;
	private DataOutputStream dosImage;
	
	private DataInputStream disRemote;
	private DataOutputStream dosRemote;
	
	private DataInputStream disMouse;
	private DataOutputStream dosMouse;
	
	private DataInputStream disKeyboard;
	private DataOutputStream dosKeyboard;
	
	private DataInputStream disFile;
	private DataOutputStream dosFile;
	
	private DataInputStream disTaskManager;
	private DataOutputStream dosTaskManager;
	
	private DataInputStream disStream;
	private DataOutputStream dosStream;
	
	private DataInputStream disBD;
	private DataOutputStream dosBD;
	
	private boolean isRunning;
	
	public ClientHandler(Socket socketImage, Socket socketRemote, Socket socketMouse, Socket socketKeyboard, Socket socketFile, Socket socketTaskManager, Socket socketStream, Socket socketBD) {
		this.socketImage = socketImage;
		this.socketRemote = socketRemote;
		this.socketMouse = socketMouse;
		this.socketKeyboard = socketKeyboard;
		this.socketFile = socketFile;
		this.socketTaskManager = socketTaskManager;
		this.socketStream = socketStream;
//		this.socketBD = socketBD;
		
		this.id = "1";
		this.isRunning = true;
		
		try {
			this.disImage = new DataInputStream(socketImage.getInputStream());
			this.dosImage = new DataOutputStream(socketImage.getOutputStream());
			
			this.disRemote = new DataInputStream(socketRemote.getInputStream());
			this.dosRemote = new DataOutputStream(socketRemote.getOutputStream());
			
			this.disMouse = new DataInputStream(socketMouse.getInputStream());
			this.dosMouse = new DataOutputStream(socketMouse.getOutputStream());
			
			this.disKeyboard = new DataInputStream(socketKeyboard.getInputStream());
			this.dosKeyboard = new DataOutputStream(socketKeyboard.getOutputStream());
			
			this.disFile = new DataInputStream(socketFile.getInputStream());
			this.dosFile = new DataOutputStream(socketFile.getOutputStream());
			
			this.disTaskManager = new DataInputStream(socketTaskManager.getInputStream());
			this.dosTaskManager = new DataOutputStream(socketTaskManager.getOutputStream());
			
			this.disStream = new DataInputStream(socketStream.getInputStream());
			this.dosStream = new DataOutputStream(socketStream.getOutputStream());
			
			this.disBD = new DataInputStream(socketBD.getInputStream());
			this.dosBD = new DataOutputStream(socketBD.getOutputStream());
		} catch (Exception e) {
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
		 Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         try {
			dosRemote.writeInt(screenSize.width);
			dosRemote.writeInt(screenSize.height);
			dosRemote.flush();
         } catch (IOException e) {
			e.printStackTrace();
         }
         // Them luong chat vao day
         new Thread(this::handleClientEvents).start();
         new Thread(this::handleMouseEvents).start();
         new Thread(this::handleKeyboardEvents).start();
         new Thread(this::handleRemoteDesktop).start();
         new Thread(this::handleFileTransfer).start();
         new Thread(this::sendRunningApps).start();
         new Thread(this::killApp).start(); 
         new Thread(this::handleStreamDesktop).start();
         new Thread(this::handleBlockDomain).start();
		//List apps
		
	}
	
	private void handleBlockDomain() {
		while(isRunning) {
			try {
				String event = disBD.readUTF();
				String ip = disBD.readUTF();
				String name = disBD.readUTF();
				
				String blockCmd = "cmd.exe /c powershell -Command \"Start-Process powershell -ArgumentList '-Command \"New-NetFirewallRule -DisplayName ''Block IP " + name + "'' -Direction Outbound -Action Block -RemoteAddress " + ip + "\"' -Verb RunAs\"";
				String removeCmd = "cmd.exe /c powershell -Command \"Start-Process powershell -ArgumentList '-Command \"Remove-NetFirewallRule -DisplayName ''Block IP " + name + "''\"' -Verb RunAs\"";
				
				String cmd;
				if(event.equals("BLOCKED")) {
					cmd = blockCmd;
				}
				else {
					cmd = removeCmd;
				}
				
				System.out.println(event);
				System.out.println(cmd);
				ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", cmd);
				Process process = pb.start();
				int exitCode = process.waitFor();
				if (exitCode == 0) {
					System.out.println(event + " thanh cong");
					dosBD.writeUTF("SUCCESSED");
				} else {
					System.out.println(event + " that bai: " + exitCode);
					dosBD.writeUTF("FAILED");
	            }
	            dosBD.flush();
				
			} catch (Exception e) {
//				e.printStackTrace();
				closeAllConnections();
			}
		}
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
	             int preSize = 0;
	             while(isRunning) {
	            	 List<String[]> apps = getRunningApps();
		             if(apps.size() != preSize) {
		            	 dosTaskManager.writeInt(apps.size());  
			             for (String[] app : apps) {
			            	 dosTaskManager.writeUTF(app[0]);  
			            	 dosTaskManager.writeUTF(app[1]);  
			             }
			             dosTaskManager.flush();
			             preSize = apps.size();
		             }
	             }
		        
		    } catch (IOException e) {
//		        e.printStackTrace();
		    	closeAllConnections();
		    }
//	    }
	}
	
	private void sendScreenShot() {
		try {
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendMessage(String message) {
		try {
			dosRemote.writeUTF(message);
			dosRemote.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void commandShutDown() {
		try {
			Runtime.getRuntime().exec("shutdown -s -t 3600");
			dosRemote.writeUTF("SHUTDOWN");
			dosRemote.writeUTF("Máy tính sẽ được tắt sau 60 phút");
			dosRemote.flush();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void handleStreamDesktop() {
        while (isRunning) {
            try {
            	
                Robot r = new Robot();
                Rectangle rectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                BufferedImage img = r.createScreenCapture(rectangle);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img, "png", baos);
                byte[] imageBytes = baos.toByteArray();
                
                if (socketStream != null && !socketStream.isClosed()) {
                	dosStream.writeInt(imageBytes.length);
                	dosStream.write(imageBytes);
                	dosStream.flush();
                }


                Thread.sleep(10);
            
            } catch (SocketException e) {
//                System.out.println("handleRemoteDesktop ClientHandler Socket closed: " + e.getMessage());
            	e.printStackTrace();
                break;
            } catch (Exception e) {
//                e.printStackTrace();
            	System.out.println("Lỗi handleStreamDesktop ClientHandler");
            	break;
            }
        }
    }
	
	private void handleRemoteDesktop() {
        while (isRunning) {
            try {
            	
                Robot r = new Robot();
                Rectangle rectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                BufferedImage img = r.createScreenCapture(rectangle);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img, "jpg", baos);
                byte[] imageBytes = baos.toByteArray();
                
                if (socketImage != null && !socketImage.isClosed()) {
//                	dosImage.writeUTF("REMOTE_DESKTOP");  
                	dosImage.writeInt(imageBytes.length);
                	dosImage.write(imageBytes);
                	dosImage.flush();
                }


                Thread.sleep(10);
            
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
		while(isRunning) {
			try {
				String appId = disTaskManager.readUTF();
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
//	            System.out.println("err killapp");
	            closeAllConnections();
	        } catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private void handleClientEvents() {
        try {
        	Robot robot = new Robot();

            while (isRunning) {
                if(disRemote.available() > 0) {
                	String eventType = disRemote.readUTF();
                    switch (eventType) {
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
//                    Thread.sleep(10);
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
	
	private void handleMouseEvents() {
        try {
        	Robot robot = new Robot();

            while (isRunning) {
                if(disMouse.available() > 0) {
                	String eventType = disMouse.readUTF();
                    switch (eventType) {
                        case "MOUSE_PRESS":
                        case "MOUSE_RELEASE":
                            int x = disMouse.readInt();
                            int y = disMouse.readInt();
                            int button = disMouse.readInt();
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
                            x = disMouse.readInt();
                            y = disMouse.readInt();
                            System.out.println("MOUSE_MOVE to: (" + x + ", " + y + ")");
                            robot.mouseMove(x, y);
                            break;
                            
                        case "MOUSE_DRAGGED":
                            x = disMouse.readInt();
                            y = disMouse.readInt();
                            System.out.println("MOUSE_DRAGGED to: (" + x + ", " + y + ")");
                            robot.mouseMove(x, y);
                            break;    
                        
                        case "MOUSE_WHEEL":
                            int wheelAmt = disMouse.readInt();
                            robot.mouseWheel(wheelAmt);       
                            break;  
                            
                        case "SERVER_CLOSED":
                        	isRunning = false;
                            closeAllConnections();
                            break;
                    }
//                    Thread.sleep(10);
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
	
    private void handleKeyboardEvents() {
        try {
        	Robot robot = new Robot();

            while (isRunning) {
                if(disKeyboard.available() > 0) {
                	String eventType = disKeyboard.readUTF();
                    switch (eventType) {  	                            
                        case "KEY_PRESS":
                        case "KEY_RELEASE":
                            int keyCode = disKeyboard.readInt();
                            if (eventType.equals("KEY_PRESS")) {
                                robot.keyPress(keyCode);
                            } else {
                                robot.keyRelease(keyCode);
                            }
                            break;
                            
                        case "KEY_TYPED":
                            String text = disKeyboard.readUTF();
                            setClipboardContents(text);
                            robot.delay(10);
                            pasteFromClipboard(robot);
                            break;
                        case "SERVER_CLOSED":
                        	isRunning = false;
                            closeAllConnections();
                            break;
                    }
//                    Thread.sleep(10);
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
    
    private void setClipboardContents(String text) {
        StringSelection selection = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }

    
    private void pasteFromClipboard(Robot robot) {
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.delay(10); 
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.delay(10); 
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
//    			e.printStackTrace();
    			closeAllConnections();
    		}
    	}
    }
    
    private void closeAllConnections() {
    	isRunning = false;
        try {
        	if (disImage != null) disImage.close();
            if (dosImage != null) dosImage.close();
            if (socketImage != null && !socketImage.isClosed()) socketImage.close();
        	
            if (disRemote != null) disRemote.close();
            if (dosRemote != null) dosRemote.close();
            if (socketRemote != null && !socketRemote.isClosed()) socketRemote.close();
            
            if (disMouse != null) disMouse.close();
            if (dosMouse != null) dosMouse.close();
            if (socketMouse != null && !socketMouse.isClosed()) socketMouse.close();
            
            if (disKeyboard != null) disKeyboard.close();
            if (dosKeyboard != null) dosKeyboard.close();
            if (socketKeyboard != null && !socketKeyboard.isClosed()) socketKeyboard.close();
            
            if (disFile != null) disFile.close();
            if (dosFile != null) dosFile.close();
            if (socketFile != null && !socketFile.isClosed()) socketFile.close();
            
            if (disTaskManager != null) disTaskManager.close();
            if (dosTaskManager != null) dosTaskManager.close();
            if (socketTaskManager != null && !socketTaskManager.isClosed()) socketTaskManager.close();
            
            if (disStream != null) disStream.close();
            if (dosStream != null) dosStream.close();
            if (socketStream != null && !socketStream.isClosed()) socketStream.close();
            
            if (disBD != null) disBD.close();
            if (dosBD != null) dosBD.close();
//            if (socketBD != null && !socketBD.isClosed()) socketBD.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
