package com.jimandreas.opengl.displayobjects

import android.content.Context
import android.util.AttributeSet
import com.jimandreas.opengl.common.SurfaceViewCommon


class SurfaceViewDisplayObjects : SurfaceViewCommon {

    constructor(contextIn: Context) : super(contextIn)

    constructor(contextIn: Context, attrs: AttributeSet) : super(contextIn, attrs)

    fun setRenderer(rendererIn: RendererDisplayObjects, densityIn: Float) {
        renderer = rendererIn
        density = densityIn
        super.setRenderer(rendererIn)
    }
}
