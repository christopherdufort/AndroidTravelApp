package com.android.bonvoyagetravelapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class WeatherCheckActivity extends Activity {

	private String country, nameOfArea = "";
	private String mainForecast, descriptionForecast = "";
	private String windSpeed = "";
	private String temp, minTemp, maxTemp = "";
	private String humidity, pressure = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weathercheck_activity);

		Intent intent = getIntent();
		String result = intent.getStringExtra("download");

		parseResults(result);

		assignText();
	}

	private void assignText(){
		
		TextView countryView = (TextView)findViewById(R.id.locationText);
		TextView generalDescriptionView = (TextView)findViewById(R.id.generalDescriptionValue);
		TextView detailedDescriptionView = (TextView)findViewById(R.id.detailedDescriptionValue);
		TextView temperatureView = (TextView)findViewById(R.id.temperatureValue);
		TextView temperatureMinView = (TextView)findViewById(R.id.temperatureMinValue);
		TextView temperatureMaxView = (TextView)findViewById(R.id.temperatureMaxValue);
		TextView humidityView = (TextView)findViewById(R.id.humidityValue);
		TextView pressureView = (TextView)findViewById(R.id.pressureValue);
		TextView speedView = (TextView)findViewById(R.id.speedValue);
		
		countryView.setText(country + ", " + nameOfArea);
		generalDescriptionView.setText(mainForecast);
		detailedDescriptionView.setText(descriptionForecast);
		temperatureView.setText(temp + "\u00b0");
		temperatureMinView.setText(minTemp + "\u00b0");
		temperatureMaxView.setText(maxTemp + "\u00b0");
		humidityView.setText(humidity + "%");
		pressureView.setText(pressure + " hPa");
		speedView.setText(windSpeed + " m/s");
		
	}

	private void parseResults(String result) {

		try {
			JSONObject jsonObj = new JSONObject(result);

			JSONObject sysObj = jsonObj.getJSONObject("sys");
			country = sysObj.getString("country");
			
			nameOfArea = jsonObj.getString("name");

			JSONArray array = jsonObj.getJSONArray("weather");
			descriptionForecast = array.getJSONObject(0).getString("description");
			mainForecast = array.getJSONObject(0).getString("main");

			JSONObject weatherObj = jsonObj.getJSONObject("wind");
			windSpeed = "" + weatherObj.getDouble("speed");

			JSONObject mainObj = jsonObj.getJSONObject("main");
			humidity = "" + mainObj.getInt("humidity");
			pressure = "" + mainObj.getInt("pressure");
			temp = "" + mainObj.getDouble("temp");
			minTemp = "" + mainObj.getDouble("temp_min");
			maxTemp = "" + mainObj.getDouble("temp_max");

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}// end of parsing

}
