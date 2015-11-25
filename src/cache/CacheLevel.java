package cache;

public class CacheLevel {
	int level;
	int cacheSize;
	int lineSize;
	int type;
	int associativityLevel;
	String hitWritePolicy;
	String missWritePolicy;
	int hitCycles;
	
	public CacheLevel(int l, int S, int L, int t, int m, String hitP, String missP, int hitCycles){
		level = l;
		cacheSize = S;
		lineSize = L;
		type = t;
		associativityLevel = m;
		hitWritePolicy = hitP;
		missWritePolicy = missP;
		this.hitCycles = hitCycles;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}

	public int getLineSize() {
		return lineSize;
	}

	public void setLineSize(int lineSize) {
		this.lineSize = lineSize;
	}

	public int getAssociativityLevel() {
		return associativityLevel;
	}

	public void setAssociativityLevel(int associativityLevel) {
		this.associativityLevel = associativityLevel;
	}

	public String getHitWritePolicy() {
		return hitWritePolicy;
	}

	public void setHitWritePolicy(String hitWritePolicy) {
		this.hitWritePolicy = hitWritePolicy;
	}

	public String getMissWritePolicy() {
		return missWritePolicy;
	}

	public void setMissWritePolicy(String missWritePolicy) {
		this.missWritePolicy = missWritePolicy;
	}

	public int getHitCycles() {
		return hitCycles;
	}

	public void setHitCycles(int hitCycles) {
		this.hitCycles = hitCycles;
	}
	
}
