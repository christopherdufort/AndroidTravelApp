package com.android.bonvoyagetravelapp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ManageTripsActivity extends Activity {

	private String urlString, jsonStr;
	private static DBHelper dbh;
	private ListView lv;
	private TextView tv;
	private SimpleCursorAdapter sca;
	private Cursor cursor;

	private SharedPreferences prefs;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_trips);
		dbh = DBHelper.getDBHelper(this);
		context = getApplicationContext();

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		// Custom ownership feel show current users name.
		tv = (TextView) findViewById(R.id.all_trips_name);
		tv.setText(prefs.getString("name", ""));

		// Setup multiple unique click events available trips displayed.
		setUpListeners();
	}

	/**
	 * Overwritten onResume() life cycle method called to renew resources.
	 */
	@Override
	public void onResume() {
		super.onResume();
		// updateView();
	}

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
		// dbh.close();
	}

	/**
	 * This method is called to set up the simple cursor adapter and link it to
	 * the DB cursor. This method is also responsible for setting up event
	 * listeners on those list items. Short click will display details of a
	 * trip.
	 */
	private void setUpListeners() {
		lv = (ListView) findViewById(R.id.listViewAllTrips);

		String[] from = { DBHelper.COLUMN_NAME, DBHelper.COLUMN_DESCRIPTION };
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

	private void launchSyncAPITrips() {
		urlString = "https://travel-bonvoyage.rhcloud.com/apiTrips";
		
		String email = prefs.getString("email", "");
		String password = prefs.getString("password", "");
		
		JSONObject jsonData  = new JSONObject();
		
		try{
			jsonData.put("email", email);
			jsonData.put("password", password);
		}catch (JSONException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		
		jsonStr = jsonData.toString();
		
		//first check to see if we can get on the network
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			// invoke the AsyncTask to do the dirty work.
			new DownloadTripsData().execute(urlString, jsonStr);
		}
		else{
			Toast toast = Toast.makeText(this, "No network connection available.", Toast.LENGTH_SHORT);
			toast.show();
		}
	}
	
	/**
	 * Uses AsyncTask to create a task away from the main UI thread. This task
	 * takes a URL string and uses it to create an HttpsUrlConnection. Once the
	 * connection has been established, the AsyncTask downloads the contents of
	 * the webpage via an an InputStream. The InputStream is converted into a
	 * string, which is displayed in the UI by the AsyncTask's onPostExecute
	 * method.
	 */
	private class DownloadTripsData extends AsyncTask<String, Void, String> {

		// onPreExecute log some info make sure url and data are good
		// runs in calling thread (in UI thread)
		protected void onPreExecute() {
			Log.d("HttpsURLPOST", "url " + urlString);
			Log.d("HttpsURLPOST", "json " + jsonStr);
		}
		
		/**
		 * runs in background (not in UI thread)
		 */
		@Override
		protected String doInBackground(String... params) {
			try{
				return downloadData(params);
			} catch (IOException e) {
				return "Unable to retrieve web page. URL may be invalid. " + e.getMessage();
			}catch (JSONException e){
				return "Returned JSON object/array or json parse is malformed. " + e.getMessage();
			}
		}
		
		/**
		 * onPostExecute displays the results of the AsyncTask.
		 * runs in calling thread (in UI thread)
		 */
		@Override
		protected void onPostExecute(String result) {
			
			Log.d("Results", result);
			Toast toast = Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG);
			toast.show();
		
			
		}
	} // End AsyncTask DownloadTripsData()
	
	/**
	 * Given a URL, establishes an HttpUrlConnection and retrieves the web page
	 * content as a InputStream, which it returns as a string.
	 *
	 * @param params
	 * @return
	 * @throws IOException
	 * @throws JSONException 
	 */
	private String downloadData(String... params)throws IOException, JSONException {
		InputStream is = null;
		OutputStream out;
		String contentAsString ="";
		int response;
		URL url;
		//Website api support Https - ssl
		HttpsURLConnection conn = null;
		
		byte[] bytes = params[1].getBytes("UTF-8");
		Integer bytesLeng = bytes.length;
		
		try {			
			url =  new URL(params[0]);
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

			// conn.setFixedLengthStreamingMode(params[1].getBytes().length);
			// send body unknown length
			// conn.setChunkedStreamingMode(0);
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(15000 /* milliseconds */);

			conn.setRequestProperty("Content-Type", 
					"application/json; charset=UTF-8");
			// set length of POST data to send
			conn.addRequestProperty("Content-Length", bytesLeng.toString());

			//send the POST out
			out = new BufferedOutputStream(conn.getOutputStream());

			out.write(bytes);
			out.flush();
			out.close();

			// logCertsInfo(conn);

			// now get response
			response = conn.getResponseCode();

			/*	
			 *  check the status code HTTP_OK = 200 anything else we didn't get what
			 *  we want in the data.
			 */
			if (response != HttpURLConnection.HTTP_OK) {
				Log.d("HttpsURLPOST", "Server returned: " + response + " aborting read.");
				return "Server returned: " + response + " aborting read.";
			}
			is = conn.getInputStream();
			contentAsString = readIt(is);
			return contentAsString;

		} finally {
			// Make sure that the Reader is closed after the app is finished using it.
			if (is != null) {
				try {
					is.close();  
				} catch (IOException ignore) { 
					/* ignore */	
				}
			}	
			//* Make sure the connection is closed after the app is finished using it.
			if (conn != null){
				try {
					conn.disconnect();
				} catch (IllegalStateException ignore ) { 
					/* ignore  */ 
				}
			}
		}
	}
	

	/**
	 * 
	 * Reads stream from HTTP connection and converts it to a String. See
	 * stackoverflow or a good explanation of why I did it this way.
	 * http://stackoverflow
	 * .com/questions/3459127/should-i-buffer-the-inputstream
	 * -or-the-inputstreamreader
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 * @throws JSONException 
	 */
	private String readIt(InputStream stream) throws IOException, UnsupportedEncodingException, JSONException 
	{
		String email = "error";
		StringBuilder responseStrBuilder = new StringBuilder();
		String buffer = "";
		BufferedReader reader = null;
		
		reader = new BufferedReader(new InputStreamReader(stream,"UTF-8"));  

		String line = null;  
		while ((line = reader.readLine()) != null) {  
			// could use string builder   sb.append(line + "\n");
			Log.d("RequestResponse", line);
			// the \n is for display, if I'm parsing the JSON I don't want it
			buffer += line + "\n";
			responseStrBuilder.append(line);
			JSONObject jsonObject = new JSONObject(responseStrBuilder.toString());

			//email = jsonObject.get("email").toString();
			JSONArray trips = jsonObject.getJSONArray("trips");
			
			syncLocalDb(trips);
			//JSONObject trips = jsonObject.getJSONObject("trips");
			email=trips.getJSONObject(0).getString("description");
		}  
		return email;
		//return  buffer;
		
	} // readIt()

	private void syncLocalDb(JSONArray trips) throws JSONException {
		
		String description = trips.getJSONObject(0).getString("description");
		String name = trips.getJSONObject(0).getString("name");
		
		long id = dbh.createTrip(-1, name, description);
		
		Log.d("InsertedTripId", id+"");
	
		
	}

}
