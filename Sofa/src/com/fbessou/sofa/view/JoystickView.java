package com.fbessou.sofa.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.fbessou.sofa.R;

/**
 * 
 * @author Proïd
 *
 */
public class JoystickView extends View {
	/** Bitmap image of the stick and the center; width must be equal to height **/
	private Bitmap mStickBmp, mCenterBmp;
	/** if bitmap not set, the color is used instead **/
	private int mStickColor, mCenterColor;
	/** Parameters of the bound that define the constraints of the relative stick position **/
	private BoundShape mBoundShape = BoundShape.CIRCLE;
	private int mBoundRadius = 150;
	/** Radius of the stick; Automatically set when setStickImage() is called **/
	private int mStickRadius = 60;
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
	

	public JoystickView(Context context) {
		super(context);
		setStickImage(null);
	}
	public JoystickView(Context context, AttributeSet attrs) {
		super(context, attrs);
		loadAttributes(attrs, 0);
	}
	public JoystickView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		loadAttributes(attrs, defStyleAttr);
	}
	
	private void loadAttributes(AttributeSet attrs, int defStyleAttr) {
		TypedArray a = getContext().getTheme().obtainStyledAttributes( attrs, R.styleable.Joystick, defStyleAttr, 0);
		try {
			mBoundShape = BoundShape.values()[a.getInt(R.styleable.Joystick_bound, BoundShape.CIRCLE.ordinal())];
			mBoundRadius = a.getDimensionPixelSize(R.styleable.Joystick_boundSize, 150);
			mStickRadius = a.getDimensionPixelSize(R.styleable.Joystick_stickRadius, 60);
			mCenterPosition = Position.values()[a.getInt(R.styleable.Joystick_position, Position.FOLLOW.ordinal())];
			mStickColor = 0xFF808080;
			mCenterColor = 0xFF808080;
			
			int resId;
			
			resId = a.getResourceId(R.styleable.Joystick_stickDrawable, 0);
			if(resId != 0) {
				Drawable drawable = a.getDrawable(R.styleable.Joystick_stickDrawable);
				if(drawable instanceof ColorDrawable) {
					mStickColor = ((ColorDrawable)drawable).getColor(); // Should get ResourceTypeName and compare to "color" instead of that
					setStickImage(null);
				}
				else {
					setStickImage(drawableToBitmap(drawable));
				}
			} else {
				mStickColor = a.getColor(R.styleable.Joystick_stickDrawable, mStickColor);
			}
			
			resId = a.getResourceId(R.styleable.Joystick_centerDrawable, 0);
			if(resId != 0) {
				Drawable drawable = a.getDrawable(R.styleable.Joystick_centerDrawable);
				if(drawable instanceof ColorDrawable) {
					mCenterColor = ((ColorDrawable)drawable).getColor(); // Should get ResourceTypeName and compare to "color" instead of that
					setCenterImage(null);
				}
				else {
					setCenterImage(drawableToBitmap(drawable));
				}
			} else {
				mCenterColor = a.getColor(R.styleable.Joystick_stickDrawable, mCenterColor);
			}
		} finally {
			a.recycle();
		}
	}

	/** Convert a drawable to a bitmap (http://stackoverflow.com/a/10600736) */
	public static Bitmap drawableToBitmap(Drawable drawable) {
		if(drawable == null)
			return null;
	    Bitmap bitmap = null;
	
	    if (drawable instanceof BitmapDrawable) {
	        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
	        if(bitmapDrawable.getBitmap() != null) {
	            return bitmapDrawable.getBitmap();
	        }
	    }
	
	    if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
	    	return null;
	        //bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
	    } else {
	        bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
	    }
	
	    Canvas canvas = new Canvas(bitmap);
	    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
	    drawable.draw(canvas);
	    return bitmap;
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

		// Ask for an update of the view
		invalidate();
		
		return true;
	}

	/** Paint used to draw **/
	private final Paint mPaint = new Paint();
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		mPaint.setAntiAlias(true);
		
		// Fill the background with a transparent color
		canvas.drawARGB(0, 0, 0, 0);

		canvas.save();
		canvas.translate(mCenterPos[0], mCenterPos[1]);

		// Draw the center of the joystick; If there is no bitmap defined, draw a circle
		if(mCenterBmp != null)
			canvas.drawBitmap(mCenterBmp, -mCenterBmp.getWidth()/2, -mCenterBmp.getHeight()/2, null);
		else {
			mPaint.setColor(mCenterColor);
			canvas.drawCircle(0,0, mStickRadius / 4, mPaint);
		}

		// Draw the stick; If there is no bitmap defined, draw a circle
		if(mStickBmp != null)
			canvas.drawBitmap(mStickBmp, mStickRelPos[0]*mBoundRadius - mStickRadius, mStickRelPos[1]*mBoundRadius - mStickRadius, mPaint);
		else {
			mPaint.setColor(mStickColor);
			canvas.drawCircle(mStickRelPos[0]*mBoundRadius, mStickRelPos[1]*mBoundRadius, mStickRadius, mPaint);
		}
		
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
			mStickRadius = bitmap.getWidth()/2;
		}
		else {
			mStickBmp = null;
			mStickRadius = 60;
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
}
