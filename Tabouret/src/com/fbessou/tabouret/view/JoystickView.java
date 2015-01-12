package com.fbessou.tabouret.view;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * 
 * @author Pierre Guglielminotti-Ghermotti
 *
 */
public class JoystickView extends View {
	private Bitmap mStickBmp, mCenterBmp;
	private BoundShape mBoundShape = BoundShape.CIRCLE;
	private int mBoundRadius = 150;
	private int mUserWidth, mUserHeight;
	private int mStickRadius = 0;
	private Position mCenterPosition = Position.DYNAMIC;
	private OnPositionChangedListener mPosChangedListener;

	public JoystickView(Context context, int w, int h, Bitmap stickBmp) {
		super(context);
		// Sets the stick's bitmap and radius
		setStickImage(stickBmp);
		// / Sets the touchable surface's dimensions
		mUserWidth = w;
		mUserHeight = h;
		// / Sets layout margins with radius
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(w + mStickRadius * 2, h
				+ mStickRadius * 2);
		lp.setMargins(-mStickRadius, -mStickRadius, mStickRadius, mStickRadius);
		setCenterPosition(Position.FIXED);
		setLayoutParams(lp);
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
	private float mCenterPos[] = { 0, 0 };
	private float mStickRelPos[] = { 0, 0 };

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX(), y = event.getY();
		// / Check if this touch event is destinated for this koystick
		boolean isOut = false;
		if (mStickRadius > x) {
			y = y + (mCenterPos[1] - y) * (mStickRadius - x) / (mCenterPos[0] - x);
			x = mStickRadius;
			isOut = true;
		} else if (x > mUserWidth + mStickRadius) {
			y = y + (mCenterPos[1] - y) * (mUserWidth + mStickRadius - x) / (mCenterPos[0] - x);
			x = mUserWidth + mStickRadius;
			isOut = true;
		}
		if (mStickRadius > y) {
			x = x + (mCenterPos[0] - x) * (mStickRadius - y) / (mCenterPos[1] - y);
			y = mStickRadius;
			isOut = true;
		} else if (y > mUserHeight + mStickRadius) {
			x = x + (mCenterPos[0] - x) * (mUserHeight + mStickRadius - y) / (mCenterPos[1] - y);
			y = mUserHeight + mStickRadius;
			isOut = true;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (!isOut) {
				mIsTouched = true;
				if (mCenterPosition == Position.DYNAMIC || mCenterPosition == Position.FOLLOW) {
					mCenterPos[0] = x;
					mCenterPos[1] = y;
				}
			} else
				return false;
			break;
		case MotionEvent.ACTION_MOVE:
			if (!mIsTouched)
				break;
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
			if (mPosChangedListener != null)
				mPosChangedListener.positionChanged(this,mStickRelPos[0], -mStickRelPos[1]);
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if (!mIsTouched)
				break;
			mStickRelPos[0] = 0;
			mStickRelPos[1] = 0;
			if (mPosChangedListener != null)
				mPosChangedListener.positionChanged(this,mStickRelPos[0], -mStickRelPos[1]);
			mIsTouched = false;
			break;
		}
		invalidate();
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Log.i("###", "onDraw");
		Paint paint = new Paint();
		paint.setColor(0xff00ff00);
		canvas.drawARGB(0, 0, 0, 255);
		canvas.save();
		canvas.translate(mCenterPos[0], mCenterPos[1]);

		if (mIsTouched) {
			if (mCenterBmp != null)
				canvas.drawBitmap(mCenterBmp, -mCenterBmp.getWidth() / 2,
						-mCenterBmp.getHeight() / 2, null);
			else
				canvas.drawCircle(0, 0, 20, paint);
		} else
			paint.setAlpha(128);

		if (mStickBmp != null)
			canvas.drawBitmap(mStickBmp, mStickRelPos[0] * mBoundRadius - mStickRadius,
					mStickRelPos[1] * mBoundRadius - mStickRadius, paint);
		else
			canvas.drawCircle(mStickRelPos[0] * mBoundRadius, mStickRelPos[1] * mBoundRadius,
					mStickRadius, paint);

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

	public void setCenterPosition(Position pos) {
		mCenterPosition = pos;
		// /Reset the center coordinate
		mCenterPos[0] = mUserWidth / 2 + mStickRadius;
		mCenterPos[1] = mUserHeight / 2 + mStickRadius;
	}

	public enum BoundShape {
		CIRCLE, RECT
	};

	/** Sets the bound shape; By default : CIRCLE, 75 **/
	void setBoundShape(BoundShape boundShape, int radius) {
		mBoundShape = boundShape;
		mBoundRadius = radius;
	}

	/**
	 * Sets the stick image; width must be equal to height; if null, a simple
	 * circle will be displayed
	 **/
	private void setStickImage(Bitmap bitmap) {
		if (bitmap != null && bitmap.getWidth() == bitmap.getHeight()) {
			mStickBmp = bitmap;
			mStickRadius = bitmap.getWidth() / 2;
		} else {
			mStickBmp = null;
			mStickRadius = 60;
		}

	}

	/**
	 * Sets the image of the center of the joystick; If null, a simple point
	 * will be displayed
	 **/
	public void setCenterImage(Bitmap bitmap) {
		mStickBmp = bitmap;
	}

	/** Sets the listener called when the joystick's position changed **/
	public void setOnPositionChangedListener(OnPositionChangedListener listener) {
		mPosChangedListener = listener;
	}

	public interface OnPositionChangedListener {
		void positionChanged(JoystickView joystick,float px, float py);
	}

	/**
	 * 
	 * @author Frank Bessou
	 */
	static public JoystickView parseXML(Context context, String xml) throws XmlPullParserException{
		
		return new JoystickView(context,20,20,null);
	}
}
