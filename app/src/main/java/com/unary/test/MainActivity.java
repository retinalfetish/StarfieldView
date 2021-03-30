package com.unary.test;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.unary.starfieldview.StarfieldView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StarfieldView starfield = findViewById(R.id.starfield);

        // Engage warp drive
        //starfield.setStarSpeed(-4);
        //starfield.postDelayed(new Runnable() {
        //    @Override
        //    public void run() {
        //        starfield.setStarSpeed(8);
        //        starfield.setStarAlpha(0.25f);
        //        starfield.requestLayout();
        //    }
        //}, 4000);
    }
}