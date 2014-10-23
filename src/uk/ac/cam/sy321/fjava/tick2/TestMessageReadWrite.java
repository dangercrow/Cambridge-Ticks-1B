package uk.ac.cam.sy321.fjava.tick2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;

class TestMessageReadWrite {
	static boolean writeMessage(String message, String filename){
		try{
			TestMessage msg = new TestMessage();
			msg.setMessage(message);
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
			out.writeObject(msg);
			out.close();
			return true;
		}
		catch (Exception e){
			return false;
		}
	}
	static String readMessage(String location) {
		try{
			if(location.matches("^http://.*")){
				// HTTP
				ObjectInputStream in = new ObjectInputStream((new URL(location)).openConnection().getInputStream());
				TestMessage msg = (TestMessage) in.readObject();
				in.close();
				return msg.getMessage();

			}
			else
			{
				// File
				InputStream file = new FileInputStream(location);
				TestMessage msg = (TestMessage) new ObjectInputStream(file).readObject();
				file.close();
				return msg.getMessage();				
			}
		}
		catch (Exception e){
			return null;
		}
	}
	public static void main(String args[]) throws IOException {
		boolean result = true;
		for (int i = 0; i < 100; i++){
		File temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
		result = result && testMessage("someMessage" + i, temp.getCanonicalPath());
		temp.delete();
		}
		System.out.println(result);
	}
	
	public static boolean testMessage(String msg, String filename){
		if(writeMessage(msg, filename)){
			return readMessage(filename).equals(msg);
		}
		return false;
		
	}
}