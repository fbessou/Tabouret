package com.fbessou.sofa.indicator;

import android.widget.TextView;

import com.fbessou.sofa.OutputEvent;

public class TextIndicator extends Indicator {
	public enum WriteMode{REPLACE, APPEND};
	
	/** Last string received **/
	private String mText;
	private TextView mAttachedTextView;
	private WriteMode mWriteMode;
	
	public TextIndicator() {
		super();
	}
	public TextIndicator(int id) {
		super(id);
	}
	
	/** Attaches this indicator to a text view. Each output text will be display in this view.
	 * NB: A button is also a TextView ;) **/
	public void attachTo(TextView textView, WriteMode mode) {
		mAttachedTextView = textView;
		mWriteMode = mode;
		
		if(mText != null)
			write(mText);
	}
	
	@Override
	public void onOutputEventReceived(OutputEvent event) {
		mText = event.text;
		
		// Write the text
		if(mText != null)
			write(mText);
	}
	
	/** Displays the string in the text view **/
	private void write(String s) {
		final CharSequence seq;
		// Append the text at the end
		if(mWriteMode == WriteMode.APPEND) {
			StringBuilder sb = new StringBuilder(mAttachedTextView.getText());
			sb.append(s);
			seq = sb;
		}
		// Replace the text with the new string
		else if(mWriteMode == WriteMode.REPLACE) {
			seq = s;
		}
		else {
			seq = null;
		}
		
		// Set the text of the view on the UI thread
		if(seq != null) {
			mUIHandler.post(new Runnable() {
				@Override
				public void run() {
					mAttachedTextView.setText(seq);
				}
			});
		}
	}
}
