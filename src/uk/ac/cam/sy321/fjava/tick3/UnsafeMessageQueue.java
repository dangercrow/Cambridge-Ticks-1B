package uk.ac.cam.sy321.fjava.tick3;

public class UnsafeMessageQueue<T> implements MessageQueue<T> {
	private static class Link<L> {
		L val;
		Link<L> next;
		Link(L val) { this.val = val; this.next = null; }
	}
	private Link<T> first = null;
	private Link<T> last = null;
	public void put(T val) {
		Link<T> newLink = new Link<T>(val);
		if(first==null){
			first = newLink;
			last = newLink;
		}
		else{
			last.next = newLink; 
			last = newLink;
		}
	}
	public T take() {
		while(first == null) //use a loop to block thread until data is available
			try {Thread.sleep(100);} catch(InterruptedException ie) {}
		T val = first.val;
		first = first.next;
		if (first == null){
			last = null;
		}
		return val;

	}
}

