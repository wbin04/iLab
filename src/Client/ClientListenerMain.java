package Client;
import java.net.*;
import java.util.*;

import javax.swing.SwingUtilities;

import Server.ClientHandler;

import java.io.*;
public class ClientListenerMain {
	private Socket socket;
	private Socket socketChat;
	private Socket socketRemote;
    private DataInputStream dis;
    private DataInputStream disChat;
    private DataInputStream disRemote;
    private DataOutputStream dosChat;
    private DataOutputStream dos;
    private String stt;
    private ClientChatForm clientChatForm;
    private ClientHandler clientHandler;
    public ClientListenerMain(String ipAddress, int port, String name, String stt) {
        try {
        	socket = new Socket(ipAddress, port);
        	socketChat = new Socket(ipAddress, 5000);
        	socketRemote = new Socket(ipAddress, 6000);
        	
            dis = new DataInputStream(socket.getInputStream());
            disChat = new DataInputStream(socketChat.getInputStream());
            disRemote = new DataInputStream(socketRemote.getInputStream());
            dosChat = new DataOutputStream(socketChat.getOutputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            
            clientChatForm = new ClientChatForm(socketChat, name);
            new Thread(clientChatForm).start();
            
            clientHandler = new ClientHandler(socketRemote);
			new Thread(clientHandler).start();
			
            this.stt = stt;
            
            dos.writeUTF(stt + "," + name);
            dos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    
}
