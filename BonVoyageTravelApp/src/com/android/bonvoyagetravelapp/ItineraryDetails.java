package com.android.bonvoyagetravelapp;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

/**
 * This class handles creating, showing, editing, and deleting budgeted expenses
 * and actual expenses.
 * 
 * @author Irina Patrocinio Frazao
 * @author Christopher Dufort
 * @author Annie So
 */
public class ItineraryDetails extends Activity {
	private DBHelper dbh;
	private Cursor cursor;
	private int tripId, budgetedId, actualId, currentSettingId;
	private GregorianCalendar currentDateTime;

	private TextView title;
	private EditText description, supplier, address, amount;
	private TextView arrivalDate, arrivalTime, departureDate, departureTime;
	private Spinner category, location;
	private ArrayList<Integer> categoryIds, locationIds;
	private ArrayList<String> categoryNames, locationNames;
	private Button editBudgetedButton, saveBudgetedButton, createBudgetedButton, deleteBudgetedButton;
	private GregorianCalendar arrivalDateTime, departureDateTime;

	private LinearLayout actualExpense;
	private EditText actualDescription, actualSupplier, actualAddress, actualAmount;
	private TextView actualArrivalDate, actualArrivalTime, actualDepartureDate, actualDepartureTime;
	private RatingBar stars;
	private Button editActualButton, saveActualButton, createActualButton, deleteActualButton, showNewActualButton;
	private GregorianCalendar actualArrivalDateTime, actualDepartureDateTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_itinerary_details);

		// Get a handle to the DBHelper.
		dbh = DBHelper.getDBHelper(this);

		// Get a handle to the common elements
		title = (TextView) findViewById(R.id.itinerary_title);

		// Get a handle to all the fields associated with a budgeted expense.
		category = (Spinner) findViewById(R.id.itinerary_category);
		location = (Spinner) findViewById(R.id.itinerary_location);
		description = (EditText) findViewById(R.id.itinerary_description);
		supplier = (EditText) findViewById(R.id.itinerary_supplier);
		address = (EditText) findViewById(R.id.itinerary_address);
		amount = (EditText) findViewById(R.id.itinerary_amount);
		arrivalDate = (TextView) findViewById(R.id.itinerary_arrival_date);
		arrivalTime = (TextView) findViewById(R.id.itinerary_arrival_time);
		departureDate = (TextView) findViewById(R.id.itinerary_departure_date);
		departureTime = (TextView) findViewById(R.id.itinerary_departure_time);
		editBudgetedButton = (Button) findViewById(R.id.itinerary_edit_budgeted_btn);
		saveBudgetedButton = (Button) findViewById(R.id.itinerary_save_budgeted_btn);
		createBudgetedButton = (Button) findViewById(R.id.itinerary_create_budgeted_btn);
		deleteBudgetedButton = (Button) findViewById(R.id.itinerary_delete_budgeted_btn);

		// Get a handle to the button to create a new actual expense.
		showNewActualButton = (Button) findViewById(R.id.itinerary_show_actual_btn);

		// Get a handle to the linear layout containing all fields associated
		// with an actual expense.
		actualExpense = (LinearLayout) findViewById(R.id.container_actual);

		// Get a handle to all the fields associated with an actual expense.
		actualDescription = (EditText) findViewById(R.id.itinerary_actual_description);
		actualSupplier = (EditText) findViewById(R.id.itinerary_actual_supplier);
		actualAddress = (EditText) findViewById(R.id.itinerary_actual_address);
		actualAmount = (EditText) findViewById(R.id.itinerary_actual_amount);
		actualArrivalDate = (TextView) findViewById(R.id.itinerary_actual_arrival_date);
		actualArrivalTime = (TextView) findViewById(R.id.itinerary_actual_arrival_time);
		actualDepartureDate = (TextView) findViewById(R.id.itinerary_actual_departure_date);
		actualDepartureTime = (TextView) findViewById(R.id.itinerary_actual_departure_time);
		stars = (RatingBar) findViewById(R.id.stars);
		editActualButton = (Button) findViewById(R.id.itinerary_edit_actual_btn);
		saveActualButton = (Button) findViewById(R.id.itinerary_save_actual_btn);
		createActualButton = (Button) findViewById(R.id.itinerary_create_actual_btn);
		deleteActualButton = (Button) findViewById(R.id.itinerary_delete_actual_btn);

		// Get the budgeted expense's id and the trip's id from the intent.
		Intent intent = getIntent();
		budgetedId = intent.getIntExtra("ITINERARYID", -1);
		tripId = intent.getIntExtra("tripId", -1);

		// Fill the category spinner with the correct values.
		cursor = dbh.getAllCategories();
		categoryIds = new ArrayList<Integer>();
		categoryNames = new ArrayList<String>();
		if (cursor.getCount() != 0) {
			while (cursor.moveToNext()) {
				categoryIds.add(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
				categoryNames.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_CATEGORY)));
			}

			ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_dropdown_item, categoryNames);
			category.setAdapter(spinnerArrayAdapter);
		}

		// Fill the location spinner with the correct values.
		cursor = dbh.getAllLocations();
		locationIds = new ArrayList<Integer>();
		locationNames = new ArrayList<String>();
		if (cursor.getCount() != 0) {
			while (cursor.moveToNext()) {
				locationIds.add(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
				locationNames.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_CITY)) + ", "
						+ cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_COUNTRY_CODE)));
			}

			ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_dropdown_item, locationNames);
			location.setAdapter(spinnerArrayAdapter);
		}

		// If the budgeted expense's id is not -1, you are checking the details
		// of an existing budgeted expense. If it is -1, you are creaking a new
		// budgeted expense.
		if (budgetedId != -1) {
			// Hide the button to create a new budgeted expense and set the
			// correct title.
			createBudgetedButton.setVisibility(Button.GONE);
			title.setText(R.string.itinerary_details_title);

			// Get all the the budgeted expense's details from the database.
			cursor = dbh.getBudgetedExpenseDetails(budgetedId);
			cursor.moveToFirst();

			// Set the arrival and departure date and time text fields.
			arrivalDateTime = new GregorianCalendar();
			arrivalDateTime.setTimeInMillis(
					cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_PLANNED_ARRIVAL_DATE)) * 1000);
			arrivalDate.setText(formatDate(arrivalDateTime));
			arrivalTime.setText(formatTime(arrivalDateTime));

			departureDateTime = new GregorianCalendar();
			departureDateTime.setTimeInMillis(
					cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_PLANNED_DEPARTURE_DATE)) * 1000);
			departureDate.setText(formatDate(departureDateTime));
			departureTime.setText(formatTime(departureDateTime));

			// Set the edit text fields.
			amount.setText(Double.valueOf(
					new DecimalFormat("#.00").format(cursor.getDouble(cursor.getColumnIndex(DBHelper.COLUMN_AMOUNT))))
					.toString());
			description.setText(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DESCRIPTION)));
			supplier.setText(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME_OF_SUPPLIER)));
			address.setText(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ADDRESS)));

			// Set the selection of the category spinner to the correct
			// selection.
			category.setSelection(
					categoryIds.indexOf(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_CATEGORY_ID))));

			// Set the location of the location spinner to the correct
			// selection.
			// Must be done last because it requires another call to the
			// database.
			cursor = dbh.getLocationById(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_LOCATION_ID)));
			cursor.moveToFirst();
			location.setSelection(locationNames.indexOf(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_CITY))
					+ ", " + cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_COUNTRY_CODE))));

			// Disable editing the budgeted expense fields.
			editingBudgetedFields(false);

			// Get the actual expense associated with the budgeted expense.
			cursor = dbh.getActualExpense(budgetedId);

			// If there is an actual expense associated with the budgeted
			// expense, show it. If not add a button to create one.
			if (cursor.getCount() != 0) {
				// If there is an actual expense associated with the budgeted
				// expense, hide the buttons responsible for showing the form
				// for creating a new actual expense and show the linear layout
				// containing all the actual expense fields.
				showNewActualButton.setVisibility(Button.GONE);
				createActualButton.setVisibility(Button.GONE);
				actualExpense.setVisibility(LinearLayout.VISIBLE);

				cursor.moveToFirst();

				actualId = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ID));

				// Set the edit text fields
				actualDescription.setText(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DESCRIPTION)));
				actualSupplier.setText(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME_OF_SUPPLIER)));
				actualAddress.setText(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ADDRESS)));
				actualAmount
						.setText(Double
								.valueOf(new DecimalFormat("#.00")
										.format(cursor.getDouble(cursor.getColumnIndex(DBHelper.COLUMN_AMOUNT))))
						.toString());

				// Set the actual arrival and departure dates and times text
				// fields.
				actualArrivalDateTime = new GregorianCalendar();
				actualArrivalDateTime
						.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ARRIVAL_DATE)) * 1000);
				actualArrivalDate.setText(formatDate(actualArrivalDateTime));
				actualArrivalTime.setText(formatTime(actualArrivalDateTime));

				actualDepartureDateTime = new GregorianCalendar();
				actualDepartureDateTime
						.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_DEPARTURE_DATE)) * 1000);
				actualDepartureDate.setText(formatDate(actualDepartureDateTime));
				actualDepartureTime.setText(formatTime(actualDepartureDateTime));

				// Set the rating bar.
				stars.setRating(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_STARS)));

				// Disable editing the actual expense fields.
				editingActualFields(false);
			} else {
				// If there is no actual expense associated with the budgeted
				// expense, show a button for showing the form for creating a
				// new actual expense and hide the linear layout containing all
				// the actual expense fields.
				showNewActualButton.setVisibility(Button.VISIBLE);
				actualExpense.setVisibility(LinearLayout.GONE);
			}
		} else {
			// If you are creating a new budgeted expense, hide everything
			// associated with actual expenses.
			showNewActualButton.setVisibility(Button.GONE);
			actualExpense.setVisibility(LinearLayout.GONE);

			// Set the appropriate title.
			title.setText(R.string.itinerary_today_title);

			// Set the arrival date and time text fields using the current date
			// and time to prevent null pointer errors from the methods that
			// handle launching the DatePickerDialog or TimePickerDialog.
			arrivalDateTime = new GregorianCalendar();
			arrivalDate.setText(formatDate(arrivalDateTime));
			arrivalTime.setText(formatTime(arrivalDateTime));

			departureDateTime = new GregorianCalendar();
			departureDate.setText(formatDate(departureDateTime));
			departureTime.setText(formatTime(departureDateTime));

			// Enable editing the budgeted expense fields. Hide the button
			// to save a budgeted expense and show the button to create a new
			// budgeted expense.
			editingBudgetedFields(true);
			saveBudgetedButton.setVisibility(Button.GONE);
			createBudgetedButton.setVisibility(Button.VISIBLE);
		}
	}

	/**
	 * Enables or disables editing the budgeted expense fields depending on the
	 * boolean sent in.
	 * 
	 * @param editing
	 *            Whether or not the budgeted fields are currently being edited.
	 */
	private void editingBudgetedFields(boolean editing) {
		if (editing) {
			Toast.makeText(this, "Editing in progress", Toast.LENGTH_SHORT).show();
			// If the budgeted expense is being edited, hide the edit and delete
			// button and only show the save button.
			editBudgetedButton.setVisibility(Button.GONE);
			deleteBudgetedButton.setVisibility(Button.GONE);
			saveBudgetedButton.setVisibility(Button.VISIBLE);
		} else {
			// If the budgeted expense is being edited, hide the save button and
			// show the edit and delete button.
			Toast.makeText(this, "Editing disabled", Toast.LENGTH_SHORT).show();
			editBudgetedButton.setVisibility(Button.VISIBLE);
			deleteBudgetedButton.setVisibility(Button.VISIBLE);
			saveBudgetedButton.setVisibility(Button.GONE);
		}

		// Set the fields so they can or cannot be edited depending on the
		// boolean sent in.
		category.setEnabled(editing);
		location.setEnabled(editing);
		description.setFocusableInTouchMode(editing);
		description.setFocusable(editing);
		description.setClickable(editing);
		supplier.setFocusableInTouchMode(editing);
		supplier.setFocusable(editing);
		supplier.setClickable(editing);
		address.setFocusableInTouchMode(editing);
		address.setFocusable(editing);
		address.setClickable(editing);
		amount.setFocusableInTouchMode(editing);
		amount.setFocusable(editing);
		amount.setClickable(editing);
		arrivalDate.setClickable(editing);
		arrivalTime.setClickable(editing);
		departureDate.setClickable(editing);
		departureTime.setClickable(editing);
	}

	// TODO Make sure every field has values.
	/**
	 * Creates a new budgeted expense.
	 * 
	 * @param view
	 *            The view that called this method.
	 */
	public void createBudgeted(View view) {
		// Hide the button to create a new budgeted expense, set the
		// appropriate title, and show the button to show the create form for a
		// new actual expense.
		createBudgetedButton.setVisibility(Button.GONE);
		title.setText(R.string.itinerary_details_title);
		showNewActualButton.setVisibility(Button.VISIBLE);

		// Disable editing the budgeted expense fields.
		editingBudgetedFields(false);

		// Create the new budgeted expense in the database.
		budgetedId = (int) dbh.createBudgetedExpense(tripId, locationIds.get(location.getSelectedItemPosition()),
				arrivalDateTime, departureDateTime, Double.parseDouble(amount.getText().toString()),
				description.getText().toString(), categoryIds.get(category.getSelectedItemPosition()),
				supplier.getText().toString(), address.getText().toString());
		amount.setText(Double.valueOf(new DecimalFormat("#.00").format(Double.parseDouble(amount.getText().toString())))
				.toString());
	}

	/**
	 * Enables editing the budgeted expense.
	 * 
	 * @param view
	 *            The view that called this method.
	 */
	public void editBudgeted(View view) {
		editingBudgetedFields(true);
	}

	// TODO Make sure every field has values.
	/**
	 * Saves the changes made to a budgeted expense.
	 * 
	 * @param view
	 *            The view that called this method.
	 */
	public void saveBudgeted(View view) {
		// Disable editing the budgeted expense fields.
		editingBudgetedFields(false);

		// Save the changes to the database.
		dbh.updateBudgetedExpense(budgetedId, locationIds.get(location.getSelectedItemPosition()), arrivalDateTime,
				departureDateTime, Double.parseDouble(amount.getText().toString()), description.getText().toString(),
				categoryIds.get(category.getSelectedItemPosition()), supplier.getText().toString(),
				address.getText().toString());
		amount.setText(Double.valueOf(new DecimalFormat("#.00").format(Double.parseDouble(amount.getText().toString())))
				.toString());
	}

	/**
	 * Deletes the budgeted expense. Also exits the activity.
	 * 
	 * @param view
	 *            The view that called this method.
	 */
	public void deleteBudgeted(View view) {
		dbh.deleteBudgetedExpense(budgetedId);
		finish();
	}

	/**
	 * Enables or disables editing the actual expense fields depending on the
	 * boolean sent in.
	 * 
	 * @param editing
	 *            Whether or not the budgted fields are currently being edited.
	 */
	private void editingActualFields(boolean editing) {
		if (editing) {
			// If the actual expense is being edited, hide the edit and delete
			// button and only show the save button.
			Toast.makeText(this, "Editing in progress", Toast.LENGTH_SHORT).show();
			editActualButton.setVisibility(Button.GONE);
			deleteActualButton.setVisibility(Button.GONE);
			saveActualButton.setVisibility(Button.VISIBLE);
		} else {
			// If the actual expense is being edited, hide the save button and
			// show the edit and delete button.
			Toast.makeText(this, "Editing disabled", Toast.LENGTH_SHORT).show();
			editActualButton.setVisibility(Button.VISIBLE);
			deleteActualButton.setVisibility(Button.VISIBLE);
			saveActualButton.setVisibility(Button.GONE);
		}

		// Set the fields so they can or cannot be edited depending on the
		// boolean sent in.
		actualDescription.setFocusableInTouchMode(editing);
		actualDescription.setFocusable(editing);
		actualDescription.setClickable(editing);
		actualSupplier.setFocusableInTouchMode(editing);
		actualSupplier.setFocusable(editing);
		actualSupplier.setClickable(editing);
		actualAddress.setFocusableInTouchMode(editing);
		actualAddress.setFocusable(editing);
		actualAddress.setClickable(editing);
		actualAmount.setFocusableInTouchMode(editing);
		actualAmount.setFocusable(editing);
		actualAmount.setClickable(editing);
		actualArrivalDate.setClickable(editing);
		actualArrivalTime.setClickable(editing);
		actualDepartureDate.setClickable(editing);
		actualDepartureTime.setClickable(editing);
		stars.setIsIndicator(!editing);
	}

	/**
	 * Shows all the fields that need to be filled to create an actual expense.
	 * 
	 * @param view
	 *            The view that called this method.
	 */
	public void showActual(View view) {
		// Hide the show new actual button and show the linear layout
		// containing all the actual expense fields.
		showNewActualButton.setVisibility(Button.GONE);
		actualExpense.setVisibility(LinearLayout.VISIBLE);

		// Reset all the fields so that if an actual expense was deleted and
		// then a new one was created the old values wouldn't still be there.
		actualDescription.setText("");
		actualSupplier.setText("");
		actualAddress.setText("");
		actualAmount.setText("");
		stars.setRating(0);

		actualArrivalDateTime = new GregorianCalendar();
		actualArrivalDate.setText(formatDate(actualArrivalDateTime));
		actualArrivalTime.setText(formatTime(actualArrivalDateTime));

		actualDepartureDateTime = new GregorianCalendar();
		actualDepartureDate.setText(formatDate(actualDepartureDateTime));
		actualDepartureTime.setText(formatTime(actualDepartureDateTime));

		// Enable editing the actual expense fields. Hide the button to save an
		// actual expense and show the button to create a new actual expense.
		editingActualFields(true);
		saveActualButton.setVisibility(Button.GONE);
		createActualButton.setVisibility(Button.VISIBLE);
	}

	// TODO Make sure every field has values
	/**
	 * Creates a new actual expense.
	 * 
	 * @param view
	 *            The view that called this method.
	 */
	public void createActual(View view) {
		// Hide the button to create a new budgeted expense and disable editing
		// the actual expense fields.
		createActualButton.setVisibility(Button.GONE);
		editingActualFields(false);

		// Create the new actual expense in the database.
		actualId = (int) dbh.createActualExpense(budgetedId, actualArrivalDateTime, actualDepartureDateTime,
				Double.parseDouble(actualAmount.getText().toString()), actualDescription.getText().toString(),
				categoryIds.get(category.getSelectedItemPosition()), actualSupplier.getText().toString(),
				actualAddress.getText().toString(), (int) (stars.getRating()));
		actualAmount.setText(
				Double.valueOf(new DecimalFormat("#.00").format(Double.parseDouble(actualAmount.getText().toString())))
						.toString());
	}

	/**
	 * Enables editing the actual expense.
	 * 
	 * @param view
	 *            The view that called this method.
	 */
	public void editActual(View view) {
		editingActualFields(true);
	}

	// TODO Make sure every field has values
	/**
	 * Saves the changes made to a actual expense.
	 * 
	 * @param view
	 *            The view that called this method.
	 */
	public void saveActual(View view) {
		// Disable editing the actual expense fields.
		editingActualFields(false);

		// Save the changes to the database.
		dbh.updateActualExpense(actualId, actualArrivalDateTime, actualDepartureDateTime,
				Double.parseDouble(actualAmount.getText().toString()), actualDescription.getText().toString(),
				categoryIds.get(category.getSelectedItemPosition()), actualSupplier.getText().toString(),
				actualAddress.getText().toString(), (int) (stars.getRating()));
		actualAmount.setText(
				Double.valueOf(new DecimalFormat("#.00").format(Double.parseDouble(actualAmount.getText().toString())))
						.toString());
	}

	/**
	 * Deletes the actual expense.
	 * 
	 * @param view
	 *            The view that called this method.
	 */
	public void deleteActual(View view) {
		// Show the button to show the form for creating a new actual expense
		// and hide the linear layout containing all the actual expense fields.
		showNewActualButton.setVisibility(Button.VISIBLE);
		actualExpense.setVisibility(LinearLayout.GONE);
		dbh.deleteActualExpense(actualId);
	}

	/**
	 * Launches the DatePickerDialog.
	 * 
	 * @param view
	 *            The view that called this method.
	 */
	public void setDate(View view) {
		// Get the id of the view that is being set and save it to a local
		// variable so it can be used later.
		currentSettingId = view.getId();

		// Get the correct date and time depending on which view was clicked.
		if (currentSettingId == arrivalDate.getId())
			currentDateTime = arrivalDateTime;
		else if (currentSettingId == departureDate.getId())
			currentDateTime = departureDateTime;
		else if (currentSettingId == actualArrivalDate.getId())
			currentDateTime = actualArrivalDateTime;
		else
			currentDateTime = actualDepartureDateTime;

		// Set the DatePickerDialog to have the same date as the view that was
		// clicked.
		DatePickerDialog datePicker = new DatePickerDialog(this, handleSetDate, currentDateTime.get(Calendar.YEAR),
				currentDateTime.get(Calendar.MONTH), currentDateTime.get(Calendar.DAY_OF_MONTH));

		// Show the DatePickerDialog.
		datePicker.show();
	}

	private OnDateSetListener handleSetDate = new OnDateSetListener() {
		@Override
		/**
		 * Handles the event once a date has been picked using the
		 * DatePickerDialog.
		 * 
		 * @param view
		 *            The view that called this method.
		 * @param year
		 *            The year that was set.
		 * @param monthOfYear
		 *            The month that was set.
		 * @param dayOfMonth
		 *            The day that was set.
		 */
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			TextView currentSetView, correspondingDateView, correspondingTimeView;
			GregorianCalendar correspondingDateTime;

			// Get the correct fields depending on which field was initially
			// being set.
			if (currentSettingId == arrivalDate.getId()) {
				currentSetView = arrivalDate;
				currentDateTime = arrivalDateTime;
				correspondingDateView = departureDate;
				correspondingTimeView = departureTime;
				correspondingDateTime = departureDateTime;
			} else if (currentSettingId == departureDate.getId()) {
				currentSetView = departureDate;
				currentDateTime = departureDateTime;
				correspondingDateView = arrivalDate;
				correspondingTimeView = arrivalTime;
				correspondingDateTime = arrivalDateTime;
			} else if (currentSettingId == actualArrivalDate.getId()) {
				currentSetView = actualArrivalDate;
				currentDateTime = actualArrivalDateTime;
				correspondingDateView = actualDepartureDate;
				correspondingTimeView = actualDepartureTime;
				correspondingDateTime = actualDepartureDateTime;
			} else {
				currentSetView = actualDepartureDate;
				currentDateTime = actualDepartureDateTime;
				correspondingDateView = actualArrivalDate;
				correspondingTimeView = actualArrivalTime;
				correspondingDateTime = actualArrivalDateTime;
			}

			// Set the currently being set date and time with the new date.
			currentDateTime.set(Calendar.YEAR, year);
			currentDateTime.set(Calendar.MONTH, monthOfYear);
			currentDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

			// Update the view that is currently being set.
			currentSetView.setText(formatDate(currentDateTime));

			if (currentSettingId == arrivalDate.getId() || currentSettingId == actualArrivalDate.getId()) {
				// If the field being set is an arrival date, check if the new
				// arrival date and time is after the current departure date and
				// time. If it is, update the departure date and departure time
				// to match the arrival date date time.
				if (currentDateTime.compareTo(correspondingDateTime) > 0)
					updateDateTime(currentDateTime, correspondingDateTime, correspondingDateView,
							correspondingTimeView);
			} else {
				// If the field being set is a departure date, check if the new
				// departure date and time is before the current departure date
				// and time. If it is, update the departure date and departure
				// time to match the departure date and time.
				if (currentDateTime.compareTo(correspondingDateTime) < 0)
					updateDateTime(currentDateTime, correspondingDateTime, correspondingDateView,
							correspondingTimeView);
			}
		}
	};

	/**
	 * Launches the TimePickerDialog.
	 * 
	 * @param view
	 *            The view that called this method.
	 */
	public void setTime(View view) {
		// Get the id of the view that is being set and save it to a local
		// variable so it can be used later.
		currentSettingId = view.getId();

		// Get the correct date and time depending on which view was clicked.
		if (currentSettingId == arrivalTime.getId())
			currentDateTime = arrivalDateTime;
		else if (currentSettingId == departureTime.getId())
			currentDateTime = departureDateTime;
		else if (currentSettingId == actualArrivalTime.getId())
			currentDateTime = actualArrivalDateTime;
		else
			currentDateTime = actualDepartureDateTime;

		// Set the TimePickerDialog to have the same date as the view that was
		// clicked.
		TimePickerDialog timePicker = new TimePickerDialog(this, handleSetTime,
				currentDateTime.get(Calendar.HOUR_OF_DAY), currentDateTime.get(Calendar.MINUTE), false);

		// Show the TimePickerDialog.
		timePicker.show();
	}

	private OnTimeSetListener handleSetTime = new OnTimeSetListener() {
		@Override
		/**
		 * Handles the event once a time has been picked using the
		 * TimePickerDialog.
		 * 
		 * @param view
		 *            The view that called this method.
		 * @param hourOfDay
		 *            The hour of day that was set.
		 * @param minute
		 *            The minute that was set.
		 */
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			TextView currentSetView, correspondingDateView, correspondingTimeView;
			GregorianCalendar correspondingDateTime;

			// Get the correct fields depending on which field was initially
			// being set.
			if (currentSettingId == arrivalTime.getId()) {
				currentSetView = arrivalTime;
				currentDateTime = arrivalDateTime;
				correspondingDateView = departureDate;
				correspondingTimeView = departureTime;
				correspondingDateTime = departureDateTime;
			} else if (currentSettingId == departureTime.getId()) {
				currentSetView = departureTime;
				currentDateTime = departureDateTime;
				correspondingDateView = arrivalDate;
				correspondingTimeView = arrivalTime;
				correspondingDateTime = arrivalDateTime;
			} else if (currentSettingId == actualArrivalTime.getId()) {
				currentSetView = actualArrivalTime;
				currentDateTime = actualArrivalDateTime;
				correspondingDateView = actualDepartureDate;
				correspondingTimeView = actualDepartureTime;
				correspondingDateTime = actualDepartureDateTime;
			} else {
				currentSetView = actualDepartureTime;
				currentDateTime = actualDepartureDateTime;
				correspondingDateView = actualArrivalDate;
				correspondingTimeView = actualArrivalTime;
				correspondingDateTime = actualArrivalDateTime;
			}

			// Set the currently being set date and time with the new time.
			currentDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
			currentDateTime.set(Calendar.MINUTE, minute);

			// Update the view that is currently being set.
			currentSetView.setText(formatDate(currentDateTime));

			if (currentSettingId == arrivalTime.getId() || currentSettingId == actualArrivalTime.getId()) {
				// If the field being set is an arrival date, check if the new
				// arrival date and time is after the current departure date and
				// time. If it is, update the departure date and departure time
				// to match the arrival date date time.
				if (currentDateTime.compareTo(correspondingDateTime) > 0)
					updateDateTime(currentDateTime, correspondingDateTime, correspondingDateView,
							correspondingTimeView);
			} else {
				// If the field being set is a departure date, check if the new
				// departure date and time is before the current departure date
				// and time. If it is, update the departure date and departure
				// time to match the departure date and time.
				if (currentDateTime.compareTo(correspondingDateTime) < 0)
					updateDateTime(currentDateTime, correspondingDateTime, correspondingDateView,
							correspondingTimeView);
			}
		}
	};

	/**
	 * Updates a GregorianCalendar object and the date and time text views that
	 * rely on it to display.
	 * 
	 * @param currentDateTime
	 *            The correct date and time.
	 * @param correspondingDateTime
	 *            The date and time to update.
	 * @param correspondingDateView
	 *            The date text view to update.
	 * @param correspondingTimeView
	 *            The time text view to update.
	 */
	private void updateDateTime(GregorianCalendar currentDateTime, GregorianCalendar correspondingDateTime,
			TextView correspondingDateView, TextView correspondingTimeView) {
		correspondingDateTime.set(Calendar.YEAR, currentDateTime.get(Calendar.YEAR));
		correspondingDateTime.set(Calendar.MONTH, currentDateTime.get(Calendar.MONTH));
		correspondingDateTime.set(Calendar.DAY_OF_MONTH, currentDateTime.get(Calendar.DAY_OF_MONTH));
		correspondingDateTime.set(Calendar.HOUR_OF_DAY, currentDateTime.get(Calendar.HOUR_OF_DAY));
		correspondingDateTime.set(Calendar.MINUTE, currentDateTime.get(Calendar.MINUTE));
		correspondingDateView.setText(formatDate(correspondingDateTime));
		correspondingTimeView.setText(formatTime(correspondingDateTime));
	}

	/**
	 * Takes a GregorianCalendar and returns a formatted string representing the
	 * date.
	 * 
	 * @param date
	 *            The GregorianCalendar object with the date to format.
	 * @return The formatted string.
	 */
	private String formatDate(GregorianCalendar date) {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
		fmt.setCalendar(date);
		String dateFormatted = fmt.format(date.getTime());
		return dateFormatted;
	}

	/**
	 * Takes a GregorianCalendar and returns a formatted string representing the
	 * time.
	 * 
	 * @param time
	 *            The GregorianCalendar object with the time to format.
	 * @return The formatted string.
	 */
	private String formatTime(GregorianCalendar time) {
		SimpleDateFormat fmt = new SimpleDateFormat("hh:mm a", Locale.getDefault());
		fmt.setCalendar(time);
		String dateFormatted = fmt.format(time.getTime());
		return dateFormatted;
	}
}