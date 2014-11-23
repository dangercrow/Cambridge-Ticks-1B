package uk.ac.cam.sy321.fjava.tick5;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import uk.ac.cam.cl.fjava.messages.ChangeNickMessage;
import uk.ac.cam.cl.fjava.messages.ChatMessage;
import uk.ac.cam.cl.fjava.messages.Message;
import uk.ac.cam.cl.fjava.messages.RelayMessage;
import uk.ac.cam.cl.fjava.messages.StatusMessage;

public class ClientHandler {
	private Socket socket;
	private MultiQueue<Message> multiQueue;
	private String nickname;
	private MessageQueue<Message> clientMessages;
	private final Database database;

	public ClientHandler(Socket s, MultiQueue<Message> q, final Database db) {
		// BEGIN Initialisation 
		socket = s;
		multiQueue = q;
		nickname = "Anonymous" + 
				String.format("%05d", (new Random()).nextInt(100000));
		clientMessages = new SafeMessageQueue<Message>();
		q.register(clientMessages);
		database = db;
		try {
			db.incrementLogins();
		} catch (SQLException e2) {
			e2.printStackTrace();
		}
		// END Initialisation

		// BEGIN input thread
		final Thread incoming = new Thread(){
			@Override
			public void run() {
				ObjectInputStream o = null;
				try {
					o = new ObjectInputStream(socket.getInputStream());
				} catch (IOException e) {
					e.printStackTrace();
				}
				assert o!=null;
				while(true){
					try {
						Object message = o.readObject();
						if(message instanceof ChatMessage){
							RelayMessage m = new RelayMessage(nickname,(ChatMessage) message);
							multiQueue.put(m);
							try {
								database.addMessage(m);
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
						else if(message instanceof ChangeNickMessage){
							String newNick = ((ChangeNickMessage) message).name;
							multiQueue.put(new StatusMessage(nickname+" is now known as "+newNick+"."));
							nickname = newNick;
						}
						else if(message instanceof StatusMessage){
							multiQueue.put((StatusMessage) message);
						}
						else{
							// This never happens since clients 
							// don't send non-standard messages
							assert false;
						}
					} catch (IOException e) {
						multiQueue.put(new StatusMessage(nickname + " has disconnected."));
						multiQueue.deregister(clientMessages);
						return;
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		};
		incoming.start();
		// END input thread

		// BEGIN output thread
		final Thread outgoing = new Thread(){
			@Override
			public void run() {
				ObjectOutputStream o = null;
				try {
					o = new ObjectOutputStream(socket.getOutputStream());
				} catch (IOException e) {
					e.printStackTrace();
				}
				assert o!=null;
				try {
					List<RelayMessage> recent = database.getRecent();
					for(int i = recent.size() - 1; i>=0; i-- ){
						o.writeObject(recent.get(i));
					}
				} catch (SQLException | IOException e1) {
					e1.printStackTrace();
				}
				multiQueue.put(new StatusMessage(nickname + " connected from " 
						+ socket.getInetAddress().getHostName() + "."));
				while(incoming.isAlive()){
					try {
						o.writeObject(clientMessages.take());
					} catch (SocketException e) {
						if(!incoming.isAlive()){
							return;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
		outgoing.start();
		// END output thread

	}
}

