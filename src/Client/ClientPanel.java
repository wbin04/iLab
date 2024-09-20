package Client;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JLabel;

public class ClientPanel {

    private JPanel clientPanel;
    private JLabel lbNum; 
    private JLabel lbName;

    public ClientPanel(int i) {
        initialize(i);
    }

    /**
     * Initialize the contents of the client panel.
     */
    private void initialize(int clientNumber) {
        clientPanel = new JPanel();
        clientPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
        clientPanel.setBounds(28, 10, 250, 150);
        clientPanel.setLayout(null);
        clientPanel.setName("Client" + clientNumber);

        JButton btnView = new JButton("View");
        btnView.setBounds(10, 119, 85, 21);
        clientPanel.add(btnView);

        JButton btnClose = new JButton("Close");
        btnClose.setBounds(149, 119, 85, 21);
        clientPanel.add(btnClose);

        lbNum = new JLabel("May so " + clientNumber);
        lbNum.setBounds(10, 10, 69, 13);
        clientPanel.add(lbNum);

        lbName = new JLabel("NVA");
        lbName.setBounds(10, 56, 69, 13);
        clientPanel.add(lbName);

        JLabel lbIP = new JLabel("ip");
        lbIP.setBounds(10, 33, 69, 13);
        clientPanel.add(lbIP);
    }

    public JPanel getClientPanel() {
        return this.clientPanel;
    }
    
    public void updateClientName(String clientName) {
        lbName.setText(clientName); 
    }
}