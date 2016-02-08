package com.learnopengles.android.objects;


/*
 * http://userpages.umbc.edu/~squire/download/make_helix_635.c
 */
import android.opengl.GLES20;
import android.util.Log;

public class ToroidHelix {
    private static final String LOG_TAG = BufferManager.class.getSimpleName();

    private static final int POSITION_DATA_SIZE_IN_ELEMENTS = 3;
    private static final int NORMAL_DATA_SIZE_IN_ELEMENTS = 3;
    private static final int COLOR_DATA_SIZE_IN_ELEMENTS = 4;

    private static final int BYTES_PER_FLOAT = 4;
    private static final int BYTES_PER_SHORT = 2;

    private static final int STRIDE_IN_FLOATS =
            (POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS + COLOR_DATA_SIZE_IN_ELEMENTS);
    private static final int STRIDE_IN_BYTES = STRIDE_IN_FLOATS * BYTES_PER_FLOAT;

    private static final float NORMAL_BRIGHTNESS_FACTOR = 7f;
    private static final float ELLIPSE_X_FACTOR = 2f / 9f;
    private static final float ELLIPSE_Z_FACTOR = 1f;

    private int mNumIndices = 0;
    private int mCylinderIndexCount;
    final int[] vbo_top_and_bottom = new int[1];
    final int[] vbo_body = new int[1];
    final int[] ibo = new int[1];

    private BufferManager mBufMgr;
    private float[] vertexData;

    private static final int LINES  = 259;     /* storage max for nx  */
    private static final int POINTS = 259;      /* storage max for ny */
    private static final int TRIS   = 68000;      /* storage max for points   */
    
    public ToroidHelix(
            BufferManager mb,
            float[] color /*RGBA*/) {
        

        mBufMgr = mb;
//        float vx, vy, vz;
//        float angleStep = ((2.0ff * (float) Math.PI) / numSlices);
//        // HACK -
//        // TODO: the calculation for how many triangles
//        // TODO: separate out the generation of ends from the body
//
//        float[] vertexData = mBufMgr.getFloatArray(6 * numSlices + 1 * STRIDE_IN_FLOATS);
//        int offset = mBufMgr.getFloatArrayIndex();
        
//            struct point { float x,y,z; int index; };
//            struct point raw[LINES][POINTS];
//            struct tri_poly { int p1, p2, p3; };
//            struct tri_poly tri_raw[TRIS];
        
            float raw_x[][] = new float[LINES][POINTS];
            float raw_y[][] = new float[LINES][POINTS];
            float raw_z[][] = new float[LINES][POINTS];
            int   raw_index[][] = new int[LINES][POINTS];
        
            int[] tri_raw_p1 = new int[TRIS];
            int[] tri_raw_p2 = new int[TRIS];
            int[] tri_raw_p3 = new int[TRIS];
        
            float phi, theta;
            float phi1, theta1;
            int i, j;
            int points, polys;
            int status;
            float x0, y0, z0;
            float x1, y1, z1;
            float x2, y2, z2;
            float x3, y3, z3;
            float x2d, y2d, z2d, r2d, r2n;
            float vx2, vy2, vz2;
            float rx2, ry2, rz2;
            float pi = 3.141592653589793238462643383279502884197f;
            float r1 = 8.0f;  /* major radius of torrus */
            float r2 = 4.0f;  /* minor radius of torrus */
            float r3 = 1.0f;  /* minor radius of helix */
            float F  = 8.0f;  /* wrapping factor of r2 around r1 */

  /*   for big helix  */
            float phi_step = pi/128.0f;
            int nx = 257;

            float theta_step = pi/8.0f;
            int ny = 17;

  /* for smooth shaded helix, smaller steps */
            phi_step = pi/64.0f;
            nx = 129;
            theta_step = pi/8.0f;
            ny = 17;


            // printf("make_helix_635 running \n");

            points = 1; /* thats the standard */

            phi1 = 0.0f;
            for(i=0; i<nx; i++)   /* loop around toride at radius r2, around that at r2 */
            {                     /* this makes x1+x2, the center of the generated figure */
                phi = phi1;
                phi1 = phi1 + phi_step;
                x1 =r1*(float)Math.sin(phi);
                y1 =r1*(float)Math.cos(phi);
                z1 = 0.0f;

                x2 = x1 + r2*(float)Math.sin(phi)*(float)Math.cos(phi*F); /* F is number of helix loops */
                y2 = y1 + r2*(float)Math.cos(phi)*(float)Math.cos(phi*F);
                z2 = z1 + r2*         (float)Math.sin(phi*F);

    /* the derivitive of x1+x2 to get velocity vector direction */
                x2d =  r1*(float)Math.cos(phi) + r2*(float)Math.cos(phi)*(float)Math.cos(phi*F) - F*r2*(float)Math.sin(phi)*(float)Math.sin(phi*F);
                y2d = -r1*(float)Math.sin(phi) - r2*(float)Math.sin(phi)*(float)Math.cos(phi*F) - F*r2*(float)Math.cos(phi)*(float)Math.sin(phi*F);
                z2d = F*r2*(float)Math.cos(phi*F);
                r2d = (float)Math.sqrt(x2d*x2d+y2d*y2d+z2d*z2d); /* normalize */
                x2d = x2d/r2d;
                y2d = y2d/r2d;
                z2d = z2d/r2d;

    /* r2,x2,y2,z2 only, thus subtract out r1,x1,y1,z1 */
                r2n = (float)Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1)+(z2-z1)*(z2-z1));
                rx2 = (x2-x1)/r2n;
                ry2 = (y2-y1)/r2n;
                rz2 = (z2-z1)/r2n; /* now have r2 vector, normal to velocity */

