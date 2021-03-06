package cache;

import tomasulo.Simulation;
import mainMemory.MainMemory;

public class Cache {
	int numberOflevels;
	CacheLevel[] levels;
	
	public Cache(int l) {
		numberOflevels = l;
		levels = new CacheLevel[l];
	}
	
	public void write(String[] block, String address) {
		for (int j = 0; j < levels.length; j++) {
			String[] tags = levels[j].getTags();
			String[][] data = levels[j].getData();
			boolean[] valid = levels[j].getValid();
			if (levels[j].getType() == 0) { //direct mapped
				int offsetBitsNum = (int) (Math.log( levels[j].getLineSize() ) / Math.log( 2 ));
				int numOfLines = levels[j].getLevelSize() / levels[j].getLineSize();
				int indexBitsNum = (int) (Math.log( numOfLines ) / Math.log( 2 ));
				int TagBitsNum = 16 - (offsetBitsNum + indexBitsNum);
				
				String tag = address.substring(0, TagBitsNum);
				String indexValBin = address.substring(TagBitsNum, (TagBitsNum + indexBitsNum));
				
				int index = Integer.parseInt(indexValBin, 2);
				
				tags[index] = tag;
				valid[index] = true;
				for (int k=0; k < block.length;k++) {
					data[index][k] = block[k];
				}
			}
			if (levels[j].getType() == 1) { //fully associative
				int offsetBitsNum = (int) (Math.log( levels[j].getLineSize() ) / Math.log( 2 ));
				int TagBitsNum = 16 - offsetBitsNum;
				
				String tag = address.substring(0, TagBitsNum);
				if (LevelisFull(j)) {
					tags[0] = tag;
					valid[0] = true;
					for (int k=0; k < block.length;k++) {
						data[0][k] = block[k];
					}
				}
				else{
					int m = getFirstEmptyEntry(j);
					tags[m] = tag;
					valid[m] = true;
					for (int k=0; k < block.length;k++) {
						data[m][k] = block[k];
					}
				}
			}
			if (levels[j].getType() == 2) { //set associative
				int blocks = levels[j].getLevelSize() / levels[j].getLineSize();
				int sets = blocks / levels[j].getAssociativityLevel();
				int indexBitsNum = (int) (Math.log( sets / Math.log( 2 )));
				int offsetBitsNum = (int) (Math.log( levels[j].getLineSize() ) / Math.log( 2 ));
				int TagBitsNum = 16 - (offsetBitsNum + indexBitsNum);
				
				String tagValBin = address.substring(0, TagBitsNum);
				String indexValBin = address.substring(TagBitsNum, (TagBitsNum + indexBitsNum));
				
				int index = Integer.parseInt(indexValBin, 2);
				int location = index * levels[j].getAssociativityLevel();
				int tempLoc = location;
				boolean copied = false;
								
				for (int k = 0; k < (levels[j].getLineSize()/2*levels[j].getAssociativityLevel()); k++) {
					if (valid[location] == false) {
						valid[location] = true;
						tags[location] = tagValBin;
						for (int x=0; x < block.length;x++) {
							data[location][x] = block[x];
						}
					copied = true;
					break;
					}
					location++;
				}
				if (!copied){
					valid[tempLoc] = true;
					tags[tempLoc] = tagValBin;
					for (int x=0; x < block.length;x++) {
						data[tempLoc][x] = block[x];
					}
				}
			}
			
			levels[j].setData(data);
			levels[j].setTags(tags);
			levels[j].setValid(valid);
		}
	}
	
