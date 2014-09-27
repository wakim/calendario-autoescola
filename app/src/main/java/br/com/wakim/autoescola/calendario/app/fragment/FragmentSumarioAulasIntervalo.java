package br.com.wakim.autoescola.calendario.app.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.faizmalkani.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.model.Aula;
import br.com.wakim.autoescola.calendario.app.model.DefaultEventImpl;
import br.com.wakim.autoescola.calendario.app.model.GridMode;
import br.com.wakim.autoescola.calendario.app.model.task.AbstractOperationAsyncTask;
import br.com.wakim.autoescola.calendario.app.model.task.AulaOperationAsyncTask;
import br.com.wakim.autoescola.calendario.app.model.task.AulasAsyncTaskLoader;
import br.com.wakim.autoescola.calendario.app.utils.FontHelper;
import br.com.wakim.autoescola.calendario.app.utils.Params;
import br.com.wakim.autoescola.calendario.app.view.ObservableScrollView;
import br.com.wakim.weekcalendarview.WeekCalendarHeaderView;
import br.com.wakim.weekcalendarview.WeekCalendarView;
import br.com.wakim.weekcalendarview.listener.OnDateClickListener;
import br.com.wakim.weekcalendarview.listener.OnDragListener;
import br.com.wakim.weekcalendarview.model.DropAction;
import br.com.wakim.weekcalendarview.model.Event;
import br.com.wakim.weekcalendarview.utils.XY;
import hirondelle.date4j.DateTime;

/**
 * Created by wakim on 17/08/14.
 */
