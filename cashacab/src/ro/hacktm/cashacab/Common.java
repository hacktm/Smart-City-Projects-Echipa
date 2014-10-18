package ro.hacktm.cashacab;

public class Common {
	public static Double getDistance(Double lat1, Double lng1, Double lat2, Double lng2){
		Double dist=0.0;
		if (lat1 == lat2 && lng1 == lng2)
			return null;

		double theta = lng1 - lng2;
		dist = Math.sin(degreeToRad(lat1)) * Math.sin(degreeToRad(lat2)) + Math.cos(degreeToRad(lat1)) * 
				Math.cos(degreeToRad(lat2)) * Math.cos(degreeToRad(theta));
		if (dist > 1)
			dist = 1.0;

		if (dist < -1)
			dist = -1.0;

		dist = Math.acos(dist);
		dist = radToDegree(dist);
		dist = dist * 60 * 1.1515 * 1.609344 * 1000;
		System.out.println(dist);
		return dist; 
	}

	public static Double degreeToRad(double degree){
		return degree * Math.PI / 180;
	}

	public static Double radToDegree(double rad){
		return rad * 180 / Math.PI;
	}
	public Double haversine(double lat1, double lng1, double lat2, double lng2){
		int R = 6371;
		double dLat = degreeToRad(lat2 - lat1);
		double dLng = degreeToRad(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(degreeToRad(lat1)) * 
				Math.cos(degreeToRad(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
		double  c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double   d = R * c;
		d = d * 0.62137119;
		return d;
	}
}
