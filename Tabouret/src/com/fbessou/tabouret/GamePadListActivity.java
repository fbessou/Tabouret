package com.fbessou.tabouret;

import com.fbessou.sofa.GameIOProxy;
import com.fbessou.tabouret.view.GamePadListView;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_pad_list);
		View v = findViewById(R.id.list_activity_layout);
		v.setBackgroundColor(Color.parseColor("#053856"));
		mList = new GamePadListView(this);
		((ScrollView)findViewById(R.id.scrollView)).addView(mList);
		
		// As we don't know if we are connected to a Network,
		// we assume that this device is the Group Owner.
		// So, we start a proxy that will handle redirection
		getApplicationContext().startService(new Intent(this,GameIOProxy.class));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.game_pad_list, menu);
	    return super.onCreateOptionsMenu(menu);
	}
}
