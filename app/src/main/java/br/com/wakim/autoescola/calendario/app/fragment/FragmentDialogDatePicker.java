package br.com.wakim.autoescola.calendario.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import br.com.wakim.autoescola.calendario.R;
import hirondelle.date4j.DateTime;

/**
 * Created by wakim on 10/09/14.
 */
public class FragmentDialogDatePicker extends FragmentMaterialDialog {

	DialogListener mListener;
	DatePicker mDatePicker;

	@Override
	public void onDestroy() {
		super.onDestroy();

		mListener = null;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		mDatePicker = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		mDatePicker = (DatePicker) view.findViewById(R.id.fdp_date_picker);

		return view;
	}

	public void setDialogListener(DialogListener listener) {
		mListener = listener;
	}

	@Override
	public boolean onConfirm() {
		if(mListener != null) {
			mListener.onDatePickerConfirm(new DateTime(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth(), 0, 0, 0, 0));
		}

		return true;
	}

	@Override
	public boolean onCancel() {
		if(mListener != null) {
			mListener.onDatePickerCancel();
		}

		return true;
	}

	@Override
	public int getLayoutResourceId() {
		return R.layout.fragment_date_picker;
	}

	public static interface DialogListener {
		public void onDatePickerCancel();
		public void onDatePickerConfirm(DateTime date);
	}
}
