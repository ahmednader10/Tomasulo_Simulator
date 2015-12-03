package tomasulo;

import entries.station;

public class Simulation {
	String[] InstructionBuffer;
	int IBindex;
	ReservationStations rs;
	int cycle;
	int pipeline;
	ROB rob;
	public Simulation(int count){
		InstructionBuffer = new String[count];
		IBindex = 0;
		cycle = 0;
	}
	public void prepare(int p, ReservationStations s, ROB r) {
		rs = s;
		pipeline = p;
		rob = r;
		issue();
	}
	public void issue(){
		
	}
	public String getInstructionType(String x) {
		String[] instruction = x.split(" ");
		return instruction[0];
	}
	public String getFU(String instruction) {
		if (getInstructionType(instruction).equalsIgnoreCase("add") || 
				getInstructionType(instruction).equalsIgnoreCase("sub") || 
					getInstructionType(instruction).equalsIgnoreCase("nand") ||
						getInstructionType(instruction).equalsIgnoreCase("addi")){
			return "ADD";
		}
		else{
			if (getInstructionType(instruction).equalsIgnoreCase("mul")){
				return "MUL";
			}
			else{
				if (getInstructionType(instruction).equalsIgnoreCase("lw")){
					return "LOAD";
				}
				else{
					if (getInstructionType(instruction).equalsIgnoreCase("sw")){
						return "ST";
					}
					else{
						return getInstructionType(instruction);
					}
				}
			}
		}
	}
	public station getEmptyStation(String instruction){
		String type = getFU(instruction);
		for (int i = 0; i < rs.getStations().length; i++) {
			station s = rs.getStations()[i];
			String name = s.getName();
			String compare = name.substring(0, name.length() - 1);
			if ((!s.isBusy()) &&  type.equals(compare)) {
				return s;
			}
		}
		return null;
	}
	public int getinstructionCycles(String instruction){
		String type = getFU(instruction);
		for (int i = 0; i < rs.getStations().length; i++) {
			station s = rs.getStations()[i];
			String name = s.getName();
			String compare = name.substring(0, name.length() - 1);
			if ((!s.isBusy()) &&  type.equals(compare)) {
				return s.getCyclesLeft();
			}
		}
		return -1;
	}
	public void incrementIBindex(){ 
		if (IBindex == InstructionBuffer.length -1){
			IBindex = 0;
		}
		else{
			IBindex++;
		}
	}
	public boolean bufferhasSpace() {
		for (int i = 0; i < InstructionBuffer.length; i++) {
			if (InstructionBuffer[i] == null || InstructionBuffer[i] == "")
				return true;
		}
		return false;
	}
	public String[] getInstructionBuffer() {
		return InstructionBuffer;
	}
	public void setInstructionBuffer(String[] instructionBuffer) {
		InstructionBuffer = instructionBuffer;
	}
	
	public int getIBindex() {
		return IBindex;
	}
	
 
}
