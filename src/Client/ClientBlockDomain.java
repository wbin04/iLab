package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ClientBlockDomain extends Stage{
	@FXML
	private TextField tfDomain;
	@FXML
	private Button btnBlock;
	@FXML
    private TableView<DomainInfo> tableView;
    @FXML
    private TableColumn<DomainInfo, String> nameColumn;
    @FXML
    private TableColumn<DomainInfo, String> ipColumn;
	@FXML
	private Button btnRemove;
	
	private ObservableList<DomainInfo> domainList;
	
	private Socket socketBD;
	private DataOutputStream dosBD;
	private DataInputStream disBD;
	
	public ClientBlockDomain(Socket socketBD) {
		try {
			this.socketBD = socketBD;
			this.dosBD = new DataOutputStream(this.socketBD.getOutputStream());
			this.disBD = new DataInputStream(this.socketBD.getInputStream());
			
			initializeUI();
			setEvents();
			initializeTable();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	private void initializeUI() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ClientBlockDomain.fxml"));
			loader.setController(this);
            Scene scene = new Scene(loader.load());
	        this.setTitle("Chặn tên miền");
	        this.setScene(scene);
	        this.setOnCloseRequest(event -> {
	        	this.hide();
	       	 	event.consume(); 
	        });
	        this.setResizable(false);
	        this.hide();;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void setEvents() {
		domainList = FXCollections.observableArrayList();
//		tableView.setItems(domainList);
		btnBlock.setOnAction(event -> {
			String domainName = tfDomain.getText();
			System.out.println(domainName);
			getIP(domainName);
			blockDomain();
		});
		btnRemove.setOnAction(event -> {
			selectRemoveDomain();
		});
	}
	
	public void getIP(String domain) {
        try {
            InetAddress[] addresses = InetAddress.getAllByName(domain);
            String[] ips = new String[addresses.length];
            for (int i = 0; i < addresses.length; i++) {
                ips[i] = addresses[i].getHostAddress();
                domainList.add(new DomainInfo(ips[i], domain));
            }
//            tableView.refresh();
//            return ips;
        } catch (Exception e) {
            e.printStackTrace();
//            return new String[0];
        }
    }
    
    public void blockDomain() {
    	for (DomainInfo domainInfo : domainList) {  
    		String ip = domainInfo.getIp();
    		String name = domainInfo.getName();
            String adminCmd = "cmd.exe /c powershell -Command \"Start-Process powershell -ArgumentList '-Command \"New-NetFirewallRule -DisplayName ''Block IP " + name + "'' -Direction Outbound -Action Block -RemoteAddress " + ip + "\"' -Verb RunAs\"";

            System.out.println("Executing command: " + adminCmd);

            try {
//                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", adminCmd);
//                Process process = pb.start();
//                int exitCode = process.waitFor();
//
//                if (exitCode == 0) {
//                    System.out.println("Successfully blocked IP: " + ip);
//                } else {
//                    System.out.println("Failed to block IP: " + ip + " with exit code: " + exitCode);
//                }

            	this.dosBD.writeUTF("BLOCKED");
            	this.dosBD.writeUTF(ip);
            	this.dosBD.writeUTF(name);
            	this.dosBD.flush();
            	
            	String result = disBD.readUTF();
            	if(result.equals("SUCCESSED")) {
            		System.out.println("BlockDomain thanh cong");
            	}
            	else {
            		System.out.println("BlockDomain that bai");
            	}
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void selectRemoveDomain() {
    	DomainInfo selectedDomain = tableView.getSelectionModel().getSelectedItem();

        if (selectedDomain != null) {
            String ip = selectedDomain.getIp();
            String name = selectedDomain.getName();
            System.out.println(ip + " " + name);
            String removeCmd = "cmd.exe /c powershell -Command \"Start-Process powershell -ArgumentList '-Command \"Remove-NetFirewallRule -DisplayName ''Block IP " + name + "''\"' -Verb RunAs\"";
            System.out.println("Executing command: " + removeCmd);

            try {
//                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", removeCmd);
//                Process process = pb.start();
//                int exitCode = process.waitFor();
//
//                if (exitCode == 0) {
//                    System.out.println("Successfully removed firewall rule for domain: " + name);
//                    domainList.remove(selectedDomain);
//                } else {
//                    System.out.println("Failed to remove firewall rule for domain: " + name + " with exit code: " + exitCode);
//                }
                
            	dosBD.writeUTF("REMOVED");
                dosBD.writeUTF(ip);
                dosBD.writeUTF(name);
                dosBD.flush();
                
                String result = disBD.readUTF();
            	if(result.equals("SUCCESSED")) {
            		System.out.println("RemoveDomain thanh cong");
            		domainList.remove(selectedDomain);
            	}
            	else {
            		System.out.println("RemoveDomain that bai");
            	}
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Hãy chọn 1 tên miền để đóng.");
        }
        
        for(DomainInfo domain : domainList) {
        	System.out.println(domain.getName());
        }
    }
    
    private void initializeTable() {
        ipColumn.setCellValueFactory(data -> data.getValue().ipProperty());
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());

        nameColumn.setResizable(false);
        ipColumn.setResizable(false);

        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        tableView.setItems(domainList);
    }
    
    public static class DomainInfo {
        private final StringProperty name;
        private final StringProperty ip;

        public DomainInfo(String ip, String name) {
            this.ip = new SimpleStringProperty(ip);
            this.name = new SimpleStringProperty(name);
        }

        public String getName() {
            return name.get();
        }

        public StringProperty nameProperty() {
            return name;
        }

        public String getIp() {
            return ip.get();
        }

        public StringProperty ipProperty() {
            return ip;
        }
    }
}
