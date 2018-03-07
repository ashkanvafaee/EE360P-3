/*
 * //UT-EID1= av28837
 * //UT-EID2= rg36763
 * 
 */
import java.util.Scanner;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;
public class BookClient {
  public static void main (String[] args) {
    String hostAddress;
    int tcpPort;
    int udpPort;
    int clientId;
    
    String mode = "U";
    Scanner din;
    PrintStream pout;
    Socket server;
    
    int len = 1024;
    byte [] buf = new byte[len];
    byte [] rbuf = new byte[len];

    
    
    if (args.length != 2) {
      System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
      System.out.println("\t(1) <command-file>: file with commands to the server");
      System.out.println("\t(2) client id: an integer between 1..9");
      System.exit(-1);
    }

    String commandFile = args[0];
    clientId = Integer.parseInt(args[1]);
    hostAddress = "localhost";
    tcpPort = 7000;// hardcoded -- must match the server's tcp port
    udpPort = 8000;// hardcoded -- must match the server's udp port

    
   
    
    try {
    	 PrintWriter out = new PrintWriter(new FileOutputStream("out_" + clientId + ".txt")); 
    	
        Scanner sc = new Scanner(new FileReader(commandFile));

        while(sc.hasNextLine()) {
          String cmd = sc.nextLine();
          String[] tokens = cmd.split(" ");

          if (tokens[0].equals("setmode")) {
            // TODO: set the mode of communication for sending commands to the server 
        	  mode = tokens[1];
          }
          else if (tokens[0].equals("borrow")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
				String book = tokens[2];
				
				for(int i=3; i < tokens.length; i++) {
					book += " " + tokens[i];
				}
        	  
        	  if(mode.equals("T")) {
        		  server = new Socket(InetAddress.getByName(hostAddress), tcpPort);
        		  //server = new Socket(hostAddress, tcpPort);

        		  din = new Scanner(server.getInputStream());
        		  pout = new PrintStream (server.getOutputStream());
        		  
        		  pout.println(tokens[0] + " " + tokens[1] + " " + book);
        		  
        		  out.println(din.nextLine()); 

        		  server.close();
        		 
        	  }
        	  else {
        		  
        		  DatagramSocket datasocket = new DatagramSocket();
        		  buf = new byte[(tokens[0] + " " + tokens[1] + " " + book).length()];
        		  buf = (tokens[0] + " " + tokens[1] + " " + book).getBytes();
        		  DatagramPacket datapacket,returnpacket;
        		  
        		  datapacket = new DatagramPacket(buf, buf.length, InetAddress.getByName(hostAddress), udpPort);
        		  
        		  datasocket.send(datapacket);
        		  
        		  
        		  
        		  returnpacket = new DatagramPacket(rbuf,rbuf.length);
        		  datasocket.receive(returnpacket);
        		  
        		  String received = new String(returnpacket.getData());
        		  received = received.substring(0, returnpacket.getLength());
        		  
        		  out.println(received);
        		  
        		  datasocket.close();
        		  
        		  
        	  }
        	  
        	  
          } else if (tokens[0].equals("return")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
        	  
        	  if(mode.equals("T")) {
        		  server = new Socket(InetAddress.getByName(hostAddress), tcpPort);
        		  pout = new PrintStream (server.getOutputStream());   	
        		  din = new Scanner(server.getInputStream());
        		  pout.println(tokens[0] + " " + tokens[1]);
        		  
        		  out.println(din.nextLine());
        		  server.close();
        	  }
        	  else {
        		  
        		  DatagramSocket datasocket = new DatagramSocket();
        		  buf = new byte[(tokens[0] + " " + tokens[1]).length()];
        		  buf = (tokens[0] + " " + tokens[1]).getBytes();
        		  DatagramPacket datapacket,returnpacket;
        		          		  
        		  datapacket = new DatagramPacket(buf, buf.length, InetAddress.getByName(hostAddress), udpPort);
        		  
        		  datasocket.send(datapacket);
        		  
        		  returnpacket = new DatagramPacket(rbuf,rbuf.length);
        		  datasocket.receive(returnpacket);
        		  
        		  String received = new String(returnpacket.getData());
        		  received = received.substring(0, returnpacket.getLength());
        		  
        		  out.println(received);
        		  
        		  datasocket.close();
        		  
        	  }
        	  
          } else if (tokens[0].equals("inventory")) {
            // TODO: send appropriate command to the server and display the
        	  
        	  if(mode.equals("T")) {
        		  server = new Socket(InetAddress.getByName(hostAddress), tcpPort);
        		  pout = new PrintStream (server.getOutputStream());   	
        		  din = new Scanner(server.getInputStream());
        		  pout.println(tokens[0]);

        		  
        		  String s = din.nextLine();
        		  
        		  while(!s.equals("done")) {
        			  out.println(s);
        			  s = din.nextLine();
        		  }
        		  
        		  server.close();
        		  
        	  }
        	  else {
        		  
        		  DatagramSocket datasocket = new DatagramSocket();
        		  buf = new byte[(tokens[0]).length()];
        		  buf = (tokens[0]).getBytes();
        		  DatagramPacket datapacket,returnpacket;
        		  
        		  datapacket = new DatagramPacket(buf, buf.length, InetAddress.getByName(hostAddress), udpPort);        		  
        		  
        		  datasocket.send(datapacket);
        		  
        		  returnpacket = new DatagramPacket(rbuf,rbuf.length);
        		  datasocket.receive(returnpacket);
        		  
        		  String s = new String(returnpacket.getData());
        		  s = s.substring(0, returnpacket.getLength());
        		  
        		  while (!s.equals("done")) {
        			  out.println(s);
        			  datasocket.receive(returnpacket);
        			  s = new String(returnpacket.getData());
        			  s = s.substring(0, returnpacket.getLength());
        		  }
        		  
        		  
        		  datasocket.close();
        		  
        	  }
        	  
            // appropriate responses form the server
          } else if (tokens[0].equals("list")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
        	
        	  if(mode.equals("T")) {
        		  server = new Socket(InetAddress.getByName(hostAddress), tcpPort);
        		  pout = new PrintStream (server.getOutputStream());   	
        		  din = new Scanner(server.getInputStream());
        		  pout.println(tokens[0] + " " + tokens[1]);
        		  
        		  String s = din.nextLine();
        		  
        		  if(!s.contains("\"") && !s.equals("done")) {
        			  out.println(s);
        		  }
        		  
        		  else {
            		  while(!s.equals("done")) {
            			  out.println(s);
            			  s = din.nextLine();
            		  }
        		  }

        		  server.close();
        	  }
        	  else {
        		  DatagramSocket datasocket = new DatagramSocket();
        		  buf = new byte[(tokens[0] + " " + tokens[1]).length()];
        		  buf = (tokens[0] + " " + tokens[1]).getBytes();
        		  DatagramPacket datapacket,returnpacket;
        		  
        		  datapacket = new DatagramPacket(buf, buf.length, InetAddress.getByName(hostAddress), udpPort);
        		  
        		  datasocket.send(datapacket);
        		  
        		  
        		  returnpacket = new DatagramPacket(rbuf,rbuf.length);
        		  datasocket.receive(returnpacket);
        		  
        		  String s = new String(returnpacket.getData());
        		  s = s.substring(0,returnpacket.getLength());
        		  
        		  if(!s.contains("\"") && !s.equals("done")) {
        			  out.println(s);
        		  }
        		  else {
        			  while(!s.equals("done")) {
        				  out.println(s);
        				  returnpacket = new DatagramPacket(rbuf,rbuf.length);
        				  datasocket.receive(returnpacket);
        				  s = new String(returnpacket.getData());
        				  s = s.substring(0,returnpacket.getLength());
        				  
        			  }
        		  }
        		  
        		  
        		  datasocket.close();
        		  
        	  }
        	  
        	  
          } else if (tokens[0].equals("exit")) {
            // TODO: send appropriate command to the server 
        	  if(mode.equals("T")) {
        		  server = new Socket(InetAddress.getByName(hostAddress), tcpPort);
        		  pout = new PrintStream (server.getOutputStream());   	
        		  pout.println(tokens[0]);
        		  
        		  server.close();
        		  out.close();
        		  return;
        	  
        	  }
        	  else {      		  
        		  DatagramSocket datasocket = new DatagramSocket();
        		  buf = new byte[(tokens[0]).length()];
        		  buf = (tokens[0]).getBytes();
        		  DatagramPacket datapacket;
        		  
        		  datapacket = new DatagramPacket(buf, buf.length, InetAddress.getByName(hostAddress), udpPort);
        		  
        		  datasocket.send(datapacket);                		  
        		  
        		  out.close();
        		  datasocket.close();
        		  return;
        	  }
        	  
          } else {
            System.out.println("ERROR: No such command");
          }
        }
    } catch (Exception e) {
	e.printStackTrace();
    }
  }
}
