package entries;

import cache.Cache;
import mainMemory.MainMemory;

public class station {
	String name;
	boolean busy;
	String Op;
	int Vj;
	int Vk;
	int Qj;
	int Qk;
	int Dest;
	int A;
	int cyclesLeft;
	int tempcyclesleft;
	String step;
	
	public station(String n, int c){
		name = n;
		busy = false;
		Op = "";
		Vj = -1;
		Vk = -1;
		Qj = -1;
		Qk = -1;
		Dest = -1;
		A = -1;
		cyclesLeft = c;
		tempcyclesleft = c;
		step = "";
	}
	public station(String n, boolean b, String o, int vj, int vk, int qj, int qk, int d, int a, int c){
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
		tempcyclesleft = c;
	}
	public int run(MainMemory memory, Cache cache, ROBentry rob) {
		
		if (Op.equalsIgnoreCase("add")) {
			return Vj + Vk;
		}
		if (Op.equalsIgnoreCase("addi")) {
			return A;
		}
		if (Op.equalsIgnoreCase("sub")) {
			return Vj - Vk;
		}
		if (Op.equalsIgnoreCase("mul")) {
			return Vj * Vk;
		}
		if (Op.equalsIgnoreCase("nand")) {
			return ~(Vj & Vk);
		}
		if (Op.equalsIgnoreCase("lw")) {
			String [][] y = memory.getMem();
			
			return Integer.parseInt(y[A][0]);
		}
		if (Op.equalsIgnoreCase("sw")) {
			rob.setDest(A);
			return Vk;
		}
		if (Op.equalsIgnoreCase("beq")) {
			rob.setBranchTaken(Vj == Vk);
			return A;
		}
		return -1;
		
	}

	public void clear() {
		Vj = Vk = Dest = A = 0;
		Qj = Qk = -1;
		busy = false;
		Op = null;
		step = null;
		cyclesLeft = tempcyclesleft;
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
	public int getQj() {
		return Qj;
	}
	public void setQj(int qj) {
		Qj = qj;
	}
	public int getQk() {
		return Qk;
	}
	public void setQk(int qk) {
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
	public String getStep() {
		return step;
	}
	public void setStep(String step) {
		this.step = step;
	}
	
	
 
}
