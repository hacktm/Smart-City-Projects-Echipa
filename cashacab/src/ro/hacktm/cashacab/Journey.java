package ro.hacktm.cashacab;

public class Journey {
	
	private Long startMoment;
	private Long stopMoment;
	private Double price;
	private Long idle;
	private Double distance;
	private Integer id;
	private Integer user_id;
	
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Long getStartMoment() {
		return startMoment;
	}
	public void setStartMoment(Long startMoment) {
		this.startMoment = startMoment;
	}
	public Long getStopMoment() {
		return stopMoment;
	}
	public void setStopMoment(Long stopMoment) {
		this.stopMoment = stopMoment;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public Long getIdle() {
		return idle;
	}
	public void setIdle(Long idle) {
		this.idle = idle;
	}
	public Double getDistance() {
		return distance;
	}
	public void setDistance(Double distance) {
		this.distance = distance;
	} 

	
	
	public Integer getUser_id() {
		return user_id;
	}
	public void setUser_id(Integer user_id) {
		this.user_id = user_id;
	}
	@Override
	public String toString() {
		return "Journey [startMoment=" + startMoment + ", stopMoment="
				+ stopMoment + ", price=" + price + ", idle=" + idle
				+ ", distance=" + distance + "]";
	}

}
