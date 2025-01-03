package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class ServerChatForm implements Runnable{
	@FXML
    private ScrollPane scrollPane;
	@FXML
	private TextFlow chatArea;
	@FXML
	private TextField chatField;
	@FXML
	private Button btnSend;
	
	private Stage stage;
	
	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	private boolean isRunning = true;
	
	private String name;
	
	public void set(Socket socketChat, String stt, String name) {
	    try {
	    	this.name = name;
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("ServerChatForm.fxml"));
	        loader.setController(this); 
	        Parent root = loader.load();

	        stage = new Stage();
	        stage.setTitle("Tin nhắn của Máy số " + stt + ": " + name);
	        stage.setScene(new Scene(root, 400, 500));
	        stage.setOnCloseRequest(event -> {
                stage.hide(); 
                event.consume(); 
            });
	        stage.hide();

	        this.socket = socketChat;
	        this.dis = new DataInputStream(socket.getInputStream());
	        this.dos = new DataOutputStream(socket.getOutputStream());

	        setEvents();
	    } catch (IOException e) {
//	        e.printStackTrace();
	    }
	}
	
	public void showServerChatForm() {
	    if (stage != null) {
	    	stage.setResizable(false);
	        stage.show();
	    } else {
	        System.out.println("Stage is null, cannot show chat form.");
	    }
	}

	
	private void setEvents() {
		chatArea.heightProperty().addListener((observable, oldValue, newValue) -> {
    	    scrollPane.setVvalue(1.0); 
    	});
		btnSend.setOnAction(event -> sendMessage());
	    chatField.setOnAction(event -> sendMessage());
	}

	private void sendMessage() {
		String message = chatField.getText();
        if(!message.equals("")){
        	try {
            	dos.writeUTF(message); 
                dos.flush();  
                System.out.println("Send successfully");
                chatField.setText(""); 
                appendText(message, true);
            } catch (Exception ex) {
//                ex.printStackTrace();
            }
        }
	}
	
	private void receiveMessage() {
	    try {
	        while (isRunning) {
	            if (dis.available() > 0) {
	                String message = dis.readUTF();
	                System.out.println("Message in ClientChatForm: " + message);
	                if (message != null) {
	                    if (message.equals("SERVER_CLOSED")) {
	                        appendText("Server đã đóng!", false);
	                        isRunning = false;
	                    } else {
	                        appendText(message, false);
	                    }
	                }
	            }
	        }
	    } catch (EOFException eofEx) {
	        appendText("Kết nối đã bị đóng.", false);
	        isRunning = false;
	    } catch (IOException e) {
	        if (isRunning) {
//	            e.printStackTrace();
	            appendText("Lỗi khi nhận tin nhắn: " + e.getMessage(), false);
	        }
	    } finally {
	        try {
	        	Platform.runLater(() -> {
	                if (stage != null) {
	                    stage.hide();
	                }
	            });
	            if (dis != null) dis.close();
	            if (socket != null && !socket.isClosed()) socket.close();
	        } catch (IOException e) {
//	            e.printStackTrace();
//	        	System.out.println("loi serverChatForm stage hide");
	        }
	    }
	}

	 
	@Override
	public void run() {
		receiveMessage();
	}
	
	private void appendText(String msg, boolean isChat) {
        Platform.runLater(() -> {
            TextFlow textFlow = new TextFlow();
            textFlow.setMaxWidth(350);  

            Label label = new Label();
            label.setPadding(new Insets(10, 20, 10, 20));
            label.setWrapText(true);  
            label.setMaxWidth(300);  
            label.setText(msg);

            FlowPane flowPane = new FlowPane();
            flowPane.setPrefWidth(350);
            
            Region spacer = new Region();
            spacer.setPrefHeight(50);
            
            if (isChat) {
                label.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-background-color: #102a43; -fx-background-radius: 10;");
                textFlow.getChildren().add(label);
                flowPane.getChildren().add(textFlow);
                flowPane.getChildren().add(spacer);
                flowPane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);  
            } 
            else {
            	Text prefixText = new Text(name + ": ");
            	prefixText.setStyle("-fx-font-size: 18px;"); 
                prefixText.setFill(Color.RED);

                Text fileText = new Text(msg);
                fileText.setStyle("-fx-font-size: 18px; -fx-text-fill: #102a43;"); 

                textFlow.getChildren().addAll(prefixText, fileText);
                textFlow.setStyle("-fx-padding: 10px 20px 10px 20px; -fx-background-color: #FFFFFF; -fx-background-radius: 10;");
                flowPane.getChildren().add(textFlow);
                flowPane.getChildren().add(spacer);
                flowPane.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
//                flowPane.setAlignment(Pos.CENTER);
            }
            
            chatArea.getChildren().add(flowPane);
            scrollPane.setVvalue(1.0); 
        });
    }
}
