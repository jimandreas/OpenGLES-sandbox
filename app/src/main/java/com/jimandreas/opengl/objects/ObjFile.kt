package com.jimandreas.opengl.objects

import android.annotation.SuppressLint
import android.content.res.AssetManager
import android.opengl.GLES20
import android.os.Bundle
import android.os.SystemClock
import com.jimandreas.opengl.displayobjfile.ActivityDisplayObjFile
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

@SuppressLint("DefaultLocale")
class ObjFile(activity: ActivityDisplayObjFile) {
    private val assetManager: AssetManager = activity.assets
    private var haveMaterialColor = false
    private var materialColor = FloatArray(3)
    private var material = Bundle()
    private var triangleIndexCount: Int = 0
    private val vbo = IntArray(1)
    private val ibo = IntArray(1)

    var maxX = 0f
    var maxY = 0f
    var maxZ = 0f
    var minX = 1e6f
    var minY = 1e6f
    var minZ = 1e6f
    private var lastVertexNumber = 0

    private var vertices: MutableList<Float> = ArrayList()
    private var normals: MutableList<Float> = ArrayList()
    private var colors: MutableList<Float> = ArrayList()
    private var indices: MutableList<Int> = ArrayList()
    private var normalIndex: MutableList<Int> = ArrayList()
    private var textureIndex: MutableList<Int> = ArrayList()

    fun parse(objFileName: String) {
        // Timber.i("start parsing files = " + objFileName);
        val start_time = SystemClock.uptimeMillis().toFloat()

        flushAllBuffers()
        inputMaterialTemplateLibrary("$objFileName.mtl")
        parseObjFile("$objFileName.obj")

        val elapsed_time = (SystemClock.uptimeMillis() - start_time) / 1000
        val pretty_print = String.format("%6.2f", elapsed_time)

        Timber.i("finished parsing in $pretty_print seconds.")
        Timber.i("max xyz min xyz %7.2f %7.2f %7.2f and %7.2f %7.2f %7.2f",
                maxX, maxY, maxZ, minX, minY, minZ)
    }

    private fun inputMaterialTemplateLibrary(objFileName: String) {
        var inputStream: InputStream? = null
        val reader: BufferedReader?
        var line: String? = null
        try {
            inputStream = assetManager.open(objFileName, AssetManager.ACCESS_BUFFER)
            if (inputStream == null) {
                Timber.i("cannot open$objFileName, returning")
                return
            }

            reader = BufferedReader(InputStreamReader(inputStream))

            var name: String? = null
            line = reader.readLine()
            while (line != null) {
                // Timber.i("line is: " + line);
                if (line.isEmpty()) {
                    line = reader.readLine()
                    continue
                }
                if (line[0] == 'n' && line[1] == 'e') {
                    name = parseMaterialTemplateName(line)
                } else if (line[0] == 'K' && line[1] == 'a') {
                    parseKaColor(name, line)
                }
                line = reader.readLine()
            }
        } catch (e: IOException) {
            Timber.i("IO error in file $objFileName")
            if (line != null) {
                Timber.i("IO exception at line: $line")
            }
        } finally {
            inputStream?.close()
        }
    }

    /*
     * ParseKaColorLine
     *   Assumptions:
     *
     */
    private fun parseKaColor(mat_name: String?, line: String) {

        if (mat_name == null) {
            return
        }
        var first_float = line.substring(3)
        first_float = first_float.trim { it <= ' ' }
        val second_space_index = first_float.indexOf(' ') + 1
        var second_float = first_float.substring(second_space_index)
        second_float = second_float.trim { it <= ' ' }
        val third_space_index = second_float.indexOf(' ') + 1
        var third_float = second_float.substring(third_space_index)
        third_float = third_float.trim { it <= ' ' }

        val color = FloatArray(3)
        color[0] = parseFloat(first_float.substring(0, second_space_index - 1))
        color[1] = parseFloat(second_float.substring(0, third_space_index - 1))
        color[2] = parseFloat(third_float)

        material.putFloatArray(mat_name, color)
        haveMaterialColor = true
    }

    /*
     * ParseMaterialTemplateName
     *   Assumptions:
     *     picking just the Ka will work on the binding between name and color value
     */
    private fun parseMaterialTemplateName(line: String): String {
        val space_index = line.indexOf(' ') + 1
        var mtl_name = line.substring(space_index)
        mtl_name = mtl_name.trim { it <= ' ' }
        return mtl_name
    }

