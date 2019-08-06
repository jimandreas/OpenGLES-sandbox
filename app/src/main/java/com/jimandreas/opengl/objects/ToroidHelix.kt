@file:Suppress("FunctionName", "LocalVariableName", "unused")

package com.jimandreas.opengl.objects


/*
 * Algorithm credit:
 * http://userpages.umbc.edu/~squire/cs437_lect.html
 * Lecture 14, Curves and Surfaces, targets
 * http://userpages.umbc.edu/~squire/download/make_helix_635.c
 */

/*
 * modifications to the original algorithm (jim a):
 *
 * walk the indices to generate normals for each TRI, and then
 * assemble the TRIs into a packed VBO (vertex XYZ + Normal + color4f).
 *
 * some of the original code and secondary steps are commented out but left in
 *  - this makes things a bit messy but helps to sort of document the progression
 *
 * reference (great for study of packed VBO's:
 *
 * http://www.learnopengles.com/android-lesson-seven-an-introduction-to-vertex-buffer-objects-vbos/
 */

import android.os.SystemClock
import timber.log.Timber
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class ToroidHelix(val mColor: FloatArray /*RGBA*/)
{
    private var mNumIndices = 0

    private var raw_x = Array(LINES) { FloatArray(POINTS) }
    private var raw_y = Array(LINES) { FloatArray(POINTS) }
    private var raw_z = Array(LINES) { FloatArray(POINTS) }
    private var raw_index = Array(LINES) { IntArray(POINTS) }

    private var vertexData: FloatArray
    private var offset: Int = 0

    init {
        sCount = 0   // 12288 is normal count

        vertexData = BufferManager.getFloatArray(12288 * STRIDE_IN_FLOATS)
        offset = BufferManager.sFloatArrayIndex

        val start_time = SystemClock.uptimeMillis().toFloat()
        Timber.i("start calculation")

        //        int[] tri_raw_p1 = new int[TRIS];
        //        int[] tri_raw_p2 = new int[TRIS];
        //        int[] tri_raw_p3 = new int[TRIS];

        var phi: Float
        var theta: Float
        var phi1: Float = 0.0f
        var theta1: Float
        var i: Int = 0
        var j: Int
        var points: Int = 1
        var polys: Int = 0
        var x1: Float
        var y1: Float
        var z1: Float
        var x2: Float
        var y2: Float
        var z2: Float
        var x3: Float
        var y3: Float
        var z3: Float
        var x2d: Float
        var y2d: Float
        var z2d: Float
        var r2d: Float
        var r2n: Float
        var vx2: Float
        var vy2: Float
        var vz2: Float
        var rx2: Float
        var ry2: Float
        var rz2: Float
        val pi = 3.141592653589793238462643383279502884197f
        val r1 = 8.0f  /* major radius of torus */
        val r2 = 4.0f  /* minor radius of torus */
        val r3 = 1.0f  /* minor radius of helix */
        val F = 8.0f  /* wrapping factor of r2 around r1 */

        /*   for big helix  */
        //        float phi_step = pi / 128.0f;
        //        int nx = 257;
        //
        //        float theta_step = pi / 8.0f;
        //        int ny = 17;

        /* for smooth shaded helix, smaller steps */
        val phi_step = pi / 64.0f
        val nx = 129
        val theta_step = pi / 8.0f
        val ny = 17

        /* that is the standard */

        /* loop around toride at radius r2, around that at r2 */
        /* this makes x1+x2, the center of the generated figure */

        while (i < nx) {
            phi = phi1
            phi1 += phi_step
            x1 = r1 * sin(phi.toDouble()).toFloat()
            y1 = r1 * cos(phi.toDouble()).toFloat()
            z1 = 0.0f

            x2 = x1 + r2 * sin(phi.toDouble()).toFloat() * cos((phi * F).toDouble()).toFloat() /* F is number of helix loops */
            y2 = y1 + r2 * cos(phi.toDouble()).toFloat() * cos((phi * F).toDouble()).toFloat()
            z2 = z1 + r2 * sin((phi * F).toDouble()).toFloat()

            /* the derivative of x1+x2 to get velocity vector direction */
            x2d = r1 * cos(phi.toDouble()).toFloat() + r2 * cos(phi.toDouble()).toFloat() * cos((phi * F).toDouble()).toFloat() - F * r2 * sin(phi.toDouble()).toFloat() * sin((phi * F).toDouble()).toFloat()
            y2d = -r1 * sin(phi.toDouble()).toFloat() - r2 * sin(phi.toDouble()).toFloat() * cos((phi * F).toDouble()).toFloat() - F * r2 * cos(phi.toDouble()).toFloat() * sin((phi * F).toDouble()).toFloat()
            z2d = F * r2 * cos((phi * F).toDouble()).toFloat()
            r2d = sqrt((x2d * x2d + y2d * y2d + z2d * z2d).toDouble()).toFloat() /* normalize */
            x2d /= r2d
            y2d /= r2d
            z2d /= r2d

            /* r2,x2,y2,z2 only, thus subtract out r1,x1,y1,z1 */
            r2n = sqrt(((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) + (z2 - z1) * (z2 - z1)).toDouble()).toFloat()
            rx2 = (x2 - x1) / r2n
            ry2 = (y2 - y1) / r2n
            rz2 = (z2 - z1) / r2n /* now have r2 vector, normal to velocity */

            vx2 = ry2 * z2d - rz2 * y2d /* cross product */
            vy2 = rz2 * x2d - rx2 * z2d
            vz2 = rx2 * y2d - ry2 * x2d /* this and r2 vector define plane of cross section */

            theta1 = 0.0f
            j = 0
            while (j < ny)
            /* walk around cross section generating skin */ {
                theta = theta1
                theta1 += theta_step
                x3 = r3 * (rx2 * cos(theta.toDouble()).toFloat() + vx2 * sin(theta.toDouble()).toFloat())
                y3 = r3 * (ry2 * cos(theta.toDouble()).toFloat() + vy2 * sin(theta.toDouble()).toFloat())
                z3 = r3 * (rz2 * cos(theta.toDouble()).toFloat() + vz2 * sin(theta.toDouble()).toFloat())

                raw_x[i][j] = x2 + x3  /* actually sum of x1+x2+x3, the final point on surface */
                raw_y[i][j] = y2 + y3
                raw_z[i][j] = z2 + z3

                raw_index[i][j] = points
                points++
                j++
            }
            i++
        }
        points--


        /* now build set of points defining surface */
        //        for (i = 0; i < (nx - 1); i++) {
        ///* reuse last point of i and j as first point */
        //            for (j = 0; j < (ny - 1); j++) {
        //                tri_raw_p1[polys] = raw_index[i][j];
        //                tri_raw_p2[polys] = raw_index[i][j + 1];
        //                tri_raw_p3[polys] = raw_index[i + 1][j + 1];
        //                polys++;
        //                tri_raw_p1[polys] = raw_index[i][j];
        //                tri_raw_p2[polys] = raw_index[i + 1][j + 1];
        //                tri_raw_p3[polys] = raw_index[i + 1][j];
        //                polys++;
        //            }
        //        }

        //        for (i = 0; i < nx; i++) {  /* loop around toride at radius r2, around that at r2 */
        //            for (i = 0; i < polys; i++) {  /* now output 3 point polygons */
        ////                Timber.i("indx are "
        ////                        + tri_raw_p1[i] + " "
        ////                        + tri_raw_p2[i] + " "
        ////                        + tri_raw_p3[i]);
        //            }
        //        }

        polys = 0             /* now build set of points defining surface */
        i = 0
        while (i < nx - 1) {
            /* reuse last point of i and j as first point */
            j = 0
            while (j < ny - 1) {
                //                add_to_buffer(raw_index[i][j], ny);
                //                add_to_buffer(raw_index[i][j + 1], ny);
                //                add_to_buffer(raw_index[i + 1][j + 1], ny);
                // reverse 2 and 3 winding
                //                add_to_buffer(raw_index[i][j], ny);
                //                add_to_buffer(raw_index[i + 1][j + 1], ny);
                //                add_to_buffer(raw_index[i][j + 1], ny);

                triangle(
                        raw_index[i][j],
                        raw_index[i + 1][j + 1],
                        raw_index[i][j + 1],
                        ny
                )

                polys++
                //                add_to_buffer(raw_index[i][j], ny);
                //                add_to_buffer(raw_index[i + 1][j + 1], ny);
                //                add_to_buffer(raw_index[i + 1][j], ny);
                // reverse 2 and 3 winding
                //                add_to_buffer(raw_index[i][j], ny);
                //                add_to_buffer(raw_index[i + 1][j], ny);
                //                add_to_buffer(raw_index[i + 1][j + 1], ny);

                triangle(
                        raw_index[i][j],
                        raw_index[i + 1][j],
                        raw_index[i + 1][j + 1],
                        ny
                )

                polys++
                j++
            }
            i++
        }

        val elapsed_time = (SystemClock.uptimeMillis() - start_time) / 1000
        val pretty_print = String.format("%6.2f", elapsed_time)
        Timber.i("end calculating in $pretty_print seconds, count is $sCount")

        mNumIndices = offset
        BufferManager.sFloatArrayIndex = offset
    }

    private fun triangle(t1_indexIn: Int, t2_indexIn: Int, t3_indexIn: Int, blocking: Int) {
        var t1_index = t1_indexIn
        var t2_index = t2_indexIn
        var t3_index = t3_indexIn
        t1_index--
        t2_index--
        t3_index--
        v1[0] = raw_x[t1_index / blocking][t1_index % blocking]
        v1[1] = raw_y[t1_index / blocking][t1_index % blocking]
        v1[2] = raw_z[t1_index / blocking][t1_index % blocking]

        v2[0] = raw_x[t2_index / blocking][t2_index % blocking]
        v2[1] = raw_y[t2_index / blocking][t2_index % blocking]
        v2[2] = raw_z[t2_index / blocking][t2_index % blocking]

        v3[0] = raw_x[t3_index / blocking][t3_index % blocking]
        v3[1] = raw_y[t3_index / blocking][t3_index % blocking]
        v3[2] = raw_z[t3_index / blocking][t3_index % blocking]

        n = XYZ.getNormal(v1, v2, v3)

        add_to_buffer(v1)
        add_to_buffer(v2)
        add_to_buffer(v3)

    }

    private fun add_to_buffer(v: FloatArray) {

        vertexData[offset++] = v[0]
        vertexData[offset++] = v[1]
        vertexData[offset++] = v[2]

        vertexData[offset++] = n[0] * NORMAL_BRIGHTNESS_FACTOR
        vertexData[offset++] = n[1] * NORMAL_BRIGHTNESS_FACTOR
        vertexData[offset++] = n[2] * NORMAL_BRIGHTNESS_FACTOR

        // color value
        vertexData[offset++] = mColor[0]
        vertexData[offset++] = mColor[1]
        vertexData[offset++] = mColor[2]
        vertexData[offset++] = mColor[3]
    }

    companion object {
        private const val POSITION_DATA_SIZE_IN_ELEMENTS = 3
        private const val NORMAL_DATA_SIZE_IN_ELEMENTS = 3
        private const val COLOR_DATA_SIZE_IN_ELEMENTS = 4

        private const val BYTES_PER_FLOAT = 4
        private const val BYTES_PER_SHORT = 2

        private const val STRIDE_IN_FLOATS = POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS + COLOR_DATA_SIZE_IN_ELEMENTS
        private const val STRIDE_IN_BYTES = STRIDE_IN_FLOATS * BYTES_PER_FLOAT

        //    private static final float NORMAL_BRIGHTNESS_FACTOR = 7f;
        private const val NORMAL_BRIGHTNESS_FACTOR = 21f

        private const val LINES = 259     /* storage max for nx  */
        // private static final int POINTS = 259;      /* storage max for ny */
        private const val POINTS = 20      /* storage max for ny */
        private const val TRIS = 68000      /* storage max for points   */

        private var v1 = FloatArray(3)
        private var v2 = FloatArray(3)
        private var v3 = FloatArray(3)
        private var n = FloatArray(3)

        private var sCount: Int = 0
    }
}

// debug to print formatted vertices
//        String svx = String.format("%6.2f", vx);
//        String svy = String.format("%6.2f", vy);
//        String svz = String.format("%6.2f", vz);
//
//        String snvx = String.format("%6.2f", nvx);
//        String snvy = String.format("%6.2f", nvy);
//        String snvz = String.format("%6.2f", nvz);

//        Timber("vert ", index + " x y z nx ny nz "
//                        + svx + " " + svy + " " + svz + " and " + snvx + " " + snvy + " " + snvz );
//                    + " clr "
//                    + vertexData[i + 6] + " " + vertexData[i + 7] + " "
//                    + vertexData[i + 8] + " " + vertexData[i + 8]

