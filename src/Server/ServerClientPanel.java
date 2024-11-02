package Server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import Client.ClientListener;
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
	private Label lbIP;
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
	private Socket socket = null;
    private Socket socketChat = null;
    private Socket socketRemote = null;
    private Socket socketFile = null;
    private DataOutputStream dosChat;
    private DataOutputStream dosRemote;
    private DataOutputStream dosFile;
    ServerChatForm serverChatForm;
    ClientListener clientListener;
	
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
	}
	
	public void setIP(String IP) {
		controller.lbIP.setText(IP);
	}
	
	public void setName(String name) {
		controller.lbName.setText(name);
	}
	
	public void setStatus(boolean status) {
		controller.btnChat.setVisible(status);
		controller.btnView.setVisible(status);
		controller.btnFile.setVisible(status);
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public void setSocketChat(Socket socketChat) {
		this.socketChat = socketChat;
		
		serverChatForm = new ServerChatForm();
		serverChatForm.set(this.socketChat, controller.lbNum.getText());
    	new Thread(serverChatForm).start();
	}

	public void setSocketRemote(Socket socketRemote, Socket socketFile) {
		this.socketRemote = socketRemote;
		this.socketFile = socketFile;
		
		clientListener = new ClientListener(this.socketChat, this.socketRemote, this.socketFile, controller.lbNum.getText());
		clientListener.startListening();
	}
	
	public void setEvents() {
		controller.btnChat.setOnAction(event -> {
	    	try {
				dosChat = new DataOutputStream(socketChat.getOutputStream());   
				serverChatForm.showServerChatForm();     			
			} catch (Exception e2) {
				// TODO: handle exception
				e2.printStackTrace();
			}
		});
		controller.btnView.setOnAction(event -> {
	    	try {
				dosRemote = new DataOutputStream(this.socketRemote.getOutputStream());  
//				dosRemote.writeUTF("REMOTE_DESKTOP");
				clientListener.showView();
			} catch (Exception e2) {
				// TODO: handle exception
				e2.printStackTrace();
			}
		});
		controller.btnFile.setOnAction(event -> {
	    	try {
				dosFile = new DataOutputStream(this.socketFile.getOutputStream());  
//				dosRemote.writeUTF("REMOTE_DESKTOP");
				clientListener.showFolder();
			} catch (Exception e2) {
				// TODO: handle exception
				e2.printStackTrace();
			}
		});
	}
}
