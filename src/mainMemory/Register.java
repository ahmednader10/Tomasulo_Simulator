package mainMemory;

public class Register {
	int[] value;
	int status;
	int Name;
	public Register(int n){
		value = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		status = -1;
		Name = n;
	}
	public Register(int[] v, int s) {
		value = v;
		status = s;
	}
	public int[] getValue() {
		return value;
	}
	public void setValue(int[] value) {
		this.value = value;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getName() {
		return Name;
	}
	public void setName(int name) {
		Name = name;
	}
	

}
