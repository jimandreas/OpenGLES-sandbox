package com.jimandreas.opengl.objects;


import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class TriangleTest {

    private static final int POSITION_DATA_SIZE_IN_ELEMENTS = 3;
    private static final int NORMAL_DATA_SIZE_IN_ELEMENTS = 3;
    private static final int COLOR_DATA_SIZE_IN_ELEMENTS = 4;

    private static final int BYTES_PER_FLOAT = 4;
    private static final int BYTES_PER_SHORT = 2;

    private static final int STRIDE_IN_ELEMENTS =
            (POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS + COLOR_DATA_SIZE_IN_ELEMENTS);

    private static final int STRIDE_IN_BYTES = STRIDE_IN_ELEMENTS * BYTES_PER_FLOAT;

    /** Size of the position data in elements. */
    private final int mPositionDataSize = 3;

    /** Size of the color data in elements. */
    private final int mColorDataSize = 4;

    /** Size of the normal data in elements. */
    private final int mNormalDataSize = 3;

    static final int TRIANGLES_PER_STRIP = 3;
    static final int NUM_STRIPS = 1;
    static final float MIN_POSITION = -1f;
    static final float POSITION_RANGE = 2f;

    final int[] vbo = new int[1];
    final int[] ibo = new int[1];

    int indexCount;

    // simplify to one triangle strip
    public TriangleTest() {
        final float[] heightMapVertexData = new float[6 * STRIDE_IN_ELEMENTS];
        int offset = 0;
        float out_from_z = 3.0f;
        float greenValue = 0.7f;
        
        // Position 1
        heightMapVertexData[offset++] = 0f;
        heightMapVertexData[offset++] = 1f;
        heightMapVertexData[offset++] = 0f;
        // normal
        heightMapVertexData[offset++] = 0f;
        heightMapVertexData[offset++] = 0f;
        heightMapVertexData[offset++] = out_from_z;
        // teapot green
        heightMapVertexData[offset++] = 0f;
        heightMapVertexData[offset++] = greenValue;
        heightMapVertexData[offset++] = 0f;
        heightMapVertexData[offset++] = 1f;

// Position 2
        heightMapVertexData[offset++] = .5f;
        heightMapVertexData[offset++] = 1f;
        heightMapVertexData[offset++] = 0f;
        // normal
        heightMapVertexData[offset++] = 0f;
        heightMapVertexData[offset++] = 0f;
        heightMapVertexData[offset++] = out_from_z;
        // teapot green
        heightMapVertexData[offset++] = 0f;
        heightMapVertexData[offset++] = greenValue;
        heightMapVertexData[offset++] = 0f;
        heightMapVertexData[offset++] = 1f;

// Position 3
        heightMapVertexData[offset++] = 1f;
        heightMapVertexData[offset++] = 1f;
        heightMapVertexData[offset++] = 0f;
        // normal
        heightMapVertexData[offset++] = 0f;
        heightMapVertexData[offset++] = 0f;
        heightMapVertexData[offset++] = out_from_z;
        // teapot green
        heightMapVertexData[offset++] = 0f;
        heightMapVertexData[offset++] = greenValue;
        heightMapVertexData[offset++] = 0f;
        heightMapVertexData[offset++] = 1f;

// Position 4
        heightMapVertexData[offset++] = 0f;
        heightMapVertexData[offset++] = .6f;
        heightMapVertexData[offset++] = 0f;
        // normal
        heightMapVertexData[offset++] = 0f;
        heightMapVertexData[offset++] = 0f;
        heightMapVertexData[offset++] = out_from_z;
        // teapot green
        heightMapVertexData[offset++] = 0f;
        heightMapVertexData[offset++] = greenValue;
        heightMapVertexData[offset++] = 0f;
        heightMapVertexData[offset++] = 1f;

// Position 5
        heightMapVertexData[offset++] = .5f;
        heightMapVertexData[offset++] = .6f;
        heightMapVertexData[offset++] = 0f;
        // normal
        heightMapVertexData[offset++] = 0f;
        heightMapVertexData[offset++] = 0f;
        heightMapVertexData[offset++] = out_from_z;
        // teapot green
        heightMapVertexData[offset++] = 0f;
        heightMapVertexData[offset++] = greenValue;
        heightMapVertexData[offset++] = 0f;
        heightMapVertexData[offset++] = 1f;

// Position 6
        heightMapVertexData[offset++] = 1f;
        heightMapVertexData[offset++] = .6f;
        heightMapVertexData[offset++] = 0f;
        // normal
        heightMapVertexData[offset++] = 0f;
        heightMapVertexData[offset++] = 0f;
        heightMapVertexData[offset++] = out_from_z;
        // teapot green
        heightMapVertexData[offset++] = 0f;
        heightMapVertexData[offset++] = greenValue;
        heightMapVertexData[offset++] = 0f;
        heightMapVertexData[offset++] = 1f;

        final short[] indexData = new short[6];
        offset = 0;


        indexData[offset++] = 0;
        indexData[offset++] = 3;
        indexData[offset++] = 1;
        indexData[offset++] = 4;
        indexData[offset++] = 2;
        indexData[offset++] = 5;

        indexCount = indexData.length;

        final FloatBuffer heightMapVertexDataBuffer = ByteBuffer
                .allocateDirect(heightMapVertexData.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        heightMapVertexDataBuffer.put(heightMapVertexData).position(0);

        final ShortBuffer heightMapIndexDataBuffer = ByteBuffer
                .allocateDirect(indexData.length * BYTES_PER_SHORT).order(ByteOrder.nativeOrder())
                .asShortBuffer();
        heightMapIndexDataBuffer.put(indexData).position(0);

        GLES20.glGenBuffers(1, vbo, 0);
        GLES20.glGenBuffers(1, ibo, 0);

        if (vbo[0] > 0 && ibo[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, heightMapVertexDataBuffer.capacity() * BYTES_PER_FLOAT,
                    heightMapVertexDataBuffer, GLES20.GL_STATIC_DRAW);

            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, heightMapIndexDataBuffer.capacity()
                    * BYTES_PER_SHORT, heightMapIndexDataBuffer, GLES20.GL_STATIC_DRAW);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        } else {
            // errorHandler.handleError(ErrorHandler.ErrorType.BUFFER_CREATION_ERROR, "glGenBuffers");
            throw new RuntimeException("error on buffer gen");
        }
    
    }

    public void render(
            int positionAttribute,
            int colorAttribute,
            int normalAttribute) {

        if (vbo[0] > 0 && ibo[0] > 0) {

            // Use culling to remove back faces.
            // GLES20.glDisable(GLES20.GL_CULL_FACE);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);

            // Bind Attributes
            GLES20.glVertexAttribPointer(positionAttribute, POSITION_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                    STRIDE_IN_BYTES, 0);
            GLES20.glEnableVertexAttribArray(positionAttribute);

            GLES20.glVertexAttribPointer(normalAttribute, NORMAL_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                    STRIDE_IN_BYTES, POSITION_DATA_SIZE_IN_ELEMENTS * BYTES_PER_FLOAT);
            GLES20.glEnableVertexAttribArray(normalAttribute);

            GLES20.glVertexAttribPointer(colorAttribute, COLOR_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                    STRIDE_IN_BYTES, (POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS) * BYTES_PER_FLOAT);
            GLES20.glEnableVertexAttribArray(colorAttribute);

            // Draw
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
            GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, indexCount, GLES20.GL_UNSIGNED_SHORT, 0);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

            // Use culling to remove back faces.
            // GLES20.glEnable(GLES20.GL_CULL_FACE);
        }
    }

    public void release() {
        if (vbo[0] > 0) {
            GLES20.glDeleteBuffers(vbo.length, vbo, 0);
            vbo[0] = 0;
        }

        if (ibo[0] > 0) {
            GLES20.glDeleteBuffers(ibo.length, ibo, 0);
            ibo[0] = 0;
        }
    }
}
