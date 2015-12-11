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
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

/**
 * This class implements an activity that will display 
 * the current weather information for your current location.
 * It shows details like a general weather description, the temperature,
 * the humidity, the pressure and the wind speed.
 * 
 *  @author Irina Patrocinio Frazao, Christopher Dufort and Annie So
 */
public class WeatherCheckActivity extends Activity {

	private String country, nameOfArea = "";
	private String mainForecast, descriptionForecast = "";
	private String windSpeed = "";
	private String temp, minTemp, maxTemp = "";
	private String humidity, pressure = "";

	private DBHelper dbh;
	private SharedPreferences prefs;

	/**
	 * This method sets the layout and starts the 
	 * process of downloading the necessary weather data.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weathercheck_activity);
		
		dbh = DBHelper.getDBHelper(this);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		/*since this will run in the background,
		 * nothing directly after must rely on it*/
		getWeather();
	}

	/**
	 * This method gets the user's location, whether it is
	 * by the current trip's location or the device's 
	 * location and sends the appropriate data to another method.
	 */
	private void getWeather() {
		int tripId = prefs.getInt("CURRENTTRIP", -1);

		if (tripId != -1) {
			// there is a current trip(last edited/viewed)
			
			//gets location id
			Cursor locationIdCursor = dbh.getLocationIdByTripId("" + tripId);
			locationIdCursor.moveToFirst();
			int locationId = locationIdCursor.getInt(locationIdCursor.getColumnIndex(DBHelper.COLUMN_LOCATION_ID));

			//gets the full location with id
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

			//calls method with latitude and longitude
			if (lastLocation != null)
				launchWeatherAPI(lastLocation.getLatitude(), lastLocation.getLongitude(), null, null);
			else {
				//the device might not have a last known location all the time
				LocationListener listener = new myLocationListener();
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
			}
		}
	}

	/**
	 * This private inner class is needed to implement the LocationListener.
	 * If the device doesn't have an actual location, it is used to call 
	 * the api everytime the device's location changes. 
	 * 
	 * @author Irina Patrocinio Frazao, Christopher Dufort and Annie So
	 */
	private class myLocationListener implements LocationListener
	{
		@Override
		public void onLocationChanged(Location location) {
			launchWeatherAPI(location.getLatitude(), location.getLongitude(), null, null);
		}

		@Override
		public void onProviderDisabled(String provider) { /*unimplemented*/ }
		@Override
		public void onProviderEnabled(String provider) { /*unimplemented*/ }
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) { /*unimplemented*/ }
	}

	/**
	 * This method creates the appropriate url for the weather
	 * api and sends it to the private AsyncTask class.
	 * 
	 * @param lat
	 * 			the latitude of the device, if applicable
	 * @param lon
	 * 			the longitude of the device, if applicable
	 * @param country
	 * 			the country of the location, if applicable
	 * @param code
	 * 			THE country code of the location, if applicable
	 */
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

		//this calls the onPreExecute of the AsyncTask
		if (networkInfo != null && networkInfo.isConnected()) {
			new DownloadWeatherData().execute(url);
		}
	}

	/**
	 * This private class implements AsyncTask to get the information from the api,
	 * parse the results and assign them to the input fields.
	 * 
	 * @author Irina Patrocinio Frazao, Christopher Dufort and Annie So
	 */
	private class DownloadWeatherData extends AsyncTask<String, Void, String> {

		/**
		 * This method takes the result from the api
		 *  call and passes it to the parsing method
		 */
		@Override
		protected void onPostExecute(String result) {

			if (result.equals("")) {
				((TextView) findViewById(R.id.weatherTitle)).setText("Unable to Connect.");
			} else {
				parseResults(result);
				assignText();
			}
		}

		/**
		 * This method opens a connection with the url 
		 * and reads the input from the api.
		 */
		@Override
		protected String doInBackground(String... params) {

			InputStream input = null;
			HttpURLConnection conn = null;
			String result = null;

			try {
				//the url passed to the method
				URL url = new URL(params[0]);
				
				// create and open the connection
				conn = (HttpURLConnection) url.openConnection();

				conn.setRequestMethod("GET");

				// specifies whether this connection allows receiving data
				conn.setDoInput(true);

				// Starts the query
				conn.connect();

				int responseCode = conn.getResponseCode();

				//the connection has a problem
				if (responseCode != HttpURLConnection.HTTP_OK)
					return "";

				// get the stream for the data from the website
				input = conn.getInputStream();

				// read the stream
				char[] buffer = new char[500];
				BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
				reader.read(buffer);

				// sends result to onPostExecute()
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

	/**
	 * This method takes the string that came from the api call,
	 * creates a JSON object with it and parses it to extract
	 * the information that we need and save it in private fields.
	 * 
	 * @param result
	 * 			the result coming from the api to be parsed.
	 */
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

	/**
	 * This method gets the references to all the 
	 * input elements and assign them with the appropriate value.
	 */
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
