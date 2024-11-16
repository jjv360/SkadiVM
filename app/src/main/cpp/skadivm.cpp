#include <jni.h>
#include <unistd.h>
#include <cstdio>
#include <dlfcn.h>

/**
 * Load Qemu into our own process for safety, and run it as if it was run as a program.
 */
extern "C"
JNIEXPORT jint JNICALL
Java_com_jjv360_skadivm_logic_VMRunner_runQemu(JNIEnv *env, jobject thiz, jstring working_dir, jstring qemu_binary, jstring cmdline, jobject line_in) {

    // Change working directory
    auto workingDir = env->GetStringUTFChars(working_dir, nullptr);
    chdir(workingDir);
    env->ReleaseStringUTFChars(working_dir, workingDir);

    // Open process
    auto qemuBinary = env->GetStringUTFChars(qemu_binary, nullptr);
    auto fd = dlopen(qemuBinary, RTLD_NOW);
    env->ReleaseStringUTFChars(qemu_binary, qemuBinary);



    // Done, close and get the exit code
    dlclose(fd);
    return 1;

}