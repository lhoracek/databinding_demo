package cz.lhoracek.databinding

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.webkit.WebView
import android.widget.OverScroller
import androidx.core.view.*
import androidx.customview.widget.ViewDragHelper.INVALID_POINTER
import java.lang.Math.abs

class NestedScrollWebView : WebView, NestedScrollingChild3 {

    private var mLastMotionY: Int = 0

    private val mScrollOffset = IntArray(2)
    private val mScrollConsumed = IntArray(2)

    private var mNestedOffsetY: Float = 0f
    private val mChildHelper = NestedScrollingChildHelper(rootView)
    private var mIsBeingDragged = false
    private var mVelocityTracker: VelocityTracker? = null
    private var mTouchSlop: Int
    private var mMinimumVelocity: Float
    private var mMaximumVelocity: Float
    private var mActivePointerId: Int = INVALID_POINTER

    private var mScroller: OverScroller = OverScroller(context)

    init {
        isNestedScrollingEnabled = true
        val configuration = ViewConfiguration.get(context)
        mTouchSlop = configuration.scaledTouchSlop
        mMinimumVelocity = configuration.scaledMinimumFlingVelocity.toFloat()
        mMaximumVelocity = configuration.scaledMaximumFlingVelocity.toFloat()
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.action
        if (action == MotionEvent.ACTION_MOVE && mIsBeingDragged) { // most common
            return true
        }

        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_MOVE -> {
                val activePointerId = mActivePointerId
                if (activePointerId == INVALID_POINTER) {
                    return super.onTouchEvent(ev)
                }

                val pointerIndex = ev.findPointerIndex(activePointerId)
                if (pointerIndex == -1) {
                    Log.e(
                        TAG, "Invalid pointerId=" + activePointerId
                                + " in onInterceptTouchEvent"
                    )
                    return super.onTouchEvent(ev)
                }

                val y = ev.getY(pointerIndex).toInt()
                val yDiff = Math.abs(y - mLastMotionY)
                if (yDiff > mTouchSlop && nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL == 0) {
                    mIsBeingDragged = true
                    mLastMotionY = y
                    initVelocityTrackerIfNotExists()
                    mVelocityTracker!!.addMovement(ev)
                    mNestedOffsetY = 0f
                    val parent = parent
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
            }
            MotionEvent.ACTION_DOWN -> {
                mLastMotionY = ev.y.toInt()
                mActivePointerId = ev.getPointerId(0)

                initOrResetVelocityTracker()
                mVelocityTracker!!.addMovement(ev)

                mScroller.computeScrollOffset()
                mIsBeingDragged = !mScroller.isFinished

                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                mIsBeingDragged = false
                mActivePointerId = INVALID_POINTER
                recycleVelocityTracker()
                if (mScroller.springBack(scrollX, scrollY, 0, 0, 0, getScrollRange())) {
                    ViewCompat.postInvalidateOnAnimation(this)
                }
                stopNestedScroll()
            }
        }

        return mIsBeingDragged
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {

        initVelocityTrackerIfNotExists()

        val action = event.actionMasked

        if (action == MotionEvent.ACTION_DOWN) {
            mNestedOffsetY = 0f
        }

        val trackedEvent = MotionEvent.obtain(event)
        trackedEvent.offsetLocation(0f, mNestedOffsetY)
        Log.d(TAG, "$action")
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                if (mIsBeingDragged) {
                    val parent = parent
                    parent?.requestDisallowInterceptTouchEvent(true)
                }

                if (!mScroller.isFinished) {
                    abortAnimatedScroll()
                }

                mLastMotionY = event.y.toInt()
                mActivePointerId = event.getPointerId(0)
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH)
            }
            MotionEvent.ACTION_MOVE -> {
                val activePointerIndex = event.findPointerIndex(mActivePointerId)
                if (activePointerIndex == -1) {
                    Log.e(TAG, "Invalid pointerId=$mActivePointerId in onTouchEvent")
                    return super.onTouchEvent(event)
                }

                val y = event.getY(activePointerIndex).toInt()
                var deltaY = mLastMotionY - y

                if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset, ViewCompat.TYPE_TOUCH)) {
                    deltaY -= mScrollConsumed[1]
                    mNestedOffsetY += mScrollOffset[1]
                }
                if (!mIsBeingDragged && abs(deltaY) > mTouchSlop) {
                    parent?.requestDisallowInterceptTouchEvent(true)
                    mIsBeingDragged = true
                    if (deltaY > 0) {
                        deltaY -= mTouchSlop
                    } else {
                        deltaY += mTouchSlop
                    }
                }
                if (mIsBeingDragged) {
                    mLastMotionY = y - mScrollOffset[1]

                    val oldY = scrollY
                    val range = getScrollRange()
                    val overscrollMode = overScrollMode
                    val canOverscroll =
                        overscrollMode == View.OVER_SCROLL_ALWAYS || overscrollMode == View.OVER_SCROLL_IF_CONTENT_SCROLLS && range > 0

                    if (overScrollByCompat(
                            0, deltaY, 0, oldY, 0, range, 0,
                            0, true
                        ) && !hasNestedScrollingParent(ViewCompat.TYPE_TOUCH)
                    ) {
                        mVelocityTracker?.clear()
                    }

                    val scrolledDeltaY = scrollY - oldY
                    val dyUnconsumed = deltaY - scrolledDeltaY

                    mScrollConsumed[1] = 0

                    dispatchNestedScroll(
                        0, scrolledDeltaY, 0, dyUnconsumed, mScrollOffset, ViewCompat.TYPE_TOUCH, mScrollConsumed
                    )

                    mLastMotionY -= mScrollOffset[1]
                    mNestedOffsetY += mScrollOffset[1]

                    if (canOverscroll) {
                        deltaY -= mScrollConsumed[1]
                    }

                }
            }
            MotionEvent.ACTION_UP -> {
                mVelocityTracker?.let {velocityTracker ->
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity)
                    val initialVelocity = velocityTracker.getYVelocity(mActivePointerId).toInt()
                    if (abs(initialVelocity) > mMinimumVelocity) {
                        if (!dispatchNestedPreFling(0f, (-initialVelocity).toFloat())) {
                            fling(-initialVelocity)
                        }
                    } else if (mScroller.springBack(
                            scrollX, scrollY, 0, 0, 0,
                            getScrollRange())) {
                        ViewCompat.postInvalidateOnAnimation(this)
                    }
                }
                mActivePointerId = INVALID_POINTER
                endDrag()
            }
            MotionEvent.ACTION_CANCEL -> {
                if (mIsBeingDragged) {
                    if (mScroller.springBack(
                            scrollX, scrollY, 0, 0, 0,
                            getScrollRange()
                        )
                    ) {
                        ViewCompat.postInvalidateOnAnimation(this)
                    }
                }
                mActivePointerId = INVALID_POINTER
                endDrag()
            }
        }

        mVelocityTracker?.addMovement(trackedEvent)
        trackedEvent.recycle()

        return super.onTouchEvent(event)
    }

    private fun abortAnimatedScroll() {
        mScroller.abortAnimation()
        stopNestedScroll(ViewCompat.TYPE_NON_TOUCH)
    }

    private fun endDrag() {
        mIsBeingDragged = false
        recycleVelocityTracker()
        stopNestedScroll()
    }

    private fun getScrollRange(): Int {
        return computeVerticalScrollRange()
    }

    private fun fling(velocityY: Int) {
        val height = height
        mScroller.fling(
            scrollX, scrollY, // start
            0, velocityY, // velocities
            0, 0, // x
            Integer.MIN_VALUE, Integer.MAX_VALUE, // y
            0, height / 2
        )
        runAnimatedScroll(true)
    }

    private fun runAnimatedScroll(participateInNestedScrolling: Boolean) {
        if (participateInNestedScrolling) {
            startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_NON_TOUCH)
        } else {
            stopNestedScroll(ViewCompat.TYPE_NON_TOUCH)
        }
        mLastMotionY = scrollY
        ViewCompat.postInvalidateOnAnimation(this)
    }

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        if (disallowIntercept) {
            recycleVelocityTracker()
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept)
    }

    private fun initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        } else {
            mVelocityTracker?.clear()
        }
    }

    private fun initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
    }

    private fun recycleVelocityTracker() {
        mVelocityTracker?.recycle()
        mVelocityTracker = null
    }

    override fun overScrollBy(
        deltaX: Int, deltaY: Int,
        scrollX: Int, scrollY: Int,
        scrollRangeX: Int, scrollRangeY: Int,
        maxOverScrollX: Int, maxOverScrollY: Int,
        isTouchEvent: Boolean
    ): Boolean {
        // this is causing double scroll call (doubled speed), but this WebView isn't overscrollable
        // all overscrolls are passed to appbar, so commenting this out during drag
        if (!mIsBeingDragged) {
            overScrollByCompat(
                deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY,
                maxOverScrollX, maxOverScrollY, isTouchEvent
            )
        }
        // without this call webview won't scroll to top when url change or when user pick input
        // (webview should move a bit making input still in viewport when "adjustResize")
        return true
    }

    // NestedScrollingChild

    override fun isNestedScrollingEnabled(): Boolean {
        return mChildHelper.isNestedScrollingEnabled
    }

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        mChildHelper.isNestedScrollingEnabled = enabled
    }

    override fun startNestedScroll(axes: Int, type: Int): Boolean {
        return mChildHelper.startNestedScroll(axes, type)
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return startNestedScroll(axes, ViewCompat.TYPE_TOUCH)
    }

    override fun stopNestedScroll(type: Int) {
        mChildHelper.stopNestedScroll(type)
    }

    override fun stopNestedScroll() {
        stopNestedScroll(ViewCompat.TYPE_TOUCH)
    }

    override fun hasNestedScrollingParent(type: Int): Boolean {
        return mChildHelper.hasNestedScrollingParent(type)
    }

    override fun hasNestedScrollingParent(): Boolean {
        return hasNestedScrollingParent(ViewCompat.TYPE_TOUCH)
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int,
        offsetInWindow: IntArray?
    ): Boolean {
        return dispatchNestedScroll(
            dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
            offsetInWindow, ViewCompat.TYPE_TOUCH
        )
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int,
        offsetInWindow: IntArray?, type: Int
    ): Boolean {
        return mChildHelper.dispatchNestedScroll(
            dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
            offsetInWindow, type
        )
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int,
        offsetInWindow: IntArray?, type: Int, consumed: IntArray
    ) {
        mChildHelper.dispatchNestedScroll(
            dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
            offsetInWindow, type, consumed
        )
    }

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean {
        return dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, ViewCompat.TYPE_TOUCH)
    }


    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, false)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun getNestedScrollAxes(): Int {
        return ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun computeScroll() {
        if (mScroller.isFinished) {
            return
        }

        mScroller.computeScrollOffset()
        val y = mScroller.currY
        var unconsumed = y - mLastMotionY
        mLastMotionY = y

        // Nested Scrolling Pre Pass
        mScrollConsumed[1] = 0
        dispatchNestedPreScroll(
            0, unconsumed, mScrollConsumed, null,
            ViewCompat.TYPE_NON_TOUCH
        )
        unconsumed -= mScrollConsumed[1]


        if (unconsumed != 0) {
            // Internal Scroll
            val oldScrollY = scrollY
            overScrollByCompat(
                0, unconsumed, scrollX, oldScrollY, 0, computeVerticalScrollRange(),
                0, 0, false
            )
            val scrolledByMe = scrollY - oldScrollY
            unconsumed -= scrolledByMe

            // Nested Scrolling Post Pass
            mScrollConsumed[1] = 0
            dispatchNestedScroll(
                0, 0, 0, unconsumed, mScrollOffset,
                ViewCompat.TYPE_NON_TOUCH, mScrollConsumed
            )
            unconsumed -= mScrollConsumed[1]
        }

        if (unconsumed != 0) {
            abortAnimatedScroll()
        }

        if (!mScroller!!.isFinished) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    // copied from NestedScrollView exacly as it looks, leaving overscroll related code, maybe future use
    private fun overScrollByCompat(
        deltaX: Int, deltaY: Int,
        scrollX: Int, scrollY: Int,
        scrollRangeX: Int, scrollRangeY: Int,
        maxOverScrollX: Int, maxOverScrollY: Int,
        isTouchEvent: Boolean
    ): Boolean {
        var maxOverScrollX = maxOverScrollX
        var maxOverScrollY = maxOverScrollY
        val overScrollMode = overScrollMode
        val canScrollHorizontal = computeHorizontalScrollRange() > computeHorizontalScrollExtent()
        val canScrollVertical = computeVerticalScrollRange() > computeVerticalScrollExtent()
        val overScrollHorizontal =
            overScrollMode == View.OVER_SCROLL_ALWAYS || overScrollMode == View.OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollHorizontal
        val overScrollVertical =
            overScrollMode == View.OVER_SCROLL_ALWAYS || overScrollMode == View.OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollVertical

        var newScrollX = scrollX + deltaX
        if (!overScrollHorizontal) {
            maxOverScrollX = 0
        }

        var newScrollY = scrollY + deltaY
        if (!overScrollVertical) {
            maxOverScrollY = 0
        }

        // Clamp values if at the limits and record
        val left = -maxOverScrollX
        val right = maxOverScrollX + scrollRangeX
        val top = -maxOverScrollY
        val bottom = maxOverScrollY + scrollRangeY

        var clampedX = false
        if (newScrollX > right) {
            newScrollX = right
            clampedX = true
        } else if (newScrollX < left) {
            newScrollX = left
            clampedX = true
        }

        var clampedY = false
        if (newScrollY > bottom) {
            newScrollY = bottom
            clampedY = true
        } else if (newScrollY < top) {
            newScrollY = top
            clampedY = true
        }

        if (clampedY && !hasNestedScrollingParent(ViewCompat.TYPE_NON_TOUCH)) {
            mScroller.springBack(newScrollX, newScrollY, 0, 0, 0, computeVerticalScrollRange())
        }

        onOverScrolled(newScrollX, newScrollY, clampedX, clampedY)

        return clampedX || clampedY
    }

    companion object {
        val TAG = NestedScrollWebView::class.java.simpleName
    }

}