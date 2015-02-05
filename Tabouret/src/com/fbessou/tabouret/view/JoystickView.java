package com.fbessou.tabouret.view;

import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.fbessou.sofa.Analog2DSensor;
import com.fbessou.tabouret.NodeParser;

/**
 * 
 * @author Pierre Guglielminotti-Ghermotti
 *
 */
public class JoystickView extends View {
	/** Sensor associated with this joystick **/
	private final Analog2DSensor sensor=new Analog2DSensor();
	/** Bitmap image of the stick and the center; width must be equal to height **/
	private Bitmap mStickBmp, mCenterBmp;
	/** Parameters of the bound that define the constraints of the relative stick position **/
	private BoundShape mBoundShape = BoundShape.CIRCLE;
	private int mBoundRadius = 150;
	/** Radius of the stick; Automatically set when setStickImage() is called **/
	private int mStickRadius=60;
	/** Type of positioning of the joystick center (see enum Position) **/
	private Position mCenterPosition = Position.DYNAMIC;
	/** Listener used when the position of the stick is changed (see onTouchEvent()) **/
	private OnPositionChangedListener mPosChangedListener;
	/** Position (X,Y) of the center of the joystick **/
	private float mCenterPos[] = {0,0};
	/** Position (X,Y) of the stick relatively to the center; With -1.0f <= X,Y <= 1.0f **/ 
	private float mStickRelPos[] = {0,0};
	
	private int mStoredSize[] = {500,500};
	public JoystickView(Context context, int w, int h, Bitmap stickBmp) {
		super(context);
		// Set the stick's bitmap and radius
		setStickImage(stickBmp);
		

	}
	/* (non-Javadoc)
	 * @see android.view.View#onSizeChanged(int, int, int, int)
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mStoredSize[0]=w;
		mStoredSize[1]=h;
		centerStick();
	}
	/**
	 * 
	 */
	public JoystickView(Context context) {
		super(context);
		setStickImage(null);
	}

	/**
	 * Returns the X position of the joystick; returns 0 if the joystick is
	 * released
	 **/
	public float getJoystickX() {
		return mStickRelPos[0];
	}

	/**
	 * Returns the Y position of the joystick; returns 0 if the joystick is
	 * released
	 **/
	public float getJoystickY() {
		return mStickRelPos[1];
	}

	private boolean mIsTouched = false;
	@SuppressLint("ClickableViewAccessibility") // Doesn't matter! :P
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX(), y = event.getY();

