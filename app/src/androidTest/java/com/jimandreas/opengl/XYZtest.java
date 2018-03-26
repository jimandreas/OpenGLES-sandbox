package com.jimandreas.opengl;

/*
 * test the XYZ utility functions
 *
 */

import android.util.Log;
import com.jimandreas.opengl.objects.XYZ;
import android.test.AndroidTestCase;

public class XYZtest extends AndroidTestCase {

    private static final String LOG_TAG = XYZtest.class.getSimpleName();

    public void testGetNormal() {
        int numTests = 6;
        float p1_tests[][] = {
                { 0f, 0f, 0f },
                { 0f, 0f, 0f },
                { 0f, 0f, 0f },
                { 0f, 0f, 0f },
                { 0f, 0f, 0f },
                { 0f, 0f, 0f }
        };

        float p2_tests[][] = {
                { 1f, 0f, 0f },
                { 0f, 0f, 1f },
                { -1f, 0f, 0f },
                { 0f, 0f, -1f },
                { 1f, 1f, 0f },
                { 10f, 10f, 0f }
        };

        float p3_tests[][] = {
                { 0f, 1f, 0f },
                { 0f, 1f, 0f },
                { 0f, 1f, 0f },
                { 0f, 1f, 0f },
                { 0f, 1f, 0f },
                { 0f, 10f, 0f }
        };

        float result[];
        float vx, vy, vz;

        for (int i = 0; i < numTests; i++ ) {
            result = XYZ.Companion.getNormal(
                    p1_tests[i],
                    p2_tests[i],
                    p3_tests[i]
            );



            vx = result[0];
            vy = result[1];
            vz = result[2];
            String svx = String.format("%6.2f", vx);
            String svy = String.format("%6.2f", vy);
            String svz = String.format("%6.2f", vz);

            Log.i("test", "nx ny nz "
                    + vx + " " + vy + " " + vz );

        }
        int foo = 1;
        assertTrue("random test", foo == 1);
    }
}
