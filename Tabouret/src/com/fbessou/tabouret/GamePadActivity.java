package com.fbessou.tabouret;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;

import com.fbessou.tabouret.view.GamePadLayout;
import com.fbessou.tabouret.view.JoystickView;
import com.fbessou.tabouret.view.JoystickView.OnPositionChangedListener;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class GamePadActivity extends Activity {
	Configuration mConfig;
	RelativeLayout mainLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// If we are starting this activity from the gamepadChooser,
		//TODO load the gamepad
		Intent intent = getIntent();
		if(intent.hasExtra("gamepad_path")){
			Log.i("MainActivity",intent.getStringExtra("gamepad_path"));
			GamePadLoader gpload = new GamePadLoader(this, "Coucou");
			GamePadLayout layout= gpload.parseFile(intent.getStringExtra("gamepad_path"));
			layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
			setContentView(layout);
			setRequestedOrientation(layout.getOrientation());
		}
		Toast.makeText(this, "How did you", 100).show();
	}
	
	void loadLayout(String xmlLayoutName) {
		mainLayout.setBackgroundColor(Color.parseColor("red"));
	}

	void saveLayout(String fileName) {
		try {
			String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
			Toast.makeText(this, dir,Toast.LENGTH_SHORT).show();
			File myDir = new File(dir+"/Tabouret");
			myDir.mkdir();
			File f = new File(myDir, fileName);
			OutputStreamWriter o = new OutputStreamWriter(new FileOutputStream(f));
			o.write("Test");
			o.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.i(getPackageName(), "Can't write to external storage");
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
}
