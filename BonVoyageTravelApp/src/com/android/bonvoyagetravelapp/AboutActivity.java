package com.android.bonvoyagetravelapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_activity);
	}

	public void dawsonWebPage(View view) {
		String url = "http://www.dawsoncollege.qc.ca/";
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(intent);
	}

	public void computerScienceWebPage(View view) {
		String url = "http://www.dawsoncollege.qc.ca/computer-science-technology/";
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(intent);
	}

	public void launchWiki(View view) {
		String url = "https://sites.google.com/site/bonvoyagetravelproject/";
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(intent);
	}

}
