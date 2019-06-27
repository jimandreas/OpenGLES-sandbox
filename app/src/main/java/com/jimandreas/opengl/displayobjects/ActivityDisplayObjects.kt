package com.jimandreas.opengl.displayobjects

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import com.jimandreas.opengl.R

class ActivityDisplayObjects : Activity() {

    private lateinit var mRenderer: RendererDisplayObjects
    private lateinit var surfaceView: SurfaceViewDisplayObjects

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.display_objects)

        surfaceView = findViewById(R.id.gl_surface_view)

        // Check if the system supports OpenGL ES 2.0.
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo = activityManager.deviceConfigurationInfo
        val supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000

        if (!supportsEs2) {
            throw RuntimeException("This device does not support OpenGL ES2")
        }

        // Request an OpenGL ES 2.0 compatible context.
        surfaceView.setEGLContextClientVersion(2)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        mRenderer = RendererDisplayObjects( this, surfaceView )
        surfaceView.setRenderer(mRenderer, displayMetrics.density)

        findViewById<View>(R.id.button_only_ibo).setOnClickListener { toggleIBO() }
        findViewById<View>(R.id.button_switch_rendering_mode).setOnClickListener { toggleWireframe() }
        findViewById<View>(R.id.button_switch_shaders).setOnClickListener { toggleShader() }

        val ibo: View = findViewById(R.id.button_only_ibo)
        ibo.setOnClickListener {
            surfaceView.queueEvent { mRenderer.toggleRenderIBOFlag() }

        }
    }

    override fun onResume() {
        // The activity must call the GL surface view's onResume() on activity
        // onResume().
        super.onResume()
        surfaceView.onResume()
    }

    override fun onPause() {
        // The activity must call the GL surface view's onPause() on activity
        // onPause().
        super.onPause()
        surfaceView.onPause()
    }

    private fun toggleIBO() {
        surfaceView.queueEvent { mRenderer.toggleRenderIBOFlag() }
    }

    private fun toggleShader() {
        surfaceView.queueEvent { mRenderer.toggleShader() }
    }

    private fun toggleWireframe() {
        surfaceView.queueEvent { mRenderer.toggleWireframeFlag() }
    }

    fun updateShaderStatus(useVertexShading: Boolean) {
        runOnUiThread {
            if (useVertexShading) {
                (findViewById<View>(R.id.button_switch_shaders) as Button).setText(R.string.button_objects_using_pixel_shading)
            } else {
                (findViewById<View>(R.id.button_switch_shaders) as Button).setText(R.string.button_objects_using_vertex_shading)
            }
        }
    }

    fun updateWireframeStatus(wireFrameRendering: Boolean) {
        runOnUiThread {
            if (wireFrameRendering) {
                (findViewById<View>(
                        R.id.button_switch_rendering_mode) as Button).setText(R.string.button_objects_using_triangle_rendering)
            } else {
                (findViewById<View>(
                        R.id.button_switch_rendering_mode) as Button).setText(R.string.button_objects_using_wireframe_rendering)
            }
        }
    }

    fun updateRenderOnlyIBOStatus(renderOnlyIBO: Boolean) {
        runOnUiThread {
            if (renderOnlyIBO) {
                (findViewById<View>(
                        R.id.button_only_ibo) as Button).setText(R.string.button_objects_with_direct)
            } else {
                (findViewById<View>(
                        R.id.button_only_ibo) as Button).setText(R.string.button_objects_only_ibo)
            }
        }
    }
}

/*
class SurfaceViewDisplayObjects(contextIn: Context) : SurfaceViewCommon(contextIn) {
    fun setRenderer(rendererIn: RendererDisplayObjects, densityIn: Float) {
        renderer = rendererIn
        density = densityIn
        super.setRenderer(rendererIn)
    }
}
*/
