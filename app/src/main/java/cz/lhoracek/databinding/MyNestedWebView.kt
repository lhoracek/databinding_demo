package cz.lhoracek.databinding

import androidx.core.view.NestedScrollingChild3

class MyNestedWebView : NestedScrollingChild3 {
    override fun setNestedScrollingEnabled(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun isNestedScrollingEnabled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun startNestedScroll(axes: Int, type: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun startNestedScroll(axes: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun stopNestedScroll(type: Int) {
        TODO("Not yet implemented")
    }

    override fun stopNestedScroll() {
        TODO("Not yet implemented")
    }

    override fun hasNestedScrollingParent(type: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun hasNestedScrollingParent(): Boolean {
        TODO("Not yet implemented")
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int,
        consumed: IntArray
    ) {
        TODO("Not yet implemented")
    }

    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?, type: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?): Boolean {
        TODO("Not yet implemented")
    }

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?, type: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean {
        TODO("Not yet implemented")
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        TODO("Not yet implemented")
    }
}