package org.pacien.tincapp.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import org.pacien.tincapp.BuildConfig;
import org.pacien.tincapp.R;
import org.pacien.tincapp.context.AppInfo;

/**
 * @author pacien
 */
public abstract class BaseActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.base);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu m) {
		getMenuInflater().inflate(R.menu.menu_base, m);
		return true;
	}

	public void aboutDialog(MenuItem i) {
		new AlertDialog.Builder(this)
				.setTitle(BuildConfig.APPLICATION_ID)
				.setMessage(getResources().getString(R.string.app_short_desc) + "\n\n" +
						getResources().getString(R.string.app_copyright) + " " +
						getResources().getString(R.string.app_license) + "\n\n" +
						AppInfo.all(getResources()))
				.setNeutralButton(R.string.action_open_project_website, (dialog, which) -> openWebsite(R.string.app_website_url))
				.setPositiveButton(R.string.action_close, (dialog, which) -> { /* nop */ })
				.show();
	}

	protected ViewGroup getContentView() {
		return (ViewGroup) findViewById(R.id.main_content);
	}

	protected void openWebsite(@StringRes int url) {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(url))));
	}

	protected void notify(@StringRes int msg) {
		Snackbar.make(findViewById(R.id.activity_base), msg, Snackbar.LENGTH_LONG).show();
	}

	protected void copyIntoClipboard(String label, String str) {
		ClipboardManager c = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		c.setPrimaryClip(ClipData.newPlainText(label, str));
		notify(R.string.message_text_copied);
	}

}
