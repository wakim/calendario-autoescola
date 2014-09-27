package br.com.wakim.autoescola.calendario.app.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.activeandroid.content.ContentProvider;
import com.antonyt.infiniteviewpager.InfinitePagerAdapter;
import com.antonyt.infiniteviewpager.InfiniteViewPager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.adapter.CalendarWeekAdapter;
import br.com.wakim.autoescola.calendario.app.adapter.DisciplinasSpinnerCursorAdapter;
import br.com.wakim.autoescola.calendario.app.fragment.FragmentDialogAlert;
import br.com.wakim.autoescola.calendario.app.fragment.FragmentDialogDatePicker;
import br.com.wakim.autoescola.calendario.app.fragment.FragmentSumarioAulasIntervalo;
import br.com.wakim.autoescola.calendario.app.model.Aula;
import br.com.wakim.autoescola.calendario.app.model.DefaultEventImpl;
import br.com.wakim.autoescola.calendario.app.model.Disciplina;
import br.com.wakim.autoescola.calendario.app.model.GridMode;
import br.com.wakim.autoescola.calendario.app.model.task.AbstractOperationAsyncTask;
import br.com.wakim.autoescola.calendario.app.model.task.AulaOperationAsyncTask;
import br.com.wakim.autoescola.calendario.app.model.task.NotObserverCursorLoader;
import br.com.wakim.autoescola.calendario.app.utils.AnimationAdapter;
import br.com.wakim.autoescola.calendario.app.utils.CalendarHelper;
import br.com.wakim.autoescola.calendario.app.utils.Params;
import br.com.wakim.autoescola.calendario.app.utils.SerializablePair;
import hirondelle.date4j.DateTime;

/**
 * Created by wakim on 28/08/14.
 */
