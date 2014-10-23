package uk.ac.cam.sy321.fjava.tick1;

public class HelloWorld {
	public static void main(String[] args) {
		System.out.println("Hello, " + (args.length==1 ? args[0] : "world"));
	}
}
