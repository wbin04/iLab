package Server;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

public class LookupServer {
    private ConcurrentHashMap<String, String> serverRegistry = new ConcurrentHashMap<>();

    public void init(int port) {
        int lookupPort = port;
        try (ServerSocket lookupServerSocket = new ServerSocket(lookupPort)) {
            System.out.println("Lookup server chạy trên cổng: " + lookupPort);

            while (true) {
                Socket clientSocket = lookupServerSocket.accept();
                new Thread(() -> handleRequest(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(Socket clientSocket) {
        try (
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            String action = dis.readUTF();  
            System.out.println(action);
            if (action.equals("REGISTER")) {
                String code = dis.readUTF();
                String serverInfo = dis.readUTF();  // IP:Port:className
                System.out.println(serverInfo);
                serverRegistry.put(code, serverInfo);
                dos.writeUTF("REGISTERED");
            } else if (action.equals("LOOKUP")) {
                String code = dis.readUTF();
                String serverInfo = serverRegistry.get(code);
                if (serverInfo != null) {
                    dos.writeUTF(serverInfo);
                } else {
                    dos.writeUTF("NOT_FOUND");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
