package Server;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
public class ClientHandlerTaskManager implements Runnable{
	 private Socket socket;

	    public ClientHandlerTaskManager(Socket socket) {
	        this.socket = socket;
	    }

	    @Override
	    public void run() {
	        try (DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
	            List<String[]> apps = getRunningApps();

	            // Gửi số lượng ứng dụng cho client
	            output.writeInt(apps.size());

	            // Gửi từng ứng dụng với tên và ID
	            for (String[] app : apps) {
	                output.writeUTF(app[0]); // Tên ứng dụng
	                output.writeUTF(app[1]); // PID ứng dụng
	            }

	            output.flush();
	            System.out.println("Sent data to client");

	        } catch (IOException ex) {
	            ex.printStackTrace();
	        }
	    }

	    private List<String[]> getRunningApps() {
	        List<String[]> apps = new ArrayList<>();
	        try {
	            String command = "powershell.exe gps | where {$_.mainwindowhandle -ne 0} | select ProcessName, Id";
	            Process process = Runtime.getRuntime().exec(command);
	            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

	            String line;
	            while ((line = reader.readLine()) != null) {
	                if (line.trim().isEmpty() || line.startsWith("ProcessName") || line.startsWith("--")) {
	                    continue;
	                }

	                // Tách tên ứng dụng và PID
	                String[] parts = line.trim().split("\\s+");
	                if (parts.length >= 2) {
	                    String appName = parts[0];
	                    String appId = parts[1];
	                    apps.add(new String[]{appName, appId});
	                }
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return apps;
	    }
}
