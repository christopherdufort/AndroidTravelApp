package com.android.bonvoyagetravelapp;

import java.text.DecimalFormat;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class TipCalculator extends Activity {

	// default
	private double percentage = 10.0;

	private TextView error;
	private TextView tipTotal;
	private TextView priceTotal;
	private TextView eachPersonBill;

	private double dividedPrice;
	private double finalPrice;
	private double tipTotalAmount;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tip_activity);
		
		error = (TextView) findViewById(R.id.billErrorText);
		tipTotal = (TextView) findViewById(R.id.tipTotalAmount);
		priceTotal = (TextView) findViewById(R.id.finalPriceAmount);
		eachPersonBill = (TextView) findViewById(R.id.eachPersonAmount);

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

	protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		savedInstanceState.putDouble("eachPersonBill", dividedPrice);
		savedInstanceState.putDouble("priceTotal", finalPrice);
		savedInstanceState.putDouble("tipTotal", tipTotalAmount);
		savedInstanceState.putDouble("percentage", percentage);

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
