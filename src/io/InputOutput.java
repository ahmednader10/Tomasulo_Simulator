package io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


import cache.Cache;
import cache.CacheLevel;

import mainMemory.MainMemory;

public class InputOutput {
	public static void main(String[]args) throws NumberFormatException, IOException{
		BufferedReader in = new BufferedReader (new InputStreamReader (System.in));
		
		System.out.println("Enter Main Memory acces time in cycles:");
	    int hitCycles = Integer.parseInt(in.readLine());
	    MainMemory memory = new MainMemory(hitCycles);
	    
	    System.out.println("Enter Number of Cache Levels:");
	    int cacheLevels = Integer.parseInt(in.readLine());
	    Cache cache = new Cache(cacheLevels);
	    CacheLevel[] levels = cache.getLevels();
	  
	    for (int i = 0; i < cacheLevels; i++) {
	    	int level = i+1;
	    	System.out.println("Enter cache level's "+ level + " Size:");
		    int S = Integer.parseInt(in.readLine());
		    
		    System.out.println("Enter cache level's "+ level + " Line Size:");
		    int L = Integer.parseInt(in.readLine());
		    
		    System.out.println("Enter cache level's "+ level + " associativity Level:");
		    int m = Integer.parseInt(in.readLine());
		    
		    System.out.println("Enter cache level's "+ level + " Write Policy in case of a Hit:");
		    String hitP = in.readLine();
		    
		    System.out.println("Enter cache level's "+ level + " Write Policy in case of a Miss:");
		    String missP = in.readLine();
		    
		    System.out.println("Enter cache level's "+ level + " Access Time in Cycles:");
		    int hitTime = Integer.parseInt(in.readLine());
		    
		    levels[i] = new CacheLevel(level, S, L, m, hitP, missP, hitTime);
	    }
	    cache.setLevels(levels);
	} 
	
 
}
