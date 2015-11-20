package mainMemory;

public class MainMemory {
	boolean [] memory;
	int MAX_SIZE = 16;
	public MainMemory(){
		memory = new boolean[MAX_SIZE];
	}
	public boolean[] getMem() {
		return memory;
	}

	public void setMem(boolean[] memory) {
		this.memory = memory;
	}
}
