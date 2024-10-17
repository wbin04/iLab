package Server;

import java.awt.*;
import java.util.*;
import java.net.*;

public class RemoteServer {
	public RemoteServer() {
		try {
			ServerSocket serverSocket = new ServerSocket(2222);
			System.out.println("Server is waiting for connection on port 2222");
			int i = 0;
			while(true) {
				Socket soc = serverSocket.accept();
				ClientHandler clientHandler = new ClientHandler(soc, "" + i);
				++i;
				Thread client = new Thread(clientHandler);
				client.start();
//				ClientHandlerTaskManager clientTM = new ClientHandlerTaskManager(soc);
//				Thread client = new Thread(clientTM);
//				client.start();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
