package com.learnopengles.sandbox.objects;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Sphere {
    private static final int POSITION_DATA_SIZE_IN_ELEMENTS = 3;
    private static final int NORMAL_DATA_SIZE_IN_ELEMENTS = 3;
    private static final int COLOR_DATA_SIZE_IN_ELEMENTS = 4;

    private static final int BYTES_PER_FLOAT = 4;
    private static final int BYTES_PER_SHORT = 2;
    
    private static final int STRIDE_IN_FLOATS =
            (POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS + COLOR_DATA_SIZE_IN_ELEMENTS);
    private static final int STRIDE_IN_BYTES = STRIDE_IN_FLOATS * BYTES_PER_FLOAT;
    private int mNumIndices;

    final int[] vbo = new int[1];
    final int[] ibo = new int[1];

    public Sphere(int numSlices, float radius, float[] color /*RGBA*/ ) {
        int i, j;
        int offset = 0;
        float vx, vy, vz;
        
        int numVertices = (numSlices + 1) * (numSlices + 1);

        float angleStep = ((2.0f * (float) Math.PI) / numSlices);
        final float[] vertexData = new float[numVertices * STRIDE_IN_FLOATS];
        
        /*
         * note the use of less-than-equals - the first point is repeated to complete the circle
         */
        for (i = 0; i <= numSlices; i++) {
            for (j = 0; j <= numSlices; j++) {
                vx = (float) (radius
                            * Math.sin(angleStep/2.0f * (float) i)
                            * Math.sin(angleStep * (float) j));
                vy = (float) (radius * Math.cos(angleStep/2.0f * (float) i));
                vz = (float) (radius
                            * Math.sin(angleStep/2.0f * (float) i)
                            * Math.cos(angleStep * (float) j));

                vertexData[offset++] = vx;
                vertexData[offset++] = vy;
                vertexData[offset++] = vz;
                // normal vector
//                vertexData[offset++] = 0f;
//                vertexData[offset++] = 5f;
//                vertexData[offset++] = 0f;

                vertexData[offset++] = vx / radius * 3.0f;
                vertexData[offset++] = vy / radius * 3.0f;
                vertexData[offset++] = vz / radius * 3.0f;

                // debug
                if ((i == numSlices)) {
                    vertexData[offset++] = 1.0f;
                    vertexData[offset++] = 1.0f;
                    vertexData[offset++] = 1.0f;
                    vertexData[offset++] = color[3];
                } else {
                    // color value
                    vertexData[offset++] = color[0];
                    vertexData[offset++] = color[1];
                    vertexData[offset++] = color[2];
                    vertexData[offset++] = color[3];
                }
            }
        }

        /*
         * debugging print out of vertices
         */
//        for (i = 0; i < vertexData.length; i += STRIDE_IN_FLOATS) {
//            vx = vertexData[i + 0];
//            vy = vertexData[i + 1];
//            vz = vertexData[i + 2];
//            String svx = String.format("%6.2f", vx);
//            String svy = String.format("%6.2f", vy);
//            String svz = String.format("%6.2f", vz);
//
//            Timber("cyl ", i/STRIDE_IN_FLOATS + " x y z "
//                    + svx + " " + svy + " " + svz + " and "
//                    + vertexData[i + 6] + " " + vertexData[i + 7] + " " + vertexData[i + 8]);
//        }

        // Now build the index data
        final int numStripsRequired = numSlices;
        final int numDegensRequired = 2 * (numStripsRequired - 1);
        final int verticesPerStrip = 2 * (numSlices+1);

        final short[] indexData = new short[(verticesPerStrip * numStripsRequired) + numDegensRequired];

        offset = 0;
// FMI:  on numbering the indexes using degenerate triangle index repeats, see
// http://www.learnopengles.com/android-lesson-eight-an-introduction-to-index-buffer-objects-ibos/

        int x;
        for (int y = 0; y < numSlices; y++) {
            if (y > 0) {
                // Degenerate begin: repeat first vertex
                indexData[offset++] = (short) (((y+0) * (numSlices+1)) + 0);
            }

            for (x = 0; x <= numSlices; x++) {
                indexData[offset++] = (short) (((y+0) * (numSlices+1)) + x);
                indexData[offset++] = (short) (((y+1) * (numSlices+1)) + x);
            }

            if (y < numSlices - 1) {
                // Degenerate end: repeat last vertex
                indexData[offset++] = (short) (((y+1) * (numSlices+1)) + --x);
            }
        }

        mNumIndices = indexData.length;

        final FloatBuffer vertexDataBuffer = ByteBuffer
                .allocateDirect(vertexData.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexDataBuffer.put(vertexData).position(0);

        final ShortBuffer indexDataBuffer = ByteBuffer
                .allocateDirect(indexData.length * BYTES_PER_SHORT).order(ByteOrder.nativeOrder())
                .asShortBuffer();
        indexDataBuffer.put(indexData).position(0);

        GLES20.glGenBuffers(1, vbo, 0);
        GLES20.glGenBuffers(1, ibo, 0);

        if (vbo[0] > 0 && ibo[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexDataBuffer.capacity() * BYTES_PER_FLOAT,
                    vertexDataBuffer, GLES20.GL_STATIC_DRAW);

            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexDataBuffer.capacity()
                    * BYTES_PER_SHORT, indexDataBuffer, GLES20.GL_STATIC_DRAW);

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
            int normalAttribute,
            boolean doWireframeRendering ) {

        // Draw
        int todo;
        if (doWireframeRendering) {
            todo = GLES20.GL_LINE_STRIP;
        } else {
            todo = GLES20.GL_TRIANGLE_STRIP;
        }

        if (vbo[0] > 0 && ibo[0] > 0) {
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
            GLES20.glDrawElements(todo, mNumIndices, GLES20.GL_UNSIGNED_SHORT, 0);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
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
