package com.android.bonvoyagetravelapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.manage_trips, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
