cmake_minimum_required(VERSION 3.22.1)
project(llama_runner VERSION 1.0.0 LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(LLAMA_SRC ${CMAKE_CURRENT_LIST_DIR}/../../../../llama.cpp)

if(NOT EXISTS "${LLAMA_SRC}/CMakeLists.txt")
    message(FATAL_ERROR "llama.cpp not found at ${LLAMA_SRC}. Please run 'git submodule update --init --recursive'")
endif()

# llama.cpp build options for Android
if(DEFINED ANDROID_ABI)
    message(STATUS "Configuring llama.cpp for Android ABI: ${ANDROID_ABI}")
    if(ANDROID_ABI STREQUAL "arm64-v8a")
        set(GGML_SYSTEM_ARCH "ARM" CACHE STRING "")
        set(GGML_CPU_KLEIDIAI ON CACHE BOOL "")
        set(GGML_OPENMP ON CACHE BOOL "")
    elseif(ANDROID_ABI STREQUAL "x86_64")
        set(GGML_SYSTEM_ARCH "x86" CACHE STRING "")
        set(GGML_CPU_KLEIDIAI OFF CACHE BOOL "")
        set(GGML_OPENMP OFF CACHE BOOL "")
    endif()
endif()

add_subdirectory(${LLAMA_SRC} ${CMAKE_BINARY_DIR}/llama-build)

find_library(log-lib log)

# Our JNI library
add_library(llama_runner SHARED llama_runner_jni.cpp)

target_include_directories(llama_runner PRIVATE
    ${LLAMA_SRC}/include
    ${LLAMA_SRC}/common
    ${LLAMA_SRC}/ggml/include
    ${LLAMA_SRC}/ggml/src
)

target_link_libraries(llama_runner
    llama
    common
    ${log-lib}
)

# Enable optimizations for release builds
if(CMAKE_BUILD_TYPE MATCHES Release)
    target_compile_options(llama_runner PRIVATE -O3)
endif()
