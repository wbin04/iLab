package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ClientForm extends Application {
	@FXML
	private TextField tfClassCode;
	@FXML
	private TextField tfClassName;
	@FXML
	private TextField tfName;
	@FXML
	private ComboBox<String> cbbNum;
	@FXML
	private Button btnFind;
	@FXML
	private Button btnDisconnect;
	@FXML
	private Button btnJoin;
	
	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	
	private String ipAddress;
	private int port;
	
	private boolean isRunning;
	
	@Override
	public void start(Stage primaryStage) {
		 try {
			 FXMLLoader loader = new FXMLLoader(getClass().getResource("ClientForm.fxml"));
	         Parent root = loader.load();
	         primaryStage.setTitle("ClientForm");
	         primaryStage.setScene(new Scene(root));
	         primaryStage.setOnCloseRequest(event -> {
	        	 event.consume(); 
	         });
	         primaryStage.setResizable(false);
	         primaryStage.show();
	         
	         ClientForm controller = loader.getController();
	         controller.setEvents();
	         controller.setStatus(true);
		} catch (IOException e) {
			// TODO: handle exception
			System.out.println("ClientForm loi start: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	private void findServerByCode() {
	    int lookupServerPort = 1024;

	    new Thread(() -> {
	        try {
	            String localIP = InetAddress.getLocalHost().getHostAddress(); 
	            String[] octets = localIP.split("\\.");  
	            String temp = octets[0] + "." + octets[1] + ".";
	            boolean found = false;

	            for (int c = 0; c <= 255 && !found; c++) {  
	                for (int d = 1; d <= 254 && !found; d++) {  
	                    String ip = temp + c + "." + d;
	                    try (Socket lookupSocket = new Socket()) {  
	                    	lookupSocket.connect(new InetSocketAddress(ip, lookupServerPort), 50);
	                        DataOutputStream dosTemp = new DataOutputStream(lookupSocket.getOutputStream());
	                        DataInputStream disTemp = new DataInputStream(lookupSocket.getInputStream());

	                        String code = tfClassCode.getText();  

	                        dosTemp.writeUTF("LOOKUP");
	                        dosTemp.writeUTF(code);

	                        String serverInfo = disTemp.readUTF();
	                        System.out.println("Phản hồi từ server: " + serverInfo);

	                        if (serverInfo.equals("NOT_FOUND")) {
	                            continue;  
	                        }

	                        String[] parts = serverInfo.split(":");
	                        ipAddress = parts[0];
	                        port = Integer.parseInt(parts[1]);
	                        String name = parts[2];
	                        tfClassName.setText(name);

	                        socket = new Socket(ipAddress, port);
	                        System.out.println("Kết nối đến server thành công tại IP: " + ipAddress);

	                        found = true;  
	                        connectToServer();  
	                    } catch (IOException e) {
	                        System.out.println("Không kết nối được đến IP: " + ip);
	                    }
	                }
	            }

	            if (!found) {
	                Platform.runLater(() -> System.out.println("Không tìm thấy LookupServer nào."));
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }).start();  
	}

	private void connectToServer() {
	    try {
	        dis = new DataInputStream(socket.getInputStream());
	        dos = new DataOutputStream(socket.getOutputStream());

	        dos.writeUTF("CONNECT_TO_SERVER");
	        dos.flush();

	        String listMachines = dis.readUTF();
	        System.out.println(listMachines);
	        String[] machines = listMachines.split(",");

	        Platform.runLater(() -> {
	            cbbNum.getItems().clear();
	            for (String machine : machines) {
	                if (!machine.isEmpty()) {
	                    cbbNum.getItems().add(machine);
	                }
	            }
	            cbbNum.getSelectionModel().selectFirst();
	            
	            btnFind.setText("Tìm");
	            btnFind.setDisable(false);
	        	setStatus(false);
	        });
	    } catch (IOException e) {
	        e.printStackTrace();
	        Platform.runLater(() -> System.out.println("Lỗi kết nối đến server chính."));
	    }
	}


	
	private void setEvents() {    	
        btnFind.setOnAction(event -> {
        	if(isRunning) {
        		try {
					dos.writeUTF("DISCONNECTED");
					dos.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
    		btnFind.setText("Đang tìm");
    		btnFind.setDisable(true);
			setStatus(true);
    		findServerByCode();
    		
        });
        
        btnJoin.setOnAction(event -> {
        	setStatus(true);
    		String name = tfName.getText();
    		String stt = (String)cbbNum.getValue();
        	try {
				Stage stage = (Stage) btnJoin.getScene().getWindow();
				dos.writeUTF(stt + "," + name);
	            dos.flush();
	            
	            new ClientListenerMain(ipAddress, port, name, stt, stage);
//	            stage.hide();
				
	            isRunning = true;
//				dos.close();
//				dis.close();
	            
	            new Thread(() -> {
	            	while(isRunning) {
		            	try {
							dos.writeUTF("RUNNING");
			            	dos.flush();
			            	
			            	Thread.sleep(1000);
						} catch (Exception e) {
							// TODO Auto-generated catch block
//							e.printStackTrace();
							System.out.println("Server đã đóng kết nối");
							setStatus(true);
				        	isRunning = false;
						} 
		            }
	            }).start();
			} catch (Exception e2) {
				// TODO: handle exception
				System.out.println("Loi btnJoin ClientLoginForm");
				setStatus(true);
	        	isRunning = false;
			}
        });
    }
	
	private void setStatus(boolean status) {
		tfClassName.setDisable(status);
		tfName.setDisable(status);
		cbbNum.setDisable(status);
		btnJoin.setDisable(status);
	}
}
