package uk.ac.cam.sy321.fjava.tick1;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;

public class StringChat {
	public static void main(final String[] args) throws IOException, InterruptedException {
		String server = null;
		int port = 0;
		final Socket s;
		// "s" is declared as final so it can only be assigned once, as changing the socket
		// mid-run makes no sense, as this class is only intended for one server at a time.
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
		final DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
		final OutputStream out = s.getOutputStream();
		Thread output = new Thread() {
			@Override
			public void run() {
				try{
					while(true){
						if(in.available() > 0){
							System.out.print((char) in.read());
						}
					}
				}
				catch(IOException e){
					e.printStackTrace();
				}
			}
		};
		output.setDaemon(true);
		output.start();
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		String userInput="";
		while(!userInput.equalsIgnoreCase("/quit")) {
			userInput = r.readLine();
			char[] chars = userInput.toCharArray();
			byte[] bytes = new byte[chars.length];
			for (int i = 0; i<chars.length; i++){
				bytes[i] = (byte) chars[i];
			}
			out.write(bytes);
		}
		s.close();
	}
}

