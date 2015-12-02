package entries;

public class station {
	String name;
	boolean busy;
	String Op;
	int Vj;
	int Vk;
	String Qj;
	String Qk;
	int Dest;
	int A;
	int cyclesLeft;
	
	public station(String n, int c){
		name = n;
		busy = false;
		Op = "";
		Vj = -1;
		Vk = -1;
		Qj = "";
		Qk = "";
		Dest = -1;
		A = -1;
		cyclesLeft = c;
	}
	public station(String n, boolean b, String o, int vj, int vk, String qj, String qk, int d, int a, int c){
		name = n;
		busy = b;
		Op = o;
		Vj = vj;
		Vk = vk;
		Qj = qj;
		Qk = qk;
		Dest = d;
		A = a;
		cyclesLeft = c;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isBusy() {
		return busy;
	}
	public void setBusy(boolean busy) {
		this.busy = busy;
	}
	public String getOp() {
		return Op;
	}
	public void setOp(String op) {
		Op = op;
	}
	public int getVj() {
		return Vj;
	}
	public void setVj(int vj) {
		Vj = vj;
	}
	public int getVk() {
		return Vk;
	}
	public void setVk(int vk) {
		Vk = vk;
	}
	public String getQj() {
		return Qj;
	}
	public void setQj(String qj) {
		Qj = qj;
	}
	public String getQk() {
		return Qk;
	}
	public void setQk(String qk) {
		Qk = qk;
	}
	public int getDest() {
		return Dest;
	}
	public void setDest(int dest) {
		Dest = dest;
	}
	public int getA() {
		return A;
	}
	public void setA(int a) {
		A = a;
	}
	public int getCyclesLeft() {
		return cyclesLeft;
	}
	public void setCyclesLeft(int cyclesLeft) {
		this.cyclesLeft = cyclesLeft;
	}
	
	
 
}
