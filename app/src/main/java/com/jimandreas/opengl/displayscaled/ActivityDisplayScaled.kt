package com.jimandreas.opengl.displayscaled

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import com.jimandreas.opengl.R

class ActivityDisplayScaled : Activity() {

    private lateinit var mRenderer: RendererDisplayScaled
    private lateinit var surfaceView: SurfaceViewDisplayScaled

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.display_scaled)

        surfaceView = findViewById(R.id.gl_surface_view)

        // Check if the system supports OpenGL ES 2.0.
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo = activityManager.deviceConfigurationInfo
        val supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000

        if (supportsEs2) {
            // Request an OpenGL ES 2.0 compatible context.
            surfaceView.setEGLContextClientVersion(2)

            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)

            mRenderer = RendererDisplayScaled(this, surfaceView)
            surfaceView.setRenderer(mRenderer, displayMetrics.density)
        } else {
            // This is where you could create an OpenGL ES 1.x compatible
            // renderer if you wanted to support both ES 1 and ES 2.
            return
        }

        findViewById<View>(R.id.button_fewer).setOnClickListener { fewerTris() }
        findViewById<View>(R.id.button_more).setOnClickListener { moreTris() }
        findViewById<View>(R.id.button_switch_rendering_mode).setOnClickListener { toggleWireframe() }
        findViewById<View>(R.id.button_switch_shaders).setOnClickListener { toggleShader() }
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

    fun fewerTris() {
        surfaceView.queueEvent { mRenderer.fewerTris() }
    }

    fun moreTris() {
        surfaceView.queueEvent { mRenderer.moreTris() }
    }

    fun toggleShader() {
        surfaceView.queueEvent { mRenderer.toggleShader() }
    }

    fun toggleWireframe() {
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