package com.amy.titledrecyclerview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.amy.swipeitemlayout.SwipeItemLayout;

public class SwipeItemDisplayActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipeitemdisplay);
    }

    public void onSlide(View view) {
        SwipeItemLayout itemLayout = findViewById(R.id.swipeitemlast);
        itemLayout.slideAuto(false);
    }
}
