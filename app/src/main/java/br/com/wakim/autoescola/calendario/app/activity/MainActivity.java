package br.com.wakim.autoescola.calendario.app.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.fragment.FragmentEditDisciplina;
import br.com.wakim.autoescola.calendario.app.fragment.FragmentSumarioDisciplinas;
import br.com.wakim.autoescola.calendario.app.model.Disciplina;
import br.com.wakim.autoescola.calendario.app.utils.Params;

/**
 * Created by wakim on 11/08/14.
 */
public class MainActivity extends BaseActivity
	implements FragmentSumarioDisciplinas.SumarioDisciplinasCallback,
		FragmentEditDisciplina.DisciplinaCallback {

	FragmentEditDisciplina mEditDisciplina;
	ViewGroup mSecondaryContainer;

	boolean mIsTablet = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mIsTablet = getResources().getBoolean(R.bool.tablet);

		mSecondaryContainer = (ViewGroup) findViewById(R.id.am_secondary_fragment);

		FragmentManager fm = getSupportFragmentManager();

		if(savedInstanceState == null) {
			FragmentSumarioDisciplinas sumarioDisciplinas = new FragmentSumarioDisciplinas();

			fm.beginTransaction().add(R.id.am_main_fragment, sumarioDisciplinas).commit();
		} else {
			if(savedInstanceState.getBoolean(Params.EDITING_DISCIPLINA, false)) {
				mSecondaryContainer.setVisibility(View.VISIBLE);
			}
		}

		fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
			@Override
			public void onBackStackChanged() {
				FragmentEditDisciplina fragment = getEditDisciplina();

				if(mSecondaryContainer != null && (fragment == null || ! fragment.isVisible())) {
					mSecondaryContainer.setVisibility(View.GONE);
				}

				mEditDisciplina = null;
			}
		});

		setTitle(R.string.disciplinas);
		setTitlePadding(getResources().getDimensionPixelSize(R.dimen.padding_left_list_title));
	}

	@Override
	public boolean getDisplayHomeAsUpEnabled() {
		return false;
	}

	@Override
	public void onNovaDisciplinaClicked() {

		if(! mIsTablet) {

			mEditDisciplina = new FragmentEditDisciplina();

			getSupportFragmentManager()
				.beginTransaction()
				.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left)
				.replace(R.id.am_main_fragment, mEditDisciplina, getString(R.string.edit_disciplina_tag))
				.addToBackStack(null)
				.commit();
		} else {

			FragmentEditDisciplina fragment = getEditDisciplina();

			if(fragment == null) {
				mEditDisciplina = new FragmentEditDisciplina();

				mSecondaryContainer.setVisibility(View.VISIBLE);

				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

				if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
					ft.setCustomAnimations(R.anim.slide_in_right, 0, R.anim.slide_out_right, 0);
				} else {
					ft.setCustomAnimations(R.anim.slide_in_bottom, 0, R.anim.slide_out_bottom, 0);
				}

				ft.add(R.id.am_secondary_fragment, mEditDisciplina, getString(R.string.edit_disciplina_tag))
					.addToBackStack(null)
					.commit();
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		Fragment edit = getEditDisciplina();

		outState.putBoolean(Params.EDITING_DISCIPLINA, edit != null && edit.isVisible());
	}

	@Override
	public void onDisciplinaClicked(Disciplina disciplina) {
		Intent detalhes = new Intent(this, DisciplinaActivity.class);
		detalhes.putExtra(Params.DISCIPLINA, (Parcelable) disciplina);

		startActivity(detalhes);
	}

	FragmentEditDisciplina getEditDisciplina() {
		if(mEditDisciplina == null) {
			mEditDisciplina = (FragmentEditDisciplina) getSupportFragmentManager().findFragmentByTag(getString(R.string.edit_disciplina_tag));
		}

		return mEditDisciplina;
	}

	@Override
	public void onEditDisciplinaAccept(String nome, String simbolo, int cor, int limiteAulas) {
		Disciplina d = new Disciplina();

		d.setNome(nome);
		d.setSimbolo(simbolo);
		d.setCor(cor);
		d.setLimite(limiteAulas);

		d.saveAndCalculate();
	}

	@Override
	public void onEditDisciplinaCancel() {
		getSupportFragmentManager().popBackStack();
	}
}
