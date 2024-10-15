package Client;

public class TransferClient {
	public static void main(String[] args) {
		String filePath = "D:/PBL4/txt1.txt";
		try {
			ClientListener client = new ClientListener(filePath);
			Thread thread = new Thread(client);
			thread.start();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
