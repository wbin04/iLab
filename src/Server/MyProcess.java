package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

import javax.swing.JPanel;

import Client.ClientPanel;

public class MyProcess implements Runnable {
    private Socket socket;
    private int clientNumber;
    private JPanel serverPanel;
    private BufferedReader in;
    private PrintWriter out;
    private Map<Integer, ClientPanel> clientFormsMap;
    private Map<Integer, Boolean> isOpen;

    public MyProcess(Socket socket, int clientNumber, JPanel serverPanel, Map<Integer, ClientPanel> clientFormsMap, Map<Integer, Boolean> isOpen) {
        this.socket = socket;
        this.clientNumber = clientNumber;
        this.serverPanel = serverPanel;
        this.clientFormsMap = clientFormsMap;  
        this.isOpen = isOpen;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            StringBuilder machineList = new StringBuilder();
            for (int machineNumber : clientFormsMap.keySet()) {
            	if(!isOpen.get(machineNumber))
            		machineList.append(machineNumber).append(",");  
            }
            
            out.println(machineList.toString());  
            out.flush();
            
            String clientInfo = in.readLine(); 
            String[] parts = clientInfo.split(":");
            String selectedMachine = parts[0];
            String clientName = parts[1];

            ClientPanel clientForm = clientFormsMap.get(Integer.parseInt(selectedMachine));

            if (clientForm != null) {
                clientForm.updateClientName(clientName);
                isOpen.put(Integer.parseInt(selectedMachine), true);
                serverPanel.revalidate();
                serverPanel.repaint();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}