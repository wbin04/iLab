package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.*;
import java.net.*;
import java.util.*;

public class ClientListener implements Runnable{
	String filePath;
	Socket soc;
	public ClientListener(String filePath) {
		try {
			soc = new Socket("localhost", 5000);
			this.filePath = filePath;
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			OutputStream out = soc.getOutputStream();
			DataOutputStream dos = new DataOutputStream(soc.getOutputStream());
			
			File file = new File(filePath);
			FileInputStream fileIn = new FileInputStream(file);
			
			dos.writeUTF(file.getName());
			dos.writeLong(file.length());
			System.out.println("Đang gửi file: " + file.getName());
			
			byte[] buffer = new byte[65536];
			int bytesRead;
			while((bytesRead = fileIn.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
			System.out.println("File đã được gửi thành công.");
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
