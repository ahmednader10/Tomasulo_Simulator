package tomasulo;

import cache.Cache;
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
	Cache Dcache;
	Cache Icache;
	int totalInstructions;
	
	boolean finished;
	boolean committed;
	public Simulation(int count){
		InstructionBuffer = new String[count];
		IBindex = 0;
		cycle = 0;
	}
	public void prepare(int p, ReservationStations s, ROB r, MainMemory m, Cache dcache, Cache icache, int n) {
		reservationstations = s;
		pipeline = p;
		rob = r;
		gpr = new GPRegisters();
		memory = m;
		Dcache = dcache;
		Icache = icache;
		totalInstructions = n;
		simulate();
	}
	public void simulate() {
		while (!committed) {
			
			issue();
			execute();
			write();
			commit();
			cycle++;
		}
		Dcache.calculateAMAT(totalInstructions, memory.getHitCycles());
		System.out.println("Cycles: " + cycle);
		for (int i = 0; i < Dcache.getLevels().length; i++) {
			int level = i+1;
			System.out.println("Icache level "+level+"'s hit ratio is: "+(1-Icache.getLevels()[i].getMissRate()));
			System.out.println("Dcache level "+level+"'s hit ratio is: "+(1-Dcache.getLevels()[i].getMissRate()));
		}
		System.out.println("Icache's AMAT is: "+Icache.calculateAMAT(totalInstructions, memory.getHitCycles()));
		System.out.println("Dcache's AMAT is: "+Dcache.calculateAMAT(totalInstructions, memory.getHitCycles()));
		
		System.out.println("Icache's IPC is: "+(1.0/Icache.CPI(totalInstructions, memory.getHitCycles())));
		System.out.println("Dcache's IPC is: "+(1.0/Dcache.CPI(totalInstructions, memory.getHitCycles())));
	}
	public void issue(){
		if (bufferisEmpty() || rob.isFull()){
			finished = true;
			return;
		}
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
				if (isMemory(instruction)) {
					Register source = getInstructionRT(instruction);
					int imm = getInstructionImm(instruction);
					int address = source.getValue() + imm;
					String address2 = Integer.toBinaryString(address);
					int bitsleft = 16 - address2.length();
					for(int j = 0; j < bitsleft; j++){
						address2 = "0"+address2;
					}
					rs.setCyclesLeft(Dcache.DcacheRead(address2, memory,false, -1));
				}
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
	
	public void write() {
		for (int i = 0; i < reservationstations.getStations().length; i++) {
			station rs = reservationstations.getStations()[i];
			if (rs.isBusy() && rs.getStep().equalsIgnoreCase("execute") && rs.getCyclesLeft() <= 0) {
				// Updating Reorder Buffer Entry
				ROBentry robEntry = (ROBentry) rob.getRob()[rs.getDest()];
				robEntry.setReady(true);
				int result = rs.run(memory, Dcache, robEntry); // Value from functional unit
				if (result == -1)
					return;

				rs.setStep("write");
				robEntry.setValue(result);

				if (RSwritesToReg(rs.getOp())
						&& gpr.getRegisters()[robEntry.getDest()].getStatus() == rs.getDest()) {
					gpr.getRegisters()[robEntry.getDest()].setStatus(-1);
				}

				// Updating reservation stations
				for (int j = 0; j < reservationstations.getStations().length; j++) {
					station resvStation = reservationstations.getStations()[j];
					if (rs.getDest() == resvStation.getQj()) {
						resvStation.setQj(-1);
						resvStation.setVj(result);
					}
					if (rs.getDest() == resvStation.getQk()) {
						resvStation.setQk(-1);
						resvStation.setVk(result);
					}
				}
				rs.setBusy(false);
				rs.clear();
			}

		}
	}
	public void commit() {
		if (rob.isEmpty()) {
			if (finished)
				committed = true;
			return; // Empty buffer
		}

		ROBentry entry = (ROBentry) rob.getFirst();		
		if(!entry.isReady()) return;
		
		if (entry.getType().equalsIgnoreCase("sw")) {
			int add = entry.getDest();
			String address = Integer.toBinaryString(add);
			int bitsleft = 16 - address.length();
			for(int j = 0; j < bitsleft; j++){
				address = "0"+address;
			}
			Dcache.DcacheRead(address, memory, true, entry.getValue());
			rob.moveHead();
		}
		// Assume immediate value is in VALUE field of rob entry
		else {	
			if (entry.getType().equalsIgnoreCase("beq")) {
				if ((entry.getValue() > 0 && entry.isBranchTaken())
					|| (entry.getValue() < 0 && !entry.isBranchTaken())) {
					rob.flush();
	
					for (int i = 0; i < reservationstations.getStations().length; i++)
						reservationstations.getStations()[i].clear();
					for (int i = 0; i < 8; i++)
						gpr.getRegisters()[i].setStatus(-1);
	
					memory.setPC((entry.isBranchTaken()) ? entry.getDest() + entry.getValue()
							: entry.getDest());
					finished = false;
				} else {
					rob.moveHead();
				}
			}
			else{
				gpr.getRegisters()[entry.getDest()].setValue(entry.getValue());
				rob.moveHead();
			}
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
	public boolean RSwritesToReg(String type) {
		return type.equalsIgnoreCase("lw") || type.equalsIgnoreCase("add")
				|| type.equalsIgnoreCase("sub") || type.equalsIgnoreCase("addi")
				|| type.equalsIgnoreCase("nand") || type.equalsIgnoreCase("mul");

	}
	public String getInstructionType(String x) {
		String[] instruction = x.split(" ");
		return instruction[0];
	}
	public int getInstructionImm(String x) {
		String[] instruction = x.split(" ");
		return Integer.parseInt(instruction[3]);
	}
	public Register getInstructionRT(String x) {
		String[] instruction = x.split(" ");
		if (getInstructionType(x).equalsIgnoreCase("LW") || 
				getInstructionType(x).equalsIgnoreCase("SW")) {
			int index = Character.getNumericValue(instruction[2].charAt(instruction[2].length() - 2));
			return gpr.getRegisters()[index];
		}
		if (getInstructionType(x).equalsIgnoreCase("jmp") || getInstructionType(x).equalsIgnoreCase("JALR")){
			int index = Character.getNumericValue(instruction[1].charAt(instruction[1].length() - 2));
			return gpr.getRegisters()[index];
		}
		if (getInstructionType(x).equalsIgnoreCase("RET")  ) {
			int index = Character.getNumericValue(instruction[1].charAt(instruction[1].length() - 1));
			return gpr.getRegisters()[index];
		}
		int index = Character.getNumericValue(instruction[3].charAt(instruction[3].length() - 1));
		return gpr.getRegisters()[index];
	}
	public Register getInstructionRS(String x) {
		String[] instruction = x.split(" ");
		if (getInstructionType(x).equalsIgnoreCase("JMP") || getInstructionType(x).equalsIgnoreCase("JALR")) {
			int index = Character.getNumericValue(instruction[1].charAt(instruction[1].length() - 2));
			return gpr.getRegisters()[index];
		}
		if (getInstructionType(x).equalsIgnoreCase("RET")  ) {
			int index = Character.getNumericValue(instruction[1].charAt(instruction[1].length() - 1));
			return gpr.getRegisters()[index];
		}
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
						getInstructionType(instruction).equalsIgnoreCase("addi") || 
							getInstructionType(instruction).equalsIgnoreCase("jmp") ||
								getInstructionType(instruction).equalsIgnoreCase("jalr")){
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
						return null;
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
