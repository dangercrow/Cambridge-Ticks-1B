package uk.ac.cam.sy321.fjava.tick3;

public class SafeMessageQueue<T> implements MessageQueue<T> {
	private static class Link<L> {
		L val;
		Link<L> next;
		Link(L val) { this.val = val; this.next = null; }
	}
	private Link<T> first = null;
	private Link<T> last = null;
	public synchronized void put(T val) {
		Link<T> newLink = new Link<T>(val);
		if(first==null){
			first = newLink;
			last = newLink;
		}
		else{
			last.next = newLink; 
			last = newLink;
		}
		this.notify();
	}
	public synchronized T take() {
		while(first == null) //use a loop to block thread until data is available
			try {
				this.wait();
				}
		catch(InterruptedException ie) {}
		T val = first.val;
		first = first.next;
		if (first == null){
			last = null;
		}
		return val;

	}
}
