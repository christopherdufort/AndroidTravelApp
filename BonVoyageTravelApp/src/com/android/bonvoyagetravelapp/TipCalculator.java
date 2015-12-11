package com.android.bonvoyagetravelapp;

import java.text.DecimalFormat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * This class implements a tip calculator that let's
 * the user input  his price, the percentage of tip
 * he wants to give and with how many people will 
 * the final bill be split.
 * 
 * @author Irina Patrocinio Frazao, Christopher Dufort and Annie So
 */
public class TipCalculator extends Activity {

	// default
	private double percentage = 10.0;
	private double dividedPrice;
	private double finalPrice;
	private double tipTotalAmount;
	
	private TextView error;
	private TextView tipTotal;
	private TextView priceTotal;
	private TextView eachPersonBill;

	/**
	 * This method sets the layout of the page and keeps
	 * the state of the user's input when the activity
	 * gets recreated during the rotation of the device.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tip_activity);
		
		//get references to view elements
		error = (TextView) findViewById(R.id.billErrorText);
		tipTotal = (TextView) findViewById(R.id.tipTotalAmount);
		priceTotal = (TextView) findViewById(R.id.finalPriceAmount);
		eachPersonBill = (TextView) findViewById(R.id.eachPersonAmount);

		/*retrieves the user input from the bundle and set them back.
		 *keeping state on rotation*/
		if (savedInstanceState != null) {
			tipTotalAmount = savedInstanceState.getDouble("tipTotal");
			dividedPrice = savedInstanceState.getDouble("eachPersonBill");
			finalPrice = savedInstanceState.getDouble("priceTotal");
			percentage = savedInstanceState.getDouble("percentage");
			
			tipTotal.setText("" + tipTotalAmount);
			priceTotal.setText("" + finalPrice);
			eachPersonBill.setText("" + dividedPrice);
		}
	}

	/**
	 * This method saves the user's input in the bundle
	 */
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		
		//saves the user's input in the bundle
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putDouble("eachPersonBill", dividedPrice);
		savedInstanceState.putDouble("priceTotal", finalPrice);
		savedInstanceState.putDouble("tipTotal", tipTotalAmount);
		savedInstanceState.putDouble("percentage", percentage);
	}

	/**
	 * This method saves the percentage of tip the user wants
	 * to add to his bill depending which radio button is selected.
	 * 
	 * @param view
	 * 			the specific radio button that was selected
	 */
	public void onRadioButtonClicked(View view) {

		switch (view.getId()) {
		case R.id.radio_ten:
			percentage = 10.0;
			break;

		case R.id.radio_fifteen:
			percentage = 15.0;
			break;

		case R.id.radio_twenty:
			percentage = 20.0;
			break;
		}
	}

	/**
	 * This method calculates the tip amount,
	 * the final amount of the bill and how much
	 * each person has to pay.
	 * It populates the view with all these calculated numbers.
	 * 
	 * @param view
	 * 			the specific button that was clicked
	 */
	public void calculateTip(View view) {
		try {
			error.setText("");

			EditText billTotalInput = (EditText) findViewById(R.id.billTotalAmount);
			double billAmount = Double.parseDouble(billTotalInput.getText().toString());

			// takes text of item
			Spinner people = (Spinner) findViewById(R.id.peopleSpinner);
			double numberOfPeople = Double.parseDouble(people.getSelectedItem().toString());

			// calculate the total of the tip
			tipTotalAmount = (percentage * billAmount) / 100.0;
			tipTotal.setText("$" + tipTotalAmount);

			// calculate final price
			finalPrice = billAmount + tipTotalAmount;
			priceTotal.setText("$" + finalPrice);

			// calculate each persons bill
			dividedPrice = finalPrice / numberOfPeople;
			eachPersonBill.setText("$" + Double.valueOf(new DecimalFormat("#.##").format(dividedPrice)));

		} catch (NumberFormatException nfe) {
			error.setText("Your bill amount must be a Number");
		}
	}

}
