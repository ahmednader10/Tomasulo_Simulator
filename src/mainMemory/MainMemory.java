package mainMemory;

public class MainMemory {
	String [] memory;
	int memorySize = 64*1024;
	int hitCycles;
	int PC;
	public MainMemory(int cycles){
		memory = new String[memorySize];
		hitCycles = cycles;
	}
	public String[] getMem() {
		return memory;
	}

	public void setMem(String[] memory) {
		this.memory = memory;
	}
}
