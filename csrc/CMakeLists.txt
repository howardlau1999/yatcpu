# Make CMake happy
cmake_minimum_required(VERSION 3.18)
project(yatcpu-programs C CXX ASM)

# Setting variables
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O0 --target=riscv32-unknown-elf -march=rv32i -mabi=ilp32")
set(CMAKE_ASM_FLAGS "${CMAKE_ASM_FLAGS} -O0 --target=riscv32-unknown-elf -march=rv32i -mabi=ilp32")
set(C_PROGRAMS tetris hello fibonacci quicksort)
set(ASM_PROGRAMS mmio sb)
set(LINKER_SCRIPT ${CMAKE_SOURCE_DIR}/link.lds)
set(LINKER_FLAGS -T ${LINKER_SCRIPT}) 
set(OBJCOPY_ARGS -O binary -j .text -j .data)
if(NOT DEST_DIR)
	set(DEST_DIR "../src/main/resources")
endif()

# Let CMake know that there exists header files
include_directories("${CMAKE_SOURCE_DIR}")

add_library(prelude init.S)
set_target_properties(prelude PROPERTIES LINK_DEPENDS ${LINKER_SCRIPT})

# Let's build our executables
foreach(program IN LISTS C_PROGRAMS)
	add_executable(${program} ${program}.c)
	set_target_properties(${program} PROPERTIES LINK_DEPENDS ${LINKER_SCRIPT})
	target_link_libraries(${program} prelude ${LINKER_FLAGS})
endforeach()

foreach(program IN LISTS ASM_PROGRAMS)
	add_executable(${program} ${program}.S)
	set_target_properties(${program} PROPERTIES LINK_DEPENDS ${LINKER_SCRIPT})
endforeach()

set(PROGRAMS litenes)
# NES Emulator
include_directories(${CMAKE_SOURCE_DIR}/LiteNES/include)
add_library(fce
	${CMAKE_SOURCE_DIR}/LiteNES/src/fce/common.c
	${CMAKE_SOURCE_DIR}/LiteNES/src/fce/cpu-addressing.c
	${CMAKE_SOURCE_DIR}/LiteNES/src/fce/cpu.c
	${CMAKE_SOURCE_DIR}/LiteNES/src/fce/fce.c
	${CMAKE_SOURCE_DIR}/LiteNES/src/fce/memory.c
	${CMAKE_SOURCE_DIR}/LiteNES/src/fce/mmc.c
	${CMAKE_SOURCE_DIR}/LiteNES/src/fce/ppu.c
	${CMAKE_SOURCE_DIR}/LiteNES/src/fce/psg.c
)
add_executable(litenes 
	${CMAKE_SOURCE_DIR}/LiteNES/src/main.c
  ${CMAKE_SOURCE_DIR}/LiteNES/src/hal.c
)
set_target_properties(litenes PROPERTIES LINK_DEPENDS ${LINKER_SCRIPT})
target_link_libraries(litenes prelude fce ${LINKER_FLAGS})

# Copy the .text and .data section to .asmbin files
foreach(program IN LISTS C_PROGRAMS ASM_PROGRAMS PROGRAMS)
	add_custom_command(
		TARGET ${program}
		POST_BUILD
		COMMAND ${CMAKE_OBJCOPY} ARGS ${OBJCOPY_ARGS} $<TARGET_FILE:${program}> ${CMAKE_SOURCE_DIR}/${DEST_DIR}/${program}.asmbin
	)
endforeach()