public class CalendarioAulasActivity extends BaseActivity
	implements ActionBar.OnNavigationListener, ViewPager.OnPageChangeListener,
	FragmentDialogDatePicker.DialogListener, FragmentSumarioAulasIntervalo.SumarioAulasIntervaloCallback,
	LoaderManager.LoaderCallbacks<Cursor>,FragmentDialogAlert.DialogListener {

	Disciplina mDisciplina;

	TimeZone mTz = TimeZone.getDefault();
	SimpleDateFormat mDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

	DateTime mDate, mToday = DateTime.today(mTz);
	GridMode mMode = GridMode.DAY;

	Spinner mDisciplinaSpinner;
	InfiniteViewPager mViewPager;

	GridModeSpinnerAdapter mSpinnerAdapter;
	DisciplinasSpinnerCursorAdapter mDisciplinaAdapter;
	CalendarWeekAdapter mWeeksAdapter;
	InfinitePagerAdapter mInfiniteAdapter;

	FragmentDialogDatePicker mDatePicker;
	FragmentDialogAlert mDialogReplacement;

	int mVirtualCurrentPage = 0;
	boolean mIsTablet = false,
			mAddMode = false;

	Integer mProgressCount = 0;

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if(mDatePicker != null) {
			mDatePicker.setDialogListener(null);
		}

		if(mDialogReplacement != null) {
			mDialogReplacement.setDialogListener(null);
		}

		mViewPager = null;
		mDisciplinaSpinner = null;

		mDisciplina = null;
		mDate = null;
		mDatePicker = null;

		mSpinnerAdapter = null;
		mDisciplinaAdapter = null;
		mInfiniteAdapter = null;
		mWeeksAdapter = null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mIsTablet = getResources().getBoolean(R.bool.tablet);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_calendario_aulas);
		setTitle(null);

		int realCurrentPage = CalendarWeekAdapter.NUMBER_OF_PAGES / 2;

		if(savedInstanceState == null) {
			Intent i = getIntent();
			Calendar currentDate;

			if(i.hasExtra(Params.CURRENT_DATE)) {
				currentDate = (Calendar) i.getSerializableExtra(Params.CURRENT_DATE);
			} else {
				currentDate = Calendar.getInstance();
				mAddMode = true;
			}

			mDisciplina = i.hasExtra(Params.DISCIPLINA) ? i.<Disciplina>getParcelableExtra(Params.DISCIPLINA) : null;
			mDate = CalendarHelper.convertDateToDateTime(currentDate);
		} else {
			mDate = savedInstanceState.containsKey(Params.CURRENT_DATE) ? (DateTime) savedInstanceState.getSerializable(Params.CURRENT_DATE) : DateTime.now(mTz);

			mDisciplina = savedInstanceState.<Disciplina>getParcelable(Params.DISCIPLINA);
			realCurrentPage = savedInstanceState.getInt(Params.CURRENT_PAGE);

			mDatePicker = (FragmentDialogDatePicker) getSupportFragmentManager().findFragmentByTag(getString(R.string.date_picker_dialog_tag));

			if(mDatePicker != null && mDatePicker.isVisible()) {
				mDatePicker.setDialogListener(this);
			}

			mMode = (GridMode) savedInstanceState.getSerializable(Params.GRID_MODE);

			mAddMode = savedInstanceState.getBoolean(Params.ADD_AULA_MODE, mAddMode);
		}

		mDialogReplacement = (FragmentDialogAlert) getSupportFragmentManager().findFragmentByTag(getString(R.string.alert_dialog_tag));

		if(mDialogReplacement != null && mDialogReplacement.isVisible()) {
			mDialogReplacement.setDialogListener(this);
		}

		mViewPager = (InfiniteViewPager) findViewById(R.id.aca_viewpager);
		mDisciplinaSpinner = (Spinner) findViewById(R.id.aca_disciplinas);

		mWeeksAdapter = new CalendarWeekAdapter(getSupportFragmentManager());
		mInfiniteAdapter = new InfinitePagerAdapter(mWeeksAdapter);
		mSpinnerAdapter = new GridModeSpinnerAdapter();
		mDisciplinaAdapter = new DisciplinasSpinnerCursorAdapter(this);

		mDisciplinaSpinner.setAdapter(mDisciplinaAdapter);

		mViewPager.setAdapter(mInfiniteAdapter);
		mViewPager.setCurrentItem(realCurrentPage);
		mViewPager.setOnPageChangeListener(this);

		mVirtualCurrentPage = mViewPager.getVirtualCurrentItem();

		updateDateElements(savedInstanceState == null);

		ActionBar ab = getSupportActionBar();

		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ab.setListNavigationCallbacks(mSpinnerAdapter, this);

		if(mAddMode) {
			((View) mDisciplinaSpinner.getParent()).setVisibility(View.VISIBLE);
		}

		getSupportLoaderManager().initLoader(Params.DISCIPLINA_LOADER_ID, null, this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable(Params.CURRENT_DATE, mDate);
		outState.putSerializable(Params.GRID_MODE, mMode);
		outState.putParcelable(Params.DISCIPLINA, mDisciplina);

		outState.putInt(Params.CURRENT_PAGE, mViewPager.getCurrentItem());
		outState.putBoolean(Params.ADD_AULA_MODE, mAddMode);
	}

	FragmentSumarioAulasIntervalo getFragment(DateTime date) {
		int count = mWeeksAdapter.getCount();
		int positionMeio = mViewPager.getCurrentItem();
		FragmentSumarioAulasIntervalo fragMeio = mWeeksAdapter.getFragments(positionMeio);
		int realPosition = positionMeio;

		if(mMode == GridMode.WEEK) {
			realPosition += (fragMeio.getBaseDate().getWeekIndex() - date.getWeekIndex());
		} else {
			realPosition += fragMeio.getBaseDate().numDaysFrom(date);
		}

		if(realPosition < 0 || realPosition > count) {
			return null;
		}

		return mWeeksAdapter.getFragments(realPosition);
	}

	public void updateDateElements(boolean firstTime) {
		List<FragmentSumarioAulasIntervalo> fragments = mWeeksAdapter.getFragments(getSupportFragmentManager(), R.id.aca_viewpager);
		int count = fragments.size();
		int currentPage = mViewPager.getCurrentItem();
		int position = ((currentPage - 1) + count) % count;

		DateTime increment = mDate.plusDays(- mMode.getDays());

		for(int i = 0; i < count; ++i, increment = increment.plusDays(mMode.getDays()), position = (position + 1) % count) {
			FragmentSumarioAulasIntervalo frag = fragments.get(position);

			if(frag.getBaseDate() == null || (frag.getBaseDate() != null && ! frag.getBaseDate().equals(increment))) {
				frag.setBaseDate(increment);
			}

			if(frag.getGridMode() == null || (frag.getGridMode() != null && ! frag.getGridMode().equals(mMode))) {
				frag.setGridMode(mMode);
			}

			if(firstTime && mDate.equals(increment)) {
				frag.scrollToDate();
			}

			if(firstTime && mAddMode) {
				frag.enableAulaBatchInsertMode();
			}
		}

		mSpinnerAdapter.notifyDataSetChanged();
	}

	public void updateDateElements() {
		updateDateElements(false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if(id == android.R.id.home) {
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	void updateMode(GridMode newMode) {
		if(mMode == newMode) {
			return;
		}

		mMode = newMode;

		mSpinnerAdapter.notifyDataSetChanged();
		updateDateElements();
	}

	void goToDate(DateTime date) {
		mDate = date;
		updateDateElements();
	}

	void goToToday() {
		goToDate(DateTime.today(mTz));
	}

	void showDatePicker() {
		if(mDatePicker == null) {
			mDatePicker = new FragmentDialogDatePicker();

			mDatePicker.setShowsDialog(true);
			mDatePicker.setCancelable(true);
		}

		mDatePicker.setDialogListener(this);
		mDatePicker.show(getSupportFragmentManager(), getString(R.string.date_picker_dialog_tag));
	}

	@Override
	public boolean onNavigationItemSelected(int i, long l) {
		ActionBar ab = getSupportActionBar();

		switch(i) {
			case 0:
			case 1:
				updateMode(GridMode.values()[i]);
				break;
			case 2:
				goToToday();
				ab.setSelectedNavigationItem(mMode.ordinal());
				break;
			case 3:
				showDatePicker();
				ab.setSelectedNavigationItem(mMode.ordinal());
				break;
		}

		return true;
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

	@Override
	public void onPageSelected(int position) {
		int previousPage = mVirtualCurrentPage;
		mVirtualCurrentPage = position;

		mDate = mDate.plusDays((mVirtualCurrentPage - previousPage) * mMode.getDays());

		updateDateElements();
	}

	@Override
	public void onPageScrollStateChanged(int state) {}

	@Override
	public void onDialogCancel() {}

	@Override
	public void onDialogConfirm() {
		SerializablePair<Long, Long> parameters = (SerializablePair<Long, Long>) mDialogReplacement.getParameter();

		long aulaId = parameters.first;
		DateTime date = DateTime.forInstant(parameters.second, mTz);

		new AulaOperationAsyncTask(aulaId, AbstractOperationAsyncTask.Operation.DELETE).execute();
		onDateClick(date);
	}

	@Override
	public void onDatePickerCancel() {}

	@Override
	public void onDatePickerConfirm(DateTime date) {
		goToDate(date);
	}

	@Override
	public void onEnableAulaBatchInsertMode() {
		List<FragmentSumarioAulasIntervalo> fragments = mWeeksAdapter.getFragments(getSupportFragmentManager(), R.id.aca_viewpager);
		int count = fragments.size();

		for(int i = 0; i < count; ++i) {
			FragmentSumarioAulasIntervalo frag = fragments.get(i);
			frag.enableAulaBatchInsertMode();
		}

		((View) mDisciplinaSpinner.getParent()).setVisibility(View.VISIBLE);
		//animateDisciplinaSpinner(true);
		mAddMode = true;
	}

	@Override
	public void onCancelAulaBatchInsertMode() {
		List<FragmentSumarioAulasIntervalo> fragments = mWeeksAdapter.getFragments(getSupportFragmentManager(), R.id.aca_viewpager);
		int count = fragments.size();

		for(int i = 0; i < count; ++i) {
			FragmentSumarioAulasIntervalo frag = fragments.get(i);
			frag.cancelAulaBatchInsertMode();
		}

		((View) mDisciplinaSpinner.getParent()).setVisibility(View.GONE);
		//animateDisciplinaSpinner(false);
		mAddMode = false;
	}

	@Override
	public void onDateClick(DateTime time) {
		Disciplina disciplina = mDisciplinaAdapter.getDisciplina(mDisciplinaSpinner.getSelectedItemPosition());
		insertAula(disciplina, time);
	}

	@Override
	public void onAulaClick(long aulaId, long disciplinaId, String nomeDisciplina, DateTime date) {
		Date data = new Date(date.getMilliseconds(TimeZone.getDefault()));
		Disciplina disciplina = mDisciplinaAdapter.getDisciplina(mDisciplinaSpinner.getSelectedItemPosition());

		if(disciplina.getId() == disciplinaId) {
			return;
		}

		String dataFormatada = mDateFormat.format(data);
		String message = getString(R.string.substituir_aula_message, disciplina.getNome(), dataFormatada, nomeDisciplina);

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

		mDialogReplacement.setParameter(new SerializablePair<Long, Long>(aulaId, date.getMilliseconds(mTz)));
		mDialogReplacement.setDialogListener(this);
		mDialogReplacement.show(getSupportFragmentManager(), getString(R.string.alert_dialog_tag));
	}

	/***
	 * Todo precisa melhorar para usar!
	 * @param visible
	 */
	void animateDisciplinaSpinner(boolean visible) {
		final View spinnerParent = (View) mDisciplinaSpinner.getParent();

		final float spinnerTop = spinnerParent.getTop();
		final float spinnerHeight = getResources().getDimensionPixelSize(R.dimen.height_tab_action_bar);
		final float viewPagerTop = mViewPager.getTop();

		/*
		if(visible) {
			ObjectAnimator oa = ObjectAnimator.ofFloat(mViewPager, "y", viewPagerTop, viewPagerTop + spinnerHeight);

			oa.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationStart(Animator animation) {

					ObjectAnimator oa2 = ObjectAnimator.ofFloat(spinnerParent, "y", spinnerTop, viewPagerTop);

					oa2.addListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationStart(Animator animation) {
							spinnerParent.setVisibility(View.VISIBLE);
						}
					});

					oa2.start();
				}
			});

			oa.start();
		} else {
			AnimatorSet as = new AnimatorSet();
			ObjectAnimator oa = ObjectAnimator.ofFloat(spinnerParent, "y", spinnerTop, - spinnerHeight);

			as.playSequentially(
					ObjectAnimator.ofFloat(mViewPager, "y", viewPagerTop, viewPagerTop - spinnerHeight),
					oa
			);

			oa.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					spinnerParent.setVisibility(View.GONE);
				}
			});

			as.start();
		}
		*/

		if(visible) {
			TranslateAnimation pagerAnimation = new TranslateAnimation(
				TranslateAnimation.ABSOLUTE, 0,
				TranslateAnimation.ABSOLUTE, 0,
				TranslateAnimation.ABSOLUTE, 0,
				TranslateAnimation.ABSOLUTE, spinnerHeight
			);

			final TranslateAnimation spinnerAnimation = new TranslateAnimation(
					TranslateAnimation.ABSOLUTE, 0,
					TranslateAnimation.ABSOLUTE, 0,
					TranslateAnimation.ABSOLUTE, - spinnerHeight,
					TranslateAnimation.ABSOLUTE, 0
			);

			spinnerAnimation.setDuration(300l);
			spinnerAnimation.setFillAfter(true);

			pagerAnimation.setDuration(300l);
			spinnerAnimation.setFillAfter(true);

			pagerAnimation.setAnimationListener(new AnimationAdapter() {
				@Override
				public void onAnimationEnd(Animation animation) {
					spinnerParent.startAnimation(spinnerAnimation);
					spinnerParent.setVisibility(View.VISIBLE);
				}
			});

			mViewPager.startAnimation(pagerAnimation);
		} else {
			TranslateAnimation spinnerAnimation = new TranslateAnimation(
					TranslateAnimation.ABSOLUTE, 0,
					TranslateAnimation.ABSOLUTE, 0,
					TranslateAnimation.ABSOLUTE, 0,
					TranslateAnimation.ABSOLUTE, - spinnerHeight
			);

			final TranslateAnimation pagerAnimation = new TranslateAnimation(
					TranslateAnimation.ABSOLUTE, 0,
					TranslateAnimation.ABSOLUTE, 0,
					TranslateAnimation.ABSOLUTE, spinnerHeight,
					TranslateAnimation.ABSOLUTE, 0
			);

			spinnerAnimation.setDuration(300l);
			spinnerAnimation.setFillAfter(true);

			pagerAnimation.setDuration(300l);
			pagerAnimation.setFillAfter(true);

			pagerAnimation.setAnimationListener(new AnimationAdapter() {
				@Override
				public void onAnimationEnd(Animation animation) {
					spinnerParent.postDelayed(new Runnable() {
						@Override
						public void run() {
							spinnerParent.setVisibility(View.GONE);
						}
					}, 50);
				}
			});

			spinnerAnimation.setAnimationListener(new AnimationAdapter() {
				@Override
				public void onAnimationEnd(Animation animation) {
					mViewPager.startAnimation(pagerAnimation);
				}
			});

			spinnerParent.startAnimation(spinnerAnimation);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new NotObserverCursorLoader(
				this, // Parent activity context
				ContentProvider.createUri(Disciplina.class, null), // Table to query
				mDisciplinaAdapter.getProjection(), // Projection to return
				null, // No selection clause
				null, // No selection arguments
				Disciplina.NOME + " ASC" // Default sort order
		);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		int position = findDisciplina(data);

		mDisciplinaAdapter.swapCursor(data);
		mDisciplinaSpinner.setSelection(position, true);
	}

	int findDisciplina(Cursor cursor) {
		if(! cursor.moveToFirst()) {
			return -1;
		}

		int i = 0;
		int idIndex = cursor.getColumnIndex(BaseColumns._ID);
		long desiredId = mDisciplina.getId();

		do {
			if(desiredId == cursor.getLong(idIndex)) {
				return i;
			}

			i++;
		} while(cursor.moveToNext());

		cursor.moveToFirst();

		return -1;
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mDisciplinaAdapter.swapCursor(null);
	}

	public class GridModeSpinnerAdapter extends BaseAdapter {

		LayoutInflater mInflater;

		String[] mMeses = getResources().getStringArray(R.array.meses);
		String[] mMesesAbreviados = getResources().getStringArray(R.array.meses_abreviados);
		String[] mDias = getResources().getStringArray(R.array.dias);
		String[] mPeriodos = getResources().getStringArray(R.array.periodo);

		public GridModeSpinnerAdapter() {
			mInflater = getLayoutInflater();
		}

		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public Object getItem(int position) {
			return GridMode.values()[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {

			Holder holder;

			if(convertView == null) {
				convertView = mInflater.inflate(R.layout.list_item_spinner_grid_mode_dropdown, null);

				holder = new Holder();

				holder.text1 = (TextView) convertView.findViewById(android.R.id.text1);
				holder.text2 = (TextView) convertView.findViewById(android.R.id.text2);

				convertView.setTag(R.layout.list_item_spinner_grid_mode_dropdown, holder);
			}

			holder = (Holder) convertView.getTag(R.layout.list_item_spinner_grid_mode_dropdown);

			switch(position) {
				case 0:
					int indexMonth = mDate.getMonth() - 1;
					int day = mDate.getDay();

					holder.text1.setText(R.string.dia);
					holder.text2.setText(mMeses[indexMonth].concat(" ").concat(Integer.toString(day)));
					break;
				case 1:
					DateTime firstDayOfWeek = GridMode.WEEK.getStartDate(mDate);
					DateTime lastDayOfWeek = GridMode.WEEK.getEndDate(mDate);

					int firstIndexOfMonth = firstDayOfWeek.getMonth() - 1;
					int lastIndexOfMonth = lastDayOfWeek.getMonth() - 1;

					int firstDay = firstDayOfWeek.getDay();
					int lastDay = lastDayOfWeek.getDay();

					holder.text1.setText(R.string.semana);
					holder.text2.setText(
							mMesesAbreviados[firstIndexOfMonth].concat(" ").concat(Integer.toString(firstDay))
								.concat(" - ")
								.concat(mMesesAbreviados[lastIndexOfMonth]).concat(" ").concat(Integer.toString(lastDay))
					);
					break;
				case 2:
					holder.text1.setText(R.string.ir_para_hoje);
					holder.text2.setText(null);
					break;
				case 3:
					holder.text1.setText(R.string.escolher_data);
					holder.text2.setText(null);
			}

			return convertView;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			Holder holder;

			if(convertView == null) {
				convertView = mInflater.inflate(R.layout.list_item_spinner_grid_mode, null);

				holder = new Holder();

				holder.text1 = (TextView) convertView.findViewById(android.R.id.text1);
				holder.text2 = (TextView) convertView.findViewById(android.R.id.text2);

				convertView.setTag(R.layout.list_item_spinner_grid_mode, holder);
			}

			holder = (Holder) convertView.getTag(R.layout.list_item_spinner_grid_mode);

			int indexMonth = mDate.getMonth() - 1;

			if(GridMode.DAY == mMode) {
				int diffDays = mToday.getDayOfYear() - mDate.getDayOfYear();
				int weekDayIndex = mDate.getWeekDay() - 1;

				String secondaryText = mDias[weekDayIndex];

				if(-1 <= diffDays && diffDays <= 1) {
					secondaryText = mPeriodos[(mPeriodos.length / 2) - diffDays].concat(", ").concat(secondaryText);
				}

				holder.text1.setText(mMeses[indexMonth].concat(" ").concat(Integer.toString(mDate.getDay())).concat(", ").concat(Integer.toString(mDate.getYear())));
				holder.text2.setText(secondaryText);
			} else {
				DateTime firstDayOfYear = new DateTime(mDate.getYear(), 1, 1, 0, 0, 0, 0);

				holder.text1.setText(mMeses[indexMonth].concat(" ").concat(Integer.toString(mDate.getYear())));
				holder.text2.setText(getString(R.string.semana).concat(" ").concat(Integer.toString(mDate.getWeekIndex(firstDayOfYear))));
			}

			return convertView;
		}

		private class Holder {
			TextView text1, text2;
		}
	}

	@Override
	public void setSupportProgressBarIndeterminateVisibility(boolean visible) {
		synchronized (mProgressCount) {
			if (visible) {

				if(mProgressCount == 0) {
					super.setSupportProgressBarIndeterminateVisibility(true);
				}

				mProgressCount++;
			} else {
				mProgressCount--;

				if(mProgressCount < 0) {
					mProgressCount = 0;
				}

				if (mProgressCount != 0) {
					return;
				}

				super.setSupportProgressBarIndeterminateVisibility(false);
			}
		}
	}

	void insertAula(Disciplina disciplina, DateTime time) {
		Aula aula = new Aula();

		aula.setData(time.getMilliseconds(mTz));
		aula.setDisciplina(disciplina);

		final AulaOperationAsyncTask operation = new AulaOperationAsyncTask(aula, AbstractOperationAsyncTask.Operation.PERSIST);

		operation.setPostOperation(
			new AbstractOperationAsyncTask.OperationRunnable<Aula>() {
				@Override
				public void run(Aula a) {
					insertEvent(a.getDisciplina(), a.getId(), a.getDataAsDateTime());
				}
			}
		);

		operation.execute();
	}

	void insertEvent(Disciplina disciplina, long idAula, DateTime time) {
		DefaultEventImpl event = new DefaultEventImpl(time, disciplina.getSimbolo(), disciplina.getCor(), idAula, disciplina.getId(), disciplina.getNome());
		FragmentSumarioAulasIntervalo fragment = getFragment(time);

		if(fragment == null) {
			return;
		}

		fragment.addEvent(event);
	}
}
