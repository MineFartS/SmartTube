

WORKING_DIR := $(call my-dir)
include $(CLEAR_VARS)

# build libopus.a
LOCAL_PATH := $(WORKING_DIR)
include libopus.mk

# build libopusV2JNI.so
include $(CLEAR_VARS)
LOCAL_PATH := $(WORKING_DIR)
LOCAL_MODULE := libopusV2JNI
LOCAL_ARM_MODE := arm
LOCAL_CPP_EXTENSION := .cc
LOCAL_SRC_FILES := opus_jni.cc
LOCAL_LDLIBS := -llog -lz -lm
LOCAL_STATIC_LIBRARIES := libopus
include $(BUILD_SHARED_LIBRARY)
