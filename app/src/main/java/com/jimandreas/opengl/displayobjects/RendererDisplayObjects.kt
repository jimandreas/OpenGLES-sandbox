package com.jimandreas.opengl.displayobjects

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import com.jimandreas.opengl.objects.*
import timber.log.Timber
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/*
 * notes : the teapot needs fixing but the Kronos data also had issues with direct rendering...
 */
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
class RendererDisplayObjects(activity2: ActivityDisplayObjects) : GLSurfaceView.Renderer {

    public var touchX : Float = 300f
    public var touchY : Float = 300f
    public var scaleCurrentF : Float = 0.5f

    private lateinit var activity: ActivityDisplayObjects
    private lateinit var glSurfaceView: GLSurfaceView

    // update to add touch control - these are set by the SurfaceView class
    // These still work without volatile, but refreshes are not guaranteed to happen.
    @Volatile
    var deltaX: Float = 0.toFloat()
    @Volatile
    var deltaY: Float = 0.toFloat()
    @Volatile
    var deltaTranslateX: Float = 0.toFloat()
    @Volatile
    var deltaTranslateY: Float = 0.toFloat()
    @Volatile
    var scaleF = 1.0f
    @Volatile
    var scaleDelta = 0f

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

    /**
     * Store the projection matrix. This is used to project the scene onto a 2D viewport.
     */
    private val projectionMatrix = FloatArray(16)

    /**
     * Allocate storage for the final combined matrix. This will be passed into the shader program.
     */
    private val MVPMatrix = FloatArray(16)

    /**
     * Stores a copy of the model matrix specifically for the light position.
     */
    private val lightModelMatrix = FloatArray(16)

    /**
     * This will be used to pass in the transformation matrix.
     */
    private var MVPMatrixHandle: Int = 0

    /**
     * This will be used to pass in the modelview matrix.
     */
    private var MVMatrixHandle: Int = 0

    /**
     * This will be used to pass in the light position.
     */
    private var lightPosHandle: Int = 0

    /**
     * This will be used to pass in model position information.
     */
    private var positionHandle: Int = 0

    /**
     * This will be used to pass in model color information.
     */
    private var colorHandle: Int = 0

    /**
     * This will be used to pass in model normal information.
     */
    private var normalHandle: Int = 0

    /**
     * How many bytes per float.
     */
    private val bytesPerFloat = 4

    /**
     * Size of the position data in elements.
     */
    private val positionDataSize = 3

    /**
     * Size of the color data in elements.
     */
    private val colorDataSize = 4

    /**
     * Size of the normal data in elements.
     */
    private val normalDataSize = 3

