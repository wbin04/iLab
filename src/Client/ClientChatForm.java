package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
    private Stage clientFormStage;
    private boolean isRunning = true;
	
	public ClientChatForm(Socket socketChat, String name, Stage clientFormStage) {
		initializeUI();
		try {
			this.socket = socketChat;
			this.dos = new DataOutputStream(socket.getOutputStream());
			this.dis = new DataInputStream(socket.getInputStream());
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
    	        stage.setResizable(false);
    	        stage.show();

    	        setEvents();
    	    } catch (IOException e) {
    	        e.printStackTrace();
    	    }
    	});

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
        if(!message.equals("")) {
        	try {
                dos.writeUTF(message); 
                dos.flush();  
                chatField.setText(""); 
                appendText(message, true, false);
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
                    	 appendText("Server đã đóng!", false, false);
                         isRunning = false;
                     }
                     else if (message.startsWith("REMOTE:")) {
                         appendText("Server đang xem màn hình của bạn!", false, false);
                     }
                     else if (message.startsWith("FILE:")) {
                         String fileName = message.substring(5);
                         appendText(fileName, false, true);
                     } 
                     else {
                         appendText(message, false, false);
                     }
                 }
             }
         } catch (Exception e) {
        	 if (isRunning) {  
                 e.printStackTrace();
                 appendText("Lỗi khi nhận tin nhắn: " + e.getMessage(), false, false);
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
	
	private void appendText(String msg, boolean isChat, boolean isFile) {
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
            
            if (isChat) {
                label.setStyle("-fx-font-size: 18px; -fx-fill: black; -fx-background-color: #DCF8C6; -fx-background-radius: 10;");
                textFlow.getChildren().add(label);
                flowPane.getChildren().add(textFlow);
                flowPane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);  
            } 
            else if(isFile) {
            	Text text1 = new Text("Đã nhận: ");
                text1.setStyle("-fx-font-size: 18px; -fx-fill: black;"); 

                Text fileText = new Text(msg);
                fileText.setStyle("-fx-font-size: 18px; -fx-fill: red;"); 
                
                Text text2 = new Text(" tại thư mục ");
                text2.setStyle("-fx-font-size: 18px; -fx-fill: black;"); 

                Text folderText = new Text("D:/Remote/File");
                folderText.setStyle("-fx-font-size: 18px; -fx-fill: red;");

                textFlow.getChildren().addAll(text1, fileText, text2, folderText);
                textFlow.setStyle("-fx-padding: 10px 20px 10px 20px; -fx-background-color: #FFFFFF; -fx-background-radius: 10;");
                flowPane.getChildren().add(textFlow);
                flowPane.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
//                flowPane.setAlignment(Pos.CENTER);
            }
            else {
                label.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;");
                textFlow.getChildren().add(label);
                flowPane.getChildren().add(textFlow);
                flowPane.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);  
            }
            
            chatArea.getChildren().add(flowPane);
            scrollPane.setVvalue(1.0); 
        });
    }
}
