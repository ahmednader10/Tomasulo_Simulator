package cache;

import io.InputOutput;
import mainMemory.MainMemory;

public class Cache {
	int numberOflevels;
	CacheLevel[] levels;
	
	public Cache(int l) {
		numberOflevels = l;
		levels = new CacheLevel[l];
	}
	
	/*public void write(String address) {
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
					String[] block = getBlockFromMemory(tagValBin, indexValBin, i);
					String[] tags = levels[i].getTags();
					String[][] data = levels[i].getData();
					if(i == levels.length -1) {
						if (LevelisFull(i)) {
							tags[0] = tagValBin;
							for (int k=0; k < block.length;k++) {
								data[0][k] = block[k];
							}
							
						}
						else{
							int j = getFirstEmptyEntry(i);
							tags[j] = tagValBin;
							for (int k=0; k < block.length;k++) {
								data[j][k] = block[k];
							}
						}
						levels[i].setData(data);
						levels[i].setTags(tags);
					}
				}
			}
			if (levels[i].getType() == 1) {
				
			}
		}
	}*/
	
	public void read(String address) {
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
						String[] block = readBlockFromMemory(tagValBin, indexValBin, i);
						// write back the block in all levels of cache
						for (int j = 0; j < levels.length; j++) {
							String[] tags = levels[j].getTags();
							String[][] data = levels[j].getData();
							boolean[] valid = levels[j].getValid();
							
							
							tags[index] = tagValBin;
							valid[index] = true;
							for (int k=0; k < block.length;k++) {
								data[index][k] = block[k];
							}
							
							levels[j].setData(data);
							levels[j].setTags(tags);
							levels[j].setValid(valid);
						}
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
					String[] block = readBlockFromMemory(tagValBin, null, i);
					// write back the block in all levels of cache
					for (int j = 0; j < levels.length; j++) {
						String[] tags = levels[j].getTags();
						String[][] data = levels[j].getData();
						boolean[] valid = levels[j].getValid();
						if (LevelisFull(j)) {
							tags[0] = tagValBin;
							valid[0] = true;
							for (int k=0; k < block.length;k++) {
								data[0][k] = block[k];
							}
						}
						else{
							int m = getFirstEmptyEntry(i);
							tags[m] = tagValBin;
							valid[m] = true;
							for (int k=0; k < block.length;k++) {
								data[m][k] = block[k];
							}
						}
						
						levels[j].setData(data);
						levels[j].setTags(tags);
						levels[j].setValid(valid);
					}
				}
			}
			if(levels[i].getType() == 1){ // set associative
				
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
	public String[] readBlockFromMemory(String tag, String index, int level) {
		InputOutput io = new InputOutput();
		String address = "";
		if (index != null){
			address = tag+index;
		}
		else{
			address = tag;
		}
		int bitsleft = 16 - address.length();
		for(int i = 0; i < bitsleft; i++){
			address = address + "0";
		}
		
		MainMemory Mainmemory = io.getMemory();
		String[][] memory = Mainmemory.getMem();
		String[] result = new String[levels[level].getLineSize()/2];
		
		int startVal = Integer.parseInt(address, 2);
		
		for (int j = 0; j < result.length; j++) {
			result[j] = memory[startVal][j];
			startVal += 2;
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
