package com.amy.swipeitemlayout;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class SwipeItemLayout extends FrameLayout {

    public enum SwipeDirection {
        Left, Right
    }

    public enum LayoutModel {
        PullOut, LayDown
    }

    private static boolean isBlocking = true;
    // 顶部视图
    private View mTopView;
    // 底部视图
    private View mBottomView;

    //layout之魂
    private int mTopLeft;
    // 顶部视图外边距
    private MarginLayoutParams mTopLp;
    // 底部视图外边距
    private MarginLayoutParams mBottomLp;
    //手势识别部分;
    //private GestureDetectorCompat mGestureDetectorCompat;
    //private GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener;
    //拖动识别部分
    private ViewDragHelper mDragHelper;
    private ViewDragHelper.Callback mDragHelperCallBack;

    public SwipeItemLayout(Context context) {
        this(context, null, 0);
    }

    public SwipeItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        //mSimpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        //};
        //mGestureDetectorCompat = new GestureDetectorCompat(getContext(), mSimpleOnGestureListener);

        mDragHelperCallBack = new ViewDragHelper.Callback() {

            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return child == mTopView;
            }

            @Override
            public int getViewHorizontalDragRange(View child) {
                return mDragRange + mSpringDistance;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                int minTopLeft;
                int maxTopLeft;

                if (mSwipeDirection == SwipeDirection.Left) {
                    minTopLeft = getPaddingLeft() + mTopLp.leftMargin - (mDragRange + mSpringDistance);
                    maxTopLeft = getPaddingLeft() + mTopLp.leftMargin;
                } else {
                    minTopLeft = getPaddingLeft() + mTopLp.leftMargin;
                    maxTopLeft = getPaddingLeft() + mTopLp.leftMargin + (mDragRange + mSpringDistance);
                }

                return Math.min(Math.max(minTopLeft, left), maxTopLeft);
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                //Todo : return super.clampViewPositionVertical(child, top, dy);
                return 0;
            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                mTopLeft = left;

                requestLayout();
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);
            }
        };

        mDragHelper = ViewDragHelper.create(this, 1.0f, mDragHelperCallBack);
    }

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

        mTopLeft = getPaddingLeft() + mTopLp.leftMargin;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        final int bottomWidth = mBottomView.getMeasuredWidth();
        final int bottomHeight = mBottomView.getMeasuredHeight();

        mDragRange = mBottomView.getMeasuredWidth() + mBottomLp.leftMargin + mBottomLp.rightMargin;
        //Todo : set this

        final int topWidth = mTopView.getMeasuredWidth();
        final int topHeight = mTopView.getMeasuredHeight();

        final int topTop = getPaddingTop() + mTopLp.topMargin;
        final int topBottom = topTop + topHeight;
        final int topRight = Math.min(mTopLeft + topWidth, r - getPaddingRight() - mTopLp.rightMargin);


        final int bottomTop = getPaddingTop() + mBottomLp.topMargin;
        final int bottomBottom = bottomTop + bottomHeight;
        int bottomLeft;
        int bottomRight;

        if (mSwipeDirection == SwipeDirection.Left) {
            //左滑
            if (mLayoutModel == LayoutModel.PullOut) {
                bottomLeft = topRight + mBottomLp.leftMargin + mTopLp.rightMargin;
                bottomRight = bottomLeft + bottomWidth;
            } else {
                //Todo : this will be some problem when wrap content
                bottomRight = topWidth - getPaddingRight() - mBottomLp.rightMargin;
                //bottomRight = r - getPaddingRight() - mBottomLp.rightMargin;
                bottomLeft = bottomRight - bottomWidth;
            }
        } else {
            //右滑
            if (mLayoutModel == LayoutModel.PullOut) {
                bottomRight = mTopLeft - mBottomLp.rightMargin - mTopLp.leftMargin;
                bottomLeft = bottomRight - bottomWidth;
            } else {
                bottomLeft = getPaddingLeft() + mBottomLp.leftMargin;
                bottomRight = bottomLeft + bottomWidth;
            }
        }

        LogUtil.d("width : " + bottomWidth + " top : " + topWidth);
        LogUtil.d("hole R: " + r + " L : " + l + " T : " + t + " B : " + b);
        LogUtil.d("top R : " + topRight + " L : " + mTopLeft + " T : " + topTop + " B: " + topBottom);
        LogUtil.d("bottom R : " + bottomRight + " L : " + bottomLeft + " T : " + bottomTop + " B: " + bottomBottom);
        LogUtil.d("DragRange  : " + mDragRange);
        mBottomView.layout(bottomLeft, bottomTop, bottomRight, bottomBottom);
        mTopView.layout(mTopLeft, topTop, topRight, topBottom);
    }

    //拦截触控事件
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mDragHelper.cancel();
            return false;
        }

        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mDragHelper.processTouchEvent(ev);
        return true;
    }

    //----------------------------------------------API--------------------------------------

    public static boolean enableDebug = false;

    private static boolean isBlockMode = false;

    /**
     * 是否开启阻塞模式,阻塞模式下,所有的swipeItemLayout控件只能有一个处于open状态.
     *
     * @param open
     */
    public static void openBlockMode(boolean open) {
        isBlockMode = open;
    }

    /**
     * 拖动的弹簧距离
     */
    private int mSpringDistance = 0;

    /**
     * 设置拖动的距离
     *
     * @param distance
     */
    public void setSpringDistance(int distance) {
        mSpringDistance = distance;
    }

    /**
     * 允许拖动的距离【注意：最终允许拖动的距离是 (mDragRange + mSpringDistance)】
     */
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
    private LayoutModel mLayoutModel = LayoutModel.LayDown;

    public void setLayoutModel(LayoutModel layoutModel) {
        mLayoutModel = layoutModel;
    }

}
