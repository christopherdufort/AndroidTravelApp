package com.android.bonvoyagetravelapp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * ManageTrips Activity is the primary activity used for syncing and managing
 * the users list of trips. From this activity the user and resync all his trips
 * with the server. This activity will also display all trips associated with
 * said user and allow them to select a trip.
 * 
 * @author Irina Patrocinio Frazao
 * @author Christopher Dufort
 * @author Annie So
 * @since JDK 1.6
 * @version 1.0.0-Release
 */
public class ManageTripsActivity extends Activity {

	private String urlString, jsonStr;
	private static DBHelper dbh;
	private ListView lv;
	private TextView tv;
	private SimpleCursorAdapter sca;
	private Cursor cursor;

	// Used for long term storage.
	private SharedPreferences prefs;

	/**
	 * Overriden onCreate method used to set up the view. This method will
	 * retrieve user name from preferences in order to provide a custom
	 * ownership feel.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_trips);
		dbh = DBHelper.getDBHelper(this);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		// Custom ownership feel show current users name.
		tv = (TextView) findViewById(R.id.all_trips_name);
		tv.setText(prefs.getString("name", ""));

		// Setup multiple unique click events available trips displayed.
		setUpListeners();
	}

	/**
	 * Overwritten onResume() life cycle used to call updateView Also calls
	 * update view.
	 */
	@Override
	public void onResume() {
		super.onResume();
		updateView();
	}

	/**
	 * UpdateView method called to renew resources, and refresh the view.
	 */
	private void updateView() {

		// change cursor so the data view will be updated
		cursor = dbh.getAllTrips();

		// have the adapter use the new cursor, changeCursor closes old cursor
		// too
		sca.changeCursor(cursor);

		// Adapter tell the observers of change in data set.
		sca.notifyDataSetChanged();
	}

	/**
	 * Overwritten onPause() life cycle method called to release resources.
	 */
	@Override
	public void onPause() {
		super.onPause();
		// Release resources.
		dbh.close();
	}

