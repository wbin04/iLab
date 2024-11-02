package Client;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    
    private String stt;
	private Dimension serverScreenSize;
    private ImagePanel imagePanel;
    private Stage stage;

	public ClientListener(Socket socketChat, Socket socketRemote, Socket socketFile, String stt) {
		try {
			this.socketChat = socketChat;
						
			this.socketRemote = socketRemote;
			disRemote = new DataInputStream(socketRemote.getInputStream());
			dosRemote = new DataOutputStream(socketRemote.getOutputStream());
			
			this.socketFile = socketFile;
			disFile = new DataInputStream(socketFile.getInputStream());
			dosFile = new DataOutputStream(socketFile.getOutputStream());
			
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
        stage.setTitle("Màn hình của: " + stt);
        
        MenuBar menuBar = new MenuBar();
        
        Menu fileMenu = new Menu("File");
        MenuItem transferFileMenu = new MenuItem("Truyền file");
        fileMenu.getItems().add(transferFileMenu);

        Menu toolsMenu = new Menu("Tools");
        MenuItem taskManagerMenu = new MenuItem("Task Manager");
        MenuItem screenshotMenu = new MenuItem("Screenshot");
        MenuItem shutDownMenu = new MenuItem("Shut down");
        toolsMenu.getItems().addAll(taskManagerMenu, screenshotMenu, shutDownMenu);

        menuBar.getMenus().addAll(fileMenu, toolsMenu);

        imagePanel = new ImagePanel(socketRemote, serverScreenSize);
        imagePanel.setWidth(800);
        imagePanel.setHeight(600);

        
        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(imagePanel);

        Scene scene = new Scene(root, 1000, 800);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            stage.hide(); 
            event.consume(); 
        });
        stage.hide();

        transferFileMenu.setOnAction(event -> transferFile());
        taskManagerMenu.setOnAction(event -> taskManager());
        screenshotMenu.setOnAction(event -> sendCommand("SCREEN_SHOT"));
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
	
	private void taskManager() {
        sendCommand("REQUEST_RUNNING_APPS");
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
							new ClientTaskManager(socketRemote);
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
	        if (disRemote != null) disRemote.close();
	        if (dosRemote != null) dosRemote.close();
	        if (socketRemote != null && !socketRemote.isClosed()) socketRemote.close();
	        if (disFile != null) disFile.close();
	        if (dosFile != null) dosFile.close();
	        if (socketFile != null && !socketFile.isClosed()) socketFile.close();
	        System.out.println("Tất cả kết nối đã được đóng.");
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
}
