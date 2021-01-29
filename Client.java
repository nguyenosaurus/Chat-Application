package src2;

import java.io.*; 
import java.net.*; 
import java.util.*;

import src2.ChatManager; 

public class Client 
{ 
	private HashMap <String,String[]> list;
	private ArrayList <ChatManager> cm = new ArrayList<ChatManager>();
	private final int ServerPort = 1234;
	private String destinationPath = "C:\\Users\\Public\\File\\";
	private String username;
	
	public static void main(String args[]) throws UnknownHostException, IOException 
	{
		Client client = new Client();
		client.createConnection();
	}
	
	public ArrayList<ChatManager> getChatManagerList() {
		return cm;
	}
	
	public void addChatManagerList(ChatManager c) {
		cm.add(c);
	}
	
	public String getUsername() {
		return username;
	}

	public void createConnection()
	{ 
		try {
		Scanner scn = new Scanner(System.in);
		// getting localhost ip 
		System.out.println("Enter server IP address: ");
		String addr = scn.nextLine();
		InetAddress ip = InetAddress.getByName(addr); 
		
		// establish the connection 
		Socket s = new Socket(ip, ServerPort);
		(new Thread(){public void run() {
			try {
				while (true) {
					ObjectInputStream ois = new ObjectInputStream(
				            s.getInputStream());
				    list = (HashMap<String, String[]>) ois.readObject();
				}
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
		}}).start();
		DataOutputStream dos = new DataOutputStream(s.getOutputStream());
		System.out.println("Enter username: ");
		username = scn.nextLine();
		dos.writeUTF(username);
		DatagramSocket ds = new DatagramSocket(s.getLocalPort());
		ChatListener r = new ChatListener(ds,this); 
		Thread t = new Thread(r);
		t.start();
		String peer;
		while (true) {
			System.out.println("Who do you want to talk to ?");
			peer = scn.nextLine();
			String[] p = list.get(peer);
            if (p == null) {
            	System.out.println("Wrong username");
            	continue;
            }
            String ipaddress = p[0]; 
          	String port = p[1];
          	String sa = "/"+ipaddress+":"+port;
          	boolean breakFlag = false;

          	for (ChatManager x : cm) {
				if (x.getSocketAddress().equals(sa)) {
					breakFlag = true;
					break;
				}
			}
  
          	if (!breakFlag) {
			ChatManager c = new ChatManager(peer,ipaddress,port,ds,username);
			cm.add(c);
			c.start();
          	}
		}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	} 
}

