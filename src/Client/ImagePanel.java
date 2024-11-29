package Client;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.application.Platform;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ImagePanel extends Canvas {
    private BufferedImage img;
    private DataOutputStream dosMouse;
    private DataOutputStream dosKeyboard;
    private Dimension serverScreenSize;

    public ImagePanel(Socket socketMouse, Socket socketKeyboard, Dimension serverScreenSize, boolean isRemote) {
        this.serverScreenSize = serverScreenSize;
        try {
            this.dosMouse = new DataOutputStream(socketMouse.getOutputStream());
            this.dosKeyboard = new DataOutputStream(socketKeyboard.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(isRemote) {
        	this.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> sendMouseEvent(e, "MOUSE_PRESS"));
            this.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> sendMouseEvent(e, "MOUSE_RELEASE"));
            this.addEventHandler(MouseEvent.MOUSE_MOVED, e -> sendMouseEvent(e, "MOUSE_MOVE"));
            this.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> sendMouseEvent(e, "MOUSE_DRAGGED"));

            this.addEventHandler(ScrollEvent.SCROLL, e -> {
                try {
                	int scrollAmount = (int) Math.signum(e.getDeltaY()) * -1;
                    dosMouse.writeUTF("MOUSE_WHEEL");
                    dosMouse.writeInt(scrollAmount);
                    dosMouse.flush();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            this.setFocusTraversable(true);
            this.addEventHandler(KeyEvent.KEY_PRESSED, e -> sendKeyEvent(e, "KEY_PRESS"));
            this.addEventHandler(KeyEvent.KEY_RELEASED, e -> sendKeyEvent(e, "KEY_RELEASE"));
            this.addEventHandler(KeyEvent.KEY_TYPED, e -> sendKeyEvent(e, "KEY_TYPED"));
        }
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
        Platform.runLater(() -> {
        	drawImage();
        });
    }

    private void sendMouseEvent(MouseEvent e, String eventType) {
        try {
            double scaleX = serverScreenSize.getWidth() / getWidth();
            double scaleY = serverScreenSize.getHeight() / getHeight();
            
            switch (eventType) {
	            case "MOUSE_PRESS":
	            case "MOUSE_RELEASE":
	            	dosMouse.writeUTF(eventType);
	                dosMouse.writeInt((int) (e.getX() * scaleX));
	                dosMouse.writeInt((int) (e.getY() * scaleY));
	                dosMouse.writeInt(e.getButton() == MouseButton.PRIMARY ? 1 : (e.getButton() == MouseButton.SECONDARY ? 3 : 2));
	                dosMouse.flush();
	                break;
	
	            case "MOUSE_MOVE":
//	            	new Thread(() -> {
	            		try {
//	            			double currentX = e.getX() * scaleX;
//	            	        double currentY = e.getY() * scaleY;
//	            	        
//	            	        if (Math.abs(currentX - lastX) < MOVE_THRESHOLD && Math.abs(currentY - lastY) < MOVE_THRESHOLD) {
//	                            return; 
//	                        }
//	                        lastX = currentX;
//	                        lastY = currentY;
	                        
							dosMouse.writeUTF(eventType);
							dosMouse.writeInt((int) e.getX());
					        dosMouse.writeInt((int) e.getY());
//			                dos.writeInt(e.getButton() == MouseButton.PRIMARY ? 1 : (e.getButton() == MouseButton.SECONDARY ? 3 : 2));
			                dosMouse.flush();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
//	            	}).start();
	                break;
	                
	            case "MOUSE_DRAGGED":
	            	dosMouse.writeUTF(eventType);
	                dosMouse.writeInt((int) (e.getX() * scaleX));
	                dosMouse.writeInt((int) (e.getY() * scaleY));
//	                dos.writeInt(e.getButton() == MouseButton.PRIMARY ? 1 : (e.getButton() == MouseButton.SECONDARY ? 3 : 2));
	                dosMouse.flush();
	                break;     
	        }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void sendKeyEvent(KeyEvent e, String eventType) {
        try {
            KeyCode code = e.getCode();
            String character = e.getCharacter();
            
            if (eventType.equals("KEY_PRESS") || eventType.equals("KEY_RELEASE")) {
                if (isSpecialKey(code)) {
                    dosKeyboard.writeUTF(eventType);
                    dosKeyboard.writeInt(code.getCode()); 
                    dosKeyboard.flush();
                }
            }

            else if (eventType.equals("KEY_TYPED") && !character.isEmpty()) {
                if (!character.equals("\b") && !character.equals("\r")) {  
                    dosKeyboard.writeUTF("KEY_TYPED");
                    dosKeyboard.writeUTF(character); 
                    dosKeyboard.flush();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private boolean isSpecialKey(KeyCode code) {
        return code == KeyCode.BACK_SPACE || code == KeyCode.ENTER || code == KeyCode.TAB ||
               code == KeyCode.CONTROL || code == KeyCode.DELETE || code == KeyCode.ESCAPE ||
               code == KeyCode.UP || code == KeyCode.DOWN || 
               code == KeyCode.LEFT || code == KeyCode.RIGHT;
    }
}
