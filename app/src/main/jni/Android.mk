LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include $(CLEAR_VARS)
LOCAL_MODULE:= libtesseract
LOCAL_SRC_FILES:= ../../../../tesseract/lib/$(TARGET_ARCH_ABI)/libtesseract.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= liblept
LOCAL_SRC_FILES:= ../../../../tesseract/lib/$(TARGET_ARCH_ABI)/liblept.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libtiff
LOCAL_SRC_FILES:= ../../../../tesseract/lib/$(TARGET_ARCH_ABI)/libtiff.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libpng
LOCAL_SRC_FILES:= ../../../../tesseract/lib/$(TARGET_ARCH_ABI)/libpng.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libjpeg
LOCAL_SRC_FILES:= ../../../../tesseract/lib/$(TARGET_ARCH_ABI)/libjpeg.so
include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE    := tesseract-test
LOCAL_C_INCLUDES := ../../../../tesseract/include
LOCAL_LDLIBS += -ljnigraphics
LOCAL_SRC_FILES := OcrEngineWrapper.cpp
LOCAL_SHARED_LIBRARIES := libtesseract liblept libtiff libjpeg libpng
include $(BUILD_SHARED_LIBRARY)
