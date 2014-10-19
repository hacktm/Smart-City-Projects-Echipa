package ro.hacktm.cashacab;

public class Trip {
   private Integer distance;
    private String stop_moment;
    private String start_moment;
    private String price;
    private Integer idle;
    private Integer id_user;
    private Integer trip_id;
    private String startAddress;
    private String stopAddress;
    
	public Integer getDistance() {
		return distance;
	}
	public void setDistance(Integer distance) {
		this.distance = distance;
	}
	public String getStop_moment() {
		return stop_moment;
	}
	public void setStop_moment(String stop_moment) {
		this.stop_moment = stop_moment;
	}
	public String getStart_moment() {
		return start_moment;
	}
	public void setStart_moment(String start_moment) {
		this.start_moment = start_moment;
	}
	public String getStartAddress() {
		return startAddress;
	}
	public String getStopAddress() {
		return stopAddress;
	}
	public void setStopAddress(String stopAddress) {
		this.stopAddress = stopAddress;
	}
	public void setStartAddress(String startAddress) {
		this.startAddress = startAddress;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public Integer getIdle() {
		return idle;
	}
	public void setIdle(Integer idle) {
		this.idle = idle;
	}
	public Integer getId_user() {
		return id_user;
	}
	public void setId_user(Integer id_user) {
		this.id_user = id_user;
	}
	public Integer getTrip_id() {
		return trip_id;
	}
	public void setTrip_id(Integer trip_id) {
		this.trip_id = trip_id;
	}
	
	@Override
	public String toString() {
		return "Trip [distance=" + distance + ", stop_moment=" + stop_moment
				+ ", start_moment=" + start_moment + ", price=" + price
				+ ", idle=" + idle + ", id_user=" + id_user + ", trip_id="
				+ trip_id + ", startAddress=" + startAddress + ", stopAddress="
				+ stopAddress + "]";
	}
}
