package com.fbessou.sofa.indicator;

import android.content.Context;
import android.os.Vibrator;

import com.fbessou.sofa.OutputEvent;

public class FeedbackIndicator extends Indicator {
	Vibrator mVibrator;
	
	public FeedbackIndicator(Context context) {
		super();
		mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
	}
	public FeedbackIndicator(Context context, int id) {
		super(id);
		mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
	}
	
	@Override
	public void onOutputEventReceived(OutputEvent event) {
		if(mVibrator != null && mVibrator.hasVibrator()) {
			// Cancel the last sequence
			mVibrator.cancel();
			// Play the sequence
			mVibrator.vibrate(event.vibrations, -1);
		}
	}
}
