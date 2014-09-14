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
import android.widget.DatePicker;
import android.widget.TextView;

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
			mListener.onConfirm(new DateTime(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth(), 0, 0, 0, 0));
		}

		return true;
	}

	@Override
	public boolean onCancel() {
		if(mListener != null) {
			mListener.onCancel();
		}

		return true;
	}

	@Override
	public int getLayoutResourceId() {
		return R.layout.fragment_date_picker;
	}

	public static interface DialogListener {
		public void onCancel();
		public void onConfirm(DateTime date);
	}
}