	public void read(String address, MainMemory memory, int offset, Simulation sim) {
		
		for (int i = 0; i < levels.length; i++){
			if (levels[i].getType() == 0){ //direct mapped
				int offsetBitsNum = (int) (Math.log( levels[i].getLineSize() ) / Math.log( 2 ));
				int numOfLines = levels[i].getLevelSize() / levels[i].getLineSize();
				int indexBitsNum = (int) (Math.log( numOfLines ) / Math.log( 2 ));
				int TagBitsNum = 16 - (offsetBitsNum + indexBitsNum);
				
				String tagValBin = address.substring(0, TagBitsNum);
				String indexValBin = address.substring(TagBitsNum, (TagBitsNum + indexBitsNum));
				
				int index = Integer.parseInt(indexValBin, 2);
				
				String tag = levels[i].getTags()[index];
		
				if(tagValBin.equals(tag)){
					levels[i].setNumOfHits(levels[i].getNumOfHits()+1);
					String instruction = memory.getMem()[Integer.parseInt(address, 2)][offset];
					//send to instruction buffer
					String[] x = sim.getInstructionBuffer();
					if (sim.bufferhasSpace()) {
						x[sim.getIBindex()] = instruction;
						sim.incrementIBindex();
						sim.setInstructionBuffer(x);
					}
					return;
				}
				else {
					levels[i].setNumOfMisses(levels[i].getNumOfMisses()+1);
					if(i == levels.length -1) {
						String[] block = readBlockFromMemory(memory, address, i);
						// write back the block in all levels of cache
						write(block, address);
					}
				}
			}
			if (levels[i].getType() == 1) { // fully associative
				int offsetBitsNum = (int) (Math.log( levels[i].getLineSize() ) / Math.log( 2 ));
				int TagBitsNum = 16 - offsetBitsNum;
				
				String tagValBin = address.substring(0, TagBitsNum);
				for (int j = 0; j < levels[i].getTags().length; j++){
					if(tagValBin.equals(levels[i].getTags()[j])){
						levels[i].setNumOfHits(levels[i].getNumOfHits()+1);
						String instruction = memory.getMem()[Integer.parseInt(address, 2)][offset];
						//send to instruction buffer
						String[] x = sim.getInstructionBuffer();
						if (sim.bufferhasSpace()) {
							x[sim.getIBindex()] = instruction;
							sim.incrementIBindex();
							sim.setInstructionBuffer(x);
						}
						return;
					}
				}
				//didn't find in Cache
				levels[i].setNumOfMisses(levels[i].getNumOfMisses()+1);
				if(i == levels.length -1) {
					String[] block = readBlockFromMemory(memory, address, i);
					// write back the block in all levels of cache
					write(block, address);
				}
			}
			if(levels[i].getType() == 2){ // set associative
				int blocks = levels[i].getLevelSize() / levels[i].getLineSize();
				int sets = blocks / levels[i].getAssociativityLevel();
				int indexBitsNum = (int) (Math.log( sets / Math.log( 2 )));
				int offsetBitsNum = (int) (Math.log( levels[i].getLineSize() ) / Math.log( 2 ));
				int TagBitsNum = 16 - (offsetBitsNum + indexBitsNum);
				
				String tagValBin = address.substring(0, TagBitsNum);
				String indexValBin = address.substring(TagBitsNum, (TagBitsNum + indexBitsNum));
				
				int index = Integer.parseInt(indexValBin, 2);
				int location = index * levels[i].getAssociativityLevel();
				
				for (int k = 0; k < (levels[i].getLineSize()/2*levels[i].getAssociativityLevel()); k++) {
					if(tagValBin.equals(levels[i].getTags()[location])){
						levels[i].setNumOfHits(levels[i].getNumOfHits()+1);
						String instruction = memory.getMem()[Integer.parseInt(address, 2)][offset];
						//send to instruction buffer
						String[] x = sim.getInstructionBuffer();
						if (sim.bufferhasSpace()) {
							x[sim.getIBindex()] = instruction;
							sim.incrementIBindex();
							sim.setInstructionBuffer(x);
						}
						return;
					}
					location++;
				}
				//didn't find in Cache
				levels[i].setNumOfMisses(levels[i].getNumOfMisses()+1);
				if(i == levels.length -1) {
					String[] block = readBlockFromMemory(memory, address, i);
					// write back the block in all levels of cache
					write(block, address);
				}
			}
		}
		String instruction = memory.getMem()[Integer.parseInt(address, 2)][offset];
		//send to instruction buffer in case of not found in cache
		String[] x = sim.getInstructionBuffer();
		if (sim.bufferhasSpace()) {
			x[sim.getIBindex()] = instruction;
			sim.incrementIBindex();
			sim.setInstructionBuffer(x);
		}
	}
	
public int DcacheRead(String address, MainMemory memory, boolean write, int value) {
		int cycles = 0;
		int add = Integer.parseInt(address, 2);
		for (int i = 0; i < levels.length; i++){
			levels[i].setNumOfHits(0);
			levels[i].setNumOfMisses(0);
			if (levels[i].getType() == 0){ //direct mapped
				int offsetBitsNum = (int) (Math.log( levels[i].getLineSize() ) / Math.log( 2 ));
				int numOfLines = levels[i].getLevelSize() / levels[i].getLineSize();
				int indexBitsNum = (int) (Math.log( numOfLines ) / Math.log( 2 ));
				int TagBitsNum = 16 - (offsetBitsNum + indexBitsNum);
				
				String tagValBin = address.substring(0, TagBitsNum);
				String indexValBin = address.substring(TagBitsNum, (TagBitsNum + indexBitsNum));
				String offset = address.substring(TagBitsNum + indexBitsNum, address.length());
				
				int index = Integer.parseInt(indexValBin, 2);
				int offsetval = Integer.parseInt(offset, 2);
				int location = add - offsetval;
				String tag = levels[i].getTags()[index];
		
				if(tagValBin.equals(tag)){
					levels[i].setNumOfHits(levels[i].getNumOfHits()+1);
					cycles += levels[i].getHitCycles();
					if (write) {
						if (levels[i].hitWritePolicy == 1) {
							levels[i].data[index][offsetval] = value+"";
						}
						else {
							String [][] x = memory.getMem();
							x[index][offsetval] = value+"";
							memory.setMem(x);
						}
					}
					return cycles;
				}
				else {
					
					String loc = Integer.toBinaryString(location);
					levels[i].setNumOfMisses(levels[i].getNumOfMisses()+1);
					if(i == levels.length -1) {
						cycles += memory.getHitCycles();
						if (write) {
							String[] block = readBlockFromMemory(memory, loc, i);
							String [][] y = memory.getMem();
							y[location][offsetval] = value+"";
							memory.setMem(y);
							// write back the block in all levels of cache
							write(block, address);
						}
					}
					return cycles;
				}
			}
			if (levels[i].getType() == 1) { // fully associative
				int offsetBitsNum = (int) (Math.log( levels[i].getLineSize() ) / Math.log( 2 ));
				int TagBitsNum = 16 - offsetBitsNum;
				
				String tagValBin = address.substring(0, TagBitsNum);
				String offset = address.substring(TagBitsNum, address.length());
				
				int offsetval = Integer.parseInt(offset, 2);
				for (int j = 0; j < levels[i].getTags().length; j++){
					if(tagValBin.equals(levels[i].getTags()[j])){
						levels[i].setNumOfHits(levels[i].getNumOfHits()+1);
						cycles += levels[i].getHitCycles();
						if (write) {
							if (levels[i].hitWritePolicy == 1) {
								levels[i].data[0][offsetval] = value+"";
							}
							else {
								String [][] x = memory.getMem();
								x[0][offsetval] = value+"";
								memory.setMem(x);
							}
						}
						return cycles;
					}
				}
				
				//didn't find in Cache
				int location = add - offsetval;
				String loc = Integer.toBinaryString(location);
				levels[i].setNumOfMisses(levels[i].getNumOfMisses()+1);
				if(i == levels.length -1) {
					cycles += memory.getHitCycles();
					if (write) {
						String[] block = readBlockFromMemory(memory, loc, i);
						String [][] y = memory.getMem();
						y[location][offsetval] = value+"";
						memory.setMem(y);
						// write back the block in all levels of cache
						write(block, address);
					}
				}
				return cycles;
			}
			if(levels[i].getType() == 2){ // set associative
				int blocks = levels[i].getLevelSize() / levels[i].getLineSize();
				int sets = blocks / levels[i].getAssociativityLevel();
				int indexBitsNum = (int) (Math.log( sets / Math.log( 2 )));
				int offsetBitsNum = (int) (Math.log( levels[i].getLineSize() ) / Math.log( 2 ));
				int TagBitsNum = 16 - (offsetBitsNum + indexBitsNum);
				
				String tagValBin = address.substring(0, TagBitsNum);
				String indexValBin = address.substring(TagBitsNum, (TagBitsNum + indexBitsNum));
				String offset = address.substring(TagBitsNum + indexBitsNum, address.length() -1);
				
				
				int offsetval = Integer.parseInt(offset, 2);
				int index = Integer.parseInt(indexValBin, 2);
				int location = index * levels[i].getAssociativityLevel();
				
				for (int k = 0; k < (levels[i].getLineSize()/2*levels[i].getAssociativityLevel()); k++) {
					if(tagValBin.equals(levels[i].getTags()[location])){
						levels[i].setNumOfHits(levels[i].getNumOfHits()+1);
						cycles += levels[i].getHitCycles();
						if (write) {
							if (levels[i].hitWritePolicy == 1) { // write back
								levels[i].data[index][offsetval] = value+"";
							}
							else { // write through
								String [][] x = memory.getMem();
								x[index][offsetval] = value+"";
								memory.setMem(x);
							}
						}
						return cycles;
					}
					location++;
				}
				//didn't find in Cache
				int location2 = add - offsetval;
				String loc = Integer.toBinaryString(location2);
				levels[i].setNumOfMisses(levels[i].getNumOfMisses()+1);
				if(i == levels.length -1) {
					cycles += memory.getHitCycles();
					if (write) {
						String[] block = readBlockFromMemory(memory, loc, i);
						String [][] y = memory.getMem();
						y[location2][offsetval] = value+"";
						memory.setMem(y);
						// write back the block in all levels of cache
						write(block, address);
					}
				}
				return cycles;
			}
		}
		return cycles;
	}
	
