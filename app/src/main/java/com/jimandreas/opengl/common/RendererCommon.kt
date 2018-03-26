package com.jimandreas.opengl.common

import android.app.Activity
import android.opengl.GLSurfaceView

abstract class RendererCommon(activityIn: Activity, surfaceViewIn: SurfaceViewCommon) {
    var scaleCurrent = 0.5f
    var scalePrevious = 0f
    var deltaX = 0f
    var deltaY = 0f
    var deltaTranslateX = 0f
    var deltaTranslateY = 0f
    var touchX = 300f
    var touchY = 300f
}
