package ro.hacktm.cashacab;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ro.hacktm.cashacab.R;
import ro.hacktm.cashacab.R.id;
import ro.hacktm.cashacab.R.layout;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {
	static EditText username, password;
	Button loginButton;
	TextView title;
	public static Tariffs tariff;
	public static String url;
	public static UserInfo useri;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		title = (TextView) findViewById(R.id.title);
		username = (EditText) findViewById(R.id.usernameEditText);
		password = (EditText) findViewById(R.id.passwordEditText);
		loginButton = (Button) findViewById(R.id.buttonLogin);
		//username.setText("text");
		//password.setText("text");
		loginButton.setFocusableInTouchMode(true);
		loginButton.requestFocus();
		//		ActionBar actionBar =getSupportActionBar();
		//		
		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/erasbd.ttf");
		title.setTypeface(tf);
		loginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String user = username.getText().toString();
				String pass = password.getText().toString();

				if(user.equals("")){
					Toast.makeText(getApplicationContext(), "Please enter your username", Toast.LENGTH_SHORT).show();
					return;
				}else
					if(pass.equals("")){
						Toast.makeText(getApplicationContext(), "Please enter your password", Toast.LENGTH_SHORT).show();
						return;
					}

				url = "http://192.168.0.107:8081/webservice/login?username="+user+"&pswd="+pass+"&";

				new GetLogin().execute();

			}
		});


	}

	private class GetLogin extends AsyncTask<Void, Void, Void> {

		private ProgressDialog pDialog;

		// URL to get contacts JSON


		// JSON Node names
		private static final String TAG_STATUS = "status";
		private static final String TAG_TARIFFS = "tarrifs";
		private static final String TAG_USER_INFO = "user_info";
		private static final String TAG_CITY_PRICE = "city";
		private static final String TAG_CITY_NIGHT_PRICE = "hwy";
		private static final String TAG_STATIONARY_PRICE= "stationary";
		private static final String TAG_OUTSIDE_PRICE = "day";
		private static final String TAG_OUTSIDE_NIGHT_PRICE = "night";
		private static final String TAG_NAME = "name";
		private static final String TAG_USER_ID = "id_user";


		// contacts JSONArray
		//	    JSONArray uinfo = null;
		//	    JSONArray tariffsJSON = null;
		Login login;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Showing progress dialog
			pDialog = new ProgressDialog(LoginActivity.this);
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

			if (jsonStr != null) {
				try {
					JSONObject jsonObj = new JSONObject(jsonStr);

					// Getting JSON Array node
					//uinfo = jsonObj.getJSONArray(TAG_USER_INFO);
					// tariffsJSON = jsonObj.getJSONArray(TAG_TARIFFS);

					// looping through All Contacts
					// for (int i = 0; i < trips.length(); i++) {

					login = new Login();
					tariff = new Tariffs();
					useri = new UserInfo();

					// JSONObject c = trips.getJSONObject(i);

					login.setStatus(jsonObj .getString(TAG_STATUS));

					JSONObject tariffs = jsonObj .getJSONObject(TAG_TARIFFS);
					tariff.setCityPrice(tariffs.getString(TAG_CITY_PRICE));
					tariff.setStationaryPrice(tariffs.getString(TAG_STATIONARY_PRICE));
					tariff.setCityNightPrice(tariffs.getString(TAG_CITY_NIGHT_PRICE));
					tariff.setOutsidePrice(tariffs.getString(TAG_OUTSIDE_PRICE));
					tariff.setOutsideNightPrice(tariffs.getString(TAG_OUTSIDE_NIGHT_PRICE));

					JSONObject userinfo = jsonObj .getJSONObject(TAG_USER_INFO);

					useri.setName(userinfo.getString(TAG_NAME));
					useri.setUser_id(userinfo.getString(TAG_USER_ID));
					//	                        trip.setStartAddress(c.getString(TAG_START_ADDRESS));
					//	                        trip.setStopAddress(c.getString(TAG_STOP_ADDRESS));
					//System.out.println(trip);
					//  }
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Log.e("ServiceHandler", "Couldn't get any data from the url");
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// Dismiss the progress dialog
			if (pDialog.isShowing())
				pDialog.dismiss();
			/**
			 * Updating parsed JSON data into ListView
			 * */
			if(!login.getStatus().equals(""))
				if(login.getStatus().equalsIgnoreCase("ok")){

					Intent i = new Intent(LoginActivity.this, MainActivity.class);
					startActivity(i);
					finish();
				}
				else
				{
					Toast.makeText(getApplicationContext(), "Username or password does not match", Toast.LENGTH_SHORT).show();
				}

		}

	}
}
