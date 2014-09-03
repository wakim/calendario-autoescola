package br.com.wakim.autoescola.calendario.app.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Calendar;
import java.util.TimeZone;

import br.com.wakim.autoescola.calendario.R;
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
	implements ActionBar.OnNavigationListener {

	Disciplina mDisciplina;

	TimeZone mTz = TimeZone.getDefault();

	DateTime mDate, mToday = DateTime.today(mTz);
	GridMode mMode = GridMode.DAY;

	GridModeSpinnerAdapter mAdapter;

	FragmentSumarioAulasIntervalo mSumarioDia;

	boolean mIsTablet = false;

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mSumarioDia = null;

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

		if(savedInstanceState == null) {
			Intent i = getIntent();

			Calendar currentDate = i.hasExtra(Params.CURRENT_DATE) ? (Calendar) i.getSerializableExtra(Params.CURRENT_DATE) : Calendar.getInstance();
			mDisciplina = i.hasExtra(Params.DISCIPLINA) ? i.<Disciplina>getParcelableExtra(Params.DISCIPLINA) : null;

			mDate = CalendarHelper.convertDateToDateTime(currentDate);

			mSumarioDia = FragmentSumarioAulasIntervalo.newInstance(mDate, mMode);

			getSupportFragmentManager()
				.beginTransaction()
				.add(R.id.aca_main_fragment, mSumarioDia, getString(R.string.sumario_aulas_dia_tag))
				.commit();
		} else {
			Calendar currentDate = savedInstanceState.containsKey(Params.CURRENT_DATE) ? (Calendar) savedInstanceState.getSerializable(Params.CURRENT_DATE) : Calendar.getInstance();

			mDisciplina = savedInstanceState.<Disciplina>getParcelable(Params.DISCIPLINA);
			mDate = CalendarHelper.convertDateToDateTime(currentDate);
		}

		mAdapter = new GridModeSpinnerAdapter();

		ActionBar ab = getSupportActionBar();

		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ab.setListNavigationCallbacks(mAdapter, this);
	}

	void innerAddSecondaryFragment(Fragment newFragment, String tag) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		if(mIsTablet) {
			if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right);
			} else {
				ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_bottom, R.anim.slide_in_bottom, R.anim.slide_out_bottom);
			}

			ft.replace(R.id.aca_secondary_fragment, newFragment, tag);
		} else {
			ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_bottom, R.anim.slide_in_bottom, R.anim.slide_out_bottom)
			  .replace(R.id.aca_main_fragment, newFragment, tag);
		}

		ft.addToBackStack(null).commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if(android.R.id.home == id) {
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	FragmentSumarioAulasIntervalo getSumarioDia() {
		if(mSumarioDia == null) {
			mSumarioDia = (FragmentSumarioAulasIntervalo) getSupportFragmentManager().findFragmentByTag(getString(R.string.sumario_aulas_dia_tag));
		}

		return mSumarioDia;
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

	@Override
	public boolean onNavigationItemSelected(int i, long l) {
		mMode = GridMode.values()[i];

		FragmentSumarioAulasIntervalo fragment = getSumarioDia();

		if(fragment != null) {
			fragment.changeMode(mMode);
		}

		mAdapter.notifyDataSetChanged();

		return true;
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
			return 2;
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
			if(convertView == null) {
				convertView = mInflater.inflate(R.layout.list_item_spinner_grid_mode_dropdown, null);
			}

			ViewGroup group = (ViewGroup) convertView;

			TextView primary = (TextView) group.getChildAt(0);
			TextView secondary = (TextView) group.getChildAt(1);

			if(position == 0) {
				int indexMonth = mDate.getMonth() - 1;
				int day = mDate.getDay();

				primary.setText(R.string.dia);
				secondary.setText(mMeses[indexMonth].concat(" ").concat(Integer.toString(day)));
			} else {
				DateTime firstDayOfWeek = GridMode.WEEK.getStartDate(mDate);
				DateTime lastDayOfWeek = GridMode.WEEK.getEndDate(mDate);

				int firstIndexOfMonth = firstDayOfWeek.getMonth() - 1;
				int lastIndexOfMonth = lastDayOfWeek.getMonth() - 1;

				int firstDay = firstDayOfWeek.getDay();
				int lastDay = lastDayOfWeek.getDay();

				primary.setText(R.string.semana);
				secondary.setText(
					mMesesAbreviados[firstIndexOfMonth].concat(" ").concat(Integer.toString(firstDay))
					.concat(" - ")
					.concat(mMesesAbreviados[lastIndexOfMonth]).concat(" ").concat(Integer.toString(lastDay))
				);
			}

			return convertView;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null) {
				convertView = mInflater.inflate(R.layout.list_item_spinner_grid_mode, null);
			}

			ViewGroup group = (ViewGroup) convertView;

			TextView secondary = (TextView) group.getChildAt(0);
			TextView primary = (TextView) group.getChildAt(1);

			int indexMonth = mDate.getMonth() - 1;

			if(GridMode.DAY == mMode) {
				int diffDays = mToday.getDayOfYear() - mDate.getDayOfYear();
				int weekDayIndex = mDate.getWeekDay() - 1;

				String secondaryText = mDias[weekDayIndex];

				if(-1 <= diffDays && diffDays <= 1) {
					secondaryText = mPeriodos[(mPeriodos.length / 2) + diffDays].concat(", ").concat(secondaryText);
				}

				primary.setText(mMeses[indexMonth].concat(" ").concat(Integer.toString(mDate.getDay())).concat(", ").concat(Integer.toString(mDate.getYear())));
				secondary.setText(secondaryText);
			} else {
				primary.setText(mMeses[indexMonth].concat(" ").concat(Integer.toString(mDate.getYear())));
				secondary.setText(getString(R.string.semana).concat(" ").concat(Integer.toString(mDate.getWeekIndex(mDate.getStartOfMonth()))));
			}

			return convertView;
		}
	}
}
