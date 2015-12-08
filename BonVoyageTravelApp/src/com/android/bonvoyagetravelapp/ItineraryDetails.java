package com.android.bonvoyagetravelapp;

import java.text.SimpleDateFormat;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class ItineraryDetails extends Activity {

	private DBHelper dbh;
	private int budgetedId;
	private Cursor cursor;
	private EditText category, location, description, supplier, address, amount;
	private TextView arrivalDate, arrivalTime, departureDate, departureTime;
	private boolean editing;
	private Button editButton;
	private TextView tv;
	private int locationId;
	private int categoryID;
	private GregorianCalendar arrivalDateTime, departureDateTime;
	private int currentSettingId;
	private GregorianCalendar currentDateTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_itinerary_details);
		dbh = DBHelper.getDBHelper(this);
		tv = (TextView) findViewById(R.id.itinerary_title);

		Intent intent = getIntent();
		budgetedId = intent.getIntExtra("ITINERARYID", -1);

		editButton = (Button) findViewById(R.id.itinerary_edit_btn);

		// TODO move this all off into another method async preferable
		category = (EditText) findViewById(R.id.itinerary_category);
		location = (EditText) findViewById(R.id.itinerary_location);
		description = (EditText) findViewById(R.id.itinerary_description);
		supplier = (EditText) findViewById(R.id.itinerary_supplier);
		address = (EditText) findViewById(R.id.itinerary_address);
		amount = (EditText) findViewById(R.id.itinerary_amount);
		arrivalDate = (TextView) findViewById(R.id.itinerary_arrival_date);
		arrivalTime = (TextView) findViewById(R.id.itinerary_arrival_time);
		departureDate = (TextView) findViewById(R.id.itinerary_departure_date);
		departureTime = (TextView) findViewById(R.id.itinerary_departure_time);

		if (budgetedId != -1) {
			tv.setText(R.string.itinerary_details_title);
			editing = false;
			// TODO remember to close cursor in a pause
			cursor = dbh.getBudgetedDetails(budgetedId);
			cursor.moveToFirst();
			// budgeted table ColumnIndex
			// id 0
			// trip id 1
			// location id 2
			// arrival 3
			// departure 4
			// amount 5
			// description 6
			// category 7
			// supplier 8
			// address 9

			arrivalDateTime = new GregorianCalendar();
			arrivalDateTime.setTimeInMillis(cursor.getLong(3) * 1000);
			arrivalDate.setText(formatDate(arrivalDateTime));
			arrivalTime.setText(formatTime(arrivalDateTime));

			departureDateTime = new GregorianCalendar();
			departureDateTime.setTimeInMillis(cursor.getLong(4) * 1000);
			departureDate.setText(formatDate(departureDateTime));
			departureTime.setText(formatTime(departureDateTime));

			amount.setText(cursor.getString(5));
			description.setText(cursor.getString(6));

			supplier.setText(cursor.getString(8));
			address.setText(cursor.getString(9));

			locationId = cursor.getInt(2); // foreign key
			categoryID = cursor.getInt(7); // foreign key

			cursor = dbh.getLocationById(locationId);
			cursor.moveToFirst();

			location.setText(cursor.getString(1) + ", " + cursor.getString(2));

			cursor = dbh.getCategoryById(categoryID);
			cursor.moveToFirst();

			category.setText(cursor.getString(1));

			lockAllFields();
		} else {
			tv.setText(R.string.itinerary_today_title);
			editButton.setText(R.string.itinerary_edit_save);
			editing = true;
		}

	}

	// TODO do this in a loop or something its really ugly
	private void lockAllFields() {

		if (editing) {
			Toast toast = Toast.makeText(this, "Editing in progress", Toast.LENGTH_SHORT);
			toast.show();
			category.setFocusableInTouchMode(true);
			category.setFocusable(true);
			category.setClickable(true);
			location.setFocusableInTouchMode(true);
			location.setFocusable(true);
			location.setClickable(true);
			description.setFocusableInTouchMode(true);
			description.setFocusable(true);
			description.setClickable(true);
			supplier.setFocusableInTouchMode(true);
			supplier.setFocusable(true);
			supplier.setClickable(true);
			address.setFocusableInTouchMode(true);
			address.setFocusable(true);
			address.setClickable(true);
			amount.setFocusableInTouchMode(true);
			amount.setFocusable(true);
			amount.setClickable(true);
			arrivalDate.setClickable(true);
			arrivalTime.setClickable(true);
			departureDate.setClickable(true);
			departureTime.setClickable(true);
		} else {
			Toast toast = Toast.makeText(this, "Editing disabled", Toast.LENGTH_SHORT);
			toast.show();
			category.setFocusable(false);
			category.setClickable(false);
			location.setFocusable(false);
			location.setClickable(false);
			description.setFocusable(false);
			description.setClickable(false);
			supplier.setFocusable(false);
			supplier.setClickable(false);
			address.setFocusable(false);
			address.setClickable(false);
			amount.setFocusable(false);
			amount.setClickable(false);
			arrivalDate.setClickable(false);
			arrivalTime.setClickable(false);
			departureDate.setClickable(false);
			departureTime.setClickable(false);
		}
	}

	// TODO find better solution?
	public void editBudgeted(View view) {

		if (editing) {
			editing = false;
			editButton.setText(R.string.itinerary_edit_budgeted);
			lockAllFields();
			updateDb();
		} else {
			editing = true;
			editButton.setText(R.string.itinerary_edit_save);
			lockAllFields();
		}

	}

	private void updateDb() {
		// FIXME transfer string into date arrival.getText(),
		// departure.getText()
		// TODO probably another way does not need tostring
		dbh.updateBudgetedExpense(budgetedId, arrivalDateTime, departureDateTime,
				Double.parseDouble(amount.getText().toString()), description.getText().toString(), categoryID,
				supplier.getText().toString(), address.getText().toString());

	}

	public void setDate(View view) {
		currentSettingId = view.getId();
		if (currentSettingId == arrivalDate.getId())
			currentDateTime = arrivalDateTime;
		else
			currentDateTime = departureDateTime;

		DatePickerDialog datePicker = new DatePickerDialog(this, handleSetDate, currentDateTime.get(Calendar.YEAR),
				currentDateTime.get(Calendar.MONTH), currentDateTime.get(Calendar.DAY_OF_MONTH));

		if (currentSettingId == arrivalDate.getId())
			datePicker.getDatePicker()
					.setMaxDate(new GregorianCalendar(departureDateTime.get(Calendar.YEAR),
							departureDateTime.get(Calendar.MONTH), departureDateTime.get(Calendar.DAY_OF_MONTH), 23, 59,
							59).getTimeInMillis());
		else
			datePicker.getDatePicker()
					.setMinDate(new GregorianCalendar(arrivalDateTime.get(Calendar.YEAR),
							arrivalDateTime.get(Calendar.MONTH), arrivalDateTime.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
									.getTimeInMillis());

		datePicker.show();
	}

	private OnDateSetListener handleSetDate = new OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			// TODO Auto-generated method stub
			TextView currentSetView;
			if (currentSettingId == arrivalDate.getId()) {
				currentSetView = arrivalDate;
				currentDateTime = arrivalDateTime;
			} else {
				currentSetView = departureDate;
				currentDateTime = departureDateTime;
			}

			currentDateTime.set(Calendar.YEAR, year);
			currentDateTime.set(Calendar.MONTH, monthOfYear);
			currentDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			currentSetView.setText(formatDate(currentDateTime));
		}
	};

	public void setTime(View view) {
		currentSettingId = view.getId();
		if (currentSettingId == arrivalTime.getId())
			currentDateTime = arrivalDateTime;
		else
			currentDateTime = departureDateTime;

		TimePickerDialog timePicker = new TimePickerDialog(this, handleSetTime, currentDateTime.get(Calendar.HOUR_OF_DAY),
				currentDateTime.get(Calendar.MINUTE), false);
		timePicker.show();
	}

	private OnTimeSetListener handleSetTime = new OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			if (currentSettingId == arrivalTime.getId()) {
				// Save the old value of hour and minute in case the newly set
				// hour and minute makes the arrival date time after the
				// departure date time.
				int oldHour = arrivalDateTime.get(Calendar.HOUR_OF_DAY);
				int oldMinute = arrivalDateTime.get(Calendar.MINUTE);

				arrivalDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
				arrivalDateTime.set(Calendar.MINUTE, minute);

				if (arrivalDateTime.compareTo(departureDateTime) > 0) {
					arrivalDateTime.set(Calendar.HOUR_OF_DAY, oldHour);
					arrivalDateTime.set(Calendar.MINUTE, oldMinute);

					// TODO: Show some kind of error message saying the set time
					// made the arrival date time after the departure date time
				}else{
					arrivalTime.setText(formatTime(arrivalDateTime));
				}
			} else{
				// Save the old value of hour and minute in case the newly set
				// hour and minute makes the arrival date time after the
				// departure date time.
				int oldHour = departureDateTime.get(Calendar.HOUR_OF_DAY);
				int oldMinute = departureDateTime.get(Calendar.MINUTE);

				departureDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
				departureDateTime.set(Calendar.MINUTE, minute);

				if (departureDateTime.compareTo(arrivalDateTime) < 0) {
					departureDateTime.set(Calendar.HOUR_OF_DAY, oldHour);
					departureDateTime.set(Calendar.MINUTE, oldMinute);

					// TODO: Show some kind of error message saying the set time
					// made the arrival date time after the departure date time
				}else{
					departureTime.setText(formatTime(departureDateTime));
				}
			}
		}
	};

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