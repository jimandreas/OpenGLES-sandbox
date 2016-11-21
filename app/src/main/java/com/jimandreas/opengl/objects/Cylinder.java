package com.jimandreas.opengl.objects;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Cylinder {
    private static final int POSITION_DATA_SIZE_IN_ELEMENTS = 3;
    private static final int NORMAL_DATA_SIZE_IN_ELEMENTS = 3;
    private static final int COLOR_DATA_SIZE_IN_ELEMENTS = 4;

    private static final int BYTES_PER_FLOAT = 4;
    private static final int BYTES_PER_SHORT = 2;

    private static final int STRIDE_IN_FLOATS =
            (POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS + COLOR_DATA_SIZE_IN_ELEMENTS);
    private static final int STRIDE_IN_BYTES = STRIDE_IN_FLOATS * BYTES_PER_FLOAT;

    private static final float NORMAL_BRIGHTNESS_FACTOR = 7f;

    private int mNumIndices;
    private int mCylinderIndexCount;
    final int[] vbo_top_and_bottom = new int[1];
    final int[] vbo_body = new int[1];
    final int[] ibo = new int[1];

    public Cylinder(int numSlices, float radius, float height, float[] color /*RGBA*/) {
        int i, j;
        int offset = 0;
        float vx, vy, vz;
        float angleStep = ((2.0f * (float) Math.PI) / numSlices);
        final float[] vertexData = new float[
                ((numSlices + 1) * STRIDE_IN_FLOATS * 2)
                + 2 * STRIDE_IN_FLOATS /* for the center points in each fan */];
        /*
         * top plate of cylinder ("base"
         */
        vertexData[offset++] = 0f;  // center of circle
        vertexData[offset++] = height / 2.0f;
        vertexData[offset++] = 0f;
        // normal vector
        vertexData[offset++] = 0f; // normal is straight up
        vertexData[offset++] = 3f;
        vertexData[offset++] = 0f;
        // color value
        vertexData[offset++] = color[0];
        vertexData[offset++] = color[1];
        vertexData[offset++] = color[2];
        vertexData[offset++] = color[3];

        /*
         * loop to generate vertices.   Note that the less/equal on the 
         * loop condition will repeat the circle origin
         */
        for (i = 0; i <= numSlices; i++) {
            float angleInRadians =
                    ((float) i / (float) numSlices)
                            * ((float) Math.PI * 2f);

            vertexData[offset++] = radius * (float) Math.cos(angleInRadians);
            vertexData[offset++] = height / 2.0f;
            vertexData[offset++] = radius * -(float) Math.sin(angleInRadians);

            // normal vector
            vertexData[offset++] = 0f; // normal is wierd??
            vertexData[offset++] = 3f;
            vertexData[offset++] = 0f;
            // color value
            vertexData[offset++] = color[0];
            vertexData[offset++] = color[1];
            vertexData[offset++] = color[2];
            vertexData[offset++] = color[3];
        }

        /* 
         * *** bottom plate of circle ***
         */
        vertexData[offset++] = 0f;  // center of circle
        vertexData[offset++] = -height / 2.0f;
        vertexData[offset++] = 0f;
        // normal vector
        vertexData[offset++] = 0f;
        vertexData[offset++] = -3f;
        vertexData[offset++] = 0f;
        // color value
        vertexData[offset++] = color[0];
        vertexData[offset++] = color[1];
        vertexData[offset++] = color[2];
        vertexData[offset++] = color[3];

        for (i = 0; i <= numSlices; i++) {
            float angleInRadians =
                    ((float) i / (float) numSlices)
                            * ((float) Math.PI * 2f);

            vertexData[offset++] = radius * (float) Math.cos(angleInRadians);
            vertexData[offset++] = -height / 2.0f;
            vertexData[offset++] = radius * (float) Math.sin(angleInRadians);
            // normal vector
            vertexData[offset++] = 0f;
            vertexData[offset++] = -3f;
            vertexData[offset++] = 0f;

//
//            // debug
//            if ((i == 0) || (i == numSlices)) {
//                vertexData[offset++] = 1.0f;
//                vertexData[offset++] = 1.0f;
//                vertexData[offset++] = 1.0f;
//                vertexData[offset++] = color[3];
//            } else {
                // color value
                vertexData[offset++] = color[0];
                vertexData[offset++] = color[1];
                vertexData[offset++] = color[2];
                vertexData[offset++] = color[3];
//            }

        }
        mNumIndices = numSlices + 1;
        /*
         * DEBUG:
         * print out list of formated vertex data
         */
//        for (i = 0; i < ((numSlices + 2) * STRIDE_IN_FLOATS) * 2; i+= STRIDE_IN_FLOATS) {
//            vx = vertexData[i + 0];
//            vy = vertexData[i + 1];
//            vz = vertexData[i + 2];
//            String svx = String.format("%6.2f", vx);
//            String svy = String.format("%6.2f", vy);
//            String svz = String.format("%6.2f", vz);
//
//            Timber("cyl ", i + " x y z "
//                    + svx + " " + svy + " " + svz + " and "
//                    + vertexData[i + 6] + " " + vertexData[i + 7] + " " + vertexData[i + 8]);
//        }

        {
            FloatBuffer vertexDataBuffer = ByteBuffer
                            .allocateDirect(vertexData.length * BYTES_PER_FLOAT)
                            .order(ByteOrder.nativeOrder())
                            .asFloatBuffer();
            vertexDataBuffer.put(vertexData).position(0);

            GLES20.glGenBuffers(1, vbo_top_and_bottom, 0);

            if (vbo_top_and_bottom[0] > 0) {
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_top_and_bottom[0]);
                GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexDataBuffer.capacity() * BYTES_PER_FLOAT,
                        vertexDataBuffer, GLES20.GL_STATIC_DRAW);

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            } else {
                // errorHandler.handleError(ErrorHandler.ErrorType.BUFFER_CREATION_ERROR, "glGenBuffers");
                throw new RuntimeException("error on buffer gen");
            }
            vertexDataBuffer.limit(0);
        }

        /*
         * replace the up/down normals with directional normals for the barrel
         * of the cylinder
         */

        offset = STRIDE_IN_FLOATS; // start after the center point of the fan
        float normalX, normalZ;
        for (i = 0; i <= numSlices; i++) {
            
            normalX = vertexData[offset++];
            offset++;
            normalZ = vertexData[offset++];

            // normal vector
            vertexData[offset++] = normalX * NORMAL_BRIGHTNESS_FACTOR;
            vertexData[offset++] = 0f;
            vertexData[offset++] = normalZ * NORMAL_BRIGHTNESS_FACTOR;
            // skip over color value
            offset += COLOR_DATA_SIZE_IN_ELEMENTS;
        }

        // now do the bottom of the cylinder body
        offset += STRIDE_IN_FLOATS; // start after the center point of the fan
        // float normalX, normalZ;
        for (i = 0; i <= numSlices; i++) {

            normalX = vertexData[offset++];
            offset++;
            normalZ = vertexData[offset++];

            // normal vector
            vertexData[offset++] = normalX * NORMAL_BRIGHTNESS_FACTOR;
            vertexData[offset++] = 0f;
            vertexData[offset++] = normalZ * NORMAL_BRIGHTNESS_FACTOR;
            // skip over color value
            offset += COLOR_DATA_SIZE_IN_ELEMENTS;
        }

        /*
         * DEBUG:
         * optional vertex printout
         */
