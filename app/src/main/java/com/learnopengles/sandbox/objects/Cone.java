package com.learnopengles.sandbox.objects;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Cone {
    private static final int POSITION_DATA_SIZE_IN_ELEMENTS = 3;
    private static final int NORMAL_DATA_SIZE_IN_ELEMENTS = 3;
    private static final int COLOR_DATA_SIZE_IN_ELEMENTS = 4;

    private static final int BYTES_PER_FLOAT = 4;
    private static final int BYTES_PER_SHORT = 2;

    private static final int STRIDE_IN_FLOATS = (POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS + COLOR_DATA_SIZE_IN_ELEMENTS);
    private static final int STRIDE_IN_BYTES = STRIDE_IN_FLOATS * BYTES_PER_FLOAT;

    /** Size of the position data in elements. */
    private final int mPositionDataSize = 3;

    /** Size of the color data in elements. */
    private final int mColorDataSize = 4;

    /** Size of the normal data in elements. */
    private final int mNormalDataSize = 3;

    private FloatBuffer mVertices;
    private FloatBuffer mNormals;
    private FloatBuffer mTexCoords;
    private ShortBuffer mIndices;
    private int mNumIndices;

    final int[] vbo_top = new int[1];
    final int[] vbo_bottom = new int[1];
    // final int[] ibo = new int[1];

    public Cone(int numSlices,
                float radius, float length,
                float[] color,
                float[] base_color /*RGBA*/ ) {
        int i, j;
        int offset = 0;
        float vx, vy, vz;
        float angleStep = ((2.0f * (float) Math.PI) / numSlices);
        final float[] vertexData = new float[(numSlices+1) * STRIDE_IN_BYTES];

        // top point of cone
        vertexData[offset++] = 0f;  // center of circle
        vertexData[offset++] = length/2.0f;
        vertexData[offset++] = 0f;
        // normal vector
        vertexData[offset++] = 1f; // normal is straight up
        vertexData[offset++] = 0f;
        vertexData[offset++] = 0f;
        // color value
        vertexData[offset++] = color[0];
        vertexData[offset++] = color[1];
        vertexData[offset++] = color[2];
        vertexData[offset++] = color[3];
        
        for (i = 0; i <= numSlices+1; i++) {
            float angleInRadians =
                    ((float) i / (float) numSlices)
                            * ((float) Math.PI * 2f);

            vertexData[offset++] = radius * (float)Math.cos(angleInRadians);
            vertexData[offset++] = -length/2.0f;
            vertexData[offset++] = radius * (float)Math.sin(angleInRadians);
            // normal vector
            vertexData[offset++] = -(float)Math.cos(angleInRadians)/radius;
            vertexData[offset++] = 0f;
            vertexData[offset++] = -(float)Math.sin(angleInRadians)/radius;;
            // color value
            vertexData[offset++] = color[0];
            vertexData[offset++] = color[1];
            vertexData[offset++] = color[2];
            vertexData[offset++] = color[3];
            
        }

//        for (i = 0; i < ((numSlices + 2) * STRIDE_IN_FLOATS); i+= STRIDE_IN_FLOATS) {
//            vx = vertexData[i + 0];
//            vy = vertexData[i + 1];
//            vz = vertexData[i + 2];
//            String svx = String.format("%.2f", vx);
//            String svy = String.format("%.2f", vy);
//            String svz = String.format("%.2f", vz);
//
//            Timber("cyl", " x y z "
//                    + svx + " " + svy + " " + svz + " and "
//                    + vertexData[i + 6] + " " + vertexData[i + 7] + " " + vertexData[i + 8]);
//        }
        mNumIndices = numSlices + 1;

        final FloatBuffer sphereVertexDataBuffer = ByteBuffer
                .allocateDirect(vertexData.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        sphereVertexDataBuffer.put(vertexData).position(0);

        GLES20.glGenBuffers(1, vbo_top, 0);

        if (vbo_top[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_top[0]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, sphereVertexDataBuffer.capacity() * BYTES_PER_FLOAT,
                    sphereVertexDataBuffer, GLES20.GL_STATIC_DRAW);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        } else {
            // errorHandler.handleError(ErrorHandler.ErrorType.BUFFER_CREATION_ERROR, "glGenBuffers");
            throw new RuntimeException("error on buffer gen");
        }

        /* 
         * *** bottom circular plate of cone ***
         */

        offset = 0;
        vertexData[offset++] = 0f;  // center of circle
        vertexData[offset++] = -length/2.0f;
        vertexData[offset++] = 0f;
        // normal vector
        vertexData[offset++] = 0f;
        vertexData[offset++] = 3f;
        vertexData[offset++] = 0f;
        // color value
        vertexData[offset++] = base_color[0];
        vertexData[offset++] = base_color[1];
        vertexData[offset++] = base_color[2];
        vertexData[offset++] = base_color[3];

        for (i = 0; i <= numSlices+1; i++) {
            float angleInRadians =
                    ((float) i / (float) numSlices)
                            * ((float) Math.PI * 2f);

            vertexData[offset++] = radius * (float)Math.cos(angleInRadians);
            vertexData[offset++] = -length/2.0f;
            vertexData[offset++] = radius * -(float)Math.sin(angleInRadians);
            // normal vector
            vertexData[offset++] = 0f;
            vertexData[offset++] = 3f;
            vertexData[offset++] = 0f;
            // color value
            vertexData[offset++] = base_color[0];
            vertexData[offset++] = base_color[1];
            vertexData[offset++] = base_color[2];
            vertexData[offset++] = base_color[3];

        }

//        for (i = 0; i < ((numSlices + 2) * STRIDE_IN_FLOATS); i+= STRIDE_IN_FLOATS) {
//            vx = vertexData[i + 0];
//            vy = vertexData[i + 1];
//            vz = vertexData[i + 2];
//            String svx = String.format("%.2f", vx);
//            String svy = String.format("%.2f", vy);
//            String svz = String.format("%.2f", vz);
//
//            Timber("cyl", " x y z "
//                    + svx + " " + svy + " " + svz + " and "
//                    + vertexData[i + 6] + " " + vertexData[i + 7] + " " + vertexData[i + 8]);
//        }
        mNumIndices = numSlices + 1;

        final FloatBuffer sphereVertexDataBufferBottom = ByteBuffer
                .allocateDirect(vertexData.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        sphereVertexDataBufferBottom.put(vertexData).position(0);

        GLES20.glGenBuffers(1, vbo_bottom, 0);

        if (vbo_bottom[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_bottom[0]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, sphereVertexDataBufferBottom.capacity() * BYTES_PER_FLOAT,
                    sphereVertexDataBufferBottom, GLES20.GL_STATIC_DRAW);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        } else {
            // errorHandler.handleError(ErrorHandler.ErrorType.BUFFER_CREATION_ERROR, "glGenBuffers");
            throw new RuntimeException("error on buffer gen");
        }
    }

    public void render(
            int positionAttribute,
            int colorAttribute,
            int normalAttribute,
            boolean doWireframeRendering ) {

        // Draw
        int todo;
        if (doWireframeRendering) {
            todo = GLES20.GL_LINES;
        } else {
            todo = GLES20.GL_TRIANGLE_FAN;
        }

        if (vbo_bottom[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_bottom[0]);

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

            // Draw - no indexes
            GLES20.glDrawArrays(todo, 0, mNumIndices+2);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        }

        if (doWireframeRendering) {
            todo = GLES20.GL_LINES;
        } else {
            todo = GLES20.GL_TRIANGLE_FAN;
        }
        if (vbo_top[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_top[0]);

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

            // Draw - no indexes
            // GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
            // GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, mNumIndices, GLES20.GL_UNSIGNED_SHORT, 0);
            GLES20.glDrawArrays(todo, 0, mNumIndices+2);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            // GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        }




    }

    public void release() {
        if (vbo_top[0] > 0) {
            GLES20.glDeleteBuffers(vbo_top.length, vbo_top, 0);
            vbo_top[0] = 0;
        }
        if (vbo_bottom[0] > 0) {
            GLES20.glDeleteBuffers(vbo_bottom.length, vbo_bottom, 0);
            vbo_bottom[0] = 0;
        }

//        if (ibo[0] > 0) {
//            GLES20.glDeleteBuffers(ibo.length, ibo, 0);
//            ibo[0] = 0;
//        }
    }
}
