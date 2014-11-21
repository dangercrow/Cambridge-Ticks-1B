package uk.ac.cam.sy321.fjava.tick2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.ac.cam.cl.fjava.messages.ChangeNickMessage;
import uk.ac.cam.cl.fjava.messages.ChatMessage;
import uk.ac.cam.cl.fjava.messages.DynamicObjectInputStream;
import uk.ac.cam.cl.fjava.messages.Execute;
import uk.ac.cam.cl.fjava.messages.NewMessageType;
import uk.ac.cam.cl.fjava.messages.RelayMessage;
import uk.ac.cam.cl.fjava.messages.StatusMessage;
import uk.ac.cam.sy321.fjava.tick2.FurtherJavaPreamble.Ticker;

@FurtherJavaPreamble(
		author = "Sahil Youngs",
		crsid = "sy321",
		date = "24th October 2014",
		summary = "Plaintext Chat Client",
		ticker = Ticker.A
		)

public class ChatClient{
	@SuppressWarnings("resource")
	public static void main(final String[] args) throws IOException, InterruptedException {
		String server = null;
		int port = 0;
		final Socket s;
		final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		final Date dateTime = new Date();
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
		System.out.println(dateFormat.format(dateTime) +
				" [Client] Connected to "+server+" on port "+port+".");
		final DynamicObjectInputStream in = new DynamicObjectInputStream(s.getInputStream());
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
						}else if(msgIn instanceof RelayMessage){
							RelayMessage msg = (RelayMessage) msgIn;
							System.out.println(dateFormat.format(msg.getCreationTime()) +
									" [" + msg.getFrom() + "] " + msg.getMessage());
						}else if(msgIn instanceof NewMessageType){
							NewMessageType msg = (NewMessageType) msgIn;
							in.addClass(msg.getName(), msg.getClassData());
							System.out.println(dateFormat.format(msg.getCreationTime()) +
									" [Client] New class " + msg.getName() + " loaded.");
						}else{
							// Unknown type
							Class<?> newClass = msgIn.getClass();
							System.out.print(dateFormat.format(dateTime) + " [Client] ");
							System.out.print(newClass.getSimpleName()+": ");
							String fields = new String();
							for (Field i : newClass.getDeclaredFields()){
								i.setAccessible(true);
								fields = fields + i.getName() +"("+i.get(msgIn)+"), ";
							}
							System.out.println(fields.substring(0, fields.length()-2));
							// Invoke methods
							for(Method i : newClass.getMethods()){
								if(i.getParameterTypes().length == 0 && i.isAnnotationPresent(Execute.class)){
									i.invoke(msgIn, (Object[]) null);
								}
							}
						}
					}
				}
				catch(IOException e){
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		};
		output.setDaemon(true);
		output.start();
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));

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


