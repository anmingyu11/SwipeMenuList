package com.amy.swipeitemlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class SwipeItemLayout extends FrameLayout {

    public enum SwipeDirection {
        Left, Right
    }

    public enum LayoutModel {
        PullOut, LayDown
    }

    public SwipeItemLayout(Context context) {
        super(context);
    }

    public SwipeItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // 顶部视图
    private View mTopView;
    // 底部视图
    private View mBottomView;
    // 允许拖动的距离【注意：最终允许拖动的距离是 (mDragRange + mSpringDistance)】
    private int mDragRange;


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (getChildCount() != 2) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + "必须有且只有两个子控件");
        }

        // 避免底部视图被隐藏时还能获取焦点被点击
        //Todo : mBottomView.setVisibility(INVISIBLE);

        mTopView = getChildAt(1);
        mBottomView = getChildAt(0);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    //----------------------------------------------APIS--------------------------------------

    /**
     * 设置拖动距离
     * @param dragRange
     */
    public void setDragRange(int dragRange){
        mDragRange = dragRange;
    }
}
