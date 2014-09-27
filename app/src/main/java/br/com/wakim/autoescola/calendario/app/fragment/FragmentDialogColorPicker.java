package br.com.wakim.autoescola.calendario.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
public class FragmentDialogColorPicker extends FragmentMaterialDialog {

	DialogListener mListener;
	ColorPicker mPicker;

	@Override
	public void onDestroy() {
		super.onDestroy();

		mListener = null;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		mPicker = null;
	}

	public FragmentDialogColorPicker() {}

	public FragmentDialogColorPicker(int cor) {
		Bundle b = new Bundle();

		b.putInt(Params.COLOR, cor);

		setArguments(b);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		mPicker = (ColorPicker) view.findViewById(R.id.color_picker);

		if(savedInstanceState == null) {
			int cor = getArguments().getInt(Params.COLOR, getResources().getColor(R.color.primary));
			configureColorPicker(view, cor);
		}

		return view;
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
	public boolean onConfirm() {
		if(mListener != null) {
			mListener.onColorPickerConfirm(mPicker.getColor());
		}

		return true;
	}

	@Override
	public boolean onCancel() {
		if(mListener != null) {
			mListener.onColorPickerCancel();
		}

		return true;
	}

	@Override
	public int getLayoutResourceId() {
		return R.layout.fragment_color_picker;
	}

	public static interface DialogListener {
		public void onColorPickerCancel();
		public void onColorPickerConfirm(int cor);
	}
}
