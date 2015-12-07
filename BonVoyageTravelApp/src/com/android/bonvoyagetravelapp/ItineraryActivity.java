package com.android.bonvoyagetravelapp;

import java.util.Date;

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
import android.util.Log;
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
	private TextView tv;
	private TextView title;
	private DBHelper dbh;
	private SimpleCursorAdapter sca;
	private SharedPreferences prefs;
	private String tripName;
	private boolean today;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_itinerary);
		dbh = DBHelper.getDBHelper(this);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		title = (TextView) findViewById(R.id.trip_itinerary_title);

		tripId = prefs.getInt("CURRENTTRIP", -1);
		tripName = prefs.getString("CURRENTTRIPNAME", "");

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (extras.containsKey("MANAGE")) {
				title.setText(R.string.manage_itinerary_title);
			}
			if (extras.containsKey("TODAY")) {
				title.setText(R.string.today_itinerary_title);
				today = true;
			}
		}

		if (tripId == -1 && today == false) {
			//current
			// if no trip was viewed, the current one is the first one in db
			Cursor cursor = dbh.getAllTrips();
			cursor.moveToFirst();
			tripId = cursor.getInt(1);
			tripName = cursor.getString(5);

		} 
		else if (today == false) {
			//CURRENT TRIP
			// store this id in shared prefs for current itinerary.
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt("CURRENTTRIP", tripId);
			editor.putString("CURRENTTRIPNAME", tripName);
			editor.commit();
		}
		else 
		{
			//Today
			Log.d("debug", "in saved instance sstate not equal to null");
			tripName = "today";
			Cursor cursor = dbh.getBudgetedExpenses(new Date());
			cursor.moveToFirst();
		}

		tv = (TextView) findViewById(R.id.trip_name);
		tv.setText(tripName);



		// Setup multiple unique click events available for tasks shown.
		setUpListeners();
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshView();
	}

	public void createItinerary(View view) {

		Intent intent = new Intent(getApplicationContext(), ItineraryDetails.class);
		intent.putExtra("ITINERARYID", -1);
		startActivity(intent);
	}

	private void setUpListeners() {
		lv = (ListView) findViewById(R.id.listViewAllItinerary);

		String[] from = { DBHelper.COLUMN_NAME_OF_SUPPLIER, DBHelper.COLUMN_DESCRIPTION, DBHelper.COLUMN_AMOUNT };

		int[] to = { R.id.itinerary_supplier, R.id.itinerary_description, R.id.itinerary_amount };

		// TODO make all these cursors fields and shared var
		Cursor cursor;
		if (today != true) {
			cursor = dbh.getBudgetedExpenses(tripId);
		} else {
			cursor = dbh.getBudgetedExpenses(new Date());
		}

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

				// See custom method below
				// TODO replace by @string resource
				showAlert("Deleting Notice", itineraryId);

				// Return true to consume the click event.
				return true;
			}
		});
	}

	protected void showAlert(String title, final int itineraryId) {
		// Build up a dialog box.
		Builder builder = new Builder(this);
		builder.setTitle(title);
		builder.setMessage("Are you sure you want to delete this expense?");
		builder.setCancelable(true);
		// Two possible buttons.
		builder.setNegativeButton("No", null); // Do nothing
		builder.setPositiveButton("Yes", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dbh.deleteBudgetedExpense(itineraryId);

				refreshView();

				dialog.dismiss();
			}
		});
		// Display
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	protected void refreshView() {
		Cursor newCursor = dbh.getBudgetedExpenses(tripId);
		sca.changeCursor(newCursor);
		sca.notifyDataSetChanged();

	}
}
