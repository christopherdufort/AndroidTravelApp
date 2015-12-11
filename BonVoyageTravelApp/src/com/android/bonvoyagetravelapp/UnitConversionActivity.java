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

/**
 * UnitConversionActivity is a stand along activity used to perform conversions between different units of measurements.
 * By using an exisiting API JConvert, we dont have to reinvent the wheel in terms of coding unit conversion,
 * and have access to many more conversions then we have time to code on our own.
 * Conversions can be done between Imperial and metric, or any combination include Volume,Distance and Mass conversions.
 * 
 * @author Irina Patrocinio Frazao
 * @author Christopher Dufort
 * @author Annie So
 * @since JDK 1.6
 * @version 1.0.0-Release
 */
public class UnitConversionActivity extends Activity {

	private String unitChoice = "Distance"; //default
	private String resultToShow = "";
	
	private TextView resultView;
	private Spinner from;
	private Spinner to;
	
	/**
	 * Overriden onCreate method used to setup the UI.
	 * Also retrieves choice of units from bundle and called spinner population.
	 */
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
	 * This event handler is called when different radio buttons are selected.
	 * Depending on which button is selected a new unitchoice will be set.
	 * unitChoice is used by the JConvert api and populate spinners method.
	 * 
	 * if time implement this through tabs and fragments instead :)
	 * @param view
	 * 			The view object that triggered this event handler to be called.
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
	/**
	 * This method is called to populate the two spinners in the UI.
	 * Depending on the value of the unitChoice variable when passed to the JConvert api will return all the units for associated choice.
	 * Setting the dropDownViewResource and setting adapters.
	 */
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
	/**
	 * This method is called to convert one unit of measuremen to another.
	 * This method makes use of the jconvert api to access multiple units and their associated conversions.
	 * Jconvert accepts unitchoice and types as strings and converts returning values to be displayed.
	 * This conversion should be spun off into an asynctask to run in background as it does take some time.
	 *
	 * @see http://jconvert.sourceforge.net/howtoAPI.html
	 * @param view
	 * 			The view that triggeres this event handler
	 */
	public void convertUnits(View view){
		double valueToConvert;
		String convertMe;
		
		EditText textValueToConvert = (EditText) findViewById(R.id.valueToConvert);
		convertMe = textValueToConvert.getText().toString();
		if (convertMe.length() < 1){
			valueToConvert = 0; //Should display visible error message.
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
