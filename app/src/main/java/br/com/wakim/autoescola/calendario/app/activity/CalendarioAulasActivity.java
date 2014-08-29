package br.com.wakim.autoescola.calendario.app.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.util.LruCache;

import com.activeandroid.content.ContentProvider;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.adapter.DisciplinasSpinnerCursorAdapter;
import br.com.wakim.autoescola.calendario.app.fragment.CustomCalendarFragment;
import br.com.wakim.autoescola.calendario.app.model.Disciplina;
import br.com.wakim.autoescola.calendario.app.utils.Params;
import hirondelle.date4j.DateTime;

/**
 * Created by wakim on 28/08/14.
 */
public class CalendarioAulasActivity extends BaseActivity
	implements LoaderManager.LoaderCallbacks<Cursor>, ActionBar.OnNavigationListener {

	private static final String CALDROID_BUNDLE_KEY = "CALDROID_BUNDLE_KEY";

	DisciplinasSpinnerCursorAdapter mCursorAdapter;
	CustomCalendarFragment mCalendar;
	Disciplina mDisciplina;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_calendario_aulas);
		setTitle(null);

		mCalendar = (CustomCalendarFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.caldroid_fragment_tag));
		mCursorAdapter = new DisciplinasSpinnerCursorAdapter(this);

		ActionBar ab = getSupportActionBar();

		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ab.setListNavigationCallbacks(mCursorAdapter, this);

		CaldroidFragment.selectedBackgroundDrawable = R.color.primary_300;

		if(savedInstanceState == null) {
			Intent i = getIntent();
			Calendar currentDate = i.hasExtra(Params.CURRENT_DATE) ? (Calendar) i.getSerializableExtra(Params.CURRENT_DATE) : Calendar.getInstance();

			mDisciplina = i.hasExtra(Params.DISCIPLINA) ? i.<Disciplina>getParcelableExtra(Params.DISCIPLINA) : null;

			mCalendar = CustomCalendarFragment.newInstance(null, currentDate.get(Calendar.MONTH) + 1, currentDate.get(Calendar.YEAR));

			getSupportFragmentManager()
				.beginTransaction()
				.add(R.id.aca_primary_container, mCalendar, getString(R.string.caldroid_fragment_tag))
				.commit();
		} else {
			mDisciplina = savedInstanceState.<Disciplina>getParcelable(Params.DISCIPLINA);

			if(mCalendar != null) {
				mCalendar.getArguments().putAll(savedInstanceState.getBundle(CALDROID_BUNDLE_KEY));
			}
		}

		getSupportLoaderManager().initLoader(Params.DISCIPLINA_LOADER_ID, null, this);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if(mCalendar != null) {
			mCalendar.saveStatesToKey(outState, CALDROID_BUNDLE_KEY);
		}
	}

	CustomCalendarFragment getCalendarFragment() {
		if(mCalendar == null) {
			mCalendar = (CustomCalendarFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.caldroid_fragment_tag));
		}

		return mCalendar;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		return new CursorLoader(
				this, // Activity context
				ContentProvider.createUri(Disciplina.class, null), // Table to query
				mCursorAdapter.getProjection(), // Projection to return
				null, // No selection clause
				null, // No selection arguments
				Disciplina.NOME + " ASC" // Default sort order
		);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		mCursorAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		mCursorAdapter.swapCursor(null);
	}

	@Override
	public boolean onNavigationItemSelected(int i, long l) {
		// TODO trocar as marcacoes
		return false;
	}
}