	/**
	 * This method is called to set up the simple cursor adapter and link it to
	 * the DB cursor. This method is also responsible for setting up event
	 * listeners on those list items. Short click will display details of a
	 * trip.
	 */
	private void setUpListeners() {
		lv = (ListView) findViewById(R.id.listViewAllTrips);

		// Column data to use
		String[] from = { DBHelper.COLUMN_NAME, DBHelper.COLUMN_DESCRIPTION };
		// View Widgets to populate
		int[] to = { R.id.text_view_trip_name, R.id.text_view_trip_description };

		Cursor cursor = dbh.getAllTrips();

		sca = new SimpleCursorAdapter(this, R.layout.activity_manage_trips_list, cursor, from, to, 0);

		lv.setAdapter(sca);

		// Event listener for short clicks will open details of trip in new
		// activity.
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Cursor cursortemp = (Cursor) parent.getItemAtPosition(position);

				int tripId = cursortemp.getInt(0);
				String tripName = cursortemp.getString(5);

				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt("CURRENTTRIP", tripId);
				editor.putString("CURRENTTRIPNAME", tripName);
				editor.commit();

				// Fire intent to Itinerary activity with bundle describing who
				// called it.
				Intent intent = new Intent(getApplicationContext(), ItineraryActivity.class);
				intent.putExtra("MANAGE", true);
				startActivity(intent);
			}
		});
	}

	/**
	 * syncTask is an event handler for the sync trip button in the manage trip
	 * activity. This method is responsible for updating the local SQLite db
	 * with changes made server side.
	 * 
	 * @param view
	 *            the view that triggered this event handler (button click)
	 */
	public void syncTrips(View view) {
		Toast toast = Toast.makeText(this, "Syncing with server", Toast.LENGTH_SHORT);
		toast.show();
		launchSyncAPITrips();
	}

	/**
	 * launchSyncAPITrips called when user requested all trips to be refreshed
	 * with those from server This method will make a Post requests to server
	 * side RESTFUL API with email and password taken from settings. Results if
	 * successful will include JSON objects filled with trips and expenses for
	 * user along side other table data needed to populate local sqlite db. URL
	 * is hardcoded. JSONObject class used to form JSON Objects.
	 * Uses connectivity manager and calls execution of async inner class.
	 */
	private void launchSyncAPITrips() {
		urlString = "https://travel-bonvoyage.rhcloud.com/apiTrips";

		// Taken from settings.
		String email = prefs.getString("email", "");
		String password = prefs.getString("password", "");

		// used to build json data
		JSONObject jsonData = new JSONObject();

		try {
			jsonData.put("email", email);
			jsonData.put("password", password);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// string is used for transmission
		jsonStr = jsonData.toString();

		// first check to see if we can get on the network using the
		// connectivity manager
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			// invoke the AsyncTask to do the dirty work with background task
			// and messaging.
			new DownloadTripsData().execute(urlString, jsonStr);
		} else {
			Toast toast = Toast.makeText(this, "No network connection available.", Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	/**
	 * DownloadTripsData class entension of AsyncTask Uses AsyncTask to create a
	 * task away from the main UI thread. This task takes a URL string and uses
	 * it to create an HttpsUrlConnection. Once the connection has been
	 * established, the AsyncTask downloads the contents of the webpage via an
	 * an InputStream. The InputStream is converted into a JSON object and JSON
	 * Array which are parsed to then seed the local db.
	 */
	private class DownloadTripsData extends AsyncTask<String, Void, String> {

		// onPreExecute log some info make sure url and data are good
		// runs in calling thread (in UI thread)
		protected void onPreExecute() {
			Log.d("onPreExecute", "url " + urlString);
			Log.d("onPreExecute", "json " + jsonStr);
		}

		/**
		 * runs in background (not in UI thread)
		 */
		@Override
		protected String doInBackground(String... params) {
			try {
				return downloadData(params);
			} catch (IOException e) {
				return "Unable to retrieve web page. URL may be invalid. " + e.getMessage();
			} catch (JSONException e) {
				return "Returned JSON object/array or json parse is malformed. " + e.getMessage();
			}
		}

		/**
		 * onPostExecute logs the results of the AsyncTask. runs in calling
		 * thread (in UI thread) and calls update view.
		 */
		@Override
		protected void onPostExecute(String result) {

			Log.d("Results", result);
			Toast toast = Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG);
			toast.show();

			updateView();

		}
	} // End AsyncTask DownloadTripsData()

	/**
	 * Given a URL, establishes an HttpUrlConnection and retrieves the web page
	 * content as a InputStream, which it sends to be parsed and returns if it was successful. 
	 *
	 * @param params
	 *            String url and string representation of data to be sent.
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	private String downloadData(String... params) throws IOException, JSONException {
		InputStream is = null;
		OutputStream out;
		String contentAsString = "";
		int response;
		URL url;
		// Website api support Https - ssl
		HttpsURLConnection conn = null;

		byte[] bytes = params[1].getBytes("UTF-8");
		Integer bytesLeng = bytes.length;

		try {
			url = new URL(params[0]);
		} catch (MalformedURLException e) {
			Log.d("HttpsURLPOST", e.getMessage());
			return "ERROR call the developer: " + e.getMessage();
		}
		try {
			// create and open the connection
			conn = (HttpsURLConnection) url.openConnection();

			// output = true, uploading POST data
			// input = true, downloading response to POST
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");

			conn.setReadTimeout(10000);
			conn.setConnectTimeout(15000 /* milliseconds */);

			conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			// set length of POST data to send
			conn.addRequestProperty("Content-Length", bytesLeng.toString());

			// create the post message out
			out = new BufferedOutputStream(conn.getOutputStream());

			// Send the message
			out.write(bytes);
			out.flush();
			out.close();

			// retrieve response code
			response = conn.getResponseCode();

			/*
			 * check the status code HTTP_OK = 200 anything else we didn't get
			 * what we want in the data.
			 */
			if (response != HttpURLConnection.HTTP_OK) {
				Log.d("HttpsURLPOST", "Server returned: " + response + " aborting read.");
				if (response == HttpURLConnection.HTTP_UNAUTHORIZED)
					return "Server returned: " + response + " invalid credentials - aborting read.";
				else
					return "Server returned: " + response + " aborting read.";
			}
			Log.d("downloadData", "Downloading new data tables dropped and recreated");
			dbh.recreateAllTables();
			is = conn.getInputStream();
			// parse response
			contentAsString = readIt(is);
			return contentAsString;

		} finally {
			// Make sure that the input stream used for reading is closed after
			// the app is finished
			// using it.
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// Wont happen , but not a good idea to ignore an exception.
					e.printStackTrace();
				}
			}
			// * Make sure the connection is closed after the app is finished
			// using it.
			if (conn != null) {
				try {
					conn.disconnect();
				} catch (IllegalStateException e) {
					// Wont happen , but not a good idea to ignore an exception.
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 
	 * Reads stream from HTTP connection and sends data to a local method to
	 * populate db. Using stringBuilder and JSON Objects. This method will
	 * formulate multiple JSONArrays and JSONObjects from the contents of the
	 * response message. This method currently parses one huge response, in the
	 * future we should make multiple smaller requests.
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 * @throws JSONException
	 */
	private String readIt(InputStream stream) throws IOException, UnsupportedEncodingException, JSONException {
		StringBuilder responseStrBuilder = new StringBuilder();
		BufferedReader reader = null;

		// Read stream from https
		reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

		String line = null;
		while ((line = reader.readLine()) != null) {
			// use string builder
			Log.d("RequestResponse", line);
			// I'm parsing the JSON
			responseStrBuilder.append(line);
			// turn the string response into a large jsonObject
			JSONObject jsonObject = new JSONObject(responseStrBuilder.toString());

			// retrieve individual json arrays from the object.
			JSONArray tripsArray = jsonObject.getJSONArray("trips");
			JSONArray budgetedArray = jsonObject.getJSONArray("budgeted");
			JSONArray locationsArray = jsonObject.getJSONArray("locations");
			JSONArray categoriesArray = jsonObject.getJSONArray("categories");
			JSONArray actualArray = jsonObject.getJSONArray("actualExpenses");

			// Seed the local db with contents of each array.
			syncLocalDb(tripsArray, budgetedArray, locationsArray, categoriesArray, actualArray);

		}
		return "Successful Sync";

	} // readIt()

	/**
	 * I apologize to anyone in advance for the mess of a method this is.
	 * 
	 * private method syncLocalDb called to fill the local sqlitedb with
	 * contents of serverside db associated with particular user. This method
	 * accepts multiple arrays and loops through each list in tandem to insert
	 * with proper foreignkey structure. In the future this method should be
	 * broken up into several smaller methods based on several smaller requests.
	 * 
	 * @param tripsArray
	 *            A JSONArray of all trips associated with user who requested
	 *            sync.
	 * @param budgetedArray
	 *            A JSONArray of all budgeted expenses associated with user who
	 *            requested sync.
	 * @param locationsArray
	 *            A JSONArray of all locations used to update local db.
	 * @param categoriesArray
	 *            A JSONArray of all categories used to update local db.
	 * @param actualArray
	 *            A JSONArray of all actual expenses associated with user who
	 *            requested sync.
	 * @throws JSONException
	 */
	private void syncLocalDb(JSONArray tripsArray, JSONArray budgetedArray, JSONArray locationsArray,
			JSONArray categoriesArray, JSONArray actualArray) throws JSONException {

		// Loop through each item in locations array inserting it into the db
		for (int l = 0; l < locationsArray.length(); l++) {
			String city = locationsArray.getJSONObject(l).getString("city");
			String country_code = locationsArray.getJSONObject(l).getString("country_code");
			String province = locationsArray.getJSONObject(l).getString("province");
			dbh.createLocation(city, country_code, province);

		}

		// Loop through each item in categoies array inserting it into the db.
		for (int c = 0; c < categoriesArray.length(); c++) {
			String category = categoriesArray.getJSONObject(c).getString("category");
			dbh.createCategory(category);
		}

		int insertedBudgetedId = -1;
		int budgeted_id = -1;
		// Loop through each item in trips array insertting it into the db
		for (int i = 0; i < tripsArray.length(); i++) {
			int tripId = tripsArray.getJSONObject(i).getInt("id");
			String description = tripsArray.getJSONObject(i).getString("description");
			String name = tripsArray.getJSONObject(i).getString("name");

			int insertedTripId = (int) dbh.createTrip(tripId, name, description);
			Log.d("InsertedTripId", insertedTripId + "");

			// Loop through each item in budgeted array inseting it into db with
			// associated newly inserted trip id
			for (int b = 0; b < budgetedArray.length(); b++) {
				budgeted_id = budgetedArray.getJSONObject(b).getInt("id");
				int trip_id = budgetedArray.getJSONObject(b).getInt("trip_id");
				int location_id = budgetedArray.getJSONObject(b).getInt("location_id");
				int category_id = budgetedArray.getJSONObject(b).getInt("category_id");

				// convert serverside php date to local date format
				String planned_arrival_date = budgetedArray.getJSONObject(b).getString("planned_arrival_date");
				int firstHyphen = planned_arrival_date.indexOf("-");
				int secondHyphen = planned_arrival_date.indexOf("-", firstHyphen + 1);
				int space = planned_arrival_date.indexOf(" ");
				int firstColon = planned_arrival_date.indexOf(":", space + 1);
				int secondColon = planned_arrival_date.indexOf(":", firstColon + 1);
				int year = Integer.parseInt(planned_arrival_date.substring(0, firstHyphen));
				int month = Integer.parseInt(planned_arrival_date.substring(firstHyphen + 1, secondHyphen));
				int day = Integer.parseInt(planned_arrival_date.substring(secondHyphen + 1, space));
				int hour = Integer.parseInt(planned_arrival_date.substring(space + 1, firstColon));
				int minute = Integer.parseInt(planned_arrival_date.substring(firstColon + 1, secondColon));	
				GregorianCalendar plannedArrivalDate = new GregorianCalendar(year, month - 1, day);
				plannedArrivalDate.set(Calendar.HOUR_OF_DAY, hour);
				plannedArrivalDate.set(Calendar.MINUTE, minute);
				plannedArrivalDate.set(Calendar.SECOND, 0);

				// convert serverside php date to local date format
				String planned_departure_date = budgetedArray.getJSONObject(b).getString("planned_departure_date");
				firstHyphen = planned_departure_date.indexOf("-");
				secondHyphen = planned_departure_date.indexOf("-", firstHyphen + 1);
				space = planned_departure_date.indexOf(" ");
				firstColon = planned_departure_date.indexOf(":", space + 1);
				secondColon = planned_departure_date.indexOf(":", firstColon + 1);
				year = Integer.parseInt(planned_departure_date.substring(0, firstHyphen));
				month = Integer.parseInt(planned_departure_date.substring(firstHyphen + 1, secondHyphen));
				day = Integer.parseInt(planned_departure_date.substring(secondHyphen + 1, space));
				hour = Integer.parseInt(planned_departure_date.substring(space + 1, firstColon));
				minute = Integer.parseInt(planned_departure_date.substring(firstColon + 1, secondColon));
				GregorianCalendar plannedDepartureDate = new GregorianCalendar(year, month - 1, day);
				plannedDepartureDate.set(Calendar.HOUR_OF_DAY, hour);
				plannedDepartureDate.set(Calendar.MINUTE, minute);
				plannedDepartureDate.set(Calendar.SECOND, 0);

				double amount = budgetedArray.getJSONObject(b).getDouble("amount");
				String budgetedDescription = budgetedArray.getJSONObject(b).getString("description");
				String name_of_supplier = budgetedArray.getJSONObject(b).getString("name_of_supplier");
				String address = budgetedArray.getJSONObject(b).getString("address");
				if (trip_id == tripId) {
					//Insert the budgeted expense with all the retrieved fields.
					insertedBudgetedId = (int) dbh.createBudgetedExpense(insertedTripId, location_id,
							plannedArrivalDate, plannedDepartureDate, amount, budgetedDescription, category_id,
							name_of_supplier, address);
					Log.d("InsertedBudgetedId", insertedBudgetedId + "");
				}

				// Loop through all items in actual array if they match budgeted
				// insert them with its newly inserted id
				for (int a = 0; a < actualArray.length(); a++) {
					if (budgeted_id == actualArray.getJSONObject(a).getInt("budgeted_id")) {
						int actualCategory_id = actualArray.getJSONObject(a).getInt("category_id");

						// convert serverside php date to local date format
						String arrival_date = actualArray.getJSONObject(a).getString("arrival_date");
						firstHyphen = arrival_date.indexOf("-");
						secondHyphen = arrival_date.indexOf("-", firstHyphen + 1);
						space = arrival_date.indexOf(" ");
						firstColon = arrival_date.indexOf(":", space + 1);
						secondColon = arrival_date.indexOf(":", firstColon + 1);
						year = Integer.parseInt(arrival_date.substring(0, firstHyphen));
						month = Integer.parseInt(arrival_date.substring(firstHyphen + 1, secondHyphen));
						day = Integer.parseInt(arrival_date.substring(secondHyphen + 1, space));
						hour = Integer.parseInt(arrival_date.substring(space + 1, firstColon));
						minute = Integer.parseInt(arrival_date.substring(firstColon + 1, secondColon));
						GregorianCalendar arrivalDate = new GregorianCalendar(year, month - 1, day);
						arrivalDate.set(Calendar.HOUR_OF_DAY, hour);
						arrivalDate.set(Calendar.MINUTE, minute);
						arrivalDate.set(Calendar.SECOND, 0);

						// convert serverside php date to local date format
						String departure_date = actualArray.getJSONObject(a).getString("arrival_date");
						firstHyphen = departure_date.indexOf("-");
						secondHyphen = departure_date.indexOf("-", firstHyphen + 1);
						space = departure_date.indexOf(" ");
						firstColon = departure_date.indexOf(":", space + 1);
						secondColon = departure_date.indexOf(":", firstColon + 1);
						year = Integer.parseInt(departure_date.substring(0, firstHyphen));
						month = Integer.parseInt(departure_date.substring(firstHyphen + 1, secondHyphen));
						day = Integer.parseInt(departure_date.substring(secondHyphen + 1, space));
						hour = Integer.parseInt(departure_date.substring(space + 1, firstColon));
						minute = Integer.parseInt(departure_date.substring(firstColon + 1, secondColon));
						GregorianCalendar departureDate = new GregorianCalendar(year, month - 1, day);
						departureDate.set(Calendar.HOUR_OF_DAY, hour);
						departureDate.set(Calendar.MINUTE, minute);
						departureDate.set(Calendar.SECOND, 0);

						double actualAmount = actualArray.getJSONObject(a).getDouble("amount");
						String actualDescription = actualArray.getJSONObject(a).getString("description");
						String actualName_of_supplier = actualArray.getJSONObject(a).getString("name_of_supplier");
						String actualAddress = actualArray.getJSONObject(a).getString("address");
						int stars = actualArray.getJSONObject(a).getInt("stars");
						
						//Insert the actual expense with all the retrieved fields
						dbh.createActualExpense(insertedBudgetedId, arrivalDate, departureDate, actualAmount,
								actualDescription, actualCategory_id, actualName_of_supplier, actualAddress, stars);
					}
				}
			}

		}
	}
}
