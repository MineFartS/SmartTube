

WORKING_DIR := $(call my-dir)
include $(CLEAR_VARS)
LIBVPX_ROOT := $(WORKING_DIR)/libvpx

# build libvpx.so
LOCAL_PATH := $(WORKING_DIR)
include libvpx.mk

# build libvpxV2JNI.so
include $(CLEAR_VARS)
LOCAL_PATH := $(WORKING_DIR)
LOCAL_MODULE := libvpxV2JNI
LOCAL_ARM_MODE := arm
LOCAL_CPP_EXTENSION := .cc
LOCAL_SRC_FILES := vpx_jni.cc
LOCAL_LDLIBS := -llog -lz -lm -landroid
LOCAL_SHARED_LIBRARIES := libvpx
LOCAL_STATIC_LIBRARIES := cpufeatures
include $(BUILD_SHARED_LIBRARY)

$(call import-module,android/cpufeatures)
