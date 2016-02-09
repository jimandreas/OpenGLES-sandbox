package com.learnopengles.sandbox.objects;


/*
 * Algorithm credit:
 * http://userpages.umbc.edu/~squire/cs437_lect.html
 * Lecture 14, Curves and Surfaces, targets
 * http://userpages.umbc.edu/~squire/download/make_helix_635.c
 */

import android.os.SystemClock;
import android.util.Log;

public class ToroidHelix {
    private static final String LOG_TAG = ToroidHelix.class.getSimpleName();

    private static final int POSITION_DATA_SIZE_IN_ELEMENTS = 3;
    private static final int NORMAL_DATA_SIZE_IN_ELEMENTS = 3;
    private static final int COLOR_DATA_SIZE_IN_ELEMENTS = 4;

    private static final int BYTES_PER_FLOAT = 4;
    private static final int BYTES_PER_SHORT = 2;

    private static final int STRIDE_IN_FLOATS =
            (POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS + COLOR_DATA_SIZE_IN_ELEMENTS);
    private static final int STRIDE_IN_BYTES = STRIDE_IN_FLOATS * BYTES_PER_FLOAT;

    private static final float NORMAL_BRIGHTNESS_FACTOR = 7f;

    private int mNumIndices = 0;

    private BufferManager mBufMgr;

    private static final int LINES = 259;     /* storage max for nx  */
    // private static final int POINTS = 259;      /* storage max for ny */
    private static final int POINTS = 20;      /* storage max for ny */
    private static final int TRIS = 68000;      /* storage max for points   */

    float raw_x[][] = new float[LINES][POINTS];
    float raw_y[][] = new float[LINES][POINTS];
    float raw_z[][] = new float[LINES][POINTS];
    int raw_index[][] = new int[LINES][POINTS];

    // adding normals
    float normal_x[][] = new float[LINES][POINTS];
    float normal_y[][] = new float[LINES][POINTS];
    float normal_z[][] = new float[LINES][POINTS];

    static int sCount;

    float[] mColor;

    float[] vertexData;
    int offset;

    public ToroidHelix(
            BufferManager mb,
            float[] color /*RGBA*/) {

        mBufMgr = mb;
        mColor = color;
        sCount = 0;   // 12288 is normal count

        vertexData = mBufMgr.getFloatArray(12288 * STRIDE_IN_FLOATS);
        offset = mBufMgr.getFloatArrayIndex();

        float start_time = SystemClock.uptimeMillis();
        Log.w(LOG_TAG, "start calculation");

//        int[] tri_raw_p1 = new int[TRIS];
//        int[] tri_raw_p2 = new int[TRIS];
//        int[] tri_raw_p3 = new int[TRIS];

        float phi, theta;
        float phi1, theta1;
        int i, j;
        int points, polys;
        float x1, y1, z1;
        float x2, y2, z2;
        float x3, y3, z3;
        float x2d, y2d, z2d, r2d, r2n;
        float vx2, vy2, vz2;
        float rx2, ry2, rz2;
        float pi = 3.141592653589793238462643383279502884197f;
        float r1 = 8.0f;  /* major radius of torus */
        float r2 = 4.0f;  /* minor radius of torus */
        float r3 = 1.0f;  /* minor radius of helix */
        float F = 8.0f;  /* wrapping factor of r2 around r1 */

/*   for big helix  */
//        float phi_step = pi / 128.0f;
//        int nx = 257;
//
//        float theta_step = pi / 8.0f;
//        int ny = 17;

/* for smooth shaded helix, smaller steps */
        float phi_step = pi / 64.0f;
        int nx = 129;
        float theta_step = pi / 8.0f;
        int ny = 17;

        points = 1; /* that is the standard */

        /* loop around toride at radius r2, around that at r2 */
        /* this makes x1+x2, the center of the generated figure */

        phi1 = 0.0f;
        for (i = 0; i < nx; i++)   {
            phi = phi1;
            phi1 = phi1 + phi_step;
            x1 = r1 * (float) Math.sin(phi);
            y1 = r1 * (float) Math.cos(phi);
            z1 = 0.0f;

            x2 = x1 + r2 * (float) Math.sin(phi) * (float) Math.cos(phi * F); /* F is number of helix loops */
            y2 = y1 + r2 * (float) Math.cos(phi) * (float) Math.cos(phi * F);
            z2 = z1 + r2 * (float) Math.sin(phi * F);

/* the derivative of x1+x2 to get velocity vector direction */
            x2d = r1 * (float) Math.cos(phi) + r2 * (float) Math.cos(phi) * (float) Math.cos(phi * F) - F * r2 * (float) Math.sin(phi) * (float) Math.sin(phi * F);
            y2d = -r1 * (float) Math.sin(phi) - r2 * (float) Math.sin(phi) * (float) Math.cos(phi * F) - F * r2 * (float) Math.cos(phi) * (float) Math.sin(phi * F);
            z2d = F * r2 * (float) Math.cos(phi * F);
            r2d = (float) Math.sqrt(x2d * x2d + y2d * y2d + z2d * z2d); /* normalize */
            x2d = x2d / r2d;
            y2d = y2d / r2d;
            z2d = z2d / r2d;

/* r2,x2,y2,z2 only, thus subtract out r1,x1,y1,z1 */
            r2n = (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) + (z2 - z1) * (z2 - z1));
            rx2 = (x2 - x1) / r2n;
            ry2 = (y2 - y1) / r2n;
            rz2 = (z2 - z1) / r2n; /* now have r2 vector, normal to velocity */

            vx2 = ry2 * z2d - rz2 * y2d; /* cross product */
            vy2 = rz2 * x2d - rx2 * z2d;
            vz2 = rx2 * y2d - ry2 * x2d; /* this and r2 vector define plane of cross section */

            theta1 = 0.0f;
            for (j = 0; j < ny; j++)  /* walk around cross section generating skin */ {
                theta = theta1;
                theta1 = theta1 + theta_step;
                x3 = r3 * (rx2 * (float) Math.cos(theta) + vx2 * (float) Math.sin(theta));
                y3 = r3 * (ry2 * (float) Math.cos(theta) + vy2 * (float) Math.sin(theta));
                z3 = r3 * (rz2 * (float) Math.cos(theta) + vz2 * (float) Math.sin(theta));

                // record normal
                normal_x[i][j] = rx2 * (float) Math.cos(theta) + vx2 * (float) Math.sin(theta);
                normal_y[i][j] = ry2 * (float) Math.cos(theta) + vy2 * (float) Math.sin(theta);
                normal_z[i][j] = rz2 * (float) Math.cos(theta) + vz2 * (float) Math.sin(theta);

                raw_x[i][j] = x2 + x3;  /* actually sum of x1+x2+x3, the final point on surface */
                raw_y[i][j] = y2 + y3;
                raw_z[i][j] = z2 + z3;

                raw_index[i][j] = points;
                points++;
            }
        }
        points--;


