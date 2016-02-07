package com.learnopengles.android.objects;


/*

Memory manager:
   "Reserve" from current float array
      Underlying manager builds float arrays in smaller blocks
      to avoid one large allocation and copying.   Blocks
      are retained.   All rendering is triangles with Stride.

      One float[] array is used for accumulation.  When full
      a new (or recycled) FloatBuffer is allocated and
      the float[] array is copied into it.

 */
public class EllipseHelix {
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
    private EllipseCalculator mEllipseCalculator;

    public EllipseHelix(
            BufferManager mb,
            int numSlices,
            float radius,
            float height,
            float[] color /*RGBA*/) {


        int i, j;

        mBufMgr = mb;

        mEllipseCalculator = new EllipseCalculator(
                mb,
                numSlices,
                radius,
                height,
                color /*RGBA*/
        );

        mEllipseCalculator.body(
                numSlices,
                radius,
                height,
                color /*RGBA*/ );
    }
}
