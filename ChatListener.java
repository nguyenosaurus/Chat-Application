package src2;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

class ChatListener implements Runnable 
{   
	private DatagramSocket ds; 
	private FileEvent fileEvent = null;
	private Client c;
	
	// constructor 
	public ChatListener(DatagramSocket ds,Client c) { 
		this.ds = ds;
		this.c = c;
	} 

	@Override
	public void run() { 
		try {
			byte[] receive = new byte[65535];
			DatagramPacket DpReceive = null;
			while (true) {
				DpReceive = new DatagramPacket(receive, receive.length);
				ds.receive(DpReceive);
				String message = new String (DpReceive.getData(), 0, DpReceive.getLength());
				String[] peer = message.split(":");
				boolean breakFlag = false;
				for (ChatManager x : c.getChatManagerList()) {
					if (x.getSocketAddress().equals(DpReceive.getSocketAddress().toString())) {
						x.addTextFromOtherUser(message);
						breakFlag = true;
						break;
					}
				}

				if (!breakFlag) {
					String[] b = DpReceive.getSocketAddress().toString().substring(1).split(":");
					String ipaddress = b[0];
					String port = b[1];
					ChatManager cm = new ChatManager(peer[0],ipaddress,port,ds,c.getUsername());
					c.addChatManagerList(cm);
					cm.start();
					cm.addTextFromOtherUser(message);
				}
				if (message.contains("\\")) {
				byte[] incomingData = new byte[1024 * 1000 * 50];
					DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
					ds.receive(incomingPacket);
					byte[] data = incomingPacket.getData();
					ByteArrayInputStream in = new ByteArrayInputStream(data);
					ObjectInputStream is = new ObjectInputStream(in);
					fileEvent = (FileEvent) is.readObject();
					if (fileEvent.getStatus().equalsIgnoreCase("Error")) {
						System.out.println("Some issue happened while packing the data @ client side");
						continue;
					}
					createAndWriteFile();
				receive = new byte[65535];
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	} 
	
	public void createAndWriteFile() {
	String outputFile = fileEvent.getDestinationDirectory() + fileEvent.getFilename();
	if (!new File(fileEvent.getDestinationDirectory()).exists()) {
		new File(fileEvent.getDestinationDirectory()).mkdirs();
	}
	
	File dstFile = new File(outputFile);
	FileOutputStream fileOutputStream = null;
	
	try {
		fileOutputStream = new FileOutputStream(dstFile);
		fileOutputStream.write(fileEvent.getFileData());
		fileOutputStream.flush();
		fileOutputStream.close();
		System.out.println("Output file : " + outputFile + " is successfully saved ");
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}

	}

}

