package com.fbessou.sofa.activity;

import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.fbessou.sofa.GamePadIOHelper;
import com.fbessou.sofa.GamePadInformation;
import com.fbessou.sofa.R;
import com.fbessou.sofa.indicator.Indicator;
import com.fbessou.sofa.indicator.TextIndicator;
import com.fbessou.sofa.indicator.TextIndicator.WriteMode;
import com.fbessou.sofa.sensor.Analog2DSensor;
import com.fbessou.sofa.sensor.KeySensor;
import com.fbessou.sofa.sensor.Sensor;
import com.fbessou.sofa.view.JoystickView;

public class GamePadActivity extends Activity {
	GamePadIOHelper easyIO;
	
	private static final int UP = 1, LEFT = 2, RIGHT = 3, DOWN = 4;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gamepad_sample);
		
		easyIO = new GamePadIOHelper();
		
		UUID uuid = getUUIDFromPreferences();
		easyIO.start(this, new GamePadInformation("Game pad's name", uuid));
		
		Analog2DSensor stick = new Analog2DSensor(Sensor.ANALOG_CATEGORY_VALUE + 1);
		stick.attachTo((JoystickView) findViewById(R.id.joystickView));
		easyIO.attachSensor(stick);
		
		KeySensor keyUp = new KeySensor(false, Sensor.KEY_CATEGORY_VALUE + UP);
		keyUp.attachTo(findViewById(R.id.buttonUp));
		easyIO.attachSensor(keyUp);
		
		KeySensor keyLeft = new KeySensor(false, Sensor.KEY_CATEGORY_VALUE + LEFT);
		keyLeft.attachTo(findViewById(R.id.buttonLeft));
		easyIO.attachSensor(keyLeft);
		
		KeySensor keyRight = new KeySensor(false, Sensor.KEY_CATEGORY_VALUE + RIGHT);
		keyRight.attachTo(findViewById(R.id.buttonRight));
		easyIO.attachSensor(keyRight);
		
		KeySensor keyDown = new KeySensor(false, Sensor.KEY_CATEGORY_VALUE + DOWN);
		keyDown.attachTo(findViewById(R.id.buttonDown));
		easyIO.attachSensor(keyDown);
		
		TextIndicator text = new TextIndicator(Indicator.TEXT_CATEGORY_VALUE + 1);
		text.attachTo((TextView)findViewById(R.id.textOutput), WriteMode.REPLACE);
		easyIO.attachIndicator(text);
		
		// easyIO.updateInformation(info);
	}

	private UUID getUUIDFromPreferences() {
		SharedPreferences prefs = getSharedPreferences("UUID", Context.MODE_PRIVATE);
		String s = prefs.getString("UUID", null);
		try {
			return UUID.fromString(s);
		} catch(Exception e) {
			// UUID not found in prefs
			UUID uuid = UUID.randomUUID();
			prefs.edit().putString("UUID", uuid.toString()).commit();
			return uuid;
		}
	}
}
