package uk.ac.cam.sy321.fjava.tick0;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class ExternalSort {

	/**
	 * Sorts the file in <b>f1</b> using the file in <b>f2</b> as temporary storage space.
	 * Sorts using an external algorithm from a different class if necessary. 
	 * @param f1 - Path to the file containing the unsorted integers 
	 * @param f2 - Path to the file to be used as free space, same size as the file in <b>f1</b>
	 * @throws FileNotFoundException If the file at <b>f1</b> or <b>f2</b> is not found
	 * @throws IOException If a read/write error occurs
	 */
	public static void sort(String f1, String f2) throws FileNotFoundException, IOException {
		RandomAccessFile fileA = new RandomAccessFile(f1, "rw");
		int fileIntLength = (int) fileA.length()/4;
		// Sort small files in memory
		if (fileIntLength < ((int) Runtime.getRuntime().freeMemory())/12){
			byte[] bytes = new byte[fileIntLength*4];
			fileA.readFully(bytes);
			int[] ints = new int[fileIntLength];
			ByteBuffer.wrap(bytes).asIntBuffer().get(ints);
			Arrays.sort(ints);
			ByteBuffer.wrap(bytes).asIntBuffer().put(ints);
			fileA.seek(0);
			fileA.write(bytes);
			fileA.close();
			return;
		}
		fileA.close();
		new ExternalHybridSort(f1, f2);
		return;
	}

	private static String byteToHex(byte b) {
		String r = Integer.toHexString(b);
		if (r.length() == 8) {
			return r.substring(6);
		}
		return r;
	}

	public static String checkSum(String f) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			DigestInputStream ds = new DigestInputStream(
					new FileInputStream(f), md);
			byte[] b = new byte[512];
			while (ds.read(b) != -1);
			String computed = "";
			for(byte v : md.digest()) 
				computed += byteToHex(v);

			ds.close();
			return computed;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "<error computing checksum>";
	}

	public static void main(String[] args) throws Exception {
		String f1 = args[0];
		String f2 = args[1];
		sort(f1, f2);
		System.out.println("The checksum is: "+checkSum(f1));
	}
}