package mainMemory;

import java.util.HashMap;

public class GPRegisters {
	HashMap<String, int[]> GPRs = new HashMap();
	public GPRegisters() {
		int[] x = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1};
		GPRs.put("reg0", x);
		GPRs.put("reg1", x);
		GPRs.put("reg2", x);
		GPRs.put("reg3", x);
		GPRs.put("reg4", x);
		GPRs.put("reg5", x);
		GPRs.put("reg6", x);
		GPRs.put("reg7", x);
	}
	public HashMap<String, int[]> getGPRs() {
		return GPRs;
	}
	public void setGPRs(HashMap<String, int[]> gPRs) {
		GPRs = gPRs;
	}
	

}
