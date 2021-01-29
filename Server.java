package src2;

import java.io.*; 
import java.util.*; 
import java.net.*; 

// Server class 
public class Server 
{ 

	// Vector to store active clients 
	static Vector<ClientHandler> ar = new Vector<>(); 
	static HashMap<String, String[]> list = new HashMap<>();

	public static void main(String[] args) throws IOException 
	{ 
		// server is listening on port 1234 
		ServerSocket ss = new ServerSocket(1234); 
		
		// running infinite loop for getting 
		// client request 
		while (true) 
		{ 
			// Accept the incoming request 
			Socket s = ss.accept();
			
			System.out.println("New client request received : " + s); 
			
			System.out.println("Creating a new handler for this client..."); 

			// Create a new handler object for handling this request. 
			ClientHandler mtch = new ClientHandler(s); 

			// Create a new Thread with this object. 
			Thread t = new Thread(mtch); 
			
			System.out.println("Adding this client to active client list"); 

			// add this client to active clients list 
			ar.add(mtch); 
			
			// start the thread. 
			t.start(); 
		} 
	} 
} 

// ClientHandler class 
class ClientHandler implements Runnable 
{ 
	private Socket s; 
	
	// constructor 
	public ClientHandler(Socket s) {  
		this.s = s; 
	} 
	
	public Socket getSocket() {
		return s;
	}
	
	@Override
	public void run() { 
			try
			{ 
				ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());
				dos.writeObject(Server.list);
				DataInputStream dis = new DataInputStream(s.getInputStream());
	            String key = dis.readUTF();
	            String[] value = new String[2];
	            value[0] = s.getInetAddress().getHostAddress();
	            value[1] = Integer.toString(s.getPort());
	            Server.list.put(key, value);
	            for (ClientHandler mc : Server.ar) {
					dos = new ObjectOutputStream(mc.getSocket().getOutputStream());
					dos.writeObject(Server.list);
				}
			} catch (IOException e) { 
				e.printStackTrace(); 
			}
	}
} 

