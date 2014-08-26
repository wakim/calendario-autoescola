package br.com.wakim.autoescola.calendario.app.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.roomorama.caldroid.CaldroidFragment;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.fragment.CustomCaldroidFragment;
import br.com.wakim.autoescola.calendario.app.fragment.DetalhesDisciplinaCallback;
import br.com.wakim.autoescola.calendario.app.fragment.FragmentDialogAlert;
import br.com.wakim.autoescola.calendario.app.fragment.FragmentEditDisciplina;
import br.com.wakim.autoescola.calendario.app.fragment.FragmentDetalhesDisciplina;
import br.com.wakim.autoescola.calendario.app.fragment.FragmentSumarioAulas;
import br.com.wakim.autoescola.calendario.app.model.Aula;
import br.com.wakim.autoescola.calendario.app.model.Disciplina;
import br.com.wakim.autoescola.calendario.app.utils.Params;
import hirondelle.date4j.DateTime;

/**
 * Created by wakim on 14/08/14.
 */
public class DisciplinaActivity extends BaseActivity
	implements FragmentEditDisciplina.DisciplinaCallback, DetalhesDisciplinaCallback, CustomCaldroidFragment.CalendarioCallback {

	private static final String CALDROID_BUNDLE_KEY = "CALDROID_BUNDLE_KEY";

	FragmentDetalhesDisciplina mDetalhesDisciplina;
	FragmentSumarioAulas mSumarioAulas;
	CustomCaldroidFragment mCaldroid;

	Disciplina mDisciplina;

	Menu mMenu;

	View mTopBackground;
	boolean mIsTablet = false;

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mDisciplina = null;

		mDetalhesDisciplina = null;
		mSumarioAulas = null;
		mTopBackground = null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mDisciplina = getIntent().<Disciplina>getParcelableExtra(Params.DISCIPLINA);
		mIsTablet = getResources().getBoolean(R.bool.tablet);

		super.onCreate(savedInstanceState);

		if(! mIsTablet) {
			getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.transparent)));
		} else {
			getSupportActionBar().setBackgroundDrawable(new ColorDrawable(darkenColor(mDisciplina.getCor())));
		}

		setTitle(null);

		setContentView(R.layout.activity_disciplina);

		mTopBackground = findViewById(R.id.ad_top_background);

		getDetalhesDisciplina().setDisciplina(mDisciplina);

		if(! mIsTablet) {
			getDetalhesDisciplina().setHeaderPadding(getResources().getDimensionPixelSize(R.dimen.ab_height));
		}

		if(mTopBackground != null) {
			mTopBackground.setBackgroundColor(darkenColor(mDisciplina.getCor()));
		}

		FragmentManager fm = getSupportFragmentManager();

		if(savedInstanceState == null) {
			mSumarioAulas = new FragmentSumarioAulas(mDisciplina);

			fm.beginTransaction().add(R.id.ad_container, mSumarioAulas, getString(R.string.sumario_aulas_tag)).commit();
		} else {
			if(savedInstanceState.getBoolean(Params.PUSHED_TO_TOP, false)) {
				setTitle(mDisciplina.getNome());
			}

			mCaldroid = (CustomCaldroidFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.caldroid_fragment_tag));

			if(mCaldroid != null) {
				mCaldroid.getArguments().putAll(savedInstanceState.getBundle(CALDROID_BUNDLE_KEY));
			}
		}

		fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
			@Override
			public void onBackStackChanged() {
				mDetalhesDisciplina = null;
				mSumarioAulas = null;

				checkMenu();
			}
		});
	}

	public int darkenColor(int color) {
		float[] hsv = new float[3];

		Color.colorToHSV(color, hsv);
		hsv[2] *= 0.8f;

		return Color.HSVToColor(hsv);
	}

	public int lightenColor(int color) {

		int alpha = color >>> 24;
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;

		alpha *= 0.6f;

		return (alpha << 24) | (r << 16) | (g << 8) | b;
	}

	@Override
	protected void configureStatusBarImmersive() {
		if(! mIsTablet) {
			if (Build.VERSION.SDK_INT >= 19) {
				View root = findViewById(android.R.id.content);

				int offset = getInternalDimensionSize(getResources(), "status_bar_height");

				root.setPadding(0, offset, 0, 0);
			}
		} else {
			super.configureStatusBarImmersive();
		}
	}

	@Override
	public void preAddContent() {
		if(! mIsTablet) {
			requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		}
	}

	@Override
	int getStatusBarTintColor() {
		if (mDisciplina.getCor() == null) {
			return super.getStatusBarTintColor();
		}

		return mIsTablet ? darkenColor(mDisciplina.getCor()) : mDisciplina.getCor();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.detalhes_disciplina, menu);

		mMenu = menu;

		checkMenu();

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()) {
			case R.id.dd_edit:
				toggleDetailsDisciplinaFragment();
				return true;
			case R.id.dd_delete:
				showDeleteDialog();
				return true;
			case android.R.id.home:
				finish();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if(getDetalhesDisciplina().isPushedToTop()) {
			pushBackAndPopBackStack();
			return;
		}

		super.onBackPressed();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if(getDetalhesDisciplina().isPushedToTop()) {
			outState.putBoolean(Params.PUSHED_TO_TOP, true);
		}

		if(mCaldroid != null) {
			mCaldroid.saveStatesToKey(outState, CALDROID_BUNDLE_KEY);
		}
	}

	void checkMenu() {
		Fragment frag1 = getFragmentEditDisciplina();
		Fragment frag2 = getFragmentCalendario();

		if(mMenu != null) {
			applyMenuVisibility((frag1 == null || ! frag1.isVisible()) && (frag2 == null || ! frag2.isVisible()));
		}
	}

	void applyMenuVisibility(boolean visible) {
		for(int i = 0; i < mMenu.size(); ++i) {
			mMenu.getItem(i).setVisible(visible);
		}
	}

	void showDeleteDialog() {
		FragmentDialogAlert alert = new FragmentDialogAlert(
			this,
			R.string.excluir_disciplina_title,
			mDisciplina.getTotalAulasConcluidas() > 0 ? R.string.excluir_disciplina_com_presenca_message : R.string.excluir_disciplina_sem_presenca_message,
			R.string.sim_caps,
			R.string.nao_caps
		);

		alert.setDialogListener(new FragmentDialogAlert.DialogListener() {
			@Override
			public void onCancel() {}

			@Override
			public void onConfirm() {
				deleteDisciplina();
			}
		});

		alert.setShowsDialog(true);
		alert.setCancelable(true);
		alert.show(getSupportFragmentManager(), getString(R.string.alert_dialog_tag));
	}

	void deleteDisciplina() {
		mDisciplina.delete();
		finish();
	}

	void toggleDetailsDisciplinaFragment() {
		FragmentDetalhesDisciplina fragment = getDetalhesDisciplina();

		if(fragment.isPushedToTop()) {
			pushBackAndPopBackStack();
		} else {
			addDetailsFragment();
		}
	}

	void addDetailsFragment() {
		FragmentEditDisciplina edicao = new FragmentEditDisciplina();

		edicao.setDisciplina(mDisciplina);

		addSecondaryFragment(edicao, getString(R.string.edit_disciplina_tag), true);
	}

	void popBackStack() {
		if(! mIsTablet) {
			setTitle(null);
		}

		getSupportFragmentManager().popBackStack();
	}

	FragmentDetalhesDisciplina getDetalhesDisciplina() {
		if(mDetalhesDisciplina == null) {
			mDetalhesDisciplina = (FragmentDetalhesDisciplina) getSupportFragmentManager().findFragmentByTag(getString(R.string.detalhes_disciplina_tag));
		}

		return mDetalhesDisciplina;
	}

	FragmentEditDisciplina getFragmentEditDisciplina() {
		return (FragmentEditDisciplina) getSupportFragmentManager().findFragmentByTag(getString(R.string.edit_disciplina_tag));
	}

	CustomCaldroidFragment getFragmentCalendario() {
		if(mCaldroid == null) {
			mCaldroid = (CustomCaldroidFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.caldroid_fragment_tag));
		}

		return mCaldroid;
	}

	@Override
	public void onEditDisciplinaAccept(String nome, String simbolo, int cor, int limiteAulas) {
		Integer previousColor = mDisciplina.getCor();

		mDisciplina.setNome(nome);
		mDisciplina.setSimbolo(simbolo);
		mDisciplina.setCor(cor);
		mDisciplina.setLimite(limiteAulas);

		mDisciplina.saveAndCalculate();
		getDetalhesDisciplina().setDisciplina(mDisciplina);

		if(previousColor != cor) {
			configureStatusBarTint(cor);
		}

		pushBackAndPopBackStack();
	}

	@Override
	public void onEditDisciplinaCancel() {
		pushBackAndPopBackStack();
	}

	@Override
	public void onCalendarioAccept(Date date) {
		// TODO
	}

	@Override
	public void onCalendarioCancel() {
		pushBackAndPopBackStack();
	}

	@Override
	public void onNovaAula() {
		showCalendarPicker(null);
	}

	@Override
	public void onEditAula(Aula aula) {
		showCalendarPicker(aula);
	}

	void showCalendarPicker(final Aula aula) {
		new AsyncTask<Void, Void, HashMap<DateTime, Integer>>() {

			@Override
			protected HashMap<DateTime, Integer> doInBackground(Void... params) {
				return getHighlightedDates();
			}

			@Override
			protected void onPostExecute(HashMap<DateTime, Integer> dates) {
				super.onPostExecute(dates);

				addCalendarPickerFragment(dates, aula == null ? null : aula.getDataAsCalendar());
			}
		}.execute();
	}

	HashMap<DateTime, Integer> getHighlightedDates() {
		HashMap<DateTime, Integer> highlightedDates = new HashMap<DateTime, Integer>();
		int cor = lightenColor(mDisciplina.getCor());

		for(Aula aula : mDisciplina.getAulas()) {
			Calendar data = aula.getDataAsCalendar();
			highlightedDates.put(new DateTime(data.get(Calendar.YEAR), data.get(Calendar.MONTH) + 1, data.get(Calendar.DAY_OF_MONTH), 0, 0, 0, 0), cor);
		}

		return highlightedDates;
	}

	void addCalendarPickerFragment(HashMap<DateTime, Integer> highlightedDates, Calendar calendar) {

		if(calendar == null) {
			calendar = Calendar.getInstance();
		}

		mCaldroid = CustomCaldroidFragment.newInstance(null, calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));

		mCaldroid.setBackgroundResourceForDateTimes(highlightedDates);

		addSecondaryFragment(mCaldroid, getString(R.string.caldroid_fragment_tag), true);
	}

	void addSecondaryFragment(final Fragment newFragment, final String tag, boolean pushingToTop) {
		FragmentDetalhesDisciplina fragment = getDetalhesDisciplina();

		if(! mIsTablet && pushingToTop) {
			fragment.pushToTop(new Runnable() {
				@Override
				public void run() {
					setTitle(mDisciplina.getNome());
					innerAddSecondaryFragment(newFragment, tag);
				}
			});
		} else {
			innerAddSecondaryFragment(newFragment, tag);
		}
	}

	void innerAddSecondaryFragment(Fragment newFragment, String tag) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		if(mIsTablet) {
			ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right)
			  .replace(R.id.ad_secondary_container, newFragment, tag);
		} else {
			ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_bottom, R.anim.slide_in_bottom, R.anim.slide_out_bottom)
			  .replace(R.id.ad_container, newFragment, tag);
		}

		ft.addToBackStack(null).commit();
	}

	void pushBackAndPopBackStack() {
		if(! mIsTablet) {
			getDetalhesDisciplina().pushBack(new Runnable() {
				@Override
				public void run() {
					popBackStack();
				}
			});
		} else {
			popBackStack();
		}
	}
}
