package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class ClientChatForm implements Runnable{
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
    private DataOutputStream dos;
    private DataInputStream dis;
    private String name;
    private Stage clientFormStage;
    private boolean isRunning = true;
	
	public ClientChatForm(Socket socketChat, String name, Stage clientFormStage) {
		initializeUI();
		try {
			this.socket = socketChat;
			this.dos = new DataOutputStream(socket.getOutputStream());
			this.dis = new DataInputStream(socket.getInputStream());
			this.name = name;
			this.clientFormStage = clientFormStage;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
    
    private void initializeUI() {
    	Platform.runLater(() -> {
    	    try {
    	        FXMLLoader loader = new FXMLLoader(getClass().getResource("ClientChatForm.fxml"));
    	        loader.setController(this);
    	        Parent root = loader.load();

    	        stage = new Stage();
    	        stage.setTitle("Client Chat Form");
    	        stage.setScene(new Scene(root, 400, 500));
    	        stage.setOnCloseRequest(event -> {
//                    stage.hide(); 
                    event.consume(); 
                });
    	        stage.show();

    	        setEvents();
    	    } catch (IOException e) {
    	        e.printStackTrace();
    	    }
    	});

    }
	
	private void setEvents() {
		btnSend.setOnAction(event -> sendMessage());
	    chatField.setOnAction(event -> sendMessage());
	}

	private void sendMessage() {
    	String message = chatField.getText();
        if(!message.equals("")) {
        	try {
                dos.writeUTF(name + ": " +message); 
                dos.flush();  
                chatField.setText(""); 
                appendText("You: " + message + "\n");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private void receiveMessage() {
    	 try {
             while (isRunning) {
                 String message = dis.readUTF(); 
                 System.out.println("Message in ClientChatForm: " + message);
                 if (message != null) {
                     if(message.equals("SERVER_CLOSED")) {
                    	 appendText("Server đã đóng!\n");
                         isRunning = false;
                     }
                     else if (message.startsWith("REMOTE:")) {
                         appendText("Server đang xem màn hình của bạn!\n");
                     }
                     else if (message.startsWith("FILE:")) {
                         String fileName = message.substring(5);
                         appendText("Server đã gửi file: " + fileName + " tại thư mục D:/Remote/File\n");
                     } 
                     else {
                         appendText("Server: " + message + "\n");
                     }
                 }
             }
         } catch (Exception e) {
        	 if (isRunning) {  
                 e.printStackTrace();
                 appendText("Lỗi khi nhận tin nhắn: " + e.getMessage() + "\n");
             }
         } finally {
             try {
            	 Platform.runLater(() -> {
                	 Stage stage = (Stage) btnSend.getScene().getWindow(); 
     	             stage.hide();
            		 clientFormStage.show();
            	 });
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
	
	private void appendText(String msg) {
    	Platform.runLater(() -> {
    	    chatArea.getChildren().add(new Text(msg)); 
    	    scrollPane.setVvalue(1.0);
    	});
    }
}
