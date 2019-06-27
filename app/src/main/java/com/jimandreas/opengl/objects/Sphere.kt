package com.jimandreas.opengl.objects

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.cos
import kotlin.math.sin

class Sphere(numSlices: Int, radius: Float, color: FloatArray /*RGBA*/) {
    private val mNumIndices: Int

    private val vbo = IntArray(1)
    private val ibo = IntArray(1)

    init {
        var i = 0
        var j: Int
        var offset = 0
        var vx: Float
        var vy: Float
        var vz: Float

        val numVertices = (numSlices + 1) * (numSlices + 1)

        val angleStep = 2.0f * Math.PI.toFloat() / numSlices
        val vertexData = FloatArray(numVertices * STRIDE_IN_FLOATS)

        /*
         * note the use of less-than-equals - the first point is repeated to complete the circle
         */
        while (i <= numSlices) {
            j = 0
            while (j <= numSlices) {
                vx = (radius.toDouble()
                        * sin((angleStep / 2.0f * i.toFloat()).toDouble())
                        * sin((angleStep * j.toFloat()).toDouble())).toFloat()
                vy = (radius * cos((angleStep / 2.0f * i.toFloat()).toDouble())).toFloat()
                vz = (radius.toDouble()
                        * sin((angleStep / 2.0f * i.toFloat()).toDouble())
                        * cos((angleStep * j.toFloat()).toDouble())).toFloat()

                vertexData[offset++] = vx
                vertexData[offset++] = vy
                vertexData[offset++] = vz
                // normal vector
                //                vertexData[offset++] = 0f;
                //                vertexData[offset++] = 5f;
                //                vertexData[offset++] = 0f;

                vertexData[offset++] = vx / radius * 3.0f
                vertexData[offset++] = vy / radius * 3.0f
                vertexData[offset++] = vz / radius * 3.0f

                // debug
                if (i == numSlices) {
                    vertexData[offset++] = 1.0f
                    vertexData[offset++] = 1.0f
                    vertexData[offset++] = 1.0f
                    vertexData[offset++] = color[3]
                } else {
                    // color value
                    vertexData[offset++] = color[0]
                    vertexData[offset++] = color[1]
                    vertexData[offset++] = color[2]
                    vertexData[offset++] = color[3]
                }
                j++
            }
            i++
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
        val numDegensRequired = 2 * (numSlices - 1)
        val verticesPerStrip = 2 * (numSlices + 1)

        val indexData = ShortArray(verticesPerStrip * numSlices + numDegensRequired)

        offset = 0
        // FMI:  on numbering the indexes using degenerate triangle index repeats, see
        // http://www.learnopengles.com/android-lesson-eight-an-introduction-to-index-buffer-objects-ibos/

        var x: Int
        for (y in 0 until numSlices) {
            if (y > 0) {
                // Degenerate begin: repeat first vertex
                indexData[offset++] = ((y + 0) * (numSlices + 1) + 0).toShort()
            }

            x = 0
            while (x <= numSlices) {
                indexData[offset++] = ((y + 0) * (numSlices + 1) + x).toShort()
                indexData[offset++] = ((y + 1) * (numSlices + 1) + x).toShort()
                x++
            }

            if (y < numSlices - 1) {
                // Degenerate end: repeat last vertex
                indexData[offset++] = ((y + 1) * (numSlices + 1) + --x).toShort()
            }
        }

        mNumIndices = indexData.size

        val vertexDataBuffer = ByteBuffer
                .allocateDirect(vertexData.size * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        vertexDataBuffer.put(vertexData).position(0)

        val indexDataBuffer = ByteBuffer
                .allocateDirect(indexData.size * BYTES_PER_SHORT).order(ByteOrder.nativeOrder())
                .asShortBuffer()
        indexDataBuffer.put(indexData).position(0)

        GLES20.glGenBuffers(1, vbo, 0)
        GLES20.glGenBuffers(1, ibo, 0)

        if (vbo[0] > 0 && ibo[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexDataBuffer.capacity() * BYTES_PER_FLOAT,
                    vertexDataBuffer, GLES20.GL_STATIC_DRAW)

            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0])
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexDataBuffer.capacity() * BYTES_PER_SHORT, indexDataBuffer, GLES20.GL_STATIC_DRAW)

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
        } else {
            // errorHandler.handleError(ErrorHandler.ErrorType.BUFFER_CREATION_ERROR, "glGenBuffers");
            throw RuntimeException("error on buffer gen")
        }
    }

    fun render(
            positionAttribute: Int,
            colorAttribute: Int,
            normalAttribute: Int,
            doWireframeRendering: Boolean) {

        // Draw
        val todo: Int
        if (doWireframeRendering) {
            todo = GLES20.GL_LINE_STRIP
        } else {
            todo = GLES20.GL_TRIANGLE_STRIP
        }

        if (vbo[0] > 0 && ibo[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])

            // Bind Attributes
            GLES20.glVertexAttribPointer(positionAttribute, POSITION_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                    STRIDE_IN_BYTES, 0)
            GLES20.glEnableVertexAttribArray(positionAttribute)

            GLES20.glVertexAttribPointer(normalAttribute, NORMAL_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                    STRIDE_IN_BYTES, POSITION_DATA_SIZE_IN_ELEMENTS * BYTES_PER_FLOAT)
            GLES20.glEnableVertexAttribArray(normalAttribute)

            GLES20.glVertexAttribPointer(colorAttribute, COLOR_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                    STRIDE_IN_BYTES, (POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS) * BYTES_PER_FLOAT)
            GLES20.glEnableVertexAttribArray(colorAttribute)

            // Draw
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0])
            GLES20.glDrawElements(todo, mNumIndices, GLES20.GL_UNSIGNED_SHORT, 0)

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
        }
    }

    fun release() {
        if (vbo[0] > 0) {
            GLES20.glDeleteBuffers(vbo.size, vbo, 0)
            vbo[0] = 0
        }

        if (ibo[0] > 0) {
            GLES20.glDeleteBuffers(ibo.size, ibo, 0)
            ibo[0] = 0
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
    }
}
