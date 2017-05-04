cmake_minimum_required(VERSION 3.4.1)
include(ExternalProject)

set(xCONFIG
	"CC=${CMAKE_C_COMPILER} ${CMAKE_C_COMPILE_OPTIONS_EXTERNAL_TOOLCHAIN}${CMAKE_C_COMPILER_EXTERNAL_TOOLCHAIN} ${CMAKE_C_COMPILE_OPTIONS_TARGET}${CMAKE_C_COMPILER_TARGET}"
	"LD=${CMAKE_LINKER}"
	"AR=${CMAKE_AR}"
	"RANLIB=${CMAKE_RANLIB}"
	"CFLAGS=${CMAKE_C_FLAGS} ${CMAKE_C_COMPILE_OPTIONS_SYSROOT}${CMAKE_SYSROOT}"
	"LDFLAGS=${CMAKE_STATIC_LINKER_FLAGS} ${CMAKE_C_COMPILE_OPTIONS_SYSROOT}${CMAKE_SYSROOT}"
	"--host=${CMAKE_C_COMPILER_TARGET}"
)

if(${ANDROID_ABI} STREQUAL "mips64")
	list(APPEND xCONFIG --disable-asm)
endif()

ExternalProject_Add(lzo
	URL					http://files.pacien.net/tmp/lzo-2.10.tar.gz
	URL_HASH			SHA1=4924676a9bae5db58ef129dc1cebce3baa3c4b5d
	CONFIGURE_COMMAND	<SOURCE_DIR>/configure ${xCONFIG} --disable-shared
	BUILD_COMMAND		make -j4
	INSTALL_COMMAND		make install DESTDIR=${CMAKE_CURRENT_BINARY_DIR} &&
						rm -r <BINARY_DIR>
)

ExternalProject_Add(libressl
	URL					http://files.pacien.net/tmp/libressl-2.5.4.tar.gz
	URL_HASH			SHA256=107a5b522fbb8318d4c3be668075e5e607296f0a9255d71674caa94571336efa
	CONFIGURE_COMMAND	<SOURCE_DIR>/configure ${xCONFIG} --disable-shared
	BUILD_COMMAND		make -j4 -C crypto
	INSTALL_COMMAND		make -C crypto install DESTDIR=${CMAKE_CURRENT_BINARY_DIR} &&
						make -C include install DESTDIR=${CMAKE_CURRENT_BINARY_DIR} &&
						rm -r <BINARY_DIR>
)

ExternalProject_Add(tinc
	DEPENDS				lzo libressl
	URL					http://files.pacien.net/tmp/tinc-1.1pre15-SNAPSHOT.tar.gz
	CONFIGURE_COMMAND	autoreconf -fsi <SOURCE_DIR> &&
						<SOURCE_DIR>/configure ${xCONFIG}
						--with-openssl=${CMAKE_CURRENT_BINARY_DIR}/usr/local
						--with-lzo=${CMAKE_CURRENT_BINARY_DIR}/usr/local
						--disable-curses
						--disable-readline
	BUILD_COMMAND		make -j4 -C src
	INSTALL_COMMAND		make -C src install DESTDIR=${CMAKE_CURRENT_BINARY_DIR} &&
						${CMAKE_COMMAND} -E copy ${CMAKE_CURRENT_BINARY_DIR}/usr/local/sbin/tinc ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libtinc.so &&
						${CMAKE_COMMAND} -E copy ${CMAKE_CURRENT_BINARY_DIR}/usr/local/sbin/tincd ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libtincd.so &&
						rm -r <BINARY_DIR>
)

add_library(exec SHARED src/main/c/exec.c)
add_dependencies(exec tinc)
