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
		dbh=DBHelper.getDBHelper(this);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		//Custom ownership feel show current users name.
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
		//updateView();
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
		//dbh.close();
	}
	
	/**
	 * This method is called to set up the simple cursor adapter and link it to
	 * the DB cursor. This method is also responsible for setting up event
	 * listeners on those list items. Short click will display details of a trip.
	 */
	private void setUpListeners() {
		lv =(ListView) findViewById(R.id.listViewAllTrips);
		
		String[] from = { DBHelper.COLUMN_NAME, DBHelper.COLUMN_DESCRIPTION};
		int[] to = { R.id.text_view_trip_name, R.id.text_view_trip_description};
		
		Cursor cursor = dbh.getAllTrips();

		sca = new SimpleCursorAdapter(this, R.layout.activity_manage_trips_list, cursor,from,to,0);
	
		lv.setAdapter(sca);
		
		// Event listener for short clicks will open details of trip in new activity.

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent,View view, int position, long id) {
				Cursor cursortemp = (Cursor) parent.getItemAtPosition(position);

				int tripId= cursortemp.getInt(0);
				String tripName = cursortemp.getString(5);
				
				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt("CURRENTTRIP", tripId);
				editor.putString("CURRENTTRIPNAME", tripName);
				editor.commit();
				
				Intent intent = new Intent(getApplicationContext(),ItineraryActivity.class);
				intent.putExtra("MANAGE", true);
				startActivity(intent);
			}
		});
	}
	
	/**
	 * syncTask is an event handler for the sync trip button in the manage trip activity.
	 * This method is responsible for updating the local SQLite db with changes made server side.
	 * 
	 * @param view
	 * 			the view that triggered this event handler (button click)
	 */
	public void syncTrips(View view) {
		Toast toast = Toast.makeText(this, "Syncing with server", Toast.LENGTH_SHORT);
		toast.show();
	}
	private void launchSyncAPI() {
		String url = "";
		String appId = "e857fa3cafcae16ad142b30675ad2cff";

		url = "travel-bonvoyage.rhcloud.com/apiTrips";

		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected()) {
			new DownloadTripsData().execute(url);
		}
	}

	private class DownloadTripsData extends AsyncTask<String, Void, String> {

		@Override
		protected void onPostExecute(String result) {

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

	} // end of DownloadTripsData

	private void parseResults(String result) {

		try {
			JSONObject jsonObj = new JSONObject(result);

		} catch (JSONException e) {
			e.printStackTrace();
		}

	} //end of parseResults
}
