package io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


import tomasulo.ROB;
import tomasulo.ReservationStations;
import tomasulo.Simulation;


import cache.Cache;
import cache.CacheLevel;
import entries.station;

import mainMemory.GPRegisters;
import mainMemory.MainMemory;
import mainMemory.Register;

public class InputOutput {
	MainMemory memory;
	GPRegisters reg = new GPRegisters();
	Cache cache;
	Register[] GPR = reg.getRegisters();
	ROB rob;
	ReservationStations Rstations;
	Simulation sim;
	public void inputData() throws NumberFormatException, IOException {
		BufferedReader in = new BufferedReader (new InputStreamReader (System.in));
		
		System.out.println("Enter Memory Line Size:");
	    int L = Integer.parseInt(in.readLine());
	    
	    System.out.println("Enter Main Memory acces time in cycles:");
	    int hitCycles = Integer.parseInt(in.readLine());
	    
	    memory = new MainMemory(hitCycles, L);
	    
	    
	    System.out.println("Enter Number of Cache Levels:");
	    int cacheLevels = Integer.parseInt(in.readLine());
	    
	    
	 
	    cache = new Cache(cacheLevels);
	    CacheLevel[] levels = cache.getLevels();
	  
	    for (int i = 0; i < cacheLevels; i++) {
	    	int level = i+1;
	    	
	    	System.out.println("Enter cache level's "+level+" Size:");
		    int S = Integer.parseInt(in.readLine());
		    
		    System.out.println("Enter cache level's "+level+" type: (0 for direct Mapped, 1 for Fully associative, 2 for set associative)");
		    int type = Integer.parseInt(in.readLine());
		    
		    int m = -1;
		    if(type == 2){
			    System.out.println("Enter cache level's "+level+" associativity Level:");
			    m = Integer.parseInt(in.readLine());
		    }
		    
		    System.out.println("Enter cache level's  "+level+" Write Policy in case of a Hit: (0 for Write Through, 1 for Write Back)");
		    int hitP = Integer.parseInt(in.readLine());
	    	   
		    System.out.println("Enter cache level's "+ level + " Access Time in Cycles:");
		    int hitTime = Integer.parseInt(in.readLine());
		    
		    levels[i] = new CacheLevel(level, hitTime, L, type, m, hitP, S);
	    }
	    cache.setLevels(levels);
	}
	
	public void InputProgram() throws NumberFormatException, IOException{
		BufferedReader in = new BufferedReader (new InputStreamReader (System.in));
		ArrayList<String> store = new ArrayList();
		
		System.out.println("Enter the starting address for your program:");
	    int startAddress = Integer.parseInt(in.readLine());
	    memory.setPC(startAddress);
	    
	    System.out.println("Enter your Program: (The last line should be EOP)");
	    
	    String instruction = in.readLine();
	    
	    while (!instruction.equals("EOP")){
	    	store.add(instruction);
	    	instruction = in.readLine();
	    }
	    System.out.println("Enter the number of instructions issued in the pipeline:");
	    int pipeline = Integer.parseInt(in.readLine());
	    
	    System.out.println("Enter the number of ROB entries:");
	    int robcount = Integer.parseInt(in.readLine());
	    rob = new ROB(robcount);
	    
	    System.out.println("Enter the size of instruction buffer:");
	    int Ibuffer = Integer.parseInt(in.readLine());
	    sim = new Simulation(Ibuffer);
	    
	    System.out.println("Enter the number of stations for Load:");
	    int ldStations = Integer.parseInt(in.readLine());
	    
	    System.out.println("Enter the number of cycles for Load:");
	    int ldcycles = Integer.parseInt(in.readLine());
	    
	    System.out.println("Enter the number of stations for Store:");
	    int stStations = Integer.parseInt(in.readLine());
	    
	    System.out.println("Enter the number of cycles for Store:");
	    int stcycles = Integer.parseInt(in.readLine());
	    
	    System.out.println("Enter the number of stations for ADD:");
	    int addStations = Integer.parseInt(in.readLine());
	    
	    System.out.println("Enter the number of cycles for ADD:");
	    int addcycles = Integer.parseInt(in.readLine());
	    
	    System.out.println("Enter the number of stations for MUL:");
	    int mulStations = Integer.parseInt(in.readLine());
	    
	    System.out.println("Enter the number of cycles for MUL:");
	    int mulcycles = Integer.parseInt(in.readLine());
	    
	    int total = ldStations + stStations + addStations + mulStations;
	    Rstations = new ReservationStations(total);
	    
	    station[] stations = Rstations.getStations();
	    
	    int j =0;
	    for (j = 0; j < ldStations; j++) {
	    	int n = j+1;
	    	stations[j] = new station("LOAD"+n,ldcycles);
	    }
	    
	    for (int k = 0; k < stStations; k++) {
	    	int n = k+1;
	    	stations[j] = new station("ST"+n,stcycles);
	    	j++;
	    }
	    
	    for (int k = 0; k < addStations; k++) {
	    	int n = k+1;
	    	stations[j] = new station("ADD"+n,addcycles);
	    	j++;
	    }
	    
	    for (int k = 0; k < mulStations; k++) {
	    	int n = k+1;
	    	stations[j] = new station("MUL"+n,mulcycles);
	    	j++;
	    }
	    
	    Rstations.setStations(stations);
	    storeProgram(store, pipeline, Rstations, rob);
	}
	
