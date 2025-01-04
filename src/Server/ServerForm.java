package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import Client.ClientFileSender;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ServerForm extends Application {
	@FXML
    private TextField tfClassName;
    @FXML
    private TextField tfClassCode;
    @FXML
    private TextField tfPort;
    @FXML
    private TextField tfNum;
    @FXML
    private TextField tfConnected;
    @FXML
    private TextField tfEmpty;
    @FXML
    private Button btnOpen;
    @FXML
    private Button btnClose;
    @FXML
    private ScrollPane chatPane;
    @FXML
    private TextFlow chatArea;
    @FXML
    private FlowPane flowPane_chat;
    @FXML
    private TextField chatField;
    @FXML
    private Button btnSend;
    @FXML
    private ScrollPane panelPane;
    @FXML
    private FlowPane clientContainer;
    @FXML
    private ComboBox<String> comboBox;
    
    private String preSelectedOpion = "Mặc định";
    
    private Map<Integer, ServerClientPanel> clientFormsMap;
    private Map<Integer, Boolean> clientConnected;
    
    private List<Socket> listSocket = new ArrayList<>();
    private List<Socket> listSocketChat = new ArrayList<>();
    private List<Socket> listSocketImage = new ArrayList<>();
    private List<Socket> listSocketRemote = new ArrayList<>();
    private List<Socket> listSocketMouse = new ArrayList<>();
    private List<Socket> listSocketKeyboard = new ArrayList<>();
    private List<Socket> listSocketFile = new ArrayList<>();
    private List<Socket> listSocketTaskManager = new ArrayList<>();
    private List<Socket> listSocketStream = new ArrayList<>();
    private List<Socket> listSocketBlockDomain = new ArrayList<>();
    private List<Socket> listSocketCamera = new ArrayList<>();
    
    private ServerSocket serverSocketInit;
    private ServerSocket serverSocket;
    private ServerSocket serverSocketChat;
    private ServerSocket serverSocketImage;
    private ServerSocket serverSocketRemote;
    private ServerSocket serverSocketMouse;
    private ServerSocket serverSocketKeyboard;
    private ServerSocket serverSocketFile;
    private ServerSocket serverSocketTaskManager;
    private ServerSocket serverSocketStream;
    private ServerSocket serverSocketBlockDomain;
    private ServerSocket serverSocketCamera;
    
    private Socket lookupSocket;
    
    private ServerForm controller;
    
    private boolean isRunning;
    private int port;
    private final Object lock = new Object();

    public static void main(String[] args) {
    	launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ServerForm.fxml"));
            Parent root = loader.load();
            primaryStage.setTitle("ServerForm");
            Scene scene = new Scene(root, 1200, 720);
            scene.getStylesheets().clear();
//            scene.getStylesheets().add(getClass().getResource("ServerForm.css").toExternalForm());
            primaryStage.setMaximized(true);
            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(event -> {
                closeServer(); 
                System.exit(0); 
            });
//            primaryStage.setResizable(false);
            primaryStage.show();

            controller = loader.getController();
            controller.setEvents(primaryStage);
            controller.setStatus(true);
        } catch (IOException e) {
        	e.printStackTrace();
        }
    }

    private void setEvents(Stage stage) {
    	comboBox.getItems().addAll("Mặc định", "Xem màn hình", "Xem camera", "Truyền file");
    	comboBox.getSelectionModel().selectFirst();
    	comboBox.setOnAction(event -> {
    	    String selectedOption = comboBox.getValue(); 
    	    
    	    for (int n : clientFormsMap.keySet()) {
    	        if (clientConnected.get(n)) {
    	            ServerClientPanel panel = clientFormsMap.get(n);
    	            switch (selectedOption) {
    	                case "Xem màn hình":
    	                	panel.stopCameraView(); 
    	                    panel.startStreamView(); 
    	                    break;
    	                case "Xem camera":
    	                	panel.stopStreamView(); 
    	                    panel.startCameraView();  
    	                    break;
    	                case "Truyền file":
    	                	panel.stopStreamView();
    	                	panel.stopCameraView();
    	                    sendFile();  
    	                	comboBox.getSelectionModel().select(preSelectedOpion);
    	                    break;
    	                default:
    	                	panel.stopStreamView();
    	                	panel.stopCameraView();
    	                    break;
    	            }
    	        }
    	    }

    	    preSelectedOpion = selectedOption;
    	});
    	
    	chatArea.heightProperty().addListener((observable, oldValue, newValue) -> {
    	    chatPane.setVvalue(1.0); 
    	});

    	btnSend.setOnAction(event -> sendMessage());
        chatField.setOnAction(event -> sendMessage());
//        btnFile.setOnAction(event -> sendFile());
        
        btnOpen.setOnAction(event -> {
        	try {
            	port = Integer.parseInt(tfPort.getText());
        		setServerCode();
        		

            	stage.setOnCloseRequest(event2 -> {
            		event2.consume(); 
            	});
    		} catch (Exception e) {
    			e.printStackTrace();
    		}   	
        });
        btnClose.setOnAction(event -> {
        	closeServer();
        	setStatus(true);
        	stage.setOnCloseRequest(event2 -> {
        		System.exit(0);
        	});
        });
    }
    
    private void setServerCode() {
//        int serverPort = port-1;
        int lookupServerPort = 1024;

        new Thread(() -> {
            LookupServer lookupServer = new LookupServer();
            lookupServer.init(lookupServerPort);  
        }).start();

        new Thread(() -> {
            try {
            	String className = tfClassName.getText();
                String code = UUID.randomUUID().toString().substring(0, 6);
//                System.out.println("Mã kết nối: " + code);


                String lookupServerIP = InetAddress.getLocalHost().getHostAddress();  
                lookupSocket = new Socket(lookupServerIP, lookupServerPort);
                DataOutputStream dos = new DataOutputStream(lookupSocket.getOutputStream());
                DataInputStream dis = new DataInputStream(lookupSocket.getInputStream());

                dos.writeUTF("REGISTER");
                dos.writeUTF(code);
                dos.writeUTF(InetAddress.getLocalHost().getHostAddress() + ":" + tfPort.getText() + ":" + className);

                if (dis.readUTF().equals("REGISTERED")) {
                    System.out.println("Đăng ký thành công trên lookup server.");

                    Platform.runLater(() -> {
                        isRunning = true;
                        chatArea.getChildren().clear();  
                        loadPanel();
                        startServerInBackground();
                        setStatus(false);
                        tfClassCode.setText(code);
                    });
                }
                lookupSocket.close();

//                serverSocketInit = new ServerSocket(serverPort);
//                while (true) {
//                    Socket clientSocket = serverSocketInit.accept();
//                    System.out.println("Client kết nối từ: " + clientSocket.getInetAddress() + clientSocket.getPort());
//                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    
    private void startServerInBackground() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                serverSocketChat = new ServerSocket(port+1);
                serverSocketImage = new ServerSocket(port+2);
                serverSocketRemote = new ServerSocket(port+3);
                serverSocketMouse = new ServerSocket(port+4);
                serverSocketKeyboard = new ServerSocket(port+5);
                serverSocketFile = new ServerSocket(port+6);
                serverSocketTaskManager = new ServerSocket(port+7);
                serverSocketStream = new ServerSocket(port+8);
                serverSocketBlockDomain = new ServerSocket(port+9);
                serverSocketCamera = new ServerSocket(port+10);

                Platform.runLater(() -> {
					try {
//						chatArea.getChildren().add(new Text("Server tại địa chỉ " + InetAddress.getLocalHost().getHostAddress() + " cổng " + port + " đang chờ kết nối...\n"));
						appendText("Server tại địa chỉ " + InetAddress.getLocalHost().getHostAddress() + " cổng " + port + " đang chờ kết nối...\n", false, false);
						chatPane.setVvalue(1.0);
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
				});
                while (isRunning) {
                	try {
                        Socket soc = serverSocket.accept();
                        DataOutputStream dos = new DataOutputStream(soc.getOutputStream());
                        DataInputStream dis = new DataInputStream(soc.getInputStream());

                        String clientSignal = dis.readUTF();
                        if (clientSignal.equals("CONNECT_TO_SERVER")) {
                            synchronized (lock) {
                                StringBuilder listMachines = new StringBuilder();
                                for (int n : clientConnected.keySet()) {
                                    if (!clientConnected.get(n)) {
                                        listMachines.append(n).append(",");
                                    }
                                }

                                if (listMachines.length() == 0) {
                                    dos.writeUTF("NO_AVAILABLE_MACHINES");
                                } else {
                                    listMachines.setLength(listMachines.length() - 1);
                                    dos.writeUTF(listMachines.toString());
                                }
                            }
                        }

                        String msg = dis.readUTF(); //JOIN_MACHINE: stt:name
                        if (msg.startsWith("JOIN_MACHINE")) {
                            String[] parts = msg.split(":")[1].split(",");
                            int selectedMachine = Integer.parseInt(parts[0]); // Số thứ tự máy
                            String clientName = parts[1]; // Tên

                            synchronized (lock) {
                                if (clientConnected.get(selectedMachine)) {
                                    dos.writeUTF("MACHINE_UNAVAILABLE");
                                } else {
                                    clientConnected.put(selectedMachine, true);
                                    dos.writeUTF("MACHINE_CONFIRMED");

                                    Socket socChat = serverSocketChat.accept();
                                    Socket socImg = serverSocketImage.accept();
                                    Socket socRemote = serverSocketRemote.accept();
                                    Socket socMouse = serverSocketMouse.accept();
                                    Socket socKeyboard = serverSocketKeyboard.accept();
                                    Socket socFile = serverSocketFile.accept();
                                    Socket socTM = serverSocketTaskManager.accept();
                                    Socket socStream = serverSocketStream.accept();
                                    Socket socBD = serverSocketBlockDomain.accept();
                                    Socket socCam = serverSocketCamera.accept();

                                    listSocket.add(soc);
                                    listSocketChat.add(socChat);
                                    listSocketImage.add(socImg);
                                    listSocketRemote.add(socRemote);
                                    listSocketMouse.add(socMouse);
                                    listSocketKeyboard.add(socKeyboard);
                                    listSocketFile.add(socFile);
                                    listSocketTaskManager.add(socTM);
                                    listSocketStream.add(socStream);
                                    listSocketBlockDomain.add(socBD);
                                    listSocketCamera.add(socCam);
                        			

                                    final Socket finalSoc = soc;
                                    final Socket finalSocChat = socChat;
                                    final Socket finalSocImg = socImg;
                                    final Socket finalSocRemote = socRemote;
                                    final Socket finalSocMouse = socMouse;
                                    final Socket finalSocKeyboard = socKeyboard;
                                    final Socket finalSocFile = socFile;
                                    final Socket finalSocTM = socTM;
                                    final Socket finalSocStream = socStream;
                                    final Socket finalSocBD = socBD;
                                    final Socket finalSocCam = socCam;
                                    javafx.application.Platform.runLater(() -> {
                                        refreshServerForm(finalSoc, selectedMachine + ":" + clientName, finalSocChat, finalSocImg, finalSocRemote, finalSocMouse, finalSocKeyboard, finalSocFile, finalSocTM, finalSocStream, finalSocBD, finalSocCam);
                                    });
                                }
                            }
                        }else if(msg.equals("DISCONNECTED")) {
            				dis.close();
            		        dos.close();
            		        soc.close();
            		        continue;
            			}
                        
                    } catch (SocketException e) {
                        if (!isRunning) {
                            System.out.println("Server không còn chấp nhận kết nối.");
                            appendText("Server không còn chấp nhận kết nối.\n", false, false);
                        } else {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
//                System.out.println("ServerForm lỗi startServerInBackground: " + e.getMessage());
            	e.printStackTrace();
            } finally {
//                closeServer();
            }
        }).start();
    }

    private void loadPanel() {
        clientContainer.getChildren().clear(); 
        clientFormsMap = new HashMap<>();
        clientConnected = new HashMap<>();
        
        int count = Integer.parseInt(tfNum.getText());
        
        for (int i = 1; i <= count; i++) {
        	
        	ServerClientPanel clientPanel = new ServerClientPanel();
        	clientContainer.getChildren().add(clientPanel.getPanel(i));
        	
        	clientFormsMap.put(i, clientPanel);
        	clientConnected.put(i, false);
        }
        
        tfConnected.setText("0");
		tfEmpty.setText("" + count);
    }
    
    private void refreshServerForm(Socket soc, String msg, Socket socketChat, Socket socketImg, Socket socketRemote, Socket socketMouse, Socket socketKeyboard, Socket socketFile, Socket socketTM, Socket socketStream, Socket socketBD, Socket socketCam) {
    	String[] parts = msg.split(":");
        int stt = Integer.parseInt(parts[0].trim()); // Lấy số thứ tự máy
        String name = parts[1].trim();

		ServerClientPanel clientPanel = clientFormsMap.get(stt); // tìm ra được clientPanel, cấp cho nó 1 socket, ban đầu khởi tạo bằng NULL
		clientPanel.setStartTime(System.currentTimeMillis());
		
		clientPanel.setName(name);
		clientPanel.setStatus(true);
		clientPanel.setSocketChat(socketChat);
		clientPanel.setSocketRemote(socketImg, socketRemote, socketMouse, socketKeyboard, socketFile, socketTM, socketStream, socketBD, socketCam);
		clientPanel.setEvents();
		
		tfConnected.setText("" + listSocket.size());
		tfEmpty.setText("" + (10-listSocket.size()));
		appendText(name + " ở máy số " + stt + " mới vừa kết nối vào server", false, false);
		System.out.println(name + " ở máy số " + stt + " mới vừa kết nối vào server\n");
        
        clientConnected.put(stt, true);
        

		removeClientPanel(soc, socketChat, socketImg, socketRemote, socketMouse, socketKeyboard, socketFile, socketTM, socketStream, socketBD, socketCam, stt, name);
    }
    
    private void removeClientPanel(Socket socket, Socket socketChat, Socket socketImg, Socket socketRemote, Socket socketMouse, Socket socketKeyBoard, Socket socketFile, Socket socketTM, Socket socketStream, Socket socketBD, Socket socketCam, int stt, String name) {
    	new Thread(() -> {
            try {
            	DataInputStream dis = new DataInputStream(socket.getInputStream());
            	while (isRunning) {
                    if (dis.available() > 0) {
//                    	System.out.println("true");
                    }
                    else {
//                    	System.out.println("false");

                		
                		appendText(name + " ở máy số " + stt + " đã ngắt kết nối", false, false);
                    	listSocket.remove(socket);
                		tfConnected.setText("" + listSocket.size());
                		tfEmpty.setText("" + (10-listSocket.size()));
                    	
                    	listSocketChat.remove(socketChat);
                    	listSocketImage.remove(socketImg);
                    	listSocketRemote.remove(socketRemote);
                    	listSocketFile.remove(socketFile);
                    	listSocketTaskManager.remove(socketTM);
                    	listSocketStream.remove(socketStream);
                    	listSocketBlockDomain.remove(socketBD);
                    	
                    	socket.close();
                    	socketChat.close();
                    	socketRemote.close();
                    	socketFile.close();
                    	socketTM.close();
                    	socketStream.close();
                    	socketBD.close();
                    	
                    	clientConnected.put(stt, false);
                    	ServerClientPanel clientPanel = clientFormsMap.get(stt); // tìm ra được clientPanel, cấp cho nó 1 socket, ban đầu khởi tạo bằng NULL
                		clientPanel.stopTime();
                		
//                		clientPanel.setName("");
//                		clientPanel.setStatus(false);
                		clientPanel.setSocketChat(null);
                		clientPanel.setSocketRemote(null, null, null, null, null, null, null, null, null);
                		clientPanel.setEvents();
                		
                    	break;
                    }
                    Thread.sleep(1000); 
                } 
            } catch (Exception e) {
//              e.printStackTrace();
            	System.out.println("Client ngat ket noi");
            	ServerClientPanel panel = clientFormsMap.get(stt);
            	panel.stopStreamView();
            }
    	}).start();
    }
    
    private void setStatus(boolean status) {
    	tfClassName.setEditable(status);
    	tfPort.setEditable(status);
    	tfNum.setEditable(status);
    	
    	tfClassCode.setDisable(status);
    	tfConnected.setDisable(status);
    	tfEmpty.setDisable(status);
    	
    	btnOpen.setDisable(!status);
    	btnClose.setDisable(status);
    	
    	chatPane.setDisable(status);
    	panelPane.setDisable(status);
    	comboBox.setDisable(status);
    	btnSend.setDisable(status);
    	chatField.setDisable(status);
    	
    	panelPane.setDisable(status);
    }
    
    private void appendText(String msg, boolean isChat, boolean isFile) {
        Platform.runLater(() -> {
            TextFlow textFlow = new TextFlow();
            textFlow.setMaxWidth(600);  

            Label label = new Label();
            label.setPadding(new Insets(10, 20, 10, 20));
            label.setWrapText(true);  
            label.setMaxWidth(550);  
            label.setText(msg);

            FlowPane flowPane = new FlowPane();
            flowPane.setPrefWidth(600);

            Region spacer = new Region();
            spacer.setPrefHeight(50);
            
            if (isChat) {
                label.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-background-color: #102a43; -fx-background-radius: 10;");
                textFlow.getChildren().add(label);
                flowPane.getChildren().add(textFlow);  
                flowPane.getChildren().add(spacer);
                flowPane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);  
            } 
            else if(isFile) {
            	Text prefixText = new Text("Bạn đã gửi file: ");
                prefixText.setStyle("-fx-font-size: 18px;"); 
                prefixText.setFill(Color.WHITE);

                Text fileText = new Text(msg);
                fileText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;"); 
                fileText.setFill(Color.YELLOW);

                textFlow.getChildren().addAll(prefixText, fileText);
                textFlow.setStyle("-fx-padding: 10px 20px 10px 20px; -fx-background-color: #102a43; -fx-background-radius: 10;");
                flowPane.getChildren().add(textFlow);
                flowPane.getChildren().add(spacer);
                flowPane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
//                flowPane.setAlignment(Pos.CENTER);
            }
            else {
                label.setStyle("-fx-background-color: white; -fx-text-fill: #102a43; -fx-background-radius: 10;");
                textFlow.getChildren().add(label);
                flowPane.getChildren().add(textFlow);
                flowPane.getChildren().add(spacer);
                flowPane.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);  
            }
            
            chatArea.getChildren().add(flowPane);
            chatPane.setVvalue(1.0);  
        });
    }
    
    private void sendMessage() {
        String msg = chatField.getText();
        if (!msg.equals("")) {
            for (Socket socket : listSocketChat) {
                try {
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    dos.writeUTF(msg);
                    dos.flush();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    System.out.println("Lỗi gửi tin nhắn tổng ServerForm");
//                    chatArea.appendText("Lỗi gửi tin nhắn tổng ServerForm\n");
                }
            }

            chatField.setText("");
            appendText(msg, true, false);
        }
    }
    
    private void sendFile() {
    	Stage stage = (Stage) comboBox.getScene().getWindow(); 
		FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
    	for(int i=0; i<listSocketFile.size(); i++) {
            if (file != null) {
                new Thread(new ClientFileSender(listSocketChat.get(i), listSocketFile.get(i), file)).start();
            }
    	}
    	if (file != null) appendText(file.getName(), false, true);
    }
    
    private void closeServer() {
        try {
            isRunning = false;
            
            if (lookupSocket != null && !lookupSocket.isClosed()) {
            	lookupSocket.close();
            }
            
            if (serverSocketInit != null && !serverSocketInit.isClosed()) {
            	serverSocketInit.close();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (serverSocketChat != null && !serverSocketChat.isClosed()) {
                serverSocketChat.close();
            }
            if (serverSocketImage != null && !serverSocketImage.isClosed()) {
                serverSocketImage.close();
            }
            if (serverSocketRemote != null && !serverSocketRemote.isClosed()) {
                serverSocketRemote.close();
            }
            if (serverSocketMouse != null && !serverSocketMouse.isClosed()) {
            	serverSocketMouse.close();
            }
            if (serverSocketKeyboard != null && !serverSocketKeyboard.isClosed()) {
            	serverSocketKeyboard.close();
            }
            if (serverSocketFile != null && !serverSocketFile.isClosed()) {
            	serverSocketFile.close();
            }
            if (serverSocketTaskManager != null && !serverSocketTaskManager.isClosed()) {
            	serverSocketTaskManager.close();
            }
            if (serverSocketStream != null && !serverSocketStream.isClosed()) {
            	serverSocketStream.close();
            }
            if (serverSocketBlockDomain != null && !serverSocketBlockDomain.isClosed()) {
            	serverSocketBlockDomain.close();
            }
            if (serverSocketCamera != null && !serverSocketCamera.isClosed()) {
            	serverSocketCamera.close();
            }

            for (Socket socket : listSocket) {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            }
            for (Socket socketChat : listSocketChat) {
                if (socketChat != null && !socketChat.isClosed()) {
                	try {
                        DataOutputStream dos = new DataOutputStream(socketChat.getOutputStream());
                        dos.writeUTF("SERVER_CLOSED");
                        dos.flush();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        System.out.println("Lỗi đóng socketChat ServerForm");
                        appendText("Lỗi đóng socketChat ServerForm\n", false, false);
                    }
                }
            }
            for (Socket socketImg : listSocketImage) {
                if (socketImg != null && !socketImg.isClosed()) {
                	try {
                        DataOutputStream dos = new DataOutputStream(socketImg.getOutputStream());
                        dos.writeUTF("SERVER_CLOSED");
                        dos.flush();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        System.out.println("Lỗi đóng socketChat ServerForm");
                        appendText("Lỗi đóng socketChat ServerForm\n", false, false);
                    }
                }
            }
            for (Socket socketRemote : listSocketRemote) {
                if (socketRemote != null && !socketRemote.isClosed()) {
                	try {
                        DataOutputStream dos = new DataOutputStream(socketRemote.getOutputStream());
                        dos.writeUTF("SERVER_CLOSED");
                        dos.flush();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        System.out.println("Lỗi đóng socketRemote ServerForm");
                        appendText("Lỗi đóng socketRemote ServerForm\n", false, false);
                    }
                }
            }
            for (Socket socketMouse : listSocketMouse) {
                if (socketMouse != null && !socketMouse.isClosed()) {
                	try {
                        DataOutputStream dos = new DataOutputStream(socketMouse.getOutputStream());
                        dos.writeUTF("SERVER_CLOSED");
                        dos.flush();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        System.out.println("Lỗi đóng socketRemote ServerForm");
                        appendText("Lỗi đóng socketRemote ServerForm\n", false, false);
                    }
                }
            }
            for (Socket socketKeyboard : listSocketKeyboard) {
                if (socketKeyboard != null && !socketKeyboard.isClosed()) {
                	try {
                        DataOutputStream dos = new DataOutputStream(socketKeyboard.getOutputStream());
                        dos.writeUTF("SERVER_CLOSED");
                        dos.flush();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        System.out.println("Lỗi đóng socketRemote ServerForm");
                        appendText("Lỗi đóng socketRemote ServerForm\n", false, false);
                    }
                }
            }
            for (Socket socketFile : listSocketFile) {
                if (socketFile != null && !socketFile.isClosed()) {
                	try {
                        DataOutputStream dos = new DataOutputStream(socketFile.getOutputStream());
                        dos.writeUTF("SERVER_CLOSED");
                        dos.flush();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        System.out.println("Lỗi đóng socketRemote ServerForm");
                        appendText("Lỗi đóng socketFile ServerForm\n", false, false);
                    }
                }
            }
            for (Socket socketTM : listSocketTaskManager) {
                if (socketTM != null && !socketTM.isClosed()) {
                	try {
                        DataOutputStream dos = new DataOutputStream(socketTM.getOutputStream());
                        dos.writeUTF("SERVER_CLOSED");
                        dos.flush();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        System.out.println("Lỗi đóng socketRemote ServerForm");
                        appendText("Lỗi đóng socketFile ServerForm\n", false, false);
                    }
                }
            }
            
            for (Socket socketStream : listSocketStream) {
                if (socketStream != null && !socketStream.isClosed()) {
                	try {
                        DataOutputStream dos = new DataOutputStream(socketStream.getOutputStream());
                        dos.writeUTF("SERVER_CLOSED");
                        dos.flush();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        System.out.println("Lỗi đóng socketRemote ServerForm");
                        appendText("Lỗi đóng socketFile ServerForm\n", false, false);
                    }
                }
            }
            for (Socket socketBlockDomain : listSocketBlockDomain) {
                if (socketBlockDomain != null && !socketBlockDomain.isClosed()) {
                	try {
                        DataOutputStream dos = new DataOutputStream(socketBlockDomain.getOutputStream());
                        dos.writeUTF("SERVER_CLOSED");
                        dos.flush();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        System.out.println("Lỗi đóng socketRemote ServerForm");
                        appendText("Lỗi đóng socketFile ServerForm\n", false, false);
                    }
                }
            }
            for (Socket socketCam : listSocketCamera) {
                if (socketCam != null && !socketCam.isClosed()) {
                	try {
                        DataOutputStream dos = new DataOutputStream(socketCam.getOutputStream());
                        dos.writeUTF("SERVER_CLOSED");
                        dos.flush();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        System.out.println("Lỗi đóng socketRemote ServerForm");
                        appendText("Lỗi đóng socketFile ServerForm\n", false, false);
                    }
                }
            }

            javafx.application.Platform.runLater(() -> {
            	appendText("Server đã được đóng.\n", false, false);
                listSocket.clear();
                listSocketChat.clear();
                listSocketImage.clear();
                listSocketRemote.clear();
                listSocketMouse.clear();
                listSocketKeyboard.clear();
                listSocketTaskManager.clear();
                listSocketStream.clear();
                listSocketBlockDomain.clear();
                listSocketCamera.clear();
                tfConnected.setText("0");
                tfEmpty.setText("0");
                clientContainer.getChildren().clear();
            });
        } catch (IOException e) {
            e.printStackTrace();
            appendText("Lỗi khi đóng server: " + e.getMessage() + "\n", false, false);
        }
    }
    	
}
