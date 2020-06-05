package com.FurkanKara;

import java.awt.AWTException;
import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;

import java.awt.BorderLayout;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class TeacherWindow {
	private JFrame frame;
	static JLabel img;
	
	ServerListener sListener;
	ServerSender sSender;
	

	/**
	 * Launch the application.
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TeacherWindow window = new TeacherWindow();
					window.frame.setVisible(true);
					

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		


		
	}

	/**
	 * Create the application.
	 */
	public TeacherWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1024, 728);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		img = new JLabel();
		frame.getContentPane().add(img, BorderLayout.CENTER);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		
		JMenuItem Start = new JMenuItem("Share Screen");
		Start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					sListener = new ServerListener();
					sSender = new ServerSender();
					JOptionPane.showMessageDialog(null,  "You can minimize the screen, screen casting has started successfully. ", "Successful", JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null,  "Something is wrong!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		fileMenu.add(Start);
		
		JMenuItem kickMenu = new JMenuItem("Kick");
		kickMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] students = new String[ServerListener.StudentsPorts.size()];
				int i = 0;
				for (String st: ServerListener.Students) {
					students[i] = st;
					i++;
				}
				JList<String> list = new JList<>(students);
				
				JOptionPane.showMessageDialog(frame, list, "Kick", JOptionPane.WARNING_MESSAGE);
				String studentToKick = list.getSelectedValue();
				if (studentToKick != null) {

					sListener.sendKickPacket(ServerListener.StudentAddrs.get(studentToKick), ServerListener.StudentsPorts.get(studentToKick));  // BUG
					sListener.kickStudent(studentToKick);
				} 

			}
		});
		fileMenu.add(kickMenu);
		
		JMenu helpMenu = new JMenu("Help");
		menuBar.add(helpMenu);
	}




	
}