	public void storeProgram(ArrayList<String> x, int pipeline, ReservationStations stats, ROB r){
		int address = memory.getPC();
		String BinAdd = Integer.toBinaryString(address);
		String[][] m = memory.getMem();
		for(int i = 0; i < x.size(); i++) {
			m[address][i] = x.get(i);
			
			String address2 = Integer.toBinaryString(address);
			int bitsleft = 16 - address2.length();
			for(int j = 0; j < bitsleft; j++){
				address2 = "0"+address2;
			}
			
			cache.read(address2, memory, i , sim);
			
			int Bin = Integer.parseInt(BinAdd, 2);
			Bin+=2;
			BinAdd = Integer.toBinaryString(Bin);

			if (i !=0 && (i % (memory.getLineSize()/2 - 1) == 0)){
				address++;
			} 
			if (address == memory.getMem().length - 1) {
				address = 0;
				BinAdd = "0";
			}
		}
		cache.calculateAMAT(x.size(), memory.getHitCycles());
		sim.prepare(pipeline, stats, r);
		
	}
	
	/*public void ExecuteProgram(ArrayList<String> x, int ByteAddress){
		for(int i = 0; i < x.size(); i++) {
			String[] instruction = x.get(i).split(" ");
			if (instruction[0].equalsIgnoreCase("ADD")){
				String result = instruction[1].substring(0, instruction[1].length()-1);
				String reg1 = instruction[2].substring(0, instruction[2].length()-1);
				String reg2 = instruction[3].substring(0, instruction[3].length());
				ADD(reg1, reg2, result);
			}
			if (instruction[0].equalsIgnoreCase("ADDI")){
				String result = instruction[1].substring(0, instruction[1].length()-1);
				String reg1 = instruction[2].substring(0, instruction[2].length()-1);
				int OP2 = Integer.parseInt(instruction[3].substring(0, instruction[3].length()));
				ADDI(reg1, OP2, result);
			}
			if (instruction[0].equalsIgnoreCase("Sub")){
				String result = instruction[1].substring(0, instruction[1].length()-1);
				String reg1 = instruction[2].substring(0, instruction[2].length()-1);
				String reg2 = instruction[3].substring(0, instruction[3].length());
				SUB(reg1, reg2, result);
			}
			if (instruction[0].equalsIgnoreCase("MUL")){
				String result = instruction[1].substring(0, instruction[1].length()-1);
				String reg1 = instruction[2].substring(0, instruction[2].length()-1);
				String reg2 = instruction[3].substring(0, instruction[3].length());
				MUL(reg1, reg2, result);
			}
			if (instruction[0].equalsIgnoreCase("NAND")){
				String result = instruction[1].substring(0, instruction[1].length()-1);
				String reg1 = instruction[2].substring(0, instruction[2].length()-1);
				String reg2 = instruction[3].substring(0, instruction[3].length());
				NAND(reg1, reg2, result);
			}
			
			ByteAddress+=2;
 		}
		
	}
	
	public void ADD (String reg1, String reg2, String reg3) {
		int OP1 = boolToDec(GPR.get(reg1));
		int OP2 = boolToDec(GPR.get(reg2));
		int result = OP1+OP2;
		String BinRes = Integer.toBinaryString(result);
		int bitsleft = 16 - BinRes.length();
		
		for(int i = 0; i < bitsleft; i++){
			BinRes = "0"+BinRes;
		}
		//System.out.println(BinRes);
		int[] finalRes = new int[BinRes.length()];
		for(int j = 0; j < BinRes.length(); j++){
			finalRes[j] = Character.getNumericValue(BinRes.charAt(j));
			//System.out.print(finalRes[j]);
		}
		GPR.put(reg3, finalRes);
		reg.setGPRs(GPR);
		//for(int k = 0; k < GPR.get(reg3).length; k++)
		//	System.out.print(GPR.get(reg3)[k]);
	}
	
	public void ADDI(String reg1, int OP2, String reg3) {
		int OP1 = boolToDec(GPR.get(reg1));
		int result = OP1+OP2;
		String BinRes = Integer.toBinaryString(result);
		int bitsleft = 16 - BinRes.length();
		
		for(int i = 0; i < bitsleft; i++){
			BinRes = "0"+BinRes;
		}
		//System.out.println(BinRes);
		int[] finalRes = new int[BinRes.length()];
		for(int j = 0; j < BinRes.length(); j++){
			finalRes[j] = Character.getNumericValue(BinRes.charAt(j));
			//System.out.print(finalRes[j]);
		}
		GPR.put(reg3, finalRes);
		reg.setGPRs(GPR);
		//System.out.println(GPR.get(reg3)[14]);
	}
	
	public void SUB (String reg1, String reg2, String reg3) {
		int OP1 = boolToDec(GPR.get(reg1));
		int OP2 = boolToDec(GPR.get(reg2));
		int result = OP1-OP2;
		String BinRes = Integer.toBinaryString(result);
		int bitsleft = 16 - BinRes.length();
		
		for(int i = 0; i < bitsleft; i++){
			BinRes = "0"+BinRes;
		}
		//System.out.println(BinRes);
		int[] finalRes = new int[BinRes.length()];
		for(int j = 0; j < BinRes.length(); j++){
			finalRes[j] = Character.getNumericValue(BinRes.charAt(j));
			//System.out.print(finalRes[j]);
		}
		GPR.put(reg3, finalRes);
		reg.setGPRs(GPR);
		//for(int k = 0; k < GPR.get(reg3).length; k++)
			//	System.out.print(GPR.get(reg3)[k]);
	}
	
	public void MUL (String reg1, String reg2, String reg3) {
		int OP1 = boolToDec(GPR.get(reg1));
		int OP2 = boolToDec(GPR.get(reg2));
		int result = OP1*OP2;
		String BinRes = Integer.toBinaryString(result);
		int bitsleft = 16 - BinRes.length();
		
		for(int i = 0; i < bitsleft; i++){
			BinRes = "0"+BinRes;
		}
		//System.out.println(BinRes);
		int[] finalRes = new int[BinRes.length()];
		for(int j = 0; j < BinRes.length(); j++){
			finalRes[j] = Character.getNumericValue(BinRes.charAt(j));
			//System.out.print(finalRes[j]);
		}
		GPR.put(reg3, finalRes);
		reg.setGPRs(GPR);
		//System.out.println(GPR.get(reg3)[14]);
	}
	
	public void NAND (String reg1, String reg2, String reg3) {
		int[] c = GPR.get(reg1);
		int[] d = GPR.get(reg2);
		String OP1="";
		String OP2="";
		for(int i = 0; i < c.length; i++){
			OP1 += c[i];
			OP2 += d[i];
		}
		
		int[] finalRes = new int[16];
		for(int j = 0; j < 16; j++){
			int x = Character.getNumericValue(OP1.charAt(j));
			int y = Character.getNumericValue(OP1.charAt(j));
			finalRes[j] = 1- x*y;
			//System.out.print(finalRes[j]);
		}
		GPR.put(reg3, finalRes);
		reg.setGPRs(GPR);
		//for(int k = 0; k < GPR.get(reg3).length; k++)
		//		System.out.print(GPR.get(reg3)[k]);
	}*/
	
	public int boolToDec(int[] x) {
		int result = 0;
		String bin="";
		for(int i = 0; i < x.length; i++) {
			bin+= x[i];
		}
		result = Integer.parseInt(bin,2);
		return result;
	}
	
	
	public MainMemory getMemory() {
		return memory;
	}

	public void setMemory(MainMemory memory) {
		this.memory = memory;
	}

	public static void main(String[]args) throws NumberFormatException, IOException{
		InputOutput io = new InputOutput();
		
		io.inputData();
	    io.InputProgram();
	    
	} 
	
 
}
