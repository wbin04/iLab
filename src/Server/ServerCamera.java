package Server;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.util.Duration;
import javafx.embed.swing.SwingFXUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

public class ServerCamera {
    private DataInputStream dis;
    private DataOutputStream dos;
    private boolean isShowCam = false;
    private Canvas canvas;
    
    private boolean isRunning;

    public ServerCamera(Socket socketCam) {
        try {
            this.dis = new DataInputStream(socketCam.getInputStream());
            this.dos = new DataOutputStream(socketCam.getOutputStream());
            this.canvas = new Canvas(300, 200); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startCameraListening() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        new Thread(() -> {
            try {
                System.out.println("Client connected, ready to control video stream...");

                while (isRunning) {
                    if (isShowCam) {
                        int size = dis.readInt();
                        if (size <= 0) break;

                        byte[] imageBytes = new byte[size];
                        dis.readFully(imageBytes);

                        MatOfByte matOfByte = new MatOfByte(imageBytes);
                        Mat frame = Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_COLOR);

                        if (!frame.empty()) {
                            BufferedImage bufferedImage = convertMatToImage(frame);

                            Platform.runLater(() -> {
                                GraphicsContext gc = canvas.getGraphicsContext2D();

                                double canvasWidth = canvas.getWidth();
                                double canvasHeight = canvas.getHeight();

                                Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                                gc.clearRect(0, 0, canvasWidth, canvasHeight); 
                                gc.drawImage(image, 0, 0, canvasWidth, canvasHeight); 
                            });
                        } else {
                            System.out.println("Empty frame received!");
                        }
                    } else {
                        Platform.runLater(() -> {
                            GraphicsContext gc = canvas.getGraphicsContext2D();
                            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());  
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


    private BufferedImage convertMatToImage(Mat frame) {
        try {
            MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(".jpg", frame, matOfByte);

            byte[] byteArray = matOfByte.toArray();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
            BufferedImage bufferedImage = ImageIO.read(byteArrayInputStream);

            return bufferedImage;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Canvas showCamera() {
        isShowCam = true;
        System.out.println("Requesting to start camera...");
        try {
        	isRunning = true;
        	startCameraListening();
			dos.writeUTF("TURN_ON_CAMERA");
	        dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return canvas;  
    }

    public void stopCamera() {
        isShowCam = false;
        System.out.println("Requesting to stop camera...");
        try {
        	isRunning = false;
			dos.writeUTF("TURN_OFF_CAMERA");
	        dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
	public Canvas canvasResize(Canvas canvas, boolean isHover) {
		if (isHover) {
	        animateCanvasResize(canvas, 576, 324);
	    } else {
	        animateCanvasResize(canvas, 300, 200);
	    }
		return canvas;
	}
    
    private void animateCanvasResize(Canvas canvas, double targetWidth, double targetHeight) {
	    Timeline timeline = new Timeline(
	        new KeyFrame(Duration.millis(300),
	            new KeyValue(canvas.widthProperty(), targetWidth),
	            new KeyValue(canvas.heightProperty(), targetHeight)
	        )
	    );
	    timeline.play();
	}
}
