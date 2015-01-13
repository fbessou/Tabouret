/**
 * 
 */
package com.fbessou.tabouret;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * @author Frank Bessou
 *
 */
public class GamePadListView extends TableLayout {

	private Configuration mConf;

	/**
	 * @param context
	 */
	public GamePadListView(Context context) {
		super(context);
		mConf = new Configuration(context);
		refresh();
	}

	public void refresh() {

		// Create a loader to parse the GamePads information
		GamePadLoader gploader = new GamePadLoader(getContext(), "NONE");
		File path = new File(mConf.getLayoutDirectory());
		Log.i("Test", path.getAbsolutePath());

		// Get all the files in the layout directory (and assume they are
		// directories)
		File dirs[] = path.listFiles();
		for (File dir : dirs) {

			// Search for layouts in all sub directories of the layout directory
			if (dir.isDirectory()) {
				Log.i("GamePadListView", "Scanning " + dir.getName() + " directory");
				// Find the file with the correct name ( ${dir_name}+".xml" )
				File files[] = dir.listFiles(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String filename) {
						Log.w("azaea", filename);
						if (filename.equals(dir.getName() + ".xml"))
							return true;
						return false;
					}
				});

				// Load the detected layout's information
				if (files.length > 0) {
					File f = files[0];
					GamePadInformation gpInfo;
					try {
						Log.i("GamePadListView", "Found xml file. Parsing...");
						gpInfo = gploader.parseInfoFromReader(new FileReader(f));
						GamePadListItem listItem = new GamePadListItem(getContext(), gpInfo, f);
						addView(listItem);
						
						View ruler = new View(getContext());
						ruler.setBackgroundColor(Color.parseColor("#101822"));
						addView(ruler, new TableLayout.LayoutParams(
								ViewGroup.LayoutParams.MATCH_PARENT, 4));
						ruler = new View(getContext());
						addView(ruler, new TableLayout.LayoutParams(
								ViewGroup.LayoutParams.MATCH_PARENT, 2));
						
					} catch (FileNotFoundException e) {
						Log.e("GamePadListView", "Can't read file " + f.getName());
					}

				}

			}
		}
	}

	/**
	 * 
	 * @author Frank Bessou An item of the GamePadListView table.
	 */
	class GamePadListItem extends TableRow implements OnTouchListener {
		/**
		 * Construct a row using
		 */
		private GamePadInformation mInfo;
		private File mFile;

		/**
		 * Create a list item describing a gamepad, and launching the
		 * corresponding gamepad on click
		 * 
		 * @param context
		 * @param info
		 *            contains authors, name, description of a GamePad
		 * @param file
		 *            is the XML file to parse to generate a layout
		 */
		public GamePadListItem(Context context, GamePadInformation info, File file) {
			super(context);
			mInfo = info;
			mFile = file;

			// Add a padding for a better look
			setPadding(10, 5, 10, 5);
			setBackgroundColor(Color.parseColor("#2d3647"));
			// Add the icon to the row
			ImageView icon = new ImageView(this.getContext());

			// look if the icon was specified by the XML
			if (info.icon != null) {
				// load the image
				Bitmap bmp = BitmapFactory.decodeFile(file.getParent() + "/" + info.icon);
				if (bmp != null) {
					icon.setImageBitmap(bmp);
				} else {
					// fall back on default image
					icon.setImageResource(R.drawable.controller_default);
				}
			} else {
				// use default image
				icon.setImageResource(R.drawable.controller_default);
			}
			this.addView(icon);

			// Add the text describing the gamepad to the row

			String mainContent = "<big>" + info.name + "</big> <i>v" + info.getVersion()
					+ "</i><br><i>" + info.description + "</i><br>";
			if (info.url != null)
				mainContent += "<a href=\"" + info.url + "\">" + info.url + "</a>";

			TextView tv = new TextView(this.getContext());
			tv.setText(Html.fromHtml(mainContent));

			// By expanding the description, the icon and the arrow are aligned
			// with borders
			TableRow.LayoutParams lp = new TableRow.LayoutParams(
					TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1.f);
			tv.setLayoutParams(lp);

			this.addView(tv);

			// Arrow to indicate the user can click
			ImageView arrow = new ImageView(context);
			arrow.setImageResource(R.drawable.arrow);
			this.addView(arrow);
			this.setOnTouchListener(this);

		}

		public File getFile() {
			return mFile;
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
				setBackgroundColor(Color.parseColor("#34495e"));
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				setBackgroundColor(Color.parseColor("#2d3647"));
				break;
			default:
				break;
			}
			return true;
		}

	}

}
