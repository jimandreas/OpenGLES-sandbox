@file:Suppress("ConstantConditionIf")

package com.jimandreas.opengl.common

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Scroller
import kotlin.math.atan2
import kotlin.math.sqrt


open class SurfaceViewCommon : GLSurfaceView {

    private var selectMode = false
    private var lastTouchState = NO_FINGER_DOWN

    lateinit var renderer: RendererCommon

    private var scroller: Scroller? = null
    private var scrollAnimator: ValueAnimator? = null
    private var gestureDetector: GestureDetector? = null
    private lateinit var contextInternal: Context

    // Offsets for touch events
    private var previousX: Float = 0f
    private var previousY: Float = 0f
    var density: Float = 0f
    private var initialSpacing: Float = 0f
    private var currentSpacing: Float = 0f

    private var oldX = 0f
    private var oldY = 0f

    private val isAnimationRunning: Boolean
        get() = !scroller!!.isFinished

    constructor(contextIn: Context) : super(contextIn) {
        init(contextIn)
    }

    constructor(contextIn: Context, attrs: AttributeSet) : super(contextIn, attrs) {
        init(contextIn)
    }

//    fun setRenderer(rendererIn: RendererDisplayObjects, densityIn: Float) {
//        renderer = rendererIn
//        density = densityIn
//        super.setRenderer(renderer)
//    }

    private fun init(contextIn: Context) {

        contextInternal = contextIn
        scroller = Scroller(contextInternal, null, true)

        // The scroller doesn't have any built-in animation functions--it just supplies
        // values when we ask it to. So we have to have a way to call it every frame
        // until the fling ends. This code (ab)uses a ValueAnimator object to generate
        // a callback on every animation frame. We don't use the animated value at all.

        scrollAnimator = ValueAnimator.ofFloat(0f, 1f)
        scrollAnimator!!.addUpdateListener {
            // tickScrollAnimation();
        }

        // Create a gesture detector to handle onTouch messages
        gestureDetector = GestureDetector(contextInternal, GestureListener())

        // Turn off long press--this control doesn't use it, and if long press is enabled,
        // you can't scroll for a bit, pause, then scroll some more (the pause is interpreted
        // as a long press, apparently)
        gestureDetector!!.setIsLongpressEnabled(false)
    }

    // with h/t to :

    // http://stackoverflow.com/questions/14818530/how-to-implement-a-two-finger-drag-gesture-on-android
    // and:
    // http://judepereira.com/blog/multi-touch-in-android-translate-scale-and-rotate/

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(m: MotionEvent?): Boolean {
        val x1: Float
        val x2: Float
        val y1: Float
        val y2: Float
        var deltax: Float
        var deltay: Float
        var deltaSpacing: Float

        // hand the event to the GestureDetector
        // ignore the result for now.
        // TODO:  hook up fling logic
        val result = gestureDetector!!.onTouchEvent(m)

        if (m == null) {
            return true
        }
        if (hack) renderMode = RENDERMODE_CONTINUOUSLY

        //Number of touches
        val pointerCount = m.pointerCount
        when {
            pointerCount > 2 -> {
                lastTouchState = MORE_FINGERS
                return true
            }
            pointerCount == 2 -> {
                if (selectMode) return true
                val action = m.actionMasked
                if (lastTouchState == MORE_FINGERS) {
                    x1 = m.getX(0)
                    y1 = m.getY(0)
                    x2 = m.getX(1)
                    y2 = m.getY(1)

                    renderer.touchX = m.x
                    renderer.touchY = m.y

                    oldX = (x1 + x2) / 2.0f
                    oldY = (y1 + y2) / 2.0f
                    lastTouchState = TWO_FINGERS_DOWN
                    return true
                }
                when (action) {
                    MotionEvent.ACTION_MOVE -> {

                        x1 = m.getX(0)
                        y1 = m.getY(0)
                        x2 = m.getX(1)
                        y2 = m.getY(1)

                        renderer.touchX = m.x
                        renderer.touchY = m.y

                        deltax = (x1 + x2) / 2.0f
                        deltax -= oldX
                        deltay = (y1 + y2) / 2.0f
                        deltay -= oldY

                        renderer.deltaTranslateX += deltax / (density * 300f)
                        renderer.deltaTranslateY -= deltay / (density * 300f)

                        oldX = (x1 + x2) / 2.0f
                        oldY = (y1 + y2) / 2.0f

                        currentSpacing = spacing(m)

                        if (lastTouchState != TWO_FINGERS_DOWN) {
                            initialSpacing = spacing(m)
                        } else {
                            deltaSpacing = currentSpacing - initialSpacing
                            deltaSpacing /= initialSpacing

                            // TODO: adjust this exponent.
                            //   for now, hack into buckets
                            if (renderer.scaleCurrent < 0.1f) {
                                renderer.scaleCurrent += -deltaSpacing / 1000f
                            } else if (renderer.scaleCurrent < 0.1f) {
                                renderer.scaleCurrent += -deltaSpacing / 500f
                            } else if (renderer.scaleCurrent < 0.5f) {
                                renderer.scaleCurrent += -deltaSpacing / 200f
                            } else if (renderer.scaleCurrent < 1f) {
                                renderer.scaleCurrent += -deltaSpacing / 50f
                            } else if (renderer.scaleCurrent < 2f) {
                                renderer.scaleCurrent += -deltaSpacing / 10f
                            } else if (renderer.scaleCurrent < 5f) {
                                renderer.scaleCurrent += -deltaSpacing / 10f
                            } else if (renderer.scaleCurrent > 5f) {
                                if (deltaSpacing > 0) {
                                    renderer.scaleCurrent += -deltaSpacing / 10f
                                }
                            }
                            // Log.w("Move", "Spacing is " + renderer.scaleCurrent + " spacing = " + deltaSpacing);
                        }
                    }
                    MotionEvent.ACTION_POINTER_DOWN -> {
                        // Log.w("touch POINTER DOWN", "");

                        x1 = m.getX(0)
                        y1 = m.getY(0)
                        x2 = m.getX(1)
                        y2 = m.getY(1)

                        renderer.touchX = m.x
                        renderer.touchY = m.y

                        oldX = (x1 + x2) / 2.0f
                        oldY = (y1 + y2) / 2.0f
                        initialSpacing = spacing(m)
                    }
                    MotionEvent.ACTION_POINTER_UP -> if (hack) renderMode = RENDERMODE_WHEN_DIRTY
                }// Log.w("Down", "touch DOWN, initialSpacing is " + initialSpacing);
                lastTouchState = TWO_FINGERS_DOWN
                return true
            }
            pointerCount == 1 -> {
                /*
                 * handle single finger swipe - rotate each item
                 */
                val x = m.x
                val y = m.y

                renderer.touchX = m.x
                renderer.touchY = m.y

                if (m.action == MotionEvent.ACTION_MOVE) {
                    if (lastTouchState != ONE_FINGER_DOWN) {  // handle anything to one finger interaction
                        lastTouchState = ONE_FINGER_DOWN
                    } else if (renderer != null) {
                        val deltaX = (x - previousX) / density / 2f
                        val deltaY = (y - previousY) / density / 2f

                        renderer.deltaX += deltaX
                        renderer.deltaY += deltaY
                        // Log.w("touch", ": dX = " + renderer.deltaX + " dY = " + renderer.deltaY);
                    }
                }
                previousX = x
                previousY = y

                return true
            }
            hack -> renderMode = RENDERMODE_WHEN_DIRTY
        }
        return super.onTouchEvent(m)
    }

