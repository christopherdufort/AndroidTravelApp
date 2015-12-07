package com.android.bonvoyagetravelapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class ItineraryDetails extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_itinerary_details);
		
		Intent intent = getIntent();
		int id = intent.getIntExtra("ITINERARYID", -1);
	}

}
