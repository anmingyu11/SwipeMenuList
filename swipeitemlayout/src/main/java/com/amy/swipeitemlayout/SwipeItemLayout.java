package com.amy.swipeitemlayout;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;

import static android.support.v4.widget.ViewDragHelper.STATE_IDLE;

public class SwipeItemLayout extends FrameLayout {

    public enum SwipeDirection {
        Left, Right
    }

    public enum LayoutModel {
        PullOut, LayDown
    }

    public enum Status {
        Opened, Closed, Moving
    }

    public interface OnWindowVisibilityChangedListener {
        /**
         * @param visibility
         * @param item
         * @see View#onWindowVisibilityChanged(int)
         */
        void onWindowVisibilityChanged(int visibility, SwipeItemLayout item);
    }

    public interface OpenStatusListener {
        void onStatusChanged(Status status, SwipeItemLayout item);
    }

    public interface DragStatusChangedListener {
        /**
         * @param state
         * @param item
         * @see ViewDragHelper#STATE_IDLE
         * @see ViewDragHelper#STATE_DRAGGING
         * @see ViewDragHelper#STATE_SETTLING
         */
        void onStatusChanged(int state, SwipeItemLayout item);
    }

    private static boolean isTouching = false;
    // 顶部视图
    private View mTopView;
    // 底部视图
    private View mBottomView;

    // 滑动控件当前的状态（打开，关闭，正在移动），默认是关闭状态
    private Status mCurrentStatus = Status.Closed;
    // 滑动控件滑动前的状态
    private Status mPreStatus = mCurrentStatus;

    //拖动比率
    private float mDragRatio;

    //layout之魂
    private int mTopLeft;

    // 顶部视图外边距
    private MarginLayoutParams mTopLp;
    // 底部视图外边距
    private MarginLayoutParams mBottomLp;
    //手势识别部分;
    private GestureDetectorCompat mGestureDetector;
    private GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener;
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

        mDragHelperCallBack = new ViewDragHelper.Callback() {

            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return child == mTopView;
            }

            @Override
            public void onViewDragStateChanged(int state) {
                if (mDragStatusChangedListener != null) {
                    mDragStatusChangedListener.onStatusChanged(state, SwipeItemLayout.this);
                }
            }

