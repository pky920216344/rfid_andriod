package com.sample.demo;

import android.annotation.TargetApi;
import android.app.Presentation;
import android.content.Context;
import android.os.Build;
import android.view.Display;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class PresentationA extends Presentation {
	private TextView a;

	public PresentationA(Context outerContext, Display display) {
		super(outerContext, display);
		setContentView(R.layout.presentation_a);
		a = (TextView) findViewById(R.id.a);
	}
	
	public void aShow() {
		show();
		a.setText("我是副屏内容");
	}
}
