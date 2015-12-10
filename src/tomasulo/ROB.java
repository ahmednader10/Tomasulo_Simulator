package tomasulo;

import entries.ROBentry;

public class ROB {
	ROBentry[] Rob;
	int head;
	int tail;
	
	public ROB(int count) {
		Rob = new ROBentry[count];
		head = tail = 0;
		for (int i = 0; i < count; i++){
			Rob[i] = new ROBentry();
		}
	}
	public int findDest(int reg) {
		boolean stop = false;
		int i = (tail+Rob.length - 1)%Rob.length;

		while (!stop) {
			if (i == head)
				stop = true;
			ROBentry robEntry = (ROBentry) Rob[i];
			if (robEntry!= null &&
					robEntry.getDest() == reg
					&& robEntry.isReady()
					&& !robEntry.getType().equalsIgnoreCase("BEQ")
					&& !robEntry.getType().equalsIgnoreCase("SW")) {
				return robEntry.getValue();
			}
			i = (i + Rob.length - 1) % Rob.length;
		}

		return -1;
	}
	
	public ROBentry getFirst(){
		if(isEmpty()) return null;
		return Rob[head];
	}
	
	public boolean isEmpty(){
		return head == tail;
	}
	
	public void flush() {
		head = tail;
	}
	
	public boolean isFull(){
		return head == ((tail+1)%Rob.length);
	}
	
	public void moveHead(){
		head++;
		head %= Rob.length;
	}
	
	public boolean add(ROBentry e){
		if(isFull()) return false;
		Rob[tail++] = e;
		tail %= Rob.length;
		return true;
	}

	public ROBentry[] getRob() {
		return Rob;
	}

	public void setRob(ROBentry[] rob) {
		Rob = rob;
	}

	public int getHead() {
		return head;
	}

	public void setHead(int head) {
		this.head = head;
	}

	public int getTail() {
		return tail;
	}

	public void setTail(int tail) {
		this.tail = tail;
	}
	
}
