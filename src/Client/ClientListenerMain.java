package Client;

import java.net.*;

import Server.ClientHandler;
import javafx.application.Platform;
import javafx.stage.Stage;

public class ClientListenerMain {
	private Socket socketChat;
	private Socket socketImg;
	private Socket socketRemote;
	private Socket socketFile;
	private Socket socketTM;
	private Socket socketStream;
	private Socket socketBD;
	private Socket socketCam;
    private ClientChatForm clientChatForm;
    private ClientHandler clientHandler;
    private ClientCamera clientCamera;

    public ClientListenerMain(String ipAddress, int port, String name, String stt, Stage stage) {
        try {
        	socketChat = new Socket(ipAddress, port+1);
        	socketImg = new Socket(ipAddress, port+2);
        	socketRemote = new Socket(ipAddress, port+3);
        	socketFile = new Socket(ipAddress, port+4);
        	socketTM = new Socket(ipAddress, port+5);
        	socketStream = new Socket(ipAddress, port+6);
        	socketBD = new Socket(ipAddress, port+7);
        	socketCam = new Socket(ipAddress, port+8);
			
        	clientChatForm = new ClientChatForm(socketChat, name, stage);
            new Thread(clientChatForm).start();
            
            clientHandler = new ClientHandler(socketImg, socketRemote, socketFile, socketTM, socketStream, socketBD);
			new Thread(clientHandler).start();
			
			clientCamera = new ClientCamera(socketCam);
			new Thread(clientCamera).start();
			
			Platform.runLater(() -> {
				stage.hide();
			});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
