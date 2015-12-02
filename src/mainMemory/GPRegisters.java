package mainMemory;


public class GPRegisters {
	Register[] registers;
	public GPRegisters() {
		registers = new Register[8];
		for (int i = 0; i < 8; i++){
			registers[i] = new Register();
		}
	}
	public Register[] getRegisters() {
		return registers;
	}
	public void setRegisters(Register[] registers) {
		this.registers = registers;
	}
	
	

}
