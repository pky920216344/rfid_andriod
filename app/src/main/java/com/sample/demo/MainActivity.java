package com.sample.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;

import com.MyApplication;

import org.xutils.view.annotation.ContentView;


@ContentView(R.layout.activity_main)
public class MainActivity extends Activity {
    private Button mybtn;
    private MyApplication application;
    private Display display;
    protected PresentationA myPresentationa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        application = MyApplication.getInstance();
        display = application.externDisplay;
        myPresentationa = showExternalA(this);

        mybtn = (Button) findViewById(R.id.mybtn);
        mybtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,FirstActivity.class);
                startActivity(intent);
            }
        });
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
