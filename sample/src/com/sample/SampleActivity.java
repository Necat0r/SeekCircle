package com.sample;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.seekcircle.SeekCircle;
import com.seekcircle.sample.R;

public class SampleActivity extends Activity
{
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		SeekCircle seekCircle = (SeekCircle)findViewById(R.id.seekCircle);
		seekCircle.setOnSeekCircleChangeListener(new SeekCircle.OnSeekCircleChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekCircle seekCircle)
			{}
			
			@Override
			public void onStartTrackingTouch(SeekCircle seekCircle)
			{}
			
			@Override
			public void onProgressChanged(SeekCircle seekCircle, int progress, boolean fromUser)
			{
				updateText();
			}
		});

		updateText();
	}
	
	private void updateText()
	{
		SeekCircle seekCircle = (SeekCircle)findViewById(R.id.seekCircle);
		TextView textProgress = (TextView)findViewById(R.id.textProgress);

		if (textProgress != null && seekCircle != null)
		{
			int progress = seekCircle.getProgress();
			textProgress.setText(Integer.toString(progress) + "%");
		}
	}
}
