package com.android.bonvoyagetravelapp;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Itinerary activity is the primary activity used for displaying lists budgeted
 * expenses. This class is used by the Today feature, the manage trips feature,
 * and the current trip itinerary feature. Itinerary consists of a view with
 * ability to add new itinerary, and list view of budgeted expenses. Each
 * expense displays a few pieces of information about itself. This activity has
 * a custom look depending on which activity called it.
 * 
 * @author Irina Patrocinio Frazao
 * @author Christopher Dufort
 * @author Annie So
 * @since JDK 1.6
 * @version 1.0.0-Release
 */
public class ItineraryActivity extends Activity {

	private int tripId;
	private ListView lv;
	private TextView tv;
	private TextView title;
	private DBHelper dbh;
	private SimpleCursorAdapter sca;
	private SharedPreferences prefs;
	private String tripName;
	private Cursor cursor;
	private Bundle extras;
	private GregorianCalendar correspondingDate = new GregorianCalendar();

	/**
	 * Overriden onCreate method that sets up the UI This method is also
	 * responsible for setting a custom title depending on which action called
	 * this class. This call will populate different cursors with groups of trips
	 * depending on which type of trips to display (current, today, manage)
	 * Different types of trips are determined by retrieving extras from the
	 * bundle.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_itinerary);
		dbh = DBHelper.getDBHelper(this);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		title = (TextView) findViewById(R.id.trip_itinerary_title);

		// If there is a current trip in shared prefs retrieve its name and id.
		tripId = prefs.getInt("CURRENTTRIP", -1);
		tripName = prefs.getString("CURRENTTRIPNAME", "");

		// retrieve the type of itinerary to display based on contents of the
		// bundle.
		extras = getIntent().getExtras();
		if (extras != null) {
			// This class called from manage trips display all budgeted for a
			// specific trip
			if (extras.containsKey("MANAGE")) {
				title.setText(R.string.manage_itinerary_title);
				cursor = dbh.getBudgetedExpenses(tripId);
			}
			// This class called from today activity, display all budgeted for a
			// specific date (todays date)
			else if (extras.containsKey("TODAY")) 
			{
				GregorianCalendar currentDate= new GregorianCalendar();
				

				// Set the DatePickerDialog to have the same date as the view that was
				// clicked.
				DatePickerDialog datePicker = new DatePickerDialog(this, handleSetDate, currentDate.get(Calendar.YEAR),
						currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH));

				// Show the DatePickerDialog.
				datePicker.show();
				
				
				Log.d("today", "in today");
				title.setText(R.string.today_itinerary_title);
				tripName = "today";
				cursor = dbh.getBudgetedExpensesByDay(correspondingDate);
				Log.d("today", "date from picker: " + correspondingDate);
				//cursor = dbh.getBudgetedExpensesByDay(new GregorianCalendar());
				Log.d("today", "length: " + cursor.getCount());
				cursor.moveToFirst();
			}
			// This class called from current trips, retrieve the trips based on
			// id from prefs or defaults to first trip.
			else if (extras.containsKey("CURRENT")) {
				if (tripId == -1) { // default to first trip in db
					cursor = dbh.getAllTrips();
					cursor.moveToFirst();
					tripId = cursor.getInt(1);
					tripName = cursor.getString(5);
					cursor = dbh.getBudgetedExpenses(tripId);
				} else { // use the id of the last edited, or modified, or
							// viewed trip
					cursor = dbh.getBudgetedExpenses(tripId);
					SharedPreferences.Editor editor = prefs.edit();
					editor.putInt("CURRENTTRIP", tripId);
					editor.putString("CURRENTTRIPNAME", tripName);
					editor.commit();
				}
			}
		}

		// Set trip title.
		tv = (TextView) findViewById(R.id.trip_name);
		tv.setText(tripName);

		// Setup multiple unique click events available for tasks shown.
		setUpListeners();
	}
	
	private OnDateSetListener handleSetDate = new OnDateSetListener() {
		
		private Calendar currentDate = new GregorianCalendar();

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			

			// Set the current date from the spinner.
			currentDate.set(Calendar.YEAR, year);
			currentDate.set(Calendar.MONTH, monthOfYear);
			currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			
			correspondingDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR));
			correspondingDate.set(Calendar.MONTH, currentDate.get(Calendar.MONTH));
			correspondingDate.set(Calendar.DAY_OF_MONTH, currentDate.get(Calendar.DAY_OF_MONTH));
			
			refreshView();

		}
	};

	/**
	 * Overriden onResume that is used to refresh the view as part of the life
	 * cycle.
	 */
	@Override
	public void onResume() {
		super.onResume();
		refreshView();
	}

	/**
	 * createItinerary is an event handler that will launch the details activity
	 * for a specific trip as its called. By pressing on a specific trip in the
	 * list the id of the trip and itinerary will be stored in the bundle and
	 * accessed by the details activity.
	 * 
	 * @param view
	 *            The view widget that called this event handler
	 */
	public void createItinerary(View view) {

		Intent intent = new Intent(getApplicationContext(), ItineraryDetails.class);
		intent.putExtra("ITINERARYID", -1);
		intent.putExtra("tripId", tripId);
		startActivity(intent);
	}

	/**
	 * Custom method used to set up the simple cursor adapter and populate a
	 * list view with results from a db query. Each element in the list is a
	 * specific budgeted itinerary expense and its fields are associated with
	 * the element in the list. A click handler is applied to each item within
	 * the list in order to allow the user to click on each individually. Short
	 * click will result in the ItinerayDetails activity being launched to
	 * display more information about the itinerary. Long click will result in a
	 * a custom dialogbox popping up to confirm the delete of the itinerary.
	 * 
	 */
	private void setUpListeners() {
		lv = (ListView) findViewById(R.id.listViewAllItinerary);

		String[] from = { DBHelper.COLUMN_NAME_OF_SUPPLIER, DBHelper.COLUMN_DESCRIPTION, DBHelper.COLUMN_AMOUNT };

		int[] to = { R.id.itinerary_supplier, R.id.itinerary_description, R.id.itinerary_amount };

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

	/**
	 * Custom delete alert used as a modal pop up to confirm that the user
	 * wishes to delete an itinerary item. Custom dialog is built and displayed
	 * showing two buttons. Clicking on yes will result in the dbh deleting the
	 * itinerary and the view refreshing.
	 * 
	 * @param title
	 *            Title of the trip used for display.
	 * @param itineraryId
	 *            If of itinerary used to delete by id.
	 */
	protected void showAlert(String title, final int itineraryId) {
		// Build up a dialog box.
		Builder builder = new Builder(this);
		builder.setTitle(title);
		builder.setMessage("Are you sure you want to delete this expense?");
		builder.setCancelable(true);
		// Two possible buttons.
		builder.setNegativeButton("No", null); // Do nothing
		builder.setPositiveButton("Yes", new OnClickListener() {
			// User clicked yes
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Delete the itinerary
				dbh.deleteBudgetedExpense(itineraryId);

				// Refresh the view
				refreshView();

				// Close the dialog
				dialog.dismiss();
			}
		});
		// Display the dialog
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	/**
	 * refreshView method called to renew resources, and refresh the view.
	 */
	protected void refreshView() {
		Cursor newCursor;

		if (extras.containsKey("TODAY"))
			newCursor = dbh.getBudgetedExpensesByDay(correspondingDate);
		else
			newCursor = dbh.getBudgetedExpenses(tripId);

		sca.changeCursor(newCursor);
		sca.notifyDataSetChanged();

	}
}