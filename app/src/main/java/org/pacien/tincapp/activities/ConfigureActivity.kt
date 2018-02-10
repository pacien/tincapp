package org.pacien.tincapp.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import android.view.View
import java8.util.concurrent.CompletableFuture
import kotlinx.android.synthetic.main.base.*
import kotlinx.android.synthetic.main.dialog_encrypt_decrypt_keys.view.*
import kotlinx.android.synthetic.main.dialog_network_generate.view.*
import kotlinx.android.synthetic.main.dialog_network_join.view.*
import kotlinx.android.synthetic.main.page_configure.*
import org.pacien.tincapp.R
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.commands.TincApp
import org.pacien.tincapp.context.App
import org.pacien.tincapp.context.AppPaths
import org.pacien.tincapp.extensions.Java.exceptionallyAccept

/**
 * @author pacien
 */
class ConfigureActivity : BaseActivity() {

  companion object {
    val REQUEST_SCAN = 0
    val SCAN_PROVIDER = "com.google.zxing.client.android"
  }

  private var joinDialog: View? = null

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode == REQUEST_SCAN && resultCode == Activity.RESULT_OK)
      joinDialog?.invitation_url?.setText(data!!.getStringExtra("SCAN_RESULT").trim())
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    layoutInflater.inflate(R.layout.page_configure, main_content)
    writeContent()
  }

  fun scanCode(@Suppress("UNUSED_PARAMETER") v: View) {
    try {
      startActivityForResult(Intent("$SCAN_PROVIDER.SCAN"), REQUEST_SCAN)
    } catch (e: ActivityNotFoundException) {
      AlertDialog.Builder(this).setTitle(R.string.action_scan_qr_code)
        .setMessage(R.string.message_no_qr_code_scanner)
        .setPositiveButton(R.string.action_install) { _, _ ->
          startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$SCAN_PROVIDER")))
        }.setNegativeButton(R.string.action_cancel, App.dismissAction).show()
    }
  }

  fun openGenerateConfDialog(@Suppress("UNUSED_PARAMETER") v: View) {
    val genDialog = layoutInflater.inflate(R.layout.dialog_network_generate, main_content, false)
    AlertDialog.Builder(this).setTitle(R.string.title_new_network).setView(genDialog)
      .setPositiveButton(R.string.action_create) { _, _ ->
        generateConf(
          genDialog.new_net_name.text.toString(),
          genDialog.new_node_name.text.toString(),
          genDialog.new_passphrase.text.toString())
      }.setNegativeButton(R.string.action_cancel, App.dismissAction).show()
  }

  fun openJoinNetworkDialog(@Suppress("UNUSED_PARAMETER") v: View) {
    joinDialog = layoutInflater.inflate(R.layout.dialog_network_join, main_content, false)
    AlertDialog.Builder(this).setTitle(R.string.title_join_network).setView(joinDialog)
      .setPositiveButton(R.string.action_join) { _, _ ->
        joinNetwork(
          joinDialog!!.net_name.text.toString(),
          joinDialog!!.invitation_url.text.toString(),
          joinDialog!!.join_passphrase.text.toString())
      }.setNegativeButton(R.string.action_cancel, App.dismissAction).show()
  }

  fun openEncryptDecryptPrivateKeyDialog(@Suppress("UNUSED_PARAMETER") v: View) {
    val encryptDecryptDialog = layoutInflater.inflate(R.layout.dialog_encrypt_decrypt_keys, main_content, false)
    AlertDialog.Builder(this).setTitle(R.string.title_private_keys_encryption).setView(encryptDecryptDialog)
      .setPositiveButton(R.string.action_apply) { _, _ ->
        encryptDecryptPrivateKeys(
          encryptDecryptDialog!!.enc_dec_net_name.text.toString(),
          encryptDecryptDialog.enc_dec_current_passphrase.text.toString(),
          encryptDecryptDialog.enc_dec_new_passphrase.text.toString())
      }.setNegativeButton(R.string.action_cancel, App.dismissAction).show()
  }

  private fun writeContent() {
    text_configuration_directory.text = AppPaths.confDir().absolutePath
    text_log_directory.text = AppPaths.cacheDir().absolutePath
    text_tinc_binary.text = AppPaths.tinc().absolutePath
  }

  private fun generateConf(netName: String, nodeName: String, passphrase: String? = null) = execAction(
    R.string.message_generating_configuration,
    Tinc.init(netName, nodeName)
      .thenCompose { TincApp.removeScripts(netName) }
      .thenCompose { TincApp.setPassphrase(netName, newPassphrase = passphrase) })

  private fun joinNetwork(netName: String, url: String, passphrase: String? = null) = execAction(
    R.string.message_joining_network,
    Tinc.join(netName, url)
      .thenCompose { TincApp.removeScripts(netName) }
      .thenCompose { TincApp.generateIfaceCfg(netName) }
      .thenCompose { TincApp.setPassphrase(netName, newPassphrase = passphrase) })

  private fun encryptDecryptPrivateKeys(netName: String, currentPassphrase: String, newPassphrase: String) = execAction(
    R.string.message_encrypting_decrypting_private_keys,
    TincApp.setPassphrase(netName, currentPassphrase, newPassphrase))

  private fun execAction(@StringRes label: Int, action: CompletableFuture<Void>) {
    showProgressDialog(label).let { progressDialog ->
      action
        .whenComplete { _, _ -> progressDialog.dismiss() }
        .thenAccept { notify(R.string.message_network_configuration_written) }
        .exceptionallyAccept { runOnUiThread { showErrorDialog(it.cause!!.localizedMessage) } }
    }
  }

}
