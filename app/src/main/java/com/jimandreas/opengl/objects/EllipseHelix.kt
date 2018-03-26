package com.jimandreas.opengl.objects


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
class EllipseHelix(
        private val mBufMgr: BufferManager,
        numSlices: Int,
        radius: Float,
        height: Float,
        color: FloatArray /*RGBA*/) {

    internal val ibo = IntArray(1)
    private val mEllipseCalculator: EllipseCalculator = EllipseCalculator(
            mBufMgr,
            numSlices,
            radius,
            height,
            color /*RGBA*/
    )

    init {

        mEllipseCalculator.body(
                numSlices,
                radius,
                height,
                color /*RGBA*/)
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
        private const val ELLIPSE_X_FACTOR = 2f / 9f
        private const val ELLIPSE_Z_FACTOR = 1f
    }
}
