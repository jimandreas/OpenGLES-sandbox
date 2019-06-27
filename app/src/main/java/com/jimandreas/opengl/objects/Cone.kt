@file:Suppress("PrivatePropertyName")

package com.jimandreas.opengl.objects

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.cos
import kotlin.math.sin

class Cone(numSlices: Int,
           radius: Float, length: Float,
           color: FloatArray,
           base_color: FloatArray) {
    
    private var numIndices: Int = 0

    private val vbo_top = IntArray(1)
    private val vbo_bottom = IntArray(1)

    init {
        var i = 0
        var offset = 0

        val vertexData = FloatArray((numSlices + 1) * STRIDE_IN_BYTES)

        // top point of cone
        vertexData[offset++] = 0f  // center of circle
        vertexData[offset++] = length / 2.0f
        vertexData[offset++] = 0f
        // normal vector
        vertexData[offset++] = 1f // normal is straight up
        vertexData[offset++] = 0f
        vertexData[offset++] = 0f
        // color value
        vertexData[offset++] = color[0]
        vertexData[offset++] = color[1]
        vertexData[offset++] = color[2]
        vertexData[offset++] = color[3]

        while (i <= numSlices + 1) {
            val angleInRadians = i.toFloat() / numSlices.toFloat() * (Math.PI.toFloat() * 2f)

            vertexData[offset++] = radius * cos(angleInRadians.toDouble()).toFloat()
            vertexData[offset++] = -length / 2.0f
            vertexData[offset++] = radius * sin(angleInRadians.toDouble()).toFloat()
            // normal vector
            vertexData[offset++] = -cos(angleInRadians.toDouble()).toFloat() / radius
            vertexData[offset++] = 0f
            vertexData[offset++] = -sin(angleInRadians.toDouble()).toFloat() / radius
            // color value
            vertexData[offset++] = color[0]
            vertexData[offset++] = color[1]
            vertexData[offset++] = color[2]
            vertexData[offset++] = color[3]
            i++

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
        numIndices = numSlices + 1

        val sphereVertexDataBuffer = ByteBuffer
                .allocateDirect(vertexData.size * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        sphereVertexDataBuffer.put(vertexData).position(0)

        GLES20.glGenBuffers(1, vbo_top, 0)

        if (vbo_top[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_top[0])
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, sphereVertexDataBuffer.capacity() * BYTES_PER_FLOAT,
                    sphereVertexDataBuffer, GLES20.GL_STATIC_DRAW)

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        } else {
            // errorHandler.handleError(ErrorHandler.ErrorType.BUFFER_CREATION_ERROR, "glGenBuffers");
            throw RuntimeException("error on buffer gen")
        }

        /*
         * *** bottom circular plate of cone ***
         */

        offset = 0
        vertexData[offset++] = 0f  // center of circle
        vertexData[offset++] = -length / 2.0f
        vertexData[offset++] = 0f
        // normal vector
        vertexData[offset++] = 0f
        vertexData[offset++] = 3f
        vertexData[offset++] = 0f
        // color value
        vertexData[offset++] = base_color[0]
        vertexData[offset++] = base_color[1]
        vertexData[offset++] = base_color[2]
        vertexData[offset++] = base_color[3]

        i = 0
        while (i <= numSlices + 1) {
            val angleInRadians = i.toFloat() / numSlices.toFloat() * (Math.PI.toFloat() * 2f)

            vertexData[offset++] = radius * cos(angleInRadians.toDouble()).toFloat()
            vertexData[offset++] = -length / 2.0f
            vertexData[offset++] = radius * -sin(angleInRadians.toDouble()).toFloat()
            // normal vector
            vertexData[offset++] = 0f
            vertexData[offset++] = 3f
            vertexData[offset++] = 0f
            // color value
            vertexData[offset++] = base_color[0]
            vertexData[offset++] = base_color[1]
            vertexData[offset++] = base_color[2]
            vertexData[offset++] = base_color[3]
            i++

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
        numIndices = numSlices + 1

        val sphereVertexDataBufferBottom = ByteBuffer
                .allocateDirect(vertexData.size * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        sphereVertexDataBufferBottom.put(vertexData).position(0)

        GLES20.glGenBuffers(1, vbo_bottom, 0)

        if (vbo_bottom[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_bottom[0])
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, sphereVertexDataBufferBottom.capacity() * BYTES_PER_FLOAT,
                    sphereVertexDataBufferBottom, GLES20.GL_STATIC_DRAW)

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
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
        var todo: Int
        todo = if (doWireframeRendering) {
            GLES20.GL_LINES
        } else {
            GLES20.GL_TRIANGLE_FAN
        }

        if (vbo_bottom[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_bottom[0])

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

            // Draw - no indexes
            GLES20.glDrawArrays(todo, 0, numIndices + 2)
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        }

        todo = if (doWireframeRendering) {
            GLES20.GL_LINES
        } else {
            GLES20.GL_TRIANGLE_FAN
        }
        if (vbo_top[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_top[0])

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

            // Draw - no indexes
            // GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
            // GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, numIndices, GLES20.GL_UNSIGNED_SHORT, 0);
            GLES20.glDrawArrays(todo, 0, numIndices + 2)
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
            // GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        }


    }

    fun release() {
        if (vbo_top[0] > 0) {
            GLES20.glDeleteBuffers(vbo_top.size, vbo_top, 0)
            vbo_top[0] = 0
        }
        if (vbo_bottom[0] > 0) {
            GLES20.glDeleteBuffers(vbo_bottom.size, vbo_bottom, 0)
            vbo_bottom[0] = 0
        }

        //        if (ibo[0] > 0) {
        //            GLES20.glDeleteBuffers(ibo.length, ibo, 0);
        //            ibo[0] = 0;
        //        }
    }

    companion object {
        private const val POSITION_DATA_SIZE_IN_ELEMENTS = 3
        private const val NORMAL_DATA_SIZE_IN_ELEMENTS = 3
        private const val COLOR_DATA_SIZE_IN_ELEMENTS = 4
        private const val BYTES_PER_FLOAT = 4
        // private const val BYTES_PER_SHORT = 2
        private const val STRIDE_IN_FLOATS = POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS + COLOR_DATA_SIZE_IN_ELEMENTS
        private const val STRIDE_IN_BYTES = STRIDE_IN_FLOATS * BYTES_PER_FLOAT
    }
}
