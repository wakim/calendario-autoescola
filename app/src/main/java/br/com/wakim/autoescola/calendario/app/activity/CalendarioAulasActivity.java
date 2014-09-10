package br.com.wakim.autoescola.calendario.app.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.antonyt.infiniteviewpager.InfinitePagerAdapter;
import com.antonyt.infiniteviewpager.InfiniteViewPager;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.adapter.CalendarWeekAdapter;
import br.com.wakim.autoescola.calendario.app.fragment.FragmentSumarioAulasIntervalo;
import br.com.wakim.autoescola.calendario.app.model.Aula;
import br.com.wakim.autoescola.calendario.app.model.Disciplina;
import br.com.wakim.autoescola.calendario.app.model.GridMode;
import br.com.wakim.autoescola.calendario.app.utils.CalendarHelper;
import br.com.wakim.autoescola.calendario.app.utils.Params;
import hirondelle.date4j.DateTime;

/**
 * Created by wakim on 28/08/14.
 */
public class CalendarioAulasActivity extends BaseActivity
	implements ActionBar.OnNavigationListener, ViewPager.OnPageChangeListener {

	Disciplina mDisciplina;

	TimeZone mTz = TimeZone.getDefault();

	DateTime mDate, mToday = DateTime.today(mTz);
	GridMode mMode = GridMode.DAY;

	GridModeSpinnerAdapter mSpinnerAdapter;
	CalendarWeekAdapter mWeeksAdapter;
	InfinitePagerAdapter mInfiniteAdapter;

	InfiniteViewPager mViewPager;

	int mVirtualCurrentPage = 0;
	boolean mIsTablet = false;

	Integer mProgressCount = 0;

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mViewPager = null;

		mDisciplina = null;
		mDate = null;
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

			Calendar currentDate = i.hasExtra(Params.CURRENT_DATE) ? (Calendar) i.getSerializableExtra(Params.CURRENT_DATE) : Calendar.getInstance();
			mDisciplina = i.hasExtra(Params.DISCIPLINA) ? i.<Disciplina>getParcelableExtra(Params.DISCIPLINA) : null;

			mDate = CalendarHelper.convertDateToDateTime(currentDate);
		} else {
			Calendar currentDate = savedInstanceState.containsKey(Params.CURRENT_DATE) ? (Calendar) savedInstanceState.getSerializable(Params.CURRENT_DATE) : Calendar.getInstance();

			mDisciplina = savedInstanceState.<Disciplina>getParcelable(Params.DISCIPLINA);
			realCurrentPage = savedInstanceState.getInt(Params.CURRENT_PAGE);
			mDate = CalendarHelper.convertDateToDateTime(currentDate);
		}

		mViewPager = (InfiniteViewPager) findViewById(R.id.aca_viewpager);
		mWeeksAdapter = new CalendarWeekAdapter(getSupportFragmentManager());
		mInfiniteAdapter = new InfinitePagerAdapter(mWeeksAdapter);
		mSpinnerAdapter = new GridModeSpinnerAdapter();

		mViewPager.setAdapter(mInfiniteAdapter);
		mViewPager.setCurrentItem(realCurrentPage);
		mViewPager.setOnPageChangeListener(this);

		mVirtualCurrentPage = mViewPager.getVirtualCurrentItem();

		updateDateElements();

		ActionBar ab = getSupportActionBar();

		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ab.setListNavigationCallbacks(mSpinnerAdapter, this);
	}

	public void updateDateElements() {
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
		}

		mSpinnerAdapter.notifyDataSetChanged();
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

	public void onAulaDismissed(Aula aula) {
		aula.delete();

		mDisciplina = aula.getDisciplina();
		mDisciplina.saveAndCalculate();
	}

	public void onAulaConcluidaToggle(Aula aula) {
		aula.setConcluida(! aula.isConcluida());
		aula.save();

		mDisciplina = aula.getDisciplina();
		mDisciplina.saveAndCalculate();
	}

	void updateMode(GridMode newMode) {
		if(mMode == newMode) {
			return;
		}

		mMode = newMode;

		mSpinnerAdapter.notifyDataSetChanged();
		updateDateElements();
	}

	void goToToday() {
		DateTime today = DateTime.today(mTz);

		mDate = today;
		updateDateElements();
	}

	@Override
	public boolean onNavigationItemSelected(int i, long l) {

		switch(i) {
			case 0:
			case 1:
				updateMode(GridMode.values()[i]);
				break;
			case 2:
				goToToday();
				break;
			case 3:
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
}
