package com.jimandreas.opengl.objects;

import android.content.Context;
import android.opengl.GLES20;
import timber.log.Timber;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Buffer Manager
 * <p/>
 * set up as a Singleton class to solve the following problems App-wide:
 * <p/>
 * 1) allocates a single vertex array for accumulating triangles
 * <p/>
 * 2) allocates a single FloatArray for preparing to copy to GL.
 * <p/>
 * 3) Allocates GL buffers
 * <p/>
 * 4) Renders the GL buffers
 * <p/>
 * for more information on Android recommended Singleton Patterns, as opposed to subclassing
 * the Application class (not recommended practice) - see this link:
 * <p/>
 * http://developer.android.com/training/volley/requestqueue.html#singleton
 * <p/>
 * and List interface:
 * https://docs.oracle.com/javase/tutorial/collections/interfaces/list.html
 */
public class BufferManager {

    private static final int INITIAL_FLOAT_BUFFER_SIZE = 150000;

    private static final int POSITION_DATA_SIZE_IN_ELEMENTS = 3;
    private static final int NORMAL_DATA_SIZE_IN_ELEMENTS = 3;
    private static final int COLOR_DATA_SIZE_IN_ELEMENTS = 4;

    private static final int BYTES_PER_FLOAT = 4;
    private static final int BYTES_PER_SHORT = 2;

    private static final int STRIDE_IN_FLOATS =
            (POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS + COLOR_DATA_SIZE_IN_ELEMENTS);
    private static final int STRIDE_IN_BYTES = STRIDE_IN_FLOATS * BYTES_PER_FLOAT;

    private static final String LOG_TAG = BufferManager.class.getSimpleName();
    private static BufferManager sInstance;
    private static Context sContext;
    private static Iterator mIterator;

    private static boolean sFloatArrayAlloatedAlready = false;
    private static float[] sFloatArray;
    private static int sFloatArraySize = INITIAL_FLOAT_BUFFER_SIZE;

    public static void setFloatArrayIndex(int sFloatArrayIndex) {
        BufferManager.sFloatArrayIndex = sFloatArrayIndex;
    }

    private static int sFloatArrayIndex;
    public int getFloatArrayIndex() {
        return sFloatArrayIndex;
    }
    private static FloatBuffer sVertexDataFloatBuffer;



    /*
     * no instancing allowed
     */
    private BufferManager(Context context) {
        sContext = context;
        mGLarrayList = new ArrayList<>();
    }

