package erpoge;
import java.io.*;
import java.net.*;

class Connection extends Thread {
	DataInputStream in;
	DataOutputStream out;
	Socket clientSocket;
	
	public Connection (Socket aClientSocket) {
		try {
			clientSocket = aClientSocket;
			in = new DataInputStream( clientSocket.getInputStream());
			out = new DataOutputStream( clientSocket.getOutputStream());
			System.out.println("Connection started");
			
			this.start();
		} catch(IOException e){
				System.out.println("Connection:" + e.getMessage());
		}
	}
	public void run() { // an echo server
		System.out.println("Run...");
		try {
			String data = in.readUTF(); // read a line of data from the stream
			System.out.println("Message: " + data);
	//		clientSocket.close();
		} catch (EOFException e){
			System.out.println ("EOF:"+e.getMessage());
		} catch (IOException e) {
			System.out.println ("readline:"+e.getMessage());
		}
	}
}