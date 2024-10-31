package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
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
	private boolean isRunning = true;
	
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
	        while (isRunning) {
	            if (dis.available() > 0) {
	                String message = dis.readUTF();
	                System.out.println("Message in ClientChatForm: " + message);
	                if (message != null) {
	                    if (message.equals("SERVER_CLOSED")) {
	                        chatArea.appendText("Server đã đóng!\n");
	                        isRunning = false;
	                    } else {
	                        chatArea.appendText("Server: " + message + "\n");
	                    }
	                }
	            }
	        }
	    } catch (EOFException eofEx) {
	        chatArea.appendText("Kết nối đã bị đóng.\n");
	        isRunning = false;
	    } catch (IOException e) {
	        if (isRunning) {
	            e.printStackTrace();
	            chatArea.appendText("Lỗi khi nhận tin nhắn: " + e.getMessage() + "\n");
	        }
	    } finally {
	        try {
	            if (dis != null) dis.close();
	            if (socket != null && !socket.isClosed()) socket.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}

	 
	@Override
	public void run() {
		// TODO Auto-generated method stub
		receiveMessage();
	}
}
