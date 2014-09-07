package br.com.wakim.autoescola.calendario.app.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.model.AulasAsyncTaskLoader;
import br.com.wakim.autoescola.calendario.app.model.DefaultEventImpl;
import br.com.wakim.autoescola.calendario.app.model.GridMode;
import br.com.wakim.autoescola.calendario.app.utils.CalendarHelper;
import br.com.wakim.autoescola.calendario.app.utils.FontHelper;
import br.com.wakim.autoescola.calendario.app.utils.Params;
import br.com.wakim.weekcalendarview.DropAction;
import br.com.wakim.weekcalendarview.Event;
import br.com.wakim.weekcalendarview.WeekCalendarHeaderView;
import br.com.wakim.weekcalendarview.WeekCalendarView;
import hirondelle.date4j.DateTime;

/**
 * Created by wakim on 17/08/14.
 */
public class FragmentSumarioAulasIntervalo extends Fragment implements LoaderManager.LoaderCallbacks<Map<DateTime, Event>>,
		WeekCalendarView.OnDragListener {

	AulasAsyncTaskLoader mLoader;
	DateTime mBaseDate;

	GridMode mMode;
	boolean mModeChanged = false;

	WeekCalendarHeaderView mWeekCalendarHeaderView;
	WeekCalendarView mWeekCalendarView;

	DateFormat mDateFormat = DateFormat.getDateTimeInstance();

	public static FragmentSumarioAulasIntervalo newInstance(Date baseDate, GridMode mode) {
		return newInstance(CalendarHelper.convertDateToDateTime(baseDate), mode);
	}

	public static FragmentSumarioAulasIntervalo newInstance(DateTime baseDate, GridMode mode) {
		Bundle bundle = new Bundle();
		FragmentSumarioAulasIntervalo fragment = new FragmentSumarioAulasIntervalo();

		bundle.putSerializable(Params.GRID_MODE, mode);
		bundle.putSerializable(Params.CURRENT_DATE, baseDate);

		fragment.setArguments(bundle);

		return fragment;
	}

	public FragmentSumarioAulasIntervalo() {}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		mLoader = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mBaseDate = getArguments().containsKey(Params.CURRENT_DATE) ? (DateTime) getArguments().getSerializable(Params.CURRENT_DATE) : mBaseDate;
		mMode = getArguments().containsKey(Params.GRID_MODE) ? (GridMode) getArguments().getSerializable(Params.GRID_MODE) : mMode;

		if(savedInstanceState != null) {
			mBaseDate = (DateTime) savedInstanceState.getSerializable(Params.CURRENT_DATE);
			mMode = (GridMode) savedInstanceState.getSerializable(Params.GRID_MODE);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_sumario_aulas_dia, null);

		mWeekCalendarView = (WeekCalendarView) view.findViewById(R.id.fsad_calendar_view);
		mWeekCalendarHeaderView = (WeekCalendarHeaderView) view.findViewById(R.id.fsad_calendar_header_view);

		mWeekCalendarHeaderView.setAbbreviateDays(mMode == GridMode.WEEK);
		mWeekCalendarHeaderView.setTwoLineHeader(mMode == GridMode.WEEK);

		mWeekCalendarView.setBaseDate(mBaseDate);
		mWeekCalendarView.setStartDate(mMode.getStartDate(mBaseDate));
		mWeekCalendarView.setEndDate(mMode.getEndDate(mBaseDate));
		mWeekCalendarView.setTypeface(FontHelper.loadTypeface(getActivity(), 1));

		mWeekCalendarView.setHeader(mWeekCalendarHeaderView);

		mWeekCalendarView.setOnDragListener(this);

		getLoaderManager().initLoader(Params.AULAS_DIA_LOADER_ID, null, this);

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable(Params.CURRENT_DATE, mBaseDate);
		outState.putSerializable(Params.GRID_MODE, mMode);
	}

	AulasAsyncTaskLoader updateDateInterval() {
		if(mLoader == null) {
			mLoader = new AulasAsyncTaskLoader(getActivity(), mMode.getStartDate(mBaseDate), mMode.getEndDate(mBaseDate));
		} else {
			mLoader.updateDates(mMode.getStartDate(mBaseDate), mMode.getEndDate(mBaseDate));
		}

		return mLoader;
	}

	@Override
	public Loader<Map<DateTime, Event>> onCreateLoader(int i, Bundle bundle) {
		return updateDateInterval();
	}

	@Override
	public void onLoadFinished(Loader<Map<DateTime, Event>> objectLoader, Map<DateTime, Event> data) {
		mLoader = (AulasAsyncTaskLoader) objectLoader;

		if(mWeekCalendarView != null) {
			mWeekCalendarView.setEvents(data);
		}

		tryChangeMode();
	}

	@Override
	public void onLoaderReset(Loader<Map<DateTime, Event>> objectLoader) {
		mLoader = (AulasAsyncTaskLoader) objectLoader;
		mWeekCalendarView.clearEvents();
	}

	public void changeMode(GridMode mode) {
		if(mMode != mode) {
			mMode = mode;
			mModeChanged = true;

			mLoader.updateDates(mMode.getStartDate(mBaseDate), mMode.getEndDate(mBaseDate));
		}
	}

	void tryChangeMode() {
		if(mModeChanged && mWeekCalendarView != null) {
			mWeekCalendarHeaderView.setTwoLineHeader(mMode == GridMode.WEEK);
			mWeekCalendarHeaderView.setAbbreviateDays(mMode == GridMode.WEEK);

			mWeekCalendarView.setStartDate(mMode.getStartDate(mBaseDate));
			mWeekCalendarView.setEndDate(mMode.getEndDate(mBaseDate));
		}
	}

	@Override
	public boolean onStartDrag(Event event) {
		return true;
	}

	@Override
	public boolean onStopDrag(Event event, DateTime date) {
		return true;
	}

	@Override
	public DropAction onStopDrag(Event draggedEvent, Event targetEvent) {
		showDropDialog(draggedEvent, targetEvent, targetEvent.getDate());
		return DropAction.WAIT;
	}

	void showDropDialog(final Event aulaSubstituta, final Event aulaAlvo, DateTime data) {
		Date date = new Date(data.getMilliseconds(TimeZone.getDefault()));
		String dataFormatada = mDateFormat.format(date);
		String nomeAulaSubstituta = ((DefaultEventImpl) aulaSubstituta).getNomeDisciplina();
		String nomeAulaAlvo = ((DefaultEventImpl) aulaAlvo).getNomeDisciplina();

		String message = getString(R.string.substituir_aula_message, nomeAulaSubstituta, dataFormatada, nomeAulaAlvo);

		FragmentDialogAlert alert = new FragmentDialogAlert(
				getActivity(),
				getString(R.string.substituir_aula_title),
				Html.fromHtml(message),
				getString(R.string.sim_caps),
				getString(R.string.nao_caps)
		);

		alert.setDialogListener(new FragmentDialogAlert.DialogListener() {
			@Override
			public void onCancel() {
				mWeekCalendarView.revertEventDrop(aulaSubstituta);
			}

			@Override
			public void onConfirm() {
				aulaSubstituta.setDate(aulaAlvo.getDate());
				mWeekCalendarView.commitEventDrop(aulaSubstituta);
			}
		});

		alert.setShowsDialog(true);
		alert.setCancelable(true);
		alert.show(getChildFragmentManager(), getString(R.string.alert_dialog_tag));
	}
}