package Server;

import java.io.*;
import java.net.*;
import java.util.*;

public class TransferServer {
	public TransferServer() {
		try {
			ServerSocket serverSocket = new ServerSocket(5000);
			System.out.println("Server is waiting for connection on port 5000");
			int i = 0;
			while(true) {
				Socket soc = serverSocket.accept();
				System.out.println("New client connected");
				ClientHandler clientHandler = new ClientHandler(soc, "" + soc.getInetAddress());
				++i;
				Thread client = new Thread(clientHandler);
				client.start();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
