package com.example.tesseracttest;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private Context myContext;
    private LinearLayout cameraPreview;
    private boolean cameraFront = true;
    public static Bitmap bitmap;

    static {
        System.loadLibrary("tesseract-test");
    }

    private static final String TESSDATA = "tessdata";
    private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/tesseracttest/tessdata/";
    private static final String TAG = "java-wrapper";
    private static final String lang = "eng";
    private  static  final boolean singleImage = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myContext = this;
        startTesseract();
        mCamera = Camera.open();
        mCamera.setDisplayOrientation(90);
        cameraPreview = (LinearLayout) findViewById(R.id.cPreview);
        mPreview = new CameraPreview(myContext, mCamera);
        cameraPreview.addView(mPreview);

        int camerasNumber = Camera.getNumberOfCameras();
        if (camerasNumber > 1) {
            releaseCamera();
            chooseCamera();
        } else {

        }
        mCamera.setPreviewCallback(previewCallback);
        mCamera.startPreview();

    }

    private int findFrontFacingCamera() {

        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;

    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        //Search for the back facing camera
        //get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;

            }

        }
        return cameraId;
    }

    public void onResume() {

        super.onResume();
        if (mCamera == null) {
            mCamera = Camera.open();
            mCamera.setDisplayOrientation(90);
            mPreview.refreshCamera(mCamera);
            Log.d("nu", "null");
        } else {
            Log.d("nu", "no null");
        }

    }

    public void chooseCamera() {
        //if the camera preview is the front
        if (cameraFront) {
            int cameraId = findBackFacingCamera();
            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview

                mCamera = Camera.open(cameraId);
                mCamera.setDisplayOrientation(90);
                mPreview.refreshCamera(mCamera);
            }
        } else {
            int cameraId = findFrontFacingCamera();
            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview
                mCamera = Camera.open(cameraId);
                mCamera.setDisplayOrientation(90);
                mPreview.refreshCamera(mCamera);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //when on Pause, release camera in order to be used from other applications
        releaseCamera();
    }

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Size previewSize = camera.getParameters().getPreviewSize();
            YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 80, baos);
            byte[] jdata = baos.toByteArray();
            Bitmap bitmap = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);
            Bitmap rotateBitmap = RotateBitmap(bitmap, 90);
            if (!singleImage) {
                String recognizedText = OCREngineImp.get_recognised_text(rotateBitmap);
                Log.d("RecognisedText", recognizedText);
            }
            bitmap.recycle();
            baos.reset();

        }
    };

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }


    public void startTesseract() {
        prepareTessData();
    }

    private void prepareTessData() {
        try {
            prepareDirectory(DATA_PATH + TESSDATA);
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
        copyTessDataFiles(TESSDATA);
    }

    private void copyTessDataFiles(String path) {

        String fullpath = DATA_PATH + lang + ".traineddata";
        Log.d("Fullpath", fullpath);
        File file = new File(fullpath);

        if (!(file.exists())) {
            try {
                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
                OutputStream out = new FileOutputStream(DATA_PATH + "tessdata/" + lang + ".traineddata");
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
        } else {
            Log.e(TAG,"already exists" + fullpath);
            OCREngineImp.ocrEngine(lang, DATA_PATH);

            if (singleImage) {
                try {
                    InputStream bitmap = getAssets().open("testImage_1_1.png");
                    Bitmap bit = BitmapFactory.decodeStream(bitmap);
                    String recognizedText = OCREngineImp.get_recognised_text(bit);
                    Log.d("RecognisedText", recognizedText);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }
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
}