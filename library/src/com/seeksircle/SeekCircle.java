package com.seeksircle;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ProgressBar;

public class SeekCircle extends ProgressCircle
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
	
	private boolean mTrackingTouch = false;
	
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
			updateTouchProgress(progress);
			
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
	
	// TODO Make this easier to override without having to re-implement it.
	
	@Override
	protected boolean updateProgress(int progress)
	{
		boolean result = super.updateProgress(progress);
		if (result)
		{
			if (mOnSeekCircleChangeListener != null)
				mOnSeekCircleChangeListener.onProgressChanged(this, progress, true);
		}
		
		return result;
	}
	
	private void updateTouchProgress(int progress)
	{
		boolean result = updateProgress(progress);
		if (result)
		{
			if (mOnSeekCircleChangeListener != null)
				mOnSeekCircleChangeListener.onProgressChanged(this, progress, true);
		}
	}
}
