package com.example.tesseracttest;

import android.graphics.Bitmap;

public class OCREngineImp {

    public static String engine(String language, String tessdatPath, Bitmap bitmap) {
        return recognition(language, tessdatPath, bitmap);
    }

    private static native String recognition(String language, String dataPath, Bitmap bmp);

    /*public static String get_recognised_text(Bitmap  bitmap) {
        return nativeReadBitmap(bitmap);
    }
    private static native String nativeReadBitmap(Bitmap bitmap); */
}
