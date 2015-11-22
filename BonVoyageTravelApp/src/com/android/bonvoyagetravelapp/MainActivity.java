package com.android.bonvoyagetravelapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
			showAlertNameBox();
		} else
			// second run, send saved name
			showUserName(prefs.getString("name", ""));
	}

	private void showUserName(String name) {
		TextView nameView = (TextView) findViewById(R.id.greetingText);

		// the name is from the actual input or shared prefs depending
		nameView.setText("Hello " + name);
	}

	private void showAlertNameBox() {

		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle(R.string.nameDialogTitle);
		alertDialog.setMessage(R.string.nameDialogInstruction);

		final EditText input = new EditText(this);
		alertDialog.setView(input);

		alertDialog.setPositiveButton(R.string.okBtn, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				// save it in prefs for next time and send input for now
				String name = input.getText().toString();

				if (name.matches("[a-zA-z]+")) {
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString("name", name);
					editor.commit();
					showUserName(name);
				}
				else
				{
					Toast.makeText(MainActivity.this, R.string.nameProblem, Toast.LENGTH_LONG).show();
					//by default an alert box will close when button is clicked
					//re-instanciating it to let the user enter name again
					//TOO MANY RESOURCES USED: will crasha after some spamming
					showAlertNameBox();
				}
			}
		});

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
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.menu_dawson) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void tipCalculator(View view) {
		Intent intent = new Intent(this, TipCalculator.class);
		startActivity(intent);
	}
}