    private fun parseObjFile(objFileName: String) {
        var inputStream: InputStream? = null
        var line: String? = null
        try {
            inputStream = assetManager.open(objFileName, AssetManager.ACCESS_BUFFER)
            if (inputStream == null) {
                Timber.e("cannot open$objFileName, returning")
                return
            }

            val reader = BufferedReader(InputStreamReader(inputStream))
            line = reader.readLine()
            while (line != null) {
                // Timber.i("line is: " + line);
                if (line.isEmpty()) {
                    line = reader.readLine()
                    continue
                }
                if (line[0] == 'v' && line[1] == ' ') {
                    parseVertex(line)
                } else if (line[0] == 'v' && line[1] == 'n') {
                    parseNormal(line)
                } else if (line[0] == 'f') {
                    parseTriangle(line)
                } else if (line[0] == 'u' && line[1] == 's') {
                    parseUsemtl(line)
                }
                line = reader.readLine()
            }
        } catch (e: IOException) {
            Timber.e("IO error in file $objFileName")
            if (line != null) {
                Timber.e("IO exception at line: $line")
            }
        } finally {
            inputStream?.close()
        }

    }

    /**
     * ParseMaterialTemplateName
     * Assumptions:
     * picking just the Ka will work on the binding between name and color value
     */
    private fun parseUsemtl(line: String) {
        val space_index = line.indexOf(' ') + 1
        var mtl_name = line.substring(space_index)
        mtl_name = mtl_name.trim { it <= ' ' }
        val material_color = material.getFloatArray(mtl_name) ?: return
        if (material_color.size != 3) {
            return
        }
        materialColor[0] = material_color[0]
        materialColor[1] = material_color[1]
        materialColor[2] = material_color[2]
        haveMaterialColor = true
    }

    /*
     * ParseTriangle
     *   Assumptions:
     *     exactly one space between the 'v' and the integer
     *     exactly one space between integer
     */
    private fun parseTriangle(line: String) {
        var first_integer = line.substring(2)
        first_integer = first_integer.trim { it <= ' ' }
        val second_space_index = first_integer.indexOf(' ') + 1
        var second_integer = first_integer.substring(second_space_index)
        second_integer = second_integer.trim { it <= ' ' }
        val third_space_index = second_integer.indexOf(' ') + 1
        var third_integer = second_integer.substring(third_space_index)
        third_integer = third_integer.trim { it <= ' ' }

        parseTriplet(first_integer.substring(0, second_space_index - 1))
        parseTriplet(second_integer.substring(0, third_space_index - 1))
        parseTriplet(third_integer)
    }

    private fun parseTriplet(item: String) {

        // TODO: handle negative indexing of normal and texture indices
        var vertex: Int

        val first_slash = item.indexOf('/')
        if (first_slash == -1) {
            vertex = parseInteger(item)
            if (vertex < 0) {
                vertex += lastVertexNumber
            }
            indices.add(vertex)
            return
        }
        // wait wait there are more indices in this line
        vertex = parseInteger(item.substring(0, first_slash))
        if (vertex < 0) {
            vertex += lastVertexNumber
        }
        indices.add(vertex)
        val leftover = item.substring(first_slash + 1, item.length)
        val second_slash = leftover.indexOf('/')
        if (second_slash == -1) {
            textureIndex.add(parseInteger(leftover))
            return
        }
        if (second_slash == 0) {
            normalIndex.add(parseInteger(leftover.substring(1, leftover.length)))
        }
    }


    /**
     * ParseNormal
     * Assumptions:
     * exactly one space between the 'v' and the float
     * exactly one space between floats
     */
    private fun parseNormal(line: String) {

        val first_float = line.substring(2)
        val second_space_index = first_float.indexOf(' ') + 1
        val second_float = first_float.substring(second_space_index)
        val third_space_index = second_float.indexOf(' ') + 1

        val vx = parseFloat(first_float.substring(0, second_space_index - 1))
        val vy = parseFloat(second_float.substring(0, third_space_index - 1))
        val vz = parseFloat(second_float.substring(third_space_index))

        normals.add(vx)
        normals.add(vy)
        normals.add(vz)
    }

