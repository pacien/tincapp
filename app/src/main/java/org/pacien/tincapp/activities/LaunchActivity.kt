package org.pacien.tincapp.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.net.VpnService
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.dialog_decrypt_keys.view.*
import org.pacien.tincapp.R
import org.pacien.tincapp.commands.TincApp
import org.pacien.tincapp.context.App
import org.pacien.tincapp.intent.action.ACTION_CONNECT
import org.pacien.tincapp.intent.action.ACTION_DISCONNECT
import org.pacien.tincapp.intent.action.TINC_SCHEME
import org.pacien.tincapp.service.TincVpnService
import org.pacien.tincapp.utils.PemUtils
import java.io.FileNotFoundException

/**
 * @author pacien
 */
class LaunchActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    when (intent.action) {
      ACTION_CONNECT -> requestPerm()
      ACTION_DISCONNECT -> disconnect()
    }
  }

  override fun onActivityResult(request: Int, result: Int, data: Intent?) {
    if (request == PERMISSION_REQUEST_CODE && result == Activity.RESULT_OK) askPassphrase()
  }

  private fun requestPerm() = VpnService.prepare(this).let {
    if (it != null)
      startActivityForResult(it, PERMISSION_REQUEST_CODE)
    else
      onActivityResult(PERMISSION_REQUEST_CODE, Activity.RESULT_OK, null)
  }

  @SuppressLint("InflateParams")
  private fun askPassphrase() {
    val netName = intent.data.schemeSpecificPart

    if (needPassphrase(netName) && intent.data.fragment == null) {
      val dialog = layoutInflater.inflate(R.layout.dialog_decrypt_keys, null, false)
      AlertDialog.Builder(this)
        .setTitle(R.string.title_unlock_private_keys).setView(dialog)
        .setPositiveButton(R.string.action_unlock) { _, _ -> connect(netName, dialog.passphrase.text.toString()) }
        .setNegativeButton(R.string.action_cancel, { _, _ -> finish() })
        .show()
    } else {
      connect(netName, intent.data.fragment)
    }
  }

  private fun needPassphrase(netName: String) = try {
    TincApp.listPrivateKeys(netName).filter { it.exists() }.any { PemUtils.isEncrypted(PemUtils.read(it)) }
  } catch (e: FileNotFoundException) {
    false
  }

  private fun connect(netName: String, passphrase: String? = null) {
    TincVpnService.startVpn(netName, passphrase)
    finish()
  }

  private fun disconnect() {
    TincVpnService.stopVpn()
    finish()
  }

  companion object {

    private val PERMISSION_REQUEST_CODE = 0

    fun connect(netName: String, passphrase: String? = null) {
      App.getContext().startActivity(Intent(App.getContext(), LaunchActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .setAction(ACTION_CONNECT)
        .setData(Uri.Builder().scheme(TINC_SCHEME).opaquePart(netName).fragment(passphrase).build()))
    }

    fun disconnect() {
      App.getContext().startActivity(Intent(App.getContext(), LaunchActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .setAction(ACTION_DISCONNECT))
    }

  }

}
