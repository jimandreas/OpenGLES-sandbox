package com.jimandreas.opengl.displayobjects

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import timber.log.Timber

import com.jimandreas.opengl.objects.BufferManager
import com.jimandreas.opengl.objects.Cone
import com.jimandreas.opengl.objects.Cube
import com.jimandreas.opengl.objects.Cylinder
import com.jimandreas.opengl.objects.Ellipse
import com.jimandreas.opengl.objects.EllipseHelix
import com.jimandreas.opengl.objects.HeightMap
import com.jimandreas.opengl.objects.Sphere
import com.jimandreas.opengl.objects.Teapot
import com.jimandreas.opengl.objects.TeapotIBO
import com.jimandreas.opengl.objects.ToroidHelix
import com.jimandreas.opengl.objects.TriangleTest
import com.jimandreas.opengl.objects.XYZ

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

    public var mTouchX : Float = 300f
    public var mTouchY : Float = 300f
    public var mScaleCurrentF : Float = 0.5f

    private lateinit var activity: ActivityDisplayObjects
    private lateinit var mGlSurfaceView: GLSurfaceView
    private val mXYZ = XYZ()
    // update to add touch control - these are set by the SurfaceView class
    // These still work without volatile, but refreshes are not guaranteed to happen.
    @Volatile
    var mDeltaX: Float = 0.toFloat()
    @Volatile
    var mDeltaY: Float = 0.toFloat()
    @Volatile
    var mDeltaTranslateX: Float = 0.toFloat()
    @Volatile
    var mDeltaTranslateY: Float = 0.toFloat()
    @Volatile
    var mScaleF = 1.0f
    @Volatile
    var mScaleDelta = 0f

    /**
     * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
     * of being located at the center of the universe) to world space.
     */
    private val mModelMatrix = FloatArray(16)

    /**
     * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
     * it positions things relative to our eye.
     */
    private val mViewMatrix = FloatArray(16)

    /**
     * Store the projection matrix. This is used to project the scene onto a 2D viewport.
     */
    private val mProjectionMatrix = FloatArray(16)

    /**
     * Allocate storage for the final combined matrix. This will be passed into the shader program.
     */
    private val mMVPMatrix = FloatArray(16)

    /**
     * Stores a copy of the model matrix specifically for the light position.
     */
    private val mLightModelMatrix = FloatArray(16)

    /**
     * This will be used to pass in the transformation matrix.
     */
    private var mMVPMatrixHandle: Int = 0

    /**
     * This will be used to pass in the modelview matrix.
     */
    private var mMVMatrixHandle: Int = 0

    /**
     * This will be used to pass in the light position.
     */
    private var mLightPosHandle: Int = 0

    /**
     * This will be used to pass in model position information.
     */
    private var mPositionHandle: Int = 0

    /**
     * This will be used to pass in model color information.
     */
    private var mColorHandle: Int = 0

    /**
     * This will be used to pass in model normal information.
     */
    private var mNormalHandle: Int = 0

    /**
     * How many bytes per float.
     */
    private val mBytesPerFloat = 4

    /**
     * Size of the position data in elements.
     */
    private val mPositionDataSize = 3

    /**
     * Size of the color data in elements.
     */
    private val mColorDataSize = 4

    /**
     * Size of the normal data in elements.
     */
    private val mNormalDataSize = 3

    /**
     * Used to hold a light centered on the origin in model space. We need a 4th coordinate so we can get translations to work when
     * we multiply this by our transformation matrices.
     */
    private val mLightPosInModelSpace = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)

    /**
     * Used to hold the current position of the light in world space (after transformation via model matrix).
     */
    private val mLightPosInWorldSpace = FloatArray(4)

    /**
     * Used to hold the transformed position of the light in eye space (after transformation via modelview matrix)
     */
    private val mLightPosInEyeSpace = FloatArray(4)

    private var mUseVertexShaderProgram = true
    /**
     * This is a handle to our per-vertex cube shading program.
     */
    private var mPerVertexProgramHandle: Int = 0
    /**
     * This is a handle to our per-pixel cube shading program.
     */
    private var mPerPixelProgramHandle: Int = 0

    private var mSelectedProgramHandle: Int = 0

    private var mWireFrameRenderingFlag = false
    private var mRenderOnlyIBO = true

    /**
     * This is a handle to our light point program.
     */
    private var mPointProgramHandle: Int = 0
    /**
     * A temporary matrix.
     */
    private val mTemporaryMatrix = FloatArray(16)

    /**
     * Store the accumulated rotation.
     */
    private val mAccumulatedRotation = FloatArray(16)
    private val mAccumulatedTranslation = FloatArray(16)
    private val mAccumulatedScaling = FloatArray(16)

    /**
     * Store the current rotation.
     */
    private val mIncrementalRotation = FloatArray(16)
    private val mCurrentTranslation = FloatArray(16)
    private val mCurrentScaling = FloatArray(16)

    private var mCube: Cube? = null
    private var mTeapot: Teapot? = null
    private var mTeapotIBO: TeapotIBO? = null
    private var mHeightMap: HeightMap? = null
    private var mSphere: Sphere? = null
    private var mCylinder: Cylinder? = null
    private var mEllipse: Ellipse? = null
    private val mEllipseHelix: EllipseHelix? = null
    private var mToroidHelix: ToroidHelix? = null
    private var mCone: Cone? = null
    private var mTriangleTest: TriangleTest? = null

    private val mBufferManager: BufferManager

    init {
        mBufferManager = BufferManager.getInstance(activity2)
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
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ)

        var vertexShader = mXYZ.vertexShaderLesson2
        var fragmentShader = mXYZ.fragmentShaderLesson2
        var vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, vertexShader)
        var fragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)

        mPerVertexProgramHandle = createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
                arrayOf("a_Position", "a_Color", "a_Normal"))

        /* add in a pixel shader from lesson 3 - switchable */
        vertexShader = mXYZ.vertexShaderLesson3
        fragmentShader = mXYZ.fragmentShaderLesson3
        vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, vertexShader)
        fragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)
        mPerPixelProgramHandle = createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
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
        mPointProgramHandle = createAndLinkProgram(pointVertexShaderHandle, pointFragmentShaderHandle,
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

        mCube = Cube()
        mTeapot = Teapot(color_teapot_green)
        mTeapotIBO = TeapotIBO(color_teapot_red)
        mHeightMap = HeightMap()

        mSphere = Sphere(
                30, // slices
                0.5f, // radius
                color_teapot_green)

        // Cylinder notes
        //   3 - slices makes a prism
        //   4 - slices makes a cube
        mCylinder = Cylinder(
                30, // slices
                0.25f, // radius
                .5f, // length
                color)

        mEllipse = Ellipse(
                30, // slices
                0.25f, // radius
                .5f, // length
                color)

        //        mEllipseHelix = new EllipseHelix(
        //                mBufferManager,
        //                10, // slices
        //                .5f, // radius
        //                .5f, // length
        //                color);


        mToroidHelix = ToroidHelix(
                mBufferManager,
                chimera_color)
        BufferManager.transferToGl()

        // commit the vertices

        mCone = Cone(
                50, // slices
                0.25f, // radius
                .5f, // length
                nice_color,
                color_red)

        mTriangleTest = TriangleTest()

        // Initialize the modifier matrices
        Matrix.setIdentityM(mAccumulatedRotation, 0)
        Matrix.setIdentityM(mAccumulatedTranslation, 0)
        Matrix.setIdentityM(mAccumulatedScaling, 0)
    }

    override fun onSurfaceChanged(glUnused: GL10?, width: Int, height: Int) {
        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height)
        mWidth = width
        mHeight = height

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        val ratio = width.toFloat() / height
        val left = -ratio * mScaleF
        val right = ratio * mScaleF
        val bottom = -1.0f * mScaleF
        val top = 1.0f * mScaleF
        val near = 1.0f
        // final float far = 20.0f;
        val far = 10.0f
        // final float far = 5.0f;  nothing visible

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far)

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
        // Matrix.rotateM(mViewMatrix, 0, 0.1f, 0.0f, 1.0f, 0.0f);

        // HACK: experiment with scaling
        // results:  view matrix does move all objects forward / backward
        //   in the view but they quickly run into the clip plane
        // (2) simple scaling of the projection matrix does nothing the model view, but
        // moves the clip plane toward the unchanged view of the model.
        // (3) reworking the logic in onSurfaceChanged does a good job of zooming in and out
        // (4) adding in a little translateM gives a good pan during the zoom
        //      the inverse translate is tricky to scale to get back to the same point.
        if (shrinking) {
            // Matrix.translateM(mViewMatrix, 0, -.011f, 0f, 0f);
            mScaleF -= 0.01f
            // onSurfaceChanged(null, mWidth, mHeight);
            if (++scaleCount > 90) {
                scaleCount = 0
                shrinking = false
            }
        } else {
            // Matrix.translateM(mViewMatrix, 0, 1f / (989f / 1000f) * .01f, 0f, 0f);
            mScaleF += 0.01f
            // onSurfaceChanged(null, mWidth, mHeight);
            if (++scaleCount > 90) {
                scaleCount = 0
                shrinking = true
            }
        }

        if (mScaleDelta != 0f) {
            mScaleF += mScaleDelta
            onSurfaceChanged(null, mWidth, mHeight)  // adjusts view
            mScaleDelta = 0f
        }

        // move the view as necessary if the user has shifted it manually
        Matrix.translateM(mViewMatrix, 0, mDeltaTranslateX, mDeltaTranslateY, 0.0f)
        mDeltaTranslateX = 0.0f
        mDeltaTranslateY = 0.0f

        // Set our per-vertex lighting program.
        if (mUseVertexShaderProgram) {
            mSelectedProgramHandle = mPerVertexProgramHandle
        } else {
            mSelectedProgramHandle = mPerPixelProgramHandle
        }

        GLES20.glUseProgram(mSelectedProgramHandle)
        // Set program handles for drawing.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mSelectedProgramHandle, "u_MVPMatrix")
        mMVMatrixHandle = GLES20.glGetUniformLocation(mSelectedProgramHandle, "u_MVMatrix")
        mLightPosHandle = GLES20.glGetUniformLocation(mSelectedProgramHandle, "u_LightPos")
        mPositionHandle = GLES20.glGetAttribLocation(mSelectedProgramHandle, "a_Position")
        mColorHandle = GLES20.glGetAttribLocation(mSelectedProgramHandle, "a_Color")
        mNormalHandle = GLES20.glGetAttribLocation(mSelectedProgramHandle, "a_Normal")

        val hack = 1 // orbit the light
        if (hack == 0) {
            // Calculate position of the light. Rotate and then push into the distance.
            Matrix.setIdentityM(mLightModelMatrix, 0)
            Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, -5.0f)
            Matrix.rotateM(mLightModelMatrix, 0, lightangleInDegrees, 0.0f, 1.0f, 0.0f)
            Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 2.0f)// original

            // HACK: makes the orbit bigger
            // Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 5.0f);

            Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0)
            Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0)

        } else { // fixed lighting position
            // Calculate position of the light. Push into the distance.
            Matrix.setIdentityM(mLightModelMatrix, 0)
            Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, -1.0f)

            Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0)
            Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0)
        }

        // GLES20.glClearDepth(1.0f);

        // Obj #1 upper left
        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.translateM(mModelMatrix, 0, -.75f, 1.0f, -2.5f)
        Matrix.scaleM(mModelMatrix, 0, 1.0f, 1.0f, 1.0f)
        do_matrix_setup()
        mCylinder!!.render(mPositionHandle, mColorHandle, mNormalHandle, mWireFrameRenderingFlag)

        // Obj #5 center
        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.translateM(mModelMatrix, 0, 0.0f, 1.0f, -2.5f)
        Matrix.scaleM(mModelMatrix, 0, .6f, .6f, .6f)
        do_matrix_setup()
        mSphere!!.render(mPositionHandle, mColorHandle, mNormalHandle, mWireFrameRenderingFlag)

        // Obj #3 upper right
        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.translateM(mModelMatrix, 0, 1.0f, .75f, -2.5f)
        Matrix.scaleM(mModelMatrix, 0, 3.5f, 3.5f, 3.5f)
        do_matrix_setup()
        mTeapotIBO!!.render(mPositionHandle, mColorHandle, mNormalHandle, mWireFrameRenderingFlag)

        // Obj #4 mid left
        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.translateM(mModelMatrix, 0, -1.0f, 0.0f, -2.5f)
        Matrix.scaleM(mModelMatrix, 0, .25f, .25f, .25f)
        do_matrix_setup()
        mCube!!.render(mPositionHandle, mColorHandle, mNormalHandle, mWireFrameRenderingFlag)

        //        // Obj #5 center
        //        Matrix.setIdentityM(mModelMatrix, 0);
        //        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -2.5f);
        //        Matrix.scaleM(mModelMatrix, 0, .6f, .6f, .6f);
        //        do_matrix_setup();
        //        // mSphere.render(mPositionHandle, mColorHandle, mNormalHandle, mWireFrameRenderingFlag);
        //
        // Obj #5 center
        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -2.5f)
        Matrix.scaleM(mModelMatrix, 0, .05f, .05f, .05f)
        // 5X large version - usefule for debugging
        // Matrix.scaleM(mModelMatrix, 0, .25f, .25f, .25f);
        do_matrix_setup()
        mBufferManager.render(mPositionHandle, mColorHandle, mNormalHandle, mWireFrameRenderingFlag)


        // Obj #6 mid right
        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.translateM(mModelMatrix, 0, 1.0f, -0.25f, -2.5f)
        Matrix.scaleM(mModelMatrix, 0, 3.5f, 3.5f, 3.5f)
        do_matrix_setup()
        if (!mRenderOnlyIBO) {
            mTeapot!!.render(mPositionHandle, mColorHandle, mNormalHandle, mWireFrameRenderingFlag)  // direct rendering
        }

        // Obj #7 bottom left
        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.translateM(mModelMatrix, 0, -1.0f, -1.0f, -2.5f)
        Matrix.scaleM(mModelMatrix, 0, .05f, .05f, .05f)
        do_matrix_setup()
        mHeightMap!!.render(mPositionHandle, mColorHandle, mNormalHandle, mWireFrameRenderingFlag)

        // Obj #2 middle
        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.translateM(mModelMatrix, 0, 0.0f, -1.0f, -2.5f)
        Matrix.scaleM(mModelMatrix, 0, 1.0f, 1.0f, 1.0f)
        do_matrix_setup()
        // mTriangleTest.render(mPositionHandle, mColorHandle, mNormalHandle, mWireFrameRenderingFlag);
        mEllipse!!.render(mPositionHandle, mColorHandle, mNormalHandle, mWireFrameRenderingFlag)

        // Obj #9 bottom right
        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.translateM(mModelMatrix, 0, 1.0f, -1.0f, -2.5f)
        Matrix.scaleM(mModelMatrix, 0, 0.9f, 0.9f, 0.9f)
        do_matrix_setup()
        mCone!!.render(mPositionHandle, mColorHandle, mNormalHandle, mWireFrameRenderingFlag)

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
        Matrix.setIdentityM(mIncrementalRotation, 0)
        Matrix.rotateM(mIncrementalRotation, 0, mDeltaX, 0.0f, 1.0f, 0.0f)
        Matrix.rotateM(mIncrementalRotation, 0, mDeltaY, 1.0f, 0.0f, 0.0f)
        mDeltaX = 0.0f
        mDeltaY = 0.0f

        // Multiply the current rotation by the accumulated rotation, and then set the accumulated rotation to the result.
        Matrix.multiplyMM(mTemporaryMatrix, 0, mIncrementalRotation, 0, mAccumulatedRotation, 0)
        System.arraycopy(mTemporaryMatrix, 0, mAccumulatedRotation, 0, 16)

        // Rotate the object taking the overall rotation into account.
        Matrix.multiplyMM(mTemporaryMatrix, 0, mModelMatrix, 0, mAccumulatedRotation, 0)
        System.arraycopy(mTemporaryMatrix, 0, mModelMatrix, 0, 16)

        // This multiplies the view matrix by the model matrix, and stores
        // the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)

        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0)

        // This multiplies the modelview matrix by the projection matrix,
        // and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mTemporaryMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0)
        System.arraycopy(mTemporaryMatrix, 0, mMVPMatrix, 0, 16)

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0)

        // Pass in the light position in eye space.
        GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2])

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
        val pointMVPMatrixHandle = GLES20.glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix")
        val pointPositionHandle = GLES20.glGetAttribLocation(mPointProgramHandle, "a_Position")

        // Pass in the position.
        GLES20.glVertexAttrib3f(pointPositionHandle, mLightPosInModelSpace[0], mLightPosInModelSpace[1], mLightPosInModelSpace[2])

        // Since we are not using a buffer object, disable vertex arrays for this attribute.
        GLES20.glDisableVertexAttribArray(pointPositionHandle)

        // Pass in the transformation matrix.
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0)
        GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mMVPMatrix, 0)

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
        var programHandle = GLES20.glCreateProgram()

        if (programHandle != 0) {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(programHandle, vertexShaderHandle)

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(programHandle, fragmentShaderHandle)

            // Bind attributes
            if (attributes != null) {
                val size = attributes.size
                for (i in 0 until size) {
                    GLES20.glBindAttribLocation(programHandle, i, attributes[i])
                }
            }

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programHandle)

            // Get the link status.
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0)

            // If the link failed, delete the program.
            if (linkStatus[0] == 0) {
                Timber.e("Error compiling program: " + GLES20.glGetProgramInfoLog(programHandle))
                GLES20.glDeleteProgram(programHandle)
                programHandle = 0
            }
        }

        if (programHandle == 0) {
            throw RuntimeException("Error creating program.")
        }

        return programHandle
    }

    fun toggleShader() {
        if (mUseVertexShaderProgram) {
            mUseVertexShaderProgram = false
            activity.updateShaderStatus(false)
        } else {
            mUseVertexShaderProgram = true
            activity.updateShaderStatus(true)
        }
    }

    fun toggleWireframeFlag() {
        if (mWireFrameRenderingFlag) {
            mWireFrameRenderingFlag = false
            activity.updateWireframeStatus(false)
        } else {
            mWireFrameRenderingFlag = true
            activity.updateWireframeStatus(true)
        }
    }

    fun toggleRenderIBOFlag() {
        if (mRenderOnlyIBO) {
            mRenderOnlyIBO = false
            activity.updateRenderOnlyIBOStatus(false)
        } else {
            mRenderOnlyIBO = true
            activity.updateRenderOnlyIBOStatus(true)
        }
    }

    companion object {

        private var scaleCount = 0
        private var shrinking = true
        private var mHeight: Int = 0
        private var mWidth: Int = 0

        var something: String = "asdf"


    }
}
