package br.com.wakim.autoescola.calendario.app.fragment;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.utils.Params;

/**
 * Created by wakim on 17/08/14.
 */
public class FragmentDialogAlert extends DialogFragment implements View.OnClickListener {

	DialogListener mListener;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog d = super.onCreateDialog(savedInstanceState);

		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		d.getWindow().setBackgroundDrawable(new ColorDrawable(R.color.transparent));

		return d;
	}

	public FragmentDialogAlert() {}

	public FragmentDialogAlert(Bundle arguments) {
		setArguments(arguments);
	}

	public FragmentDialogAlert(Context context, @StringRes int titleResId, @StringRes int messageResId, @StringRes int button1ResId, @StringRes int button2ResId) {
		Bundle bundle = new Bundle();

		bundle.putString(Params.DIALOG_TITLE, context.getResources().getString(titleResId));
		bundle.putString(Params.DIALOG_MESSAGE, context.getResources().getString(messageResId));
		bundle.putString(Params.DIALOG_BUTTON1, context.getResources().getString(button1ResId));
		bundle.putString(Params.DIALOG_BUTTON2, context.getResources().getString(button2ResId));

		setArguments(bundle);
	}

	public FragmentDialogAlert(Context context, String title, String message, String button1, String button2) {
		Bundle bundle = new Bundle();

		bundle.putString(Params.DIALOG_TITLE, title);
		bundle.putString(Params.DIALOG_MESSAGE, message);
		bundle.putString(Params.DIALOG_BUTTON1, button1);
		bundle.putString(Params.DIALOG_BUTTON2, button2);

		setArguments(bundle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_alert, null);

		TextView title = (TextView) view.findViewById(android.R.id.title);
		TextView message = (TextView) view.findViewById(android.R.id.message);

		TextView button1 = (TextView) view.findViewById(android.R.id.button1);
		TextView button2 = (TextView) view.findViewById(android.R.id.button2);

		button1.setOnClickListener(this);
		button2.setOnClickListener(this);

		title.setText(getArguments().getString(Params.DIALOG_TITLE));
		message.setText(getArguments().getString(Params.DIALOG_MESSAGE));

		button1.setText(getArguments().getString(Params.DIALOG_BUTTON1));
		button2.setText(getArguments().getString(Params.DIALOG_BUTTON2));

		return view;
	}

	public void setDialogListener(DialogListener listener) {
		mListener = listener;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		if(id == android.R.id.button2) {
			if(mListener != null) {
				mListener.onCancel();
			}

			dismiss();
		} else if(id == android.R.id.button1) {
			if(mListener != null) {
				mListener.onConfirm();
			}

			dismiss();
		}
	}

	public static interface DialogListener {
		public void onCancel();
		public void onConfirm();
	}
}
