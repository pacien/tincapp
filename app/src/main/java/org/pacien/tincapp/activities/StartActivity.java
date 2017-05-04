package org.pacien.tincapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import org.pacien.tincapp.R;
import org.pacien.tincapp.commands.PermissionFixer;
import org.pacien.tincapp.context.AppPaths;
import org.pacien.tincapp.service.TincVpnService;

/**
 * @author pacien
 */
public class StartActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getLayoutInflater().inflate(R.layout.page_start, getContentView());
	}

	@Override
	protected void onActivityResult(int request, int result, Intent data) {
		notify(result == RESULT_OK ? R.string.message_vpn_permissions_granted : R.string.message_vpn_permissions_denied);
	}

	public void requestVpnPermission(View v) {
		Intent askPermIntent = TincVpnService.prepare(this);

		if (askPermIntent != null)
			startActivityForResult(askPermIntent, 0);
		else
			onActivityResult(0, RESULT_OK, null);
	}

	public void startVpnDialog(View v) {
		final EditText i = new EditText(this);
		i.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
		i.setHint(R.string.field_net_name);

		@SuppressLint("InflateParams")
		ViewGroup vg = (ViewGroup) getLayoutInflater().inflate(R.layout.dialog_frame, null);
		vg.addView(i);

		new AlertDialog.Builder(this)
				.setTitle(R.string.title_connect_to_network)
				.setView(vg)
				.setPositiveButton(R.string.action_connect, (dialog, which) -> startVpn(i.getText().toString()))
				.setNegativeButton(R.string.action_close, (dialog, which) -> { /* nop */ })
				.show();
	}

	public void confDirDialog(View v) {
		String confDir = AppPaths.confDir(this).getPath();

		new AlertDialog.Builder(this)
				.setTitle(R.string.title_tinc_config_dir)
				.setMessage(confDir)
				.setNeutralButton(R.string.action_fix_perms, (dialog, which) -> fixPerms())
				.setNegativeButton(R.string.action_copy,
						(dialog, which) -> copyIntoClipboard(getResources().getString(R.string.title_tinc_config_dir), confDir))
				.setPositiveButton(R.string.action_close, (dialog, which) -> { /* nop */ })
				.show();
	}

	private void startVpn(String netName) {
		startService(new Intent(this, TincVpnService.class)
				.putExtra(TincVpnService.INTENT_EXTRA_NET_NAME, netName));
	}

	private void fixPerms() {
		boolean ok = PermissionFixer.makePrivateDirsPublic(getApplicationContext());
		notify(ok ? R.string.message_perms_fixed : R.string.message_perms_fix_failure);
	}

}
