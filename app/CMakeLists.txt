# Tinc Mesh VPN: Android client and user interface
# Copyright (C) 2017-2026 Euxane P. TRAN-GIRARD
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.

cmake_minimum_required(VERSION 3.19) # string(JSON)
include(ExternalProject)

set(CC_OPTIMISATION_FLAGS "-ffunction-sections -fdata-sections")
set(LD_OPTIMISATION_FLAGS "-Wl,--gc-sections")

# Source archives of the native dependencies, also used by the Nix build
file(READ "${CMAKE_CURRENT_SOURCE_DIR}/native-sources.json" NATIVE_SOURCES)
set_property(DIRECTORY APPEND PROPERTY CMAKE_CONFIGURE_DEPENDS native-sources.json)

# Directory providing the source archives instead of downloading them,
# for offline builds.
set(SOURCE_ARCHIVES_DIR "$ENV{TINCAPP_SOURCE_ARCHIVES_DIR}")

function(native_source name)
  string(JSON url GET "${NATIVE_SOURCES}" ${name} url)
  string(JSON sha256 GET "${NATIVE_SOURCES}" ${name} sha256)
  if(SOURCE_ARCHIVES_DIR)
    get_filename_component(file "${url}" NAME)
    set(url "${SOURCE_ARCHIVES_DIR}/${file}")
  endif()
  set(${name}_SOURCE URL "${url}" URL_HASH "SHA256=${sha256}" PARENT_SCOPE)
endfunction()

native_source(lzo)
native_source(libressl)
native_source(tinc)

set(xCONFIG
  "CC=${CMAKE_C_COMPILER} \
    ${CMAKE_C_COMPILE_OPTIONS_EXTERNAL_TOOLCHAIN}${CMAKE_C_COMPILER_EXTERNAL_TOOLCHAIN} \
    ${CMAKE_C_COMPILE_OPTIONS_TARGET}${CMAKE_C_COMPILER_TARGET} \
    ${CMAKE_C_COMPILE_OPTIONS_SYSROOT}${CMAKE_SYSROOT}"
  "LD=${CMAKE_LINKER}"
  "AR=${CMAKE_AR}"
  "RANLIB=${CMAKE_RANLIB}"
  "CFLAGS=${CMAKE_C_FLAGS} ${CC_OPTIMISATION_FLAGS}"
  "LDFLAGS=${CMAKE_SHARED_LINKER_FLAGS} ${LD_OPTIMISATION_FLAGS}"
  "--host=${CMAKE_C_COMPILER_TARGET}"
)

ExternalProject_Add(lzo
  ${lzo_SOURCE}
  CONFIGURE_COMMAND <SOURCE_DIR>/configure ${xCONFIG} --disable-shared
  BUILD_COMMAND     make -j8
  INSTALL_COMMAND   make install DESTDIR=${CMAKE_CURRENT_BINARY_DIR} &&
                    rm -r <BINARY_DIR>
)

ExternalProject_Add(libressl
  ${libressl_SOURCE}
  CONFIGURE_COMMAND <SOURCE_DIR>/configure ${xCONFIG} --disable-shared
  BUILD_COMMAND     make -j8 -C crypto
  INSTALL_COMMAND   make -C crypto install DESTDIR=${CMAKE_CURRENT_BINARY_DIR} &&
                    make -C include install DESTDIR=${CMAKE_CURRENT_BINARY_DIR} &&
                    rm -r <BINARY_DIR>
)

ExternalProject_Add(tinc
  DEPENDS           lzo libressl
  ${tinc_SOURCE}
  # Backport from https://github.com/pacien/tinc/commit/14de269d013f72d797c3baf249fdec65bdbcee89
  PATCH_COMMAND     patch -p1 < ${PROJECT_SOURCE_DIR}/src/main/c/tinc-async-resolver.patch
  CONFIGURE_COMMAND autoreconf -fsi <SOURCE_DIR> &&
                    <SOURCE_DIR>/configure ${xCONFIG}
                      --with-openssl=${CMAKE_CURRENT_BINARY_DIR}/usr/local
                      --with-lzo=${CMAKE_CURRENT_BINARY_DIR}/usr/local
                      --disable-curses
                      --disable-readline
  BUILD_COMMAND     make -j8 -C src
  INSTALL_COMMAND   make -C src install DESTDIR=${CMAKE_CURRENT_BINARY_DIR} &&
                    ${CMAKE_COMMAND} -E copy
                      ${CMAKE_CURRENT_BINARY_DIR}/usr/local/sbin/tinc
                      ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libtinc.so &&
                    ${CMAKE_COMMAND} -E copy
                      ${CMAKE_CURRENT_BINARY_DIR}/usr/local/sbin/tincd
                      ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libtincd.so &&
                    rm -r <BINARY_DIR>
)

add_library(main SHARED src/main/c/main.c)
add_dependencies(main tinc)
