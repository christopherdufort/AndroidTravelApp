package com.android.bonvoyagetravelapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Currency;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * An activity that handles currency conversions.
 * 
 * @author Irina Patrocinio Frazao
 * @author Christopher Dufort
 * @author Annie So
 */
public class CurrencyConversion extends Activity {
	private EditText currencyInputEditText;
	private TextView homeCurrencyTextView, conversionCurrencyTextView, currencyOutputTextView;
	private TextView currencyErrorTextView;
	private SharedPreferences prefs;
	private String homeCurrency, conversionCurrency;
	private double inputAmount, convertedAmount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.currency_conversion);

		// Get the required UI views.
		currencyInputEditText = (EditText) findViewById(R.id.currency_input);
		homeCurrencyTextView = (TextView) findViewById(R.id.home_currency);
		conversionCurrencyTextView = (TextView) findViewById(R.id.conversion_currency);
		currencyOutputTextView = (TextView) findViewById(R.id.currency_output);
		currencyErrorTextView = (TextView) findViewById(R.id.currency_error);

		// If there is a bundle, use the bundle values.
		if (savedInstanceState != null) {
			homeCurrency = savedInstanceState.getString("homeCurrency");
			conversionCurrency = savedInstanceState.getString("conversionCurrency");
			convertedAmount = savedInstanceState.getDouble("convertedAmount");
		} else {
			prefs = PreferenceManager.getDefaultSharedPreferences(this);

			// If there is no home country code in the shared preferences, use
			// Canada as a default. There should never be a case where there is
			// no home country code because the app does not let you proceed if
			// you do not fill in all the necessary information. If there is a
			// home country code in the shared preferences, use it.
			String homeCountryCode;
			if (prefs.getString("home", null) == null) {
				homeCountryCode = "CA";
			} else {
				homeCountryCode = prefs.getString("home", null);
			}

			// Use the Currency class to get the home currency based on the home
			// country code.
			homeCurrency = (Currency.getInstance(new Locale("", homeCountryCode))).getCurrencyCode();

			String conversionCountryCode;
			// If there is no destination country code in the shared
			// preferences, set the conversion country code to France so the
			// conversion currency will be Euro. If there is a destination
			// country code in the shared preferences, use it.
			if (prefs.getString("destination", null) == null) {
				conversionCountryCode = "FR";
			} else {
				conversionCountryCode = prefs.getString("destination", null);
			}

			// Use the Currency class to get the currency to convert to based on
			// the destination country code.
			conversionCurrency = (Currency.getInstance(new Locale("", conversionCountryCode))).getCurrencyCode();
		}

		// Set the next views with the appropriate values.
		homeCurrencyTextView.setText(homeCurrency);
		conversionCurrencyTextView.setText(conversionCurrency);
		currencyOutputTextView.setText(Double.valueOf(new DecimalFormat("#.00").format(convertedAmount)) + "");
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		// Save the values that are not preserved automatically by Android in a
		// bundle.
		savedInstanceState.putDouble("convertedAmount", convertedAmount);
		savedInstanceState.putString("homeCurrency", homeCurrency);
		savedInstanceState.putString("conversionCurrency", conversionCurrency);
	}

	public void convertCurrency(View view) {
		try {
			currencyErrorTextView.setText("");

			// Try to parse the input into a double. If the input is not a valid
			// double, catch the error and show an error message.
			inputAmount = Double.parseDouble(currencyInputEditText.getText().toString());

			ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(
					Context.CONNECTIVITY_SERVICE);

			// Check that there is a network manager service. If there isn't
			// one, show an error message.
			if (connectivityManager != null) {
				NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

				// Check that there is net work connectivity. If there isn't,
				// show an error message.
				if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
					// Start the AsyncTask responsible for currency conversion.
					ConversionTask ct = new ConversionTask();
					ct.execute(homeCurrency, conversionCurrency);
				} else {
					currencyErrorTextView.setText("Error. No network connectivity.");
				}
			} else {
				currencyErrorTextView.setText("Error. No network manager service.");
			}
		} catch (NumberFormatException nfe) {
			currencyErrorTextView.setText("Error. Input is not a valid amount.");
		}
	}

	private class ConversionTask extends AsyncTask<String, Void, Double> {
		private int MAXBYTE = 500;

		@Override
		protected Double doInBackground(String... params) {
			Double conversionRate = null;
			try {
				String urlString = "http://api.fixer.io/latest?base=" + URLEncoder.encode(params[0], "UTF-8")
						+ "&symbols=" + URLEncoder.encode(params[1], "UTF-8");
				URL url = new URL(urlString);

				HttpURLConnection connection = (HttpURLConnection) url.openConnection();

				// Sets the maximum amount of time to wait for a stream read.
				connection.setReadTimeout(10000);

				// Sets the maximum amount of time to wait while connection.
				connection.setConnectTimeout(15000);

				// Sets the HTTP request method
				connection.setRequestMethod("GET");

				// Specifies that the connection can receive data.
				connection.setDoInput(true);

				// Starts the connection.
				connection.connect();

				// Gets the request's response code
				int response = connection.getResponseCode();
				// If the response is anything but OK (200) return null.
				if (response != HttpURLConnection.HTTP_OK)
					return null;

				// If the response is OK, gets the InputStream for data coming
				// from the connection.
				InputStream is = connection.getInputStream();
				String data = readInputStream(is);
				is.close();
				connection.disconnect();

				// Gets a JSON object and parses it to find the conversion rate.
				JSONObject responseObject = new JSONObject(data);
				JSONObject ratesObject = responseObject.getJSONObject("rates");
				double conversion = ratesObject.getDouble(params[1]);
				conversionRate = Double.valueOf(conversion);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
			return conversionRate;
		}

		/**
		 * Receives a conversion rate that it uses to convert the amount entered
		 * in the home currency into the amount in the destination currency.
		 * 
		 * @param conversionRate
		 *            The conversion rate from the home currency to the
		 *            destination currency.
		 */
		@Override
		protected void onPostExecute(Double conversionRate) {
			// If the conversionRate is not null, use it to calculate the
			// converted amount. If the conversion rate is null, show an error
			// message.
			if (conversionRate != null) {
				double conversion = conversionRate.doubleValue();
				convertedAmount = inputAmount * conversion;
				currencyOutputTextView.setText(Double.valueOf(new DecimalFormat("#.00").format(convertedAmount)) + "");
				currencyErrorTextView.setText("");
			} else {
				currencyErrorTextView.setText("Error. Try again.");
			}
		}

		/**
		 * Reads the data from an InputStream and returns it as a string.
		 * 
		 * @param stream
		 *            The InputStream to read.
		 * @return A string containing the contents of the InputStream.
		 * @throws IOException
		 */
		private String readInputStream(InputStream stream) throws IOException {
			char[] buffer = new char[MAXBYTE];
			Reader reader = null;
			reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"), MAXBYTE);
			reader.read(buffer);
			return new String(buffer);
		}

	}
}