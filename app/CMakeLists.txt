cmake_minimum_required(VERSION 3.4.1)
set (Tutorial_VERSION_MAJOR 1)
set (Tutorial_VERSION_MINOR 0)

set (PROJECT_NAME Native)

project (${PROJECT_NAME})

set(SRC_DIR
    src/main/cpp/SerialPort.cpp
    src/main/cpp/Gpio.cpp)

add_library( # Sets the name of the library.
             ${PROJECT_NAME}
             SHARED
             ${SRC_DIR})

find_library( # Sets the name of the path variable.
              log-lib
              log )

target_link_libraries( # Specifies the target library.
                       ${PROJECT_NAME}
                       ${log-lib} )