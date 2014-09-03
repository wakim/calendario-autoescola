package br.com.wakim.autoescola.calendario.app.adapter;

import android.support.annotation.IdRes;

/**
 * Created by wakim on 30/08/14.
 */
public interface OnOptionClickListener {
	public void onOptionClick(int position, @IdRes int optionId);
}
