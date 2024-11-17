package Server;

import java.io.IOException;
import java.net.Socket;

import Client.ClientListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ServerClientPanel {
	@FXML
	private Label lbNum;
	@FXML
	private Label lbStatus;
	@FXML
	private Label lbTime;
	@FXML
	private Label lbName;
	@FXML
	private Button btnChat;
	@FXML
	private Button btnView;
	@FXML
	private Button btnFile;
	
	private FXMLLoader loader;
	private ServerClientPanel controller;
    private Socket socketChat = null;
    private Socket socketRemote = null;
    private Socket socketFile = null;
    private Socket socketTM = null;
    
    private String stt;
    private String name;
    
    ServerChatForm serverChatForm;
    ClientListener clientListener;
    private long startTime;
	
	public ServerClientPanel() {
		loader = new FXMLLoader(getClass().getResource("ServerClientPanel.fxml"));
	}
	
	public Parent getPanel(int stt) throws IOException {
        Parent clientPanel = loader.load();

        controller = loader.getController();
        setNumMachine(stt);
        setStatus(false);
        
        return clientPanel;
	}
	
	public void setNumMachine(int stt) {
		controller.lbNum.setText("Máy số " + stt);
		this.stt = ""+ stt;
	}
	
	public void setStartTime(long startTime) {
	    this.startTime = startTime;
	    startUsageTimer();
	}

	private void startUsageTimer() {
	    new Thread(() -> {
	        while (true) {
	            Platform.runLater(() -> {
	                long elapsedTime = System.currentTimeMillis() - startTime;
	                long seconds = (elapsedTime / 1000) % 60;
	                long minutes = (elapsedTime / (1000 * 60)) % 60;
	                long hours = (elapsedTime / (1000 * 60 * 60)) % 24;
	                String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
	                controller.lbTime.setText("Thời gian sử dụng: " + timeString);
	            });
	            try {
	                Thread.sleep(1000); 
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        }
	    }).start();
	}
	
	public void setName(String name) {
		this.name = name;
		controller.lbName.setText("Họ tên: " + name);
	}
	
	public void setStatus(boolean status) {
		controller.btnChat.setVisible(status);
		controller.btnView.setVisible(status);
		controller.btnFile.setVisible(status);
		if(status) {
			controller.lbStatus.setText("Đã kết nối");
			controller.lbStatus.setStyle("-fx-text-fill: green;");
		}
		else {
			controller.lbStatus.setText("Chưa kết nối");
			controller.lbStatus.setStyle("-fx-text-fill: red;");
		}
	}

	public void setSocketChat(Socket socketChat) {
		this.socketChat = socketChat;
		
		serverChatForm = new ServerChatForm();
		serverChatForm.set(this.socketChat, stt, name);
    	new Thread(serverChatForm).start();
	}

	public void setSocketRemote(Socket socketRemote, Socket socketFile, Socket socketTM) {
		this.socketRemote = socketRemote;
		this.socketFile = socketFile;
		this.socketTM = socketTM;
		
		clientListener = new ClientListener(this.socketChat, this.socketRemote, this.socketFile, this.socketTM, this.stt);
		clientListener.startListening();
	}
	
	public void setEvents() {
		controller.btnChat.setOnAction(event -> {
	    	try { 
				serverChatForm.showServerChatForm();     			
			} catch (Exception e2) {
				// TODO: handle exception
				e2.printStackTrace();
			}
		});
		controller.btnView.setOnAction(event -> {
	    	try {
				clientListener.showView();
			} catch (Exception e2) {
				// TODO: handle exception
				e2.printStackTrace();
			}
		});
		controller.btnFile.setOnAction(event -> {
	    	try {
				clientListener.showFolder();
			} catch (Exception e2) {
				// TODO: handle exception
				e2.printStackTrace();
			}
		});
	}
}
