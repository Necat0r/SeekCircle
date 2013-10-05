package com.seeksircle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;

public class SeekCircle extends View
{
	/**
	 * A callback that notifies clients when the progress level has been
	 * changed. This includes changes that were initiated by the user through a
	 * touch gesture or arrow key/trackball as well as changes that were
	 * initiated programmatically.
	 */
	public interface OnSeekCircleChangeListener
	{
		
		/**
		 * Notification that the progress level has changed. Clients can use the
		 * fromUser parameter to distinguish user-initiated changes from those
		 * that occurred programmatically.
		 * 
		 * @param seekBar
		 *            The SeekBar whose progress has changed
		 * @param progress
		 *            The current progress level. This will be in the range
		 *            0..max where max was set by
		 *            {@link ProgressBar#setMax(int)}. (The default value for
		 *            max is 100.)
		 * @param fromUser
		 *            True if the progress change was initiated by the user.
		 */
		void onProgressChanged(SeekCircle seekCircle, int progress, boolean fromUser);
		
		/**
		 * Notification that the user has started a touch gesture. Clients may
		 * want to use this to disable advancing the seek circle.
		 * 
		 * @param seekBar
		 *            The SeekBar in which the touch gesture began
		 */
		void onStartTrackingTouch(SeekCircle seekCircle);
		
		/**
		 * Notification that the user has finished a touch gesture. Clients may
		 * want to use this to re-enable advancing the seek circle.
		 * 
		 * @param seekCircle
		 *            The SeekCircle in which the touch gesture began
		 */
		void onStopTrackingTouch(SeekCircle seekCircle);
	}
	
	private OnSeekCircleChangeListener mOnSeekCircleChangeListener;
	
	/**
	 * Sets a listener to receive notifications of changes to the SeekCircle's
	 * progress level. Also provides notifications of when the user starts and
	 * stops a touch gesture within the SeekCircle.
	 * 
	 * @param listener
	 *            The seek circle notification listener
	 * 
	 * @see SeekCircle.OnSeekCircleChangeListener
	 */
	public void setOnSeekCircleChangeListener(OnSeekCircleChangeListener listener)
	{
		mOnSeekCircleChangeListener = listener;
	}
	
	private float mRingBias = 0.10f;
	private float mSectionRatio = 4.0f;
	private RectF mSectionRect = new RectF();
	private float mSectionHeight;
	
	private float mRadius;
	
	private int mMaxProgress = 100;
	private int mProgress = 0;
	
	//private float mRevolutions = 0.0f;
	
	private float mCenterX;
	private float mCenterY;
	
	private Paint mPaint;
	private int mColor1;
	private int mColor2;
	private int mInactiveColor;
	
	private boolean mTrackingTouch = false;
	
	{
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.FILL);
		
		mColor1 = Color.parseColor("#ff33b5e5");
		mColor2 = Color.parseColor("#ffff5900");
		mInactiveColor = Color.parseColor("#ff404040");
		
