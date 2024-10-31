package Client;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.embed.swing.SwingFXUtils;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ImagePanel extends Canvas {
    private BufferedImage img;
    private DataOutputStream dos;
    private Dimension serverScreenSize;

    public ImagePanel(Socket socket, Dimension serverScreenSize) {
        this.serverScreenSize = serverScreenSize;
        try {
            this.dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> sendMouseEvent(e, "MOUSE_PRESS"));
        this.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> sendMouseEvent(e, "MOUSE_RELEASE"));
        this.addEventHandler(MouseEvent.MOUSE_MOVED, e -> sendMouseEvent(e, "MOUSE_MOVE"));
        this.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> sendMouseEvent(e, "MOUSE_DRAGGED"));

        this.addEventHandler(ScrollEvent.SCROLL, e -> {
            try {
                dos.writeUTF("MOUSE_WHEEL");
                dos.writeInt((int) e.getDeltaY());
                dos.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        this.setFocusTraversable(true);
        this.addEventHandler(KeyEvent.KEY_PRESSED, e -> sendKeyEvent(e, "KEY_PRESS"));
        this.addEventHandler(KeyEvent.KEY_RELEASED, e -> sendKeyEvent(e, "KEY_RELEASE"));
        this.addEventHandler(KeyEvent.KEY_TYPED, e -> sendKeyEvent(e, "KEY_TYPED"));
    }

    private void drawImage() {
        if (img != null) {
            GraphicsContext gc = this.getGraphicsContext2D();
            Image fxImage = SwingFXUtils.toFXImage(img, null);
            gc.clearRect(0, 0, getWidth(), getHeight());
            gc.drawImage(fxImage, 0, 0, getWidth(), getHeight());
        }
    }

    public void updateImage(BufferedImage newImg) {
        this.img = newImg;
        drawImage();
    }

    private void sendMouseEvent(MouseEvent e, String eventType) {
        try {
            double scaleX = serverScreenSize.getWidth() / getWidth();
            double scaleY = serverScreenSize.getHeight() / getHeight();
            
            switch (eventType) {
	            case "MOUSE_PRESS":
	            case "MOUSE_RELEASE":
	            	dos.writeUTF(eventType);
	                dos.writeInt((int) (e.getX() * scaleX));
	                dos.writeInt((int) (e.getY() * scaleY));
	                dos.writeInt(e.getButton() == MouseButton.PRIMARY ? 1 : (e.getButton() == MouseButton.SECONDARY ? 3 : 2));
	                dos.flush();
	                break;
	
	            case "MOUSE_MOVE":
	            	dos.writeUTF(eventType);
	                dos.writeInt((int) (e.getX() * scaleX));
	                dos.writeInt((int) (e.getY() * scaleY));
//	                dos.writeInt(e.getButton() == MouseButton.PRIMARY ? 1 : (e.getButton() == MouseButton.SECONDARY ? 3 : 2));
	                dos.flush();
	                break;
	                
	            case "MOUSE_DRAGGED":
	            	dos.writeUTF(eventType);
	                dos.writeInt((int) (e.getX() * scaleX));
	                dos.writeInt((int) (e.getY() * scaleY));
//	                dos.writeInt(e.getButton() == MouseButton.PRIMARY ? 1 : (e.getButton() == MouseButton.SECONDARY ? 3 : 2));
	                dos.flush();
	                break;     
	        }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void sendKeyEvent(KeyEvent e, String eventType) {
    	try {
		      String character = e.getCharacter();
		      System.out.println(e.getCharacter() + "---" + e.getCode() + "---" + e.getCode().getCode());
		      if (eventType.equals("KEY_TYPED") && !e.getCharacter().isEmpty()) {
		    	  dos.writeUTF("KEY_TYPED");
		    	  dos.writeUTF(character);
		    	  dos.flush();
		      } else if (eventType.equals("KEY_PRESSED") || eventType.equals("KEY_RELEASED")) {
		            dos.writeUTF(eventType);
		            dos.writeInt(e.getCode().getCode());
		            dos.flush();
		      }
		  } catch (IOException ex) {
		      ex.printStackTrace();
		  }
    }

}
