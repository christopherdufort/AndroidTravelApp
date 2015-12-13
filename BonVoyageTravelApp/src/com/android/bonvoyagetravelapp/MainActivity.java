package com.android.bonvoyagetravelapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

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

/**
 * Primary starting point for the entire application. This is the main activity
 * and it is responsible for initial UI setup and also responsible for
 * retrieving settings information from the user. This class contains all of the
 * event handlers for the various main menu buttons of the application.
 * 
 * @author Irina Patrocinio Frazao
 * @author Christopher Dufort
 * @author Annie So
 * @since JDK 1.6
 * @version 1.0.0-Release
 */
public class MainActivity extends Activity {

	// Used for long term storage
	private SharedPreferences prefs;

	/**
	 * Overridden onCreate method handles setting up the UI and settings dialog.
	 * Check preferences for existing settings otherwise pops up a dialog. After
	 * initial run settings will be retrieved from shared preferences.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// Shared preferences name does not exists = first time launch of
		// application
		if (prefs.getString("name", null) == null) {
			showAlertSettingsBox("", "", "", "", "");
		} else {
			// Not first time run, retrieve names from preferences for display
			// purposes.
			showUserName(prefs.getString("name", ""));
		}
	}

	/**
	 * Private display method used to modify a text field in the GUI greeting
	 * the user with their provided name. Name is retrieved from shared
	 * preferences in onCreate or from input after dialog pop.
	 * 
	 * @param name
	 *            A string representation of the users name provided from
	 *            settings.
	 */
	private void showUserName(String name) {
		TextView nameView = (TextView) findViewById(R.id.greetingText);

		// The name is from the actual input or shared prefs depending.
		nameView.setText("Hello " + name);
	}

