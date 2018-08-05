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

package org.pacien.tincapp.activities

import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import android.view.View
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import java8.util.concurrent.CompletableFuture
import kotlinx.android.synthetic.main.base.*
import kotlinx.android.synthetic.main.dialog_encrypt_decrypt_keys.view.*
import kotlinx.android.synthetic.main.dialog_network_generate.view.*
import kotlinx.android.synthetic.main.dialog_network_join.view.*
import kotlinx.android.synthetic.main.page_configure.*
import org.pacien.tincapp.R
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.commands.TincApp
import org.pacien.tincapp.context.AppPaths
import org.pacien.tincapp.extensions.Java.exceptionallyAccept
import java.util.regex.Pattern

/**
 * @author pacien
 */
class ConfigureActivity : BaseActivity() {
  companion object {
    private val NETWORK_NAME_PATTERN = Pattern.compile("^[^\\x00/]*$")
  }

  private var joinDialog: View? = null

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
      ?.let(IntentResult::getContents)
      ?.let(String::trim)
      ?.let { joinDialog?.invitation_url?.setText(it) }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    layoutInflater.inflate(R.layout.page_configure, main_content)
    writeContent()
  }

  fun scanCode(@Suppress("UNUSED_PARAMETER") v: View) {
    IntentIntegrator(this).initiateScan()
  }

  fun openGenerateConfDialog(@Suppress("UNUSED_PARAMETER") v: View) {
    val genDialog = layoutInflater.inflate(R.layout.dialog_network_generate, main_content, false)
    AlertDialog.Builder(this).setTitle(R.string.title_new_network).setView(genDialog)
      .setPositiveButton(R.string.action_create) { _, _ ->
        generateConf(
          genDialog.new_net_name.text.toString(),
          genDialog.new_node_name.text.toString(),
          genDialog.new_passphrase.text.toString())
      }.setNegativeButton(R.string.action_cancel) { _, _ -> Unit }.show()
  }

  fun openJoinNetworkDialog(@Suppress("UNUSED_PARAMETER") v: View) {
    joinDialog = layoutInflater.inflate(R.layout.dialog_network_join, main_content, false)
    AlertDialog.Builder(this).setTitle(R.string.title_join_network).setView(joinDialog)
      .setPositiveButton(R.string.action_join) { _, _ ->
        joinNetwork(
          joinDialog!!.net_name.text.toString(),
          joinDialog!!.invitation_url.text.toString(),
          joinDialog!!.join_passphrase.text.toString())
      }.setNegativeButton(R.string.action_cancel) { _, _ -> Unit }.show()
  }

  fun openEncryptDecryptPrivateKeyDialog(@Suppress("UNUSED_PARAMETER") v: View) {
    val encryptDecryptDialog = layoutInflater.inflate(R.layout.dialog_encrypt_decrypt_keys, main_content, false)
    AlertDialog.Builder(this).setTitle(R.string.title_private_keys_encryption).setView(encryptDecryptDialog)
      .setPositiveButton(R.string.action_apply) { _, _ ->
        encryptDecryptPrivateKeys(
          encryptDecryptDialog!!.enc_dec_net_name.text.toString(),
          encryptDecryptDialog.enc_dec_current_passphrase.text.toString(),
          encryptDecryptDialog.enc_dec_new_passphrase.text.toString())
      }.setNegativeButton(R.string.action_cancel) { _, _ -> Unit }.show()
  }

  private fun writeContent() {
    text_configuration_directory.text = AppPaths.confDir().absolutePath
    text_log_directory.text = AppPaths.cacheDir().absolutePath
    text_tinc_binary.text = AppPaths.tinc().absolutePath
  }

  private fun generateConf(netName: String, nodeName: String, passphrase: String? = null) = execAction(
    R.string.message_generating_configuration,
    validateNetName(netName)
      .thenCompose { Tinc.init(netName, nodeName) }
      .thenCompose { TincApp.removeScripts(netName) }
      .thenCompose { TincApp.generateIfaceCfgTemplate(netName) }
      .thenCompose { TincApp.setPassphrase(netName, newPassphrase = passphrase) })

  private fun joinNetwork(netName: String, url: String, passphrase: String? = null) = execAction(
    R.string.message_joining_network,
    validateNetName(netName)
      .thenCompose { Tinc.join(netName, url) }
      .thenCompose { TincApp.removeScripts(netName) }
      .thenCompose { TincApp.generateIfaceCfg(netName) }
      .thenCompose { TincApp.setPassphrase(netName, newPassphrase = passphrase) })

  private fun encryptDecryptPrivateKeys(netName: String, currentPassphrase: String, newPassphrase: String) = execAction(
    R.string.message_encrypting_decrypting_private_keys,
    validateNetName(netName)
      .thenCompose { TincApp.setPassphrase(netName, currentPassphrase, newPassphrase) })

  private fun execAction(@StringRes label: Int, action: CompletableFuture<Unit>) {
    showProgressDialog(label).let { progressDialog ->
      action
        .whenComplete { _, _ -> progressDialog.dismiss() }
        .thenAccept { notify(R.string.message_network_configuration_written) }
        .exceptionallyAccept { runOnUiThread { showErrorDialog(it.cause!!.localizedMessage) } }
    }
  }

  private fun validateNetName(netName: String): CompletableFuture<Unit> =
    if (NETWORK_NAME_PATTERN.matcher(netName).matches())
      CompletableFuture.completedFuture(Unit)
    else
      CompletableFuture.failedFuture(IllegalArgumentException(resources.getString(R.string.message_invalid_network_name)))
}
