package com.jimandreas.opengl.displayobjfile

import android.content.Context
import android.util.AttributeSet
import com.jimandreas.opengl.common.SurfaceViewCommon

class SurfaceViewObjFile : SurfaceViewCommon {

    constructor(contextIn: Context) : super(contextIn)

    constructor(contextIn: Context, attrs: AttributeSet) : super(contextIn, attrs)

    fun setRenderer(rendererIn: RendererDisplayObjFile, densityIn: Float) {
        renderer = rendererIn
        density = densityIn
        super.setRenderer(rendererIn)
    }
}