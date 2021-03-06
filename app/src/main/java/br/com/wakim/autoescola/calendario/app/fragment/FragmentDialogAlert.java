package br.com.wakim.autoescola.calendario.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.Serializable;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.utils.Params;

/**
 * Created by wakim on 17/08/14.
 */
public class FragmentDialogAlert extends FragmentMaterialDialog {

	DialogListener mListener;
	Serializable mParam;

	@Override
	public void onDestroy() {
		super.onDestroy();
		mListener = null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable(Params.DIALOG_PARAM, mParam);
	}

	public FragmentDialogAlert() {}

	public FragmentDialogAlert(Bundle arguments) {
		super(arguments);
	}

	public FragmentDialogAlert(Context context, @StringRes int titleResId, @StringRes int messageResId) {
		this(context.getResources().getString(titleResId),
			 context.getResources().getString(messageResId),
			 context.getResources().getString(R.string.ok_caps),
			 context.getResources().getString(R.string.cancelar_caps)
		);
	}

	public FragmentDialogAlert(Context context, @StringRes int titleResId, @StringRes int messageResId, @StringRes int button1ResId, @StringRes int button2ResId) {
		this(context.getResources().getString(titleResId),
			 context.getResources().getString(messageResId),
			 context.getResources().getString(button1ResId),
			 context.getResources().getString(button2ResId)
		);
	}

	public FragmentDialogAlert(CharSequence title, CharSequence message) {
		this(title, message, "OK", "CANCELAR");
	}

	public FragmentDialogAlert(CharSequence title, CharSequence message, CharSequence button1, CharSequence button2) {
		super(title, button1, button2);
		getArguments().putCharSequence(Params.DIALOG_MESSAGE, message);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(savedInstanceState != null) {
			mParam = savedInstanceState.getSerializable(Params.DIALOG_PARAM);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		TextView message = (TextView) view.findViewById(android.R.id.message);
		message.setText(getArguments().getCharSequence(Params.DIALOG_MESSAGE));

		return view;
	}

	@Override
	public boolean onConfirm() {
		if(mListener != null) {
			mListener.onDialogConfirm();
		}

		return true;
	}

	@Override
	public boolean onCancel() {
		if(mListener != null) {
			mListener.onDialogCancel();
		}

		return true;
	}

	public void setParameter(Serializable param) {
		mParam = param;
	}

	public Serializable getParameter() {
		return mParam;
	}

	@Override
	public int getLayoutResourceId() {
		return R.layout.fragment_alert;
	}

	public void setDialogListener(DialogListener listener) {
		mListener = listener;
	}

	public static interface DialogListener {
		public void onDialogCancel();
		public void onDialogConfirm();
	}
}
