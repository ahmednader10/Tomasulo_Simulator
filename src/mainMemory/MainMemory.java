package mainMemory;


public class MainMemory {
	String [][] memory;
	int memorySize = 64*1024;
	int hitCycles;
	int ByteAddress;
	int LineSize;
	int PC;
	
	public MainMemory(int cycles, int S){
		
		hitCycles = cycles;
		LineSize = S;
		memory = new String[memorySize/S][LineSize/2];
	}
	public int getHitCycles() {
		return hitCycles;
	}
	public void setHitCycles(int hitCycles) {
		this.hitCycles = hitCycles;
	}
	public int getByteAddress() {
		return ByteAddress;
	}
	public void setByteAddress(int byteAddress) {
		ByteAddress = byteAddress;
	}
	
	
	
	public int getLineSize() {
		return LineSize;
	}
	public void setLineSize(int lineSize) {
		LineSize = lineSize;
	}
	public String[][] getMem() {
		return memory;
	}

	public void setMem(String[][] memory) {
		this.memory = memory;
	}
	public int getPC() {
		return PC;
	}
	public void setPC(int pC) {
		PC = pC;
	}
	
	
}