    /**
     * Used to hold a light centered on the origin in model space. We need a 4th coordinate so we can get translations to work when
     * we multiply this by our transformation matrices.
     */
    private val lightPosInModelSpace = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)

    /**
     * Used to hold the current position of the light in world space (after transformation via model matrix).
     */
    private val lightPosInWorldSpace = FloatArray(4)

    /**
     * Used to hold the transformed position of the light in eye space (after transformation via modelview matrix)
     */
    private val lightPosInEyeSpace = FloatArray(4)

    private var useVertexShaderProgram = true
    /**
     * This is a handle to our per-vertex cube shading program.
     */
    private var perVertexProgramHandle: Int = 0
    /**
     * This is a handle to our per-pixel cube shading program.
     */
    private var perPixelProgramHandle: Int = 0

    private var selectedPrograhandle: Int = 0

    private var wireFrameRenderingFlag = false
    private var renderOnlyIBO = true

    /**
     * This is a handle to our light point program.
     */
    private var pointProgramHandle: Int = 0
    /**
     * A temporary matrix.
     */
    private val temporaryMatrix = FloatArray(16)

    /**
     * Store the accumulated rotation.
     */
    private val accumulatedRotation = FloatArray(16)
    private val accumulatedTranslation = FloatArray(16)
    private val accumulatedScaling = FloatArray(16)

    /**
     * Store the current rotation.
     */
    private val incrementalRotation = FloatArray(16)
    private val currentTranslation = FloatArray(16)
    private val currentScaling = FloatArray(16)

    private var cube: Cube? = null
    private var teapot: Teapot? = null
    private var teapotIBO: TeapotIBO? = null
    private var heightMap: HeightMap? = null
    private var sphere: Sphere? = null
    private var cylinder: Cylinder? = null
    private var ellipse: Ellipse? = null
    private val ellipseHelix: EllipseHelix? = null
    private var toroidHelix: ToroidHelix? = null
    private var cone: Cone? = null
    private var triangleTest: TriangleTest? = null

    private val bufferManager: BufferManager

    init {
        bufferManager = BufferManager.getInstance(activity2)
        activity = activity2
        BufferManager.allocateInitialBuffer()
    }

    override fun onSurfaceCreated(glUnused: GL10, config: EGLConfig) {

        // Set the background clear color to black.
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        // Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE)

        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        val glError: Int
        glError = GLES20.glGetError()
        if (glError != GLES20.GL_NO_ERROR) {
            Timber.e("GLERROR: $glError")
        }
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

        var vertexShader = XYZ().vertexShaderLesson2
        var fragmentShader = XYZ().fragmentShaderLesson2
        var vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, vertexShader)
        var fragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)

        perVertexProgramHandle = createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
                arrayOf("a_Position", "a_Color", "a_Normal"))

        /* add in a pixel shader from lesson 3 - switchable */
        vertexShader = XYZ().vertexShaderLesson3
        fragmentShader = XYZ().fragmentShaderLesson3
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
        pointProgramHandle = createAndLinkProgram(pointVertexShaderHandle, pointFragmentShaderHandle,
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
        val chimera_color = floatArrayOf(229f / 256f, 196f / 256f, 153f / 256f, 1.0f)

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

        ellipse = Ellipse(
                30, // slices
                0.25f, // radius
                .5f, // length
                color)

        //        ellipseHelix = new EllipseHelix(
        //                bufferManager,
        //                10, // slices
        //                .5f, // radius
        //                .5f, // length
        //                color);


        toroidHelix = ToroidHelix(
                bufferManager,
                chimera_color)
        BufferManager.transferToGl()

        // commit the vertices

        cone = Cone(
                50, // slices
                0.25f, // radius
                .5f, // length
                nice_color,
                color_red)

        triangleTest = TriangleTest()

        // Initialize the modifier matrices
        Matrix.setIdentityM(accumulatedRotation, 0)
        Matrix.setIdentityM(accumulatedTranslation, 0)
        Matrix.setIdentityM(accumulatedScaling, 0)
    }

    override fun onSurfaceChanged(glUnused: GL10?, widthIn: Int, heightIn: Int) {
        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height)
        width = widthIn
        height = heightIn

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        val ratio = width.toFloat() / height
        val left = -ratio * scaleF
        val right = ratio * scaleF
        val bottom = -1.0f * scaleF
        val top = 1.0f * scaleF
        val near = 1.0f
        // final float far = 20.0f;
        val far = 10.0f
        // final float far = 5.0f;  nothing visible

        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far)

        val glError: Int
        glError = GLES20.glGetError()
        if (glError != GLES20.GL_NO_ERROR) {
            Timber.e("GLERROR: $glError")
        }
    }

    override fun onDrawFrame(glUnused: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Do a complete rotation every 10 seconds.
        val time = SystemClock.uptimeMillis() % 10000L
        val angleInDegrees = 360.0f / 10000.0f * time.toInt()
        val lightangleInDegrees = 360.0f / 7000.0f * (SystemClock.uptimeMillis() % 7000L).toInt()

        // lightangleInDegrees = 0f;

        // this does a nice job of rotating the camera slowly to the right
        // (scene shifts circularly to the left)
        // HACK:
        // Matrix.rotateM(viewMatrix, 0, 0.1f, 0.0f, 1.0f, 0.0f);

        // HACK: experiment with scaling
        // results:  view matrix does move all objects forward / backward
        //   in the view but they quickly run into the clip plane
        // (2) simple scaling of the projection matrix does nothing the model view, but
        // moves the clip plane toward the unchanged view of the model.
        // (3) reworking the logic in onSurfaceChanged does a good job of zooming in and out
        // (4) adding in a little translateM gives a good pan during the zoom
        //      the inverse translate is tricky to scale to get back to the same point.
        if (shrinking) {
            // Matrix.translateM(viewMatrix, 0, -.011f, 0f, 0f);
            scaleF -= 0.01f
            // onSurfaceChanged(null, width, height);
            if (++scaleCount > 90) {
                scaleCount = 0
                shrinking = false
            }
        } else {
            // Matrix.translateM(viewMatrix, 0, 1f / (989f / 1000f) * .01f, 0f, 0f);
            scaleF += 0.01f
            // onSurfaceChanged(null, width, height);
            if (++scaleCount > 90) {
                scaleCount = 0
                shrinking = true
            }
        }

        if (scaleDelta != 0f) {
            scaleF += scaleDelta
            onSurfaceChanged(null, width, height)  // adjusts view
            scaleDelta = 0f
        }

        // move the view as necessary if the user has shifted it manually
        Matrix.translateM(viewMatrix, 0, deltaTranslateX, deltaTranslateY, 0.0f)
        deltaTranslateX = 0.0f
        deltaTranslateY = 0.0f

        // Set our per-vertex lighting program.
        if (useVertexShaderProgram) {
            selectedPrograhandle = perVertexProgramHandle
        } else {
            selectedPrograhandle = perPixelProgramHandle
        }

        GLES20.glUseProgram(selectedPrograhandle)
        // Set program handles for drawing.
        MVPMatrixHandle = GLES20.glGetUniformLocation(selectedPrograhandle, "u_MVPMatrix")
        MVMatrixHandle = GLES20.glGetUniformLocation(selectedPrograhandle, "u_MVMatrix")
        lightPosHandle = GLES20.glGetUniformLocation(selectedPrograhandle, "u_LightPos")
        positionHandle = GLES20.glGetAttribLocation(selectedPrograhandle, "a_Position")
        colorHandle = GLES20.glGetAttribLocation(selectedPrograhandle, "a_Color")
        normalHandle = GLES20.glGetAttribLocation(selectedPrograhandle, "a_Normal")

        val hack = 1 // orbit the light
        if (hack == 0) {
            // Calculate position of the light. Rotate and then push into the distance.
            Matrix.setIdentityM(lightModelMatrix, 0)
            Matrix.translateM(lightModelMatrix, 0, 0.0f, 0.0f, -5.0f)
            Matrix.rotateM(lightModelMatrix, 0, lightangleInDegrees, 0.0f, 1.0f, 0.0f)
            Matrix.translateM(lightModelMatrix, 0, 0.0f, 0.0f, 2.0f)// original

            // HACK: makes the orbit bigger
            // Matrix.translateM(lightModelMatrix, 0, 0.0f, 0.0f, 5.0f);

            Matrix.multiplyMV(lightPosInWorldSpace, 0, lightModelMatrix, 0, lightPosInModelSpace, 0)
            Matrix.multiplyMV(lightPosInEyeSpace, 0, viewMatrix, 0, lightPosInWorldSpace, 0)

        } else { // fixed lighting position
            // Calculate position of the light. Push into the distance.
            Matrix.setIdentityM(lightModelMatrix, 0)
            Matrix.translateM(lightModelMatrix, 0, 0.0f, 0.0f, -1.0f)

            Matrix.multiplyMV(lightPosInWorldSpace, 0, lightModelMatrix, 0, lightPosInModelSpace, 0)
            Matrix.multiplyMV(lightPosInEyeSpace, 0, viewMatrix, 0, lightPosInWorldSpace, 0)
        }

        // GLES20.glClearDepth(1.0f);

        // Obj #1 upper left
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, -.75f, 1.0f, -2.5f)
        Matrix.scaleM(modelMatrix, 0, 1.0f, 1.0f, 1.0f)
        do_matrix_setup()
        cylinder!!.render(positionHandle, colorHandle, normalHandle, wireFrameRenderingFlag)

        // Obj #5 center
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0.0f, 1.0f, -2.5f)
        Matrix.scaleM(modelMatrix, 0, .6f, .6f, .6f)
        do_matrix_setup()
        sphere!!.render(positionHandle, colorHandle, normalHandle, wireFrameRenderingFlag)

        // Obj #3 upper right
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 1.0f, .75f, -2.5f)
        Matrix.scaleM(modelMatrix, 0, 3.5f, 3.5f, 3.5f)
        do_matrix_setup()
        teapotIBO!!.render(positionHandle, colorHandle, normalHandle, wireFrameRenderingFlag)

        // Obj #4 mid left
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, -1.0f, 0.0f, -2.5f)
        Matrix.scaleM(modelMatrix, 0, .25f, .25f, .25f)
        do_matrix_setup()
        cube!!.render(positionHandle, colorHandle, normalHandle, wireFrameRenderingFlag)

        //        // Obj #5 center
        //        Matrix.setIdentityM(modelMatrix, 0);
        //        Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, -2.5f);
        //        Matrix.scaleM(modelMatrix, 0, .6f, .6f, .6f);
        //        do_matrix_setup();
        //        // sphere.render(positionHandle, colorHandle, normalHandle, wireFrameRenderingFlag);
        //
        // Obj #5 center
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, -2.5f)
        Matrix.scaleM(modelMatrix, 0, .05f, .05f, .05f)
        // 5X large version - usefule for debugging
        // Matrix.scaleM(modelMatrix, 0, .25f, .25f, .25f);
        do_matrix_setup()
        bufferManager.render(positionHandle, colorHandle, normalHandle, wireFrameRenderingFlag)


        // Obj #6 mid right
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 1.0f, -0.25f, -2.5f)
        Matrix.scaleM(modelMatrix, 0, 3.5f, 3.5f, 3.5f)
        do_matrix_setup()
        if (!renderOnlyIBO) {
            teapot!!.render(positionHandle, colorHandle, normalHandle, wireFrameRenderingFlag)  // direct rendering
        }

        // Obj #7 bottom left
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, -1.0f, -1.0f, -2.5f)
        Matrix.scaleM(modelMatrix, 0, .05f, .05f, .05f)
        do_matrix_setup()
        heightMap!!.render(positionHandle, colorHandle, normalHandle, wireFrameRenderingFlag)

        // Obj #2 middle
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0.0f, -1.0f, -2.5f)
        Matrix.scaleM(modelMatrix, 0, 1.0f, 1.0f, 1.0f)
        do_matrix_setup()
        // triangleTest.render(positionHandle, colorHandle, normalHandle, wireFrameRenderingFlag);
        ellipse!!.render(positionHandle, colorHandle, normalHandle, wireFrameRenderingFlag)

        // Obj #9 bottom right
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 1.0f, -1.0f, -2.5f)
        Matrix.scaleM(modelMatrix, 0, 0.9f, 0.9f, 0.9f)
        do_matrix_setup()
        cone!!.render(positionHandle, colorHandle, normalHandle, wireFrameRenderingFlag)

        val glError: Int
        glError = GLES20.glGetError()
        if (glError != GLES20.GL_NO_ERROR) {
            Timber.e("GLERROR: $glError")
        }
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
        Matrix.multiplyMM(MVPMatrix, 0, viewMatrix, 0, modelMatrix, 0)

        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(MVMatrixHandle, 1, false, MVPMatrix, 0)

        // This multiplies the modelview matrix by the projection matrix,
        // and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(temporaryMatrix, 0, projectionMatrix, 0, MVPMatrix, 0)
        System.arraycopy(temporaryMatrix, 0, MVPMatrix, 0, 16)

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(MVPMatrixHandle, 1, false, MVPMatrix, 0)

        // Pass in the light position in eye space.
        GLES20.glUniform3f(lightPosHandle, lightPosInEyeSpace[0], lightPosInEyeSpace[1], lightPosInEyeSpace[2])

        val glError: Int
        glError = GLES20.glGetError()
        if (glError != GLES20.GL_NO_ERROR) {
            Timber.e("GLERROR: $glError")
        }
    }

    /**
     * Draws a point representing the position of the light.
     */
    private fun drawLight() {
        val pointMVPMatrixHandle = GLES20.glGetUniformLocation(pointProgramHandle, "u_MVPMatrix")
        val pointPositionHandle = GLES20.glGetAttribLocation(pointProgramHandle, "a_Position")

        // Pass in the position.
        GLES20.glVertexAttrib3f(pointPositionHandle, lightPosInModelSpace[0], lightPosInModelSpace[1], lightPosInModelSpace[2])

        // Since we are not using a buffer object, disable vertex arrays for this attribute.
        GLES20.glDisableVertexAttribArray(pointPositionHandle)

        // Pass in the transformation matrix.
        Matrix.multiplyMM(MVPMatrix, 0, viewMatrix, 0, lightModelMatrix, 0)
        Matrix.multiplyMM(MVPMatrix, 0, projectionMatrix, 0, MVPMatrix, 0)
        GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, MVPMatrix, 0)

        // Draw the point.
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1)
    }

    /**
     * Helper function to compile a shader.
     *
     * @param shaderType   The shader type.
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
                Timber.e("Error compiling shader: " + GLES20.glGetShaderInfoLog(shaderHandle))
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
     * @param vertexShaderHandle   An OpenGL handle to an already-compiled vertex shader.
     * @param fragmentShaderHandle An OpenGL handle to an already-compiled fragment shader.
     * @param attributes           Attributes that need to be bound to the program.
     * @return An OpenGL handle to the program.
     */
    private fun createAndLinkProgram(vertexShaderHandle: Int, fragmentShaderHandle: Int, attributes: Array<String>?): Int {
        var prograhandle = GLES20.glCreateProgram()

        if (prograhandle != 0) {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(prograhandle, vertexShaderHandle)

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(prograhandle, fragmentShaderHandle)

            // Bind attributes
            if (attributes != null) {
                val size = attributes.size
                for (i in 0 until size) {
                    GLES20.glBindAttribLocation(prograhandle, i, attributes[i])
                }
            }

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(prograhandle)

            // Get the link status.
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(prograhandle, GLES20.GL_LINK_STATUS, linkStatus, 0)

            // If the link failed, delete the program.
            if (linkStatus[0] == 0) {
                Timber.e("Error compiling program: %s", GLES20.glGetProgramInfoLog(prograhandle))
                GLES20.glDeleteProgram(prograhandle)
                prograhandle = 0
            }
        }

        if (prograhandle == 0) {
            throw RuntimeException("Error creating program.")
        }

        return prograhandle
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

    companion object {

        private var scaleCount = 0
        private var shrinking = true
        private var height: Int = 0
        private var width: Int = 0

        var something: String = "asdf"


    }
}
