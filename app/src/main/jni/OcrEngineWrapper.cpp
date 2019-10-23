//
// Created by Lshiva on 2019-10-17.
//

#import "jni.h"
#import "jni.h"
#include <iostream>
#include <string>
#include "com_example_tesseracttest_OCREngineImp.h"
#include "syslog.h"
#include "assert.h"
#include <android/bitmap.h>
#include <../../../../tesseract/include/tesseract/baseapi.h>
#include <../../../../tesseract/include/leptonica/allheaders.h>

using namespace std;
using namespace tesseract;
using std::string;

#ifdef __cplusplus
extern "C" {
#endif
         jstring regText;
         tesseract::TessBaseAPI *apiTest = new tesseract::TessBaseAPI();
JNIEXPORT

void JNICALL Java_com_example_tesseracttest_OCREngineImp_engine (JNIEnv *env, jclass clazz, jstring language, jstring dataPath) {
    jboolean isCopy;
        string tessPath = (env)->GetStringUTFChars(dataPath, &isCopy);
        string lang = (env)->GetStringUTFChars(language, &isCopy);
        const char *c_dir = env->GetStringUTFChars(dataPath, NULL);
        const char *c_lang = env->GetStringUTFChars(language, NULL);

        int returnValue =  apiTest->Init(c_dir, c_lang);

        if (returnValue) {
            syslog(LOG_CRIT, "could not initialise tesseract return value %d: datapath: %s, language: %s", returnValue, c_dir, c_lang);
        } else {
             syslog(LOG_CRIT,"Initialized Tesseract API with language=%s, TesseractVersion=%s",lang.c_str(), apiTest->Version());
        }

}

jstring JNICALL Java_com_example_tesseracttest_OCREngineImp_nativeReadBitmap (JNIEnv *env, jobject engineObj, jobject bitmap) {
 l_int32 w, h, d;
    AndroidBitmapInfo info;
    void* pixels;
    int ret;

    jclass clazz = (*env).FindClass("java/util/ArrayList");
    jobject newSuggestionObj = (*env).NewObject(clazz, (*env).GetMethodID(clazz, "<init>", "()V"));
    jmethodID addSuggestionObj = env->GetMethodID(clazz, "add", "(Ljava/lang/Object;)Z");
    regText = (*env).NewStringUTF("");

    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
         syslog(LOG_CRIT, "AndroidBitmap: AndroidBitmap_getInfo() failed! error=%d", ret);
         (*env).ThrowNew((*env).FindClass("java/lang/Exception"), "AndroidBitmap_getInfo() failed.");
        return NULL;
    }

    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        syslog(LOG_CRIT, "AndroidBitmap: Bitmap format is not RGBA_8888=%d", ret);
        (*env).ThrowNew((*env).FindClass("java/lang/Exception"), "Bitmap format is not RGBA_8888.");
        return NULL;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
          syslog(LOG_CRIT, "AndroidBitmap: AndroidBitmap_lockPixels() failed! error=%d", ret);
         (*env).ThrowNew((*env).FindClass("java/lang/Exception"), "AndroidBitmap_lockPixels() failed!.");
        return NULL;
    }

    PIX *pixd = pixCreate(info.width, info.height, 32);
    l_uint32 width = pixGetWidth(pixd);
    l_uint32 height = pixGetHeight(pixd);
    l_uint32 depth = pixGetDepth(pixd);
    l_uint32 *data = pixGetData(pixd);
    size_t size = 4 * pixGetWpl(pixd) * pixGetHeight(pixd);

    if (width <= 0 || height <= 0) {
        syslog(LOG_CRIT, "Pix: width and height must be > 0");
        (*env).ThrowNew((*env).FindClass("java/lang/Exception"), "Pix width and height must be > 0");
        return NULL;
    } else if(depth != 1 && depth != 2 && depth != 4 && depth != 8 && depth != 16 && depth != 24 && depth != 32) {
        syslog(LOG_CRIT, "Pix: Depth must be one of 1, 2, 4, 8, 16, or 32");
        (*env).ThrowNew((*env).FindClass("java/lang/Exception"), "Depth must be one of 1, 2, 4, 8, 16, or 32");
        return NULL;
    }

    l_uint32 *src = (l_uint32* ) pixels;
    l_int32 srcWpl = (info.stride / 4);
    l_uint32 *dst = pixGetData(pixd);
    l_int32 dstWpl = pixGetWpl(pixd);
    memcpy(dst, src, 4 * (info.width * info.height));
    AndroidBitmap_unlockPixels(env, bitmap);

    PIX *pixs = pixd;

    PIX *pixds = pixClone(pixs);

    if (pixds) {
         l_int32 width = pixGetWidth(pixds);
         l_int32 height = pixGetHeight(pixds);
    }
        try {
            syslog(LOG_CRIT, "Pix: Width=%d & Height:%d", width, height);
            char *outText;
            regText = (*env).NewStringUTF("");
            apiTest->SetImage(pixds);
            syslog(LOG_CRIT, "Tesseract: Set Image for recognition");

            try {
                syslog(LOG_CRIT, "Tesseract: Get UTF Recognition");
                outText = apiTest->GetUTF8Text();
            }  catch (exception &ecx) {
                syslog(LOG_CRIT, "GetUTF8Text crash %s", ecx.what());
                (*env).ThrowNew((*env).FindClass("java/lang/Exception"), "GetUTF8Text crash");
            }

            syslog(LOG_CRIT, "regText: %s", outText);
            regText = (*env).NewStringUTF(outText);
            pixDestroy(&pixd);
            pixDestroy(&pixds);
            return regText;
            } catch (exception &ecx) {
                syslog(LOG_CRIT, "Engine exception %s", ecx.what());
                (*env).ThrowNew((*env).FindClass("java/lang/Exception"), "Engine exception");
            }

    return regText;
}
#ifdef __cplusplus
}
#endif
