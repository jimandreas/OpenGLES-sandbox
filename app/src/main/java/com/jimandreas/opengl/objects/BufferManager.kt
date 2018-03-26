package com.jimandreas.opengl.objects

import android.opengl.GLES20
import android.opengl.GLES20.glEnableVertexAttribArray
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.*

/**
 * Buffer Manager
 *
 *
 * set up as a Singleton class to solve the following problems App-wide:
 *
 *
 * 1) allocates a single vertex array for accumulating triangles
 *
 * 2) allocates a single FloatArray for preparing to copy to GL.
 *
 * 3) Allocates GL buffers
 *
 * 4) Renders the GL buffers
 *
 *
 * for more information on Android recommended Singleton Patterns, as opposed to subclassing
 * the Application class (not recommended practice) - see this link:
 *
 *
 * http://developer.android.com/training/volley/requestqueue.html#singleton
 *
 *
 * and List interface:
 * https://docs.oracle.com/javase/tutorial/collections/interfaces/list.html
 */
object BufferManager {

    // private var mGLarrayList = ArrayList<GLArrayEntry>
    private var sCurrentGlArrayEntry = 0
    var sFloatArrayIndex: Int = 0
    
    fun allocateInitialBuffer() {
        if (sFloatArrayAlloatedAlready) {
        }
        sFloatArray = FloatArray(sFloatArraySize)
        
        sFloatArrayAlloatedAlready = true
        sFloatArrayIndex = 0
        sVertexDataFloatBuffer = ByteBuffer
                .allocateDirect(INITIAL_FLOAT_BUFFER_SIZE * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
    }

    fun getFloatArray(requestedNumFloats: Int): FloatArray {
        if (requestedNumFloats + sFloatArrayIndex < sFloatArraySize) {
            return sFloatArray
        }

        /*
         * no space.  have to:
         * 1) copy the current float array into the float buffer
         * 2) allocate a GL buffer
         * 3) copy the float buffer into the GL buffer
         */
        transferToGl()

        /*
         * OK that is done.   Now reset the vertex data float buffer
         */
        sVertexDataFloatBuffer.limit(sFloatArraySize)
        sFloatArrayIndex = 0
        return sFloatArray
    }

    fun transferToGl() {
        sVertexDataFloatBuffer.limit(sFloatArrayIndex)
        sVertexDataFloatBuffer
                .put(sFloatArray, 0, sFloatArrayIndex)
                .position(0)


        mGLarrayList.add(GLArrayEntry())
        val ae = mGLarrayList[sCurrentGlArrayEntry]
        GLES20.glGenBuffers(1, ae.gl_buf, 0)
        ae.numVertices = sFloatArrayIndex / STRIDE_IN_FLOATS
        val numbytes = sFloatArrayIndex * BYTES_PER_FLOAT

        if (ae.gl_buf[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, ae.gl_buf[0])
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, numbytes,
                    sVertexDataFloatBuffer, GLES20.GL_STATIC_DRAW)

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
            ae.buffer_allocated = true
        } else {
            // errorHandler(// do something );
            throw RuntimeException("error on buffer gen")
        }
        // dumpVertexList()
    }

    fun render(
            positionAttribute: Int,
            colorAttribute: Int,
            normalAttribute: Int,
            doWireframeRendering: Boolean) {

        var ae: GLArrayEntry

        // GLES20.glDisable(GLES20.GL_CULL_FACE);

        for (i in mGLarrayList.indices) {
            ae = mGLarrayList[i]

            if (ae.buffer_allocated == false) {
                continue
            }
            if (ae.gl_buf[0] > 0) {
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, ae.gl_buf[0])
                // associate the attributes with the bound buffer
                GLES20.glVertexAttribPointer(positionAttribute,
                        POSITION_DATA_SIZE_IN_ELEMENTS,
                        GLES20.GL_FLOAT,
                        false,
                        STRIDE_IN_BYTES,
                        0)  // offset
                glEnableVertexAttribArray(positionAttribute)

                GLES20.glVertexAttribPointer(normalAttribute, NORMAL_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                        STRIDE_IN_BYTES, POSITION_DATA_SIZE_IN_ELEMENTS * BYTES_PER_FLOAT)
                glEnableVertexAttribArray(normalAttribute)

                GLES20.glVertexAttribPointer(colorAttribute, COLOR_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                        STRIDE_IN_BYTES, (POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS) * BYTES_PER_FLOAT)
                glEnableVertexAttribArray(colorAttribute)

                // Draw
                val todo: Int
                if (doWireframeRendering) {
                    todo = GLES20.GL_LINES
                } else {
                    todo = GLES20.GL_TRIANGLES
                }

                GLES20.glDrawArrays(todo, 0, ae.numVertices)

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)  // release
            } else {
                // errorHandler(// do something );
                throw RuntimeException("buffer manager render: null buffer")
            }
        }

        // GLES20.glEnable(GLES20.GL_CULL_FACE);

    }

    /*  for debugging
    private fun dumpVertexList() {
        var nvx: Float
        var nvy: Float
        var nvz: Float
        var vx: Float
        var vy: Float
        var vz: Float
        var i: Int
        i = 0
        while (i < sFloatArrayIndex) {
            vx = sFloatArray[i + 0]
            vy = sFloatArray[i + 1]
            vz = sFloatArray[i + 2]
            val svx = String.format("%6.2f", vx)
            val svy = String.format("%6.2f", vy)
            val svz = String.format("%6.2f", vz)

            nvx = sFloatArray[i + 3]
            nvy = sFloatArray[i + 4]
            nvz = sFloatArray[i + 5]
            val snvx = String.format("%6.2f", nvx)
            val snvy = String.format("%6.2f", nvy)
            val snvz = String.format("%6.2f", nvz)

            Timber.i("vert " + i + " x y z nx ny nz "
                    + svx + " " + svy + " " + svz + " and " + snvx + " " + snvy + " " + snvz
                    //                    + " clr "
                    //                    + vertexData[i + 6] + " " + vertexData[i + 7] + " "
                    //                    + vertexData[i + 8] + " " + vertexData[i + 8]
            )
            i += STRIDE_IN_FLOATS
        }
    }
    */

    private const val INITIAL_FLOAT_BUFFER_SIZE = 150000

    private const val POSITION_DATA_SIZE_IN_ELEMENTS = 3
    private const val NORMAL_DATA_SIZE_IN_ELEMENTS = 3
    private const val COLOR_DATA_SIZE_IN_ELEMENTS = 4

    private const val BYTES_PER_FLOAT = 4
    private const val BYTES_PER_SHORT = 2

    private const val STRIDE_IN_FLOATS = POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS + COLOR_DATA_SIZE_IN_ELEMENTS
    private const val STRIDE_IN_BYTES = STRIDE_IN_FLOATS * BYTES_PER_FLOAT

    private var sFloatArrayAlloatedAlready = false
    lateinit var sFloatArray: FloatArray
    private const val sFloatArraySize = INITIAL_FLOAT_BUFFER_SIZE

    
    private lateinit var sVertexDataFloatBuffer: FloatBuffer

    private var mGLarrayList = ArrayList<GLArrayEntry>()

    /*
     * private class to track the allocated GL vertex buffers
     */
    class GLArrayEntry (
            var gl_buf:IntArray = IntArray(1),
            var numVertices: Int = 0,
            var buffer_allocated: Boolean = false )
}
