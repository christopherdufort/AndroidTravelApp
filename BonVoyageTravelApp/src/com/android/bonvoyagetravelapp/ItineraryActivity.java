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
		dbh = DBHelper.getDBHelper(this);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		tripId = prefs.getInt("CURRENTTRIP", -1);

		if (tripId == -1) {
			// if no trip was viewed, the current one is the first one in db
			Cursor cursor = dbh.getAllTrips();
			cursor.moveToFirst();
			tripId = cursor.getInt(1);
		}

		// store this id in shared prefs for current itinerary.
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("CURRENTTRIP", tripId);
		editor.commit();;

		// Setup multiple unique click events available for tasks shown.
		setUpListeners();

	}
	
	public void createItinerary(View view){
		
	}

	private void setUpListeners() {
		lv = (ListView) findViewById(R.id.listViewAllItinerary);

		String[] from = {DBHelper.COLUMN_AMOUNT, DBHelper.COLUMN_DESCRIPTION}; 
																								
		int[] to = { R.id.itinerary_amount, R.id.itinerary_description};

		Cursor cursor = dbh.getBudgetedExpenses(tripId);
		sca = new SimpleCursorAdapter(this, R.layout.activity_itinerary_list, cursor, from, to, 0);

		lv.setAdapter(sca);

		// Event listener for short clicks will display details about a budgeted
		// expense
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Cursor cursortemp = (Cursor) parent.getItemAtPosition(position);

				// first column is id getInt gets column data as int
				int itineraryId = cursortemp.getInt(0);

				Intent intent = new Intent(getApplicationContext(), ItineraryDetails.class);
				intent.putExtra("ITINERARYID", itineraryId);
				startActivity(intent);
			}
		});
		
		// Event listener for long clicks will delete based on confirmation
		// dialaog
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

				Cursor cursortemp = (Cursor) parent.getItemAtPosition(position);

				// first column is id getInt gets column data as int
				int itineraryId = cursortemp.getInt(0);
				String description = cursortemp.getString(6);

				// See custom method below
				// TODO replace by @string resource
				showAlert("Deleting Notice", "Are you sure you want to delete " + description, itineraryId);

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
			public void onClick(DialogInterface dialog, int which) {
				dbh.deleteBudgetedExpense(itineraryId);

				//DELETE HERE

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
}
