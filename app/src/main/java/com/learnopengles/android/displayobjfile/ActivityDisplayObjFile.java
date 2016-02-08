package com.learnopengles.android.displayobjfile;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;

import com.learnopengles.android.R;


public class ActivityDisplayObjFile extends Activity
{
    private static String LOG_TAG = "activity";
    /** Hold a reference to our GLSurfaceView */
	private GLSurfaceViewDisplayObjFile mGLSurfaceView;
	private RendererDisplayObjFile mRenderer;
    private static int mCurrentObjFileIndex = 0;

    private int mNextNameIndex = -1;
    // wire in the names and display names
    private final String[] obj_file_names = new String[] {
            "cube",
            "helixcoil",
            "teapot",
            "cow",
            "plants3",
            "Birds"



            // "lamp.obj"

    };

    String[] obj_file_display_name = new String[] {

            "Cube",
            "Coiled Helix",
            "Teapot",
            "Cow",
            "Plant",
            "Birds"



    };

    protected void loadNextObjFile() {

        if (++mNextNameIndex == obj_file_names.length) {
            mNextNameIndex = 0;
        }
        String name = obj_file_names[mNextNameIndex];
        setTitle(obj_file_display_name[mNextNameIndex]);
        mRenderer.setObjFileName(name);

        mGLSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.loadObjFile();
            }
        });
    }

    protected void loadPrevObjFile() {

        if (mNextNameIndex-- == 0) {
            mNextNameIndex = obj_file_names.length-1;
        }
        String name = obj_file_names[mNextNameIndex];
        setTitle(obj_file_display_name[mNextNameIndex]);
        mRenderer.setObjFileName(name);

        mGLSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.loadObjFile();
            }
        });
    }

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.display_obj_file);

        mGLSurfaceView = (GLSurfaceViewDisplayObjFile) findViewById(R.id.gl_surface_view);

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

            mRenderer = new RendererDisplayObjFile(this, mGLSurfaceView);
			mGLSurfaceView.setRenderer(mRenderer, displayMetrics.density);
		} 
		else 
		{
			// This is where you could create an OpenGL ES 1.x compatible
			// renderer if you wanted to support both ES 1 and ES 2.
			return;
		}

        loadNextObjFile();

		findViewById(R.id.button_next_obj).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadNextObjFile();
			}
		});

		findViewById(R.id.button_prev_obj).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadPrevObjFile();
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



    protected void toggleIBO() {
        mGLSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.toggleRenderIBOFlag();
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
                    ((Button) findViewById(R.id.button_switch_shaders)).setText(R.string.objects_using_vertex_shading);
                } else {
                    ((Button) findViewById(R.id.button_switch_shaders)).setText(R.string.objects_using_pixel_shading);
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