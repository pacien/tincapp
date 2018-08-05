/*
 * tinc app, an Android binding and user interface for the tinc mesh VPN daemon
 * Copyright (C) 2017-2018 Pacien TRAN-GIRARD
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.pacien.tincapp.utils

import org.bouncycastle.openssl.PEMException
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder
import org.bouncycastle.openssl.jcajce.JcePEMEncryptorBuilder
import org.bouncycastle.util.encoders.Hex
import org.bouncycastle.util.io.pem.PemHeader
import org.bouncycastle.util.io.pem.PemObject
import java.io.File
import java.io.FileReader
import java.io.Writer

/**
 * @author pacien
 */
object PemUtils {
  private const val DEK_INFO_HEADER_KEY = "DEK-Info"
  private const val ALGORITHM = "AES-256-CBC"
  private val PROVIDER = org.bouncycastle.jce.provider.BouncyCastleProvider()
  private val ENCRYPTED_PROC_TYPE_HEADER = PemHeader("Proc-Type", "4,ENCRYPTED")

  private class DekInfo(val algName: String, val iv: ByteArray)

  private fun dekInfoHeader(iv: ByteArray) = PemHeader(DEK_INFO_HEADER_KEY, "$ALGORITHM,${Hex.toHexString(iv)}")

  private fun PemObject.getPemHeaders() = headers.map { headerObj -> headerObj as PemHeader }

  private fun PemObject.dekInfo() = try {
    getPemHeaders()
      .find { header -> header.name == DEK_INFO_HEADER_KEY }!!
      .value!!
      .split(',')
      .let { headerParts -> DekInfo(headerParts[0], Hex.decode(headerParts[1])) }
  } catch (e: Exception) {
    throw PEMException("Malformed DEK-Info header.", e)
  }

  private fun encryptor(passPhrase: String) =
    JcePEMEncryptorBuilder(ALGORITHM)
      .setProvider(PROVIDER)
      .build(passPhrase.toCharArray())!!

  private fun decryptor(algName: String, passPhrase: String?) =
    JcePEMDecryptorProviderBuilder()
      .setProvider(PROVIDER)
      .build(passPhrase?.toCharArray())
      .get(algName)!!

  fun read(f: File) = PEMParser(FileReader(f)).readPemObject()!!

  fun write(obj: PemObject, out: Writer) =
    JcaPEMWriter(out)
      .apply { writeObject(obj) }
      .apply { close() }

  fun isEncrypted(obj: PemObject) = obj.headers.contains(ENCRYPTED_PROC_TYPE_HEADER)

  fun encrypt(obj: PemObject, passPhrase: String): PemObject {
    val encryptor = encryptor(passPhrase)
    val headers = listOf(ENCRYPTED_PROC_TYPE_HEADER, dekInfoHeader(encryptor.iv))
    val body = encryptor.encrypt(obj.content)
    return PemObject(obj.type, headers, body)
  }

  fun decrypt(obj: PemObject, passPhrase: String?): PemObject =
    if (isEncrypted(obj)) {
      val dekInfo = obj.dekInfo()
      val decryptor = decryptor(dekInfo.algName, passPhrase)
      val body = decryptor.decrypt(obj.content, dekInfo.iv)
      PemObject(obj.type, body)
    } else {
      obj
    }
}
