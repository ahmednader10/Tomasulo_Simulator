package tomasulo;

import mainMemory.GPRegisters;
import mainMemory.MainMemory;
import mainMemory.Register;
import entries.ROBentry;
import entries.station;

public class Simulation {
	String[] InstructionBuffer;
	int IBindex;
	ReservationStations reservationstations;
	int cycle;
	int pipeline;
	ROB rob;
	GPRegisters gpr;
	MainMemory memory;
	public Simulation(int count){
		InstructionBuffer = new String[count];
		IBindex = 0;
		cycle = 0;
	}
	public void prepare(int p, ReservationStations s, ROB r, MainMemory m) {
		reservationstations = s;
		pipeline = p;
		rob = r;
		gpr = new GPRegisters();
		memory = m;
		issue();
	}
	public void issue(){
		if (bufferisEmpty() || rob.isFull())
			return;
		String instruction = getNextInstruction();
		removeInstruction(instruction);
		station rs = getEmptyStation(instruction);
		if (rs != null) {
			if (getInstructionRS(instruction).getStatus() != -1) {
				rs.setQj(getInstructionRS(instruction).getStatus());
				rs.setVj(0);
			} else {
				// (Written but not committed)
				int testRob = rob.findDest(getInstructionRS(instruction).getName());
				if (testRob != -1) {
					rs.setVj(testRob);
				} else {
					rs.setVj(getInstructionRS(instruction).getName());
				}
				rs.setQj(-1);
			}
			if (getInstructionType(instruction).equalsIgnoreCase("add") || 
					getInstructionType(instruction).equalsIgnoreCase("sub") || 
						getInstructionType(instruction).equalsIgnoreCase("mul") ||
							getInstructionType(instruction).equalsIgnoreCase("nand")){
				if (getInstructionRT(instruction).getStatus() != -1) {
					rs.setQk(getInstructionRT(instruction).getStatus());
					rs.setVk(0);
				} else {
					int testRob = rob.findDest(getInstructionRT(instruction).getName());
					if (testRob != -1) {
						rs.setVk(testRob);
					} else {
						rs.setVk(getInstructionRT(instruction).getName());
					}
					rs.setQk(-1);
				}
			}
			else{
				if (getInstructionType(instruction).equalsIgnoreCase("beq") || 
						getInstructionType(instruction).equalsIgnoreCase("sw")) {
					if (getInstructionRD(instruction).getStatus() != -1) {
						rs.setQk(getInstructionRD(instruction).getStatus());
						rs.setVk(0);
					} else {
						int testRob = rob.findDest(getInstructionRD(instruction).getName());
						if (testRob != -1) {
							rs.setVk(testRob);
						} else {
							rs.setVk(getInstructionRD(instruction).getName());
						}
						rs.setQk(-1);
					}
					rs.setA(getInstructionRT(instruction).getName());
				}
				else{
					rs.setQk(-1);
					rs.setVk(-1);
					rs.setA(getInstructionRT(instruction).getName());
				}
			}
			rs.setDest(rob.getTail());
			if (rs.getCyclesLeft() < 1) {
				rs.setCyclesLeft(1);
			}
			rs.setBusy(true);
			rs.setStep("issue");
			rs.setOp(getInstructionType(instruction));
			
			int destination = getInstructionRD(instruction).getName();
			if (writesToReg(instruction)) {
				getInstructionRD(instruction).setStatus(rob.getTail());
			} else if (getInstructionType(instruction).equalsIgnoreCase("beq")) {
				destination = getInstructionAddress(instruction);
			} else {
				destination = -1;
			}
			ROBentry robEntry = new ROBentry(getInstructionType(instruction), destination);
			rob.add(robEntry);
		}
		
	}
	public void execute() {
		for (int i = 0; i < reservationstations.getStations().length; i++) {
			station entry = reservationstations.getStations()[i];
			if (entry.isBusy() == false)
				continue;

			if (entry.getStep().equalsIgnoreCase("issue") && entry.getQj() == -1 && entry.getQk() == -1){
				entry.setStep("execute");
				entry.setA(entry.getVj() + entry.getA());
			}
			else if (entry.getStep().equalsIgnoreCase("execute") && entry.getCyclesLeft() > 0)
				entry.setCyclesLeft(entry.getCyclesLeft()-1 );
		}
	}

	public boolean isMemory(String instruction) {
		return getInstructionType(instruction).equalsIgnoreCase("lw") || getInstructionType(instruction).equalsIgnoreCase("sw");
	}

	public boolean writesToReg(String instruction) {
		return getInstructionType(instruction).equalsIgnoreCase("lw") || getInstructionType(instruction).equalsIgnoreCase("add")
				|| getInstructionType(instruction).equalsIgnoreCase("sub") || getInstructionType(instruction).equalsIgnoreCase("addi")
				|| getInstructionType(instruction).equalsIgnoreCase("nand") || getInstructionType(instruction).equalsIgnoreCase("mul");

	}
	public String getInstructionType(String x) {
		String[] instruction = x.split(" ");
		return instruction[0];
	}
	public Register getInstructionRT(String x) {
		String[] instruction = x.split(" ");
		if (getInstructionType(x).equalsIgnoreCase("LW") || 
				getInstructionType(x).equalsIgnoreCase("SW")) {
			int index = Character.getNumericValue(instruction[2].charAt(instruction[2].length() - 2));
			return gpr.getRegisters()[index];
		}
		int index = Character.getNumericValue(instruction[3].charAt(instruction[3].length() - 1));
		return gpr.getRegisters()[index];
	}
	public Register getInstructionRS(String x) {
		String[] instruction = x.split(" ");
		String reg = instruction[2];
		int index = Character.getNumericValue(reg.charAt(reg.length() - 2));
		return gpr.getRegisters()[index];
	}
	//for BEQ only
	public int getInstructionAddress(String x) {
		String[] instruction = x.split(" ");
		int imm = Integer.parseInt(instruction[3]);
		return imm+ memory.getPC() + 1;
	}
	public Register getInstructionRD(String x) {
		String[] instruction = x.split(" ");
		int index = Character.getNumericValue(instruction[1].charAt(instruction[1].length() - 2));
		return gpr.getRegisters()[index];
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
		for (int i = 0; i < reservationstations.getStations().length; i++) {
			station s = reservationstations.getStations()[i];
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
		for (int i = 0; i < reservationstations.getStations().length; i++) {
			station s = reservationstations.getStations()[i];
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
	public boolean bufferisEmpty() {
		for (int i = 0; i < InstructionBuffer.length; i++) {
			if (InstructionBuffer[i] != null)
				return false;
		}
		return true;
	}
	public String getNextInstruction() {
		for (int i = 0; i < InstructionBuffer.length; i++) {
			if (InstructionBuffer[i] != null)
				return InstructionBuffer[i];
		}
		return null;
	}
	public void removeInstruction(String x) {
		for (int i = 0; i < InstructionBuffer.length; i++) {
			if (InstructionBuffer[i] != null && InstructionBuffer[i].equalsIgnoreCase(x))
				InstructionBuffer[i] = null;
		}
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
