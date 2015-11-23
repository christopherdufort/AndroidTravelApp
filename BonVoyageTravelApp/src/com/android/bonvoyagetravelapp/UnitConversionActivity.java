package com.android.bonvoyagetravelapp;

import java.util.Iterator;
import java.util.List;


import com.edsdev.jconvert.domain.ConversionType;
import com.edsdev.jconvert.persistence.DataLoader;
import com.edsdev.jconvert.presentation.ConversionTypeData;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


public class UnitConversionActivity extends Activity {

	private String unitChoice = "Distance"; //default
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.unit_conversion_activity);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.unit_conversion, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Implement this through tabs and fragments instead :)
	 * @param view
	 */
	public void onUnitRadioButtonClicked(View view){
		
		switch (view.getId()) {
		case R.id.radio_distance:
			unitChoice="Distance";
			populateSpinners();
			break;
			
		case R.id.radio_mass:
			unitChoice="Mass";
			populateSpinners();
			break;

		case R.id.radio_volume:
			unitChoice="Volume";
			populateSpinners();;
			break;
		}
	}
	private void populateSpinners() {
	
		ArrayAdapter<String> adapter;
		
		if (unitChoice.equals("Mass")){
			 adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray((R.array.mass_spinner_choices)));
		}
		else if (unitChoice.equals("Volume")){
			 adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray((R.array.volume_spinner_choices)));
		}
		else{
			adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray((R.array.distance_spinner_choices)));
		}
			
			
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		Spinner fromSpinner = (Spinner) findViewById(R.id.distanceToSpinner);
		Spinner toSpinner = (Spinner) findViewById(R.id.distanceFromSpinner);
		
		fromSpinner.setAdapter(adapter);
		toSpinner.setAdapter(adapter);
			
	}

	public void convertUnits(View view){
		double valueToConvert;
		String convertMe;
		
		EditText textValueToConvert = (EditText) findViewById(R.id.valueToConvert);
		convertMe = textValueToConvert.getText().toString();
		if (convertMe.length() < 1){
			valueToConvert = 0; //TODO REPLACEME WITH A VISIBLE ERROR MESSAGE.
		}
		else
			valueToConvert = Double.parseDouble(textValueToConvert.getText().toString());
		
		List<?> domainData = new DataLoader().loadData();
		
        ConversionTypeData ctd = null;
        Iterator<?> iter = domainData.iterator();
        while (iter.hasNext()) 
        {
            ConversionType type = (ConversionType) iter.next();
            if (type.getTypeName().equals(unitChoice)) 
            {
                ctd = new ConversionTypeData(type);
                break;
            }
        } 
        TextView resultShown = (TextView) findViewById(R.id.resultShown);
        
        Spinner from = (Spinner) findViewById(R.id.distanceFromSpinner);
        String fromUnit = from.getSelectedItem().toString();
        
        Spinner to = (Spinner) findViewById(R.id.distanceToSpinner);
        String toUnit = to.getSelectedItem().toString();
        
        resultShown.setText(Double.toString(ctd.convert(valueToConvert, fromUnit, toUnit)));
	}
}
