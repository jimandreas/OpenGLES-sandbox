@file:Suppress("FunctionName", "LocalVariableName"/*, "unused"*/)

package com.jimandreas.opengl.displayobjfile

import android.app.Activity
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.jimandreas.opengl.common.RendererCommon
import com.jimandreas.opengl.objects.*
import timber.log.Timber
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/*
 *   Alt-Enter to disable annoying Lint warnings...
 *
 *   MVP
 *     M - Model to World
 *     V - World to View
 *     P - View to Projection
 */

/**
 * This class implements our custom renderer. Note that the GL10 parameter passed in is unused for OpenGL ES 2.0
 * renderers -- the static class GLES20 is used instead.
 */
class RendererDisplayObjFile(activityIn: Activity, surfaceViewIn: SurfaceViewObjFile)
    : RendererCommon(surfaceViewIn), GLSurfaceView.Renderer {

    /*
    class RendererDisplayObjects(activityIn: Activity, surfaceViewIn: SurfaceViewDisplayObjects)
    : RendererCommon(activityIn, surfaceViewIn), GLSurfaceView.Renderer {
     */
    private val mXYZ = XYZ()

    private lateinit var objFileName: String

    // update to add touch control - these are set by the SurfaceView class
    // These still work without volatile, but refreshes are not guaranteed to happen.

    /*var scaleCurrent = 0.5f
    var scalePrevious = 0f
    var deltaX: Float = 0f
    var deltaY: Float = 0f
    var deltaTranslateX: Float = 0f
    var deltaTranslateY: Float = 0f
    var scaleDelta = 0f*/

    private var activity : ActivityDisplayObjFile
    init {
        if (activityIn is ActivityDisplayObjFile) {
            activity = activityIn
        } else {
            throw(RuntimeException("Expect ActivityDisplayObjFile as parameter"))
        }
    }

    /**
     * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
     * of being located at the center of the universe) to world space.
     */
    private val modelMatrix = FloatArray(16)

    /**
     * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
     * it positions things relative to our eye.
     */
    private val viewMatrix = FloatArray(16)

    /** Store the projection matrix. This is used to project the scene onto a 2D viewport.  */
    private val projectionMatrix = FloatArray(16)

    /** Allocate storage for the final combined matrix. This will be passed into the shader program.  */
    private val mMVPMatrix = FloatArray(16)

    /**
     * Stores a copy of the model matrix specifically for the light position.
     */
    private val lightModelMatrix = FloatArray(16)
    private var mMVPMatrixHandle: Int = 0
    private var mMVMatrixHandle: Int = 0
    private var lightPosHandle: Int = 0
    private var positionHandle: Int = 0
    private var colorHandle: Int = 0
    private var normalHandle: Int = 0

    /** Used to hold a light centered on the origin in model space. We need a 4th coordinate so we can get translations to work when
     * we multiply this by our transformation matrices.  */
    private val lightPosInModelSpace = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)

    /** Used to hold the current position of the light in world space (after transformation via model matrix).  */
    private val lightPosInWorldSpace = FloatArray(4)

    /** Used to hold the transformed position of the light in eye space (after transformation via modelview matrix)  */
    private val lightPosInEyeSpace = FloatArray(4)

    private var useVertexShaderProgram = false
    /** This is a handle to our per-vertex cube shading program.  */
    private var perVertexProgramHandle = -1
    /** This is a handle to our per-pixel cube shading program.  */
    private var perPixelProgramHandle: Int = 0

    private var selectedProgramHandle: Int = 0

    private var wireFrameRenderingFlag = false
    private var renderOnlyIBO = true

    /** This is a handle to our light point program.  */
    private var pointPrograhandle: Int = 0
    /** A temporary matrix.  */
    private val temporaryMatrix = FloatArray(16)

    /** Store the accumulated rotation.  */
    private val accumulatedRotation = FloatArray(16)
    private val accumulatedTranslation = FloatArray(16)
    private val accumulatedScaling = FloatArray(16)

    /** Store the current rotation.  */
    private val incrementalRotation = FloatArray(16)
