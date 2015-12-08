package com.android.bonvoyagetravelapp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

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

	private static DBHelper dbh;
	private ListView lv;
	private TextView tv;
	private SimpleCursorAdapter sca;
	private Cursor cursor;

	private SharedPreferences prefs;

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
		String url = "http://travel-bonvoyage.rhcloud.com/apiTrips";
		String jsonData = "{\"email\":\"" + prefs.getString("email", "") + "\",\"password\":\""
				+ prefs.getString("password", "") + "\"}";

		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected()) {
			new DownloadTripsData().execute(url, jsonData);
		}
	}

	private class DownloadTripsData extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			Log.d("debug", "IM IN A BACKGROUND");
			InputStream input = null;
			OutputStream output;
			HttpURLConnection conn = null;
			String result = "";

			try {
				byte[] jsonBytes = params[1].getBytes("UTF-8");
				Integer jsonBytesLength = jsonBytes.length;

				URL url = new URL(params[0]);
				// create and open the connection
				conn = (HttpURLConnection) url.openConnection();

				if (conn == null)
					Log.d("debug", "connection is null");
				conn.setRequestMethod("POST");
				//conn.setRequestMethod("GET");

				// specifies whether this connection allows receiving data
				conn.setDoInput(true);
				conn.setDoOutput(true);

				conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
				
				
				//output = new BufferedOutputStream(conn.getOutputStream());
				
				//Create JSONObject here
				JSONObject jsonParam = new JSONObject();
				jsonParam.put("email", prefs.getString("email", ""));
				jsonParam.put("password", prefs.getString("password", ""));

				String str = jsonParam.toString();
				byte[] data=str.getBytes("UTF-8");
				conn.addRequestProperty("Content-Length", jsonParam.toString());
				Log.d("debug", "I DIDID WRITE YET");
				DataOutputStream printout = new DataOutputStream(conn.getOutputStream ());
				printout.write(data);
				printout.flush();
				printout.close();
				//output.write();
				//output.flush();
				//output.close();
				//conn.connect();
				Log.d("debug", ""+ conn.getResponseCode());
				if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
				{
					Log.d("debug", "IM OUT OF HERE");
					Log.d("debug", ""+ conn.getResponseCode());
					return "";
				}

				// get the stream for the data from the website
				input = conn.getInputStream();
				
				if (input == null)
					Log.d("debug", "input is null");
				else
					Log.d("debug", "input is NOT null");

				// read the stream
				BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
				
				
				String line = "";  
				while ((line = reader.readLine()) != null) {  
					result += line;
				}
				Log.d("debug", result);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					if (input != null)
						input.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (conn!= null)
					conn.disconnect();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result.equals("")) {
				// cant connect make a toast
			} else {
				parseResults(result);
			}
		}

	} // end of DownloadTripsData

	private void parseResults(String result) {
		try {
			JSONObject jsonObj = new JSONObject(result);

			Log.d("json", jsonObj.toString());

			/*
			 * JSONObject sysObj = jsonObj.getJSONObject("sys"); country =
			 * sysObj.getString("country");
			 * 
			 * nameOfArea = jsonObj.getString("name");
			 * 
			 * JSONArray array = jsonObj.getJSONArray("weather");
			 * descriptionForecast =
			 * array.getJSONObject(0).getString("description"); mainForecast =
			 * array.getJSONObject(0).getString("main");
			 * 
			 * JSONObject weatherObj = jsonObj.getJSONObject("wind"); windSpeed
			 * = "" + weatherObj.getDouble("speed");
			 * 
			 * JSONObject mainObj = jsonObj.getJSONObject("main"); humidity = ""
			 * + mainObj.getInt("humidity"); pressure = "" +
			 * mainObj.getInt("pressure"); temp = "" +
			 * mainObj.getDouble("temp"); minTemp = "" +
			 * mainObj.getDouble("temp_min"); maxTemp = "" +
			 * mainObj.getDouble("temp_max");
			 */

		} catch (JSONException e) {
			e.printStackTrace();
		}
	} // end of parseResults
}