		// Check if this touch event is inside the user-defined surface.
		// If it is not the case, project the touch position.
		boolean isOut = false;
		// Check for X axis
		if (mStickRadius > x) {
			y = y + (mCenterPos[1] - y) * (mStickRadius - x) / (mCenterPos[0] - x);
			x = mStickRadius;
			isOut = true;
		} else if (x > mStoredSize[0] - mStickRadius) {
			y = y + (mCenterPos[1] - y) * (mStoredSize[0] - mStickRadius - x) / (mCenterPos[0] - x);
			x = mStoredSize[0] - mStickRadius;
			isOut = true;
		}
		// Check for Y axis
		if (mStickRadius > y) {
			x = x + (mCenterPos[0] - x) * (mStickRadius - y) / (mCenterPos[1] - y);
			y = mStickRadius;
			isOut = true;
		} else if (y > mStoredSize[1] - mStickRadius) {
			x = x + (mCenterPos[0] - x) * (mStoredSize[1] - mStickRadius - y) / (mCenterPos[1] - y);
			y = mStoredSize[1]  - mStickRadius;
			isOut = true;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (!isOut) {
				Vibrator v = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
				v.vibrate(1);
				// Start moving the stick
				mIsTouched = true;
				if (mCenterPosition == Position.DYNAMIC || mCenterPosition == Position.FOLLOW) {
					mCenterPos[0] = x;
					mCenterPos[1] = y;
				}
			}
			else {
				// The event is not used by this view				return false;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (!mIsTouched)
				break;
			
			// Get the relative stick position and apply the constraint of the bound shape
			float dx = x - mCenterPos[0],
			dy = y - mCenterPos[1];
			switch (mBoundShape) {
			case CIRCLE:
				float d = (float) Math.hypot(dx, dy);
				if (d > 0) {
					// The center is too far of the stick -> it follows its
					// friend;
					if (d > mBoundRadius) {

						if (mCenterPosition == Position.FOLLOW) {
							float reduceDist = mBoundRadius / d;
							
							// move the center
							mCenterPos[0] = x * (1 - reduceDist) + mCenterPos[0] * reduceDist;// x+(c-x)*r
							mCenterPos[1] = y * (1 - reduceDist) + mCenterPos[1] * reduceDist;// y+(c-y)*r
						}
						dx /= d;
						dy /= d;
					} else {
						dx /= mBoundRadius;
						dy /= mBoundRadius;
					}
				}
				break;
			case RECT:
				if (Math.abs(dx) > mBoundRadius) {
					dx = (dx > 0 ? 1 : -1);
					// The center is too far of the stick -> it follows its
					// friend;
					if (mCenterPosition == Position.FOLLOW)
						mCenterPos[0] = -dx * mBoundRadius + x;
				} else {
					dx /= mBoundRadius;
				}
				if (Math.abs(dy) > mBoundRadius) {
					dy = (dy > 0 ? 1 : -1);
					// The center is too far of the stick -> it follows its
					// friend;
					if (mCenterPosition == Position.FOLLOW)
						mCenterPos[1] = -dy * mBoundRadius + y;
				} else {
					dy /= mBoundRadius;
				}
				break;
			}
			
			mStickRelPos[0] = dx;
			mStickRelPos[1] = dy;
			// If a listener is existing, call the positionChanged() method.
			if (mPosChangedListener != null)
				mPosChangedListener.positionChanged(this,mStickRelPos[0], -mStickRelPos[1]);
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if (!mIsTouched)
				break;
			// Clear the stick position
			mStickRelPos[0] = 0;
			mStickRelPos[1] = 0;
			// If a listener is existing, call the positionChanged() method
			if (mPosChangedListener != null){
				mPosChangedListener.positionChanged(this,mStickRelPos[0], -mStickRelPos[1]);
			}

			mIsTouched = false;
			break;
		}
		sensor.setValue(mStickRelPos[0],-mStickRelPos[1]);
		// Ask for an update of the view
		invalidate();
		
		return true;
	}

	/** Paint used to draw **/
	private final Paint mPaint = new Paint();
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		// Fill the background with a transparent color
		canvas.drawARGB(0, 0, 0, 0);

		// The default color is grey
		mPaint.setColor(0xff808080);
		
		canvas.save();
		canvas.translate(mCenterPos[0], mCenterPos[1]);

		if(mIsTouched) {
			// Draw the center of the joystick; If there is no bitmap defined, draw a circle
			if(mCenterBmp != null)
				canvas.drawBitmap(mCenterBmp, -mCenterBmp.getWidth()/2, -mCenterBmp.getHeight()/2, null);
			else
				canvas.drawCircle(0,0, 20, mPaint);
		}
		else {
			// Reduce opacity of the stick when it is released
			mPaint.setAlpha(125);
		}

		// Draw the stick; If there is no bitmap defined, draw a circle
		if(mStickBmp != null)
			canvas.drawBitmap(mStickBmp, mStickRelPos[0]*mBoundRadius - mStickRadius, mStickRelPos[1]*mBoundRadius - mStickRadius, mPaint);
		else
			canvas.drawCircle(mStickRelPos[0]*mBoundRadius, mStickRelPos[1]*mBoundRadius, mStickRadius, mPaint);
		
