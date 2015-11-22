package com.android.bonvoyagetravelapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class TipCalculator extends Activity {

	// default
	private double percentage = 10.0;
	private TextView error;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tip_activity);
		
		error = (TextView) findViewById(R.id.billErrorText);
	}

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

	public void calculateTip(View view) {
		try {
			error.setText("");

			EditText billTotalInput = (EditText) findViewById(R.id.billTotalAmount);
			double billAmount = Double.parseDouble(billTotalInput.getText().toString());

			// takes text of item
			Spinner people = (Spinner) findViewById(R.id.peopleSpinner);
			double numberOfPeople = Double.parseDouble(people.getSelectedItem().toString());

			double tipTotalAmount = calculateTipTotal(billAmount);
			double finalPrice = calculateFinalPrice(billAmount, tipTotalAmount);
			calculateEachPersonBill(finalPrice, numberOfPeople);
			
		} catch (NumberFormatException nfe) {
			error.setText("Your bill amount must be a Number");
		}
	}

	private void calculateEachPersonBill(double finalPrice, double numberOfPeople) {
		TextView eachPersonBill = (TextView) findViewById(R.id.eachPersonAmount);

		double dividedPrice = finalPrice / numberOfPeople;

		eachPersonBill.setText("" + dividedPrice + "$");
	}

	private double calculateFinalPrice(double billAmount, double tipTotalAmount) {
		TextView priceTotal = (TextView) findViewById(R.id.finalPriceAmount);

		double finalPrice = billAmount + tipTotalAmount;

		priceTotal.setText("" + finalPrice + "$");

		return finalPrice;
	}

	private double calculateTipTotal(double billAmount) {
		TextView tipTotal = (TextView) findViewById(R.id.tipTotalAmount);

		double tipTotalAmount = (percentage * billAmount) / 100.0;

		tipTotal.setText("" + tipTotalAmount + "$");

		return tipTotalAmount;
	}

}
