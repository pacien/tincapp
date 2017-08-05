package org.pacien.tincapp.activities

import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import java8.util.concurrent.CompletableFuture
import kotlinx.android.synthetic.main.base.*
import kotlinx.android.synthetic.main.page_configure.*
import org.pacien.tincapp.R
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.commands.TincApp
import org.pacien.tincapp.context.AppPaths
import org.pacien.tincapp.extensions.Java.exceptionallyAccept

/**
 * @author pacien
 */
class ConfigureActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        layoutInflater.inflate(R.layout.page_configure, main_content)
        writeContent()
    }

    fun openGenerateConfDialog(@Suppress("UNUSED_PARAMETER") v: View) {
        val netNameField = EditText(this)
        netNameField.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        netNameField.setHint(R.string.field_net_name)

        val nodeNameField = EditText(this)
        nodeNameField.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        nodeNameField.setHint(R.string.field_node_name)

        val dialogFrame = layoutInflater.inflate(R.layout.dialog_frame, main_content, false) as ViewGroup
        dialogFrame.addView(netNameField)
        dialogFrame.addView(nodeNameField)

        AlertDialog.Builder(this).setTitle(R.string.title_new_network).setView(dialogFrame)
                .setPositiveButton(R.string.action_create) { _, _ -> generateConf(netNameField.text.toString(), nodeNameField.text.toString()) }
                .setNegativeButton(R.string.action_cancel, dismiss).show()
    }

    fun openJoinNetworkDialog(@Suppress("UNUSED_PARAMETER") v: View) {
        val netNameField = EditText(this)
        netNameField.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        netNameField.setHint(R.string.field_net_name)

        val joinUrlField = EditText(this)
        joinUrlField.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        joinUrlField.setHint(R.string.field_invitation_url)

        val dialogFrame = layoutInflater.inflate(R.layout.dialog_frame, main_content, false) as ViewGroup
        dialogFrame.addView(netNameField)
        dialogFrame.addView(joinUrlField)

        AlertDialog.Builder(this).setTitle(R.string.title_join_network).setView(dialogFrame)
                .setPositiveButton(R.string.action_join) { _, _ -> joinNetwork(netNameField.text.toString(), joinUrlField.text.toString()) }
                .setNegativeButton(R.string.action_cancel, dismiss).show()
    }

    private fun writeContent() {
        text_configuration_directory.text = AppPaths.confDir().absolutePath
        text_log_directory.text = AppPaths.cacheDir().absolutePath
        text_tinc_binary.text = AppPaths.tinc().absolutePath
    }

    private fun generateConf(netName: String, nodeName: String) = execAction(
            R.string.message_generating_configuration,
            Tinc.init(netName, nodeName)
                    .thenCompose { TincApp.removeScripts(netName) })

    private fun joinNetwork(netName: String, url: String) = execAction(
            R.string.message_joining_network,
            Tinc.join(netName, url)
                    .thenCompose { TincApp.removeScripts(netName) }
                    .thenCompose { TincApp.generateIfaceCfg(netName) })

    private fun execAction(@StringRes label: Int, action: CompletableFuture<Void>) {
        showProgressDialog(label).let { progressDialog ->
            action
                    .whenComplete { _, _ -> progressDialog.dismiss() }
                    .thenAccept { notify(R.string.message_network_configuration_created) }
                    .exceptionallyAccept { runOnUiThread { showErrorDialog(it.cause!!.localizedMessage) } }
        }
    }

}
