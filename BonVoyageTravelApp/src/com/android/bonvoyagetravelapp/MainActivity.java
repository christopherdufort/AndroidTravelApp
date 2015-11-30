package com.android.bonvoyagetravelapp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private SharedPreferences prefs;
	private DBHelper dbh;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		dbh = DBHelper.getDBHelper(this);
		context = this;

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// first time user
		if (prefs.getString("name", null) == null) {
			showAlertNameBox();
		} else
			// second run, send saved name
			showUserName(prefs.getString("name", ""));
	}

	private void showUserName(String name) {
		TextView nameView = (TextView) findViewById(R.id.greetingText);

		// the name is from the actual input or shared prefs depending
		nameView.setText("Hello " + name);
	}

	private void showAlertNameBox() {

		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle(R.string.nameDialogTitle);
		alertDialog.setMessage(R.string.nameDialogInstruction);

		final EditText input = new EditText(this);
		alertDialog.setView(input);

		alertDialog.setPositiveButton(R.string.okBtn, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				// save it in prefs for next time and send input for now
				String name = input.getText().toString();

				if (name.matches("[a-zA-z]+")) {
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString("name", name);
					editor.commit();
					showUserName(name);
				} else {
					Toast.makeText(MainActivity.this, R.string.nameProblem, Toast.LENGTH_LONG).show();
					// by default an alert box will close when button is clicked
					// re-instanciating it to let the user enter name again
					// TOO MANY RESOURCES USED: will crasha after some spamming
					showAlertNameBox();
				}
			}
		});

		alertDialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		String url = "http://www.dawsoncollege.qc.ca/computer-science-technology/";

		if (id == R.id.menu_dawson) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
			return true;
		}

		if (id == R.id.menu_about) {
			aboutActivity(null);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void aboutActivity(View view) {
		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
	}

	public void tipCalculator(View view) {
		Intent intent = new Intent(this, TipCalculator.class);
		startActivity(intent);
	}

	public void distanceVolumeWeightConversion(View view) {
		Intent intent = new Intent(this, UnitConversionActivity.class);
		startActivity(intent);
	}

	public void launchComingSoon() {
		Intent intent = new Intent(this, ComingSoon.class);
		startActivity(intent);
	}

	public void nearMe(View view) {
		launchComingSoon();
	}

	public void currencyConversion(View view) {
		launchComingSoon();
	}

	public void budget(View view) {
		launchComingSoon();
	}

	public void localCustoms(View view) {
		launchComingSoon();
	}

	public void interests(View view) {
		launchComingSoon();
	}

	public void manageTrips(View view) {
		launchComingSoon();
	}

	@SuppressWarnings("unused")
	public void weatherCheck(View view) {
		//String tripId = prefs.getString("current", null);
		String tripId = "1";

		if (tripId != null) {
			// there is a current trip(last edited/viewed)
			// zero indexed???
			//TODO: ERROR FIX ME
			
			 Cursor locationIdCursor = dbh.getLocationIdWithTripId(tripId);
			 int locationId = locationIdCursor.getInt(0);
			 
			 Cursor fullLocaltionCursor = dbh.getLocationById(locationId);
			 String country = fullLocaltionCursor.getString(1);
			 String code = fullLocaltionCursor.getString(2);
			 
			 launchWeatherAPI(0.0,0.0,country,code);
			
		} else {
			// no current trip
			// take weather with device location
			LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
			String locationProvider = LocationManager.NETWORK_PROVIDER;
			Location lastLocation = locationManager.getLastKnownLocation(locationProvider);

			launchWeatherAPI(lastLocation.getLatitude(), lastLocation.getLongitude(), null, null);
		}
	}

	private void launchWeatherAPI(double lat, double lon, String country, String code) {
		String url = "";
		String appId = "e857fa3cafcae16ad142b30675ad2cff";

		if (country != null) {
			// from db
			url = "http://api.openweathermap.org/data/2.5/weather?q=" + country + "," + code + "&appid=" + appId
					+ "&units=metric";
		} else {
			// from device
			url = "http://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + appId
					+ "&units=metric";
		}

		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected()) {
			// calls preExecute()
			new DownloadWeatherData().execute(url);

		}
	}

	private class DownloadWeatherData extends AsyncTask<String, Void, String> {

		protected void onPostExecute(String result) {
			Intent intent = new Intent(context, WeatherCheckActivity.class);
			intent.putExtra("download", result);
			startActivity(intent);
		}

		@Override
		protected String doInBackground(String... params) {

			InputStream input;
			HttpURLConnection conn;

			try {
				URL url = new URL(params[0]);
				// create and open the connection
				conn = (HttpURLConnection) url.openConnection();

				conn.setRequestMethod("GET");

				// specifies whether this connection allows receiving data
				conn.setDoInput(true);

				// Starts the query
				conn.connect();

				int responseCode = conn.getResponseCode();

				if (responseCode != HttpURLConnection.HTTP_OK)
					return "Server returned: " + responseCode + " aborting read.";

				// get the stream for the data from the website
				input = conn.getInputStream();

				// read the stream
				char[] buffer = new char[1000];
				BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
				reader.read(buffer);

				// goes to onPOstExecute()
				return new String(buffer);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// input.close();
				// conn.disconnect();
			}
			return null;
		}

	} // end of private class
}