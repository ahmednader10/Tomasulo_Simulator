package tomasulo;

import entries.ROBentry;

public class ROB {
	ROBentry[] Rob;
	int head;
	int tail;
	
	public ROB(int count) {
		Rob = new ROBentry[count];
		head = tail = 0;
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
