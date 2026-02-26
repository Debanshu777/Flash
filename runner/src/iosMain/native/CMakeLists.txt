cmake_minimum_required(VERSION 3.22)
project(llama_runner_ios LANGUAGES CXX OBJCXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

if(DEFINED LLAMA_CPP_PATH)
    set(LLAMA_SRC "${LLAMA_CPP_PATH}")
else()
    get_filename_component(LLAMA_SRC "${CMAKE_CURRENT_LIST_DIR}/../../../../llama.cpp" ABSOLUTE)
endif()

# Check if llama.cpp exists
if(NOT EXISTS "${LLAMA_SRC}/CMakeLists.txt")
    message(FATAL_ERROR "llama.cpp not found at ${LLAMA_SRC}. Please run 'git submodule update --init --recursive'")
endif()

# Ensure common library is built when llama.cpp is used as subdirectory
set(LLAMA_BUILD_COMMON ON CACHE BOOL "Build common utils" FORCE)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "Build static libraries for iOS" FORCE)
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)

add_subdirectory(${LLAMA_SRC} ${CMAKE_BINARY_DIR}/llama-build)

add_library(llama_runner_ios STATIC llama_runner.mm)

target_include_directories(llama_runner_ios PRIVATE
    ${CMAKE_CURRENT_LIST_DIR}
    ${LLAMA_SRC}/include
    ${LLAMA_SRC}/common
    ${LLAMA_SRC}/ggml/include
    ${LLAMA_SRC}/ggml/src
)

target_link_libraries(llama_runner_ios
    llama
    common
)

set_target_properties(llama_runner_ios PROPERTIES
    ARCHIVE_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}"
)

# Enable optimizations for release builds
if(CMAKE_BUILD_TYPE MATCHES Release)
    target_compile_options(llama_runner_ios PRIVATE -O3)
endif()
