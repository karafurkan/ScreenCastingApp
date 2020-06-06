package com.FurkanKara;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

public class Client implements Runnable{
	
	DatagramSocket clientSocket = null;
	InetAddress IPAddress = null;

	String studentName = null;
	boolean isNameTaken = false;
	int helloRequestCounter = 0;
	
	int timeoutCounter = 0;
	
	ArrayList<byte[]> bufferList = new ArrayList<byte[]>();
	
	static boolean connected = false;
	
	BufferedImage image;
	
	static boolean imageSaverFlag = false;
	
	String homePath;
	
	static boolean isBusy = false;
	
	public Client(String name, String addr) throws IOException{
		
		this.studentName = name;
		
		System.out.println("Client is running...");
	
		clientSocket = new DatagramSocket();
		clientSocket.setSoTimeout(20);
		
		IPAddress = InetAddress.getByName(addr);
		//IPAddress = InetAddress.getByName("localhost");

		sendHello();
		
		setHomePath();
		
		connected = true;
		clientSocket.setSoTimeout(100);

	
		new Thread(this).start();
		
		
	}
	
	/*
	 * Sets the home path in order to locate the Desktop folder.
	 */
	public void setHomePath() {
		FileSystemView filesys = FileSystemView.getFileSystemView();
		File[] roots = filesys.getRoots();
		this.homePath = filesys.getHomeDirectory().toString();
	}
	
	/*
	 * When the user wants to record the session, this function is called initially to save the screenshot
	 * sent by the server. The reason why a buffer is not kept to do this is that it will overflow after sometime.
	 * As this function runs independently from the client's job, in other words there is another thread
	 * it won't effect the receiving function.
	 */
	public void saveImages() {
		this.isBusy = true; // If the user wants the leave from the lecture this flag won't let him until it stop recording.

		class ImageSaver implements Runnable{
			public ImageSaver() {
				imageSaverFlag = true;
				new Thread(this).start();
			}

			@Override
			public void run() {
				int count = 1;
				FileSystemView filesys = FileSystemView.getFileSystemView();
				File[] roots = filesys.getRoots();
				String homePath = filesys.getHomeDirectory().toString();
				File file = new File(homePath + "/Desktop/photos");
				
				file.mkdirs();

				while(imageSaverFlag) {
					System.out.println("photo saved!");
					try {
						String fileName = count + ".png";
						
						File outputFile = new File(homePath + "/Desktop/photos/" + fileName);
						ImageIO.write(image, "png", outputFile);
						count++;
						Thread.sleep(20);
					} catch (Exception ex) {
						System.out.println("Exception in image saver thread.");
						Client.isBusy = false;
					}
				}
			}
			
		}
		
		new ImageSaver();
		
	}
	
	
	/*
	 * When the user click on the Stop Recording button, this function will be called.
	 * This function creates a video from the images located in the Desktop folder.
	 * Afterwards, it deletes the images.
	 */
	public void createVideo() {
		imageSaverFlag = false;
		VideoCreater creater = new VideoCreater();
		creater.createVideo();
		
		//After creating the video delete the folder that consists of images
		File file = new File(this.homePath + "/Desktop/photos");
		String[]entries = file.list();
		for(String s: entries){
		    File currentFile = new File(file.getPath(),s);
		    currentFile.delete();
		}
		file.delete();
	}
	
	
	/*
	 * This function sends a 'Hello' request to the server in order to join the lecture'
	 * If this function receives 'Ok' message, sends 'Ready' message back to the server.
	 * Otherwise, sends the 'Hello' message again. After sending 100 'Hello requests' it quits.
	 * If this function receives 'Error' message from the server indicating that the nickname 
	 * provided by user is already taken, It asks for another nickname. 
	 */
	public boolean sendHello() throws IOException {
		
		// After sending 100 "Hello" requests to the server, if there is no response, exits.
		if (helloRequestCounter == 100) { 
			JOptionPane.showMessageDialog(null,  "Server is down! Exiting...", "Information", JOptionPane.INFORMATION_MESSAGE);
			System.exit(0); 
		}
		helloRequestCounter++;
		
		byte[] receiveData = new byte[64008];
		
		if (isNameTaken == true) {
			studentName = JOptionPane.showInputDialog("Enter your name");
		}
		
		
		String sentence = "Hello " + studentName;  
	
		ByteBuffer data = ByteBuffer.allocate(sentence.getBytes().length);
		data.put(sentence.getBytes());
	
		DatagramPacket sendPacket = new DatagramPacket(data.array(), data.capacity(), IPAddress, 9876);
		clientSocket.send(sendPacket);
	
		data = ByteBuffer.allocate(4);
		data.clear();
		data.rewind();
		
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

		try {
			clientSocket.receive(receivePacket);
	
			for (int i = 0; i < 4; i++) {
				data.put(receivePacket.getData()[i]);
			}
			
			int result = data.getInt(0); 
			if (result == 200) {

			} else if (result == 500) {
				isNameTaken = true;
				sendHello();  	// Name is already taken. Should chose a different name and send "hello" again.
				return false;
			}
				
		} catch(Exception ex) {
			sendHello();  		// Hello request timeouts, sends it again.
			return false;
		}
		
		
		data.clear();
		data.rewind();
		
		/**
		 * Sending "READY" message to let the server know that the client is ready to receive.
		 */
		
		sentence = "READY " + studentName; 
		data = ByteBuffer.allocate(sentence.getBytes().length);
		data.put(sentence.getBytes());
		
		sendPacket = new DatagramPacket(data.array(), data.capacity(), IPAddress, 9876);
		clientSocket.send(sendPacket);
		
		return true;
	}

	
	
