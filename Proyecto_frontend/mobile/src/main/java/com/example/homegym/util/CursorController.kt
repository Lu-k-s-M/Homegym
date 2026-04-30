package com.example.homegym.util

import android.app.Activity
import android.graphics.Rect
import android.os.SystemClock
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText

class CursorController(private val activity: Activity) {

    private var cursorView: VirtualCursorView? = null
    private val minStep = 5f
    private val maxStep = 25f
    private val acceleration = 1.2f
    private var currentStep = minStep

    private var screenWidth = 0
    private var screenHeight = 0

    private val pressedKeys = mutableSetOf<Int>()
    private var isMoving = false

    private val moveRunnable = object : Runnable {
        override fun run() {
            var x = cursorView?.getCursorX() ?: 0f
            var y = cursorView?.getCursorY() ?: 0f

            if (pressedKeys.isNotEmpty()) {
                if (KeyEvent.KEYCODE_DPAD_UP in pressedKeys) y -= currentStep
                if (KeyEvent.KEYCODE_DPAD_DOWN in pressedKeys) y += currentStep
                if (KeyEvent.KEYCODE_DPAD_LEFT in pressedKeys) x -= currentStep
                if (KeyEvent.KEYCODE_DPAD_RIGHT in pressedKeys) x += currentStep

                // Keep within bounds
                if (screenWidth > 0 && screenHeight > 0) {
                    x = x.coerceIn(0f, screenWidth.toFloat())
                    y = y.coerceIn(0f, screenHeight.toFloat())
                }
                cursorView?.updatePosition(x, y)

                // Accelerate cursor speed
                if (currentStep < maxStep) {
                    currentStep += acceleration
                }
            } else {
                currentStep = minStep
            }

            // Always check for auto-scroll if the cursor is in the threshold zones
            if (screenWidth > 0 && screenHeight > 0) {
                val scrolled = checkAutoScroll(x, y)
                
                // If no keys are pressed AND we didn't scroll, we can stop the loop
                if (pressedKeys.isEmpty() && scrolled == 0) {
                    isMoving = false
                    currentStep = minStep
                    return
                }
            } else if (pressedKeys.isEmpty()) {
                isMoving = false
                currentStep = minStep
                return
            }

            if (isMoving) {
                activity.window.decorView.postOnAnimation(this)
            }
        }
    }

    fun attach() {
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
        cursorView = VirtualCursorView(activity)
        rootView.addView(cursorView, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))

