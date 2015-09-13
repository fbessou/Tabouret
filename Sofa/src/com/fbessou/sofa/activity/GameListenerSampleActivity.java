package com.fbessou.sofa.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.fbessou.sofa.GameIOHelper;
import com.fbessou.sofa.GameIOHelper.GamePadInputEvent;
import com.fbessou.sofa.GameIOHelper.GamePadStateChangedEvent;
import com.fbessou.sofa.GameIOHelper.InputEventListener;
import com.fbessou.sofa.GameIOHelper.StateChangedEventListener;
import com.fbessou.sofa.GameInformation;
import com.fbessou.sofa.InputEvent;
import com.fbessou.sofa.OutputEvent;
import com.fbessou.sofa.R;
import com.fbessou.sofa.sensor.Sensor;

public class GameListenerSampleActivity extends Activity implements StateChangedEventListener, InputEventListener {
	GameIOHelper easyIO;
	GameInformation info;
	
	RadioGroup list;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_listener_sample);
		
		// Initialize the list
		list = (RadioGroup) findViewById(R.id.radioGroup1);
		// Let each id of the list's views equal to each game pad id and -1 for broadcast
		for(int i = 0; i < list.getChildCount(); i++)
			list.getChildAt(i).setId(i-1);
		
		// Button to send feedback
		findViewById(R.id.send_feedback).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Create a feedback event
				OutputEvent feedbackEvent = new OutputEvent(OutputEvent.Type.FEEDBACK);
				// And send it to the game pad selected in the list
				easyIO.sendOutputEvent(feedbackEvent, list.getCheckedRadioButtonId());
			}
		});
		// Text to send
		((EditText)findViewById(R.id.textOutput)).setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_SEND) {
					// Create a text event
					OutputEvent textEvent = new OutputEvent(OutputEvent.Type.TEXT);
					// And send it to the game pad selected in the list
					easyIO.sendOutputEvent(textEvent, list.getCheckedRadioButtonId());
				}
				return false;
			}
		});
		
		easyIO = new GameIOHelper(this, this);
		info = new GameInformation("Name Of This Sample Game");
		easyIO.start(this, info);
	}

	@Override
	public void onInputEvent(GamePadInputEvent event) {
		// Display the event as a text in the corresponding item in the list
		String description = "";
		
		// Id of the game pad
		description += event.gamePadId;
		description += " - ";
		// Name of the player
		description += easyIO.getGamePadInformationId(event.gamePadId).staticInformations.getNickname();
		description += " - ";
		
		// Event description
		InputEvent ie = event.event;
		description += ie.eventType + " ";
		
		switch(ie.eventType) {
		case FLOATDOWN:
		case FLOATMOVE:
		case FLOATUP:
			// Event triggered by the joystick
			if(ie.padId == Sensor.ANALOG_CATEGORY_VALUE + 1) {
				description += "joystick ";
				if(ie.values.length > 0)
					description += ie.values[0] + " ";
				if(ie.values.length > 1)
					description += ie.values[1] + " ";
				if(ie.values.length > 2)
					description += ie.values[2] + " ";
			}
			break;
		case KEYDOWN:
		case KEYUP:
			// Event triggered by a 
			switch(ie.padId) {
			case Sensor.KEY_CATEGORY_VALUE + GamePadSampleActivity.UP:
				description += "button up ";
				break;
			case Sensor.KEY_CATEGORY_VALUE + GamePadSampleActivity.DOWN:
				description += "button down ";
				break;
			case Sensor.KEY_CATEGORY_VALUE + GamePadSampleActivity.LEFT:
				description += "button left ";
				break;
			case Sensor.KEY_CATEGORY_VALUE + GamePadSampleActivity.RIGHT:
				description += "button right ";
				break;
			}
			break;
		case TEXT:
			break;
		default:
			break;
		}
		
		// Display the description
		((RadioButton)list.getChildAt(event.gamePadId+1)).setText(description);
	}

	@Override
	public void onPadEvent(GamePadStateChangedEvent event) {
		// Display the event as a text in the corresponding item in the list
		String description = "";
		
		// Id of the game pad
		description += event.gamePadId;
		description += " - ";
		// Name of the player
		description += easyIO.getGamePadInformationId(event.gamePadId).staticInformations.getNickname();
		description += " - ";
		
		switch(event.eventType) {
		case INFORMATION:
			description += "Renamed";
			break;
		case JOINED:
			description += "Joined";
			break;
		case LEFT:
			description += "Disconnected";
			break;
		case UNEXPECTEDLY_DISCONNECTED:
			description += "Lost";
			break;
		default:
			break;
		}
		
		// Display the description
		RadioButton rb = ((RadioButton)list.getChildAt(event.gamePadId+1));
		if(rb.getVisibility() != View.VISIBLE)
			rb.setVisibility(View.VISIBLE);
		rb.setText(description);
	}
}
