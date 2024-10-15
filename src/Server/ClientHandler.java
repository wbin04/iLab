package Server;
import java.util.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.*;
public class ClientHandler  implements Runnable{
	private String id;
	private Socket socket;
	private DataInputStream input;
	private DataOutputStream output;
	
	public ClientHandler(Socket socket, String id) {
		this.socket = socket;
		this.id = id;
		try {
			this.input = new DataInputStream(socket.getInputStream());
			this.output = new DataOutputStream(socket.getOutputStream());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			String fileName = input.readUTF();
			long fileSize = input.readLong();
			FileOutputStream fileOut = new FileOutputStream("D:/PBL4/received_" + fileName);
			
			System.out.println("Dang nhan file: " + fileName);
			InputStream in = socket.getInputStream();
			byte[] buffer = new byte[65536];
			int bytesRead;
			long totalBytesRead = 0;
			while(totalBytesRead < fileSize && (bytesRead = in.read(buffer)) != -1) { // in.read(buffer) đọc dữ liệu từ inputStream lưu vào buffer
				fileOut.write(buffer, 0, bytesRead);
				totalBytesRead += bytesRead;
			}
			System.out.println("File " + fileName + " đã nhận được thành công");
		} catch (Exception e) {
			// TODO: handle exception
		}

		
	}
}
