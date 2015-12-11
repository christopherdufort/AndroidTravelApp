package com.android.bonvoyagetravelapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// first time user
		if (prefs.getString("name", null) == null) {
			showAlertSettingsBox("","","","","");
		} else {
			// second run, send saved name
			showUserName(prefs.getString("name", ""));
		}
	}

	private void showUserName(String name) {
		TextView nameView = (TextView) findViewById(R.id.greetingText);

		// the name is from the actual input or shared prefs depending
		nameView.setText("Hello " + name);
	}

	private void showAlertSettingsBox(String name, String email, String password, String home, String destination) {

		//inflate layout
		LayoutInflater factory = LayoutInflater.from(this);
		final View view = factory.inflate(R.layout.settings_form, null);

		//build layout
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle(R.string.nameDialogTitle);
		alertDialog.setMessage(R.string.nameDialogInstruction);
		alertDialog.setView(view);

		//take all inputs and set text sent in
		final EditText nameInput = (EditText) view.findViewById(R.id.nameInput);
		nameInput.setText(name);
		
		final EditText emailInput = (EditText) view.findViewById(R.id.emailInput);
		emailInput.setText(email);
		
		final EditText passwordInput = (EditText) view.findViewById(R.id.passwordInput);
		passwordInput.setText(password);
		
		final EditText homeInput = (EditText) view.findViewById(R.id.homeInput);
		homeInput.setText(home);
		
		final EditText destinationInput = (EditText) view.findViewById(R.id.destinationInput);
		destinationInput.setText(destination);

		//if user clicks on okay
		alertDialog.setPositiveButton(R.string.okBtn, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				//get input
				String name = nameInput.getText().toString().trim();
				String email = emailInput.getText().toString().trim();
				String password = passwordInput.getText().toString().trim();
				String home = homeInput.getText().toString().trim();
				String destination = destinationInput.getText().toString().trim();

				//if one is empty, ask again
				if (name.equals("") || email.equals("") || password.equals("") || home.equals("")
						|| destination.equals("")) {
					// TOO MANY RESOURCES USED: will crash after some spamming
					Toast.makeText(MainActivity.this, R.string.nameProblem, Toast.LENGTH_LONG).show();
					showAlertSettingsBox("","","","","");
				} else {
					//save in shared prefs and display name
					SharedPreferences.Editor editor = prefs.edit();

					editor.putString("name", name);
					editor.putString("email", email);
					editor.putString("password", password);
					editor.putString("home", home);
					editor.putString("destination", destination);

					editor.commit();
					showUserName(name);
				}
			}
		});

		//show dialog constructed above
		alertDialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		String url = "http://www.dawsoncollege.qc.ca/computer-science-technology/";

		if (id == R.id.menu_dawson) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
			return true;
		}

		if (id == R.id.menu_about) {
			aboutActivity(null);
			return true;
		}

		if (id == R.id.menu_settings) {
			showAlertSettingsBox(prefs.getString("name", ""),
					prefs.getString("email", ""), prefs.getString("password", ""),
					prefs.getString("home", ""), prefs.getString("destination", ""));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void aboutActivity(View view) {
		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
	}
	
	public void currentTripItinerary(View view) {
		Intent intent = new Intent(this, ItineraryActivity.class);
		intent.putExtra("CURRENT", true);
		startActivity(intent);
	}
	public void today(View view){
		Intent intent = new Intent(this, ItineraryActivity.class);
		intent.putExtra("TODAY", true);
		startActivity(intent);
	}

	public void manageTrips(View view) {
		Intent intent = new Intent(this, ManageTripsActivity.class);
		startActivity(intent);
	}
	
	public void tipCalculator(View view) {
		Intent intent = new Intent(this, TipCalculator.class);
		startActivity(intent);
	}

	public void distanceVolumeWeightConversion(View view) {
		Intent intent = new Intent(this, UnitConversionActivity.class);
		startActivity(intent);
	}

	public void weatherCheck(View view) {
		Intent intent = new Intent(this, WeatherCheckActivity.class);
		startActivity(intent);
	}

	public void currencyConversion(View view) {
		Intent intent = new Intent(this, CurrencyConversion.class);
		startActivity(intent);
	}


	public void launchComingSoon() {
		Intent intent = new Intent(this, ComingSoon.class);
		startActivity(intent);
	}

	public void budget(View view) {
		launchComingSoon();
	}

	public void localCustoms(View view) {
		launchComingSoon();
	}

	public void interests(View view) {
		launchComingSoon();
	}

	public void nearMe(View view) {
		launchComingSoon();
	}
}