	/*
	 * When the user disconnect from the lecture or close the window,
	 * this function is called in order to let the server know that user is left.
	 */
	public void sendFIN() throws IOException {
		String sentence = "FIN " + studentName;  
		ByteBuffer data = ByteBuffer.allocate(sentence.getBytes().length);
		data.put(sentence.getBytes());
		DatagramPacket sendPacket = new DatagramPacket(data.array(), data.capacity(), IPAddress, 9876);
		clientSocket.send(sendPacket);
		
		
	}
	

	/*
	 * When the user click on the 'Capture Image' button to take a snapshot, this function is called.
	 * This function takes the last image sent by the server and stores it in the Desktop folder.
	 */
	public void captureImage() throws IOException {
		
		if (image != null) {
			SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss");
			Date date = new Date(System.currentTimeMillis());
			
			String fileName = formatter.format(date) + ".jpg";
			
			System.out.println(homePath);
			File outputFile = new File(this.homePath + "/Desktop/" + fileName);

			
			ImageIO.write(image, "jpg", outputFile);
			JOptionPane.showMessageDialog(null,  "Image has been captured successfully.", "Successful", JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null,  "An error occured while capturing the image.", "Error", JOptionPane.ERROR_MESSAGE);
		}
		
		
	}
	
	
	/*
	 * This functions is responsible for receiving the images that are being sent by the server.
	 * 
	 * If this function successfully receives the whole image, 
	 * it stores the image in a global BufferedImage variable and let the other functions use it if needed.
	 * 
	 * As this function receives the whole image chunk by chunk,
	 * if there is any error in any chunk, the function drops the whole image.
	 * 
	 * For the error detection; checksum, chunk number, size of the packet, the total size of the image are checked.  
	 *
	 */
	public void receivePackets() throws IOException{
		
		timeoutCounter++;
		
		byte[] receiveData = new byte[64020];
		
		Checksum checksum = new CRC32();
	
		int expectedChunkNumber = 1;
		

		int numberOfPackets = 0;
		
		ByteBuffer buffer = null;
		
		while (true) {
			
			DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
			
			try {				
				
				clientSocket.receive(receivedPacket);
				timeoutCounter = 0;
			
			} catch (Exception ex){
				
				//System.out.println("Timed out!");
				if (timeoutCounter == 300) {
					JOptionPane.showMessageDialog(null,  "Server is down! Exiting...", "Information", JOptionPane.INFORMATION_MESSAGE);
					System.exit(0); 
				}
				return;
				
			}
			
			int packetLength = receivedPacket.getLength();
			
			

			ByteBuffer data = ByteBuffer.allocate(packetLength);
			data.put(receiveData, 0, packetLength);
			
			data.rewind();
			
			long checksumVal = data.getLong();
					
			
			int receivedChunkNumber = data.getInt();
			
			
			if (receivedChunkNumber == -1) { 
				checksum.reset();
				checksum.update(data.array(), 8 , data.capacity() - 8);
				long calculatedChecksum = checksum.getValue();
				
				if ( (calculatedChecksum == checksumVal) && (numberOfPackets + 1 == expectedChunkNumber )) { // That means the screenshot has been successfully arrived.
					//System.out.println("Exit normally"); 
					break; 
				}
				return;  // packet has -1 chunkNumber arrived earlier, which indicates packet loss has occurred. Or checksum is wrong, which means data is broken
			}
			
			if (receivedChunkNumber == -200) {
				JOptionPane.showMessageDialog(null,  "You have been kicked!", "Warning", JOptionPane.WARNING_MESSAGE);
				System.exit(0);
			}
			
			int payloadSize = data.getInt();
		
			int totalSize = data.getInt();

			
			data.rewind(); 
			checksum.reset();
			checksum.update(data.array(), 20 , payloadSize);
			long calculatedChecksum = checksum.getValue();
			
			if (calculatedChecksum != checksumVal) { // Checksum is wrong, which means data is broken. Drop the packet.
				System.out.println("***************");
				System.out.println("EXPECTED CHECKSUM : " + calculatedChecksum);
				System.out.println("RECEIVED CHECKSUM : " + checksumVal);
				return; 
			}
		
			

		
			if (receivedChunkNumber != expectedChunkNumber) { // Expected chunk did not arrive, drop the packet.
				
				System.out.println("PACKET LOSS");
				
				return;
			}
			
			
			/**
			 * When the first packet arrives, initializes the total array that will hold the screenshot.
			 */
			if (expectedChunkNumber == 1) {
				
				
				buffer = ByteBuffer.allocate(totalSize);
				
				
				
				numberOfPackets = (totalSize / 64000) + 1;  // How many chunks there are in the whole screenshot.
			} 
			data.rewind(); ////////////TEST
			buffer.put(data.array(), 20, payloadSize);
			
			expectedChunkNumber++;
			
			data.clear();
			
		}
		
		if (buffer != null) {
			
			
			
			ByteArrayInputStream bais = new ByteArrayInputStream(buffer.array());

			//BufferedImage bImage = ImageIO.read(bais);

			image = ImageIO.read(bais);
			
			//BufferedImage scaled = scale(bImage);
			
			StudentWindow.img.setIcon(new ImageIcon(image));
			
		}

		

	}
	
	@Override
	public void run() {
		
		while(true) {
			try {
				receivePackets();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
