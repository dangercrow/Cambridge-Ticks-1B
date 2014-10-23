package uk.ac.cam.sy321.fjava.tick2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.ac.cam.cl.fjava.messages.ChangeNickMessage;
import uk.ac.cam.cl.fjava.messages.ChatMessage;
import uk.ac.cam.cl.fjava.messages.RelayMessage;
import uk.ac.cam.cl.fjava.messages.StatusMessage;

public class ChatClient {
	@SuppressWarnings("resource")
	public static void main(final String[] args) throws IOException, InterruptedException {
		String server = null;
		int port = 0;
		final Socket s;
		String errorMessage = "This application requires two arguments: <machine> <port>";
		if (args.length!=2){
			System.err.println(errorMessage);
			return;
		}
		server = args[0];
		try{
			port = Integer.parseInt(args[1]);
		}
		catch(NumberFormatException e){
			System.err.println(errorMessage);
			return;
		}
		try{
			s = new Socket(server,port);
		}
		catch(ConnectException e){
			System.err.println("Cannot connect to " + server + " on port " + port);
			return;
		}
		final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		final ObjectInputStream in = new ObjectInputStream(s.getInputStream());
		final ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
		Thread output = new Thread() {
			@Override
			public void run() {
				try{
					while(true){
						Object msgIn = in.readObject();
						if(msgIn instanceof StatusMessage){
							StatusMessage msg = (StatusMessage) msgIn;
							System.out.println(dateFormat.format(msg.getCreationTime()) +
									" [Server] " + msg.getMessage());
							continue;
						}
						if(msgIn instanceof RelayMessage){
							RelayMessage msg = (RelayMessage) msgIn;
							System.out.println(dateFormat.format(msg.getCreationTime()) +
									" [" + msg.getFrom() + "] " + msg.getMessage());
							continue;
						}

					}
				}
				catch(IOException e){
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		};
		output.setDaemon(true);
		output.start();
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));

		//TODO
		final Date dateTime = new Date();
		String userInput="";
		while(true) {
			userInput = r.readLine();
			if(userInput.startsWith("\\")){
				//Command
				String[] command = userInput.split(" ");
				if(command[0].equalsIgnoreCase("\\quit")){
					System.out.println(dateFormat.format(dateTime) +
							" [Client] Connection terminated.");
					return;
				}
				else if(command[0].equalsIgnoreCase("\\nick")){
					//TODO change nick
					if(command.length!=2){
						System.out.println(dateFormat.format(dateTime) +
								" [Client] Incorrect command. \\nick [new nick]");
						continue;
					}
					ChangeNickMessage msg = new ChangeNickMessage(command[1]);
					out.writeObject(msg);
					continue;
				}
				else{
					System.out.println(dateFormat.format(dateTime) +
							" [Client] Unknown command \"" + command[0].substring(1) +"\"");
					continue;
				}
			}
			out.writeObject(new ChatMessage(userInput));
		}
	}
}


