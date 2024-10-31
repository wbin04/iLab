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
	private Socket soc;
	private Dimension serverScreenSize;
    private DataOutputStream dos;
    private DataInputStream dis;
    private ImagePanel imagePanel;
    private Stage stage;

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

        imagePanel = new ImagePanel(soc, serverScreenSize);
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
	
	private void transferFile() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            new Thread(new ClientFileSender(soc, file)).start();
        }
    }
	
	private void taskManager() {
        sendCommand("REQUEST_RUNNING_APPS");
    }
	
	private void sendCommand(String command) {
        try {
            dos.writeUTF(command);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	private void takeScreenShot() {
		try {
			int len = dis.readInt();
			byte tmp[] = new byte[len];
			dis.readFully(tmp);
			ByteArrayInputStream bais = new ByteArrayInputStream(tmp);
			BufferedImage img2 = ImageIO.read(bais);
			
			String path = "D:/Remote/ScreenShot";
//			String path = "C:/Users/Administrator/Downloads/Remote/ScreenShot";
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
//					System.out.println("MessageType: " + messageType);
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
