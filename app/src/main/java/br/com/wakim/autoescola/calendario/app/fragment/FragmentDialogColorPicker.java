package br.com.wakim.autoescola.calendario.app.fragment;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.utils.Params;

/**
 * Created by wakim on 17/08/14.
 */
public class FragmentDialogColorPicker extends DialogFragment implements View.OnClickListener {

	DialogListener mListener;
	ColorPicker mPicker;

	Integer mColor;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog d = super.onCreateDialog(savedInstanceState);

		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		d.getWindow().setBackgroundDrawable(new ColorDrawable(R.color.transparent));

		return d;
	}

	public FragmentDialogColorPicker() {}

	public FragmentDialogColorPicker(int cor) {
		Bundle b = new Bundle();

		b.putInt(Params.COLOR, cor);

		setArguments(b);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(savedInstanceState != null) {
			mColor = savedInstanceState.getInt(Params.COLOR);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_color_picker, null);

		TextView button1 = (TextView) view.findViewById(android.R.id.button1);
		TextView button2 = (TextView) view.findViewById(android.R.id.button2);

		button1.setOnClickListener(this);
		button2.setOnClickListener(this);

		mPicker = (ColorPicker) view.findViewById(R.id.color_picker);

		if(mColor == null) {
			mColor = getResources().getColor(R.color.primary);
		}

		mColor = getArguments().getInt(Params.COLOR, mColor);

		configureColorPicker(view, mColor);

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(Params.COLOR, mPicker.getColor());
	}

	void configureColorPicker(View view, int color) {

		SVBar svBar = (SVBar) view.findViewById(R.id.color_svbar);
		SaturationBar saturationBar = (SaturationBar) view.findViewById(R.id.color_saturationbar);
		ValueBar valueBar = (ValueBar) view.findViewById(R.id.color_valuebar);
		OpacityBar opacityBar = (OpacityBar) view.findViewById(R.id.color_opacitybar);

		mPicker.addOpacityBar(opacityBar);
		mPicker.addSVBar(svBar);
		mPicker.addSaturationBar(saturationBar);
		mPicker.addValueBar(valueBar);

		mPicker.setOldCenterColor(color);
		mPicker.setColor(color);
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
				mListener.onConfirm(mPicker.getColor());
			}

			dismiss();
		}
	}

	public static interface DialogListener {
		public void onCancel();
		public void onConfirm(int cor);
	}
}
