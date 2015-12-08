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
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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

		currencyInputEditText = (EditText) findViewById(R.id.currency_input);
		homeCurrencyTextView = (TextView) findViewById(R.id.home_currency);
		conversionCurrencyTextView = (TextView) findViewById(R.id.conversion_currency);
		currencyOutputTextView = (TextView) findViewById(R.id.currency_output);
		currencyErrorTextView = (TextView) findViewById(R.id.currency_error);

		if (savedInstanceState != null) {
			homeCurrency = savedInstanceState.getString("homeCurrency");
			conversionCurrency = savedInstanceState.getString("conversionCurrency");
			convertedAmount = savedInstanceState.getDouble("convertedAmount");
		} else {
			prefs = PreferenceManager.getDefaultSharedPreferences(this);

			String homeCountryCode;
			if (prefs.getString("home", null) == null) {
				// TODO If there is no home country code set, ask for one.
				homeCountryCode = "CA";
			} else {
				homeCountryCode = prefs.getString("home", null);
			}

			homeCurrency = (Currency.getInstance(new Locale("", homeCountryCode))).getCurrencyCode();

			String conversionCountryCode;
			// TODO I thought the conversion currency was based on the location the current trip
			// If there is no current trip, set the conversion country code to
			// France so the conversion currency will be Euros.
			if (prefs.getString("destination", null) == null) {
				conversionCountryCode = "FR";
			} else {
				// TODO Get country code from current trip
				conversionCountryCode = prefs.getString("destination", null);
			}

			conversionCurrency = (Currency.getInstance(new Locale("", conversionCountryCode))).getCurrencyCode();
		}

		homeCurrencyTextView.setText(homeCurrency);
		conversionCurrencyTextView.setText(conversionCurrency);
		currencyOutputTextView.setText(Double.valueOf(new DecimalFormat("#.00").format(convertedAmount)) + "");
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		savedInstanceState.putDouble("convertedAmount", convertedAmount);
		savedInstanceState.putString("homeCurrency", homeCurrency);
		savedInstanceState.putString("conversionCurrency", conversionCurrency);
	}

	public void convertCurrency(View view) {
		try {
			currencyErrorTextView.setText("");

			inputAmount = Double.parseDouble(currencyInputEditText.getText().toString());

			ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(
					Context.CONNECTIVITY_SERVICE);
			if (connectivityManager != null) {
				NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
				if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
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
			Log.d("ConversionTask", "Running ConversionTask");
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
					return conversionRate;

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
				// TODO Show an error?
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Show an error?
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Show error?
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Show error?
				e.printStackTrace();
			}
			return conversionRate;
		}

		@Override
		protected void onPostExecute(Double conversionRate) {
			// If the conversionRate is not null, use it to calculate the
			if (conversionRate != null) {
				double conversion = conversionRate.doubleValue();
				convertedAmount = inputAmount * conversion;
				currencyOutputTextView.setText(Double.valueOf(new DecimalFormat("#.00").format(convertedAmount)) + "");
			}
		}

		private String readInputStream(InputStream stream) throws IOException {
			char[] buffer = new char[MAXBYTE];
			Reader reader = null;
			reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"), MAXBYTE);
			reader.read(buffer);
			return new String(buffer);
		}

	}
}
