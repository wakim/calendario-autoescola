package br.com.wakim.autoescola.calendario.app.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidGridAdapter;
import com.roomorama.caldroid.CaldroidListener;

import java.util.Date;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.adapter.CustomCaldroidGridAdapter;
import hirondelle.date4j.DateTime;

/**
 * Created by wakim on 25/08/14.
 */
public class CustomCaldroidFragment extends CaldroidFragment {

	CalendarioCallback mCallback;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		setShowsDialog(false);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if(activity instanceof CalendarioCallback) {
			mCallback = (CalendarioCallback) activity;
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mCallback = null;
	}

	@Override
	public CaldroidGridAdapter getNewDatesGridAdapter(int month, int year) {
		return new CustomCaldroidGridAdapter(getActivity(), month, year, getCaldroidData(), extraData);
	}

	public static CustomCaldroidFragment newInstance(String dialogTitle, int month, int year) {
		CustomCaldroidFragment f = new CustomCaldroidFragment();

		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putString(DIALOG_TITLE, dialogTitle);
		args.putInt(MONTH, month);
		args.putInt(YEAR, year);

		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setCaldroidListener(new CaldroidListener() {
			@Override
			public void onSelectDate(Date date, View view) {
				if(mCallback != null) {
					mCallback.onCalendarioAccept(date);
				}
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		setCaldroidListener(null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.calendario, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if(mCallback == null) {
			return super.onOptionsItemSelected(item);
		}

		if(item.getItemId() == R.id.ed_cancel) {
			mCallback.onCalendarioCancel();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public static interface CalendarioCallback {
		public void onCalendarioAccept(Date date);
		public void onCalendarioCancel();
	}
}
