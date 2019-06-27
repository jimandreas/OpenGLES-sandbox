package com.jimandreas.opengl.displayobjfile

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.jimandreas.opengl.R

class ActivityDisplayObjFile : Activity() {

    private lateinit var mRenderer: RendererDisplayObjFile
    private lateinit var surfaceView: SurfaceViewObjFile

    private var mNextNameIndex = -1
    private lateinit var objNameTextView: TextView
    
    private fun loadNextObjFile() {

        if (++mNextNameIndex == obj_file_names.size) {
            mNextNameIndex = 0
        }
        val name = obj_file_names[mNextNameIndex]
        mRenderer.setObjFileName(name)
        objNameTextView.text = obj_file_display_name[mNextNameIndex]

        surfaceView.queueEvent { mRenderer.loadObjFile() }
    }

    private fun loadPrevObjFile() {

        if (mNextNameIndex-- == 0) {
            mNextNameIndex = obj_file_names.size - 1
        }
        val name = obj_file_names[mNextNameIndex]
        objNameTextView.text = obj_file_display_name[mNextNameIndex]
        mRenderer.setObjFileName(name)

        surfaceView.queueEvent { mRenderer.loadObjFile() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.display_obj_file)

        surfaceView = findViewById(R.id.gl_surface_view)
        objNameTextView = findViewById(R.id.obj_name)

        // Check if the system supports OpenGL ES 2.0.
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo = activityManager.deviceConfigurationInfo
        val supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000

        if (supportsEs2) {
            // Request an OpenGL ES 2.0 compatible context.
            surfaceView.setEGLContextClientVersion(2)

            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)

            mRenderer = RendererDisplayObjFile(this, surfaceView)
            surfaceView.setRenderer(mRenderer, displayMetrics.density)
        } else {
            // This is where you could create an OpenGL ES 1.x compatible
            // renderer if you wanted to support both ES 1 and ES 2.
            return
        }

        loadNextObjFile()

        findViewById<View>(R.id.button_next_obj).setOnClickListener { loadNextObjFile() }

        findViewById<View>(R.id.button_prev_obj).setOnClickListener { loadPrevObjFile() }

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


    fun toggleIBO() {
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

    companion object {
        // wire in the names and display names
        private val obj_file_names = arrayOf("triangletest",
                // "cube",
                "helixcoil", "teapot", "cow", "teddybear")

        private val obj_file_display_name = arrayOf("Triangle",
                // "Cube",
                "Coiled Helix", "Teapot", "Cow", "Teddy Bear")
    }
}

/*
class SurfaceViewObjFile(contextIn: Context) : SurfaceViewCommon(contextIn) {
    fun setRenderer(rendererIn: RendererDisplayObjFile, densityIn: Float) {
        renderer = rendererIn
        density = densityIn
        super.setRenderer(rendererIn)
    }
}*/
