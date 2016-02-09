package com.learnopengles.sandbox.displayscaled;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;

import com.learnopengles.sandbox.R;

public class ActivtyDisplayScaled extends Activity
{
    private static String LOG_TAG = "activity";

    /** Hold a reference to our GLSurfaceView */
	private GLSurfaceViewDisplayScaled mGLSurfaceView;
	private RendererDisplayScaled mRenderer;

	private ScaleGestureDetector mScaleDetector;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

        setContentView(R.layout.display_scaled);

        mScaleDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
            }
            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                Log.d(LOG_TAG, "zoom ongoing, scale: " + detector.getScaleFactor());
                return false;
            }
        });

        mGLSurfaceView = (GLSurfaceViewDisplayScaled) findViewById(R.id.gl_surface_view);

		// Check if the system supports OpenGL ES 2.0.
		final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
		final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

		if (supportsEs2) 
		{
			// Request an OpenGL ES 2.0 compatible context.
			mGLSurfaceView.setEGLContextClientVersion(2);

            final DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            mRenderer = new RendererDisplayScaled(this, mGLSurfaceView);
			mGLSurfaceView.setRenderer(mRenderer, displayMetrics.density);
		} 
		else 
		{
			// This is where you could create an OpenGL ES 1.x compatible
			// renderer if you wanted to support both ES 1 and ES 2.
			return;
		}

		findViewById(R.id.button_fewer).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fewerTris();
			}
		});
		findViewById(R.id.button_more).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				moreTris();
			}
		});

		findViewById(R.id.button_switch_rendering_mode).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleWireframe();
			}
		});

		findViewById(R.id.button_switch_shaders).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleShader();
			}
		});
	}

	@Override
	protected void onResume() {
		// The activity must call the GL surface view's onResume() on activity
		// onResume().
		super.onResume();
		mGLSurfaceView.onResume();
	}

	@Override
	protected void onPause() {
		// The activity must call the GL surface view's onPause() on activity
		// onPause().
		super.onPause();
		mGLSurfaceView.onPause();
	}

    protected void fewerTris() {
        mGLSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.fewerTris();
            }
        });
    }

    protected void moreTris() {
        mGLSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.moreTris();
            }
        });
    }

	protected void toggleShader() {
		mGLSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.toggleShader();
            }
        });
	}

    protected void toggleWireframe() {
        mGLSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.toggleWireframeFlag();
            }
        });
    }

    public void updateShaderStatus(final boolean useVertexShading) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (useVertexShading) {
                    ((Button) findViewById(R.id.button_switch_shaders)).setText(R.string.objects_using_pixel_shading);
                } else {
                    ((Button) findViewById(R.id.button_switch_shaders)).setText(R.string.objects_using_vertex_shading);
                }
            }
        });
    }
    public void updateWireframeStatus(final boolean wireFrameRendering) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (wireFrameRendering) {
                    ((Button) findViewById(
                            R.id.button_switch_rendering_mode)).setText(R.string.objects_using_triangle_rendering);
                } else {
                    ((Button) findViewById(
                            R.id.button_switch_rendering_mode)).setText(R.string.objects_using_wireframe_rendering);
                }
            }
        });
    }

    public void updateRenderOnlyIBOStatus(final boolean renderOnlyIBO) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (renderOnlyIBO) {
                    ((Button) findViewById(
                            R.id.button_only_ibo)).setText(R.string.objects_with_direct);
                } else {
                    ((Button) findViewById(
                            R.id.button_only_ibo)).setText(R.string.objects_only_ibo);
                }
            }
        });
    }
}