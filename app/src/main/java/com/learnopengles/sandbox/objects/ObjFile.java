package com.learnopengles.sandbox.objects;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.SystemClock;
import timber.log.Timber;

import com.learnopengles.sandbox.displayobjfile.ActivityDisplayObjFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("DefaultLocale")
public class ObjFile {

    private static String LOG_TAG = "ObjFile";
    private AssetManager mAssetManager;
    boolean mHaveMaterialColor = false;
    float[] mMaterialColor = new float[3];

    Bundle mMaterial = new Bundle();

    public ObjFile(ActivityDisplayObjFile mActivity) {
        mAssetManager = mActivity.getAssets();
    }

    public void parse(String objFileName) {
        // Timber.i("start parsing files = " + objFileName);
        float start_time = SystemClock.uptimeMillis();

        flushAllBuffers();
        inputMaterialTemplateLibrary(objFileName.concat(".mtl"));
        parseObjFile(objFileName.concat(".obj"));

        float elapsed_time = (SystemClock.uptimeMillis() - start_time) / 1000;
        String pretty_print = String.format("%6.2f", elapsed_time);

        Timber.i("finished parsing in " + pretty_print + " seconds.");
        Timber.i("max xyz min xyz" + mMaxX + " " + mMaxY + " " + mMaxZ + " and "
                + mMinX + " " + mMinY + " " + mMinZ);
    }

