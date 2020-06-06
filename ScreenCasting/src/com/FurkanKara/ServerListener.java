package com.FurkanKara;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class ServerListener implements Runnable{
	
	static DatagramSocket serverSocket = null;

	static ArrayList<String> Students = new ArrayList<String>(); //Students' names
	
	static HashMap<String, Integer> StudentsPorts = new HashMap<String, Integer>();  // name : port

	static HashMap<String, InetAddress> StudentAddrs = new HashMap<String, InetAddress>();  // name : addr
	
	public ServerListener() throws SocketException, UnknownHostException {
		
		
		serverSocket = new DatagramSocket(9876);
		
		
		new Thread(this).start();
	}
	
	
	/*
	 * This functions removes the student info from the arraylist and hashmaps.
	 */
	public void kickStudent(String name) {
		StudentsPorts.remove(name);
		StudentAddrs.remove(name);
		Students.remove(name);
	}

	
	/*
	 * When the teacher wants to kick a student, this function sends the kick packet to the client side to let them know 
	 * that he/she is kicked.
	 */
	public void sendKickPacket(InetAddress addr, int port) {
		
		Checksum checksum = new CRC32();
			
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putInt(-200); 
		
		checksum.update(bb.array(), 0, 4);
		
		ByteBuffer sendData = ByteBuffer.allocate(12);
		sendData.putLong(checksum.getValue());
		sendData.putInt(-200); 
		
		
		DatagramPacket sendPacket = new DatagramPacket(sendData.array(), sendData.capacity(), addr, port);
		try {
			serverSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * When a client wants to join, he or she sends a 'Hello' request, this function is responsible for checking the data in the arraylist
	 * to determine whether the nickname is already taken. If it is the case then this function sends an error message to the client side.
	 * Otherwise, sends an 'Ok' message.
	 */
	public void helloRequestHandler(int port, String name, InetAddress addr) {
		if (StudentsPorts.containsKey(name)) {
			System.out.println("WARNING!" + name + " is already connected to the server.");
			ByteBuffer sendData = ByteBuffer.allocate(4);
			sendData.putInt(500); // Sends 'error' message indicating that the name is already taken.
			DatagramPacket sendPacket = new DatagramPacket(sendData.array(), sendData.capacity(), addr, port);
			try {
				serverSocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			
			System.out.println(name + " is connected to the server.");

			ByteBuffer sendData = ByteBuffer.allocate(4);
			sendData.putInt(200); // Sends 'ok' message indicating that the student is added successfully.
			DatagramPacket sendPacket = new DatagramPacket(sendData.array(), sendData.capacity(), addr, port);
			try {
				serverSocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		
		while (true) {
			byte[] buff = new byte[100];
			DatagramPacket receivePacket = new DatagramPacket(buff, buff.length);
			try {
				serverSocket.receive(receivePacket);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			int port = receivePacket.getPort();	
			
			String sentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
			
			InetAddress addr = receivePacket.getAddress();

			System.out.println("RECEIVED: " + sentence + " from " + port);

			String[] splitted_message = sentence.split(" ");
			
			
			if (splitted_message[0].equals("Hello")) {
				
				helloRequestHandler(port,splitted_message[1], addr);
				
			} else if (splitted_message[0].equals("READY")) {

				StudentsPorts.put(splitted_message[1],port);
				StudentAddrs.put(splitted_message[1],addr);
				Students.add(splitted_message[1]);
				
			} else if(splitted_message[0].equals("FIN")) {
				kickStudent(splitted_message[1]);		
				Thread th = new Thread(new Runnable() {
					
					@Override
					public void run() {
						JOptionPane.showMessageDialog(null, splitted_message[1] + " has left.", "Information", JOptionPane.INFORMATION_MESSAGE);
						
					}
				});
				th.start();
				
			}

		
				
		}
		
	}
	

	
}
