package Client;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
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
	@Override
	public void start(Stage primaryStage) {
		 try {
			 FXMLLoader loader = new FXMLLoader(getClass().getResource("ClientForm.fxml"));
	         Parent root = loader.load();
	         primaryStage.setTitle("ClientForm");
	         primaryStage.setScene(new Scene(root, 400, 240));
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
		for(int i=1; i<=10; i++) {
        	cbbNum.getItems().add(""+i);
        }
		cbbNum.getSelectionModel().selectFirst();

        btnConnect.setOnAction(event -> {
        	String ip = tfIP.getText();
    		int port = Integer.parseInt(tfPort.getText());
    		String name = tfName.getText();
    		String stt = (String)cbbNum.getValue();
    		
    		try {
				Stage stage = (Stage) btnConnect.getScene().getWindow(); 
	            stage.hide();
				ClientListenerMain clientListenerMain = new ClientListenerMain(ip, port, name, stt, stage);
			} catch (Exception e2) {
				// TODO: handle exception
				System.out.println("Loi btnConnect ClientLoginForm");
			}
        });
    }
}
