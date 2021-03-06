package uk.ac.cam.sy321.fjava.tick4;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import uk.ac.cam.cl.fjava.messages.Message;

public class ChatServer {
	// Due to socket technically never being closed (only closed on VM close)
	@SuppressWarnings("resource")
	public static void main(String args[]) {
		int port;

		// BEGIN Command line parsing
		try{
			port = Integer.parseInt(args[0]);
		}
		catch (NumberFormatException| ArrayIndexOutOfBoundsException e) {
			System.out.println("Usage: java ChatServer <port>");
			return;
		}
		// END Command line parsing

		// BEGIN Initialisation
		ServerSocket socket;
		try {
			socket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Cannot use port number " + port);
			return;
		}
		MultiQueue<Message> multiQueue = new MultiQueue<Message>();
		//END Initialisation

		assert socket != null;

		// BEGIN Loop
		while(true){
			Socket nextClient = null;
			try {
				nextClient = socket.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}
			assert nextClient != null;
			new ClientHandler(nextClient, multiQueue);
		}
		// END Loop
	}
}

