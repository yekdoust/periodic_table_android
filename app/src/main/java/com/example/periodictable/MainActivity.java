package com.example.periodictable;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        TextView v = new TextView(this);
        v.setBackgroundColor(Color.rgb(25, 35, 45));
        v.setTextColor(Color.WHITE);
        v.setGravity(Gravity.CENTER);
        v.setTextSize(28);
        v.setText(
            "PERIODIC TABLE ANDROID\n\n" +
            "VERSION 0.24.0\n" +
            "BUILD 24\n\n" +
            "TEST SCREEN\n\n" +
            "اگر این صفحه دیده می‌شود، برنامه صحیح اجرا شده است."
        );

        setContentView(v);
    }
}
