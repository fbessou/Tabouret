package com.fbessou.tabouret;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fbessou.sofa.GamePadIOClient;
import com.fbessou.sofa.GamePadIOClient.GameMessageListener;
import com.fbessou.sofa.GamePadInformation;
import com.fbessou.sofa.OutputEvent;
import com.fbessou.tabouret.view.GamePadLayout;

public class GamePadActivity extends Activity implements GameMessageListener {
	//Configuration contains paths for layout/resource directory
	Configuration mConfig;
	RelativeLayout mainLayout;
	
	private GamePadIOClient mGamePadIOClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mConfig = new Configuration(this);
		//call parent's constructor
		super.onCreate(savedInstanceState);
		
		com.fbessou.sofa.Log.setLogFilterMask(com.fbessou.sofa.Log.FILTER_NOTHING);
		// Initialize or retrieve the GamePadIOClient fragment.
		mGamePadIOClient = GamePadIOClient.getGamePadIOClient((Activity)this, new GamePadInformation(this));
		mGamePadIOClient.setGameMessageListener(this);
		
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
	
	public GamePadIOClient getGamePadIOClient(){
		return mGamePadIOClient;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	
	@Override
	public void onOutputReceived(OutputEvent event) {
		Toast.makeText(this, "Output event received : "+event.toString(), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onGameRenamed(String newName) {
		Toast.makeText(this, "Game has renamed : "+newName, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onGameLeft() {
		Toast.makeText(this, "Game has left", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onCustomMessageReceived(String customMessage) {
		// TODO Auto-generated method stub
		
	}
	

}
