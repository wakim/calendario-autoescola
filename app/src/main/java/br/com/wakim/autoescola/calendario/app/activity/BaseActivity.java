package br.com.wakim.autoescola.calendario.app.activity;

import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import br.com.wakim.autoescola.calendario.R;

/**
 * Created by wakim on 11/08/14.
 */
public class BaseActivity extends ActionBarActivity {

	SystemBarTintManager mManager;

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mManager = null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		preAddContent();
		super.onCreate(savedInstanceState);

		ActionBar ab = getSupportActionBar();

		ab.setDisplayHomeAsUpEnabled(getDisplayHomeAsUpEnabled());
		ab.setIcon(android.R.color.transparent);

		if (Build.VERSION.SDK_INT >= 19) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

			mManager = new SystemBarTintManager(this);

			mManager.setStatusBarTintEnabled(true);
			mManager.setStatusBarTintColor(getStatusBarTintColor());
		}

		configureStatusBarImmersive();
	}

	void setTitlePadding(int padding) {
		int resourceId = getResources().getIdentifier("action_bar_title", "id", "android");

		if (resourceId == 0) {
			resourceId = R.id.action_bar_title;
		}

		View view = findViewById(resourceId);

		if(view != null) {
			view.setPadding(view.getPaddingLeft() + padding, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
		}
	}

	void configureStatusBarTint(int color) {
		mManager.setStatusBarTintColor(getStatusBarTintColor());
	}

	public void preAddContent() {}

	int getStatusBarTintColor() {
		return getResources().getColor(R.color.status_bar);
	}

	protected void configureStatusBarImmersive() {
		if (Build.VERSION.SDK_INT >= 19) {
			View root = findViewById(android.R.id.content);

			int offset = getResources().getDimensionPixelOffset(R.dimen.ab_height) + getInternalDimensionSize(getResources(), "status_bar_height");

			root.setPadding(0, offset, 0, 0);
		}
	}

	protected int getInternalDimensionSize(Resources res, String key) {
		int result = 0;
		int resourceId = res.getIdentifier(key, "dimen", "android");
		if (resourceId > 0) {
			result = res.getDimensionPixelSize(resourceId);
		}
		return result;
	}

	public boolean getDisplayHomeAsUpEnabled() {
		return true;
	}
}
