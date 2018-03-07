/*
 * //UT-EID1= av28837
 * //UT-EID2= rg36763
 * 
 */
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class BookServer extends Thread{
	static Map<String, Integer> inventory = Collections.synchronizedMap(new HashMap<String, Integer>());
	static Map<String, ArrayList<Integer>> userRecord = Collections.synchronizedMap(new HashMap<String, ArrayList<Integer>>());
	static Map<Integer, String> IdToBook = Collections.synchronizedMap(new HashMap<Integer, String>());
	static Map<Integer, String> IdToUser = Collections.synchronizedMap(new HashMap<Integer, String>());
	static ArrayList<String> bookOrder = new ArrayList<>();
	static BookServer lock = new BookServer();

	public Socket s;
	static AtomicInteger record_id = new AtomicInteger(1);
	
	public static DatagramSocket datasocket;
	
	
	public static void main(String[] args) {
		
		int tcpPort;
		int udpPort;
		if (args.length != 1) {
			System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
			System.exit(-1);
		}
		String fileName = args[0];
		tcpPort = 7000;
		udpPort = 8000;

		// parse the inventory file
		BufferedReader br;
		try {
			datasocket = new DatagramSocket(udpPort);
			br = new BufferedReader(new FileReader(fileName));

			String currentLine;

			while ((currentLine = br.readLine()) != null) {
				inventory.put(currentLine.substring(0, currentLine.lastIndexOf("\"") + 1), Integer.parseInt(currentLine.substring(currentLine.lastIndexOf("\"") + 1).trim()));
				bookOrder.add(currentLine.substring(0, currentLine.lastIndexOf("\"") + 1));
			}
		} catch (IOException e) {
		}
		
		BookServer bs = new BookServer();
		BookServer.UDPServer udps = bs.new UDPServer();
		udps.spin = true;
		Thread t_udp = udps;
		t_udp.start();

		
		Socket localS;
		
		
		try {
			ServerSocket listener = new ServerSocket (tcpPort);
			while((localS = listener.accept()) != null) {
	
				//System.out.println("connected");
				
				BookServer temp = new BookServer();
				temp.s = localS;
				
				Thread t = temp;
				t.start();
				
			}
	
		} catch (IOException e) {
			
		}
		

		// TODO: handle request from clients
	}
	
	// TCP
	public void run() {
		synchronized(lock) {
			
			try {
				Scanner sc = new Scanner (s.getInputStream());
				PrintStream pout = new PrintStream(s.getOutputStream());
				String command = sc.nextLine();
				
				String [] tokens = command.split(" ");
				
				if(tokens[0].equals("borrow")) {
					String book = tokens[2];
					
					for(int i=3; i < tokens.length; i++) {
						book += " " + tokens[i];
					}
					
					if(inventory.get(book) == null) {	
						pout.println("Request Failed - We do not have this book");
					}
					else if(inventory.get(book) == 0) {
						pout.println("Request Failed - Book not available");
					}
					
					else {
						pout.println("Your request has been approved, " + record_id.get() + " " + tokens[1] + " " + book);
						
						inventory.replace(book, inventory.get(book) - 1);
						IdToBook.put(record_id.get(), book);						// Map ID to book
						
						if(userRecord.containsKey(tokens[1])) {						// Place ID in user's checked-out list
							userRecord.get(tokens[1]).add(record_id.get());
						}
						else {
							userRecord.put(tokens[1], new ArrayList<Integer>());
							userRecord.get(tokens[1]).add(record_id.get());
						}
						
						IdToUser.put(record_id.get(), tokens[1]);
						record_id.set(record_id.get() + 1);	
					}
					
					
					s.close();
				}
				
				
				
				else if(tokens[0].equals("return")) {
					
					if(!IdToBook.containsKey(Integer.parseInt(tokens[1]))) {
						pout.println(tokens[1] + " " + "not found, no such borrow record");
					}
					else {
						inventory.replace(IdToBook.get(Integer.parseInt(tokens[1])), inventory.get(IdToBook.get(Integer.parseInt(tokens[1]))) + 1);
						userRecord.get(IdToUser.get(Integer.parseInt(tokens[1]))).remove((Object)Integer.parseInt(tokens[1]));
						pout.println(tokens[1] + " " + "is returned");
					}
					
					s.close();
			
				}
				
				else if(tokens[0].equals("list")) {
					
					if(!userRecord.containsKey(tokens[1])) {
						pout.println("No record found for " + tokens[1]);
					}
					else {
						for(int i = 0; i < userRecord.get(tokens[1]).size(); i++) {
							pout.println( userRecord.get(tokens[1]).get(i) + " " + IdToBook.get(userRecord.get(tokens[1]).get(i)));
						}
						pout.println("done");
					}
					
					s.close();
				}
				
				
				else if(tokens[0].equals("inventory")) {			
					
					for(int i=0; i<bookOrder.size(); i++) {
						pout.println(bookOrder.get(i) + " " + inventory.get(bookOrder.get(i)));
					}
					
					pout.println("done");
					s.close();
				}
				
				else {
					PrintWriter out = new PrintWriter(new FileOutputStream("inventory.txt")); 
					
					for(int i=0; i<bookOrder.size(); i++) {
						out.println(bookOrder.get(i) + " " + inventory.get(bookOrder.get(i)));					
					}
					
					out.close();
					s.close();
				}
				
				
			} catch (IOException e) {
			}
			
		}
		
	}
	
	
	class UDPServer extends Thread{
		int len = 1024;
		DatagramPacket datapacket, returnpacket;
		byte [] buf = new byte [len];		
		Boolean spin = false;
		
		
		// UDP
		public void run() {
			if(spin) {
				try {				
					while(true) {			
						datapacket = new DatagramPacket(buf, buf.length);
						datasocket.receive(datapacket);	
						
						UDPServer temp = new UDPServer();
						temp.datapacket = datapacket;
						temp.start();
					}
			
					
				} catch (Exception e) {
				}
							
			}
			
			
			synchronized (lock) {
				
				try {
					
					
					String command = new String(datapacket.getData());
					command = command.substring(0, datapacket.getLength());
					
					String [] tokens = command.split(" ");
					
					if(tokens[0].equals("borrow")) {
						String book = tokens[2];
						
						for(int i=3; i < tokens.length; i++) {
							book += " " + tokens[i];
						}
						
						if(inventory.get(book) == null) {	
							//pout.println("Request Failed - We do not have this book");
							buf = new byte ["Request Failed - We do not have this book".length()];
							buf = "Request Failed - We do not have this book".getBytes();
							returnpacket = new DatagramPacket(buf, buf.length, datapacket.getAddress(), datapacket.getPort());
							datasocket.send(returnpacket);
						}
						else if(inventory.get(book) == 0) {
							//pout.println("Request Failed - Book not available");
							buf = new byte ["Request Failed - Book not available".length()];
							buf = "Request Failed - Book not available".getBytes();
							returnpacket = new DatagramPacket(buf, buf.length, datapacket.getAddress(), datapacket.getPort());
							datasocket.send(returnpacket);
						}
						
						else {
							//pout.println("Your request has been approved, " + record_id.get() + " " + tokens[1] + " " + book);
							
							buf = new byte [("Your request has been approved, " + record_id.get() + " " + tokens[1] + " " + book).length()];
							buf = ("Your request has been approved, " + record_id.get() + " " + tokens[1] + " " + book).getBytes();
							returnpacket = new DatagramPacket(buf, buf.length, datapacket.getAddress(), datapacket.getPort());
							
							
							inventory.replace(book, inventory.get(book) - 1);
							IdToBook.put(record_id.get(), book);						// Map ID to book
							
							if(userRecord.containsKey(tokens[1])) {						// Place ID in user's checked-out list
								userRecord.get(tokens[1]).add(record_id.get());
							}
							else {
								userRecord.put(tokens[1], new ArrayList<Integer>());
								userRecord.get(tokens[1]).add(record_id.get());
							}
							
							datasocket.send(returnpacket);
							IdToUser.put(record_id.get(), tokens[1]);
							record_id.set(record_id.get() + 1);	
						}				
					}
					
					
					
					else if(tokens[0].equals("return")) {
						
						if(!IdToBook.containsKey(Integer.parseInt(tokens[1]))) {
							//pout.println(tokens[1] + " " + "not found, no such borrow record");
							buf = new byte [(tokens[1] + " " + "not found, no such borrow record").length()];
							buf = (tokens[1] + " " + "not found, no such borrow record").getBytes();
							returnpacket = new DatagramPacket(buf, buf.length, datapacket.getAddress(), datapacket.getPort());
							datasocket.send(returnpacket);
						}
						else {
							inventory.replace(IdToBook.get(Integer.parseInt(tokens[1])), inventory.get(IdToBook.get(Integer.parseInt(tokens[1]))) + 1);
							userRecord.get(IdToUser.get(Integer.parseInt(tokens[1]))).remove((Object)Integer.parseInt(tokens[1]));
							//pout.println(tokens[1] + " " + "is returned");
							buf = new byte [(tokens[1] + " " + "is returned").length()];
							buf = (tokens[1] + " " + "is returned").getBytes();
							returnpacket = new DatagramPacket(buf, buf.length, datapacket.getAddress(), datapacket.getPort());
							datasocket.send(returnpacket);
						}
				
					}
					
					else if(tokens[0].equals("list")) {
						
						if(!userRecord.containsKey(tokens[1])) {
							//pout.println("No record found for " + tokens[1]);
							buf = new byte [("No record found for " + tokens[1]).length()];
							buf = ("No record found for " + tokens[1]).getBytes();
							returnpacket = new DatagramPacket(buf, buf.length, datapacket.getAddress(), datapacket.getPort());
							datasocket.send(returnpacket);
						}
						else {
							for(int i = 0; i < userRecord.get(tokens[1]).size(); i++) {
								//pout.println( userRecord.get(tokens[1]).get(i) + " " + IdToBook.get(userRecord.get(tokens[1]).get(i)));
								buf = new byte [( userRecord.get(tokens[1]).get(i) + " " + IdToBook.get(userRecord.get(tokens[1]).get(i))).length()];
								buf = ( userRecord.get(tokens[1]).get(i) + " " + IdToBook.get(userRecord.get(tokens[1]).get(i))).getBytes();
								returnpacket = new DatagramPacket(buf, buf.length, datapacket.getAddress(), datapacket.getPort());
								datasocket.send(returnpacket);
							}
							//pout.println("done");
							buf = new byte [("done").length()];
							buf = ("done").getBytes();
							returnpacket = new DatagramPacket(buf, buf.length, datapacket.getAddress(), datapacket.getPort());
							datasocket.send(returnpacket);
						}		
					}
					
					
					else if(tokens[0].equals("inventory")) {			
						
						for(int i=0; i<bookOrder.size(); i++) {
							//pout.println(bookOrder.get(i) + " " + inventory.get(bookOrder.get(i)));
							buf = new byte [(bookOrder.get(i) + " " + inventory.get(bookOrder.get(i))).length()];
							buf = (bookOrder.get(i) + " " + inventory.get(bookOrder.get(i))).getBytes();
							returnpacket = new DatagramPacket(buf, buf.length, datapacket.getAddress(), datapacket.getPort());
							datasocket.send(returnpacket);
						}
						
						//pout.println("done");
						buf = new byte [("done").length()];
						buf = ("done").getBytes();
						returnpacket = new DatagramPacket(buf, buf.length, datapacket.getAddress(), datapacket.getPort());
						datasocket.send(returnpacket);
					}
					
					else {
						PrintWriter out = new PrintWriter(new FileOutputStream("inventory.txt")); 
						
						for(int i=0; i<bookOrder.size(); i++) {
							out.println(bookOrder.get(i) + " " + inventory.get(bookOrder.get(i)));					
						}
						
						out.close();
					}
					

					
					
				} catch (IOException e) {
				}
				
			}

		}
		
	}	
	
}
