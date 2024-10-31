package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class ServerForm extends Application {
    @FXML
    private FlowPane clientContainer; 
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
    private TextArea chatArea;
    @FXML
    private TextField chatField;
    @FXML
    private Button btnSend;
    
    private Map<Integer, ServerClientPanel> clientFormsMap;private List<Socket> listSocket = new ArrayList<>();
    private List<Socket> listSocketChat = new ArrayList<>();
    private List<Socket> listSocketRemote = new ArrayList<>();
    private List<Socket> listSocketFile = new ArrayList<>();
    private ServerSocket serverSocket;
    private ServerSocket serverSocketChat;
    private ServerSocket serverSocketRemote;
    private ServerSocket serverSocketFile;
    private ServerForm controller;
    
    private boolean isRunning;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ServerForm.fxml"));
            Parent root = loader.load();
            primaryStage.setTitle("ServerForm");
            primaryStage.setScene(new Scene(root, 1000, 600));
            primaryStage.setOnCloseRequest(event -> {
                closeServer(); 
                System.exit(0); 
            });
            primaryStage.show();

            controller = loader.getController();
            controller.setEvents();
        } catch (IOException e) {
        	e.printStackTrace();
        }
    }

    private void setEvents() {
    	btnSend.setOnAction(event -> sendMessage());
        chatField.setOnAction(event -> sendMessage());
        
        btnOpen.setOnAction(event -> {
        	try {
        		isRunning = true;
        		chatArea.setText("");
            	loadPanel();
            	startServerInBackground();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}   	
        });
        btnClose.setOnAction(event -> {
        	closeServer();
        });
    }
    
    private void startServerInBackground() {
        new Thread(() -> {
            try {
            	int port = Integer.parseInt(tfPort.getText());
                serverSocket = new ServerSocket(port);
                serverSocketChat = new ServerSocket(port+1);
                serverSocketRemote = new ServerSocket(port+2);
                serverSocketFile = new ServerSocket(port+3);

                chatArea.appendText("Server đang chờ kết nối...\n");
                while (isRunning) {
                	try {
                        Socket soc = serverSocket.accept();
                        Socket socChat = serverSocketChat.accept();
                        Socket socRemote = serverSocketRemote.accept();
                        Socket socFile = serverSocketFile.accept();

                        listSocket.add(soc);
                        listSocketChat.add(socChat);
                        listSocketRemote.add(socRemote);
                        listSocketFile.add(socFile);

                        // Cập nhật UI phải thực hiện trong luồng JavaFX
                        final Socket finalSoc = soc;
                        final Socket finalSocChat = socChat;
                        final Socket finalSocRemote = socRemote;
                        final Socket finalSocFile = socFile;
                        javafx.application.Platform.runLater(() -> {
                            refreshServerForm(finalSoc, finalSocChat, finalSocRemote, finalSocFile);
                        });
                    } catch (SocketException e) {
                        if (!isRunning) {
                            System.out.println("Server không còn chấp nhận kết nối.");
                            chatArea.appendText("Server không còn chấp nhận kết nối.\n");
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

    private void loadPanel() throws IOException {
        clientContainer.getChildren().clear(); 
        clientFormsMap = new HashMap<>();
        
        for (int i = 1; i <= 10; i++) {
        	
        	ServerClientPanel clientPanel = new ServerClientPanel();
        	clientContainer.getChildren().add(clientPanel.getPanel(i));
        	
        	clientFormsMap.put(i, clientPanel);
        }
        
        tfConnected.setText("0");
		tfEmpty.setText("10");
    }
    
    private void refreshServerForm(Socket soc, Socket socketChat, Socket socketRemote, Socket socketFile) {
    	DataInputStream dis;
		try {
			dis = new DataInputStream(soc.getInputStream());
			String msg = dis.readUTF();
//			System.out.println("Server: " + msg);
			String[] parts = msg.split(",");
	        int stt = Integer.parseInt(parts[0]);
	        String name = parts[1];
	        
			ServerClientPanel clientPanel = clientFormsMap.get(stt); // tìm ra được clientPanel, cấp cho nó 1 socket, ban đầu khởi tạo bằng NULL
			clientPanel.setIP("IP: "+soc.getInetAddress().getLocalHost().getHostAddress());
			clientPanel.setName("Họ tên: " + name);
//			clientPanel.setStatus();
			clientPanel.setSocket(soc);
			clientPanel.setSocketChat(socketChat);
			clientPanel.setSocketRemote(socketRemote, socketFile);
			clientPanel.setEvents();
			
			tfConnected.setText("" + listSocket.size());
			tfEmpty.setText("" + (10-listSocket.size()));
			chatArea.appendText(name + " ở máy số " + stt + " mới vừa kết nối vào server\n");
			System.out.println(name + " ở máy số " + stt + " mới vừa kết nối vào server\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void sendMessage() {
        String msg = chatField.getText();
        if (!msg.equals("")) {
            for (Socket socket : listSocketChat) {
                try {
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    dos.writeUTF(msg);
                    dos.flush();

                    chatField.setText("");
                    chatArea.appendText("Server: " + msg + "\n");
                } catch (IOException e1) {
                    e1.printStackTrace();
                    System.out.println("Lỗi gửi tin nhắn tổng ServerForm");
                    chatArea.appendText("Lỗi gửi tin nhắn tổng ServerForm\n");
                }
            }
        }
    }
    
    private void closeServer() {
        try {
            isRunning = false;
            
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (serverSocketChat != null && !serverSocketChat.isClosed()) {
                serverSocketChat.close();
            }
            if (serverSocketRemote != null && !serverSocketRemote.isClosed()) {
                serverSocketRemote.close();
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
                        chatArea.appendText("Lỗi đóng socketChat ServerForm\n");
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
                        chatArea.appendText("Lỗi đóng socketRemote ServerForm\n");
                    }
                }
            }

            javafx.application.Platform.runLater(() -> {
                chatArea.appendText("Server đã được đóng.\n");
                listSocket.clear();
                listSocketChat.clear();
                listSocketRemote.clear();
                tfConnected.setText("0");
                tfEmpty.setText("0");
                clientContainer.getChildren().clear();
            });
        } catch (IOException e) {
            e.printStackTrace();
            chatArea.appendText("Lỗi khi đóng server: " + e.getMessage() + "\n");
        }
    }
    	
}
