@file:Suppress( "UNUSED_PARAMETER")
package com.jimandreas.opengl.common

abstract class RendererCommon(surfaceViewIn: SurfaceViewCommon) {
    var scaleCurrent = 0.5f
    var scalePrevious = 0f
    var deltaX = 0f
    var deltaY = 0f
    var deltaTranslateX = 0f
    var deltaTranslateY = 0f
    var touchX = 300f
    var touchY = 300f
}
