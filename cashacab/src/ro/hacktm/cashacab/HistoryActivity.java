package ro.hacktm.cashacab;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ro.hacktm.cashacab.HistoryArrayAdapter;
import ro.hacktm.cashacab.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter;

public class HistoryActivity extends ActionBarActivity {
	HistoryArrayAdapter adapter;
	ListView listView;

	// Hashmap for ListView
	List<Trip> tripList;
	List<StartCoord> startList;
	List<StopCoord> stopList;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);

		ActionBar actionBar = getSupportActionBar();
		ColorDrawable colorDrawable = new ColorDrawable(
				Color.parseColor("#ffe02f"));
		actionBar.setBackgroundDrawable(colorDrawable);
		//actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		listView = (ListView) findViewById(R.id.historyListView);
		tripList = new ArrayList<Trip>();
		new GetTrips().execute();

		//		s = new SingleRow();
		//		s.setDistance("1.5km");
		//		s.setPrice("13,78lei");
		//		s.setStartAddress("Str. Severin");
		//		s.setStopAddress("Str Aries");
		//		
		//		list = new ArrayList<SingleRow>();
		//		list.add(s);
		//		list.add(s);
		//		list.add(s);
		//		list.add(s);
		//		list.add(s);
		//		adapter = new HistoryArrayAdapter(getApplicationContext(), R.layout.history_layout, list);
		//		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent i = new Intent(HistoryActivity.this, JourneyActivity.class);
				startActivity(i);
			}
		});
	}

	private class GetTrips extends AsyncTask<Void, Void, Void> {

		private ProgressDialog pDialog;

		// URL to get contacts JSON
		private static final String url = "http://192.168.0.107:8081/webservice/get_trips?id_user=1&";

		// JSON Node names
		private static final String TAG_DISTANCE = "distance";
		private static final String TAG_STOP_MOMENT = "stop_moment";
		private static final String TAG_START_MOMENT = "start_moment";
		private static final String TAG_STOP_ADDRESS = "stop_location";
		private static final String TAG_START_ADDRESS = "start_location";
		private static final String TAG_PRICE = "price";
		private static final String TAG_IDLE = "idle";
		private static final String TAG_USER_ID = "id_user";
		private static final String TAG_TRIPS = "trips_list";
		private static final String TAG_LAT = "lat";
		private static final String TAG_LNG = "lng";

		private static final String TAG_RESULTS = "results";
		private static final String TAG_FORMADRESS = "formatted_address";

		// contacts JSONArray
		JSONArray trips = null;
		JSONArray results1 = null;
		JSONArray results2 = null;
		Trip trip= new Trip();

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Showing progress dialog
			pDialog = new ProgressDialog(HistoryActivity.this);
			pDialog.setMessage("Please wait...");
			pDialog.setCancelable(false);
			pDialog.show();

		}

		@Override
		protected Void doInBackground(Void... arg0) {
			// Creating service handler class instance
			ServiceHandler sh = new ServiceHandler();

			// Making a request to url and getting response
			String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

			Log.d("Response: ", "> " + jsonStr);
			StartCoord start = new StartCoord();;
			StopCoord stop = new StopCoord();
			if (jsonStr != null) {
				try {
					JSONObject jsonObj = new JSONObject(jsonStr);

					// Getting JSON Array node
					trips = jsonObj.getJSONArray(TAG_TRIPS);

					// looping through All Contacts
					for (int i = 0; i < trips.length(); i++) {

						JSONObject c = trips.getJSONObject(i);

						trip.setDistance(Integer.valueOf(c.getString(TAG_DISTANCE)));
						trip.setIdle(Integer.valueOf(c.getString(TAG_IDLE)));
						trip.setPrice(c.getString(TAG_PRICE));
						trip.setStart_moment(c.getString(TAG_START_MOMENT));
						trip.setStop_moment(c.getString(TAG_STOP_MOMENT));
						trip.setId_user(Integer.valueOf(c.getString(TAG_USER_ID)));                
						//   trip.setStartAddress(c.getString(TAG_START_ADDRESS));
						//                        System.out.println(trip.getStartAddress());
						//    trip.setStopAddress(c.getJSONArray((TAG_STOP_ADDRESS.)));

						JSONObject startPositions = c.getJSONObject(TAG_START_ADDRESS);
						start = new StartCoord();
						start.setLat(startPositions.getDouble(TAG_LAT));
						start.setLng(startPositions.getDouble(TAG_LNG));
						JSONObject stopPositions = c.getJSONObject(TAG_STOP_ADDRESS);
						stop.setLat(stopPositions.getDouble(TAG_LAT));
						stop.setLng(stopPositions.getDouble(TAG_LNG));
						//stopList.add(stop);
						//startList.add(start);
						//tripList.add(trip);
						//for(int i = 0 ; i<startList.size() ; i++) {
							String url1 = "https://maps.googleapis.com/maps/api/geocode/json?latlng="+start.getLat()+","+start.getLng();

							// Making a request to url and getting response
							String jsonStr1 = sh.makeServiceCall(url1, ServiceHandler.GET);


							if (jsonStr1 != null) {
								try {
									JSONObject jsonObj1 = new JSONObject(jsonStr1);

									// Getting JSON Array node
									results1 = jsonObj1.getJSONArray(TAG_RESULTS);

									// looping through All Contacts
									// for (int i = 0; i < results1.length(); i++) {
									JSONObject c1 = results1.getJSONObject(1);

									trip.setStartAddress(c1.getString(TAG_FORMADRESS));

									//tripList.add(trip);
									System.out.println(trip);
									//  }
								} catch (JSONException e) {
									e.printStackTrace();
								}
							} else {
								Log.e("ServiceHandler", "Couldn't get any data from the url");
							}
					


							String url2 = "https://maps.googleapis.com/maps/api/geocode/json?latlng="+stop.getLat()+","+stop.getLng();

							// Making a request to url and getting response
							String jsonStr2 = sh.makeServiceCall(url2, ServiceHandler.GET);


							if (jsonStr != null) {
								try {
									JSONObject jsonObj2 = new JSONObject(jsonStr2);

									// Getting JSON Array node
									results2 = jsonObj2.getJSONArray(TAG_RESULTS);

									// looping through All Contacts
									//  for (int i = 0; i < results2.length(); i++) {
									JSONObject c2 = results2.getJSONObject(1);

									trip.setStopAddress(c2.getString(TAG_FORMADRESS));         
									System.out.println(trip);
									// }
								} catch (JSONException e) {
									e.printStackTrace();
								}
							} else {
								Log.e("ServiceHandler", "Couldn't get any data from the url");
							}
							
							tripList.add(trip);  
						System.out.println(trip);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			 else {
				Log.e("ServiceHandler", "Couldn't get any data from the url");
			}


//			for(int i = 0 ; i<startList.size() ; i++) {
//				String url1 = "https://maps.googleapis.com/maps/api/geocode/json?latlng="+startList.get(i).getLat()+","+startList.get(i).getLng();
//
//				// Making a request to url and getting response
//				String jsonStr1 = sh.makeServiceCall(url1, ServiceHandler.GET);
//
//
//				if (jsonStr != null) {
//					try {
//						JSONObject jsonObj = new JSONObject(jsonStr1);
//
//						// Getting JSON Array node
//						results1 = jsonObj.getJSONArray(TAG_RESULTS);
//
//						// looping through All Contacts
//						// for (int i = 0; i < results1.length(); i++) {
//						JSONObject c = results1.getJSONObject(1);
//
//						trip.setStartAddress(c.getString(TAG_FORMADRESS));
//
//						//tripList.add(trip);
//						System.out.println(trip);
//						//  }
//					} catch (JSONException e) {
//						e.printStackTrace();
//					}
//				} else {
//					Log.e("ServiceHandler", "Couldn't get any data from the url");
//				}
//			}
//
//			for(int i = 0 ; i<stopList.size() ; i++) {
//				String url2 = "https://maps.googleapis.com/maps/api/geocode/json?latlng="+stopList.get(i).getLat()+","+stopList.get(i).getLng();
//
//				// Making a request to url and getting response
//				String jsonStr2 = sh.makeServiceCall(url2, ServiceHandler.GET);
//
//
//				if (jsonStr != null) {
//					try {
//						JSONObject jsonObj = new JSONObject(jsonStr2);
//
//						// Getting JSON Array node
//						results2 = jsonObj.getJSONArray(TAG_RESULTS);
//
//						// looping through All Contacts
//						//  for (int i = 0; i < results2.length(); i++) {
//						JSONObject c = results2.getJSONObject(1);
//
//						trip.setStopAddress(c.getString(TAG_FORMADRESS));
//						tripList.add(trip);              
//						System.out.println(trip);
//						// }
//					} catch (JSONException e) {
//						e.printStackTrace();
//					}
//				} else {
//					Log.e("ServiceHandler", "Couldn't get any data from the url");
//				}
//			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// Dismiss the progress dialog
			
			/**
			 * Updating parsed JSON data into ListView
			 * */
			//            ListAdapter adapter = new SimpleAdapter(
			//                    HistoryActivity.this, tripList,
			//                    R.layout.list_item, new String[] { trip.getDistance(), TAG_START_MOMENT,
			//                            TAG_IDLE }, new int[] { R.id.name,
			//                            R.id.email, R.id.mobile });

			HistoryArrayAdapter adapter = new HistoryArrayAdapter(
					HistoryActivity.this, 
					R.layout.history_layout, tripList);
			listView.setAdapter(adapter);

			if (pDialog.isShowing())
				pDialog.dismiss();
		}

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
