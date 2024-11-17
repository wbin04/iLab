package Client;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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
	
	private Socket socketRemote;
    private DataOutputStream dosRemote;
    private DataInputStream disRemote;
    
    private Socket socketFile;
    private DataOutputStream dosFile;
    private DataInputStream disFile;
    
    private Socket socketTM;
    private DataOutputStream dosTM;
    private DataInputStream disTM;
    
    private String stt;
	private Dimension serverScreenSize;
    private ImagePanel imagePanel;
    private Stage stage;
    
    private ClientTaskManager taskManager;

	public ClientListener(Socket socketChat, Socket socketRemote, Socket socketFile, Socket socketTM, String stt) {
		try {
			this.socketChat = socketChat;
						
			this.socketRemote = socketRemote;
			disRemote = new DataInputStream(socketRemote.getInputStream());
			dosRemote = new DataOutputStream(socketRemote.getOutputStream());
			
			this.socketFile = socketFile;
			disFile = new DataInputStream(socketFile.getInputStream());
			dosFile = new DataOutputStream(socketFile.getOutputStream());
			
			this.socketTM = socketTM;
			disTM = new DataInputStream(socketTM.getInputStream());
			dosTM = new DataOutputStream(socketTM.getOutputStream());
			
			this.stt = stt;
			
			int serverWidth = disRemote.readInt();
			int serverHeight = disRemote.readInt();
			serverScreenSize = new Dimension(serverWidth, serverHeight);
		} catch (Exception e) {
			// TODO: handle exception
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

        imagePanel = new ImagePanel(socketRemote, serverScreenSize);
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
        
        imagePanel.setWidth(1344);
        imagePanel.setHeight(756);

        
        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(imagePanel);

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
        blockDomainMenu.setOnAction(event -> showDomainInputDialog());
        shutDownMenu.setOnAction(event -> sendCommand("SHUT_DOWN"));
    }
	
	public void showView() {
	    if (stage != null) {
	        stage.show();
	        try {
				DataOutputStream dosChat = new DataOutputStream(socketChat.getOutputStream());
				dosChat.writeUTF("REMOTE:");
				dosChat.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	public void startListening() {
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
							BufferedImage img2 = ImageIO.read(bais);
							
							imagePanel.updateImage(img2);
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
				// TODO: handle exception
				e.printStackTrace();
			}
		}).start();
	}
	
	private void closeConnections() {
	    try {
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
	        System.out.println("Tất cả kết nối đã được đóng.");
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
}