		mPaint.setColor(mColor1); // Set default
	}
	
	private float interpolate(float a, float b, float proportion)
	{
		return (a + ((b - a) * proportion));
	}
	
	/** Returns an interpolated color, between <code>a</code> and <code>b</code> */
	private int interpolateColor(int a, int b, float proportion)
	{
		float[] hsva = new float[3];
		float[] hsvb = new float[3];
		Color.colorToHSV(a, hsva);
		Color.colorToHSV(b, hsvb);
		for (int i = 0; i < 3; i++)
		{
			hsvb[i] = interpolate(hsva[i], hsvb[i], proportion);
		}
		return Color.HSVToColor(hsvb);
	}
	
	public SeekCircle(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}
	
	public SeekCircle(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	
	public SeekCircle(Context context)
	{
		super(context);
	}
	
	private void updateDimensions(int width, int height)
	{
		// Update center position
		mCenterX = width / 2.0f;
		mCenterY = height / 2.0f;
		
		// Find shortest dimension
		int diameter = Math.min(width, height);
		
		float outerRadius = diameter / 2;
		float sectionHeight = outerRadius * mRingBias;
		float sectionWidth = sectionHeight / mSectionRatio;
		
		mRadius = outerRadius - sectionHeight / 2;
		mSectionRect.set(-sectionWidth / 2, -sectionHeight / 2, sectionWidth / 2, sectionHeight / 2);
		mSectionHeight = sectionHeight;
	}
	
	@Override
	protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		updateDimensions(getWidth(), getHeight());
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		
		updateDimensions(w, h);
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		// Center our canvas
		canvas.translate(mCenterX, mCenterY);
		
		float rotation = 360.0f / (float) mMaxProgress;
		for (int i = 0; i < mMaxProgress; ++i)
		{
			canvas.save();
			
			canvas.rotate((float) i * rotation);
			canvas.translate(0, -mRadius);
			
			if (i < mProgress)
			{
				float bias = (float) i / (float) (mMaxProgress - 1);
				int color = interpolateColor(mColor1, mColor2, bias);
				mPaint.setColor(color);
			}
			else
			{
				canvas.scale(0.7f, 0.7f);
				mPaint.setColor(mInactiveColor);
			}
			
			canvas.drawRect(mSectionRect, mPaint);
			canvas.restore();
		}
		
		super.onDraw(canvas);
	}
	
	public boolean onTouchEvent(MotionEvent event)
	{
		float x = event.getX();
		float y = event.getY();
		
		float dx = mCenterX - x;
		float dy = y - mCenterY;
		
		float distance = (float) Math.sqrt(dx * dx + dy * dy);
		
		boolean inRange = Math.abs(distance - mRadius) <= mSectionHeight;
		boolean inDeadZone = false; // distance <= mRadius * 0.2f; // 20%
									// deadzone to avoid some quick flips
		
		boolean updateProgress = false;
		
		switch (event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				if (inRange)
				{
					mTrackingTouch = true;
					updateProgress = true;
				}
				
				break;
			
			case MotionEvent.ACTION_MOVE:
				if (mTrackingTouch && !inDeadZone)
					updateProgress = true;
				break;
			
			case MotionEvent.ACTION_UP:
				if (mTrackingTouch && !inDeadZone)
					updateProgress = true;
				mTrackingTouch = false;
				break;
			
			case MotionEvent.ACTION_CANCEL:
				mTrackingTouch = false;
				break;
		}
		
		if (updateProgress)
		{
			float bias = (float) ((Math.atan2(dx, dy) + Math.PI) / (Math.PI * 2.0));
			int progress = Math.round(bias * ((float) mMaxProgress));
			
			// // Bypass clamping if it's a down event
			// if (event.getAction() == MotionEvent.ACTION_DOWN)
			updateProgress(progress, true);
			
			// Avoid flipping at the top.
			// else if ((Math.abs(progress - mProgress) < mMaxProgress/2) &&
			// notAlreadyTrackingRevolutions)
			// {
			//
			//
			// updateProgress(progress, true);
			//
			// }
			
			// TODO Clamp to extremes at the top...
			// TODO Bypass clamping if it's the down event.
			// TODO Keep track on expected rotation position (like it's whinded
			// up and clamped at the top). Thus having to go back to reduce down
			// again.
			
			return true;
		}
		
		return super.onTouchEvent(event);
	}
	
	/**
	 * Get max progress
	 * 
	 * @return Max progress
	 */
	public float getMax()
	{
		return mMaxProgress;
	}
	
	/**
	 * Set max progress
	 * 
	 * @param max
	 */
	public void setMax(int max)
	{
		int newMax = Math.max(max, 1);
		if (newMax != mMaxProgress)
			mMaxProgress = newMax;
		
		updateProgress(mProgress, false);
		invalidate();
	}
	
	/**
	 * Get Progress
	 * 
	 * @return progress
	 */
	public int getProgress()
	{
		return mProgress;
	}
	
	/**
	 * Set progress
	 * 
	 * @param progress
	 */
	public void setProgress(int progress)
	{
		updateProgress(progress, false);
	}
	
	private void updateProgress(int progress, boolean fromUser)
	{
		// Clamp progress
		progress = Math.max(0, Math.min(mMaxProgress, progress));
		
		if (progress != mProgress)
		{
			mProgress = progress;
			
			if (mOnSeekCircleChangeListener != null)
				mOnSeekCircleChangeListener.onProgressChanged(this, progress, true);
			
			invalidate();
		}
	}
}
