package ro.hacktm.cashacab;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class JourneyActivity extends ActionBarActivity {

	private double lastLat1=0.0;
	private double lastLng1=0.0;
	private GoogleMap myMap;

	//    private static final LocationRequest REQUEST = LocationRequest.create()
	//            .setInterval(5000)         // 5 seconds
	//            .setFastestInterval(16)    // 16ms = 60fps
	//            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	TextView startStreet, stopStreet, totalDistance, totalPrice, totalIdle, journeyNumber, startTime, stopTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_journey);

		startStreet = (TextView) findViewById(R.id.startPlace);
		stopStreet = (TextView) findViewById(R.id.stopStreet);
		totalDistance = (TextView) findViewById(R.id.distance);
		totalPrice = (TextView) findViewById(R.id.price);
		totalIdle = (TextView) findViewById(R.id.idleTime);
		journeyNumber = (TextView) findViewById(R.id.journeyNumber);
		startTime = (TextView) findViewById(R.id.startTime);
		stopTime = (TextView) findViewById(R.id.stopTime);


		ActionBar actionBar = getSupportActionBar();
		ColorDrawable colorDrawable = new ColorDrawable(
				Color.parseColor("#ffe02f"));
		actionBar.setBackgroundDrawable(colorDrawable);
		//actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		FragmentManager myFragmentManager = getFragmentManager();
		MapFragment myMapFragment 
		= (MapFragment)myFragmentManager.findFragmentById(R.id.map);
		myMap = myMapFragment.getMap();

		int minTime = 6000;
		// The minimum distance (in meters) traveled until you will be notified
		float minDistance = 1;
		// Create a new instance of the location listener
		MyLocationListener myLocListener = new MyLocationListener();
		// Get the location manager from the system
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Get the criteria you would like to use
		Criteria criteria = new Criteria();
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setSpeedRequired(false);

		// Get the best provider from the criteria specified, and false to say it can turn the provider on if it isn't already
		String bestProvider = locationManager.getBestProvider(criteria, false);
		// Request location updates
		locationManager.requestLocationUpdates(bestProvider, minTime, minDistance, myLocListener);
		Location location = locationManager.getLastKnownLocation(bestProvider);
		
			/*lastLat1 = location.getLatitude();
			lastLng1 = location.getLongitude();
			Double lat = lastLat1;
			Double lng = lastLng1;
		*/

		//myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 14f));
		// Get back the mutable Polyline
		

		List<Positions> lst= new ArrayList<Positions>();
		lst=getPositon();
		myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lst.get(0).getLat(), lst.get(0).getLng()), 14f));
		myMap.addMarker(new MarkerOptions().position(
				new LatLng(lst.get(0).getLat(), lst.get(0).getLng())).icon(
                BitmapDescriptorFactory.defaultMarker()));
		
		for(int i=1;i!=lst.size();i++){
			Polyline line = myMap.addPolyline(new PolylineOptions()
			.add(new LatLng(lst.get(i-1).getLat(), lst.get(i-1).getLng()), new LatLng(lst.get(i).getLat(), lst.get(i).getLng()))
			.width(10)
			.color(Color.BLUE));
		}
		int size = lst.size();
		myMap.addMarker(new MarkerOptions().position(
				new LatLng(lst.get(size-1).getLat(), lst.get(size-1).getLng())).icon(
                BitmapDescriptorFactory.defaultMarker()));

	}
	private class MyLocationListener implements LocationListener
	{
		@Override
		public void onLocationChanged(Location loc)
		{
			if (loc != null)
			{
				// Do something knowing the location changed by the distance you requested
				double ret=Common.getDistance(lastLat1, lastLat1,loc.getLatitude(),loc.getLongitude());
				Toast.makeText(getApplicationContext(), ret+"", Toast.LENGTH_SHORT).show();

				lastLat1=loc.getLatitude();
				lastLng1 = loc.getLongitude();

			}
		}

		@Override
		public void onProviderDisabled(String arg0)
		{
			// Do something here if you would like to know when the provider is disabled by the user
		}

		@Override
		public void onProviderEnabled(String arg0)
		{
			// Do something here if you would like to know when the provider is enabled by the user
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2)
		{
			// Do something here if you would like to know when the provider status changes
		}
	}
	//
	//	@Override
	//	public boolean onCreateOptionsMenu(Menu menu) {
	//		// Inflate the menu; this adds items to the action bar if it is present.
	//		getMenuInflater().inflate(R.menu.journey, menu);
	//		return true;
	//	}
	//

	
		public List<Positions> getPositon(){
			
		
		List<Positions> lista = new ArrayList<Positions>();

		ReaderCVS obj = new ReaderCVS();
		//   			obj.run();

			//BufferedReader br;
			//String csvFile = getAssets().open("positions/trajectory.csv");
			BufferedReader br = null;
			String line = "";
			String cvsSplitBy = ",";

			try {
				int count=0;
				br = new BufferedReader(new InputStreamReader(getAssets().open("positions/trajectory.csv")));
				while ((line = br.readLine()) != null) {
					Positions p = new Positions();
					count ++;
					// use comma as separator
					String[] country = line.split(cvsSplitBy);
					p.setLat(Double.valueOf(country[2]));
					p.setLng(Double.valueOf(country[3]));

					lista.add(p);



				}
				System.out.println(count);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			System.out.println("Done");
			System.out.println(lista);

		return lista;
		
		
	}	 


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();

		if (itemId == android.R.id.home) {
			// app icon in action bar clicked; go home
			finish();

		}
		return false;
	}
}
