package Client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
public class RemoteClient {
	public static void main(String[] args) {
		ClientListener client = new ClientListener();
        client.startListening();
	}
}