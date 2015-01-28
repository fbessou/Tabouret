/**
 * 
 */
package com.fbessou.tabouret.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;

import com.fbessou.tabouret.Configuration;
import com.fbessou.tabouret.GamePadInformation;
import com.fbessou.tabouret.GamePadLoader;
import com.fbessou.tabouret.GamePadActivity;
import com.fbessou.tabouret.R;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * @author Frank Bessou
 * A view containing a list of gamepad descriptions.
 * On click on an item, the clicked gamepad opens.
 */
public class GamePadListView extends TableLayout {
	/**
	 * Configuration of the program (resource diretories...)
	 */
	private Configuration mConf;

	/**
	 * @param context Used to instantiate this view
	 */
	public GamePadListView(Context context) {
		super(context);
		mConf = new Configuration(context);
		refresh();
	}
	/**
	 * List files again
	 */
	public void refresh() {

		// Create a loader to parse the GamePads information
		GamePadLoader gploader = new GamePadLoader("NONE");
		File path = new File(mConf.getLayoutDirectory());
		Log.i("Test", path.getAbsolutePath());

		// Get all the files in the layout directory (and assume they are
		// directories)
		File dirs[] = path.listFiles();
		for (File dir : dirs) {

			// Search for layouts in all sub directories of the layout directory
			if (dir.isDirectory()) {
				//Log.i("GamePadListView", "Scanning " + dir.getName() + " directory");
				// Find the file with the correct name ( ${dir_name}+".xml" )
				File files[] = dir.listFiles(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String filename) {
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
						Log.i("GamePadListView", "Found xml file"+f.getName()+". Parsing...");
						gpInfo = gploader.parseInfoFromReader(new FileReader(f));
						GamePadListItem listItem = new GamePadListItem(getContext(), gpInfo, f);
						addView(listItem);
						
						View ruler = new View(getContext());
						ruler.setBackgroundColor(Color.parseColor("#101822"));
						addView(ruler, new TableLayout.LayoutParams(
								ViewGroup.LayoutParams.MATCH_PARENT, 4));
						ruler = new View(getContext());
						addView(ruler, new TableLayout.LayoutParams(
								ViewGroup.LayoutParams.MATCH_PARENT, 15));
						
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
	class GamePadListItem extends TableRow implements OnTouchListener , OnClickListener {
		/**
		 * Construct a row using
		 */
		private GamePadInformation mInfo;
		private File mFile;
		private Drawable mIconDrawable;
		
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
			setBackgroundColor(Color.parseColor("#384356"));
			// Add the icon to the row
			ImageView icon = new ImageView(this.getContext());

			// look if the icon was specified by the XML
			if (info.icon != null) {
				// load the image
				Bitmap bmp = BitmapFactory.decodeFile(file.getParent() + "/" + info.icon);
				if (bmp != null) {
					icon.setImageBitmap(bmp);
					mIconDrawable = new BitmapDrawable(getResources(),bmp);
				} else {
					// fall back on default image
					icon.setImageResource(R.drawable.controller_default);

				}
			} else {
				// use default image
				icon.setImageResource(R.drawable.controller_default);
			}
			mIconDrawable = icon.getDrawable();
			
			TableRow.LayoutParams lp = new TableRow.LayoutParams();
			lp.gravity=Gravity.CENTER_VERTICAL;

			this.addView(icon,lp);

			// Add the text describing the gamepad to the row

			String mainContent = "<big><b>" + info.name + "</b></big> <i>v" + info.getVersion()
					+ "</i><br><i>" + info.description + "</i><br>";
			if (info.url != null)
				mainContent += "<a href=\"" + info.url + "\">" + info.url + "</a>";

			TextView tv = new TextView(this.getContext());
			tv.setText(Html.fromHtml(mainContent));

			// By expanding the description, the icon and the arrow are aligned
			// with borders
			TableRow.LayoutParams lpDescr = new TableRow.LayoutParams(
					TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.f);
			tv.setLayoutParams(lpDescr);

			this.addView(tv);

			// Arrow to indicate the user can click
			ImageView arrow = new ImageView(context);
			arrow.setImageResource(R.drawable.arrow);
			this.addView(arrow,lp);
			
			// Listeners
			this.setOnTouchListener(this);
			this.setOnClickListener(this);

		}

		public File getFile() {
			return mFile;
		}


		/**
		 * Handler for detecting 
		 */
		private final Handler mClickEventHandler = new Handler();
		
		/**
		 * When true, indicate a long click has been detected and
		 * the onTouch musn't detect detect ACTION_UP.
		 */
		private boolean mLongPressDetected = false;
		/**
		 * When true, indicate a long click has been detected and
		 * the onTouch musn't detect detect ACTION_UP.
		 */
		private boolean mCancelClick=false;

		/**
		 * Runnable called on a long click on an item.
		 * Build up an AlertDialog containing more information
		 */
		Runnable mLongPressDetector = new Runnable() {
		    public void run() {
		    	mLongPressDetected=true;
		       AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		       //TODO add link to website
		       //TODO add delete
		       //TODO add use
		       builder.setIcon(mIconDrawable)
		       		  .setTitle(mInfo.name);
		       AlertDialog dialog= builder.create();
		       dialog.setCanceledOnTouchOutside(true);
		       dialog.show();

		       //Add a haptic effect
		       ((Vibrator)getContext().getSystemService(Service.VIBRATOR_SERVICE)).vibrate(new long[]{0,80,50,50},-1);

		    }   
		};

		
		/**
		 * Wait a little before changing the background and launching vibration
		 * to avoid a too sensitive feeling
		 */
		Runnable clickNotifier = new Runnable() {
			@Override
			public void run() {
				if(!mLongPressDetected || !mCancelClick){
					setBackgroundColor(Color.parseColor("#18577c"));
					((Vibrator)getContext().getSystemService(Service.VIBRATOR_SERVICE)).vibrate(50);
				}
				
			}
		};
		
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
				mClickEventHandler.postDelayed(clickNotifier, 60);
				mClickEventHandler.postDelayed(mLongPressDetector, 1000);
				
				mCancelClick=false;
				break;
			case MotionEvent.ACTION_MOVE:
				mCancelClick=true;
				break;
			case MotionEvent.ACTION_CANCEL:
				mCancelClick=true;
				setBackgroundColor(Color.parseColor("#384356"));
				mClickEventHandler.removeCallbacks(mLongPressDetector);
				mClickEventHandler.removeCallbacks(clickNotifier);

				break;
			case MotionEvent.ACTION_UP:
				setBackgroundColor(Color.parseColor("#384356"));
				if(!mLongPressDetected || !mCancelClick){
					mClickEventHandler.removeCallbacks(clickNotifier);
					mClickEventHandler.removeCallbacks(mLongPressDetector);

					v.performClick();
				}
				else
					mLongPressDetected=false;
				break;
			default:
				break;
			}
			return true;
		}
		
		/* (non-Javadoc)
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getContext(),GamePadActivity.class);
			intent.putExtra("gamepad_path", getFile().getAbsolutePath());
			getContext().startActivity(intent);
		}
		
	}// class GamePadListItem

}// class GamePadListView
