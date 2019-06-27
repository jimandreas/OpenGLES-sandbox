package com.jimandreas.opengl.objects

import android.opengl.GLES20

class EllipseHelix {

    private var mNumIndices = 0
    private val vbo_top_and_bottom = IntArray(1)
    private val vbo_body = IntArray(1)

    fun create(numSlices: Int, radius: Float, height: Float, color: FloatArray) {

        var i = 0
        // HACK -
        // TODO: the calculation for how many triangles
        // TODO: separate out the generation of ends from the body
        val vertexData = BufferManager.getFloatArray(6 * numSlices + 1 * STRIDE_IN_FLOATS)
        var offset = BufferManager.sFloatArrayIndex

        // 6 * numSlices+1 * STRIDE

        /* BODY BODY BODY
         * loop to generate vertices.   Note that the less/equal on the
         * loop condition will repeat the circle origin
         */

        while (i <= numSlices) {

            // TODO: fix number of slices and generate sin/cos lookup table

            val angleInRadians1 = i.toFloat() / numSlices.toFloat() * (Math.PI.toFloat() * 2f)

            val angleInRadians2 = (i + 1).toFloat() / numSlices.toFloat() * (Math.PI.toFloat() * 2f)
            run {
                // first top point
                vertexData[offset++] = radius * Math.cos(angleInRadians1.toDouble()).toFloat() * ELLIPSE_X_FACTOR
                vertexData[offset++] = height / 2.0f
                vertexData[offset++] = radius * -Math.sin(angleInRadians1.toDouble()).toFloat() * ELLIPSE_Z_FACTOR

                // normal vector
                vertexData[offset++] = radius * Math.cos(angleInRadians1.toDouble()).toFloat() * ELLIPSE_X_FACTOR
                vertexData[offset++] = 0.0f
                vertexData[offset++] = radius * -Math.sin(angleInRadians1.toDouble()).toFloat() * ELLIPSE_Z_FACTOR
                // color value
                vertexData[offset++] = color[0]
                vertexData[offset++] = color[1]
                vertexData[offset++] = color[2]
                vertexData[offset++] = color[3]
            }


            run {
                // first bottom point
                vertexData[offset++] = radius * Math.cos(angleInRadians1.toDouble()).toFloat() * ELLIPSE_X_FACTOR
                vertexData[offset++] = -height / 2.0f
                vertexData[offset++] = radius * -Math.sin(angleInRadians1.toDouble()).toFloat() * ELLIPSE_Z_FACTOR

                // normal vector
                vertexData[offset++] = radius * Math.cos(angleInRadians1.toDouble()).toFloat() * ELLIPSE_X_FACTOR
                vertexData[offset++] = 0.0f
                vertexData[offset++] = radius * -Math.sin(angleInRadians1.toDouble()).toFloat() * ELLIPSE_Z_FACTOR

                // color value
                vertexData[offset++] = color[0]
                vertexData[offset++] = color[1]
                vertexData[offset++] = color[2]
                vertexData[offset++] = color[3]
            }


            run {
                // SECOND BOTTOM point
                vertexData[offset++] = radius * Math.cos(angleInRadians2.toDouble()).toFloat() * ELLIPSE_X_FACTOR
                vertexData[offset++] = -height / 2.0f
                vertexData[offset++] = radius * -Math.sin(angleInRadians2.toDouble()).toFloat() * ELLIPSE_Z_FACTOR

                // normal vector
                vertexData[offset++] = radius * Math.cos(angleInRadians2.toDouble()).toFloat() * ELLIPSE_X_FACTOR
                vertexData[offset++] = 0.0f
                vertexData[offset++] = radius * -Math.sin(angleInRadians2.toDouble()).toFloat() * ELLIPSE_Z_FACTOR
                // color value
                vertexData[offset++] = color[0]
                vertexData[offset++] = color[1]
                vertexData[offset++] = color[2]
                vertexData[offset++] = color[3]
            } // OK that is one triangle.

            // SECOND triangle NOW

            run {
                // first top point
                vertexData[offset++] = radius * Math.cos(angleInRadians1.toDouble()).toFloat() * ELLIPSE_X_FACTOR
                vertexData[offset++] = height / 2.0f
                vertexData[offset++] = radius * -Math.sin(angleInRadians1.toDouble()).toFloat() * ELLIPSE_Z_FACTOR

                // normal vector
                vertexData[offset++] = radius * Math.cos(angleInRadians1.toDouble()).toFloat() * ELLIPSE_X_FACTOR
                vertexData[offset++] = 0.0f
                vertexData[offset++] = radius * -Math.sin(angleInRadians1.toDouble()).toFloat() * ELLIPSE_Z_FACTOR
                // color value
                vertexData[offset++] = color[0]
                vertexData[offset++] = color[1]
                vertexData[offset++] = color[2]
                vertexData[offset++] = color[3]
            }

            run {
                // SECOND BOTTOM point
                vertexData[offset++] = radius * Math.cos(angleInRadians2.toDouble()).toFloat() * ELLIPSE_X_FACTOR
                vertexData[offset++] = -height / 2.0f
                vertexData[offset++] = radius * -Math.sin(angleInRadians2.toDouble()).toFloat() * ELLIPSE_Z_FACTOR

                // normal vector
                vertexData[offset++] = radius * Math.cos(angleInRadians2.toDouble()).toFloat() * ELLIPSE_X_FACTOR
                vertexData[offset++] = 0.0f
                vertexData[offset++] = radius * -Math.sin(angleInRadians2.toDouble()).toFloat() * ELLIPSE_Z_FACTOR

                // color value
                vertexData[offset++] = color[0]
                vertexData[offset++] = color[1]
                vertexData[offset++] = color[2]
                vertexData[offset++] = color[3]
            }

            run {
                // SECOND TOP point
                vertexData[offset++] = radius * Math.cos(angleInRadians2.toDouble()).toFloat() * ELLIPSE_X_FACTOR
                vertexData[offset++] = height / 2.0f
                vertexData[offset++] = radius * -Math.sin(angleInRadians2.toDouble()).toFloat() * ELLIPSE_Z_FACTOR

                // normal vector
                vertexData[offset++] = radius * Math.cos(angleInRadians2.toDouble()).toFloat() * ELLIPSE_X_FACTOR
                vertexData[offset++] = 0.0f
                vertexData[offset++] = radius * -Math.sin(angleInRadians2.toDouble()).toFloat() * ELLIPSE_Z_FACTOR
                // color value
                vertexData[offset++] = color[0]
                vertexData[offset++] = color[1]
                vertexData[offset++] = color[2]
                vertexData[offset++] = color[3]
            }
            i++

        }  // end for loop for body

        mNumIndices = offset
        BufferManager.sFloatArrayIndex = offset

        /*
         * DEBUG:
         * optional vertex printout
         */
        //        float nvx, nvy, nvz;
        //
        //        for (i = 0; i < offset; i += STRIDE_IN_FLOATS) {
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
        //            Timber("vert ", i + " x y z nx ny nz "
        //                            + svx + " " + svy + " " + svz + " and " + snvx + " " + snvy + " " + snvz
        ////                    + " clr "
        ////                    + vertexData[i + 6] + " " + vertexData[i + 7] + " "
        ////                    + vertexData[i + 8] + " " + vertexData[i + 8]
        //            );
        //        }


        //        FloatBuffer vertexDataBuffer = ByteBuffer
        //                .allocateDirect(offset /* intead of the buffer length */ * BYTES_PER_FLOAT)
        //                .order(ByteOrder.nativeOrder())
        //                .asFloatBuffer();
        //        // public FloatBuffer put(float[] src, int srcOffset, int floatCount) {
        //        vertexDataBuffer
        //                .put(vertexData, 0, offset)
        //                .position(0);
        //
        //        GLES20.glGenBuffers(1, vbo_body, 0);
        //
        //        if (vbo_body[0] > 0) {
        //            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_body[0]);
        //            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexDataBuffer.capacity() * BYTES_PER_FLOAT,
        //                    vertexDataBuffer, GLES20.GL_STATIC_DRAW);
        //
        //            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        //        } else {
        //            // errorHandler.handleError(ErrorHandler.ErrorType.BUFFER_CREATION_ERROR, "glGenBuffers");
        //            throw new RuntimeException("error on buffer gen");
        //        }
        //        vertexDataBuffer.limit(0);
    }

