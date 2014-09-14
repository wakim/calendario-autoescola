package br.com.wakim.autoescola.calendario.app.fragment;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
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
 * Created by wakim on 11/09/14.
 */
public abstract class FragmentMaterialDialog extends DialogFragment implements View.OnClickListener {

	public FragmentMaterialDialog() {}

	public FragmentMaterialDialog(Bundle arguments) {
		setArguments(arguments);
	}

	public FragmentMaterialDialog(Context context, @StringRes int titleResId) {
		this(context.getResources().getString(titleResId),
			 context.getResources().getString(R.string.ok_caps),
			 context.getResources().getString(R.string.cancelar_caps)
		);
	}

	public FragmentMaterialDialog(Context context, @StringRes int titleResId, @StringRes int button1ResId, @StringRes int button2ResId) {
		this(context.getResources().getString(titleResId),
			 context.getResources().getString(button1ResId),
			 context.getResources().getString(button2ResId)
		);
	}

	public FragmentMaterialDialog(CharSequence title) {
		this(title, "OK", "CANCELAR");
	}

	public FragmentMaterialDialog(CharSequence title, CharSequence button1, CharSequence button2) {
		Bundle bundle = new Bundle();

		bundle.putCharSequence(Params.DIALOG_TITLE, title);
		bundle.putCharSequence(Params.DIALOG_BUTTON1, button1);
		bundle.putCharSequence(Params.DIALOG_BUTTON2, button2);

		setArguments(bundle);
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog d = super.onCreateDialog(savedInstanceState);

		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		d.getWindow().setBackgroundDrawable(new ColorDrawable(R.color.transparent));

		return d;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(getLayoutResourceId(), null);

		TextView button1 = (TextView) view.findViewById(android.R.id.button1);
		TextView button2 = (TextView) view.findViewById(android.R.id.button2);

		if(button1 != null) {
			button1.setOnClickListener(this);
		}

		if(button2 != null) {
			button2.setOnClickListener(this);
		}

		if(getArguments() != null) {

			if(getArguments().containsKey(Params.DIALOG_TITLE)) {
				TextView title = (TextView) view.findViewById(android.R.id.title);
				title.setText(getArguments().getCharSequence(Params.DIALOG_TITLE));
			}

			if(button1 != null && getArguments().containsKey(Params.DIALOG_BUTTON1)) {
				button1.setText(getArguments().getCharSequence(Params.DIALOG_BUTTON1));
			}

			if(button2 != null && getArguments().containsKey(Params.DIALOG_BUTTON2)) {
				button2.setText(getArguments().getCharSequence(Params.DIALOG_BUTTON2));
			}
		}

		return view;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		if(id == android.R.id.button2) {
			if(onCancel()) {
				dismiss();
			}
		} else if(id == android.R.id.button1) {
			if(onConfirm()) {
				dismiss();
			}
		}
	}

	public abstract boolean onConfirm();
	public abstract boolean onCancel();

	public abstract @LayoutRes int getLayoutResourceId();
}
