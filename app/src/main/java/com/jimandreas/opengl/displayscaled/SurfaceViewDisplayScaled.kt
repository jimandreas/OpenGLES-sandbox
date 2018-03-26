package com.jimandreas.opengl.displayscaled

import android.content.Context
import android.util.AttributeSet
import com.jimandreas.opengl.common.SurfaceViewCommon

class SurfaceViewDisplayScaled : SurfaceViewCommon {

    constructor(contextIn: Context) : super(contextIn)

    constructor(contextIn: Context, attrs: AttributeSet) : super(contextIn, attrs)

    fun setRenderer(rendererIn: RendererDisplayScaled, densityIn: Float) {
        renderer = rendererIn
        density = densityIn
        super.setRenderer(rendererIn)
    }
}