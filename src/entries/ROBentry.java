package entries;

public class ROBentry {
	String Type;
	int Dest;
	int Value;
	boolean ready;
	int cyclesLeft;
	boolean BranchTaken;
	public ROBentry() {
		Type = "";
		Dest = -1;
		Value = -1;
		BranchTaken = false;
	}
	public ROBentry(String t, int d) {
		Type = t;
		Dest = d;
	}
	public String getType() {
		return Type;
	}
	public void setType(String type) {
		Type = type;
	}
	public int getDest() {
		return Dest;
	}
	public void setDest(int dest) {
		Dest = dest;
	}
	public int getValue() {
		return Value;
	}
	public void setValue(int value) {
		Value = value;
	}
	public boolean isReady() {
		return ready;
	}
	public void setReady(boolean ready) {
		this.ready = ready;
	}
	public boolean isBranchTaken() {
		return BranchTaken;
	}
	public void setBranchTaken(boolean branchTaken) {
		BranchTaken = branchTaken;
	}
	
	

}
