package Client;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

public class ClientFileSender implements Runnable{
	File file;
	Socket socketChat;
	DataOutputStream dosChat;
	Socket socketFile;
	DataOutputStream dosFile;
	public ClientFileSender(Socket socketChat, Socket socketFile, File file) {
		try {
			this.file = file;
			this.socketChat = socketChat;
			this.dosChat = new DataOutputStream(socketChat.getOutputStream());
			this.socketFile = socketFile;
			this.dosFile = new DataOutputStream(socketFile.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			FileInputStream fileIn = new FileInputStream(file);
			dosFile.writeUTF(file.getName());
			dosFile.writeLong(file.length());
			System.out.println("Đang gửi file: " + file.getName());
			
			byte[] buffer = new byte[8192];
			int bytesRead;
			while((bytesRead = fileIn.read(buffer)) != -1) {
				dosFile.write(buffer, 0, bytesRead);
			}
			dosFile.flush();
			fileIn.close();
			dosChat.writeUTF("FILE:" + file.getName());
			System.out.println("File đã được gửi thành công.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
