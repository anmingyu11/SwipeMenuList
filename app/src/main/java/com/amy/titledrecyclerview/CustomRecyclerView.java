package com.amy.titledrecyclerview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class CustomRecyclerView extends RecyclerView {
    public CustomRecyclerView(Context context) {
        this(context, null, 0);
    }

    public CustomRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private OnTouchListener mOnTouchListener;

    public void setTouchListener(OnTouchListener onTouchListener) {
        mOnTouchListener = onTouchListener;
    }

}
