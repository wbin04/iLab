package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import Client.ClientPanel;

public class MyProcess implements Runnable {
    private Socket socket;
    private int clientNumber;
    private JPanel serverPanel;
    private BufferedReader in;
    private PrintWriter out;
    private Map<Integer, ClientPanel> clientFormsMap;
    private Map<Integer, Boolean> isOpen;
    private List<MyProcess> clientProcesses;
    private String clientName;
    private int selectedMachine;
    private ServerForm serverForm;

    public MyProcess(Socket socket, int clientNumber, JPanel serverPanel, Map<Integer, ClientPanel> clientFormsMap, Map<Integer, Boolean> isOpen, List<MyProcess> clientProcesses, ServerForm serverForm) {
		this.socket = socket;
		this.clientNumber = clientNumber;
		this.serverPanel = serverPanel;
		this.clientFormsMap = clientFormsMap;
		this.isOpen = isOpen;
		this.clientProcesses = clientProcesses;
		this.serverForm = serverForm; 
	}

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
//            serverForm.addClientWriter(out);
            
            sendAvailableMachineList();
            
            String clientInfo = in.readLine();
            processClientInfo(clientInfo);
            
            String noticeMessage = clientName + " ở máy số " + selectedMachine + " kết nối thành công";
            broadcastMessage(noticeMessage);
            updateServerChatArea(noticeMessage);
            
            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                System.out.println("Received from client " + clientName + ": " + clientMessage);
                broadcastMessage(clientMessage);
                updateServerChatArea(clientMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void sendAvailableMachineList() {
        StringBuilder machineList = new StringBuilder();
        for (int machineNumber : clientFormsMap.keySet()) {
            if (!isOpen.get(machineNumber)) {
                machineList.append(machineNumber).append(",");
            }
        }
        System.out.println("Sending machine list to client: " + machineList.toString());
        out.println(machineList.toString());
        out.flush();
    }

    private void processClientInfo(String clientInfo) {
        String[] parts = clientInfo.split(":");
        selectedMachine = Integer.parseInt(parts[0]);
        clientName = parts[1];
        
        ClientPanel clientForm = clientFormsMap.get(selectedMachine);
        if (clientForm != null) {
            SwingUtilities.invokeLater(() -> {
                clientForm.updateClientName(clientName);
                isOpen.put(selectedMachine, true);
                serverPanel.revalidate();
                serverPanel.repaint();
            });
        }
    }

    private void broadcastMessage(String message) {
        synchronized (clientProcesses) {
            for (MyProcess process : clientProcesses) {
                if (process != this) {
                    process.out.println(message);
                    process.out.flush();
                }
            }
        }
    }

    private void updateServerChatArea(String message) {
        SwingUtilities.invokeLater(() -> {
            serverForm.appendToChatArea(message + "\n");
            
            // cap nhat clientPanel
            ClientPanel clientForm = clientFormsMap.get(selectedMachine);
            if (clientForm != null) {
                // xu ly
            }
        });
    }

    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) {
                serverForm.removeClientWriter(out);
                out.close();
            }
            if (socket != null) socket.close();
            
            isOpen.put(selectedMachine, false);
            SwingUtilities.invokeLater(() -> {
                ClientPanel clientForm = clientFormsMap.get(selectedMachine);
                if (clientForm != null) {
                    clientForm.updateClientName("Available");
                    serverPanel.revalidate();
                    serverPanel.repaint();
                }
            });
            
            synchronized (clientProcesses) {
                clientProcesses.remove(this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}