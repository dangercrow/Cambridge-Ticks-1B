package uk.ac.cam.sy321.fjava.tick0;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.PriorityQueue;

public class ExternalHybridSort {

	/**
	 * Sorts the integer file at <b>pathA</b>, using <b>pathB</b> as a tempfile.
	 * @param pathA - Path to a file containing integers to be sorted
	 * @param pathB - Path to a file of the same size as <b>pathA</b>, used as temporary storage 
	 * @throws IOException
	 */
	public ExternalHybridSort(String pathA, String pathB)
			throws IOException{
		Runtime r = Runtime.getRuntime();
		if(r.freeMemory() < r.totalMemory() / 2){
			System.gc();
		}

		//Sort pathA into slices in pathB
		int[]sliceInfo = sortToSlices(pathA, pathB);

		// We now have slices which we can merge.
		mergeSlices(pathA, pathB, sliceInfo);
	}

	private int[] sortToSlices(String pathA, String pathB)
			throws IOException{	
		RandomAccessFile fileA = new RandomAccessFile(pathA,"r");
		RandomAccessFile fileB = new RandomAccessFile(pathB,"rw");

		int freeIntMemory = (int) ( (int) Runtime.getRuntime().freeMemory()/12); // Save a little for overhead
		int fileIntLength = (int) fileA.length() / 4;

		boolean incompleteSlice = fileIntLength % freeIntMemory != 0;
		int[] sliceInfo = new int[(fileIntLength / freeIntMemory) + (incompleteSlice ? 1 : 0)]; // Stores the pieces end positions.

		int sortedInts = 0;
		int sliceCount = 0;

		while(sortedInts < fileIntLength){
			int intsLeft = fileIntLength - sortedInts;
			int actualSize = (freeIntMemory > intsLeft) ? intsLeft : freeIntMemory;
			sliceInfo[sliceCount] = sortedInts;
			byte[] bytes = new byte[actualSize*4];
			fileA.read(bytes);
			int[] ints = new int[actualSize];
			ByteBuffer.wrap(bytes).asIntBuffer().get(ints);
			Arrays.sort(ints);
			ByteBuffer.wrap(bytes).asIntBuffer().put(ints);
			fileB.write(bytes);
			sortedInts += actualSize;
			sliceCount++;
		}
		fileA.close();
		fileB.close();
		return sliceInfo;
	}

	private void mergeSlices(String pathA, String pathB, int[] sliceInfo)
			throws IOException {
		RandomAccessFile fileA = new RandomAccessFile(pathA, "rw");
		int fileIntLength = (int) (fileA.length() / 4);
		DataOutputStream writeA = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileA.getFD())));

		PriorityQueue<FileQueue> QPQueue = new PriorityQueue<FileQueue>(sliceInfo.length);
		int sliceMem = (int) Runtime.getRuntime().freeMemory() / (5 * sliceInfo.length);
		for (int i = 0; i <sliceInfo.length; i++){
			int readLimit = (i == sliceInfo.length-1) ? fileIntLength : sliceInfo[i+1];
			QPQueue.offer(new BufferedFileQueue(pathB, sliceInfo[i], readLimit, sliceMem));
		}

		// Get minimum from the queue
		for (int i = 0; i < fileIntLength; i++){
			FileQueue head = QPQueue.poll();
			Integer val = head.popInt();
			if (head.peekInt()!=null)
			{
				QPQueue.offer(head);
			}
			else{
				head.close();
			}
			writeA.writeInt(val);
		}

		writeA.close();
		fileA.close();
	}
}
