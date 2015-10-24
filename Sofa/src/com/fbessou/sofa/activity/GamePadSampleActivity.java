package com.fbessou.sofa.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.fbessou.sofa.GamePadIOHelper;
import com.fbessou.sofa.GamePadInformation;
import com.fbessou.sofa.R;
import com.fbessou.sofa.indicator.FeedbackIndicator;
import com.fbessou.sofa.indicator.Indicator;
import com.fbessou.sofa.indicator.TextIndicator;
import com.fbessou.sofa.indicator.TextIndicator.WriteMode;
import com.fbessou.sofa.sensor.AccelerometerSensor;
import com.fbessou.sofa.sensor.Analog2DSensor;
import com.fbessou.sofa.sensor.KeySensor;
import com.fbessou.sofa.sensor.Sensor;
import com.fbessou.sofa.view.JoystickView;

public class GamePadSampleActivity extends Activity {
	GamePadIOHelper easyIO;
	GamePadInformation infos;
	
	public static final int UP = 1, LEFT = 2, RIGHT = 3, DOWN = 4;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gamepad_sample);

		infos = new GamePadInformation(this);
		easyIO = new GamePadIOHelper(this, infos);
		
		easyIO.start(null);
		
		Analog2DSensor stick = new Analog2DSensor(Sensor.ANALOG_CATEGORY_VALUE + 1, (JoystickView) findViewById(R.id.joystickView));
		easyIO.attachSensor(stick);
		
		KeySensor keyUp = new KeySensor(Sensor.KEY_CATEGORY_VALUE + UP, findViewById(R.id.buttonUp));
		easyIO.attachSensor(keyUp);
		
		KeySensor keyLeft = new KeySensor(Sensor.KEY_CATEGORY_VALUE + LEFT, findViewById(R.id.buttonLeft));
		easyIO.attachSensor(keyLeft);
		
		KeySensor keyRight = new KeySensor(Sensor.KEY_CATEGORY_VALUE + RIGHT, findViewById(R.id.buttonRight));
		easyIO.attachSensor(keyRight);
		
		KeySensor keyDown = new KeySensor(Sensor.KEY_CATEGORY_VALUE + DOWN, findViewById(R.id.buttonDown));
		easyIO.attachSensor(keyDown);
		
		AccelerometerSensor accel = new AccelerometerSensor(Sensor.WORLD_CATEGORY_VALUE+1);
		easyIO.attachSensor(accel);
		
		TextIndicator text = new TextIndicator(Indicator.TEXT_CATEGORY_VALUE + 1, (TextView)findViewById(R.id.textOutput), WriteMode.REPLACE);
		easyIO.attachIndicator(text);
		
		FeedbackIndicator feedback = new FeedbackIndicator(this, Indicator.FEEDBACK_CATEGORY_VALUE + 1);
		easyIO.attachIndicator(feedback);
		// easyIO.updateInformation(info);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Add an option to change the user's nickname in the menu
		menu.add(42, 42, 0, "Change nickname");
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if(item.getItemId() == 42)
			changeNamePrompt();
		return super.onMenuItemSelected(featureId, item);
	}
	
	private void changeNamePrompt() {
		// This edit text is the content of the dialog
		final EditText edit = new EditText(this);
		edit.setText(infos.getNickname());
		
		// Build a dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Change your nickname");
		builder.setView(edit);
		builder.setPositiveButton("Change", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				infos.setNickname(edit.getText().toString());
				easyIO.updateInformation(infos);
			}
		});
		builder.setNegativeButton("Cancel", null);
		
		// Display the dialog and let the user enter his new nickname
		builder.show();
	}
}