    /**
     * Determine the space between the first two fingers
     */
    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt((x * x + y * y).toDouble()).toFloat()
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2, y / 2)
    }

    /**
     * Calculate the degree to be rotated by.
     *
     * @param event
     * @return Degrees
     */
    private fun rotation(event: MotionEvent): Float {
        val deltax = (event.getX(0) - event.getX(1)).toDouble()
        val deltay = (event.getY(0) - event.getY(1)).toDouble()
        val radians = atan2(deltay, deltax)
        return Math.toDegrees(radians).toFloat()
    }

    /**
     * Extends [GestureDetector.SimpleOnGestureListener] to provide custom gesture
     * processing.
     */
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {

            // Timber.w("onScroll");
            return true
        }

        // not implemented - probably a bad idea
        //   might be good to average out the pivot to help with jitter
        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {

            // Timber.w("onFling");
            //            // Set up the Scroller for a fling
            //            float scrollTheta = vectorToScalarScroll(
            //                    velocityX,
            //                    velocityY,
            //                    e2.getX() - pieBounds.centerX(),
            //                    e2.getY() - pieBounds.centerY());
            //            scroller.fling(
            //                    0,
            //                    (int) getPieRotation(),
            //                    0,
            //                    (int) scrollTheta / FLING_VELOCITY_DOWNSCALE,
            //                    0,
            //                    0,
            //                    Integer.MIN_VALUE,
            //                    Integer.MAX_VALUE);
            //
            //            // Start the animator and tell it to animate for the expected duration of the fling.
            //            if (Build.VERSION.SDK_INT >= 11) {
            //                scrollAnimator.setDuration(scroller.getDuration());
            //                scrollAnimator.start();
            //            }
            return true
        }

        override fun onDown(e: MotionEvent): Boolean {

            if (isAnimationRunning) {
                stopScrolling()
            }
            return true
        }
    }

    private fun stopScrolling() {
        scroller!!.forceFinished(true)
        onScrollFinished()
    }

    /**
     * Called when the user finishes a scroll action.
     */
    private fun onScrollFinished() {

    }

    companion object {

        private const val NO_FINGER_DOWN = 0
        private const val ONE_FINGER_DOWN = 1
        private const val TWO_FINGERS_DOWN = 2
        private const val MORE_FINGERS = 3

        private const val hack = true   // play with Rendermode
    }
}
