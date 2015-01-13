package com.fbessou.tabouret;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;

/**
 * Activity Displaying the list of GamePad installed on this device.
 * 
 * @author Frank Bessou
 *
 */
public class GamePadListActivity extends Activity{

	private GamePadListView mList;
	Configuration mConf;

	/**
	 * Allow detection of click
	 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_pad_list);
		View v = findViewById(R.id.list_activity_layout);
		mList = new GamePadListView(this);
		((ScrollView)findViewById(R.id.scrollView)).addView(mList);
		//getActionBar().setDisplayHomeAsUpEnabled(true);
	}

}
