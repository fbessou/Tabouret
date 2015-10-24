package com.fbessou.sofa.indicator;

import android.content.Context;
import android.os.Vibrator;

import com.fbessou.sofa.OutputEvent;

/**
 * Requires the caller to hold the permission android.Manifest.permission.VIBRATE.
 * @author Proïd
 *
 */
public class FeedbackIndicator extends Indicator {
	Vibrator mVibrator;
	
	public FeedbackIndicator(Context context) {
		super(FEEDBACK_ID);
		mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
	}
	
	@Override
	public void onOutputEventReceived(OutputEvent event) {
		if(mVibrator != null && mVibrator.hasVibrator()) {
			// Cancel the last sequence
			mVibrator.cancel();
			
			if(event.feedbackDuration > 0)
				mVibrator.vibrate(event.feedbackDuration);
			else
				mVibrator.vibrate(event.vibrations, -1);
		}
	}
}
