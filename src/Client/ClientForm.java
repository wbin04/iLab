package Client;

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
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class ClientForm extends Application {
	@FXML
	private TextField tfIP;
	@FXML
	private TextField tfPort;
	@FXML
	private TextField tfName;
	@FXML
	private ComboBox<String> cbbNum;
	@FXML
	private Button btnConnect;
	@FXML
	private FlowPane flowPane;
	@FXML
	private Button btnDisconnect;
	@FXML
	private Button btnJoin;
	
	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	
	@Override
	public void start(Stage primaryStage) {
		 try {
			 FXMLLoader loader = new FXMLLoader(getClass().getResource("ClientForm.fxml"));
	         Parent root = loader.load();
	         primaryStage.setTitle("ClientForm");
	         primaryStage.setScene(new Scene(root, 600, 300));
	         primaryStage.setOnCloseRequest(event -> {
	        	 event.consume(); 
	         });
	         primaryStage.setResizable(false);
	         primaryStage.show();
	         
	         ClientForm controller = loader.getController();
	         controller.setEvents();
		} catch (IOException e) {
			// TODO: handle exception
			System.out.println("ClientForm loi start: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	private void setEvents() {
		flowPane.setVisible(false);
    	
        btnConnect.setOnAction(event -> {
        	btnConnect.setVisible(false);
        	flowPane.setVisible(true);
        	tfName.setDisable(false);
        	cbbNum.setDisable(false);
        	String ip = tfIP.getText();
    		int port = Integer.parseInt(tfPort.getText());
    		
    		try { 
				socket = new Socket(ip, port);
				dis = new DataInputStream(socket.getInputStream());
				dos = new DataOutputStream(socket.getOutputStream());
				
				dos.writeUTF("CONNECT_TO_SERVER");
				dos.flush();
				
				String listMachines = dis.readUTF();
				System.out.println(listMachines);
				String[] machines = listMachines.split(",");

				for (String machine : machines) {
				    if (!machine.isEmpty()) { 
				        cbbNum.getItems().add(machine);
				    }
				}
				cbbNum.getSelectionModel().selectFirst();
			} catch (Exception e2) {
				// TODO: handle exception
				System.out.println("Loi btnConnect ClientLoginForm");
				btnConnect.setVisible(true);
	        	flowPane.setVisible(false);
	        	tfName.setDisable(true);
	        	cbbNum.setDisable(true);
			}
        });
        
        btnJoin.setOnAction(event -> {
        	btnConnect.setVisible(true);
        	flowPane.setVisible(false);
        	tfName.setDisable(true);
        	cbbNum.setDisable(true);
        	String ip = tfIP.getText();
    		int port = Integer.parseInt(tfPort.getText());
    		String name = tfName.getText();
    		String stt = (String)cbbNum.getValue();
        	try {
				Stage stage = (Stage) btnJoin.getScene().getWindow();
				dos.writeUTF(stt + "," + name);
	            dos.flush();
	            
	            new ClientListenerMain(ip, port, name, stt, stage);
				
				dos.close();
				dis.close();
			} catch (Exception e2) {
				// TODO: handle exception
				System.out.println("Loi btnJoin ClientLoginForm");
				btnConnect.setVisible(true);
	        	flowPane.setVisible(false);
	        	tfName.setDisable(true);
	        	cbbNum.setDisable(true);
			}
        });
        
        btnDisconnect.setOnAction(event -> {
        	try {
				dos.writeUTF("DISCONNECTED");
				dos.flush();
		        
		        btnConnect.setVisible(true);
	        	flowPane.setVisible(false);
	        	tfName.setDisable(true);
	        	cbbNum.setDisable(true);
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("Loi btnJoin ClientLoginForm");
				btnConnect.setVisible(true);
	        	flowPane.setVisible(false);
	        	tfName.setDisable(true);
	        	cbbNum.setDisable(true);
			}
        });
    }
}
