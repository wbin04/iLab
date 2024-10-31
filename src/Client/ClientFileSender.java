package Client;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientFileSender implements Runnable{
	File file;
	Socket soc;
	DataOutputStream dos;
	public ClientFileSender(Socket socket, File file) {
		try {
			this.soc = socket;
			this.file = file;
			this.dos = new DataOutputStream(soc.getOutputStream());
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
			dos.writeUTF(file.getName());
			dos.writeLong(file.length());
			System.out.println("Đang gửi file: " + file.getName());
			
			byte[] buffer = new byte[8192];
			int bytesRead;
			while((bytesRead = fileIn.read(buffer)) != -1) {
				dos.write(buffer, 0, bytesRead);
			}
			dos.flush();
			fileIn.close();
			System.out.println("File đã được gửi thành công.");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
