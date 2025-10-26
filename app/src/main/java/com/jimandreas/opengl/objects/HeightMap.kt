package com.jimandreas.opengl.objects

import android.opengl.GLES20
import android.opengl.Matrix
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder

class HeightMap {
    private val vbo = IntArray(1)
    private val ibo = IntArray(1)

    private var indexCount = 0

    init {
        try {
            val floatsPerVertex = (POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS
                    + COLOR_DATA_SIZE_IN_ELEMENTS)
            val xLength = SIZE_PER_SIDE
            val yLength = SIZE_PER_SIDE

            val heightMapVertexData = FloatArray(xLength * yLength * floatsPerVertex)

            var offset = 0

            // First, build the data for the vertex buffer
            for (y in 0 until yLength) {
                for (x in 0 until xLength) {
                    val xRatio = x / (xLength - 1).toFloat()

                    // Build our heightmap from the top down, so that our triangles are counter-clockwise.
                    val yRatio = 1f - y / (yLength - 1).toFloat()

                    val xPosition = MIN_POSITION + xRatio * POSITION_RANGE
                    val yPosition = MIN_POSITION + yRatio * POSITION_RANGE

                    // Position
                    heightMapVertexData[offset++] = xPosition
                    heightMapVertexData[offset++] = yPosition
                    heightMapVertexData[offset++] = (xPosition * xPosition + yPosition * yPosition) / 10f

                    // Cheap normal using a derivative of the function.
                    // The slope for X will be 2X, for Y will be 2Y.
                    // Divide by 10 since the position's Z is also divided by 10.
                    val xSlope = 2 * xPosition / 10f
                    val ySlope = 2 * yPosition / 10f

                    // Calculate the normal using the cross product of the slopes.
                    val planeVectorX = floatArrayOf(1f, 0f, xSlope)
                    val planeVectorY = floatArrayOf(0f, 1f, ySlope)
                    val normalVector = floatArrayOf(planeVectorX[1] * planeVectorY[2] - planeVectorX[2] * planeVectorY[1], planeVectorX[2] * planeVectorY[0] - planeVectorX[0] * planeVectorY[2], planeVectorX[0] * planeVectorY[1] - planeVectorX[1] * planeVectorY[0])

                    // Normalize the normal
                    val length = Matrix.length(normalVector[0], normalVector[1], normalVector[2])

                    heightMapVertexData[offset++] = normalVector[0] / length
                    heightMapVertexData[offset++] = normalVector[1] / length
                    heightMapVertexData[offset++] = normalVector[2] / length

                    // Add some fancy colors.
                    heightMapVertexData[offset++] = xRatio
                    heightMapVertexData[offset++] = yRatio
                    heightMapVertexData[offset++] = 0.5f
                    heightMapVertexData[offset++] = 1f
                }
            }

            // Now build the index data
            val numStripsRequired = yLength - 1
            val numDegensRequired = 2 * (numStripsRequired - 1)
            val verticesPerStrip = 2 * xLength

            val heightMapIndexData = ShortArray(verticesPerStrip * numStripsRequired + numDegensRequired)

            offset = 0

            for (y in 0 until yLength - 1) {
                if (y > 0) {
                    // Degenerate begin: repeat first vertex
                    heightMapIndexData[offset++] = (y * yLength).toShort()
                }

                for (x in 0 until xLength) {
                    // One part of the strip
                    heightMapIndexData[offset++] = (y * yLength + x).toShort()
                    heightMapIndexData[offset++] = ((y + 1) * yLength + x).toShort()
                }

                if (y < yLength - 2) {
                    // Degenerate end: repeat last vertex
                    heightMapIndexData[offset++] = ((y + 1) * yLength + (xLength - 1)).toShort()
                }
            }

            indexCount = heightMapIndexData.size

            val heightMapVertexDataBuffer = ByteBuffer
                    .allocateDirect(heightMapVertexData.size * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
            heightMapVertexDataBuffer.put(heightMapVertexData).position(0)

            val heightMapIndexDataBuffer = ByteBuffer
                    .allocateDirect(heightMapIndexData.size * BYTES_PER_SHORT).order(ByteOrder.nativeOrder())
                    .asShortBuffer()
            heightMapIndexDataBuffer.put(heightMapIndexData).position(0)

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
        } catch (t: Throwable) {
            Timber.e(t, "HeightMap")
            // errorHandler.handleError(ErrorHandler.ErrorType.BUFFER_CREATION_ERROR, t.getLocalizedMessage());
            throw RuntimeException("error on buffer gen")
        }

    }

    fun render(
            positionAttribute: Int,
            colorAttribute: Int,
            normalAttribute: Int,
            doWireframeRendering: Boolean) {

        // Draw
        val todo: Int = if (doWireframeRendering) {
            GLES20.GL_LINE_STRIP
        } else {
            GLES20.GL_TRIANGLE_STRIP
        }

        if (vbo[0] > 0 && ibo[0] > 0) {

            // Use culling to remove back faces.
            GLES20.glDisable(GLES20.GL_CULL_FACE)

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])

            // Bind Attributes
            GLES20.glVertexAttribPointer(positionAttribute, POSITION_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                    STRIDE, 0)
            GLES20.glEnableVertexAttribArray(positionAttribute)

            GLES20.glVertexAttribPointer(normalAttribute, NORMAL_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                    STRIDE, POSITION_DATA_SIZE_IN_ELEMENTS * BYTES_PER_FLOAT)
            GLES20.glEnableVertexAttribArray(normalAttribute)

            GLES20.glVertexAttribPointer(colorAttribute, COLOR_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                    STRIDE, (POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS) * BYTES_PER_FLOAT)
            GLES20.glEnableVertexAttribArray(colorAttribute)

            // Draw
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0])
            GLES20.glDrawElements(todo, indexCount, GLES20.GL_UNSIGNED_SHORT, 0)

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)

            // Use culling to remove back faces.
            GLES20.glEnable(GLES20.GL_CULL_FACE)
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

        private const val STRIDE = (POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS + COLOR_DATA_SIZE_IN_ELEMENTS) * BYTES_PER_FLOAT

        private const val SIZE_PER_SIDE = 32
        private const val MIN_POSITION = -5f
        private const val POSITION_RANGE = 10f
    }
}
