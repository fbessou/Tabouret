package com.fbessou.tabouret;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.test.PerformanceTestCase;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Activity Displaying the list of GamePad installed on this device.
 * 
 * @author Frank Bessou
 *
 */
public class GamePadListActivity extends Activity implements OnTouchListener {

	private TableLayout mList;
	Configuration mConf;

	/**
	 * Allow detection of click
	 */
	boolean click = true;
	float touchPos[] = { 0, 0 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mConf = new Configuration(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_pad_list);
		mList = (TableLayout) findViewById(R.id.gamepad_list);
		mList.setBackgroundColor(Color.DKGRAY);
		refreshList();
	}

	/**
	 * Refresh the list of GamePads available
	 */
	void refreshList() {
		GamePadLoader loader = new GamePadLoader(this, null);

		// Get all files in the layout directory
		File path = new File(mConf.getLayoutDirectory());
		Log.i("Test", path.getAbsolutePath());
		File files[] = path.listFiles();

		// For each file, retrieve information
		for (File file : files) {

			try {
				GamePadInformation info = loader.parseInfoFromReader(new FileReader(file));
				TableRow row = new TableRow(this);

				ImageView icon = new ImageView(this);

				boolean imageNF = false;
				if (info.icon != null) {
					icon.setImageResource(R.drawable.controller_default);
				} else {
					// icon.setImageBitmap(BitmapFactory.decodeFile(mConf.getResourcesDirectory().));
					icon.setImageResource(R.drawable.controller_default);
				}

				row.addView(icon);

				// Name
				String mainContent = "<big>" + info.name + "</big> <i>v" + info.getVersion()
						+ "</i><br><i>" + info.description + "</i><br>";

				if (info.url != null)
					mainContent += "<a href=\"" + info.url + "\">" + info.url + "</a><br>";

				TextView tv = new TextView(this);
				tv.setText(Html.fromHtml(mainContent));
				tv.setClickable(true);
				tv.setMovementMethod(LinkMovementMethod.getInstance());
				tv.setFocusable(true);
				row.addView(tv);

				/**
				 * // Authors if (info.authors != null) { tv = new
				 * TextView(this); tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,
				 * 20); tv.setText("<i>" + info.authors + "</i>\n");
				 * row.addView(tv); }
				 **/

				// Row style
				row.setOnTouchListener(this);
				mList.addView(row);

				View ruler = new View(this);
				ruler.setBackgroundColor(getResources()
						.getColor(android.R.color.primary_text_light));
				// row = new Line(this);
				// row.addView(ruler,new
				// TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,2));
				mList.addView(ruler, new TableLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, 2));

			} catch (FileNotFoundException e) {
				e.printStackTrace();
				continue;
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View,
	 * android.view.MotionEvent)
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			v.setBackgroundColor(Color.GRAY);
			touchPos[0] = event.getX();
			touchPos[1] = event.getY();
			click=true;
			break;
		case MotionEvent.ACTION_UP:
			if(click){
				v.performClick();
			}
		case MotionEvent.ACTION_CANCEL:
			v.setBackgroundColor(Color.TRANSPARENT);
			break;
		case MotionEvent.ACTION_MOVE:
			if (click != false) {
				float dx = touchPos[0] - event.getX();
				float dy = touchPos[1] - event.getY();
				float dist = dx * dx + dy * dy;
				if (dist > 10)
					click = false;
			}
		default:
			break;
		}
		return true;
	}
}
