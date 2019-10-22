package com.example.tesseracttest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class MainActivity extends AppCompatActivity {

    private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/tesseracttest/tessdata/";
    private static final String lang = "eng";
    private static final String TESSDATA = "tessdata";
    private static final String TAG = "java-wrapper";

    static {
        System.loadLibrary("tesseract-test");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startEngines();


    }
    public void startEngines() {
        prepareTessData();
    }
    private static Bitmap getTextImage(String text, int width, int height) {
        final Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        final Canvas canvas = new Canvas(bmp);
        canvas.drawColor(Color.WHITE);
        drawTextNewLines(text, canvas);

        return bmp;
    }
    /**
     * Draws text (with newlines) centered onto the canvas. If the text does not fit horizontally,
     * it will be cut off. If the text does not fit vertically, the start of the text will be at
     * the top of the image and whatever not fitting onto the image being cut off. If the text
     * fits vertically it will be centered vertically.
     *
     * @param text String to draw onto the canvas
     * @param canvas Canvas to draw text onto
     */
    private static void drawTextNewLines(String text,  Canvas canvas){
        final Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(24.0f);

        String[] textArray = text.split("\n");
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        int count = textArray.length;
        int lineSize = (int) (paint.descent() - paint.ascent());
        int maxLinesToPushUp = height / lineSize;
        maxLinesToPushUp = count < maxLinesToPushUp ? count : maxLinesToPushUp;
        int pixelsToPushUp = (maxLinesToPushUp - 1) / 2 * lineSize;

        int x = width / 2;
        int y = (height / 2) - pixelsToPushUp;

        for (String line : textArray){
            canvas.drawText(line, x, y, paint);
            y += lineSize;
        }
    }
    private void prepareTessData() {
        try {
            prepareDirectory(Environment.getExternalStorageDirectory() + "/tesseracttest/" + TESSDATA);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void prepareDirectory(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "Create of directory" + path + " Failed, check the android manifest has permission to write to external storage");
            }
        } else {
            Log.i(TAG, "Created Directory " + path);
        }

        try {
            Log.d(TAG, "copyTessDataFiles: " + getAssets().list(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
        copyTessDataFiles();
    }

    private void copyTessDataFiles() {

        String fullpath = DATA_PATH + lang + ".traineddata";
        Log.d("Fullpath", fullpath);

        if (!(new File(DATA_PATH + lang + ".traineddata")).exists()) {
            try {
                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
                OutputStream out = new FileOutputStream(DATA_PATH  + "/" + lang + ".traineddata");
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();

                out.close();

                Log.v(TAG, "Copied " + lang + " traineddata");

            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
            }
        }
        //send bitmap
        final String inputText = "test recognition";
        final Bitmap bmp = getTextImage(inputText, 640, 480);
        Log.d("TesseractTest", "bitmap" + bmp);

        try {
            InputStream bitmap=getAssets().open("testImage_1_1.png");
            Bitmap bit = BitmapFactory.decodeStream(bitmap);
            // get recognised text
            String recognizedText = OCREngineImp.engine(lang, DATA_PATH, bit);
            Log.d("RecognisedText", recognizedText);

            TextView recognisedText = (TextView) findViewById(R.id.recognisedText);
            recognisedText.setText(recognizedText);

        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }


    }
}
