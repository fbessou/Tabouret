package com.fbessou.tabouret;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fbessou.sofa.GameBinder;
import com.fbessou.tabouret.view.GamePadLayout;

public class GamePadActivity extends Activity {
	//Configuration contains paths for layout/resource directory
	Configuration mConfig;
	RelativeLayout mainLayout;
	
	private GameBinder mGameBinder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		//call parent's constructor
		super.onCreate(savedInstanceState);
		
		// Initialize or retrieve the GameBinder fragment.
		FragmentManager fm = getFragmentManager();
		if((mGameBinder=(GameBinder) fm.findFragmentByTag("gameBinder"))==null){
			//This is the first time the activity is launched
			//we have to create a new GameBinder
			mGameBinder=new GameBinder();
			
			//register the newly created GameBinder in the fragment manager for
			// future usage
			fm.beginTransaction().add(mGameBinder, "gameBinder").commit();
		}
		
		Intent intent = getIntent();
		if(intent.hasExtra("gamepad_path")){
			Log.i("MainActivity",intent.getStringExtra("gamepad_path"));
			GamePadLoader gpload = new GamePadLoader("Coucou");
			GamePadLayout layout= gpload.parseFile(this,intent.getStringExtra("gamepad_path"));
			
			// Enable full-screen for this activity
			layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
			setContentView(layout);
			setRequestedOrientation(layout.getOrientation());
		}

	
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
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public GameBinder getGameBinder(){
		return mGameBinder;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	

}
