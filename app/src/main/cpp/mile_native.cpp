#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_milepicture_app_MiLeApplication_getNativeEngineVersion(
        JNIEnv* env,
        jobject /* this */) {
    std::string version = "MiLePicture Native Engine v2.0 (ARM64-v8a 16KB Aligned)";
    return env->NewStringUTF(version.c_str());
}
