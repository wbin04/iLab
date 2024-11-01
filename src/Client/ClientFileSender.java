package Client;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientFileSender implements Runnable{
	File file;
	Socket socketFile;
	DataOutputStream dosFile;
	public ClientFileSender(Socket socket, File file) {
		try {
			this.socketFile = socket;
			this.file = file;
			this.dosFile = new DataOutputStream(socketFile.getOutputStream());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			FileInputStream fileIn = new FileInputStream(file);
//			dos.writeUTF("TRANSFER_FILE");
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
			System.out.println("File đã được gửi thành công.");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
