package com.android.bonvoyagetravelapp;

import java.util.Date;

import android.app.Activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ItineraryDetails extends Activity {
	
	private DBHelper dbh;
	private int budgetedId;
	private Cursor cursor;
	private EditText category,location,description,supplier,address,amount,arrival,departure;
	private boolean editing;
	private Button editButton;
	private TextView tv;
	private int locationId;
	private int categoryID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_itinerary_details);
		dbh=DBHelper.getDBHelper(this);
		tv = (TextView) findViewById(R.id.itinerary_title);
				
		Intent intent = getIntent();
		budgetedId = intent.getIntExtra("ITINERARYID", -1);
		
		editButton = (Button) findViewById(R.id.itinerary_edit_btn);
		
		//TODO move this all off into another method async preferable
		category = (EditText) findViewById(R.id.itinerary_category);
		location = (EditText) findViewById(R.id.itinerary_location);
		description = (EditText) findViewById(R.id.itinerary_description);
		supplier = (EditText) findViewById(R.id.itinerary_supplier);
		address = (EditText) findViewById(R.id.itinerary_address);
		amount = (EditText) findViewById(R.id.itinerary_amount);
		arrival = (EditText) findViewById(R.id.itinerary_arrival);
		departure = (EditText) findViewById(R.id.itinerary_departure);
		
		if (budgetedId != -1)
		{
			tv.setText(R.string.itinerary_details_title);
			editing = false;
			//TODO remember to close cursor in a pause
			cursor = dbh.getBudgetedDetails(budgetedId);
			cursor.moveToFirst();
			//		budgeted table	ColumnIndex
			//		id  				0
			//		trip id 			1
			//		location id 		2
			//		arrival 			3
			//		departure			4
			//		amount				5
			//		description 		6
			//		category			7
			//		supplier			8
			//		address				9
			
			
			arrival.setText(cursor.getString(3));
			departure.setText(cursor.getString(4));
			amount.setText(cursor.getString(5));
			description.setText(cursor.getString(6));
			
			supplier.setText(cursor.getString(8));
			address.setText(cursor.getString(9));
			
			 locationId = cursor.getInt(2); //foreign key
			 categoryID = cursor.getInt(7); //foreign key
			
			cursor = dbh.getLocationById(locationId);
			cursor.moveToFirst();
			
			location.setText(cursor.getString(1) + ", " + cursor.getString(2));
			
			
			cursor = dbh.getCategoryById(categoryID);
			cursor.moveToFirst();
			
			category.setText(cursor.getString(1));
			
			lockAllFields();
		}
		else{
			tv.setText(R.string.itinerary_today_title);
			editButton.setText(R.string.itinerary_edit_save);
			editing = true;
		}
			
		

	}
	//TODO do this in a loop or something its really ugly
	private void lockAllFields() {

		if (editing){
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
			arrival.setFocusableInTouchMode(true);
			arrival.setFocusable(true);
			arrival.setClickable(true);
			departure.setFocusableInTouchMode(true);
			departure.setFocusable(true);
			departure.setClickable(true);
		}
		else
		{
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
			arrival.setFocusable(false);
			arrival.setClickable(false);
			departure.setFocusable(false);
			departure.setClickable(false);
		}
	}
	//TODO find better solution?
	public void editBudgeted(View view) {
	
		if (editing){
			editing = false;
			editButton.setText(R.string.itinerary_edit_budgeted);
			lockAllFields();
			updateDb();
		}
		else{
			editing= true;
			editButton.setText(R.string.itinerary_edit_save);
			lockAllFields();
		}

	}
	private void updateDb() {
		//FIXME transfer string into date arrival.getText(), departure.getText()
		//TODO probably another way does not need tostring
		dbh.updateBudgetedExpense(budgetedId, new Date(), new Date(),
				Double.parseDouble(amount.getText().toString()), description.getText().toString(), categoryID, supplier.getText().toString(), address.getText().toString());

	}

}