        polys = 0;             /* now build set of points defining surface */
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
////                Log.w(LOG_TAG, "indx are "
////                        + tri_raw_p1[i] + " "
////                        + tri_raw_p2[i] + " "
////                        + tri_raw_p3[i]);
//            }
//        }

        polys = 0;             /* now build set of points defining surface */
        for (i = 0; i < (nx - 1); i++) {
/* reuse last point of i and j as first point */
            for (j = 0; j < (ny - 1); j++) {
//                add_to_buffer(raw_index[i][j], ny);
//                add_to_buffer(raw_index[i][j + 1], ny);
//                add_to_buffer(raw_index[i + 1][j + 1], ny);
                // reverse 2 and 3 winding
                add_to_buffer(raw_index[i][j], ny);
                add_to_buffer(raw_index[i + 1][j + 1], ny);
                add_to_buffer(raw_index[i][j + 1], ny);

                polys++;
//                add_to_buffer(raw_index[i][j], ny);
//                add_to_buffer(raw_index[i + 1][j + 1], ny);
//                add_to_buffer(raw_index[i + 1][j], ny);
                // reverse 2 and 3 winding
                add_to_buffer(raw_index[i][j], ny);
                add_to_buffer(raw_index[i + 1][j], ny);
                add_to_buffer(raw_index[i + 1][j + 1], ny);
                polys++;
            }
        }

        float elapsed_time = (SystemClock.uptimeMillis() - start_time) / 1000;
        String pretty_print = String.format("%6.2f", elapsed_time);
        Log.w(LOG_TAG, "end calculating in " + elapsed_time + " seconds, count is " + sCount);

        mNumIndices = offset;
        mBufMgr.setFloatArrayIndex(offset);
    }

    private void add_to_buffer(int index, int blocking) {

        sCount++;
        --index; // zero based index now.
        float vx = raw_x[index / blocking][index % blocking];
        float vy = raw_y[index / blocking][index % blocking];
        float vz = raw_z[index / blocking][index % blocking];

        if ((vx == 0f) && (vy == 0f) && (vz == 0f)) {
            Log.e(LOG_TAG, "oops all vertices are zero at index " + index);
        }

        float nvx = normal_x[index / blocking][index % blocking];
        float nvy = normal_y[index / blocking][index % blocking];
        float nvz = normal_z[index / blocking][index % blocking];

        vertexData[offset++] = vx;
        vertexData[offset++] = vy;
        vertexData[offset++] = vz;

        vertexData[offset++] = -9.1f * nvx;
        vertexData[offset++] = -9.1f * nvy;
        vertexData[offset++] = -9.1f * nvz;

//        vertexData[offset++] = 0.0f;
//        vertexData[offset++] = 0.0f;
//        vertexData[offset++] = 7.0f;

//        vertexData[offset++] = -1f * vx;
//        vertexData[offset++] = -1f * vy;
//        vertexData[offset++] = 7f * vz;

        // color value
        vertexData[offset++] = mColor[0];
        vertexData[offset++] = mColor[1];
        vertexData[offset++] = mColor[2];
        vertexData[offset++] = mColor[3];
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

//        Log.w("vert ", index + " x y z nx ny nz "
//                        + svx + " " + svy + " " + svz + " and " + snvx + " " + snvy + " " + snvz );
//                    + " clr "
//                    + vertexData[i + 6] + " " + vertexData[i + 7] + " "
//                    + vertexData[i + 8] + " " + vertexData[i + 8]