/*    private val currentTranslation = FloatArray(16)
    private val currentScaling = FloatArray(16)*/

    private lateinit var cube: Cube
    private lateinit var teapot: Teapot
    private lateinit var teapotIBO: TeapotIBO
    private lateinit var heightMap: HeightMap
    private lateinit var sphere: Sphere
    private lateinit var cylinder: Cylinder
    private lateinit var cone: Cone
    private lateinit var triangleTest: TriangleTest
    private val objFile: ObjFile = ObjFile(activity)

    override fun onSurfaceCreated(glUnused: GL10, config: EGLConfig) {
        // Set the background clear color to black.
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        // Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE)

        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        // Position the eye in front of the origin.
        val eyeX = 0.0f
        val eyeY = 0.0f
        val eyeZ = -0.5f

        // We are looking toward the distance
        val lookX = 0.0f
        val lookY = 0.0f
        val lookZ = -5.0f

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        val upX = 0.0f
        val upY = 1.0f
        val upZ = 0.0f

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ)

        var vertexShader = mXYZ.vertexShaderLesson2
        var fragmentShader = mXYZ.fragmentShaderLesson2
        var vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, vertexShader)
        var fragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)

        perVertexProgramHandle = createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
                arrayOf("a_Position", "a_Color", "a_Normal"))

        /* add in a pixel shader from lesson 3 - switchable */
        vertexShader = mXYZ.vertexShaderLesson3
        fragmentShader = mXYZ.fragmentShaderLesson3
        vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, vertexShader)
        fragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)
        perPixelProgramHandle = createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
                arrayOf("a_Position", "a_Color", "a_Normal"))

        // Define a simple shader program for our point (the orbiting light source)
        val pointVertexShader = ("uniform mat4 u_MVPMatrix;      \n"
                + "attribute vec4 a_Position;     \n"
                + "void main()                    \n"
                + "{                              \n"
                + "   gl_Position = u_MVPMatrix   \n"
                + "               * a_Position;   \n"
                + "   gl_PointSize = 5.0;         \n"
                + "}                              \n")

        val pointFragmentShader = ("precision mediump float;       \n"
                + "void main()                    \n"
                + "{                              \n"
                + "   gl_FragColor = vec4(1.0,    \n"
                + "   1.0, 1.0, 1.0);             \n"
                + "}                              \n")

        val pointVertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, pointVertexShader)
        val pointFragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, pointFragmentShader)
        pointPrograhandle = createAndLinkProgram(pointVertexShaderHandle, pointFragmentShaderHandle,
                arrayOf("a_Position"))

        /*
         * begin the geometry assortment allocations
         */
        // float color[] = new float[] { 0.5f, 0.5f, 0.0f, 0.0f };
        val nice_color = floatArrayOf(218f / 256f, 182f / 256f, 85f / 256f, 1.0f)
        val color = floatArrayOf(0.0f, 0.4f, 0.0f, 1.0f)
        val color_red = floatArrayOf(0.6f, 0.0f, 0.0f, 1.0f)
        val color_teapot_green = floatArrayOf(0f, 0.3f, 0.0f, 1.0f)
        val color_teapot_red = floatArrayOf(0.3f, 0.0f, 0.0f, 1.0f)
        val color_bright_white = floatArrayOf(0.8f, 0.8f, 0.8f, 1.0f)

        cube = Cube()
        teapot = Teapot(color_teapot_green)
        teapotIBO = TeapotIBO(color_teapot_red)
        heightMap = HeightMap()

        sphere = Sphere(
                30, // slices
                0.5f, // radius
                color_teapot_green)

        // Cylinder notes
        //   3 - slices makes a prism
        //   4 - slices makes a cube
        cylinder = Cylinder(
                30, // slices
                0.25f, // radius
                .5f, // length
                color)
        cone = Cone(
                50, // slices
                0.25f, // radius
                .5f, // length
                nice_color,
                color_red)
        triangleTest = TriangleTest()

        objFile.build_buffers(color_bright_white)

        // Initialize the modifier matrices
        Matrix.setIdentityM(accumulatedRotation, 0)
        Matrix.setIdentityM(accumulatedTranslation, 0)
        Matrix.setIdentityM(accumulatedScaling, 0)
    }

    override fun onSurfaceChanged(glUnused: GL10?, widthIn: Int, heightIn: Int) {
        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, widthIn, heightIn)
        width = widthIn
        height = heightIn

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        val ratio = width.toFloat() / height
        val left = -ratio * scaleCurrent
        val right = ratio * scaleCurrent
        val bottom = -1.0f * scaleCurrent
        val top = 1.0f * scaleCurrent
        val near = 1.0f
        val far = 20.0f
        // final float far = 5.0f;  nothing visible

        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far)

        val glError: Int = GLES20.glGetError()
        if (glError != GLES20.GL_NO_ERROR) {
            Timber.e("GLERROR: $glError")
        }
    }

    override fun onDrawFrame(glUnused: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        if (scaleCurrent != scalePrevious) {
            onSurfaceChanged(null, width, height)
            scalePrevious = scaleCurrent
        }

        // move the view as necessary if the user has shifted it manually
        Matrix.translateM(viewMatrix, 0, deltaTranslateX, deltaTranslateY, 0.0f)
        deltaTranslateX = 0.0f
        deltaTranslateY = 0.0f

        // Set our per-vertex lighting program.
        selectedProgramHandle = if (useVertexShaderProgram) {
            perVertexProgramHandle
        } else {
            perPixelProgramHandle
        }

        GLES20.glUseProgram(selectedProgramHandle)
        // Set program handles for drawing.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(selectedProgramHandle, "u_MVPMatrix")
        mMVMatrixHandle = GLES20.glGetUniformLocation(selectedProgramHandle, "u_MVMatrix")
        lightPosHandle = GLES20.glGetUniformLocation(selectedProgramHandle, "u_LightPos")
        positionHandle = GLES20.glGetAttribLocation(selectedProgramHandle, "a_Position")
        colorHandle = GLES20.glGetAttribLocation(selectedProgramHandle, "a_Color")
        normalHandle = GLES20.glGetAttribLocation(selectedProgramHandle, "a_Normal")

        Matrix.setIdentityM(lightModelMatrix, 0)
        Matrix.translateM(lightModelMatrix, 0, 0.0f, 0.0f, -1.0f)

        Matrix.multiplyMV(lightPosInWorldSpace, 0, lightModelMatrix, 0, lightPosInModelSpace, 0)
        Matrix.multiplyMV(lightPosInEyeSpace, 0, viewMatrix, 0, lightPosInWorldSpace, 0)


        //        // Obj #1 upper left
        //        Matrix.setIdentityM(modelMatrix, 0);
        //        Matrix.translateM(modelMatrix, 0, -.75f, 1.0f, -2.5f);
        //        Matrix.scaleM(modelMatrix, 0, 1.0f, 1.0f, 1.0f);
        //        do_matrix_setup();
        //        drawCylinder();

        // autoscale for the AssetObj
        val maxX = objFile.maxX
        val maxY = objFile.maxY
        val maxZ = objFile.maxZ
        val aveMax = (maxX + maxY + maxZ) / 2.0f
        val scaleF = 1.0f / aveMax
        // Obj #2 center
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, -2.5f)
        Matrix.scaleM(modelMatrix, 0, scaleF, scaleF, scaleF)
        do_matrix_setup()
        drawAssetObj()


        // Obj #9 bottom right
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 1.0f, -1.0f, -2.5f)
        Matrix.scaleM(modelMatrix, 0, 0.9f, 0.9f, 0.9f)
        do_matrix_setup()
        cone.render(positionHandle, colorHandle, normalHandle, wireFrameRenderingFlag)
    }


    private fun do_matrix_setup() {
        /*
         * Set a matrix that contains the additional *incremental* rotation
         * as indicated by the user touching the screen
         */
        Matrix.setIdentityM(incrementalRotation, 0)
        Matrix.rotateM(incrementalRotation, 0, deltaX, 0.0f, 1.0f, 0.0f)
        Matrix.rotateM(incrementalRotation, 0, deltaY, 1.0f, 0.0f, 0.0f)
        deltaX = 0.0f
        deltaY = 0.0f

        // Multiply the current rotation by the accumulated rotation, and then set the accumulated rotation to the result.
        Matrix.multiplyMM(temporaryMatrix, 0, incrementalRotation, 0, accumulatedRotation, 0)
        System.arraycopy(temporaryMatrix, 0, accumulatedRotation, 0, 16)

        // Rotate the object taking the overall rotation into account.
        Matrix.multiplyMM(temporaryMatrix, 0, modelMatrix, 0, accumulatedRotation, 0)
        System.arraycopy(temporaryMatrix, 0, modelMatrix, 0, 16)

        // This multiplies the view matrix by the model matrix, and stores
        // the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, viewMatrix, 0, modelMatrix, 0)

        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0)

        // This multiplies the modelview matrix by the projection matrix,
        // and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(temporaryMatrix, 0, projectionMatrix, 0, mMVPMatrix, 0)
        System.arraycopy(temporaryMatrix, 0, mMVPMatrix, 0, 16)

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0)

        // Pass in the light position in eye space.
        GLES20.glUniform3f(lightPosHandle, lightPosInEyeSpace[0], lightPosInEyeSpace[1], lightPosInEyeSpace[2])

        val glError: Int = GLES20.glGetError()
        if (glError != GLES20.GL_NO_ERROR) {
            Timber.e("GLERROR: $glError")
        }
    }

    /**
     * Helper function to compile a shader.
     *
     * @param shaderType The shader type.
     * @param shaderSource The shader source code.
     * @return An OpenGL handle to the shader.
     */
    private fun compileShader(shaderType: Int, shaderSource: String): Int {
        var shaderHandle = GLES20.glCreateShader(shaderType)

        if (shaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(shaderHandle, shaderSource)

            // Compile the shader.
            GLES20.glCompileShader(shaderHandle)

            // Get the compilation status.
            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0)

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                Timber.e("Error compiling shader: %s", GLES20.glGetShaderInfoLog(shaderHandle))
                GLES20.glDeleteShader(shaderHandle)
                shaderHandle = 0
            }
        }

        if (shaderHandle == 0) {
            throw RuntimeException("Error creating shader.")
        }

        return shaderHandle
    }

    /**
     * Helper function to compile and link a program.
     *
     * @param vertexShaderHandle An OpenGL handle to an already-compiled vertex shader.
     * @param fragmentShaderHandle An OpenGL handle to an already-compiled fragment shader.
     * @param attributes Attributes that need to be bound to the program.
     * @return An OpenGL handle to the program.
     */
    private fun createAndLinkProgram(vertexShaderHandle: Int, fragmentShaderHandle: Int, attributes: Array<String>?): Int {
        var programhandle = GLES20.glCreateProgram()

        if (programhandle != 0) {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(programhandle, vertexShaderHandle)

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(programhandle, fragmentShaderHandle)

            // Bind attributes
            if (attributes != null) {
                val size = attributes.size
                for (i in 0 until size) {
                    GLES20.glBindAttribLocation(programhandle, i, attributes[i])
                }
            }

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programhandle)

            // Get the link status.
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(programhandle, GLES20.GL_LINK_STATUS, linkStatus, 0)

            // If the link failed, delete the program.
            if (linkStatus[0] == 0) {
                Timber.e("Error compiling program: %s", GLES20.glGetProgramInfoLog(programhandle))
                GLES20.glDeleteProgram(programhandle)
                programhandle = 0
            }
        }

        if (programhandle == 0) {
            throw RuntimeException("Error creating program.")
        }

        return programhandle
    }

    /* asset obj */
    private fun drawAssetObj() {
        // Pass in the position information
        objFile.render(positionHandle,
                colorHandle,
                normalHandle,
                wireFrameRenderingFlag
        )
    }

    fun toggleShader() {
        if (useVertexShaderProgram) {
            useVertexShaderProgram = false
            activity.updateShaderStatus(false)
        } else {
            useVertexShaderProgram = true
            activity.updateShaderStatus(true)
        }
    }

    fun toggleWireframeFlag() {
        if (wireFrameRenderingFlag) {
            wireFrameRenderingFlag = false
            activity.updateWireframeStatus(false)
        } else {
            wireFrameRenderingFlag = true
            activity.updateWireframeStatus(true)
        }
    }

    fun toggleRenderIBOFlag() {
        if (renderOnlyIBO) {
            renderOnlyIBO = false
            activity.updateRenderOnlyIBOStatus(false)
        } else {
            renderOnlyIBO = true
            activity.updateRenderOnlyIBOStatus(true)
        }
    }

    fun loadObjFile() {
        objFile.parse(objFileName)
        // TODO: fix this hack on detecting when OPENGL is up and running
        if (perVertexProgramHandle != -1) {
            val color_bright_white = floatArrayOf(0.8f, 0.8f, 0.8f, 1.0f)
            objFile.build_buffers(color_bright_white)
        }
    }

    fun setObjFileName(name: String) {
        objFileName = name
    }

    companion object {
/*        private var scaleCount = 0
        private var shrinking = true*/
        private var height: Int = 0
        private var width: Int = 0
    }
}
