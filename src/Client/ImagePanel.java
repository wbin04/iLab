package Client;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.util.*;

import javax.swing.JPanel;

import java.net.*;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

import javax.swing.JPanel;

import java.net.*;

public class ImagePanel extends JPanel{
	BufferedImage img;
	int off = 0;
	Socket socket;
	DataOutputStream dos;
	Dimension serverScreenSize;

	public ImagePanel(Socket socket, Dimension serverScreenSize) {
		this.socket = socket;
		try {
			this.dos = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.serverScreenSize = serverScreenSize;
		
		
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				sendMouseEvent(e, "MOUSE_PRESS");
			}
			
			@Override
            public void mouseReleased(MouseEvent e) {
                sendMouseEvent(e, "MOUSE_RELEASE");
            }
		});
		
		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				sendMouseEvent(e, "MOUSE_MOVE");
			}
			
			public void mouseDragged(MouseEvent e) {
                sendMouseEvent(e, "MOUSE_DRAGGED");
            }
		});
		
		addMouseWheelListener(new MouseWheelListener() {
		    @Override
		    public void mouseWheelMoved(MouseWheelEvent e) {
		        try {
		            dos.writeUTF("MOUSE_WHEEL");
		            dos.writeInt(e.getWheelRotation());
		            dos.flush();
		        } catch (IOException ex) {
		            ex.printStackTrace();
		        }
		    }
		});
		
		setFocusable(true);
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.isControlDown() || e.isAltDown()) {
		            return;
		        }
				sendKeyEvent(e, "KEY_PRESS");
			}
			
			@Override
            public void keyReleased(KeyEvent e) {
                sendKeyEvent(e, "KEY_RELEASE");
            }
		});
	}
	
	@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (img != null) {
            g.drawImage(img, off, off, getWidth() - off, getHeight() - off, 
                    0, 0, img.getWidth(), img.getHeight(), null);
        }
    }
	
    // Phương thức để cập nhật hình ảnh từ server
    public void updateImage(BufferedImage newImg) {
        this.img = newImg;
        repaint();
    }
    
    private void sendMouseEvent(MouseEvent e, String eventType) {
    	try {
    		double scaleX = serverScreenSize.getWidth() / getWidth();
    		double scaleY = serverScreenSize.getHeight() / getHeight();
			dos.writeUTF(eventType);
//			dos.writeInt((int)(e.getX()));
//			dos.writeInt((int)(e.getY()));
			dos.writeInt((int) (e.getX() * scaleX));
		    dos.writeInt((int) (e.getY() * scaleY));
			dos.writeInt(e.getButton());
			dos.flush();
		} catch (Exception e2) {
			// TODO: handle exception
			e2.printStackTrace();
		}
    }
    
    private void sendKeyEvent(KeyEvent e, String eventType) {
//    	try {
//			dos.writeUTF(eventType);
//			dos.writeInt(e.getKeyCode());
//			dos.flush();
//		} catch (Exception e2) {
//			// TODO: handle exception
//			e2.printStackTrace();
//		}
    	
    	try {
            if (Character.isLetterOrDigit(e.getKeyChar())) {
                dos.writeUTF("TYPED_TEXT");
                dos.writeUTF(String.valueOf(e.getKeyChar())); 
            } else {
                dos.writeUTF(eventType); 
                dos.writeInt(e.getKeyCode());
            }
            dos.flush();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }
}
