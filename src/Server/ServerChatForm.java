package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ServerChatForm implements Runnable{
	@FXML
	private TextArea chatArea;
	@FXML
	private TextField chatField;
	@FXML
	private Button btnSend;
	
	private Stage stage;
	
	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	boolean isChatFormOn = false;
	
	public void set(Socket socketChat, String stt) {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("ServerChatForm.fxml"));
	        loader.setController(this); 
	        Parent root = loader.load();

	        stage = new Stage();
	        stage.setTitle("ServerChatForm");
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
	        e.printStackTrace();
	    }
	}
	
	public void showServerChatForm() {
	    if (stage != null) {
	        stage.show();
	    } else {
	        System.out.println("Stage is null, cannot show chat form.");
	    }
	}

	
	private void setEvents() {
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
                chatArea.appendText("Bạn: " + message + "\n");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
	}
	
	 private void receiveMessage() {
       	 try {
             while (true) {
                String message = dis.readUTF(); 
                chatArea.appendText(message + "\n"); // ten client + message
             }
            } catch (Exception e) {
                e.printStackTrace();
            }
       }
	 
	@Override
	public void run() {
		// TODO Auto-generated method stub
		receiveMessage();
	}
}
