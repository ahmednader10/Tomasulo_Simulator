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
	Cache Icache;
	Cache Dcache;
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
	    
	    
	 
	    Icache = new Cache(cacheLevels);
	    CacheLevel[] levels = Icache.getLevels();
	    
	    Dcache = new Cache(cacheLevels);
	    CacheLevel[] Dlevels = Dcache.getLevels();
	  
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
		    Dlevels[i] = new CacheLevel(level, hitTime, L, type, m, hitP, S);
	    }
	    Icache.setLevels(levels);
	    Dcache.setLevels(levels);
	    
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
	    
	    System.out.println("Enter the number of stations for Store:");
	    int stStations = Integer.parseInt(in.readLine());
	    
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
	    	stations[j] = new station("LOAD"+n,0);
	    }
	    
	    for (int k = 0; k < stStations; k++) {
	    	int n = k+1;
	    	stations[j] = new station("ST"+n,0);
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
			
			Icache.read(address2, memory, i , sim);
			
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
		Icache.calculateAMAT(x.size(), memory.getHitCycles());
		sim.prepare(pipeline, stats, r, memory, Dcache, Icache, x.size());
		
	}
	
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
