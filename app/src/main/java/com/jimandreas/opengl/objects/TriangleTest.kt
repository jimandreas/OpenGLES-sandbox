package com.jimandreas.opengl.objects


import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TriangleTest {
    private val vbo = IntArray(1)
    private val ibo = IntArray(1)

    private var indexCount: Int = 0

    // simplify to one triangle strip
    init {
        val heightMapVertexData = FloatArray(6 * STRIDE_IN_ELEMENTS)
        var offset = 0
        val out_from_z = 3.0f
        val greenValue = 0.7f

        // Position 1
        heightMapVertexData[offset++] = 0f
        heightMapVertexData[offset++] = 1f
        heightMapVertexData[offset++] = 0f
        // normal
        heightMapVertexData[offset++] = 0f
        heightMapVertexData[offset++] = 0f
        heightMapVertexData[offset++] = out_from_z
        // teapot green
        heightMapVertexData[offset++] = 0f
        heightMapVertexData[offset++] = greenValue
        heightMapVertexData[offset++] = 0f
        heightMapVertexData[offset++] = 1f

        // Position 2
        heightMapVertexData[offset++] = .5f
        heightMapVertexData[offset++] = 1f
        heightMapVertexData[offset++] = 0f
        // normal
        heightMapVertexData[offset++] = 0f
        heightMapVertexData[offset++] = 0f
        heightMapVertexData[offset++] = out_from_z
        // teapot green
        heightMapVertexData[offset++] = 0f
        heightMapVertexData[offset++] = greenValue
        heightMapVertexData[offset++] = 0f
        heightMapVertexData[offset++] = 1f

        // Position 3
        heightMapVertexData[offset++] = 1f
        heightMapVertexData[offset++] = 1f
        heightMapVertexData[offset++] = 0f
        // normal
        heightMapVertexData[offset++] = 0f
        heightMapVertexData[offset++] = 0f
        heightMapVertexData[offset++] = out_from_z
        // teapot green
        heightMapVertexData[offset++] = 0f
        heightMapVertexData[offset++] = greenValue
        heightMapVertexData[offset++] = 0f
        heightMapVertexData[offset++] = 1f

        // Position 4
        heightMapVertexData[offset++] = 0f
        heightMapVertexData[offset++] = .6f
        heightMapVertexData[offset++] = 0f
        // normal
        heightMapVertexData[offset++] = 0f
        heightMapVertexData[offset++] = 0f
        heightMapVertexData[offset++] = out_from_z
        // teapot green
        heightMapVertexData[offset++] = 0f
        heightMapVertexData[offset++] = greenValue
        heightMapVertexData[offset++] = 0f
        heightMapVertexData[offset++] = 1f

        // Position 5
        heightMapVertexData[offset++] = .5f
        heightMapVertexData[offset++] = .6f
        heightMapVertexData[offset++] = 0f
        // normal
        heightMapVertexData[offset++] = 0f
        heightMapVertexData[offset++] = 0f
        heightMapVertexData[offset++] = out_from_z
        // teapot green
        heightMapVertexData[offset++] = 0f
        heightMapVertexData[offset++] = greenValue
        heightMapVertexData[offset++] = 0f
        heightMapVertexData[offset++] = 1f

        // Position 6
        heightMapVertexData[offset++] = 1f
        heightMapVertexData[offset++] = .6f
        heightMapVertexData[offset++] = 0f
        // normal
        heightMapVertexData[offset++] = 0f
        heightMapVertexData[offset++] = 0f
        heightMapVertexData[offset++] = out_from_z
        // teapot green
        heightMapVertexData[offset++] = 0f
        heightMapVertexData[offset++] = greenValue
        heightMapVertexData[offset++] = 0f
        heightMapVertexData[offset++] = 1f

        val indexData = ShortArray(6)
        offset = 0


        indexData[offset++] = 0
        indexData[offset++] = 3
        indexData[offset++] = 1
        indexData[offset++] = 4
        indexData[offset++] = 2
        indexData[offset++] = 5

        indexCount = indexData.size

        val heightMapVertexDataBuffer = ByteBuffer
                .allocateDirect(heightMapVertexData.size * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        heightMapVertexDataBuffer.put(heightMapVertexData).position(0)

        val heightMapIndexDataBuffer = ByteBuffer
                .allocateDirect(indexData.size * BYTES_PER_SHORT).order(ByteOrder.nativeOrder())
                .asShortBuffer()
        heightMapIndexDataBuffer.put(indexData).position(0)

        GLES20.glGenBuffers(1, vbo, 0)
        GLES20.glGenBuffers(1, ibo, 0)

        if (vbo[0] > 0 && ibo[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, heightMapVertexDataBuffer.capacity() * BYTES_PER_FLOAT,
                    heightMapVertexDataBuffer, GLES20.GL_STATIC_DRAW)

            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0])
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, heightMapIndexDataBuffer.capacity() * BYTES_PER_SHORT, heightMapIndexDataBuffer, GLES20.GL_STATIC_DRAW)

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
            normalAttribute: Int) {

        if (vbo[0] > 0 && ibo[0] > 0) {

            // Use culling to remove back faces.
            // GLES20.glDisable(GLES20.GL_CULL_FACE);

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
            GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, indexCount, GLES20.GL_UNSIGNED_SHORT, 0)

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)

            // Use culling to remove back faces.
            // GLES20.glEnable(GLES20.GL_CULL_FACE);
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

        private const val STRIDE_IN_ELEMENTS = POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS + COLOR_DATA_SIZE_IN_ELEMENTS

        private const val STRIDE_IN_BYTES = STRIDE_IN_ELEMENTS * BYTES_PER_FLOAT

    }
}
