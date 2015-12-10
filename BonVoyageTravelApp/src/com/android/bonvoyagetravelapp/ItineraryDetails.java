package com.android.bonvoyagetravelapp;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
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

public class ItineraryDetails extends Activity {

	private DBHelper dbh;
	private int tripId, budgetedId, actualId;
	private Cursor cursor;
	private ArrayList<Integer> categoryIds, locationIds;
	private ArrayList<String> categoryNames, locationNames;
	private Spinner category, location;
	private EditText description, supplier, address, amount;
	private TextView arrivalDate, arrivalTime, departureDate, departureTime;
	private LinearLayout actualExpense;
	private EditText actualDescription, actualSupplier, actualAddress, actualAmount;
	private TextView actualArrivalDate, actualArrivalTime, actualDepartureDate, actualDepartureTime;
	private RatingBar stars;
	private Button editBudgetedButton, saveBudgetedButton, createBudgetedButton, deleteBudgetedButton;
	private Button editActualButton, saveActualButton, createActualButton, deleteActualButton, createNewActualButton;
	private TextView title;
	private GregorianCalendar arrivalDateTime, departureDateTime;
	private GregorianCalendar actualArrivalDateTime, actualDepartureDateTime;
	private int currentSettingId;
	private GregorianCalendar currentDateTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_itinerary_details);

		// Get a handle to the DBHelper.
		dbh = DBHelper.getDBHelper(this);

		// Get a handle to the common elements
		title = (TextView) findViewById(R.id.itinerary_title);

		// TODO move this all off into another method async preferable
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
		createNewActualButton = (Button) findViewById(R.id.create_actual_btn);

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

		Intent intent = getIntent();
		budgetedId = intent.getIntExtra("ITINERARYID", -1);
		tripId = intent.getIntExtra("tripId", -1);

		// Fill the spinners with the correct values
		cursor = dbh.getCategories();
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

		cursor = dbh.getLocations();
		locationIds = new ArrayList<Integer>();
		locationNames = new ArrayList<String>();
		Log.d("locations", cursor.getCount() + "");
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

		for (String s : locationNames) {
			Log.d("locations", s);
		}

		if (budgetedId != -1) {
			createBudgetedButton.setVisibility(Button.GONE);
			title.setText(R.string.itinerary_details_title);
			// TODO remember to close cursor in a pause
			cursor = dbh.getBudgetedDetails(budgetedId);
			cursor.moveToFirst();

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

			amount.setText(Double.valueOf(
					new DecimalFormat("#.00").format(cursor.getDouble(cursor.getColumnIndex(DBHelper.COLUMN_AMOUNT))))
					.toString());
			description.setText(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DESCRIPTION)));

			supplier.setText(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME_OF_SUPPLIER)));
			address.setText(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ADDRESS)));

			category.setSelection(
					categoryIds.indexOf(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_CATEGORY_ID))));

			cursor = dbh.getLocationById(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_LOCATION_ID)));
			cursor.moveToFirst();

			location.setSelection(locationNames.indexOf(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_CITY))
					+ ", " + cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_COUNTRY_CODE))));

			editingBudgetedFields(false);

			// Get the actual expense associated with the budgeted expense.
			cursor = dbh.getActualExpenses(budgetedId);

			// If there is an actual expense associated with the budgeted
			// expense, show it. If not add a button to create one.
			if (cursor.getCount() != 0) {
				createNewActualButton.setVisibility(Button.GONE);
				createActualButton.setVisibility(Button.GONE);
				actualExpense.setVisibility(LinearLayout.VISIBLE);

				cursor.moveToFirst();

				actualId = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ID));

				actualDescription.setText(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DESCRIPTION)));
				actualSupplier.setText(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME_OF_SUPPLIER)));
				actualAddress.setText(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ADDRESS)));
				actualAmount
						.setText(Double
								.valueOf(new DecimalFormat("#.00")
										.format(cursor.getDouble(cursor.getColumnIndex(DBHelper.COLUMN_AMOUNT))))
						.toString());

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

				stars.setRating(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_STARS)));

				editingActualFields(false);
			} else {
				createNewActualButton.setVisibility(Button.VISIBLE);
				actualExpense.setVisibility(LinearLayout.GONE);
			}
		} else {
			createNewActualButton.setVisibility(Button.GONE);
			actualExpense.setVisibility(LinearLayout.GONE);

			title.setText(R.string.itinerary_today_title);

			arrivalDateTime = new GregorianCalendar();
			arrivalDate.setText(formatDate(arrivalDateTime));
			arrivalTime.setText(formatTime(arrivalDateTime));

			departureDateTime = new GregorianCalendar();
			departureDate.setText(formatDate(departureDateTime));
			departureTime.setText(formatTime(departureDateTime));

			editingBudgetedFields(true);
			saveBudgetedButton.setVisibility(Button.GONE);
		}
	}

	private void editingBudgetedFields(boolean editing) {
		if (editing) {
			Toast.makeText(this, "Editing in progress", Toast.LENGTH_SHORT).show();
			editBudgetedButton.setVisibility(Button.GONE);
			deleteBudgetedButton.setVisibility(Button.GONE);
			saveBudgetedButton.setVisibility(Button.VISIBLE);
		} else {
			Toast.makeText(this, "Editing disabled", Toast.LENGTH_SHORT).show();
			editBudgetedButton.setVisibility(Button.VISIBLE);
			deleteBudgetedButton.setVisibility(Button.VISIBLE);
			saveBudgetedButton.setVisibility(Button.GONE);
		}

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

	private void editingActualFields(boolean editing) {
		if (editing) {
			Toast.makeText(this, "Editing in progress", Toast.LENGTH_SHORT).show();
			editActualButton.setVisibility(Button.GONE);
			deleteActualButton.setVisibility(Button.GONE);
			saveActualButton.setVisibility(Button.VISIBLE);
		} else {
			Toast.makeText(this, "Editing disabled", Toast.LENGTH_SHORT).show();
			editActualButton.setVisibility(Button.VISIBLE);
			deleteActualButton.setVisibility(Button.VISIBLE);
			saveActualButton.setVisibility(Button.GONE);
		}

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

	public void editBudgeted(View view) {
		editingBudgetedFields(true);
	}

	// TODO Make sure every field has values.
	public void saveBudgeted(View view) {
		editingBudgetedFields(false);
		dbh.updateBudgetedExpense(budgetedId, locationIds.get(location.getSelectedItemPosition()), arrivalDateTime,
				departureDateTime, Double.parseDouble(amount.getText().toString()), description.getText().toString(),
				categoryIds.get(category.getSelectedItemPosition()), supplier.getText().toString(),
				address.getText().toString());
		amount.setText(Double.valueOf(new DecimalFormat("#.00").format(Double.parseDouble(amount.getText().toString())))
				.toString());
	}

	// TODO Make sure every field has values.
	public void createBudgeted(View view) {
		createBudgetedButton.setVisibility(Button.GONE);
		title.setText(R.string.itinerary_details_title);
		createNewActualButton.setVisibility(Button.VISIBLE);
		editingBudgetedFields(false);

		budgetedId = (int) dbh.createBudgetedExpense(tripId, locationIds.get(location.getSelectedItemPosition()),
				arrivalDateTime, departureDateTime, Double.parseDouble(amount.getText().toString()),
				description.getText().toString(), categoryIds.get(category.getSelectedItemPosition()),
				supplier.getText().toString(), address.getText().toString());
		amount.setText(Double.valueOf(new DecimalFormat("#.00").format(Double.parseDouble(amount.getText().toString())))
				.toString());
	}
	
	public void deleteBudgeted(View view){
		dbh.deleteBudgetedExpense(budgetedId);
		finish();
	}

	public void editActual(View view) {
		editingActualFields(true);
	}

	// TODO Make sure every field has values
	public void saveActual(View view) {
		editingActualFields(false);
		dbh.updateActualExpense(actualId, actualArrivalDateTime, actualDepartureDateTime,
				Double.parseDouble(actualAmount.getText().toString()), actualDescription.getText().toString(),
				categoryIds.get(category.getSelectedItemPosition()), actualSupplier.getText().toString(),
				actualAddress.getText().toString(), (int) (stars.getRating()));
		actualAmount.setText(
				Double.valueOf(new DecimalFormat("#.00").format(Double.parseDouble(actualAmount.getText().toString())))
						.toString());
	}
	
	public void showActual(View view){
		createNewActualButton.setVisibility(Button.GONE);
		actualExpense.setVisibility(LinearLayout.VISIBLE);
		
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
		
		editingActualFields(true);
		saveActualButton.setVisibility(Button.GONE);
		createActualButton.setVisibility(Button.VISIBLE);
	}

	// TODO Make sure every field has values
	public void createActual(View view) {
		createActualButton.setVisibility(Button.GONE);
		editingActualFields(false);

		actualId = (int) dbh.createActualExpense(budgetedId, actualArrivalDateTime, actualDepartureDateTime,
				Double.parseDouble(actualAmount.getText().toString()), actualDescription.getText().toString(),
				categoryIds.get(category.getSelectedItemPosition()), actualSupplier.getText().toString(),
				actualAddress.getText().toString(), (int) (stars.getRating()));
		actualAmount.setText(
				Double.valueOf(new DecimalFormat("#.00").format(Double.parseDouble(actualAmount.getText().toString())))
						.toString());
	}
	
	public void deleteActual(View view){
		createNewActualButton.setVisibility(Button.VISIBLE);
		actualExpense.setVisibility(LinearLayout.GONE);
		dbh.deleteActualExpense(actualId);
	}

	public void setDate(View view) {
		currentSettingId = view.getId();

		if (currentSettingId == arrivalDate.getId())
			currentDateTime = arrivalDateTime;
		else if (currentSettingId == departureDate.getId())
			currentDateTime = departureDateTime;
		else if (currentSettingId == actualArrivalDate.getId())
			currentDateTime = actualArrivalDateTime;
		else
			currentDateTime = actualDepartureDateTime;

		DatePickerDialog datePicker = new DatePickerDialog(this, handleSetDate, currentDateTime.get(Calendar.YEAR),
				currentDateTime.get(Calendar.MONTH), currentDateTime.get(Calendar.DAY_OF_MONTH));

		datePicker.show();
	}

	private OnDateSetListener handleSetDate = new OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			TextView currentSetView, correspondingDateView, correspondingTimeView;
			GregorianCalendar correspondingDateTime;

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

			currentDateTime.set(Calendar.YEAR, year);
			currentDateTime.set(Calendar.MONTH, monthOfYear);
			currentDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

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

	public void setTime(View view) {
		currentSettingId = view.getId();

		if (currentSettingId == arrivalTime.getId())
			currentDateTime = arrivalDateTime;
		else if (currentSettingId == departureTime.getId())
			currentDateTime = departureDateTime;
		else if (currentSettingId == actualArrivalTime.getId())
			currentDateTime = actualArrivalDateTime;
		else
			currentDateTime = actualDepartureDateTime;

		TimePickerDialog timePicker = new TimePickerDialog(this, handleSetTime,
				currentDateTime.get(Calendar.HOUR_OF_DAY), currentDateTime.get(Calendar.MINUTE), false);
		timePicker.show();
	}

	private OnTimeSetListener handleSetTime = new OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			TextView currentSetView, correspondingDateView, correspondingTimeView;
			GregorianCalendar correspondingDateTime;

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

			currentDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
			currentDateTime.set(Calendar.MINUTE, minute);

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

	private String formatDate(GregorianCalendar date) {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-M-d");
		fmt.setCalendar(date);
		String dateFormatted = fmt.format(date.getTime());
		return dateFormatted;
	}

	private String formatTime(GregorianCalendar time) {
		SimpleDateFormat fmt = new SimpleDateFormat("hh:mm a");
		fmt.setCalendar(time);
		String dateFormatted = fmt.format(time.getTime());
		return dateFormatted;
	}
}