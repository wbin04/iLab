package Client;

import java.net.*;

import Server.ClientHandler;
import javafx.application.Platform;
import javafx.stage.Stage;

public class ClientListenerMain {
	private Socket socketChat;
	private Socket socketImg;
	private Socket socketRemote;
	private Socket socketMouse;
	private Socket socketKeyboard;
	private Socket socketFile;
	private Socket socketTaskManager;
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
        	socketMouse = new Socket(ipAddress, port+4);
        	socketKeyboard = new Socket(ipAddress, port+5);
        	socketFile = new Socket(ipAddress, port+6);
        	socketTaskManager = new Socket(ipAddress, port+7);
        	socketStream = new Socket(ipAddress, port+8);
        	socketBD = new Socket(ipAddress, port+9);
        	socketCam = new Socket(ipAddress, port+10);
			
        	clientChatForm = new ClientChatForm(socketChat, name, stage);
            new Thread(clientChatForm).start();
            
            clientHandler = new ClientHandler(socketImg, socketRemote, socketMouse, socketKeyboard, socketFile, socketTaskManager, socketStream, socketBD);
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
