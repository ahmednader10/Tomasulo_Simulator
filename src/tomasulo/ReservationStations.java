package tomasulo;

import entries.station;

public class ReservationStations {
	station[] stations;
	int count;
	public ReservationStations(int c) {
		count = c;
		stations = new station[count];
	}
	public station[] getStations() {
		return stations;
	}
	public void setStations(station[] stations) {
		this.stations = stations;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	
	
}
