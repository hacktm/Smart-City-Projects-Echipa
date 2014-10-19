package ro.hacktm.cashacab;

public class Tariffs {
	private String cityPrice;
	private String stationaryPrice;
	private String cityNightPrice;
	private String outsidePrice;
	private String outsideNightPrice;
	
	public String getCityPrice() {
		return cityPrice;
	}
	public void setCityPrice(String cityPrice) {
		this.cityPrice = cityPrice;
	}
	public String getStationaryPrice() {
		return stationaryPrice;
	}
	public void setStationaryPrice(String stationaryPrice) {
		this.stationaryPrice = stationaryPrice;
	}
	public String getCityNightPrice() {
		return cityNightPrice;
	}
	public void setCityNightPrice(String cityNightPrice) {
		this.cityNightPrice = cityNightPrice;
	}
	public String getOutsidePrice() {
		return outsidePrice;
	}
	public void setOutsidePrice(String outsidePrice) {
		this.outsidePrice = outsidePrice;
	}
	public String getOutsideNightPrice() {
		return outsideNightPrice;
	}
	public void setOutsideNightPrice(String outsideNightPrice) {
		this.outsideNightPrice = outsideNightPrice;
	}
}
