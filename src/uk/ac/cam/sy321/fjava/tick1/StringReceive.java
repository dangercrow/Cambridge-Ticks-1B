package uk.ac.cam.sy321.fjava.tick1;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.Socket;

public class StringReceive {
	public static void main(String[] args) throws IOException{
		String machine;
		Socket s = null;
		int port;
		String errorMessage = "This application requires two arguments: <machine> <port>";
		if (args.length!=2){
			System.err.println(errorMessage);
			return;
		}
		machine = args[0];
		try{
			port = Integer.parseInt(args[1]);
		}
		catch(NumberFormatException e){
			System.err.println(errorMessage);
			return;
		}
		try {
			s = new Socket(machine, port);
			InputStream in = s.getInputStream();
			while(true){
				System.out.print((char) in.read());
			}
		}catch (ConnectException e) {
			System.err.println("Cannot connect to " + machine + " on port " + port);
			if (s!= null) s.close();
			return;
		}
	}
}