//        float nvx, nvy, nvz;
//        for (i = 0; i < ((numSlices + 2) * STRIDE_IN_FLOATS) * 2; i += STRIDE_IN_FLOATS) {
//            vx = vertexData[i + 0];
//            vy = vertexData[i + 1];
//            vz = vertexData[i + 2];
//            String svx = String.format("%6.2f", vx);
//            String svy = String.format("%6.2f", vy);
//            String svz = String.format("%6.2f", vz);
//
//            nvx = vertexData[i + 3];
//            nvy = vertexData[i + 4];
//            nvz = vertexData[i + 5];
//            String snvx = String.format("%6.2f", nvx);
//            String snvy = String.format("%6.2f", nvy);
//            String snvz = String.format("%6.2f", nvz);
//
//            Timber("cyl ", i + " x y z nx ny nz "
//                    + svx + " " + svy + " " + svz + " and " + snvx + " " + snvy + " " + snvz
//                    + " clr "
//                    + vertexData[i + 6] + " " + vertexData[i + 7] + " " + vertexData[i + 8]);
//        }

        /*
         * saved the revised vertex + normal + color data in a new VBO
         */
        final FloatBuffer cylBodyVertexDataBuffer = ByteBuffer
                .allocateDirect(vertexData.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        cylBodyVertexDataBuffer.put(vertexData).position(0);

        /*
         * the index for the body of the cylinder. 
         */
        offset = 0;
        int x;
        final short[] indexData = new short[(2 * (numSlices+1))];
        for (x = 1; x < numSlices+2; x++) {

            indexData[offset++] = (short) x;
            indexData[offset++] = (short) ((2*(numSlices+2)) - x);
        }
        mCylinderIndexCount = indexData.length;

        final ShortBuffer indexDataBuffer = ByteBuffer
                .allocateDirect(indexData.length * BYTES_PER_SHORT).order(ByteOrder.nativeOrder())
                .asShortBuffer();
        indexDataBuffer.position(0);
        indexDataBuffer.put(indexData).position(0);

        GLES20.glGenBuffers(1, vbo_body, 0);
        GLES20.glGenBuffers(1, ibo, 0);
        if ((vbo_body[0] > 0) && (ibo[0] > 0)) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_body[0]);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);

            GLES20.glBufferData(
                    GLES20.GL_ARRAY_BUFFER,
                    cylBodyVertexDataBuffer.capacity() * BYTES_PER_FLOAT,
                    cylBodyVertexDataBuffer, GLES20.GL_STATIC_DRAW);

            GLES20.glBufferData(
                    GLES20.GL_ELEMENT_ARRAY_BUFFER,
                    indexDataBuffer.capacity() * BYTES_PER_SHORT,
                    indexDataBuffer, GLES20.GL_STATIC_DRAW);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
            cylBodyVertexDataBuffer.limit(0);
            indexDataBuffer.limit(0);
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

        // Debug: disable culling to remove back faces.
        // GLES20.glDisable(GLES20.GL_CULL_FACE);

        /*
         * draw top and bottom with triangle fans,
         * no indices needed
         */
        if (vbo_top_and_bottom[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_top_and_bottom[0]);
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

            // Draw - no indexes, top and bottom
            GLES20.glDrawArrays(todo, 0, mNumIndices + 1);
            GLES20.glDrawArrays(todo, mNumIndices + 1, mNumIndices + 1);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);  // release
        }

        if (doWireframeRendering) {
            todo = GLES20.GL_LINE_STRIP;
        } else {
            todo = GLES20.GL_TRIANGLE_STRIP;
        }

        /*
         * draw using the IBO - index buffer object,
         * and the revised vertex data that has
         * corrected normals for the body.
         */
        if ((vbo_body[0] > 0) && (ibo[0] > 0)) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_body[0]);
            // associate the attributes with the bound buffer
            GLES20.glVertexAttribPointer(positionAttribute,
                    POSITION_DATA_SIZE_IN_ELEMENTS,
                    GLES20.GL_FLOAT,
                    false,
                    STRIDE_IN_BYTES,
                    0);  // offset
            GLES20.glEnableVertexAttribArray(positionAttribute);

            // for tracking in gllog
            GLES20.glEnableVertexAttribArray(positionAttribute);
            GLES20.glEnableVertexAttribArray(positionAttribute);
            GLES20.glEnableVertexAttribArray(positionAttribute);
            GLES20.glEnableVertexAttribArray(positionAttribute);
            GLES20.glEnableVertexAttribArray(positionAttribute);


            GLES20.glVertexAttribPointer(normalAttribute, NORMAL_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                    STRIDE_IN_BYTES, POSITION_DATA_SIZE_IN_ELEMENTS * BYTES_PER_FLOAT);
            GLES20.glEnableVertexAttribArray(normalAttribute);

            GLES20.glVertexAttribPointer(colorAttribute, COLOR_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                    STRIDE_IN_BYTES, (POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS) * BYTES_PER_FLOAT);
            GLES20.glEnableVertexAttribArray(colorAttribute);

            /*
             * draw using the IBO - index buffer object
             */
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
            GLES20.glDrawElements(
                    todo,
                    mCylinderIndexCount,
                    GLES20.GL_UNSIGNED_SHORT,
                    0);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0); // release
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);  // release
        }
        // Debug:  Use culling to remove back faces.
        // GLES20.glEnable(GLES20.GL_CULL_FACE);
    }

    public void release() {
        if (vbo_top_and_bottom[0] > 0) {
            GLES20.glDeleteBuffers(vbo_top_and_bottom.length, vbo_top_and_bottom, 0);
            vbo_top_and_bottom[0] = 0;
        }
        if (ibo[0] > 0) {
            GLES20.glDeleteBuffers(ibo.length, ibo, 0);
            ibo[0] = 0;
        }
    }
}