    private void inputMaterialTemplateLibrary(String objFileName) {
        InputStream inputStream;
        BufferedReader reader = null;
        String line = null;
        try {
            inputStream = mAssetManager.open(objFileName, AssetManager.ACCESS_BUFFER);
            if (inputStream == null) {
                Timber.i("cannot open" + objFileName + ", returning");
                return;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String name = null;
            while ((line = reader.readLine()) != null) {
                // Timber.i("line is: " + line);
                if (line.length() == 0) {
                    continue;
                }
                if ((line.charAt(0) == 'n') && (line.charAt(1) == 'e')) {
                    name = parseMaterialTemplateName(line);
                } else if ((line.charAt(0) == 'K') && (line.charAt(1) == 'a')) {
                    parseKaColor(name, line);
                }
            }
        } catch (IOException e) {
            Timber.i("IO error in file " + objFileName);
            if (line != null) {
                Timber.i("IO exception at line: " + line);
            }

        }
    }

    /*
     * ParseKaColorLine
     *   Assumptions:
     *
     */
    private void parseKaColor(String mat_name, String line) {

        if (mat_name == null) {
            return;
        }
        String first_float = line.substring(3);
        first_float = first_float.trim();
        int second_space_index = first_float.indexOf(' ') + 1;
        String second_float = first_float.substring(second_space_index);
        second_float = second_float.trim();
        int third_space_index = second_float.indexOf(' ') + 1;
        String third_float = second_float.substring(third_space_index);
        third_float = third_float.trim();

        float[] color = new float[3];
        color[0] = parseFloat(first_float.substring(0, second_space_index - 1));
        color[1] = parseFloat(second_float.substring(0, third_space_index - 1));
        color[2] = parseFloat(third_float);

        mMaterial.putFloatArray(mat_name, color);
        mHaveMaterialColor = true;
    }

    /*
     * ParseMaterialTemplateName
     *   Assumptions:
     *     picking just the Ka will work on the binding between name and color value
     */
    private String parseMaterialTemplateName(String line) {
        int space_index = line.indexOf(' ') + 1;
        String mtl_name = line.substring(space_index);
        mtl_name = mtl_name.trim();
        return mtl_name;
    }

    private void parseObjFile(String objFileName) {
        InputStream inputStream;
        BufferedReader reader = null;
        String line = null;
        try {
            inputStream = mAssetManager.open(objFileName, AssetManager.ACCESS_BUFFER);
            if (inputStream == null) {
                Timber.e("cannot open" + objFileName + ", returning");
                return;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            while ((line = reader.readLine()) != null) {
                // Timber.i("line is: " + line);
                if (line.length() == 0) {
                    continue;
                }
                if ((line.charAt(0) == 'v') && (line.charAt(1) == ' ')) {
                    parseVertex(line);
                } else if ((line.charAt(0) == 'v') && (line.charAt(1) == 'n')) {
                    parseNormal(line);
                } else if (line.charAt(0) == 'f') {
                    parseTriangle(line);
                } else if ((line.charAt(0) == 'u') && (line.charAt(1) == 's')) {
                    parseUsemtl(line);
                }
            }
        } catch (IOException e) {
            Timber.e("IO error in file " + objFileName);
            if (line != null) {
                Timber.e("IO exception at line: " + line);
            }
        }
    }

    /**
     * ParseMaterialTemplateName
     * Assumptions:
     * picking just the Ka will work on the binding between name and color value
     */
    private void parseUsemtl(String line) {
        int space_index = line.indexOf(' ') + 1;
        String mtl_name = line.substring(space_index);
        mtl_name = mtl_name.trim();
        float[] material_color = mMaterial.getFloatArray(mtl_name);
        if (material_color == null) {
            return;
        }
        if (material_color.length != 3) {
            return;
        }
        mMaterialColor[0] = material_color[0];
        mMaterialColor[1] = material_color[1];
        mMaterialColor[2] = material_color[2];
        mHaveMaterialColor = true;
    }

    /*
     * ParseTriangle
     *   Assumptions:
     *     exactly one space between the 'v' and the integer
     *     exactly one space between integer
     */
    private void parseTriangle(String line) {
        String first_integer = line.substring(2);
        first_integer = first_integer.trim();
        int second_space_index = first_integer.indexOf(' ') + 1;
        String second_integer = first_integer.substring(second_space_index);
        second_integer = second_integer.trim();
        int third_space_index = second_integer.indexOf(' ') + 1;
        String third_integer = second_integer.substring(third_space_index);
        third_integer = third_integer.trim();

        parseTriplet(first_integer.substring(0, second_space_index - 1));
        parseTriplet(second_integer.substring(0, third_space_index - 1));
        parseTriplet(third_integer);
    }

    private void parseTriplet(String item) {

        // TODO: handle negative indexing of normal and texture indices
        int vertex;

        int first_slash = item.indexOf('/');
        if (first_slash == -1) {
            vertex = parseInteger(item);
            if (vertex < 0) {
                vertex += mLastVertexNumber;
            }
            mIndices.add(vertex);
            return;
        }
        // wait wait there are more indices in this line
        vertex = parseInteger(item.substring(0, first_slash));
        if (vertex < 0) {
            vertex += mLastVertexNumber;
        }
        mIndices.add(vertex);
        String leftover = item.substring(first_slash + 1, item.length());
        int second_slash = leftover.indexOf('/');
        if (second_slash == -1) {
            mTextureIndex.add(parseInteger(leftover));
            return;
        }
        if (second_slash == 0) {
            mNormalIndex.add(parseInteger(leftover.substring(1, leftover.length())));
        }
    }


    /**
     * ParseNormal
     * Assumptions:
     * exactly one space between the 'v' and the float
     * exactly one space between floats
     */
    private void parseNormal(String line) {

        String first_float = line.substring(2);
        int second_space_index = first_float.indexOf(' ') + 1;
        String second_float = first_float.substring(second_space_index);
        int third_space_index = second_float.indexOf(' ') + 1;

        float vx = parseFloat(first_float.substring(0, second_space_index - 1));
        float vy = parseFloat(second_float.substring(0, third_space_index - 1));
        float vz = parseFloat(second_float.substring(third_space_index));

        mNormals.add(vx);
        mNormals.add(vy);
        mNormals.add(vz);
    }

    /*
     * ParseVertex
     *   Assumptions:
     *     exactly one space between the 'v' and the float
     *     exactly one space between floats
     */
    private void parseVertex(String line) {

        String first_float = line.substring(2);
        first_float = first_float.trim();
        int second_space_index = first_float.indexOf(' ') + 1;
        String second_float = first_float.substring(second_space_index);
        second_float = second_float.trim();
        int third_space_index = second_float.indexOf(' ') + 1;
        String third_float = second_float.substring(third_space_index);
        third_float = third_float.trim();

        float vx = parseFloat(first_float.substring(0, second_space_index - 1));
        float vy = parseFloat(second_float.substring(0, third_space_index - 1));
        float vz = parseFloat(third_float);

        mMaxX = Math.max(mMaxX, vx);
        mMaxY = Math.max(mMaxY, vy);
        mMaxZ = Math.max(mMaxZ, vz);

        mMinX = Math.min(mMinX, vx);
        mMinY = Math.min(mMinY, vy);
        mMinZ = Math.min(mMinZ, vz);

        mVertices.add(vx);
        mVertices.add(vy);
        mVertices.add(vz);
        mLastVertexNumber++;

        if (mHaveMaterialColor) {
            mColors.add(mMaterialColor[0]);
            mColors.add(mMaterialColor[1]);
            mColors.add(mMaterialColor[2]);
        }
    }

    private float parseFloat(String s) {
        try {
            return Float.parseFloat(s);
        } catch (RuntimeException e) {
            return 0f;
        }
    }

    private int parseInteger(String s) {
        try {
            return Integer.parseInt(s);
        } catch (RuntimeException e) {
            Timber.e("Bad Integer : " + s);
            return 0;
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
    public void build_buffers(float[] color /*RGBA*/) {
        int i;
        int offset = 0;
        final float[] vertexData = new float[
                mVertices.size() / 3
                        * STRIDE_IN_FLOATS];

        float vx, vy, vz;

        /*
         * loop to generate vertices.
         */
        for (i = 0; i < mVertices.size(); i += 3) {

            vertexData[offset++] = mVertices.get(i + 0);
            vertexData[offset++] = mVertices.get(i + 1);
            vertexData[offset++] = mVertices.get(i + 2);

            vertexData[offset++] = 0.0f; // set normal to zero for now
            vertexData[offset++] = 0.0f;
            vertexData[offset++] = 0.0f;

            if (mHaveMaterialColor) {
                vertexData[offset++] = mColors.get(i + 0);
                vertexData[offset++] = mColors.get(i + 1);
                vertexData[offset++] = mColors.get(i + 2);
                vertexData[offset++] = 1.0f;  // TODO: unwire the alpha?
            } else {
                // color value
                vertexData[offset++] = color[0];
                vertexData[offset++] = color[1];
                vertexData[offset++] = color[2];
                vertexData[offset++] = color[3];
            }
        }

        // calculate the normal,
        // set it in the packed VBO.
        // If current normal is non-zero, average it with previous value.

        int v1i, v2i, v3i;
        for (i = 0; i < mIndices.size(); i += 3) {
            v1i = mIndices.get(i + 0) - 1;
            v2i = mIndices.get(i + 1) - 1;
            v3i = mIndices.get(i + 2) - 1;

            v1[0] = mVertices.get(v1i * 3 + 0);
            v1[1] = mVertices.get(v1i * 3 + 1);
            v1[2] = mVertices.get(v1i * 3 + 2);

            v2[0] = mVertices.get(v2i * 3 + 0);
            v2[1] = mVertices.get(v2i * 3 + 1);
            v2[2] = mVertices.get(v2i * 3 + 2);

            v3[0] = mVertices.get(v3i * 3 + 0);
            v3[1] = mVertices.get(v3i * 3 + 1);
            v3[2] = mVertices.get(v3i * 3 + 2);

            n = XYZ.getNormal(v1, v2, v3);

            vertexData[v1i * STRIDE_IN_FLOATS + 3 + 0] = n[0] * NORMAL_BRIGHTNESS_FACTOR;
            vertexData[v1i * STRIDE_IN_FLOATS + 3 + 1] = n[1] * NORMAL_BRIGHTNESS_FACTOR;
            vertexData[v1i * STRIDE_IN_FLOATS + 3 + 2] = n[2] * NORMAL_BRIGHTNESS_FACTOR;

            vertexData[v2i * STRIDE_IN_FLOATS + 3 + 0] = n[0] * NORMAL_BRIGHTNESS_FACTOR;
            vertexData[v2i * STRIDE_IN_FLOATS + 3 + 1] = n[1] * NORMAL_BRIGHTNESS_FACTOR;
            vertexData[v2i * STRIDE_IN_FLOATS + 3 + 2] = n[2] * NORMAL_BRIGHTNESS_FACTOR;

            vertexData[v3i * STRIDE_IN_FLOATS + 3 + 0] = n[0] * NORMAL_BRIGHTNESS_FACTOR;
            vertexData[v3i * STRIDE_IN_FLOATS + 3 + 1] = n[1] * NORMAL_BRIGHTNESS_FACTOR;
            vertexData[v3i * STRIDE_IN_FLOATS + 3 + 2] = n[2] * NORMAL_BRIGHTNESS_FACTOR;

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

        final FloatBuffer vertexDataBuffer = ByteBuffer
                .allocateDirect(vertexData.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexDataBuffer.put(vertexData).position(0);

        if (vbo[0] > 0) {
            GLES20.glDeleteBuffers(1, vbo, 0);
        }
        GLES20.glGenBuffers(1, vbo, 0);

        if (vbo[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexDataBuffer.capacity() * BYTES_PER_FLOAT,
                    vertexDataBuffer, GLES20.GL_STATIC_DRAW);

            // GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        } else {
            // errorHandler.handleError(ErrorHandler.ErrorType.BUFFER_CREATION_ERROR, "glGenBuffers");
            throw new RuntimeException("error on buffer gen");
        }

        /*
         * create the buffer for the indices
         */
        offset = 0;
        int x;
        final short[] indexData = new short[mIndices.size()];
        for (x = 0; x < mIndices.size(); x++) {

            short index = mIndices.get(x).shortValue();
            indexData[offset++] = --index;
        }
        mTriangleIndexCount = indexData.length;

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

        final ShortBuffer indexDataBuffer = ByteBuffer
                .allocateDirect(indexData.length * BYTES_PER_SHORT).order(ByteOrder.nativeOrder())
                .asShortBuffer();
        indexDataBuffer.put(indexData).position(0);

        if (ibo[0] > 0) {
            GLES20.glDeleteBuffers(1, ibo, 0);
        }
        GLES20.glGenBuffers(1, ibo, 0);
        if (ibo[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER,
                    indexDataBuffer.capacity()
                            * BYTES_PER_SHORT, indexDataBuffer, GLES20.GL_STATIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        } else {
            // errorHandler.handleError(ErrorHandler.ErrorType.BUFFER_CREATION_ERROR, "glGenBuffers");
            throw new RuntimeException("error on buffer gen");
        }
    }

    public void render(
            int positionAttribute,
            int colorAttribute,
            int normalAttribute,
            boolean doWireframeRendering) {

        // Debug: disable culling to remove back faces.
        GLES20.glDisable(GLES20.GL_CULL_FACE);

        // TODO : make sure the buffer is NOT released before the Indexes are bound!!
        /*
         * draw using the IBO - index buffer object,
         * and the revised vertex data that has
         * corrected normals for the body.
         */
        if ((vbo[0] > 0) && (ibo[0] > 0)) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
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

            // Draw
            int todo;
            if (doWireframeRendering) {
                todo = GLES20.GL_LINES;
            } else {
                todo = GLES20.GL_TRIANGLES;
            }

            /*
             * draw using the IBO - index buffer object
             */
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
            GLES20.glDrawElements(
                    todo, /* GLES20.GL_TRIANGLES, */
                    mTriangleIndexCount,
                    GLES20.GL_UNSIGNED_SHORT,
                    1);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0); // release
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);  // release
        }
        // Debug:  Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE);
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

    // clean out old data, reset state
    private void flushAllBuffers() {
        mMaxX = 0f;
        mMaxY = 0f;
        mMaxZ = 0f;
        mMinX = 1e6f;
        mMinY = 1e6f;
        mMinZ = 1e6f;
        mLastVertexNumber = 0; // zero based counting :-)
        mVertices.clear();
        mNormals.clear();
        mColors.clear();
        mIndices.clear();
        mTextureIndex.clear();
        mHaveMaterialColor = false;
    }

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
    private int mTriangleIndexCount;
    final int[] vbo = new int[1];
    final int[] ibo = new int[1];

    public float mMaxX = 0f;
    public float mMaxY = 0f;
    public float mMaxZ = 0f;
    public float mMinX = 1e6f;
    public float mMinY = 1e6f;
    public float mMinZ = 1e6f;
    private int mLastVertexNumber = 0;

    List<Float> mVertices = new ArrayList<>();
    List<Float> mNormals = new ArrayList<>();
    List<Float> mColors = new ArrayList<>();
    List<Integer> mIndices = new ArrayList<>();
    List<Integer> mNormalIndex = new ArrayList<>();
    List<Integer> mTextureIndex = new ArrayList<>();

    static float[] v1 = new float[3];
    static float[] v2 = new float[3];
    static float[] v3 = new float[3];
    static float[] n = new float[3];

}