public class FragmentSumarioAulasIntervalo extends Fragment
	implements LoaderManager.LoaderCallbacks<Map<DateTime, Event>>, OnDragListener,
	OnDateClickListener, FragmentDialogAlert.DialogListener, DetalhesAulaCallback, View.OnClickListener {

	AulaOperationAsyncTask mAulaOperationAsyncTask;

	AulasAsyncTaskLoader mLoader;
	DateTime mBaseDate;

	GridMode mMode;
	boolean mModeChanged = false,
			mScrollToDate = false,
			mBatchInsertMode = false;

	WeekCalendarHeaderView mWeekCalendarHeaderView;
	WeekCalendarView mWeekCalendarView;

	SimpleDateFormat mDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

	// If dialog of replace event is showing and orientation changed, lets store the events
	Event mTargetEvent, mDraggedEvent;
	FragmentDialogAlert mDialogReplacement;
	FragmentDetalhesAula mDetalhesAula;

	SumarioAulasIntervaloCallback mCallback;

	FloatingActionButton mFAB;

	public static FragmentSumarioAulasIntervalo newInstance(DateTime baseDate, GridMode mode, int loaderIncrementId) {
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

		if(mDialogReplacement != null) {
			mDialogReplacement.setDialogListener(null);
		}

		if(mAulaOperationAsyncTask != null) {
			mAulaOperationAsyncTask.setPostOperation(null);
		}

		getLoaderManager().destroyLoader(Params.AULAS_DIA_LOADER_ID);
		mLoader = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(getArguments() != null) {
			mBaseDate = getArguments().containsKey(Params.CURRENT_DATE) ? (DateTime) getArguments().getSerializable(Params.CURRENT_DATE) : mBaseDate;
			mMode = getArguments().containsKey(Params.GRID_MODE) ? (GridMode) getArguments().getSerializable(Params.GRID_MODE) : mMode;
		}

		if(savedInstanceState != null) {
			mBaseDate = (DateTime) savedInstanceState.getSerializable(Params.CURRENT_DATE);
			mMode = (GridMode) savedInstanceState.getSerializable(Params.GRID_MODE);

			mTargetEvent = savedInstanceState.getParcelable(Params.TARGET_EVENT);
			mDraggedEvent = savedInstanceState.getParcelable(Params.DRAGGED_EVENT);

			mBatchInsertMode = savedInstanceState.getBoolean(Params.BATCH_INSERT_MODE, false);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_sumario_aulas_dia, null);

		mWeekCalendarView = (WeekCalendarView) view.findViewById(R.id.fsad_calendar_view);
		mWeekCalendarHeaderView = (WeekCalendarHeaderView) view.findViewById(R.id.fsad_calendar_header_view);

		mWeekCalendarView.setTypeface(FontHelper.loadTypeface(getActivity(), 1));
		mWeekCalendarView.setHeader(mWeekCalendarHeaderView);

		mWeekCalendarView.setOnDragListener(this);
		mWeekCalendarView.setOnDateClickListener(this);

		mWeekCalendarView.setTag(getTag());
		mWeekCalendarHeaderView.setTag(getTag());

		mLoader = (AulasAsyncTaskLoader) getLoaderManager().initLoader(Params.AULAS_DIA_LOADER_ID, null, this);

		((ActionBarActivity) getActivity()).setSupportProgressBarIndeterminateVisibility(true);

		mDialogReplacement = (FragmentDialogAlert) getChildFragmentManager().findFragmentByTag(getString(R.string.alert_dialog_tag));

		if(mDialogReplacement != null && mDialogReplacement.isVisible()) {
			mDialogReplacement.setDialogListener(this);
		}

		view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				getView().getViewTreeObserver().removeOnPreDrawListener(this);
				internalScrollToDate();
				return true;
			}
		});

		mFAB = (FloatingActionButton) view.findViewById(R.id.fsad_fab);

		mFAB.setOnClickListener(this);
		mFAB.listenTo((ObservableScrollView) view.findViewById(R.id.fsad_scrollview));

		updateFAB(false);

		return view;
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);

		populateData();
	}

	public void populateData() {

		if(mWeekCalendarView == null || mWeekCalendarHeaderView == null) {
			return;
		}

		mWeekCalendarHeaderView.setAbbreviateDays(mMode == GridMode.WEEK);
		mWeekCalendarHeaderView.setTwoLineHeader(mMode == GridMode.WEEK);

		mWeekCalendarView.setDates(mBaseDate, mMode.getStartDate(mBaseDate), mMode.getEndDate(mBaseDate));

		internalScrollToDate();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if(activity instanceof SumarioAulasIntervaloCallback) {
			mCallback = (SumarioAulasIntervaloCallback) activity;
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mCallback = null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable(Params.CURRENT_DATE, mBaseDate);
		outState.putSerializable(Params.GRID_MODE, mMode);

		outState.putParcelable(Params.TARGET_EVENT, mTargetEvent);
		outState.putParcelable(Params.DRAGGED_EVENT, mDraggedEvent);
		outState.putBoolean(Params.BATCH_INSERT_MODE, mBatchInsertMode);
	}

	public void setGridMode(GridMode mode) {
		changeMode(mode);
	}

	public void setBaseDate(DateTime baseDate) {

		mBaseDate = baseDate;

		if(getActivity() != null) {
			updateDateInterval();
		}

		populateData();
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
		((ActionBarActivity) getActivity()).setSupportProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onLoaderReset(Loader<Map<DateTime, Event>> objectLoader) {
		mLoader = (AulasAsyncTaskLoader) objectLoader;
		mWeekCalendarView.clearEvents();
	}

	public void changeMode(GridMode mode) {
		if(mMode != mode) {
			mMode = mode;

			if(mLoader != null) {
				mModeChanged = true;
				updateDateInterval();
			}
		}
	}

	void tryChangeMode() {
		if(mModeChanged && mWeekCalendarView != null) {
			populateData();
		}
	}

	@Override
	public boolean onStartDrag(Event event) {
		return true;
	}

	@Override
	public boolean onStopDrag(Event event, DateTime date) {
		new AulaOperationAsyncTask(((DefaultEventImpl) event).getIdAula(), AulaOperationAsyncTask.Operation.UPDATE_DATE)
			.setDate(date.getMilliseconds(TimeZone.getDefault()))
			.execute();

		return true;
	}

	@Override
	public DropAction onStopDrag(Event draggedEvent, Event targetEvent) {
		showDropDialog(draggedEvent, targetEvent, targetEvent.getDate());
		return DropAction.WAIT;
	}

	@Override
	public void onDateClicked(DateTime date) {
		if(mBatchInsertMode) {
			if (mCallback != null) {
				mCallback.onDateClick(date);
			}
		}
	}

	@Override
	public void onDateClicked(DateTime date, Event event) {
		DefaultEventImpl dEvent = (DefaultEventImpl) event;

		if(mBatchInsertMode && mCallback != null) {
			mCallback.onAulaClick(dEvent.getIdAula(), dEvent.getIdDisciplina(), dEvent.getNomeDisciplina(), date);
			return;
		}

		if(mDetalhesAula == null) {
			mDetalhesAula = new FragmentDetalhesAula(getActivity(), dEvent.getIdAula());

			mDetalhesAula.setShowsDialog(true);
			mDetalhesAula.setCancelable(true);
		} else {
			mDetalhesAula.getArguments().putLong(Params.AULA, dEvent.getIdAula());
		}

		mDetalhesAula.setDetalhesAulaCallback(this);
		mDetalhesAula.show(getChildFragmentManager(), getString(R.string.detalhes_aula_tag));
	}

	void showDropDialog(final Event aulaSubstituta, final Event aulaAlvo, DateTime data) {
		Date date = new Date(data.getMilliseconds(TimeZone.getDefault()));
		String dataFormatada = mDateFormat.format(date);
		String nomeAulaSubstituta = ((DefaultEventImpl) aulaSubstituta).getNomeDisciplina();
		String nomeAulaAlvo = ((DefaultEventImpl) aulaAlvo).getNomeDisciplina();
		String message = getString(R.string.substituir_aula_message, nomeAulaSubstituta, dataFormatada, nomeAulaAlvo);

		mDraggedEvent = aulaSubstituta;
		mTargetEvent = aulaAlvo;

		if(mDialogReplacement == null) {
			mDialogReplacement = new FragmentDialogAlert(
					getString(R.string.substituir_aula_title),
					Html.fromHtml(message),
					getString(R.string.sim_caps),
					getString(R.string.nao_caps)
			);

			mDialogReplacement.setShowsDialog(true);
			mDialogReplacement.setCancelable(true);
		} else {
			mDialogReplacement.getArguments().putCharSequence(Params.DIALOG_MESSAGE, Html.fromHtml(message));
		}

		mDialogReplacement.setDialogListener(this);

		mDialogReplacement.show(getChildFragmentManager(), getString(R.string.alert_dialog_tag));
	}

	public DateTime getBaseDate() {
		return mBaseDate;
	}

	public GridMode getGridMode() {
		return mMode;
	}

	@Override
	public void onDialogCancel() {
		mWeekCalendarView.revertEventDrop(mDraggedEvent);

		mDraggedEvent = null;
		mTargetEvent = null;
	}

	@Override
	public void onDialogConfirm() {
		mDraggedEvent.setDate(mTargetEvent.getDate());
		mWeekCalendarView.commitEventDrop(mDraggedEvent);

		new AulaOperationAsyncTask(((DefaultEventImpl) mDraggedEvent).getIdAula(), AulaOperationAsyncTask.Operation.UPDATE_DATE)
			.setDate(mDraggedEvent.getDate().getMilliseconds(TimeZone.getDefault()))
			.execute();

		mAulaOperationAsyncTask = new AulaOperationAsyncTask(((DefaultEventImpl) mTargetEvent).getIdAula(), AulaOperationAsyncTask.Operation.DELETE);

		mAulaOperationAsyncTask.setPostOperation(new AbstractOperationAsyncTask.OperationRunnable<Aula>() {
			@Override
			public void run(Aula aula) {
				mLoader.onContentChanged();
			}
		});

		mAulaOperationAsyncTask.execute();

		mDraggedEvent = null;
		mTargetEvent = null;
	}

	@Override
	public void onAulaConcluidaToggle(Aula aula) {
		mAulaOperationAsyncTask = new AulaOperationAsyncTask(aula, AulaOperationAsyncTask.Operation.CONCLUIDA_TOGGLE);

		mAulaOperationAsyncTask.setPostOperation(new AbstractOperationAsyncTask.OperationRunnable<Aula>() {
			@Override
			public void run(Aula a) {
				if(mDetalhesAula != null) {
					mDetalhesAula.updateAula();
				}
			}
		});

		mAulaOperationAsyncTask.execute();
	}

	@Override
	public void onAulaDeleted(Aula aula) {
		mAulaOperationAsyncTask = new AulaOperationAsyncTask(aula, AulaOperationAsyncTask.Operation.DELETE);

		mAulaOperationAsyncTask.setPostOperation(new AbstractOperationAsyncTask.OperationRunnable<Aula>() {
			@Override
			public void run(Aula a) {
				mLoader.onContentChanged();
			}
		});

		mAulaOperationAsyncTask.execute();
	}

	void internalScrollToDate() {
		if(mScrollToDate && mWeekCalendarView != null) {
			XY scrollPosition;

			if(mBaseDate.getHour() == 0) {
				mScrollToDate = false;
				return;
			}

			if((scrollPosition = mWeekCalendarView.getDatePosition(mBaseDate)).y > 0) {
				((View) mWeekCalendarView.getParent()).scrollTo(0, (int) scrollPosition.y);
				mScrollToDate = false;
			}
		}
	}

	public void scrollToDate() {
		if(mWeekCalendarView != null) {
			internalScrollToDate();
		} else {
			mScrollToDate = true;
		}
	}

	@Override
	public void onClick(View v) {
		if(R.id.fsad_fab == v.getId() && mCallback != null) {
			if(mBatchInsertMode) {
				mCallback.onCancelAulaBatchInsertMode();
			} else {
				mCallback.onEnableAulaBatchInsertMode();
			}
		}
	}

	public void enableAulaBatchInsertMode() {
		mBatchInsertMode = true;
		updateFAB(true);
	}

	public void cancelAulaBatchInsertMode() {
		mBatchInsertMode = false;
		updateFAB(true);
	}

	void updateFAB(boolean animated) {

		if(mFAB == null) {
			return;
		}

		if(! animated) {
			updateFABDrawable();
		} else {
			mFAB.setStick(false);
			mFAB.hide(true, 250l, new Runnable() {
				@Override
				public void run() {
					mFAB.hide(false, 250l, null);
					updateFABDrawable();
				}
			});
		}
	}

	void updateFABDrawable() {
		if(mBatchInsertMode) {
			mFAB.setDrawable(getResources().getDrawable(R.drawable.ic_fab_clear_dark));
			mFAB.setStick(true);
		} else {
			mFAB.setDrawable(getResources().getDrawable(R.drawable.ic_fab_add_dark));
			mFAB.setStick(false);
		}
	}

	public void addEvent(Event event) {
		mWeekCalendarView.addEvent(event);
	}

	public static interface SumarioAulasIntervaloCallback {
		public void onEnableAulaBatchInsertMode();
		public void onCancelAulaBatchInsertMode();
		public void onDateClick(DateTime time);
		public void onAulaClick(long aulaId, long disciplinaId, String nomeDisciplina, DateTime date);
	}
}