    fun render(
            positionAttribute: Int,
            colorAttribute: Int,
            normalAttribute: Int) {

        // Debug: disable culling to remove back faces.
        // GLES20.glDisable(GLES20.GL_CULL_FACE);

        /*
         * draw top and bottom with triangle fans,
         * no indices needed
         */
        if (vbo_top_and_bottom[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_top_and_bottom[0])
            // associate the attributes with the bound buffer
            GLES20.glVertexAttribPointer(positionAttribute,
                    POSITION_DATA_SIZE_IN_ELEMENTS,
                    GLES20.GL_FLOAT,
                    false,
                    STRIDE_IN_BYTES,
                    0)  // offset
            GLES20.glEnableVertexAttribArray(positionAttribute)

            GLES20.glVertexAttribPointer(normalAttribute, NORMAL_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                    STRIDE_IN_BYTES, POSITION_DATA_SIZE_IN_ELEMENTS * BYTES_PER_FLOAT)
            GLES20.glEnableVertexAttribArray(normalAttribute)

            GLES20.glVertexAttribPointer(colorAttribute, COLOR_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                    STRIDE_IN_BYTES, (POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS) * BYTES_PER_FLOAT)
            GLES20.glEnableVertexAttribArray(colorAttribute)

            // Draw Triangles (or GL_LINES for debugging)
            // GLES20.glDrawArrays(GLES20.GL_LINES, 0, mNumIndices);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mNumIndices)

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)  // release
        }

