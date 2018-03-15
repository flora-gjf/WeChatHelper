package com.gjf.wc.utils;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

/**
 * Created by guojunfu on 18/3/15.
 */

public class ImageUtil {

    public static byte[] parseBitmapToBytes(Bitmap bitmap) {
        byte[] bytes = null;

        if (bitmap != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            bytes = bos.toByteArray();
        }

        return bytes;
    }
}
