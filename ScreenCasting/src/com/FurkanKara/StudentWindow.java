package com.FurkanKara;

import java.awt.EventQueue;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;

public class StudentWindow {

	private JFrame frame;
	static JLabel img;

	Client student;
	
	/**
	 * Launch the application.
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					StudentWindow window = new StudentWindow();
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
	public StudentWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1024, 728);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
				try {
					if (Client.connected == true) {						
						student.sendFIN();
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		    }
		});
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnNewMenu = new JMenu("File");
		menuBar.add(mnNewMenu);
		
		JMenuItem Connect = new JMenuItem("Connect");
		Connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String addr = JOptionPane.showInputDialog("Enter the teacher's IP");
			        String name = JOptionPane.showInputDialog("Enter your name");
			        if (addr.isEmpty()) {
			        	JOptionPane.showMessageDialog(null,  "The address field can't be empty!", "Error", JOptionPane.ERROR_MESSAGE);
			        }
			        else if (name.isEmpty()) {
			        	JOptionPane.showMessageDialog(null,  "The name field can't be empty!", "Error", JOptionPane.ERROR_MESSAGE);
			        } else {
			        	student = new Client(name, addr);			        	
			        }
					
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null,  "The IP you entered could not be resolved!", "Error", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
			}
		});
		mnNewMenu.add(Connect);
		
		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (Client.connected == true) {						
						student.sendFIN();
					}
					System.exit(0);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		JMenuItem disconnect = new JMenuItem("Disconnect");
		disconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (Client.connected == true) {						
						student.sendFIN();
						Client.connected = false;
					} else {
						JOptionPane.showMessageDialog(null,  "You are not connected to the lecture!", "Warning", JOptionPane.WARNING_MESSAGE);
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		mnNewMenu.add(disconnect);
		
		JMenuItem capture = new JMenuItem("Capture Image");
		capture.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(Client.connected == true) {
					try {
						student.captureImage();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} else {
					JOptionPane.showMessageDialog(null,  "You haven't connected to the lecture", "Error", JOptionPane.ERROR_MESSAGE);
				}
				
			}
		});
		mnNewMenu.add(capture);
		
		JMenuItem startRecord = new JMenuItem("Start Recording");
		startRecord.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if (Client.imageSaverFlag == false) {
					student.saveImages();
				} else {
					JOptionPane.showMessageDialog(null,  "You have to stop recording first!", "Error", JOptionPane.ERROR_MESSAGE);
				}
				
			}
		});
		mnNewMenu.add(startRecord);
		
		JMenuItem stopRecord = new JMenuItem("Stop Recording");
		stopRecord.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if (Client.imageSaverFlag == true) {
					student.createVideo();
				} else {
					JOptionPane.showMessageDialog(null,  "You have to start recording first!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		mnNewMenu.add(stopRecord);
		mnNewMenu.add(exit);
		
		JMenu mnNewMenu_1 = new JMenu("Help");
		menuBar.add(mnNewMenu_1);
		
		img = new JLabel();
		frame.getContentPane().add(img, BorderLayout.CENTER);
	}

}
