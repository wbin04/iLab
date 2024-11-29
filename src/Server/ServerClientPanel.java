package Server;

import java.io.IOException;
import java.net.Socket;

import Client.ClientListener;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.Duration;

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
	@FXML
	private GridPane gridPane;
	@FXML
	private BorderPane borderPane;
	
	private FXMLLoader loader;
	private ServerClientPanel controller;
    private Socket socketChat = null;
    private Socket socketImg = null;
    private Socket socketRemote = null;
    private Socket socketFile = null;
    private Socket socketTM = null;
    private Socket socketStream = null;
    private Socket socketBD = null;
    private Socket socketCam = null;
    
    private String stt;
    private String name;
    
    ServerChatForm serverChatForm;
    ClientListener clientListener;
    ServerCamera serverCamera;
    private long startTime;
    private boolean isStartTime;
    
    private boolean isStream = false;
    private boolean isShowCamera;
    
    private Canvas cameraCanvas;
	
	public ServerClientPanel() {
		loader = new FXMLLoader(getClass().getResource("ServerClientPanel.fxml"));
	}
	
	public Parent getPanel(int stt) {
        Parent clientPanel;
		try {
			clientPanel = loader.load();
			controller = loader.getController();
	        setNumMachine(stt);
	        setStatus(false);
	        
	        return clientPanel;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
        
	}
	
	public void setNumMachine(int stt) {
		controller.lbNum.setText("Máy số " + stt);
		this.stt = ""+ stt;
	}
	
	public void setStartTime(long startTime) {
	    this.startTime = startTime;
	    isStartTime = true;
    	controller.lbTime.setText("Thời gian sử dụng");
    	startUsageTimer();
	}
	
	public void stopTime() {
		isStartTime = false;
		
		Platform.runLater(() -> {
			controller.lbStatus.setText("Đã ngắt kết nối");
			controller.lbStatus.setStyle("-fx-text-fill: orange;");
			
			controller.btnChat.setVisible(false);
			controller.btnView.setVisible(false);
			controller.btnFile.setVisible(false);
		});
		
		controller.gridPane.setOnMouseEntered(event -> {
		    event.consume();  
		});
	}

	private void startUsageTimer() {
	    new Thread(() -> {
	        while (isStartTime) {
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
		if(name.trim().equals("")) {
			controller.lbName.setText("Họ tên");
		}
		else {
			controller.lbName.setText("Họ tên: " + name);
		}
	}
	
	public void setStatus(boolean status) {
		controller.btnChat.setVisible(false);
		controller.btnView.setVisible(false);
		controller.btnFile.setVisible(false);
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

	public void setSocketRemote(Socket socketImg, Socket socketRemote, Socket socketFile, Socket socketTM, Socket socketStream, Socket socketBD, Socket socketCam) {
		this.socketImg = socketImg;
		this.socketRemote = socketRemote;
		this.socketFile = socketFile;
		this.socketTM = socketTM;
		this.socketStream = socketStream;
		this.socketBD = socketBD;
		this.socketCam = socketCam;
		
		clientListener = new ClientListener(this.socketChat, this.socketImg, this.socketRemote, this.socketFile, this.socketTM, this.socketStream, this.socketBD, this.socketCam, this.stt);
		clientListener.startImgRemoteListening();
		clientListener.startRemoteListening();
		clientListener.startStreamListening();
		
		serverCamera = new ServerCamera(this.socketCam);
		serverCamera.startCameraListening();
	}
	
	public void setEvents() {
		controller.btnChat.setOnAction(event -> {
	    	try { 
				serverChatForm.showServerChatForm();     			
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		});
		controller.btnView.setOnAction(event -> {
	    	try {
				clientListener.showView();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		});
		controller.btnFile.setOnAction(event -> {
	    	try {
				clientListener.showFolder();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		});
		controller.gridPane.setOnMouseEntered(event -> {
//			controller.gridPane.setPrefWidth(576);
//			controller.gridPane.setPrefHeight(324);
			animateResize(controller.gridPane, 576, 324);
			
			Pane pane;
			if(isShowCamera) {
				pane = new Pane(serverCamera.canvasResize(cameraCanvas, true));
			}
			else {
				pane = new Pane(clientListener.showStream(true));
			}
		    pane.setPrefWidth(576);
		    pane.setPrefHeight(324);
		    controller.borderPane.setCenter(pane);
		    animateResize(controller.borderPane, 576, 324);
		    
			controller.lbNum.setVisible(false);
			controller.lbStatus.setVisible(false);
			controller.lbTime.setVisible(false);
			controller.lbName.setVisible(false);
			controller.btnChat.setVisible(true);
			controller.btnView.setVisible(true);
			controller.btnFile.setVisible(true);
	    });
		controller.gridPane.setOnMouseExited(event -> {
//			controller.gridPane.setPrefWidth(300);
//			controller.gridPane.setPrefHeight(200);
			animateResize(controller.gridPane, 300, 200);
			if(isStream) {
				controller.borderPane.setCenter(clientListener.showStream(false));
				animateResize(controller.borderPane, 300, 200);
				controller.lbNum.setVisible(false);
				controller.lbStatus.setVisible(false);
				controller.lbTime.setVisible(false);
				controller.lbName.setVisible(false);
				controller.btnChat.setVisible(false);
				controller.btnView.setVisible(false);
				controller.btnFile.setVisible(false);
			}
			else if(isShowCamera) {
				controller.borderPane.setCenter(serverCamera.canvasResize(cameraCanvas, false));
				animateResize(controller.borderPane, 300, 200);
				controller.lbNum.setVisible(false);
				controller.lbStatus.setVisible(false);
				controller.lbTime.setVisible(false);
				controller.lbName.setVisible(false);
				controller.btnChat.setVisible(false);
				controller.btnView.setVisible(false);
				controller.btnFile.setVisible(false);
			}
			else {
				controller.borderPane.setCenter(clientListener.showStream(false));
				controller.borderPane.setCenter(null);
				animateResize(controller.borderPane, 300, 200);
				controller.lbNum.setVisible(true);
				controller.lbStatus.setVisible(true);
				controller.lbTime.setVisible(true);
				controller.lbName.setVisible(true);
				controller.btnChat.setVisible(false);
				controller.btnView.setVisible(false);
				controller.btnFile.setVisible(false);
			}
	    });
	}
	
	public void startCameraView() {
		isShowCamera = true;
	    Platform.runLater(() -> {
	        cameraCanvas = serverCamera.showCamera();  
	        controller.borderPane.setCenter(cameraCanvas);  
	        controller.lbNum.setVisible(false);
			controller.lbStatus.setVisible(false);
			controller.lbTime.setVisible(false);
			controller.lbName.setVisible(false);
			controller.btnChat.setVisible(false);
			controller.btnView.setVisible(false);
			controller.btnFile.setVisible(false);
	    });
	}
	
	public void stopCameraView() {
		if(isShowCamera) {
			isShowCamera = false;
			Platform.runLater(() -> {
				serverCamera.stopCamera();
				controller.borderPane.setCenter(null); 
				controller.lbNum.setVisible(true);
				controller.lbStatus.setVisible(true);
				controller.lbTime.setVisible(true);
				controller.lbName.setVisible(true);
				controller.btnChat.setVisible(false);
				controller.btnView.setVisible(false);
				controller.btnFile.setVisible(false);
		    });
		}
	}
	
	public void startStreamView() {
		isStream = true;
		Platform.runLater(() -> {
//			streamCanvas = clientListener.showStream(false);
			controller.borderPane.setCenter(clientListener.showStream(false));
			controller.lbNum.setVisible(false);
			controller.lbStatus.setVisible(false);
			controller.lbTime.setVisible(false);
			controller.lbName.setVisible(false);
			controller.btnChat.setVisible(false);
			controller.btnView.setVisible(false);
			controller.btnFile.setVisible(false);
		});
	}
	
	public void stopStreamView() {
		if(isStream) {
			isStream = false;
			Platform.runLater(() -> {
				controller.borderPane.setCenter(null);
				controller.lbNum.setVisible(true);
				controller.lbStatus.setVisible(true);
				controller.lbTime.setVisible(true);
				controller.lbName.setVisible(true);
				controller.btnChat.setVisible(false);
				controller.btnView.setVisible(false);
				controller.btnFile.setVisible(false);
			});
		}
	}
	
	private void animateResize(Region pane, double targetWidth, double targetHeight) {
	    Timeline timeline = new Timeline();

	    KeyValue widthValue = new KeyValue(pane.prefWidthProperty(), targetWidth);
	    KeyValue heightValue = new KeyValue(pane.prefHeightProperty(), targetHeight);

	    KeyFrame keyFrame = new KeyFrame(Duration.millis(300), widthValue, heightValue);

	    timeline.getKeyFrames().add(keyFrame);
	    timeline.play();
	}
}