            @Override
            public int getViewVerticalDragRange(View child) {
                return 0;
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

                int topViewHorizontalOffset = Math.abs(mTopLeft - (getPaddingLeft() + mTopLp.leftMargin));
                if (topViewHorizontalOffset > mDragRange) {
                    mDragRatio = 1.0f;
                } else {
                    mDragRatio = 1.0f * topViewHorizontalOffset / mDragRange;
                }

                dispatchSwipeEvent();

                //Todo requestLayout();通过offsetLeftAndRight来移动view,如用requestLayout需要时间
                if (mLayoutModel == LayoutModel.PullOut) {
                    ViewCompat.offsetLeftAndRight(mBottomView, dx);
                    ViewCompat.offsetTopAndBottom(mBottomView, dy);
                }
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                if (releasedChild != mTopView) {
                    super.onViewReleased(releasedChild, xvel, yvel);
                }
                LogUtil.d("onReleased childView : " + (releasedChild == mTopView ? " topView " : " bottomView "));

                final float dragRatioBottom = mReleaseRatio;
                final float dragRatioTop = 1 - mReleaseRatio;

                int topFinalLeft = getPaddingLeft() + mTopLp.leftMargin;
                if (mSwipeDirection == SwipeDirection.Left) {
                    //向左滑动为打开，向右滑动为关闭
                    if (xvel < -VEL_THRESHOLD
                            || (mPreStatus == Status.Closed && xvel < VEL_THRESHOLD && mDragRatio >= dragRatioBottom)
                            || (mPreStatus == Status.Opened && xvel < VEL_THRESHOLD && mDragRatio >= dragRatioTop)) {
                        // 向左的速度达到条件
                        topFinalLeft -= mDragRange;
                    }
                } else {
                    //向左滑动为关闭，向右滑动为打开
                    if (xvel > VEL_THRESHOLD
                            || (mPreStatus == Status.Closed && xvel > -VEL_THRESHOLD && mDragRatio >= dragRatioBottom)
                            || (mPreStatus == Status.Opened && xvel > -VEL_THRESHOLD && mDragRatio >= dragRatioTop)) {
                        topFinalLeft += mDragRange;
                    }
                }

                mDragHelper.settleCapturedViewAt(topFinalLeft, getPaddingTop());

                ViewCompat.postInvalidateOnAnimation(SwipeItemLayout.this);
            }
        };

        mDragHelper = ViewDragHelper.create(this, mDragSensitivity, mDragHelperCallBack);

        //GestureDetector
        mSimpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {

            Runnable mCancelPressedTask = new Runnable() {
                @Override
                public void run() {
                    setPressed(false);
                }
            };

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    requestParentDisallowInterceptTouchEvent(true);
                    return true;
                }
                return false;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (Math.abs(velocityX) > Math.abs(velocityY)) {
                    requestParentDisallowInterceptTouchEvent(true);
                    return true;
                }
                return false;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                // 2
                setPressed(false);
                if (isClosed()) {
                    return performClick();
                }
                return false;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                // 1
                if (isClosed()) {
                    setPressed(true);
                    return true;
                }
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (isClosed()) {
                    setPressed(true);
                    postDelayed(mCancelPressedTask, 300);
                    performLongClick();
                }
            }

        };
        mGestureDetector = new GestureDetectorCompat(getContext(), mSimpleOnGestureListener);
    }

    private void dispatchSwipeEvent() {
        //Status preStatus = mCurrentStatus;
        updateCurrentStatus();
    }

    private void updateCurrentStatus() {
        if (mSwipeDirection == SwipeDirection.Left) {
            // 向左滑动

            if (mTopLeft == getPaddingLeft() + mTopLp.leftMargin - mDragRange) {
                mCurrentStatus = Status.Opened;
            } else if (mTopLeft == getPaddingLeft() + mTopLp.leftMargin) {
                mCurrentStatus = Status.Closed;
            } else {
                mCurrentStatus = Status.Moving;
            }
        } else {
            // 向右滑动

            if (mTopLeft == getPaddingLeft() + mTopLp.leftMargin + mDragRange) {
                mCurrentStatus = Status.Opened;
            } else if (mTopLeft == getPaddingLeft() + mTopLp.leftMargin) {
                mCurrentStatus = Status.Closed;
            } else {
                mCurrentStatus = Status.Moving;
            }
        }

        //Update Listener State
        if (mOpenStatusListener != null) {
            mOpenStatusListener.onStatusChanged(mCurrentStatus, this);
        }

        LogUtil.d("updating Status : " + mCurrentStatus);
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
    protected void onWindowVisibilityChanged(int visibility) {

        if (mOnWindowVisibilityChangedListener != null) {
            mOnWindowVisibilityChangedListener.onWindowVisibilityChanged(visibility, this);
        }

        super.onWindowVisibilityChanged(visibility);
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                if (isBlockMode && isTouching) {
                    return false;
                } else {
                    isTouching = true;
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                isTouching = false;
                break;
            }
        }

        return super.dispatchTouchEvent(ev);
    }

    //拦截触控事件
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mDragHelper.cancel();
            return false;
        }

        return isTouching && mDragHelper.shouldInterceptTouchEvent(ev);
    }

    public void requestParentDisallowInterceptTouchEvent(boolean disallow) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallow);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //boolean result;
        mDragHelper.processTouchEvent(ev);
        mGestureDetector.onTouchEvent(ev);
        if (mOnTouchListener != null) {
            mOnTouchListener.onTouch(this, ev);
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
        super.computeScroll();
    }

    private int getTopViewOriginTargetLeft(int isOpen) {
        int left = getPaddingLeft() + mTopLp.leftMargin;
        if (mSwipeDirection == SwipeDirection.Left) {
            left = left - isOpen * mDragRange;
        } else {
            left = left + isOpen * mDragRange;
        }
        return left;
    }

    //----------------------------------------------API--------------------------------------

    /**
     * 打开或关闭滑动控件
     *
     * @param open     0 关 1 开
     * @param withAnim
     */
    public void slide(int open, boolean withAnim) {
        final int finalTop = getPaddingTop() + mTopLp.topMargin;
        final int finalTopLeft = getTopViewOriginTargetLeft(open);

        if (withAnim) {
            boolean anim;
            anim = mDragHelper.smoothSlideViewTo(mTopView, finalTopLeft, finalTop);
            if (anim) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            final int topOffset = getTopViewOriginTargetLeft(1);
            final int finalTopOffset = mCurrentStatus == Status.Closed ? topOffset : -topOffset;
            mTopView.offsetLeftAndRight(finalTopOffset);
            mDragHelperCallBack.onViewPositionChanged(mTopView, finalTopLeft, 0, finalTopOffset, 0);
        }
    }

    /**
     * 自动打开或关闭滑动控件
     */
    public void slideAuto(boolean withAnim) {
        if (mDragHelper.getViewDragState() != STATE_IDLE || mCurrentStatus == Status.Moving) {
            LogUtil.e("Top item view can not slide.");
            return;
        }

        final int open = mCurrentStatus == Status.Closed ? 1 : 0;
        slide(open, withAnim);
    }

    /**
     * 默认开启 debug = true
     *
     * @param debug
     */
    public void setDebug(boolean debug) {
        LogUtil.enableDebug(debug);
    }

    private static boolean isBlockMode = true;

    /**
     * 是否开启阻塞模式,阻塞模式下,所有的swipeItemLayout控件只能有一个接收触摸事件
     *
     * @param open
     */
    public static void openBlockMode(boolean open) {
        isBlockMode = open;
    }

    /**
     * 滑动控件当前的状态（打开，关闭，正在移动），默认是关闭状态
     *
     * @return
     */
    public Status getStatus() {
        return mCurrentStatus;
    }

    public static final int DEFAULT_VEL_THRESHOLD = 400;
    private int VEL_THRESHOLD = DEFAULT_VEL_THRESHOLD;

    /**
     * fling的速度阈值 默认400
     *
     * @param velThreshold
     */
    public void setVelThreshold(int velThreshold) {
        if (velThreshold < 0) {
            throw new IllegalArgumentException("vel cannot be < 0.");
        }
        VEL_THRESHOLD = velThreshold;
    }

    public static final float DEFAULT_RELEASE_RATIO = 0.5f;
    public float mReleaseRatio = DEFAULT_RELEASE_RATIO;

    /**
     * 释放时自动回弹的比率
     *
     * @param ratio
     */
    public void setReleaseRatio(float ratio) {
        if (ratio > 1.0f || ratio < 0.0f) {
            throw new IllegalArgumentException("release ratio must between 0 to 1");
        }

        mReleaseRatio = ratio;
    }

    public static final float DEFAULT_DRAG_SENSITIVITY = 0.5f;
    private float mDragSensitivity = DEFAULT_DRAG_SENSITIVITY;

    /**
     * 越大越敏感 ^()^
     *
     * @param sensitivity
     */
    public void setDragSensitivity(float sensitivity) {
        if (sensitivity < 0) {
            throw new IllegalArgumentException("sensitivity cannot < 0");
        }
        mDragSensitivity = sensitivity;
    }

    /**
     * 拖动的弹簧距离
     */
    private int mSpringDistance = 0;

    /**
     * 设置拖动的弹簧距离
     *
     * @param distance
     */
    public void setSpringDistance(int distance) {
        mSpringDistance = distance;
    }

    private OnWindowVisibilityChangedListener mOnWindowVisibilityChangedListener = null;

    /**
     * 在当前window的可见性(非view可见性)变化时的回调接口
     *
     * @param listener
     */
    public void setOnWindowVisibilityChangedListener(OnWindowVisibilityChangedListener listener) {
        mOnWindowVisibilityChangedListener = listener;
    }

    /**
     * 允许拖动的距离【注意：最终允许拖动的距离是 (mDragRange + mSpringDistance)】
     */
    private int mDragRange;

    /**
     * Todo: 设置拖动距离
     *
     * @param dragRange
     * public void setDragRange(int dragRange) {
     * mDragRange = dragRange;
     * }
     */

    private SwipeDirection mSwipeDirection = SwipeDirection.Left;

    /**
     * 控件滑动方向（向左，向右），默认向左滑动
     */
    public void setSwipeDirection(SwipeDirection direction) {
        mSwipeDirection = direction;
    }

    private LayoutModel mLayoutModel = LayoutModel.PullOut;

    /**
     * 移动过程中，底部视图的移动方式（拉出，被顶部视图遮住），默认是被顶部视图遮住
     *
     * @param layoutModel
     */
    public void setLayoutModel(LayoutModel layoutModel) {
        mLayoutModel = layoutModel;
    }

    public boolean isClosed() {
        return mCurrentStatus == Status.Closed;
    }

    private OpenStatusListener mOpenStatusListener;

    /**
     * 开关状态变化接口
     *
     * @param openStatusListener
     */
    public void setOpenStatusListener(OpenStatusListener openStatusListener) {
        mOpenStatusListener = openStatusListener;
    }

    private DragStatusChangedListener mDragStatusChangedListener;

    public void setDragStatusChangedListener(DragStatusChangedListener dragStatusChangedListener) {
        mDragStatusChangedListener = dragStatusChangedListener;
    }

    private OnTouchListener mOnTouchListener;

    public void setTouchListener(OnTouchListener onTouchListener) {
        mOnTouchListener = onTouchListener;
    }
}
