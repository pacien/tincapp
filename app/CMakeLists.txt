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
	URL					http://www.oberhumer.com/opensource/lzo/download/lzo-2.10.tar.gz
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
	URL					http://tinc-vpn.org/git/browse?p=tinc;a=snapshot;h=92fdabc439bdb5e16f64a4bf2ed1deda54f7c544;sf=tgz
	URL_HASH            SHA256=ddc5071e9ecadc75709fead6b16241044a74765eb108727d8fe0206ee4fecd1d
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
