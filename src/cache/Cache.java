package cache;

public class Cache {
	int numberOflevels;
	CacheLevel[] levels;
	public Cache(int l) {
		numberOflevels = l;
		levels = new CacheLevel[l];
	}
	
	public CacheLevel[] getLevels(){
		return levels;
	}
	
	public void setLevels(CacheLevel[] levels){
		this.levels = levels;
	}
	
}
