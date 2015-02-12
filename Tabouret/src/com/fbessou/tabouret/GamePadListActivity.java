package com.fbessou.tabouret;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;

import com.fbessou.sofa.GameIOProxy;
import com.fbessou.tabouret.view.GamePadListView;

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
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.action_nickname:
			AlertDialog.Builder b = new Builder(this);
			final EditText t = new EditText(this);
			b.setView(t);
			b.setTitle("Type yarh' nickname : ");
			b.setPositiveButton("Save", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String name = t.getText().toString();
					if(name.length() > 0)
						mConf.setNickname(name);
				}
			});
			b.show();
		}
		return super.onOptionsItemSelected(item);
	}
}
