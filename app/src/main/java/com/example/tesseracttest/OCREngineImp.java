package com.example.tesseracttest;

import android.graphics.Bitmap;
import android.util.Log;

public class OCREngineImp {

    public static void ocrEngine(String language, String tessdatPath) {
        engine(language, tessdatPath);
    }

    public static String get_recognised_text(Bitmap  bitmap) {
        Log.d("Textout: ", nativeReadBitmap(bitmap));
        return nativeReadBitmap(bitmap);
    }

    private static native void engine(String language, String dataPath);

    private static native String nativeReadBitmap(Bitmap bitmap);
}