	/**
	 * ShowAlertSettingsBox method is called to display an modal alert
	 * requesting settings information from the user. Values passed into
	 * parameters may be empty strings or values stored in shared preferences.
	 * Input entered into the dialog is saved in shared preferences to be used
	 * later for authentication or region selection.
	 * 
	 * @param name
	 *            Empty string or Name provided by user used for greetings.
	 * @param email
	 *            Empty string or Email provided by user used for sever
	 *            authentication.
	 * @param password
	 *            Empty string or Password provided by user used for server
	 *            authentication.
	 * @param home
	 *            Empty string or Home country code used for default weather and
	 *            currency.
	 * @param destination
	 *            Empty string or Destination country code use for currency
	 *            conversion and weather.
	 */
	private void showAlertSettingsBox(String name, String email, String password, String home, String destination) {

		// Inflate layout
		LayoutInflater factory = LayoutInflater.from(this);
		final View view = factory.inflate(R.layout.settings_form, null);

		// Build layout of dialog.
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle(R.string.nameDialogTitle);
		alertDialog.setMessage(R.string.nameDialogInstruction);
		alertDialog.setView(view);

		// Populate input fields from parameter data
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

		// if user clicks on okay
		alertDialog.setPositiveButton(R.string.okBtn, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				// get input provided by user from fields
				String name = nameInput.getText().toString().trim();
				String email = emailInput.getText().toString().trim();
				String password = passwordInput.getText().toString().trim();
				String home = homeInput.getText().toString().trim().toUpperCase(Locale.getDefault());
				String destination = destinationInput.getText().toString().trim().toUpperCase(Locale.getDefault());

				ArrayList<String> allCountryCodes = new ArrayList<String>(Arrays.asList(Locale.getISOCountries()));

				// if any one is empty, ask again (all fields required)
				if (name.equals("") || email.equals("") || password.equals("") || home.equals("")
						|| destination.equals("")) {
					// Warning TOO MANY RESOURCES USED: may crash if repeated
					// spamming by user.
					Toast.makeText(MainActivity.this, R.string.nameProblem, Toast.LENGTH_LONG).show();
					// Redeploy alert.
					showAlertSettingsBox(name, email, password, home, destination);
				} else if (!allCountryCodes.contains(home)) {
					Toast.makeText(MainActivity.this, "Enter a valid home country code.", Toast.LENGTH_LONG).show();
					showAlertSettingsBox(name, email, password, "", destination);
				} else if (!allCountryCodes.contains(destination)) {
					Toast.makeText(MainActivity.this, "Enter a valid destination country code.", Toast.LENGTH_LONG).show();
					showAlertSettingsBox(name, email, password, home, "");
				}
				else {
					// All values provided
					// save in shared prefs and display name
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

		// show dialog constructed above
		alertDialog.show();
	}

	/**
	 * Overridden onCreateOptionsMenu used to create an inflate personal menu.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Overriden onOptionsItemSelected method used to fire events based on menu
	 * clicks.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		String url = "http://www.dawsoncollege.qc.ca/computer-science-technology/";

		// Launch web browser going to dawson comp-sci page.
		if (id == R.id.menu_dawson) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
			return true;
		}

		// Call local about acitivty launching method.
		if (id == R.id.menu_about) {
			aboutActivity(null);
			return true;
		}

		// launch modal alert requesting settings.
		if (id == R.id.menu_settings) {
			showAlertSettingsBox(prefs.getString("name", ""), prefs.getString("email", ""),
					prefs.getString("password", ""), prefs.getString("home", ""), prefs.getString("destination", ""));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Launch AboutActivity activity when button is pressed.
	 * 
	 * @param view
	 *            The view that triggered the onClick event calling this method.
	 */
	public void aboutActivity(View view) {
		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
	}

	/**
	 * Launch ItineraryActivity activity when button is pressed. Sending in Key
	 * CURRENT in bundle which will trigger specific data to be displayed.
	 * 
	 * @param view
	 *            The view that triggered the onClick event calling this method.
	 */
	public void currentTripItinerary(View view) {
		Intent intent = new Intent(this, ItineraryActivity.class);
		intent.putExtra("CURRENT", true);
		startActivity(intent);
	}

	/**
	 * Launch ItineraryActivity activity when button is pressed. Sending in Key
	 * TODAY in bundle which will trigger specific data to be displayed.
	 * 
	 * @param view
	 *            The view that triggered the onClick event calling this method.
	 */
	public void today(View view) {
		Intent intent = new Intent(this, ItineraryActivity.class);
		intent.putExtra("TODAY", true);
		startActivity(intent);
	}

	/**
	 * Launch ManageTripsActivity activity when button is pressed.
	 * 
	 * @param view
	 *            The view that triggered the onClick event calling this method.
	 */
	public void manageTrips(View view) {
		Intent intent = new Intent(this, ManageTripsActivity.class);
		startActivity(intent);
	}

	/**
	 * Launch TipCalculator activity when button is pressed.
	 * 
	 * @param view
	 *            The view that triggered the onClick event calling this method.
	 */
	public void tipCalculator(View view) {
		Intent intent = new Intent(this, TipCalculator.class);
		startActivity(intent);
	}

	/**
	 * Launch UnitConversionActivity activity when button is pressed.
	 * 
	 * @param view
	 *            The view that triggered the onClick event calling this method.
	 */
	public void distanceVolumeWeightConversion(View view) {
		Intent intent = new Intent(this, UnitConversionActivity.class);
		startActivity(intent);
	}

	/**
	 * Launch WeatherCheckActivity activity when button is pressed.
	 * 
	 * @param view
	 *            The view that triggered the onClick event calling this method.
	 */
	public void weatherCheck(View view) {
		Intent intent = new Intent(this, WeatherCheckActivity.class);
		startActivity(intent);
	}

	/**
	 * Launch CurrencyConversion activity when button is pressed.
	 * 
	 * @param view
	 *            The view that triggered the onClick event calling this method.
	 */
	public void currencyConversion(View view) {
		Intent intent = new Intent(this, CurrencyConversion.class);
		startActivity(intent);
	}

	/**
	 * Launch ComingSoon activity when button is pressed.
	 * 
	 * @param view
	 *            The view that triggered the onClick event calling this method.
	 */
	public void launchComingSoon() {
		Intent intent = new Intent(this, ComingSoon.class);
		startActivity(intent);
	}

	/**
	 * Not implemented yet. Launch budget activity when button is pressed.
	 * 
	 * @param view
	 *            The view that triggered the onClick event calling this method.
	 */
	public void budget(View view) {
		launchComingSoon();
	}

	/**
	 * Not implemented yet. Launch localCustoms activity when button is pressed.
	 * 
	 * @param view
	 *            The view that triggered the onClick event calling this method.
	 */
	public void localCustoms(View view) {
		launchComingSoon();
	}

	/**
	 * Not implemented yet. Launch interests activity when button is pressed.
	 * 
	 * @param view
	 *            The view that triggered the onClick event calling this method.
	 */
	public void interests(View view) {
		launchComingSoon();
	}

	/**
	 * Not implemented yet. Launch nearMe activity when button is pressed.
	 * 
	 * @param view
	 *            The view that triggered the onClick event calling this method.
	 */
	public void nearMe(View view) {
		launchComingSoon();
	}
}