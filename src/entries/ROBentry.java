package entries;

public class ROBentry {
	String Type;
	String Dest;
	String Value;
	boolean ready;
	int cyclesLeft;
	public ROBentry() {
		Type = "";
		Dest = "";
		Value = "";
		ready = true;
	}
	public ROBentry(String t, String d, String v, boolean r) {
		Type = t;
		Dest = d;
		Value = v;
		ready = r;
	}
	public String getType() {
		return Type;
	}
	public void setType(String type) {
		Type = type;
	}
	public String getDest() {
		return Dest;
	}
	public void setDest(String dest) {
		Dest = dest;
	}
	public String getValue() {
		return Value;
	}
	public void setValue(String value) {
		Value = value;
	}
	public boolean isReady() {
		return ready;
	}
	public void setReady(boolean ready) {
		this.ready = ready;
	}
	
	

}
