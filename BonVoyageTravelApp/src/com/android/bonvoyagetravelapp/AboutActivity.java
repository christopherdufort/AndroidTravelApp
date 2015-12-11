package com.android.bonvoyagetravelapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

/**
 * This class fires several intents to launch specific web pages.
 * It is used in the about section of the app.
 * 
 * @author Irina Patrocinio Frazao, Christopher Dufort and Annie So
 */
public class AboutActivity extends Activity {

	/**
	 * This method sets the layout of the activity
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_activity);
	}

	/**
	 * This method fires an intent to launch the dawson web page.
	 * 
	 * @param view
	 * 			the view element that was clicked on
	 */
	public void dawsonWebPage(View view) {
		String url = "http://www.dawsoncollege.qc.ca/";
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(intent);
	}

	/**
	 * This method fires an intent to launch dawson's
	 *  computer science web page.
	 * 
	 * @param view
	 * 			the view element that was clicked on
	 */
	public void computerScienceWebPage(View view) {
		String url = "http://www.dawsoncollege.qc.ca/computer-science-technology/";
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(intent);
	}

	/**
	 * This method fires an intent to launch the wiki made 
	 * as an engineering log for this project.
	 * 
	 * @param view
	 * 			the view element that was clicked on
	 */
	public void launchWiki(View view) {
		String url = "https://sites.google.com/site/bonvoyagetravelproject/";
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(intent);
	}

}