        if (vbo_body[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_body[0])
            // associate the attributes with the bound buffer
            GLES20.glVertexAttribPointer(positionAttribute,
                    POSITION_DATA_SIZE_IN_ELEMENTS,
                    GLES20.GL_FLOAT,
                    false,
                    STRIDE_IN_BYTES,
                    0)  // offset
            GLES20.glEnableVertexAttribArray(positionAttribute)

            GLES20.glVertexAttribPointer(normalAttribute, NORMAL_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                    STRIDE_IN_BYTES, POSITION_DATA_SIZE_IN_ELEMENTS * BYTES_PER_FLOAT)
            GLES20.glEnableVertexAttribArray(normalAttribute)

            GLES20.glVertexAttribPointer(colorAttribute, COLOR_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                    STRIDE_IN_BYTES, (POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS) * BYTES_PER_FLOAT)
            GLES20.glEnableVertexAttribArray(colorAttribute)

            // Draw Triangles (or GL_LINES for debugging)
            // GLES20.glDrawArrays(GLES20.GL_LINES, 0, mNumIndices);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mNumIndices)

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)  // release
        }


        // Debug:  Use culling to remove back faces.
        // GLES20.glEnable(GLES20.GL_CULL_FACE);
    }

    fun release() {
        if (vbo_top_and_bottom[0] > 0) {
            GLES20.glDeleteBuffers(vbo_top_and_bottom.size, vbo_top_and_bottom, 0)
            vbo_top_and_bottom[0] = 0
        }
    }

    companion object {
        private const val POSITION_DATA_SIZE_IN_ELEMENTS = 3
        private const val NORMAL_DATA_SIZE_IN_ELEMENTS = 3
        private const val COLOR_DATA_SIZE_IN_ELEMENTS = 4

        private const val BYTES_PER_FLOAT = 4
        private const val BYTES_PER_SHORT = 2

        private const val STRIDE_IN_FLOATS = POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS + COLOR_DATA_SIZE_IN_ELEMENTS
        private const val STRIDE_IN_BYTES = STRIDE_IN_FLOATS * BYTES_PER_FLOAT

        private const val NORMAL_BRIGHTNESS_FACTOR = 7f
        private const val ELLIPSE_X_FACTOR = 2f / 9f
        private const val ELLIPSE_Z_FACTOR = 1f
    }
}
