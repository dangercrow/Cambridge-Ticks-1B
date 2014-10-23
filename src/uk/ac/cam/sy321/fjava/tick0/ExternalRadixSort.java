package uk.ac.cam.sy321.fjava.tick0;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author sy321
 * Implements the LSD Radix Sort algorithm by the <i>sort()</i> method.<br>
 * Runtime is O(n), but with large constant factors.<br>
 * Deemed functional, but not a realistically fast solution.
 */
public class ExternalRadixSort{
	
	/**
	 * Sorts the file at <b>pathA</b>, a file of 32-bit integers,
	 * using radix sort (LSD).
	 * Uses the file at <b>pathB</b> as temporary space.
	 * @param pathA - Path to file containing the items needing to be sorted.
	 * @param pathB - Path to file used as empty space. Must be of same size as file at <b>pathA</b>
	 * @throws IOException if there is an IO error in accessing files.
	 */
	public ExternalRadixSort(String pathA, String pathB) throws IOException{
		RandomAccessFile fileA = new RandomAccessFile(pathA, "rw");		
		RandomAccessFile fileB = new RandomAccessFile(pathB, "rw");
		int fileIntLength = (int) fileA.length()/4;
		FileDescriptor fileAFD = fileA.getFD();
		FileDescriptor fileBFD = fileB.getFD();
		DataInputStream readA = new DataInputStream(new BufferedInputStream(new FileInputStream(fileAFD)));
		DataInputStream readB = new DataInputStream(new BufferedInputStream(new FileInputStream(fileBFD)));
		DataOutputStream writeA = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileAFD)));
		DataOutputStream writeB = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileBFD)));

		for (int bit = 0; bit < 32; bit ++){
			fileA.seek(0);
			fileB.seek(0);
			
			// Note on 31st bit, sign bit means we prefer high bits as minimal
			boolean preferredBit = getBit(bit,0) ? bit == 31 : false;
			if(getBit(bit, 0))
			{ 
				// Bit 1, 3, etc
				radixHelper(fileIntLength, bit, readB, fileB, writeA, preferredBit);
			}
			else
			{ 
				// Bit 0, 2, etc
				radixHelper(fileIntLength, bit, readA, fileA, writeB, preferredBit);
			}
		}
		readA.close();
		readB.close();
		writeA.close();
		writeB.close();	}

	private boolean getBit(int integer, int position){
		return ((integer >> position) & 1) == 1;
	}

	private void radixHelper(int fileIntLength, int bit, DataInputStream reader, RandomAccessFile read, DataOutputStream writer, boolean favouredBit) throws IOException{
		int nextInt;
		for (int i = 0; i < fileIntLength; i ++)
		{
			nextInt = reader.readInt();
			if (getBit(nextInt, bit) == favouredBit)
			{ // Put favouredBit first
				writer.writeInt(nextInt);
			}
		}
		read.seek(0);
		for (int i = 0; i < fileIntLength; i ++)
		{
			nextInt = reader.readInt();
			if (getBit(nextInt, bit) != favouredBit)
			{ // Put all others after
				writer.writeInt(nextInt);
			}
		}
		// Ensure flush before next run
		writer.flush();
		return;
	}
}
