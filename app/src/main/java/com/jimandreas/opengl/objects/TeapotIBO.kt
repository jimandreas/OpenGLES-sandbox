@file:Suppress("LocalVariableName")

package com.jimandreas.opengl.objects

import android.opengl.GLES20
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TeapotIBO(color: FloatArray) {

    private val vbo = IntArray(1)
    private val ibo = IntArray(1)

    private val mNumIndices: Int


    init {

        val vertexData = FloatArray(2781 * STRIDE_IN_FLOATS)
        var i = 0
        /*
         * form up an interleaved vertex buffer object
         *    for a nice picture see:
         *       http://wiki.mcneel.com/developer/rhinomobile/rendering_pipeline
         */
        val positionDataLength = teapotPositionData.size
        val normalDataLength = teapotNormalData.size
        var offset = 0
        var data_index = 0
        while (i < positionDataLength / 3) {
            vertexData[offset++] = teapotPositionData[data_index + 0]
            vertexData[offset++] = teapotPositionData[data_index + 1]
            vertexData[offset++] = teapotPositionData[data_index + 2]
            // normal vector
            vertexData[offset++] = teapotNormalData[data_index + 0]
            vertexData[offset++] = teapotNormalData[data_index + 1]
            vertexData[offset++] = teapotNormalData[data_index + 2]
            // color value
            vertexData[offset++] = color[0]
            vertexData[offset++] = color[1]
            vertexData[offset++] = color[2]
            vertexData[offset++] = color[3]

            data_index += 3
            i++
        }
        /*
         * scan the index data for degenerates.
         *   could fix the data but it is nice to see where the discontinuities are located...
         */
        var numDegenerates = 0
        i = 0
        while (i < teapot_indices.size) {
            if (teapot_indices[i].toInt() == -1) {
                numDegenerates++
            }
            i++
        }
        val indexData = ShortArray(teapot_indices.size + numDegenerates)
        /*
         * copy the data.  The discontinuities are flagged by repeating indices at the end of
         * a strip and the start of the next.
         */

        /* learning:
         *  OK that didn't work!!   Just pass through the -1 indices and OpenGL knows
         *  how to deal with them.
         */
        var indexDataOffset = 0
        i = 0
        while (i < teapot_indices.size - 1) {
            if (teapot_indices[i].toInt() == -1) {
                // repeat previous and next index
                indexData[indexDataOffset++] = teapot_indices[i - 1]
                indexData[indexDataOffset++] = teapot_indices[i + 1]
                /*
                 * passing through -1's blows up Genymotion,
                 * so this experiment fails
                 */
                // indexData[indexDataOffset++] = teapot_indices[i];
                if (teapot_indices[i + 1].toInt() == -1) {
                    Timber.e("Yow double minus ones at index %d was after %d",
                            i, teapot_indices[i - 1])
                }
            } else {
                indexData[indexDataOffset++] = teapot_indices[i]
            }
            i++
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
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0])
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexDataBuffer.capacity() * BYTES_PER_FLOAT,
                    vertexDataBuffer, GLES20.GL_STATIC_DRAW)
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
            doWireframeRendering: Boolean
    ) {

        // Debug: disable culling to remove back faces.
        // GLES20.glDisable(GLES20.GL_CULL_FACE);

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
            val todo: Int
            if (doWireframeRendering) {
                todo = GLES20.GL_LINE_STRIP
            } else {
                todo = GLES20.GL_TRIANGLE_STRIP
            }

            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0])
            // GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, mNumIndices, GLES20.GL_UNSIGNED_SHORT, 0);
            GLES20.glDrawElements(todo, mNumIndices, GLES20.GL_UNSIGNED_SHORT, 0)

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
        }

        // Debug:  Use culling to remove back faces.
        // GLES20.glEnable(GLES20.GL_CULL_FACE);
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
