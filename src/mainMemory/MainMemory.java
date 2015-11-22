package mainMemory;

public class MainMemory {
	String [] memory;
	int memorySize = 64*1024;
	GPR [] R;
	int hitCycles;
	int addressLine;
	public MainMemory(int cycles){
		memory = new String[memorySize];
		R = new GPR[8];
		R[0] = new GPR(0);
		hitCycles = cycles;
	}
	public String[] getMem() {
		return memory;
	}

	public void setMem(String[] memory) {
		this.memory = memory;
	}
}
