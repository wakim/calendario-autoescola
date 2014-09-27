package br.com.wakim.autoescola.calendario.app.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.Calendar;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.fragment.DetalhesDisciplinaCallback;
import br.com.wakim.autoescola.calendario.app.fragment.FragmentDetalhesDisciplina;
import br.com.wakim.autoescola.calendario.app.fragment.FragmentDialogAlert;
import br.com.wakim.autoescola.calendario.app.fragment.FragmentEditDisciplina;
import br.com.wakim.autoescola.calendario.app.fragment.FragmentSumarioAulas;
import br.com.wakim.autoescola.calendario.app.model.Aula;
import br.com.wakim.autoescola.calendario.app.model.Disciplina;
import br.com.wakim.autoescola.calendario.app.model.task.AbstractOperationAsyncTask;
import br.com.wakim.autoescola.calendario.app.model.task.AulaOperationAsyncTask;
import br.com.wakim.autoescola.calendario.app.utils.ColorHelper;
import br.com.wakim.autoescola.calendario.app.utils.Params;

/**
 * Created by wakim on 14/08/14.
 */
public class DisciplinaActivity extends BaseActivity
	implements FragmentEditDisciplina.DisciplinaCallback, DetalhesDisciplinaCallback, FragmentDialogAlert.DialogListener {

	AulaOperationAsyncTask mAulaOperationAsyncTask;

	FragmentDetalhesDisciplina mDetalhesDisciplina;
	FragmentSumarioAulas mSumarioAulas;
	FragmentDialogAlert mDialog;

	Disciplina mDisciplina;

	Menu mMenu;

	boolean mIsTablet = false;

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if(mDialog != null) {
			mDialog.setDialogListener(null);
		}

		if(mAulaOperationAsyncTask != null) {
			mAulaOperationAsyncTask.setPostOperation(null);
		}

		mDisciplina = null;

		mDetalhesDisciplina = null;
		mSumarioAulas = null;
		mDialog = null;
		mAulaOperationAsyncTask = null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mDisciplina = getIntent().<Disciplina>getParcelableExtra(Params.DISCIPLINA);
		mIsTablet = getResources().getBoolean(R.bool.tablet);

		super.onCreate(savedInstanceState);

		if(! mIsTablet) {
			getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.transparent)));
		} else {
			getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ColorHelper.darkenColor(mDisciplina.getCor())));
		}

		setTitle(null);

		setContentView(R.layout.activity_disciplina);

		getDetalhesDisciplina().setDisciplina(mDisciplina);

		if(! mIsTablet) {
			getDetalhesDisciplina().setHeaderPadding(getResources().getDimensionPixelSize(R.dimen.ab_height));
		}

		View topBackground = findViewById(R.id.ad_top_background);
		View topAdjustment = findViewById(R.id.ad_top_adjustment);

		// Tablet
		if(topBackground != null) {
			topBackground.setBackgroundColor(ColorHelper.darkenColor(mDisciplina.getCor()));
		}

		// Landscape
		if(topAdjustment != null) {
			topAdjustment.setBackgroundColor(mDisciplina.getCor());
		}

		FragmentManager fm = getSupportFragmentManager();

		if(savedInstanceState == null) {
			mSumarioAulas = new FragmentSumarioAulas(mDisciplina);
			fm.beginTransaction().add(R.id.ad_secondary_fragment, mSumarioAulas, getString(R.string.sumario_aulas_tag)).commit();
		} else {
			if(savedInstanceState.getBoolean(Params.PUSHED_TO_TOP, false)) {
				setTitle(mDisciplina.getNome());
			}

			mDialog = (FragmentDialogAlert) fm.findFragmentByTag(getString(R.string.alert_dialog_tag));

			if(mDialog != null && mDialog.isVisible()) {
				mDialog.setDialogListener(this);
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

		return ColorHelper.darkenColor(mDisciplina.getCor());
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
			getSupportFragmentManager().getBackStackEntryCount();
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
	}

	void checkMenu() {
		Fragment frag1 = getFragmentEditDisciplina();

		if(mMenu != null) {
			applyMenuVisibility(frag1 == null || ! frag1.isVisible());
		}
	}

	void applyMenuVisibility(boolean visible) {
		for(int i = 0; i < mMenu.size(); ++i) {
			mMenu.getItem(i).setVisible(visible);
		}
	}

	void showDeleteDialog() {

		if(mDialog == null) {

			mDialog = new FragmentDialogAlert(
					this,
					R.string.excluir_disciplina_title,
					mDisciplina.getTotalAulasConcluidas() > 0 ? R.string.excluir_disciplina_com_presenca_message : R.string.excluir_disciplina_sem_presenca_message,
					R.string.sim_caps,
					R.string.nao_caps
			);

			mDialog.setShowsDialog(true);
			mDialog.setCancelable(true);
		}

		mDialog.setDialogListener(this);

		mDialog.show(getSupportFragmentManager(), getString(R.string.alert_dialog_tag));
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
	public void onNovaAula() {
		goToCalendar(null);
	}

	@Override
	public void onAulaClicked(Aula aula) {
		goToCalendar(aula.getDataAsCalendar());
	}

	@Override
	public void onScroll(int top) {
		// TODO
		FragmentDetalhesDisciplina f = getDetalhesDisciplina();
		final FragmentSumarioAulas a = mSumarioAulas;

		if(f == null || top == 0) {
			return;
		}

		final View v = f.getView();

		if(v == null) {
			return;
		}

		float height = v.getHeight();
		float actualHeight = height - (top >= (height / 2f) ? height / 2 : top);

		v.getLayoutParams().height = (int) actualHeight;

		v.post(new Runnable() {
			@Override
			public void run() {
				v.requestLayout();
				a.getView().requestLayout();
			}
		});
	}

	@Override
	public void onAulaConcluidaToggle(Aula aula) {
		mAulaOperationAsyncTask = new AulaOperationAsyncTask(aula, AulaOperationAsyncTask.Operation.CONCLUIDA_TOGGLE);

		aula.setDisciplina(mDisciplina);

		mAulaOperationAsyncTask.setPostOperation(new AbstractOperationAsyncTask.OperationRunnable<Aula>() {
			@Override
			public void run(Aula a) {
				getDetalhesDisciplina().setDisciplina(a.getDisciplina());
			}
		});

		mAulaOperationAsyncTask.execute();
	}

	@Override
	public void onAulaDeleted(Aula aula) {

		mAulaOperationAsyncTask = new AulaOperationAsyncTask(aula, AulaOperationAsyncTask.Operation.DELETE);

		aula.setDisciplina(mDisciplina);

		mAulaOperationAsyncTask.setPostOperation(new AbstractOperationAsyncTask.OperationRunnable<Aula>() {
			@Override
			public void run(Aula a) {
				getDetalhesDisciplina().setDisciplina(mDisciplina);
			}
		});

		mAulaOperationAsyncTask.execute();
	}

	void goToCalendar(Calendar calendar) {

		Intent intent = new Intent(this, CalendarioAulasActivity.class);

		if(calendar != null) {
			intent.putExtra(Params.CURRENT_DATE, calendar);
		}

		intent.putExtra(Params.DISCIPLINA, mDisciplina);

		startActivity(intent);
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

		if(mIsTablet && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right);
		} else {
			ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_bottom, R.anim.slide_in_bottom, R.anim.slide_out_bottom);
		}

		ft.replace(R.id.ad_secondary_fragment, newFragment, tag);

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

	@Override
	public void onDialogCancel() {}

	@Override
	public void onDialogConfirm() {
		deleteDisciplina();
	}
}
