package Client;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javax.imageio.ImageIO;

public class ClientListener {
	int off = 50;
	
	private Socket socketChat;
	
	private Socket socketImg;
	private DataOutputStream dosImg;
	private DataInputStream disImg;
	
	private Socket socketRemote;
    private DataOutputStream dosRemote;
    private DataInputStream disRemote;
    
    private Socket socketFile;
    private DataOutputStream dosFile;
    private DataInputStream disFile;
    
    private Socket socketTM;
    private DataOutputStream dosTM;
    private DataInputStream disTM;
    
    private Socket socketStream;
    private DataOutputStream dosStream;
    private DataInputStream disStream;
    
//    private Socket socketBD;
    
//    private Socket socketCam;
    
    private String stt;
	private Dimension serverScreenSize;
    private ImagePanel remotePanel;
    private Stage stage;
    private ImagePanel streamPanel;
    
    private ClientTaskManager taskManager;
    private ClientBlockDomain blockDomain;

	public ClientListener(Socket socketChat, Socket socketImg, Socket socketRemote, Socket socketFile, Socket socketTM, Socket socketStream, Socket socketBD, Socket socketCam, String stt) {
		try {
			this.socketChat = socketChat;
			
			this.socketImg = socketImg;
			disImg = new DataInputStream(socketImg.getInputStream());
			dosImg = new DataOutputStream(socketImg.getOutputStream());
						
			this.socketRemote = socketRemote;
			disRemote = new DataInputStream(socketRemote.getInputStream());
			dosRemote = new DataOutputStream(socketRemote.getOutputStream());
			
			this.socketFile = socketFile;
			disFile = new DataInputStream(socketFile.getInputStream());
			dosFile = new DataOutputStream(socketFile.getOutputStream());
			
			this.socketTM = socketTM;
			disTM = new DataInputStream(socketTM.getInputStream());
			dosTM = new DataOutputStream(socketTM.getOutputStream());
			
			this.socketStream = socketStream;
			disStream = new DataInputStream(socketStream.getInputStream());
			dosStream = new DataOutputStream(socketStream.getOutputStream());
			
//			this.socketBD = socketBD;
//			DataOutputStream dos = new DataOutputStream(socketBD.getOutputStream());
			
			this.stt = stt;
			
			int serverWidth = disRemote.readInt();
			int serverHeight = disRemote.readInt();
			serverScreenSize = new Dimension(serverWidth, serverHeight);
			
			blockDomain = new ClientBlockDomain(socketBD);
			Platform.runLater(() -> {
		        taskManager = new ClientTaskManager(socketTM);
		    });
			
			
		} catch (Exception e) {
		}
		
		initializeUI(stt);
//        startListening();
	}
	
	private void initializeUI(String stt) {
		stage = new Stage();
        stage.setTitle("Màn hình của: Máy số " + stt);
        
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-font-size: 16px");
        
        Menu fileMenu = new Menu("File");
        MenuItem transferFileMenu = new MenuItem("File transfer");
        MenuItem screenshotMenu = new MenuItem("Screenshot");
        fileMenu.getItems().addAll(transferFileMenu, screenshotMenu);

        Menu toolsMenu = new Menu("Tools");
        MenuItem taskManagerMenu = new MenuItem("Task Manager");
        MenuItem blockDomainMenu = new MenuItem("Block Domain");
        MenuItem shutDownMenu = new MenuItem("Shut down");
        toolsMenu.getItems().addAll(taskManagerMenu, blockDomainMenu, shutDownMenu);

        menuBar.getMenus().addAll(fileMenu, toolsMenu);

        remotePanel = new ImagePanel(socketRemote, serverScreenSize, true);
        streamPanel = new ImagePanel(socketStream, serverScreenSize, false);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();

        double aspectRatio = (double) screenWidth / screenHeight;
        
        double desiredAspectRatio = 16.0 / 9.0;
        
        int panelWidth, panelHeight;

        if (aspectRatio > desiredAspectRatio) {
            panelHeight = screenHeight;
            panelWidth = (int) (panelHeight * desiredAspectRatio);
        } else {
            panelWidth = screenWidth;
            panelHeight = (int) (panelWidth / desiredAspectRatio);
        }
        
        remotePanel.setWidth(1344);
        remotePanel.setHeight(756);

        
        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(remotePanel);

        Scene scene = new Scene(root, 1344, 756);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setOnCloseRequest(event -> {
            stage.hide(); 
            event.consume(); 
        });
        stage.hide();

        transferFileMenu.setOnAction(event -> transferFile());
        screenshotMenu.setOnAction(event -> sendCommand("SCREEN_SHOT"));
        
//        taskManagerMenu.setOnAction(event -> sendCommand("REQUEST_RUNNING_APPS"));
        taskManagerMenu.setOnAction(event -> taskManager.show());
        blockDomainMenu.setOnAction(event -> showBlockedDomain());
        shutDownMenu.setOnAction(event -> sendCommand("SHUT_DOWN"));
    }
	
	public void showView() {
	    if (stage != null) {
	        stage.show();
	    } else {
	        System.out.println("Stage is null, cannot show chat form.");
	    }
	}
	
	public void showFolder() {
		transferFile();
	}
	