        rootView.post {
            screenWidth = rootView.width
            screenHeight = rootView.height
            cursorView?.updatePosition(screenWidth / 2f, screenHeight / 2f)
        }
    }

    fun handleKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        
        // Only handle D-Pad keys
        if (keyCode != KeyEvent.KEYCODE_DPAD_UP &&
            keyCode != KeyEvent.KEYCODE_DPAD_DOWN &&
            keyCode != KeyEvent.KEYCODE_DPAD_LEFT &&
            keyCode != KeyEvent.KEYCODE_DPAD_RIGHT &&
            keyCode != KeyEvent.KEYCODE_DPAD_CENTER &&
            keyCode != KeyEvent.KEYCODE_ENTER) {
            return false
        }

        if (event.action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.repeatCount == 0) {
                    simulateClick(cursorView?.getCursorX() ?: 0f, cursorView?.getCursorY() ?: 0f)
                }
                return true
            }

            if (pressedKeys.add(keyCode)) {
                if (!isMoving) {
                    isMoving = true
                    activity.window.decorView.postOnAnimation(moveRunnable)
                }
            }
            return true
        } else if (event.action == KeyEvent.ACTION_UP) {
            pressedKeys.remove(keyCode)
            // No longer resetting isMoving = false here if we want continuous scroll
            return true
        }

        return false
    }

    private fun simulateClick(x: Float, y: Float) {
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis()
        
        val downEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0)
        activity.window.decorView.dispatchTouchEvent(downEvent)
        
        val upEvent = MotionEvent.obtain(downTime, eventTime + 100, MotionEvent.ACTION_UP, x, y, 0)
        activity.window.decorView.dispatchTouchEvent(upEvent)

        downEvent.recycle()
        upEvent.recycle()

        // Also try to find if it's an EditText to focus it
        findViewAt(activity.window.decorView, x.toInt(), y.toInt())?.let { view ->
            if (view is EditText || view.isClickable) {
                view.requestFocus()
                if (view is EditText) {
                    // Force open keyboard? Usually requestFocus is enough if inTouchMode
                }
            }
        }
    }

    private fun findViewAt(view: View, x: Int, y: Int): View? {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                val rect = Rect()
                child.getGlobalVisibleRect(rect)
                if (rect.contains(x, y) && child.visibility == View.VISIBLE) {
                    val found = findViewAt(child, x, y)
                    if (found != null) return found
                }
            }
        }
        
        val rect = Rect()
        view.getGlobalVisibleRect(rect)
        return if (rect.contains(x, y) && view.visibility == View.VISIBLE) view else null
    }

    private fun checkAutoScroll(x: Float, y: Float): Int {
        val scrollThreshold = 180f // A bit more threshold for easier activation
        var scrollSpeed = 15

        // If cursor is very close to the edge, increase scroll speed
        if (y > screenHeight - 50 || y < 50) {
            scrollSpeed = 25
        }

        return when {
            y > screenHeight - scrollThreshold -> {
                scrollViewsAt(x, y, scrollSpeed)
            }
            y < scrollThreshold -> {
                scrollViewsAt(x, y, -scrollSpeed)
            }
            else -> 0
        }
    }

    private fun scrollViewsAt(x: Float, y: Float, amount: Int): Int {
        val rootView = activity.window.decorView
        val scrollable = findScrollableElementAt(rootView, x.toInt(), y.toInt())
            ?: findFirstScrollableVertical(rootView) // Fallback to first vertical scrollable found

        scrollable?.let { v ->
            val actualAmount = if (amount > 0) {
                if (v.canScrollVertically(1)) amount else 0
            } else {
                if (v.canScrollVertically(-1)) amount else 0
            }
            
            if (actualAmount != 0) {
                v.scrollBy(0, actualAmount)
                return actualAmount
            }
        }
        return 0
    }

    private fun findFirstScrollableVertical(view: View): View? {
        if (view.visibility != View.VISIBLE) return null
        
        if (view.canScrollVertically(1) || view.canScrollVertically(-1)) {
            // Check if it's NOT a horizontal RecyclerView (usually we want to scroll the main container)
            if (view is androidx.recyclerview.widget.RecyclerView) {
                val lm = view.layoutManager
                if (lm is androidx.recyclerview.widget.LinearLayoutManager && lm.orientation == androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL) {
                    // Skip horizontal lists
                } else {
                    return view
                }
            } else {
                return view
            }
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val found = findFirstScrollableVertical(view.getChildAt(i))
                if (found != null) return found
            }
        }
        return null
    }

    private fun findScrollableElementAt(view: View, x: Int, y: Int): View? {
        if (view is ViewGroup) {
            for (i in view.childCount - 1 downTo 0) { // Check front to back
                val child = view.getChildAt(i)
                val rect = Rect()
                child.getGlobalVisibleRect(rect)
                if (rect.contains(x, y) && child.visibility == View.VISIBLE) {
                    val found = findScrollableElementAt(child, x, y)
                    if (found != null) return found
                }
            }
        }

        val rect = Rect()
        view.getGlobalVisibleRect(rect)
        return if (rect.contains(x, y) && view.visibility == View.VISIBLE && 
            (view.canScrollVertically(1) || view.canScrollVertically(-1))) {
            
            // Preference for vertical scrollable if we are doing vertical scroll
            if (view is androidx.recyclerview.widget.RecyclerView) {
                val lm = view.layoutManager
                if (lm is androidx.recyclerview.widget.LinearLayoutManager && lm.orientation == androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL) {
                    null // Skip horizontal lists here, we want the parent scrollable
                } else {
                    view
                }
            } else {
                view
            }
        } else {
            null
        }
    }
}
