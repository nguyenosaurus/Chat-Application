package src2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import src2.ChatWindow;

public class ChatManager implements Runnable{
	private Thread t;
	private ChatWindow cw;
	private String ipaddress;
	private String port;
	private DatagramSocket ds;
	private String destinationPath = "C:\\Users\\Public\\File\\";
	private String sender;
	private String receiver;
	
	public ChatManager(String receiver,String ipaddress, String port,DatagramSocket ds, String sender) throws IOException {
        this.ipaddress = ipaddress;
        this.port = port;
        this.ds = ds;
        this.sender = sender;
        this.receiver = receiver;
        t = null;
	}
	
	public String getSocketAddress() {
		return "/"+ipaddress+":"+port;
	}
	
	@Override
	public void run() {
		cw.getButton().addActionListener(new SendButtonListener());
	}
	
	public void createFrame() {
		cw = new ChatWindow(receiver);
	}
	
	public void addTextFromOtherUser(String string) {
        cw.addTextFromOtherUser(string);
    }
	
	public void start() throws IOException {
		createFrame();

		if (t==null) {
			t = new Thread(this);
			t.start();
		}
	}
	
	class SendButtonListener implements ActionListener {
       
        @Override
        public void actionPerformed(ActionEvent e){
        	try {
        	String a= cw.getText();
        	String msg =sender+":"+a;
			DatagramPacket DpSend = new DatagramPacket(msg.getBytes(),msg.getBytes().length,InetAddress.getByName(ipaddress),Integer.parseInt(port));
			ds.send(DpSend);
			cw.addTextFromThisUser(msg);
			if (msg.contains("\\")) {
				FileEvent event = getFileEvent(a);
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				ObjectOutputStream os = new ObjectOutputStream(outputStream);
				os.writeObject(event);
				byte[] data = outputStream.toByteArray();
				DpSend = new DatagramPacket(data, data.length,InetAddress.getByName(ipaddress),Integer.parseInt(port));
				ds.send(DpSend);
				System.out.println("File sent from client");
			}
        	} catch (UnknownHostException er) {
    			er.printStackTrace();
    		} catch (SocketException er) {
    			er.printStackTrace();
    		} catch (IOException er) {
    			er.printStackTrace();
    		}
        }		
    }
	
	public FileEvent getFileEvent(String message) {
		FileEvent fileEvent = new FileEvent();
		String fileName = message.substring(message.lastIndexOf("\\") + 1, message.length());
		//String sourceFilePath = message.substring(0, message.lastIndexOf("\\") + 1);
		System.out.println(message);
		fileEvent.setDestinationDirectory(destinationPath);
		fileEvent.setFilename(fileName);
		fileEvent.setSourceDirectory(message);
		File file = new File(message);
		if (file.isFile()) {
			try {
			DataInputStream diStream = new DataInputStream(new FileInputStream(file));
			long len = (int) file.length();
			byte[] fileBytes = new byte[(int) len];
			int read = 0;
			int numRead = 0;
			while (read < fileBytes.length && (numRead = diStream.read(fileBytes, read, fileBytes.length - read)) >= 0) {
				read = read + numRead;
			}
			fileEvent.setFileSize(len);
			fileEvent.setFileData(fileBytes);
			fileEvent.setStatus("Success");
			} catch (Exception e) {
				e.printStackTrace();
				fileEvent.setStatus("Error");
			}
		} else {
			System.out.println("path specified is not pointing to a file");
			fileEvent.setStatus("Error");
		}
		return fileEvent;
	}
}
