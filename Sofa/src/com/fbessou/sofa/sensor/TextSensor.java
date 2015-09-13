package com.fbessou.sofa.sensor;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.fbessou.sofa.InputEvent;
import com.fbessou.sofa.InputEvent.Type;

public class TextSensor extends Sensor {
	public TextSensor() {
		super();
	}
	
	public TextSensor(int id) {
		super(id);
	}

	/**
	 * Create a text sensor and attach it to an edit text view
	 * 
	 * @param textview Edit text to attach (an EditText is a TextView!)
	 * @param imeAction the IME action that triggers the event (see android.view.inputmethod.EditorInfo.IME_ACTION_...)
	 * @param clearEditTextOnAction True to clear the edit text content when the IME action is performed
	 */
	public TextSensor(TextView textview, int imeAction, boolean clearEditTextOnAction) {
		super();
		attachTo(textview, imeAction, clearEditTextOnAction);
	}

	/**
	 * Create a text sensor and attach it to an edit text view
	 * 
	 * @param id
	 * @param textview Edit text to attach (an EditText is a TextView!)
	 * @param imeAction the IME action that triggers the event (see android.view.inputmethod.EditorInfo.IME_ACTION_...)
	 * @param clearEditTextOnAction True to clear the edit text content when the IME action is performed
	 */
	public TextSensor(int id, TextView textview, int imeAction, boolean clearEditTextOnAction) {
		super(id);
		attachTo(textview, imeAction, clearEditTextOnAction);
	}
	
	/**
	 * Attaches a text view to this sensor. Each IME action performed corresponding to the given filter will
	 * trigger an event containing the text of the view. 
	 * @param edittext Edit text to attach
	 * @param imeAction the IME action that triggers the event (see android.view.inputmethod.EditorInfo.IME_ACTION_...)
	 * @param clearEditTextOnAction True to clear the edit text content when the IME action is performed
	 */
	public void attachTo(TextView textview, final int imeAction, final boolean clearEditTextOnAction) {
		textview.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if((actionId & imeAction) != EditorInfo.IME_ACTION_NONE || imeAction == EditorInfo.IME_ACTION_NONE) {
					// Create and trigger the text event
					InputEvent evt = new InputEvent(Type.TEXT);
					evt.text = v.getText().toString();
					
					// Clear the content if required
					if(clearEditTextOnAction)
						v.setText("");
					
					return true;
				}
				else {
					return false;
				}
			}
		});
	}
}
