package Client;

import java.net.*;

import Server.ClientHandler;
import javafx.application.Platform;
import javafx.stage.Stage;

public class ClientListenerMain {
	private Socket socketChat;
	private Socket socketRemote;
	private Socket socketFile;
	private Socket socketTM;
	private Socket socketStream;
    private ClientChatForm clientChatForm;
    private ClientHandler clientHandler;

    public ClientListenerMain(String ipAddress, int port, String name, String stt, Stage stage) {
        try {
        	socketChat = new Socket(ipAddress, port+1);
        	socketRemote = new Socket(ipAddress, port+2);
        	socketFile = new Socket(ipAddress, port+3);
        	socketTM = new Socket(ipAddress, port+4);
        	socketStream = new Socket(ipAddress, port+5);
			
        	clientChatForm = new ClientChatForm(socketChat, name, stage);
            new Thread(clientChatForm).start();
            
            clientHandler = new ClientHandler(socketRemote, socketFile, socketTM, socketStream);
			new Thread(clientHandler).start();
			
			Platform.runLater(() -> {
				stage.hide();
			});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
