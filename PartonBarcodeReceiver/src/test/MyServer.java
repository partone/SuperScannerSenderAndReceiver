package test;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.border.EmptyBorder;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Toolkit;
import javax.swing.JCheckBox;



public class MyServer extends JFrame {    
	public static JPanel contentPane;
	
	static JLabel serverText = new JLabel("Listening on port 5000");
	private static JScrollPane scrollPane_1;
	private static JTextPane receptionLog;
	private static String directory = "";
	
	public static void main(String[] args) throws IOException {
		new MyServer();
		//UI stuff
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 52, 414, 199);

		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MyServer frame = new MyServer();
					frame.setVisible(true);
	
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		Executor exe = Executors.newCachedThreadPool();
		ServerSocket serverSocket = null;
		try {
		    serverSocket = new ServerSocket(5000);
		} catch (IOException e) {
		    System.err.println("Could not listen on port: 5000");
		    System.exit(-1);
		}
		while (true) {
		    final Socket clientSocket = serverSocket.accept();
		    clientSocket.setKeepAlive(true);
		    clientSocket.setTcpNoDelay(true);
		    exe.execute(new Runnable() {

		        @Override
		        public void run() {
		            try {
		                Scanner reader = new Scanner(clientSocket.getInputStream());
		                while(reader.hasNextLine()){
		                    String line = reader.nextLine();
		                    System.out.println(line);
		                    messageToTxt(line);
		                }
		                reader.close();
		            } catch (IOException e) {
		                e.printStackTrace();
		            }
		            try {
		                clientSocket.close();
		                
		            } catch (IOException e) {
		                e.printStackTrace();
		            }
		        }
		    });
		}
		
	}

	
	
	/**
	 * Create the frame.
	 * @throws SocketException 
	 * @throws UnknownHostException 
	 */
	//Pretty much all UI stuff here
	public MyServer() throws SocketException, UnknownHostException {
		//UI stuff
		setTitle("Super Scanner Server");
		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(MyServer.class.getResource("/img/ic_launcher.png")));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		serverText.setBounds(10, 11, 269, 14);
		contentPane.add(serverText);
		JLabel ipText = new JLabel("Error getting IP");
		ipText.setBounds(10, 30, 187, 14);
		contentPane.add(ipText);
		
		//Get IP
		try(final DatagramSocket socket = new DatagramSocket()){
			  socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			  String ip = socket.getLocalAddress().getHostAddress();
			  ipText.setText("Your IP: " + ip);
			  
			  scrollPane_1 = new JScrollPane();
			  scrollPane_1.setBounds(10, 55, 414, 196);
			  contentPane.add(scrollPane_1);

			  receptionLog = new JTextPane();
			  scrollPane_1.setViewportView(receptionLog);
			  receptionLog.setEditable(false);
			  receptionLog.setText("Log ready...\nClose this window when you're done receiving\nDefauly directory set, please change\n");
			  
			  JButton btnSetDirectory = new JButton("Set directory");
			  btnSetDirectory.addActionListener(new ActionListener() {
			  	public void actionPerformed(ActionEvent arg0) {
			  		JFileChooser f = new JFileChooser();
				    f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
				    f.showSaveDialog(null);
				    directory = f.getSelectedFile().toString();
				    receptionLog.setText(receptionLog.getText() + "Directory set to: " + directory + "\n");
			  	}
			  });
			  btnSetDirectory.setBounds(289, 7, 135, 23);
			  contentPane.add(btnSetDirectory);
		}
	}
	
	//Receives a string, makes it a .txt
	public static void messageToTxt (String message) throws UnsupportedEncodingException {
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();
		if(message.trim().length() == 3) {
			receptionLog.setText(receptionLog.getText() + "[" + dateFormat.format(date) + "] Good connection with " + message + "\n");
			JScrollBar sb = scrollPane_1.getVerticalScrollBar();
			sb.setValue( sb.getMaximum() + 1 );
			return;
		}
		String[] messageArray = message.split("\\|");	//Split codes by this delimiter
		PrintWriter writer;
		
		try {
			String filename = directory + "/" + messageArray[0] + " - " + messageArray[messageArray.length - 1] + ".txt"; //Get last number
			writer = new PrintWriter(filename, "UTF-8");
			for(int i = 1; i < messageArray.length - 1; i++) {	//Omit the last character since it's the zone
				writer.println(messageArray[i]);
			}
			writer.close();
			receptionLog.setText(receptionLog.getText() + "[" + dateFormat.format(date) + "] Received " + messageArray[messageArray.length - 1] + " from " + messageArray[0] + "\n");
			JScrollBar sb = scrollPane_1.getVerticalScrollBar();
			sb.setValue( sb.getMaximum() + 1 );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
