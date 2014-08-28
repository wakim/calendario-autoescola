package br.com.wakim.autoescola.calendario.app.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
import java.util.HashMap;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.adapter.CustomCaldroidGridAdapter;
import hirondelle.date4j.DateTime;

/**
 * Created by wakim on 25/08/14.
 */
public class CustomCalendarFragment extends CaldroidFragment {

	CalendarioCallback mCallback;

	private static final String BACKGROUND_RESOURCE_FOR_DATETIMES = "BackgroundResourceForDateTimes";
	private static final String TEXTCOLOR_RESOURCE_FOR_DATETIMES = "TextColorForDateTimes";

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

	public static CustomCalendarFragment newInstance(String dialogTitle, int month, int year) {
		CustomCalendarFragment f = new CustomCalendarFragment();

		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putString(DIALOG_TITLE, dialogTitle);
		args.putInt(MONTH, month);
		args.putInt(YEAR, year);

		f.setArguments(args);

		return f;
	}

	@Override
	public Bundle getSavedStates() {
		Bundle savedState = super.getSavedStates();

		savedState.putSerializable(BACKGROUND_RESOURCE_FOR_DATETIMES, this.backgroundForDateTimeMap);
		savedState.putSerializable("TextColorForDateTimes", this.textColorForDateTimeMap);

		return savedState;
	}

	@Override
	protected void retrieveInitialArgs() {
		super.retrieveInitialArgs();

		Bundle arguments = getArguments();

		if(arguments == null) {
			return;
		}

		if(arguments.containsKey(BACKGROUND_RESOURCE_FOR_DATETIMES)) {
			this.backgroundForDateTimeMap.putAll((HashMap<DateTime, Integer>) arguments.getSerializable(BACKGROUND_RESOURCE_FOR_DATETIMES));
		}

		if(arguments.containsKey(TEXTCOLOR_RESOURCE_FOR_DATETIMES)) {
			this.textColorForDateTimeMap.putAll((HashMap<DateTime, Integer>) arguments.getSerializable(TEXTCOLOR_RESOURCE_FOR_DATETIMES));
		}
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
