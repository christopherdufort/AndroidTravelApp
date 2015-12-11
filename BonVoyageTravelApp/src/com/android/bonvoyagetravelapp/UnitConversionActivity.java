package com.android.bonvoyagetravelapp;

import java.util.Iterator;
import java.util.List;


import com.edsdev.jconvert.domain.ConversionType;
import com.edsdev.jconvert.persistence.DataLoader;
import com.edsdev.jconvert.presentation.ConversionTypeData;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


public class UnitConversionActivity extends Activity {

	private String unitChoice = "Distance"; //default
	private String resultToShow = "";
	
	private TextView resultView;
	private Spinner from;
	private Spinner to;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.unit_conversion_activity);
		
		// Single handle to each view.
		from = (Spinner) findViewById(R.id.distanceFromSpinner);
		to = (Spinner) findViewById(R.id.distanceToSpinner);
		resultView = (TextView) findViewById(R.id.resultShown);
		
		//If there is a bundle (previously destroyed instance of activity)
		if(savedInstanceState != null){
			unitChoice= savedInstanceState.getString("UNIT");
			
			populateSpinners();
			
			resultToShow = savedInstanceState.getString("RESULT");
			resultView.setText(resultToShow);
					
		}
	}

	/**
	 * This method is called when the instance of the activity is destroyed.
	 * Save UI state changes to the savedInstanceState.
	 * This bundle will be passed to onCreate if the process is
	 * killed and restarted(rotate screen).
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	  super.onSaveInstanceState(savedInstanceState);
	  
	  savedInstanceState.putString("UNIT", unitChoice);
	  savedInstanceState.putString("RESULT", resultToShow);
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
		
		//This chunk of code is used to retrieve conversion from the JConvert API
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
        } // End of chunk.
              
        String fromUnit = from.getSelectedItem().toString();
        
        String toUnit = to.getSelectedItem().toString();
        
        resultToShow = Double.toString(ctd.convert(valueToConvert, fromUnit, toUnit));
        
        resultView.setText(resultToShow);
	}
}
