package Client;
import java.net.*;
import java.util.*;

import javax.swing.SwingUtilities;

import Server.ClientHandler;
import javafx.application.Platform;
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
            
            
			
            this.stt = stt;
//            System.out.println("Client: " + stt + ", " + name);
            dos.writeUTF(stt + "," + name);
            dos.flush();
            
            String msg = dis.readUTF();
            if(msg.equals("FALSE")) {
            	clientChatForm = new ClientChatForm(socketChat, name, stage);
                new Thread(clientChatForm).start();
                
                clientHandler = new ClientHandler(socketRemote, socketFile);
    			new Thread(clientHandler).start();
    			
    			Platform.runLater(() -> {
    				stage.hide();
    			});
            }
            else {
            	System.out.println("May da duoc chon");
            	
            	dis.close();
            	dos.close();
            	disChat.close();
            	dosChat.close();
            	disRemote.close();
            	disFile.close();
            	
            	socket.close();
            	socketChat.close();
            	socketRemote.close();
            	socketFile.close();
            	
            	return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
