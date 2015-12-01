package com.android.bonvoyagetravelapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ItineraryActivity extends Activity {

	private int tripId;
	private ListView lv;
	private DBHelper dbh;
	private SimpleCursorAdapter sca;
	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_itinerary);
		dbh=DBHelper.getDBHelper(this);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		Intent intent = getIntent();
		if(intent.hasExtra("TRIPID")){
			tripId = intent.getExtras().getInt("TRIPID");
		}
		else{
			tripId = prefs.getInt("CURRENTTRIP", -1);
			TextView title = (TextView) findViewById(R.id.trip_itinerary_title);
			//TODO replace with @String reference
			title.setText("Current Trip Itinerary");
		}
		

		
		//store this id in shared prefs for current itinerary.
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("CURRENTTRIP", tripId);
		editor.commit();
		
		// Setup multiple unique click events available for tasks shown.
		setUpListeners();

	}

	private void setUpListeners() {
		lv = (ListView) findViewById(R.id.listViewAllItinerary);

		//FIXME DBHelper.COLUMN_NAME_OF_SUPPLIER not found in table?
		String[] from = { DBHelper.COLUMN_PLANNED_DEPARTURE_DATE, DBHelper.COLUMN_PLANNED_ARRIVAL_DATE, DBHelper.COLUMN_AMOUNT, DBHelper.COLUMN_DESCRIPTION, DBHelper.COLUMN_ADDRESS}; //, DBHelper.COLUMN_NAME_OF_SUPPLIER};
		int[] to = { R.id.itinerary_depart, R.id.itinerary_arrival, R.id.itinerary_amount, R.id.itinerary_description, R.id.itinerary_address};//, R.id.itinerary_supplier };

		Cursor cursor = dbh.getBudgetedExpenses(tripId);
		sca = new SimpleCursorAdapter(this, R.layout.activity_itinerary_list, cursor, from, to,0);
		
		lv.setAdapter(sca);
		
		// Event listener for short clicks will display details about a budgeted expense
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent,
					View view, int position, long id) {
				Cursor cursortemp = (Cursor) parent
						.getItemAtPosition(position);

				// first column is id getInt gets column data as int
				int itineraryId = cursortemp.getInt(0);
				
				Intent intent = new Intent(getApplicationContext(),ItineraryDetails.class);
				intent.putExtra("ITINERARYID", itineraryId);
				startActivity(intent);


			}
		});
		// Event listener for long clicks will delete based on confirmation dialaog
		lv.setOnItemLongClickListener(
				new OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(
							AdapterView<?> parent, View view,
							int position, long id) {

						Cursor cursortemp = (Cursor) parent
								.getItemAtPosition(position);

						// first column is id getInt gets column data as int
						int itineraryId = cursortemp.getInt(0);
						int description = cursortemp.getInt(6);
						
						// See custom method below
						//TODO replace by @string resource
						showAlert("Deleting Notice",
								"Are you sure you want to delete " + description, itineraryId);
						
						// Return true to consume the click event.
						return true;
					}
				});
	}

	protected void showAlert(String title, String message, final int itineraryId) {
		// Build up a dialog box.
		Builder builder = new Builder(this);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setCancelable(true);
		// Two possible buttons.
				builder.setNegativeButton("No", null); // Do nothing
				builder.setPositiveButton("Yes", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						dbh.deleteBudgetedExpense(itineraryId);

				/*		prefs = PreferenceManager
								.getDefaultSharedPreferences(
										getApplicationContext());
						SharedPreferences.Editor editor = prefs.edit();

						editor.putString("LASTDELETED", content); // Store in shared
																	// prefs to be
																	// accessed later.
						editor.putInt("LASTID", -1); // Last task was deleted.
						editor.commit();*/

						dialog.dismiss();
						refreshView();
					}
				});
				// Display
				AlertDialog dialog = builder.create();
				dialog.show();
			}

	protected void refreshView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.itinerary, menu);
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
