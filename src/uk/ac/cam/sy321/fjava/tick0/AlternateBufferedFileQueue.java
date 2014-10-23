package uk.ac.cam.sy321.fjava.tick0;

import java.io.IOException;
import java.io.RandomAccessFile;

public class AlternateBufferedFileQueue extends FileQueue{

	private int filePointer;
	private int bufferPointer;
	private int readLimit;
	private int read;
	
	final int BUFFER_SIZE = 8192;
	// Buffer size is 8192 bytes, as Java default.
	private byte[] buffer = new byte[BUFFER_SIZE];
	private boolean needsRefilling;

	public AlternateBufferedFileQueue(String filePath, int start, int limit) throws IOException{
		file = new RandomAccessFile(filePath, "r");
		filePointer = start * 4;
		bufferPointer = 0;
		readLimit = limit * 4 - filePointer;
		refillBuffer();
		needsRefilling = false;
	}

	private void refillBuffer() throws IOException{
			file.seek(filePointer + read);
			file.read(buffer);
			needsRefilling = false;
			bufferPointer = 0;
	}

	public Integer peekInt(){
		if(read == readLimit){
			return null;
		}
		if(needsRefilling){
			try {
				refillBuffer();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			needsRefilling = false;
		}
		int result = (
				((buffer[bufferPointer]   & 0xff) << 24) | 
				((buffer[bufferPointer+1] & 0xff) << 16) |
				((buffer[bufferPointer+2] & 0xff) << 8 ) | 
				(buffer[bufferPointer+3]  & 0xff)      );
		return result;
	}

	public Integer popInt() throws IOException{
		if(needsRefilling){
			refillBuffer();
			needsRefilling = false;
		}
		int result = (
				((buffer[bufferPointer]   & 0xff) << 24) | 
				((buffer[bufferPointer+1] & 0xff) << 16) |
				((buffer[bufferPointer+2] & 0xff) << 8 ) | 
				(buffer[bufferPointer+3]  & 0xff)      );
		bufferPointer+=4;
		read+=4;
		if (bufferPointer==BUFFER_SIZE){
			needsRefilling = true;
		}
		return result;
	}
	
	@Override
	public int compareTo(FileQueue arg0) {
		return Integer.compare(this.peekInt(), arg0.peekInt());
	}
}
