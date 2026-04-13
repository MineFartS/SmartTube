

LOCAL_PATH := $(call my-dir)/libopus

include $(CLEAR_VARS)

include $(LOCAL_PATH)/celt_headers.mk
include $(LOCAL_PATH)/celt_sources.mk
include $(LOCAL_PATH)/opus_headers.mk
include $(LOCAL_PATH)/opus_sources.mk
include $(LOCAL_PATH)/silk_headers.mk
include $(LOCAL_PATH)/silk_sources.mk

LOCAL_MODULE := libopus
LOCAL_ARM_MODE := arm
LOCAL_CFLAGS := -DOPUS_BUILD -DFIXED_POINT -DUSE_ALLOCA -DHAVE_LRINT \
                -DHAVE_LRINTF
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include $(LOCAL_PATH)/src \
                    $(LOCAL_PATH)/silk $(LOCAL_PATH)/celt \
                    $(LOCAL_PATH)/silk/fixed
LOCAL_SRC_FILES := $(CELT_SOURCES) $(OPUS_SOURCES) $(OPUS_SOURCES_FLOAT) \
                   $(SILK_SOURCES) $(SILK_SOURCES_FIXED)

ifneq ($(findstring armeabi-v7a, $(TARGET_ARCH_ABI)),)
LOCAL_SRC_FILES += $(CELT_SOURCES_ARM)
LOCAL_SRC_FILES += celt/arm/armopts_gnu.s.neon
LOCAL_SRC_FILES += $(subst .s,_gnu.s.neon,$(CELT_SOURCES_ARM_ASM))
LOCAL_CFLAGS += -DOPUS_ARM_ASM -DOPUS_ARM_INLINE_ASM -DOPUS_ARM_INLINE_EDSP \
                -DOPUS_ARM_INLINE_MEDIA -DOPUS_ARM_INLINE_NEON \
                -DOPUS_ARM_MAY_HAVE_NEON -DOPUS_ARM_MAY_HAVE_MEDIA \
                -DOPUS_ARM_MAY_HAVE_EDSP
endif

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include

include $(BUILD_STATIC_LIBRARY)
