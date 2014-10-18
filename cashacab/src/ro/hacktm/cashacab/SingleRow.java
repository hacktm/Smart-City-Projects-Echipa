package ro.hacktm.cashacab;

public class SingleRow {
	
	String startAddress;
	String stopAddress;
	String distance;
	String price;
	
	public SingleRow() {

	}

	SingleRow(String startAddress, String stopAddress, String distance, String price) {
		this.startAddress = startAddress;
		this.stopAddress = stopAddress;
		this.distance = distance;
		this.price = price;
	}
	
	public String getStartAddress() {
		return startAddress;
	}

	public void setStartAddress(String startAddress) {
		this.startAddress = startAddress;
	}

	public String getStopAddress() {
		return stopAddress;
	}

	public void setStopAddress(String stopAddress) {
		this.stopAddress = stopAddress;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

}