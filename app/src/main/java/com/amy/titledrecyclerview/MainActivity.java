package com.amy.titledrecyclerview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startSwipeItemRecyclerView(null);
        //startSwipeItemDisplay(null);
    }

    public void startSwipeItemDisplay(View view) {
        startActivity(new Intent(this, SwipeItemDisplayActivity.class));
    }

    public void startSwipeItemRecyclerView(View view){
        startActivity(new Intent(this,SwipeItemRecyclerViewActivity.class));
    }
}
