
package Client;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientCamera implements Runnable {
    private DataOutputStream dos;
    private DataInputStream dis;
    
    private boolean isRunning;

    public ClientCamera(Socket socketCam) {
        try {
            this.dos = new DataOutputStream(socketCam.getOutputStream());
            this.dis = new DataInputStream(socketCam.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void startCameraListener() {
    	while(true) {
    		try {
    			String msg = dis.readUTF();
    			if(msg.equals("TURN_ON_CAMERA")) {

    				handleCameraEvent();
    				isRunning = true;
    			}
    			else if(msg.equals("TURN_OFF_CAMERA")) {
    				isRunning = false;
    			}
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    }

    public void handleCameraEvent() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        new Thread(() -> {
            try {
                System.out.println("Ready to receive server commands...");

                
                VideoCapture capture = new VideoCapture();

                Mat frame = new Mat();
                MatOfByte matOfByte = new MatOfByte();
            	capture.open(0);
                while (isRunning) {
                    capture.read(frame);
                    if (frame.empty()) {
                        System.out.println("No frame captured!");
                        break;
                    }

                    boolean encoded = Imgcodecs.imencode(".jpg", frame, matOfByte);
                    if (encoded) {
                        byte[] imageBytes = matOfByte.toArray();

                        dos.writeInt(imageBytes.length);
                        dos.write(imageBytes);
                        dos.flush();
                    }
                }
				capture.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

	@Override
	public void run() {
		new Thread(this::startCameraListener).start();
	}
}