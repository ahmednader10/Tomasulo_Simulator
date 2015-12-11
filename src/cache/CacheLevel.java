package cache;

public class CacheLevel {
	int level;
	int levelSize;
	int hitCycles;
	int lineSize;
	int type;
	int associativityLevel;
	int hitWritePolicy;
	String[][] data;
	String[] Tags;
	boolean[] valid;
	int NumOfMisses;
	int NumOfHits;
	float AMAT;
	float missRate;
	float missPenalty;
	
	public CacheLevel(int l, int hitCycles, int L, int t, int m, int h, int S){
		levelSize = S;
		level = l;
		lineSize = L;
		type = t;
		associativityLevel = m;
		hitWritePolicy = h;
		this.hitCycles = hitCycles;
		NumOfMisses = 0;
		NumOfHits = 0;
		data = new String[S/L][lineSize];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++)
				data[i][j] = "";
		}
		Tags = new String[S/L];
		for (int i = 0; i < Tags.length; i++){
			Tags[i] = "";
		}
		valid = new boolean[S/L];
		for (int i = 0; i < valid.length; i++)
			valid[i] = false;
	}


	public int getLevelSize() {
		return levelSize;
	}


	public void setLevelSize(int levelSize) {
		this.levelSize = levelSize;
	}


	public int getNumOfHits() {
		return NumOfHits;
	}

	public void setNumOfHits(int numOfHits) {
		NumOfHits = numOfHits;
	}

	public int getNumOfMisses() {
		return NumOfMisses;
	}

	public void setNumOfMisses(int numOfMisses) {
		NumOfMisses = numOfMisses;
	}

	
		public String[][] getData() {
		return data;
	}


	public void setData(String[][] data) {
		this.data = data;
	}


	public String[] getTags() {
		return Tags;
	}


	public void setTags(String[] tags) {
		Tags = tags;
	}


	public boolean[] getValid() {
		return valid;
	}


	public void setValid(boolean[] valid) {
		this.valid = valid;
	}


		public int getLineSize() {
		return lineSize;
	}

	public void setLineSize(int lineSize) {
		this.lineSize = lineSize;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getAssociativityLevel() {
		return associativityLevel;
	}

	public void setAssociativityLevel(int associativityLevel) {
		this.associativityLevel = associativityLevel;
	}

	public int getHitWritePolicy() {
		return hitWritePolicy;
	}

	public void setHitWritePolicy(int hitWritePolicy) {
		this.hitWritePolicy = hitWritePolicy;
	}

	public int getLevel() {
		return level;
	}

	public int getHitCycles() {
		return hitCycles;
	}

	public void setHitCycles(int hitCycles) {
		this.hitCycles = hitCycles;
	}

	public void setLevel(int level) {
		this.level = level;
	}


	public float getAMAT() {
		return AMAT;
	}


	public void setAMAT(float aMAT) {
		AMAT = aMAT;
	}


	public float getMissRate() {
		return missRate;
	}


	public void setMissRate(float missRate) {
		this.missRate = missRate;
	}


	public float getMissPenalty() {
		return missPenalty;
	}


	public void setMissPenalty(float missPenalty) {
		this.missPenalty = missPenalty;
	}
	
	
}