		canvas.restore();

	}

	public enum Position {
		/** The center is fixed and the user cannot move it **/
		FIXED,
		/**
		 * The center appears at the finger's position and does not move until
		 * the next touch
		 **/
		DYNAMIC,
		/**
		 * The center appears at the finger's position and follows the finger if
		 * it move too far away
		 **/
		FOLLOW
	};

	/** Sets the type of positioning of the center **/
	public void setCenterPosition(Position pos) {
		mCenterPosition = pos;
	}

	public enum BoundShape {
		CIRCLE, RECT
	};

	/** Sets the bound shape; By default : CIRCLE, 75 **/
	void setBoundShape(BoundShape boundShape, int radius) {
		mBoundShape = boundShape;
		mBoundRadius = radius;
	}

	/** Sets the stick image; width must be equal to height; if null, a simple circle will be displayed.
	 * Returns the radius of the stick corresponding to the butmap or a default value if bitmap is invalid or nulll **/
	private void setStickImage(Bitmap bitmap) {
		if (bitmap != null && bitmap.getWidth() == bitmap.getHeight()) {
			mStickBmp = bitmap;
			mStickRadius= bitmap.getWidth()/2;
		}
		else {
			mStickBmp = null;
			mStickRadius=60;
		}
		
		// Set layout margins with radius
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mStoredSize[0] + mStickRadius*2, mStoredSize[1] + mStickRadius*2);
		//lp.setMargins(-mStickRadius, -mStickRadius, -mStickRadius, -mStickRadius);
		setLayoutParams(lp);

	}

	/**
	 * Sets the image of the center of the joystick; If null, a simple point
	 * will be displayed
	 **/
	public void setCenterImage(Bitmap bitmap) {
		mStickBmp = bitmap;
	}
	
	/**
	 * Move the stick to the center of the view.
	 */
	protected void centerStick(){
		// Reset the center coordinate
		mCenterPos[0] = mStoredSize[0]/2;
		mCenterPos[1] = mStoredSize[1]/2;
	}
	/** Sets the listener called when the joystick's position changed **/
	public void setOnPositionChangedListener(OnPositionChangedListener listener) {
		mPosChangedListener = listener;
	}

	public interface OnPositionChangedListener {
		void positionChanged(JoystickView joystick,float px, float py);
	}
	
	public static class Parser extends Container.Parser{
		/**
		 * @param parentParser parser from which is called this one
		 */
		public Parser(NodeParser parentParser) {
			super("joystick",parentParser);
		}
		
		/* (non-Javadoc)
		 * @see com.fbessou.tabouret.view.Container.Parser#parse()
		 */
		@Override
		public View parse() {

			JoystickView joystick = new JoystickView(getContext());
			setView(joystick);
			try {
				parseAttributes();
				parseChildren();
				applyLayout();
			} catch (XmlPullParserException e ) {
				e.printStackTrace();
			}
			mGamePad.getGameBinder().addSensor(joystick.sensor);
			return joystick;
			
		}
		
		/* (non-Javadoc)
		 * @see com.fbessou.tabouret.NodeParser#parseAttribute(java.lang.String, java.lang.String)
		 */
		@Override
		protected void parseAttribute(String key, String val) {
			if(getView()!=null){
				switch (key.toLowerCase()) {
				case "position":
					switch (val.toLowerCase()) {
					case "fixed":
						((JoystickView)getView()).setCenterPosition(Position.FIXED);
						break;
					case "dynamic":
						((JoystickView)getView()).setCenterPosition(Position.DYNAMIC);
						break;
					case "follow":
						((JoystickView)getView()).setCenterPosition(Position.FOLLOW);
						break;
					default:
						Log.e("JoystickView.Parser","Unrecognised position value : \""+val+"\". Allowed values are \"fixed\", \"follow\" and \"dynamic\"");
						break;
					}
					break;
				case "stick-image":
					((JoystickView)getView()).setStickImage(BitmapFactory
								.decodeFile(mResourceDir + "/" + val));;
					break;
				/*case "margin-left":
				case "margin-right":
				case "margin-top":
				case "margin-bottom":
					break;*/
				default:
					super.parseAttribute(key, val);
					break;
				}
			}
		}
	}
}
