cmake_minimum_required(VERSION 3.4.1)
include(ExternalProject)

set(xCONFIG
	"CC=${CMAKE_C_COMPILER} \
		${CMAKE_C_COMPILE_OPTIONS_EXTERNAL_TOOLCHAIN}${CMAKE_C_COMPILER_EXTERNAL_TOOLCHAIN} \
		${CMAKE_C_COMPILE_OPTIONS_TARGET}${CMAKE_C_COMPILER_TARGET} \
		${CMAKE_C_COMPILE_OPTIONS_SYSROOT}${CMAKE_SYSROOT}"
	"LD=${CMAKE_LINKER}"
	"AR=${CMAKE_AR}"
	"RANLIB=${CMAKE_RANLIB}"
	"CFLAGS=${CMAKE_C_FLAGS}"
	"LDFLAGS=${CMAKE_SHARED_LINKER_FLAGS}"
	"--host=${CMAKE_C_COMPILER_TARGET}"
)

ExternalProject_Add(	lzo
	URL					https://www.oberhumer.com/opensource/lzo/download/lzo-2.10.tar.gz
	URL_HASH			SHA1=4924676a9bae5db58ef129dc1cebce3baa3c4b5d
	CONFIGURE_COMMAND	<SOURCE_DIR>/configure ${xCONFIG} --disable-shared
	BUILD_COMMAND		make -j8
	INSTALL_COMMAND		make install DESTDIR=${CMAKE_CURRENT_BINARY_DIR} &&
						rm -r <BINARY_DIR>
)

ExternalProject_Add(	libressl
	URL					https://ftp.openbsd.org/pub/OpenBSD/LibreSSL/libressl-2.5.5.tar.gz
	URL_HASH			SHA256=e57f5e3d5842a81fe9351b6e817fcaf0a749ca4ef35a91465edba9e071dce7c4
	CONFIGURE_COMMAND	<SOURCE_DIR>/configure ${xCONFIG} --disable-shared
	BUILD_COMMAND		make -j8 -C crypto
	INSTALL_COMMAND		make -C crypto install DESTDIR=${CMAKE_CURRENT_BINARY_DIR} &&
						make -C include install DESTDIR=${CMAKE_CURRENT_BINARY_DIR} &&
						rm -r <BINARY_DIR>
)

ExternalProject_Add(	tinc
	DEPENDS				lzo libressl
	URL					https://tinc-vpn.org/packages/tinc-1.1pre15.tar.gz
	URL_HASH            SHA256=41dc3e40c5f8be497b779acd6f59ef4572e1430d0d0f0436f2de5cb21a59ef18
	CONFIGURE_COMMAND	autoreconf -fsi <SOURCE_DIR> &&
						<SOURCE_DIR>/configure ${xCONFIG}
						--with-openssl=${CMAKE_CURRENT_BINARY_DIR}/usr/local
						--with-lzo=${CMAKE_CURRENT_BINARY_DIR}/usr/local
						--disable-curses
						--disable-readline
	BUILD_COMMAND		make -j8 -C src
	INSTALL_COMMAND		make -C src install DESTDIR=${CMAKE_CURRENT_BINARY_DIR} &&
						${CMAKE_COMMAND} -E copy
							${CMAKE_CURRENT_BINARY_DIR}/usr/local/sbin/tinc ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libtinc.so &&
						${CMAKE_COMMAND} -E copy
							${CMAKE_CURRENT_BINARY_DIR}/usr/local/sbin/tincd ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libtincd.so &&
						rm -r <BINARY_DIR>
)

add_library(exec SHARED src/main/c/exec.c)
add_dependencies(exec tinc)
