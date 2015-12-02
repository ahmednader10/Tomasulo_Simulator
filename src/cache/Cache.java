package cache;

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
	
	public void read(String address, MainMemory memory) {
		
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
