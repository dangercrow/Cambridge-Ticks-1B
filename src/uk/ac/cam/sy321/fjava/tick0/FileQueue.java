package uk.ac.cam.sy321.fjava.tick0;

import java.io.IOException;
import java.io.RandomAccessFile;

public abstract class FileQueue implements Comparable<FileQueue>{

	protected RandomAccessFile file;
	
	/**
	 * Retrieves the first Integer in the queue without removing it.
	 * @return the next Integer in the queue. If the queue is empty, returns <b>null</b>
	 */
	public abstract Integer peekInt();
	
	/** 
	 * Retrieves the first Integer in the queue by removing it.
	 * @return the next Integer in the queue. If the queue is empty, returns <b>null</b>
	 * @throws IOException
	 */
	public abstract Integer popInt() throws IOException;
	
	/**
	 * Closes the file handle of this object.
	 * @throws IOException
	 */
	public void close() throws IOException{
		file.close();
	}

	public int compareTo(FileQueue fq) {
		return Integer.compare(peekInt(), fq.peekInt());
	}

}
