package com.fbessou.tabouret;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;

import com.fbessou.tabouret.view.JoystickView;
import com.fbessou.tabouret.view.JoystickView.OnPositionChangedListener;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
	Configuration mConfig;
	RelativeLayout mainLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mConfig=new Configuration(this);
		
		setContentView(R.layout.activity_main);
		mainLayout = (RelativeLayout) findViewById(R.id.rootNode);

		JoystickView jv = new JoystickView(this, 700, 1000, null);
		jv.setOnPositionChangedListener(new OnPositionChangedListener() {
			@Override
			public void positionChanged(JoystickView joystick, float px, float py) {
				Log.i("###", ""+px+ " "+py);
			}
		});
		mainLayout.addView(jv);
		
		mainLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		GamePadLoader gpload = new GamePadLoader(this, "Coucou");
		
		gpload.parseXML(new StringReader(
				"<?xml version=\"1.0\"?>"
				+ "<gamepad>"
				+ "<information><name>Zelda Macaroni</name><version>1.5</version><description>Testing version</description></information>"
				+ "<layout>"
				+ "</layout>"
				+ "</gamepad>"));
		
		loadLayout("test");
		saveLayout("testFrankRae");
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