    /*
     * ParseVertex
     *   Assumptions:
     *     exactly one space between the 'v' and the float
     *     exactly one space between floats
     */
    private fun parseVertex(line: String) {

        var first_float = line.substring(2)
        first_float = first_float.trim { it <= ' ' }
        val second_space_index = first_float.indexOf(' ') + 1
        var second_float = first_float.substring(second_space_index)
        second_float = second_float.trim { it <= ' ' }
        val third_space_index = second_float.indexOf(' ') + 1
        var third_float = second_float.substring(third_space_index)
        third_float = third_float.trim { it <= ' ' }

        val vx = parseFloat(first_float.substring(0, second_space_index - 1))
        val vy = parseFloat(second_float.substring(0, third_space_index - 1))
        val vz = parseFloat(third_float)

        maxX = Math.max(maxX, vx)
        maxY = Math.max(maxY, vy)
        maxZ = Math.max(maxZ, vz)

        minX = Math.min(minX, vx)
        minY = Math.min(minY, vy)
        minZ = Math.min(minZ, vz)

        vertices.add(vx)
        vertices.add(vy)
        vertices.add(vz)
        lastVertexNumber++

        if (haveMaterialColor) {
            colors.add(materialColor[0])
            colors.add(materialColor[1])
            colors.add(materialColor[2])
        }
    }

    private fun parseFloat(s: String): Float {
        try {
            return java.lang.Float.parseFloat(s)
        } catch (e: RuntimeException) {
            return 0f
        }

    }

    private fun parseInteger(s: String): Int {
        try {
            return Integer.parseInt(s)
        } catch (e: RuntimeException) {
            Timber.e("Bad Integer : $s")
            return 0
        }

    }