	public float calculateAMAT(int total, int memoryCycles) {
		float Amat = levels[0].getHitCycles();
		float missRate = 1;
		for (int i = 0; i < levels.length; i++) {
			if ( i < levels.length - 1) {
				levels[i].missPenalty = levels[i+1].getHitCycles();
			}
			else {
				levels[i].missPenalty = memoryCycles;
			}
			levels[i].missRate = levels[i].getNumOfMisses() / total;
			missRate *= levels[i].missRate;
			Amat += missRate * levels[i].missPenalty;
		}
		return Amat;
	}
	
	public float CPI(int total, int memoryCycles) {
		float CPI = 1;
		float missRate = 1;
		for (int i = 0; i < levels.length; i++) {
			if ( i < levels.length - 1) {
				levels[i].missPenalty = levels[i+1].getHitCycles();
			}
			else {
				levels[i].missPenalty = memoryCycles;
			}
			levels[i].missRate = levels[i].getNumOfMisses() / total;
			missRate *= levels[i].missRate;
			CPI += missRate * levels[i].missPenalty;
		}
		return CPI;
	}
	
	public boolean LevelisFull(int address){
		boolean[] x = levels[address].getValid();
		for (int i = 0; i < x.length; i++) {
			if (x[i] == false) {
				return false;
			}
		}
		return true;
		
	}
	public int getFirstEmptyEntry(int address) {
		boolean[] x = levels[address].getValid();
		for (int i = 0; i < x.length; i++ ){
			if (!x[i])
				return i;
		}
		return 0;
	}
	public String[] readBlockFromMemory(MainMemory Mainmemory, String address, int level) {
		
		String[][] memory = Mainmemory.getMem();
		String[] result = new String[levels[level].getLineSize()/2];
		int startVal = Integer.parseInt(address, 2);
		
		for (int j = 0; j < result.length; j++) {
			result[j] = memory[startVal][j];
		}
		return result;
		
	}
	
	public CacheLevel[] getLevels(){
		return levels;
	}
	
	public void setLevels(CacheLevel[] levels){
		this.levels = levels;
	}
	
	
	
}
