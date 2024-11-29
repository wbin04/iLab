package Client;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientTaskManager extends Stage {
    @FXML
    private TableView<AppInfo> tableView;
    @FXML
    private TableColumn<AppInfo, String> nameColumn;
    @FXML
    private TableColumn<AppInfo, String> idColumn;
    @FXML
    private Button btnClose;

    private ObservableList<AppInfo> appList;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    
    private boolean isRunning = false;

    public ClientTaskManager(Socket socketTaskManager) {
        this.socket = socketTaskManager;
        appList = FXCollections.observableArrayList();
        isRunning = true;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ClientTaskManager.fxml"));
            loader.setController(this);
            Scene scene = new Scene(loader.load());
            setScene(scene);
            setTitle("Client Task Manager");

            initializeSockets();
            initializeTable();
            receiveRunningApps();
            
            this.setOnCloseRequest(event -> {
                this.hide(); 
                event.consume(); 
            });

            this.setResizable(false);
//            this.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Lỗi khởi tạo UI: " + e.getMessage());
        }
    }

    private void initializeSockets() {
        try {
            this.dis = new DataInputStream(this.socket.getInputStream());
            this.dos = new DataOutputStream(this.socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            showError("Lỗi khởi tạo socket.");
        }
    }

    private void initializeTable() {
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        idColumn.setCellValueFactory(data -> data.getValue().idProperty());

        nameColumn.setResizable(false);
        idColumn.setResizable(false);

        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        tableView.setItems(appList);

        btnClose.setOnAction(e -> killSelectedApp());
    }


    private void killSelectedApp() {
        AppInfo selectedApp = tableView.getSelectionModel().getSelectedItem();

        if (selectedApp != null) {
            String appId = selectedApp.getId();
            try {
//                dosRemote.writeUTF("KILL_APP");
                dos.writeUTF(appId);
                dos.flush();
                System.out.println("Kill request sent for app ID: " + appId);
            } catch (IOException e) {
//                e.printStackTrace();
            	closeConnection();
                showError("Lỗi gửi yêu cầu đóng ứng dụng.");
            }
        } else {
            showError("Hãy chọn 1 ứng dụng để đóng.");
        }
    }

    private void receiveRunningApps() {
        new Thread(() -> {
            try {
                while(isRunning) {
                	int appCount = dis.readInt();
                	
                    List<AppInfo> apps = new ArrayList<>();
                    for (int i = 0; i < appCount; i++) {
                        String appName = dis.readUTF();
                        String appId = dis.readUTF();
                        if (appName != null && !appName.isEmpty() && appId != null && !appId.isEmpty()) {
                            apps.add(new AppInfo(appName, appId));
                        }
                    }

                    Platform.runLater(() -> {
                        appList.clear();
                        appList.addAll(apps);
                    });
                }

            } catch (IOException e) {
//                e.printStackTrace();
//                Platform.runLater(() -> showError("Lỗi nhận danh sách ứng dụng."));
            	closeConnection();
            }
        }).start();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void closeConnection() {
    	try {
    		if(dis != null) dis.close();
    		if(dos != null) dos.close();
			if(socket != null) socket.close();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Loi closeConnection ClientTaskManager");
		}
    }

    public static class AppInfo {
        private final StringProperty name;
        private final StringProperty id;

        public AppInfo(String name, String id) {
            this.name = new SimpleStringProperty(name);
            this.id = new SimpleStringProperty(id);
        }

        public String getName() {
            return name.get();
        }

        public StringProperty nameProperty() {
            return name;
        }

        public String getId() {
            return id.get();
        }

        public StringProperty idProperty() {
            return id;
        }
    }
}
