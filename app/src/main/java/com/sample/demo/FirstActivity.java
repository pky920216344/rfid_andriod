package com.sample.demo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;

import com.MyApplication;


public class FirstActivity extends Activity {
    private MyApplication application;
    private Display display;
    protected PresentationA myPresentationa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        application = MyApplication.getInstance();
        display = application.externDisplay;
        myPresentationa = showExternalA(this);

    }

    public PresentationA showExternalA(Context context) {
        if (display != null) {
            myPresentationa = new PresentationA(context,
                    display);
            myPresentationa.aShow();
            return myPresentationa;
        } else {
            return null;
        }
    }
}
