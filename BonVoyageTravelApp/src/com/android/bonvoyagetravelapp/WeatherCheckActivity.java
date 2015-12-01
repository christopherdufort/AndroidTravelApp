package com.android.bonvoyagetravelapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

public class WeatherCheckActivity extends Activity {

	private String country, nameOfArea = "";
	private String mainForecast, descriptionForecast = "";
	private String windSpeed = "";
	private String temp, minTemp, maxTemp = "";
	private String humidity, pressure = "";

	private DBHelper dbh;
	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weathercheck_activity);
		dbh = DBHelper.getDBHelper(this);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		/*
		 * Anything after must not be relying on the Async Task because you dont
		 * know when its gonna finish
		 */
		getWeather();
	}

	private void getWeather() {
		int tripId = prefs.getInt("current", -1);
		//int tripId = 1;

		if (tripId != -1) {
			// there is a current trip(last edited/viewed)
			Cursor locationIdCursor = dbh.getLocationIdWithTripId("" + tripId);
			locationIdCursor.moveToFirst();
			int locationId = locationIdCursor.getInt(locationIdCursor.getColumnIndex(DBHelper.COLUMN_LOCATION_ID));

			Cursor fullLocaltionCursor = dbh.getLocationById(locationId);
			fullLocaltionCursor.moveToFirst();
			String country = fullLocaltionCursor.getString(fullLocaltionCursor.getColumnIndex(DBHelper.COLUMN_CITY));
			String code = fullLocaltionCursor
					.getString(fullLocaltionCursor.getColumnIndex(DBHelper.COLUMN_COUNTRY_CODE));

			launchWeatherAPI(0.0, 0.0, country, code);

		} else {
			// no current trip
			// take weather with device location
			LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

			String locationProvider = LocationManager.NETWORK_PROVIDER;
			Location lastLocation = locationManager.getLastKnownLocation(locationProvider);

			if (lastLocation != null)
			{
				Log.d("WORKS", "location is not null");
				launchWeatherAPI(lastLocation.getLatitude(), lastLocation.getLongitude(), null, null);
			}
			else {
				//the device might not have a last known location all the time
				LocationListener listener = new myLocationListener();
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
			}
		}
	}

	private class myLocationListener implements LocationListener
	{
		@Override
		public void onLocationChanged(Location location) {
			Log.d("WORKS", "From private class");
			launchWeatherAPI(location.getLatitude(), location.getLongitude(), null, null);
		}

		@Override
		public void onProviderDisabled(String provider) { /*nothing here*/ }
		@Override
		public void onProviderEnabled(String provider) { /*nothing here*/ }
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) { /*nothing here*/ }
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
			new DownloadWeatherData().execute(url);
		}
	}

	private class DownloadWeatherData extends AsyncTask<String, Void, String> {

		@Override
		protected void onPostExecute(String result) {

			if (result.equals("")) {
				((TextView) findViewById(R.id.weatherTitle)).setText("Unable to Connect.");
			} else {
				parseResults(result);
				assignText();
			}
		}

		@Override
		protected String doInBackground(String... params) {

			InputStream input = null;
			HttpURLConnection conn = null;
			String result = null;

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
					return "";

				// get the stream for the data from the website
				input = conn.getInputStream();

				// read the stream
				char[] buffer = new char[500];
				BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
				reader.read(buffer);

				// goes to onPOstExecute()
				result = new String(buffer);

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				conn.disconnect();
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return result;
		}

	} // end of private class

	private void parseResults(String result) {

		try {
			JSONObject jsonObj = new JSONObject(result);

			JSONObject sysObj = jsonObj.getJSONObject("sys");
			country = sysObj.getString("country");

			nameOfArea = jsonObj.getString("name");

			JSONArray array = jsonObj.getJSONArray("weather");
			descriptionForecast = array.getJSONObject(0).getString("description");
			mainForecast = array.getJSONObject(0).getString("main");

			JSONObject weatherObj = jsonObj.getJSONObject("wind");
			windSpeed = "" + weatherObj.getDouble("speed");

			JSONObject mainObj = jsonObj.getJSONObject("main");
			humidity = "" + mainObj.getInt("humidity");
			pressure = "" + mainObj.getInt("pressure");
			temp = "" + mainObj.getDouble("temp");
			minTemp = "" + mainObj.getDouble("temp_min");
			maxTemp = "" + mainObj.getDouble("temp_max");

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private void assignText() {

		TextView countryView = (TextView) findViewById(R.id.locationText);
		TextView generalDescriptionView = (TextView) findViewById(R.id.generalDescriptionValue);
		TextView detailedDescriptionView = (TextView) findViewById(R.id.detailedDescriptionValue);
		TextView temperatureView = (TextView) findViewById(R.id.temperatureValue);
		TextView temperatureMinView = (TextView) findViewById(R.id.temperatureMinValue);
		TextView temperatureMaxView = (TextView) findViewById(R.id.temperatureMaxValue);
		TextView humidityView = (TextView) findViewById(R.id.humidityValue);
		TextView pressureView = (TextView) findViewById(R.id.pressureValue);
		TextView speedView = (TextView) findViewById(R.id.speedValue);

		countryView.setText(country + ", " + nameOfArea);
		generalDescriptionView.setText(mainForecast);
		detailedDescriptionView.setText(descriptionForecast);
		temperatureView.setText(temp + "\u00b0");
		temperatureMinView.setText(minTemp + "\u00b0");
		temperatureMaxView.setText(maxTemp + "\u00b0");
		humidityView.setText(humidity + "%");
		pressureView.setText(pressure + " hPa");
		speedView.setText(windSpeed + " m/s");

	}

}