	private void transferFile() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            new Thread(new ClientFileSender(socketChat, socketFile, file)).start();
        }
    }
	
	private void sendCommand(String command) {
        try {
            dosRemote.writeUTF(command);
            dosRemote.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	private void takeScreenShot() {
		try {
			int len = disRemote.readInt();
			byte tmp[] = new byte[len];
			disRemote.readFully(tmp);
			ByteArrayInputStream bais = new ByteArrayInputStream(tmp);
			BufferedImage img2 = ImageIO.read(bais);
			
			String path = "D:/Remote/ScreenShot";
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
			
			File outputFile = new File(path + "/screenshot_MaySo" + stt + "_" + datetime + ".png");
			ImageIO.write(img2, "png", outputFile);
			System.out.println("Screenshot sent");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void showBlockedDomain() {
		blockDomain.show();
	}
	
	public void showDomainInputDialog() {
		TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nhập tên miền");
        dialog.setHeaderText("Vui lòng nhập tên miền");
        dialog.setContentText("Tên miền:");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) { 
            String domain = result.get().trim(); 
            if (!domain.isEmpty()) { 
                System.out.println("Tên miền đã nhập: " + domain);
                NetworkMonitor networkMonitor = new NetworkMonitor();
                String[] ips = networkMonitor.getIP(domain);
                networkMonitor.blockDomain(ips);
                showDialog("Đã chặn tên miền: " + domain, true);
            } else {
                showDialog("Tên miền không được để trống!", false);
            }
        } else {
            System.out.println("Đã huỷ blockDomain");
        }
	}
	
	private void showDialog(String message, boolean type) {
        Alert alert;
        if(type) {
        	alert = new Alert(Alert.AlertType.INFORMATION);
        }
        else {
        	 alert = new Alert(Alert.AlertType.ERROR);
        }
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
	
	public void startImgRemoteListening() {
		new Thread(() -> {
			try {
				while (true) {
					int len = disImg.readInt();
					byte tmp[] = new byte[len];
					disImg.readFully(tmp);
					ByteArrayInputStream bais = new ByteArrayInputStream(tmp);
					BufferedImage img = ImageIO.read(bais);
					
					remotePanel.updateImage(img);
					Thread.sleep(50);
				}
			} catch (Exception e) {
//				e.printStackTrace();
				closeConnections();
			}
		}).start();
	}
	
	public void startRemoteListening() {
		new Thread(()->{
			try {
				while(true) {
					String messageType;
					try {
	                    messageType = disRemote.readUTF();
	                } catch (EOFException e) {
	                    System.out.println("Server đã đóng kết nối (EOF).");
	                    closeConnections();
	                    break;
	                }
//					System.out.println("MessageType: " + messageType);
					switch(messageType) {
						case "REMOTE_DESKTOP": 
							int len = disRemote.readInt();
							byte tmp[] = new byte[len];
							disRemote.readFully(tmp);
							ByteArrayInputStream bais = new ByteArrayInputStream(tmp);
							BufferedImage img = ImageIO.read(bais);
							
							remotePanel.updateImage(img);
							Thread.sleep(50);
							break;
						case "TASK_MANAGER":
							Platform.runLater(() -> {
						        taskManager = new ClientTaskManager(socketTM);
						    });
							break;
						case "SCREENSHOT":
							takeScreenShot();
							break;
						case "SERVER_CLOSED": 
	                        System.out.println("Server đã đóng kết nối.");
	                        closeConnections();
	                        return;
						}			
				}
			} catch (Exception e) {
//				e.printStackTrace();
				closeConnections();
			}
		}).start();
	}
	
	public ImagePanel showStream(boolean isHover) {
		if (isHover) {
//			streamPanel.setWidth(576);
//	        streamPanel.setHeight(324);
	        animateCanvasResize(streamPanel, 576, 324);
	    } else {
//	    	streamPanel.setWidth(300);
//	        streamPanel.setHeight(200);
	        animateCanvasResize(streamPanel, 300, 200);
	    }
		return streamPanel;
	}
	
	private void animateCanvasResize(Canvas canvas, double targetWidth, double targetHeight) {
	    Timeline timeline = new Timeline(
	        new KeyFrame(Duration.millis(300),
	            new KeyValue(canvas.widthProperty(), targetWidth),
	            new KeyValue(canvas.heightProperty(), targetHeight)
	        )
	    );
	    timeline.play();
	}

	
	public void startStreamListening() {
		new Thread(()->{
			try {
				while(true) {
					try {
	                    int len2 = disStream.readInt();
						byte tmp2[] = new byte[len2];
						disStream.readFully(tmp2);
						ByteArrayInputStream bais2 = new ByteArrayInputStream(tmp2);
						BufferedImage img2 = ImageIO.read(bais2);
						
						
						Platform.runLater(() -> {
							streamPanel.updateImage(img2);
					    });
						
						Thread.sleep(50);
	                } catch (EOFException e) {
	                    System.out.println("Server đã đóng kết nối (EOF).");
	                    closeConnections();
	                    break;
	                }
//					System.out.println("MessageType: " + messageType);
						
				}
			} catch (Exception e) {
//				e.printStackTrace();
				closeConnections();
			}
		}).start();
	}
	
	private void closeConnections() {
	    try {
	    	Platform.runLater(() -> {
                if (stage != null) {
                    stage.hide();
                }
            });
	    	
	    	if (socketChat != null && !socketChat.isClosed()) socketChat.close();
	    	
	        if (disRemote != null) disRemote.close();
	        if (dosRemote != null) dosRemote.close();
	        if (socketRemote != null && !socketRemote.isClosed()) socketRemote.close();
	        
	        if (disFile != null) disFile.close();
	        if (dosFile != null) dosFile.close();
	        if (socketFile != null && !socketFile.isClosed()) socketFile.close();
	        
	        if (disTM != null) disTM.close();
	        if (dosTM != null) dosTM.close();
	        if (socketTM != null && !socketTM.isClosed()) socketTM.close();
	        
	        if (disStream != null) disStream.close();
	        if (dosStream != null) dosStream.close();
	        if (socketStream != null && !socketStream.isClosed()) socketStream.close();
	        System.out.println("Tất cả kết nối đã được đóng.");
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
}