    /*
     * pull the data from the buffers and assemble
     * a packed VBO (vertex + normal + color) buffer,
     * and an indices buffer.
     *
     * Walk the indices list
     * to pull the triangle vertices, calculate the normals,
     * and stuff them back into the packed VBO.
     *
     * TODO: use the normals if supplied.
     *   Right now the code just does its own calculation
     *   for normals.
     */
    fun build_buffers(color: FloatArray /*RGBA*/) {
        var i = 0
        var offset = 0
        val vertexData = FloatArray(
                vertices.size / 3 * STRIDE_IN_FLOATS)

        /*
         * loop to generate vertices.
         */
        while (i < vertices.size) {

            vertexData[offset++] = vertices[i + 0]
            vertexData[offset++] = vertices[i + 1]
            vertexData[offset++] = vertices[i + 2]

            vertexData[offset++] = 0.0f // set normal to zero for now
            vertexData[offset++] = 0.0f
            vertexData[offset++] = 0.0f

            if (haveMaterialColor) {
                vertexData[offset++] = colors[i + 0]
                vertexData[offset++] = colors[i + 1]
                vertexData[offset++] = colors[i + 2]
                vertexData[offset++] = 1.0f  // TODO: unwire the alpha?
            } else {
                // color value
                vertexData[offset++] = color[0]
                vertexData[offset++] = color[1]
                vertexData[offset++] = color[2]
                vertexData[offset++] = color[3]
            }
            i += 3
        }

        // calculate the normal,
        // set it in the packed VBO.
        // If current normal is non-zero, average it with previous value.

        var v1i: Int
        var v2i: Int
        var v3i: Int
        i = 0
        while (i < indices.size) {
            v1i = indices[i + 0] - 1
            v2i = indices[i + 1] - 1
            v3i = indices[i + 2] - 1

            v1[0] = vertices[v1i * 3 + 0]
            v1[1] = vertices[v1i * 3 + 1]
            v1[2] = vertices[v1i * 3 + 2]

            v2[0] = vertices[v2i * 3 + 0]
            v2[1] = vertices[v2i * 3 + 1]
            v2[2] = vertices[v2i * 3 + 2]

            v3[0] = vertices[v3i * 3 + 0]
            v3[1] = vertices[v3i * 3 + 1]
            v3[2] = vertices[v3i * 3 + 2]

            n = XYZ.getNormal(v1, v2, v3)

            vertexData[v1i * STRIDE_IN_FLOATS + 3 + 0] = n[0] * NORMAL_BRIGHTNESS_FACTOR
            vertexData[v1i * STRIDE_IN_FLOATS + 3 + 1] = n[1] * NORMAL_BRIGHTNESS_FACTOR
            vertexData[v1i * STRIDE_IN_FLOATS + 3 + 2] = n[2] * NORMAL_BRIGHTNESS_FACTOR

            vertexData[v2i * STRIDE_IN_FLOATS + 3 + 0] = n[0] * NORMAL_BRIGHTNESS_FACTOR
            vertexData[v2i * STRIDE_IN_FLOATS + 3 + 1] = n[1] * NORMAL_BRIGHTNESS_FACTOR
            vertexData[v2i * STRIDE_IN_FLOATS + 3 + 2] = n[2] * NORMAL_BRIGHTNESS_FACTOR

            vertexData[v3i * STRIDE_IN_FLOATS + 3 + 0] = n[0] * NORMAL_BRIGHTNESS_FACTOR
            vertexData[v3i * STRIDE_IN_FLOATS + 3 + 1] = n[1] * NORMAL_BRIGHTNESS_FACTOR
            vertexData[v3i * STRIDE_IN_FLOATS + 3 + 2] = n[2] * NORMAL_BRIGHTNESS_FACTOR
            i += 3

        }

        /*
         * debug - print out list of formated vertex data
         */
        //        for (i = 0; i < vertexData.length; i+= STRIDE_IN_FLOATS) {
        //            vx = vertexData[i + 0];
        //            vy = vertexData[i + 1];
        //            vz = vertexData[i + 2];
        //            String svx = String.format("%6.2f", vx);
        //            String svy = String.format("%6.2f", vy);
        //            String svz = String.format("%6.2f", vz);
        //
        //            Timber("data ", i + " x y z "
        //                    + svx + " " + svy + " " + svz + " and color = "
        //                    + vertexData[i + 6] + " " + vertexData[i + 7] + " " + vertexData[i + 8]);
        //        }

        val vertexDataBuffer = ByteBuffer
                .allocateDirect(vertexData.size * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        vertexDataBuffer.put(vertexData).position(0)

        if (vbo[0] > 0) {
            GLES20.glDeleteBuffers(1, vbo, 0)
        }
        GLES20.glGenBuffers(1, vbo, 0)

        if (vbo[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexDataBuffer.capacity() * BYTES_PER_FLOAT,
                    vertexDataBuffer, GLES20.GL_STATIC_DRAW)

            // GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        } else {
            // errorHandler.handleError(ErrorHandler.ErrorType.BUFFER_CREATION_ERROR, "glGenBuffers");
            throw RuntimeException("error on buffer gen")
        }

        /*
         * create the buffer for the indices
         */
        offset = 0
        var x: Int
        val indexData = ShortArray(indices.size)
        x = 0
        while (x < indices.size) {

            var index = indices[x].toShort()
            indexData[offset++] = --index
            x++
        }
        triangleIndexCount = indexData.size

        /*
         * debug - print out list of formated vertex data
         */
        //        short ix, iy, iz;
        //        for (i = 0; i < indexData.length; i += 3) {
        //            ix = indexData[i + 0];
        //            iy = indexData[i + 1];
        //            iz = indexData[i + 2];
        //
        //            Timber("data ", i + " i1 i2 i3 "
        //                    + ix + " " + iy + " " + iz );
        //        }

        val indexDataBuffer = ByteBuffer
                .allocateDirect(indexData.size * BYTES_PER_SHORT).order(ByteOrder.nativeOrder())
                .asShortBuffer()
        indexDataBuffer.put(indexData).position(0)

        if (ibo[0] > 0) {
            GLES20.glDeleteBuffers(1, ibo, 0)
        }
        GLES20.glGenBuffers(1, ibo, 0)
        if (ibo[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0])
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER,
                    indexDataBuffer.capacity() * BYTES_PER_SHORT, indexDataBuffer, GLES20.GL_STATIC_DRAW)
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
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

        // Debug: disable culling to remove back faces.
        //GLES20.glDisable(GLES20.GL_CULL_FACE);

        // TODO : make sure the buffer is NOT released before the Indexes are bound!!
        /*
         * draw using the IBO - index buffer object,
         * and the revised vertex data that has
         * corrected normals for the body.
         */
        if (vbo[0] > 0 && ibo[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])
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

            // Draw
            val todo: Int
            if (doWireframeRendering) {
                todo = GLES20.GL_LINES
            } else {
                todo = GLES20.GL_TRIANGLES
            }

            /*
             * draw using the IBO - index buffer object
             */
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0])
            GLES20.glDrawElements(
                    todo, /* GLES20.GL_TRIANGLES, */
                    triangleIndexCount,
                    GLES20.GL_UNSIGNED_SHORT,
                    0)
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0) // release
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)  // release
        }
        // Debug:  Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE)
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

    // clean out old data, reset state
    private fun flushAllBuffers() {
        maxX = 0f
        maxY = 0f
        maxZ = 0f
        minX = 1e6f
        minY = 1e6f
        minZ = 1e6f
        lastVertexNumber = 0 // zero based counting :-)
        vertices.clear()
        normals.clear()
        colors.clear()
        indices.clear()
        textureIndex.clear()
        haveMaterialColor = false
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

        private var v1 = FloatArray(3)
        private var v2 = FloatArray(3)
        private var v3 = FloatArray(3)
        private var n = FloatArray(3)
    }

}