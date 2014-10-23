package uk.ac.cam.sy321.fjava.tick0;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * Implements the FileQueue class using Java.io methods such as BufferedInputStream and DataInputStream
 */
public class BufferedFileQueue extends FileQueue{

	private int filePointer;
	private int readLimit;
	private int read;
	private DataInputStream reader;
	private final int BUFFER_SIZE = 8192;

	public BufferedFileQueue(String filePath, int start, int limit) throws IOException{
		read = 0;
		file = new RandomAccessFile(filePath, "r");
		reader = new DataInputStream(new BufferedInputStream(new FileInputStream(file.getFD()), BUFFER_SIZE));
		filePointer = start * 4;
		file.seek(filePointer);
		readLimit = limit * 4 - filePointer;
	}
	
	public BufferedFileQueue(String filePath, int start, int limit, int intBufferSize) throws IOException{
		read = 0;
		file = new RandomAccessFile(filePath, "r");
		reader = new DataInputStream(new BufferedInputStream(new FileInputStream(file.getFD()), intBufferSize*4));
		filePointer = start * 4;
		file.seek(filePointer);
		readLimit = limit * 4 - filePointer;
	}

	@Override
	public Integer peekInt(){
		try{
			if(read == readLimit){
				return null;
			}
			reader.mark(4);
			int tmp = reader.readInt();
			reader.reset();
			return tmp;
		}
		catch (IOException e){
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public Integer popInt() throws IOException{
		read += 4;
		return reader.readInt();
	}
}
