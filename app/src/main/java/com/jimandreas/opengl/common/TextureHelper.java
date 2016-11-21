package com.jimandreas.opengl.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Build;

public class TextureHelper {
    public static int loadTexture(final Context context, final int resourceId) {
        final int[] textureHandle = new int[1];

        // debug
        boolean isPremultiplied;
        Bitmap.Config config;
        int width;
        int height;
        boolean hasAlpha;
        // end debug

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;    // No pre-scaling
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                options.inPremultiplied = false;
//            }

            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                isPremultiplied = bitmap.isPremultiplied();
                config = bitmap.getConfig();
                width = bitmap.getWidth();
                height = bitmap.getHeight();
                hasAlpha = bitmap.hasAlpha();

                int foo = width * height;
            }

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            int error;
            error = GLES20.glGetError();

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0) {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }
}
