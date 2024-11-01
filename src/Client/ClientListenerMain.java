package Client;
import java.net.*;
import java.util.*;

import javax.swing.SwingUtilities;

import Server.ClientHandler;
import javafx.stage.Stage;

import java.io.*;
public class ClientListenerMain {
	private Socket socket;
	private Socket socketChat;
	private Socket socketRemote;
	private Socket socketFile;
    private DataInputStream dis;
    private DataOutputStream dos;
    private DataInputStream disChat;
    private DataOutputStream dosChat;
    private DataInputStream disRemote;
    private DataInputStream disFile;
    private String stt;
    private ClientChatForm clientChatForm;
    private ClientHandler clientHandler;
//    private Stage 
    public ClientListenerMain(String ipAddress, int port, String name, String stt, Stage stage) {
        try {
        	socket = new Socket(ipAddress, port);
        	socketChat = new Socket(ipAddress, port+1);
        	socketRemote = new Socket(ipAddress, port+2);
        	socketFile = new Socket(ipAddress, port+3);
        	
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            disChat = new DataInputStream(socketChat.getInputStream());
            dosChat = new DataOutputStream(socketChat.getOutputStream());
            disRemote = new DataInputStream(socketRemote.getInputStream());
            disFile = new DataInputStream(socketFile.getInputStream());
            
            clientChatForm = new ClientChatForm(socketChat, name, stage);
            new Thread(clientChatForm).start();
            
            clientHandler = new ClientHandler(socketRemote, socketFile);
			new Thread(clientHandler).start();
			
            this.stt = stt;
//            System.out.println("Client: " + stt + ", " + name);
            dos.writeUTF(stt + "," + name);
            dos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
