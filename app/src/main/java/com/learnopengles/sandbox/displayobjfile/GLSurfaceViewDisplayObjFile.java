package com.learnopengles.sandbox.displayobjfile;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import timber.log.Timber;
import android.view.MotionEvent;

public class GLSurfaceViewDisplayObjFile extends GLSurfaceView
{
	private RendererDisplayObjFile mRenderer;

	// Offsets for touch events
    private float mPreviousX;
    private float mPreviousY;
    private float mDensity;
    private float mPinchZoom;
    private boolean mTwoFingerOperation = false;
    float mOldX = 0f, mOldY = 0f;

	public GLSurfaceViewDisplayObjFile(Context context)
	{
		super(context);
	}

	public GLSurfaceViewDisplayObjFile(Context context, AttributeSet attrs)
	{
		super(context, attrs);		
	}

	// with h/t to :
	
	// http://stackoverflow.com/questions/14818530/how-to-implement-a-two-finger-drag-gesture-on-android
    // and:
    // http://judepereira.com/blog/multi-touch-in-android-translate-scale-and-rotate/
	
	@Override
	public boolean onTouchEvent(MotionEvent m) 
	{
        float x1, x2, y1, y2, deltax, deltay, deltaSpacing;

		if (m != null)
		{
			//Number of touches
			int pointerCount = m.getPointerCount();
			if(pointerCount == 2){
				int action = m.getActionMasked();
				int actionIndex = m.getActionIndex();
				String actionString;
				mTwoFingerOperation = true;
				switch (action)
				{
					case MotionEvent.ACTION_DOWN:

						Timber.i("touch DOWN");
						break;
					case MotionEvent.ACTION_UP:
						Timber.i("touch UP");
						break;
					case MotionEvent.ACTION_MOVE:

                        x1 = m.getX(0);
                        y1 = m.getY(0);
                        x2 = m.getX(1);
                        y2 = m.getY(1);

                        deltax = (x1+x2) / 2.0f;
                        deltax -= mOldX;
                        deltay = (y1+y2) / 2.0f;
                        deltay -= mOldY;

                        mRenderer.mDeltaTranslateX += deltax / (mDensity * 300f);
                        mRenderer.mDeltaTranslateY -= deltay / (mDensity * 300f);

                        mOldX = (x1+x2) / 2.0f;
                        mOldY = (y1+y2) / 2.0f;

                        deltaSpacing = spacing(m);
                        deltaSpacing -= mPinchZoom;
                        deltaSpacing = deltaSpacing / (mDensity * 1000f);
                        Timber.i("pinchzoom deltaSpacing = " + deltaSpacing);
                        // mRenderer.mScaleDelta = -deltaSpacing;

                        // Timber.i("touch MOVE", "dy = " + deltay);
                        
                        break;
					case MotionEvent.ACTION_POINTER_DOWN:
						Timber.i("touch POINTER DOWN");

                        x1 = m.getX(0);
                        y1 = m.getY(0);
                        x2 = m.getX(1);
                        y2 = m.getY(1);

                        mOldX = (x1+x2) / 2.0f;
                        mOldY = (y1+y2) / 2.0f;
                        mPinchZoom = spacing(m);
                        Timber.i("touch DOWN, mPinchZoom is " + mPinchZoom);
						break;
				}

				pointerCount = 0;
				return true;
			} else {
                /*
                 * handle single finger swipe - rotate each item
                 */
				float x = m.getX();
				float y = m.getY();

				if (m.getAction() == MotionEvent.ACTION_MOVE) {
                    if (mTwoFingerOperation) {  // handle two to one finger interaction
                        mTwoFingerOperation = false;
                        mPreviousX = x;
                        mPreviousY = y;
                    }
                    if (mRenderer != null) {
                        float deltaX = (x - mPreviousX) / mDensity / 2f;
                        float deltaY = (y - mPreviousY) / mDensity / 2f;

                        mRenderer.mDeltaX += deltaX;
                        mRenderer.mDeltaY += deltaY;
                        // Timber.i("touch", ": mDX = " + mRenderer.mDeltaX + " mDY = " + mRenderer.mDeltaY);
                    }
                }
                mPreviousX = x;
				mPreviousY = y;

				return true;
			}
		}
		else
		{
			return super.onTouchEvent(m);
		}		
	}

    /**
     * Determine the space between the first two fingers
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * Calculate the degree to be rotated by.
     *
     * @param event
     * @return Degrees
     */
    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

	// Hides superclass method.
	public void setRenderer(RendererDisplayObjFile renderer, float density)
	{
		mRenderer = renderer;
		mDensity = density;
		super.setRenderer(renderer);
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