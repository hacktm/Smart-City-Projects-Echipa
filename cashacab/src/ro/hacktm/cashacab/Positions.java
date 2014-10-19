package ro.hacktm.cashacab;

public class Positions {
	@Override
	public String toString() {
		return "Positions [lat=" + lat + ", lng=" + lng + "]";
	}
	public Double getLat() {
		return lat;
	}
	public void setLat(Double lat) {
		this.lat = lat;
	}
	public Double getLng() {
		return lng;
	}
	public void setLng(Double lng) {
		this.lng = lng;
	}
	
	private Double lat;
	private Double lng;
	

}
