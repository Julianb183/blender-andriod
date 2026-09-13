cmake_minimum_required(VERSION 3.22.1)
project(pcvr_qemu_runner)

add_library(pcvr_qemu_runner SHARED qemu_runner.cpp)

target_compile_features(pcvr_qemu_runner PRIVATE cxx_std_17)
target_link_libraries(pcvr_qemu_runner log android)
