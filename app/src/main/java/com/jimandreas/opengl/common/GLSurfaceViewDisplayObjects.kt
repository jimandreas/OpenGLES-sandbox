package com.jimandreas.opengl.common

import android.content.Context
import android.graphics.PointF
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import timber.log.Timber
import android.view.MotionEvent
import com.jimandreas.opengl.displayobjects.RendererDisplayObjects

class GLSurfaceViewDisplayObjects : GLSurfaceView {
    private var mRenderer: RendererDisplayObjects? = null

    // Offsets for touch events
    private var mPreviousX: Float = 0f
    private var mPreviousY: Float = 0f
    private var mDensity: Float = 0f
    private var mPinchZoom: Float = 0f
    private var mTwoFingerOperation = false
    internal var mOldX = 0f
    internal var mOldY = 0f

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    // with h/t to :

    // http://stackoverflow.com/questions/14818530/how-to-implement-a-two-finger-drag-gesture-on-android
    // and:
    // http://judepereira.com/blog/multi-touch-in-android-translate-scale-and-rotate/

    override fun onTouchEvent(m: MotionEvent?): Boolean {
        val x1: Float
        val x2: Float
        val y1: Float
        val y2: Float
        var deltax: Float
        var deltay: Float
        var deltaSpacing: Float

        if (m == null) return false

        if (m != null) {
            //Number of touches
            var pointerCount = m.pointerCount
            if (pointerCount == 2) {
                val action = m.actionMasked
                val actionIndex = m.actionIndex
                val actionString: String
                mTwoFingerOperation = true
                when (action) {
                    MotionEvent.ACTION_DOWN ->

                        Timber.i("touch DOWN")
                    MotionEvent.ACTION_UP -> Timber.i("touch UP")
                    MotionEvent.ACTION_MOVE -> {

                        x1 = m.getX(0)
                        y1 = m.getY(0)
                        x2 = m.getX(1)
                        y2 = m.getY(1)

                        deltax = (x1 + x2) / 2.0f
                        deltax -= mOldX
                        deltay = (y1 + y2) / 2.0f
                        deltay -= mOldY

                        mRenderer!!.mDeltaTranslateX += deltax / (mDensity * 300f)
                        mRenderer!!.mDeltaTranslateY -= deltay / (mDensity * 300f)

                        mOldX = (x1 + x2) / 2.0f
                        mOldY = (y1 + y2) / 2.0f

                        deltaSpacing = spacing(m)
                        deltaSpacing -= mPinchZoom
                        deltaSpacing = deltaSpacing / (mDensity * 1000f)
                    }
                    MotionEvent.ACTION_POINTER_DOWN -> {
                        Timber.i("touch POINTER DOWN")

                        x1 = m.getX(0)
                        y1 = m.getY(0)
                        x2 = m.getX(1)
                        y2 = m.getY(1)

                        mOldX = (x1 + x2) / 2.0f
                        mOldY = (y1 + y2) / 2.0f
                        mPinchZoom = spacing(m)
                        Timber.i("touch DOWN, mPinchZoom is $mPinchZoom")
                    }
                }//                        Timber.i("deltaSpacing = " + deltaSpacing);
                // mRenderer.mScaleDelta = -deltaSpacing;
                // Timber.i("touch MOVE", "dy = " + deltay);

                pointerCount = 0
                return true
            } else {
                /*
                 * handle single finger swipe - rotate each item
                 */
                val x = m.x
                val y = m.y

                if (m.action == MotionEvent.ACTION_MOVE) {
                    if (mTwoFingerOperation) {  // handle two to one finger interaction
                        mTwoFingerOperation = false
                        mPreviousX = x
                        mPreviousY = y
                    }
                    if (mRenderer != null) {
                        val deltaX = (x - mPreviousX) / mDensity / 2f
                        val deltaY = (y - mPreviousY) / mDensity / 2f

                        mRenderer!!.mDeltaX += deltaX
                        mRenderer!!.mDeltaY += deltaY
                        // Timber.i("touch", ": mDX = " + mRenderer.mDeltaX + " mDY = " + mRenderer.mDeltaY);
                    }
                }
                mPreviousX = x
                mPreviousY = y

                return true
            }
        } else {
            return super.onTouchEvent(m)
        }
    }

    /**
     * Determine the space between the first two fingers
     */
    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
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
        val delta_x = (event.getX(0) - event.getX(1)).toDouble()
        val delta_y = (event.getY(0) - event.getY(1)).toDouble()
        val radians = Math.atan2(delta_y, delta_x)
        return Math.toDegrees(radians).toFloat()
    }

    // Hides superclass method.
    fun setRenderer(renderer: RendererDisplayObjects, density: Float) {
        mRenderer = renderer
        mDensity = density
        super.setRenderer(renderer)
    }
}

/*
    public boolean onTouch(View v, MotionEvent event) {
        // handle touch events here
        ImageView view = (ImageView) v;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    float dx = event.getX() - start.x;
                    float dy = event.getY() - start.y;
                    matrix.postTranslate(dx, dy);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = (newDist / oldDist);
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                    if (lastEvent != null && event.getPointerCount() == 3) {
                        newRot = rotation(event);
                        float r = newRot - d;
                        float[] values = new float[9];
                        matrix.getValues(values);
                        float tx = values[2];
                        float ty = values[5];
                        float sx = values[0];
                        float xc = (view.getWidth() / 2) * sx;
                        float yc = (view.getHeight() / 2) * sx;
                        matrix.postRotate(r, tx + xc, ty + yc);
                    }
                }
                break;
        }

        view.setImageMatrix(matrix);
        return true;
    }
    */