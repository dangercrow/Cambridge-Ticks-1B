package uk.ac.cam.sy321.fjava.tick0;

public class Tester {
	public static void main(String[] args) throws Exception{
		String baseloc = "C:/Users/Admin/Dropbox/Java/WSP/Ticks_1B/test-suite/test";
		int[] test = {11,17};
		for (int i : test){
			String[] s = {baseloc+i+"a.dat", baseloc+i+"b.dat"};
			System.out.print("Test "+i+": ");
			ExternalSort.main(s);
		}
	}
}