                vx2 = ry2*z2d - rz2*y2d; /* cross product */
                vy2 = rz2*x2d - rx2*z2d;
                vz2 = rx2*y2d - ry2*x2d; /* this and r2 vector define plane of cross section */

                theta1 = 0.0f;
                for(j=0; j<ny; j++)  /* walk around cross section generating skin */
                {
                    theta = theta1;
                    theta1 = theta1 + theta_step;
                    x3 = r3*(rx2*(float)Math.cos(theta) + vx2*(float)Math.sin(theta));
                    y3 = r3*(ry2*(float)Math.cos(theta) + vy2*(float)Math.sin(theta));
                    z3 = r3*(rz2*(float)Math.cos(theta) + vz2*(float)Math.sin(theta));

                    raw_x[i][j] =x2+x3;  /* actually sum of x1+x2+x3, the final point on surface */
                    raw_y[i][j] =y2+y3;
                    raw_z[i][j] =z2+z3;
                    raw_index[i][j] = points;
                    points++;
                }
            }
            points--;
            // printf("raw built \n");


            polys = 0;             /* now build set of points defining surface */
            for(i=0; i<(nx-1); i++)
            {
    /* reuse last point of i and j as first point */
                for(j=0; j<(ny-1); j++)
                {
                    tri_raw_p1[polys] = raw_index[i][j];
                    tri_raw_p2[polys] = raw_index[i][j+1];
                    tri_raw_p3[polys] = raw_index[i+1][j+1];
                    polys++;
                    tri_raw_p1[polys] = raw_index[i][j];
                    tri_raw_p2[polys] = raw_index[i+1][j+1];
                    tri_raw_p3[polys] = raw_index[i+1][j];
                    polys++;
                }
            }
//            printf("tri_raw built \n");
//            printf("Outputing points= %d polys %d \n", points, polys);
//            fp = fopen("helix_635.dat","w");
//            if(fp==NULL)
//            {
//                printf("Can not open %s \n", "helix_635.dat");
//                return 1;
//            }
//            fprintf(fp, "%d %d \n", points, polys);
//            for(i=0; i<nx; i++)   /* loop around toride at radius r2, around that at r2 */
//            {                     /* this makes x1+x2, the center of the generated figure */
//
//                for(j=0; j<ny; j++)  /* walk around cross section generating skin */
//                {
//                    fprintf(fp,"%f %f %f \n", raw[i][j].x, raw[i][j].y, raw[i][j].z);
//                }
//            }
//            for(i=0; i<polys; i++)   /* now output 3 point polygons */
//            {
//                fprintf(fp, "3 %d %d %d \n", tri_raw[i].p1, tri_raw[i].p2, tri_raw[i].p3);
//            }
//            fclose(fp);


        for(i=0; i<polys; i++)   /* now output 3 point polygons */
            {
                Log.w(LOG_TAG, "indx are "
                        + tri_raw_p1[i] + " "
                        + tri_raw_p2[i] + " "
                        + tri_raw_p3[i] );

            }
        


//        /*
//         * copy to array
//         */
//        for (i = 0; i <= numSlices; i++) {
//
//            // TODO: fix number of slices and generate (float)Math.sin/(float)Math.cos lookup table
//
//            float angleInRadians1 =
//                    ((float) i / (float) numSlices)
//                            * ((float) Math.PI * 2f);
//
//            float angleInRadians2 =
//                    ((float) (i+1) / (float) numSlices)
//                            * ((float) Math.PI * 2f);
//            {
//                // first top point
//                vertexData[offset++] = radius * (float) Math.(float)Math.cos(angleInRadians1) * ELLIPSE_X_FACTOR;
//                vertexData[offset++] = height / 2.0ff;
//                vertexData[offset++] = radius * -(float) Math.(float)Math.sin(angleInRadians1) * ELLIPSE_Z_FACTOR;
//
//                // normal vector
//                vertexData[offset++] = radius * (float) Math.(float)Math.cos(angleInRadians1) * ELLIPSE_X_FACTOR;
//                vertexData[offset++] = 0.0ff;
//                vertexData[offset++] = radius * -(float) Math.(float)Math.sin(angleInRadians1) * ELLIPSE_Z_FACTOR;
//                // color value
//                vertexData[offset++] = color[0];
//                vertexData[offset++] = color[1];
//                vertexData[offset++] = color[2];
//                vertexData[offset++] = color[3];
//            }
//
//
//
//
//        }  // end for loop for body
//
//        mNumIndices = offset;
//        mBufMgr.setFloatArrayIndex(offset);


    }


}
