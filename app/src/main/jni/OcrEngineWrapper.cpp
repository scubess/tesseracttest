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

            syslog(LOG_CRIT, "received bitmap");
            regText = (*env).NewStringUTF("this is not actual test recognised");

                PIX *pixd = pixCreate(info.width, info.height, 32);
                l_uint32 width = pixGetWidth(pixd);
                l_uint32 height = pixGetHeight(pixd);
                l_uint32 depth = pixGetDepth(pixd);
                l_uint32 *data = pixGetData(pixd);
                size_t size = 4 * pixGetWpl(pixd) * pixGetHeight(pixd);

                if (width <= 0 || height <= 0) {
                    //syslog(LOG_CRIT, "Pix width and height must be > 0");
                    (*env).ThrowNew((*env).FindClass("java/lang/Exception"), "Pix width and height must be > 0");
                    return NULL;
                } else if(depth != 1 && depth != 2 && depth != 4 && depth != 8 && depth != 16 && depth != 24 && depth != 32) {
                    //syslog(LOG_CRIT, "Depth must be one of 1, 2, 4, 8, 16, or 32");
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
                    char *outText;
                    //syslog(LOG_CRIT, "TesseractVersion count 100: %s", apiTest->Version());
                    apiTest->SetImage(pixds);
                    apiTest->Recognize(nullptr);
                    outText = apiTest->GetUTF8Text();
                    syslog(LOG_CRIT, "outtext %s", outText);
                    if ((*env).NewStringUTF(outText)) {
                        regText =  (*env).NewStringUTF(outText);
                         syslog(LOG_CRIT, "UTF reconised text : %s", outText);
                    }

                    delete [] outText;
                    //apiTest->End();
                    apiTest->Clear();
                    pixDestroy(&pixd);
                    pixDestroy(&pixds);
                } catch (exception &ecx) {
                    syslog(LOG_CRIT, "Engine exception %s", ecx.what());
                }
            return regText;
}
#ifdef __cplusplus
}
#endif
