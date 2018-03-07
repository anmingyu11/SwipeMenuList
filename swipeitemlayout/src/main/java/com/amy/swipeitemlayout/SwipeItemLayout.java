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
        this(context, null, 0);
    }

    public SwipeItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // 顶部视图
    private View mTopView;
    // 底部视图
    private View mBottomView;
    // 顶部视图外边距
    private MarginLayoutParams mTopLp;
    // 底部视图外边距
    private MarginLayoutParams mBottomLp;

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

        mTopLp = (MarginLayoutParams) mTopView.getLayoutParams();
        mBottomLp = (MarginLayoutParams) mBottomView.getLayoutParams();

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mDragRange = mBottomView.getMeasuredWidth() + mBottomLp.leftMargin + mBottomLp.rightMargin;
        //Todo : set this

        final int topWidth = mTopView.getMeasuredWidth();
        final int topHeight = mTopView.getMeasuredHeight();
        final int topTop = getPaddingTop() + mTopLp.topMargin;
        final int topBottom = topTop + topHeight;
        final int topLeft = getPaddingLeft() + mTopLp.leftMargin;
        final int topRight = Math.min(topLeft + topWidth, r - getPaddingRight() - mTopLp.rightMargin);

        final int bottomWidth = mBottomView.getMeasuredWidth();
        final int bottomHeight = mBottomView.getMeasuredHeight();
        final int bottomTop = getPaddingTop() + mBottomLp.topMargin;
        final int bottomBottom = bottomTop + bottomHeight;
        int bottomLeft = getPaddingLeft() + mBottomLp.leftMargin;
        int bottomRight = Math.min(bottomLeft + bottomWidth, r - getPaddingRight() - mBottomLp.rightMargin);

        if (mSwipeDirection == SwipeDirection.Left) {
            //左滑
            if (mLayoutModel == LayoutModel.PullOut) {
                bottomRight = topLeft - mBottomLp.rightMargin - mTopLp.leftMargin;
                bottomLeft = bottomRight - bottomWidth;
            }
        } else {
            //右滑
            if (mLayoutModel == LayoutModel.PullOut) {
                bottomLeft = topRight + mBottomLp.leftMargin + mTopLp.rightMargin;
                bottomRight = bottomLeft + bottomWidth;
            }
        }

        LogUtil.d("hole R: " + r + " L : " + l + " T : " + t + " B : " + b);
        LogUtil.d("top R : " + topRight + " L : " + topLeft + " T : " + topTop + " B: " + topBottom);
        LogUtil.d("bottom R : " + bottomRight + " L : " + bottomLeft + " T : " + bottomTop + " B: " + bottomBottom);
        mBottomView.layout(bottomLeft, bottomTop, bottomRight, bottomBottom);
        mTopView.layout(topLeft, topTop, topRight, topBottom);
    }

    //----------------------------------------------APIS--------------------------------------

    // 允许拖动的距离【注意：最终允许拖动的距离是 (mDragRange + mSpringDistance)】
    private int mDragRange;

    /**
     * 设置拖动距离
     *
     * @param dragRange
     */
    //public void setDragRange(int dragRange) {
    //    mDragRange = dragRange;
    //}

    // 控件滑动方向（向左，向右），默认向左滑动
    private SwipeDirection mSwipeDirection = SwipeDirection.Left;

    /**
     * 设置拖动方向
     */
    public void setSwipeDirection(SwipeDirection direction) {
        mSwipeDirection = direction;
    }

    // 移动过程中，底部视图的移动方式（拉出，被顶部视图遮住），默认是被顶部视图遮住
    private LayoutModel mLayoutModel = LayoutModel.PullOut;

    public void setLayoutModel(LayoutModel layoutModel) {
        mLayoutModel = layoutModel;
    }

}