    public static synchronized BufferManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new BufferManager(context);
        }
        return sInstance;
    }

    public static void allocateInitialBuffer() {
        if (sFloatArrayAlloatedAlready) {
            // TODO: reset state, erase old buffers
        }
        sFloatArray = new float[sFloatArraySize];
        if (sFloatArray == null) {
            Timber.e("Error creating initial vertex buffer.");
            throw new RuntimeException("Error creating initial vertex buffer.");
        }
// TODO: wrap the FloatBuffer
        sFloatArrayAlloatedAlready = true;
        sFloatArrayIndex = 0;
        sVertexDataFloatBuffer = ByteBuffer
                .allocateDirect(INITIAL_FLOAT_BUFFER_SIZE * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
    }

    public static float[] getFloatArray(int requestedNumFloats) {
        if (requestedNumFloats + sFloatArrayIndex < sFloatArraySize) {
            return sFloatArray;
        }

        /*
         * no space.  have to:
         * 1) copy the current float array into the float buffer
         * 2) allocate a GL buffer
         * 3) copy the float buffer into the GL buffer
         */
        transferToGl();

        /*
         * OK that is done.   Now reset the vertex data float buffer
         */
        // TODO: catch exception here
        sVertexDataFloatBuffer.limit(sFloatArraySize);
        sFloatArrayIndex = 0;
        return sFloatArray;
    }

    public static void transferToGl() {
        sVertexDataFloatBuffer.limit(sFloatArrayIndex);
        sVertexDataFloatBuffer
                .put(sFloatArray, 0, sFloatArrayIndex)
                .position(0);


        mGLarrayList.add(new GLArrayEntry());
        GLArrayEntry ae = mGLarrayList.get(sCurrentGlArrayEntry);
        GLES20.glGenBuffers(1, ae.gl_buf, 0);
        ae.numVertices = sFloatArrayIndex / STRIDE_IN_FLOATS;
        int numbytes = sFloatArrayIndex * BYTES_PER_FLOAT;

        if (ae.gl_buf[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, ae.gl_buf[0]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, numbytes,
                    sVertexDataFloatBuffer, GLES20.GL_STATIC_DRAW);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            ae.buffer_allocated = true;
        } else {
            // errorHandler(// do something );
            throw new RuntimeException("error on buffer gen");
        }

        // dumpVertexList();
    }

    /*
     * private class to track the allocated GL vertex buffers
     */
    private static class GLArrayEntry {
        int[] gl_buf;
        int numVertices;
        boolean buffer_allocated = false;
        GLArrayEntry() {
            gl_buf = new int[1];
        }
    }
    private static ArrayList<GLArrayEntry> mGLarrayList;
    private static int sCurrentGlArrayEntry = 0;

    public void render(
            int positionAttribute,
            int colorAttribute,
            int normalAttribute,
            boolean doWireframeRendering ) {

        GLArrayEntry ae;

        // GLES20.glDisable(GLES20.GL_CULL_FACE);

        for (int i = 0; i < mGLarrayList.size(); i++ ) {
            ae = mGLarrayList.get(i);
            if (ae.buffer_allocated == false) {
                continue;
            }
            if (ae.gl_buf[0] > 0) {
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, ae.gl_buf[0]);
                // associate the attributes with the bound buffer
                GLES20.glVertexAttribPointer(positionAttribute,
                        POSITION_DATA_SIZE_IN_ELEMENTS,
                        GLES20.GL_FLOAT,
                        false,
                        STRIDE_IN_BYTES,
                        0);  // offset
                GLES20.glEnableVertexAttribArray(positionAttribute);

                GLES20.glVertexAttribPointer(normalAttribute, NORMAL_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                        STRIDE_IN_BYTES, POSITION_DATA_SIZE_IN_ELEMENTS * BYTES_PER_FLOAT);
                GLES20.glEnableVertexAttribArray(normalAttribute);

                GLES20.glVertexAttribPointer(colorAttribute, COLOR_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                        STRIDE_IN_BYTES, (POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS) * BYTES_PER_FLOAT);
                GLES20.glEnableVertexAttribArray(colorAttribute);

                // Draw
                int todo;
                if (doWireframeRendering) {
                    todo = GLES20.GL_LINES;
                } else {
                    todo = GLES20.GL_TRIANGLES;
                }

                GLES20.glDrawArrays(todo, 0, ae.numVertices);

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);  // release
            }
            else {
                // errorHandler(// do something );
                throw new RuntimeException("buffer manager render: null buffer");
            }
        }

        // GLES20.glEnable(GLES20.GL_CULL_FACE);

    }
    
    private void dumpVertexList() {
    /*
     * DEBUG:
     * optional vertex printout
     */
    float nvx, nvy, nvz;
    float vx, vy, vz;
    int i;
    for (i = 0; i < sFloatArrayIndex; i += STRIDE_IN_FLOATS) {
        vx = sFloatArray[i + 0];
        vy = sFloatArray[i + 1];
        vz = sFloatArray[i + 2];
        String svx = String.format("%6.2f", vx);
        String svy = String.format("%6.2f", vy);
        String svz = String.format("%6.2f", vz);

        nvx = sFloatArray[i + 3];
        nvy = sFloatArray[i + 4];
        nvz = sFloatArray[i + 5];
        String snvx = String.format("%6.2f", nvx);
        String snvy = String.format("%6.2f", nvy);
        String snvz = String.format("%6.2f", nvz);

        Timber.i("vert " + i + " x y z nx ny nz "
                        + svx + " " + svy + " " + svz + " and " + snvx + " " + snvy + " " + snvz
//                    + " clr "
//                    + vertexData[i + 6] + " " + vertexData[i + 7] + " "
//                    + vertexData[i + 8] + " " + vertexData[i + 8]
        );
    }
    }
}
