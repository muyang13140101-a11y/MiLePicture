cmake_minimum_required(VERSION 3.22.1)

project("mile_native")

# 强制开启 16 KB 内存分页对齐 (Android 15+ 16 KB Page Alignment 官方规范)
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,max-page-size=16384")

add_library(
        mile_native
        SHARED
        mile_native.cpp)

find_library(
        log-lib
        log)

target_link_libraries(
        mile_native
        ${log-lib})
