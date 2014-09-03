package br.com.wakim.autoescola.calendario.app.fragment;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.activeandroid.content.ContentProvider;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.adapter.AulasCursorAdapter;
import br.com.wakim.autoescola.calendario.app.adapter.OnOptionClickListener;
import br.com.wakim.autoescola.calendario.app.model.Aula;
import br.com.wakim.autoescola.calendario.app.model.Disciplina;
import br.com.wakim.autoescola.calendario.app.utils.Params;

/**
 * Created by wakim on 17/08/14.
 */
public class FragmentSumarioAulas extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>,
		OnOptionClickListener, FragmentDialogAlert.DialogListener {

	CursorLoader mLoader;
	AulasCursorAdapter mCursorAdapter;

	Disciplina mDisciplina;
	Aula mAulaSelecionada;
	DetalhesDisciplinaCallback mDetalhesCallback;

	int mPosicaoAulaSelecionada;

	public FragmentSumarioAulas() {}

	public FragmentSumarioAulas(Disciplina disciplina) {
		mDisciplina = disciplina;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mDisciplina = null;
		mAulaSelecionada = null;
		mDetalhesCallback = null;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		mCursorAdapter.destroy();

		mLoader = null;
		mCursorAdapter = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDisciplina = getArguments() != null && getArguments().containsKey(Params.DISCIPLINA) ? getArguments().<Disciplina>getParcelable(Params.DISCIPLINA) : mDisciplina;

		if(savedInstanceState != null) {
			mDisciplina = savedInstanceState.<Disciplina>getParcelable(Params.DISCIPLINA);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_sumario_aulas, null);
		ListView list = (ListView) view.findViewById(android.R.id.list);

		mCursorAdapter = new AulasCursorAdapter(getActivity(), this);

		setListAdapter(mCursorAdapter);

		populateDisciplinaIfPossible();

		View emptyView = inflater.inflate(R.layout.list_empty_aula, null);

		((ViewGroup) view).addView(emptyView);

		list.setEmptyView(emptyView);

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if(activity instanceof DetalhesDisciplinaCallback) {
			mDetalhesCallback = (DetalhesDisciplinaCallback) activity;
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mDetalhesCallback = null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putParcelable(Params.DISCIPLINA, mDisciplina);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if(mDetalhesCallback != null) {
			Aula aula = mCursorAdapter.getAula(position);
			mDetalhesCallback.onAulaClicked(aula);
		}
	}

	void populateDisciplinaIfPossible() {
		if(mDisciplina != null) {
			if(mLoader == null) {
				getLoaderManager().initLoader(Params.AULAS_LOADER_ID, null, this);
			} else {
				mLoader.setSelectionArgs(new String[] { Long.toString(mDisciplina.getId()) });
			}
		}
	}

	public void setDisciplina(Disciplina disciplina) {
		mDisciplina = disciplina;
		populateDisciplinaIfPossible();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		mLoader = new CursorLoader(
				getActivity(), // Parent activity context
				ContentProvider.createUri(Aula.class, null), // Table to query
				mCursorAdapter.getProjection(), // Projection to return
				Aula.DISCIPLINA + " = ?", // No selection clause
				new String[] { Long.toString(mDisciplina.getId()) }, // No selection arguments
				Aula.DATA + " ASC" // Default sort order
		);

		return mLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> objectLoader, Cursor data) {
		if(mCursorAdapter != null) {
			mCursorAdapter.swapCursor(data);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> objectLoader) {
		if(mCursorAdapter != null) {
			mCursorAdapter.swapCursor(null);
		}
	}

	@Override
	public void onOptionClick(int position, @IdRes int optionId) {
		mAulaSelecionada = mCursorAdapter.getAula(position);
		mPosicaoAulaSelecionada = position;

		mAulaSelecionada.setDisciplina(mDisciplina);

		if(R.id.lia_check == optionId) {
			toggleConcludedAula();
		}
	}

	@Override
	public void onCancel() {}

	@Override
	public void onConfirm() {
		deleteAula();
	}

	void deleteAula() {
		if(mDetalhesCallback != null) {
			mDetalhesCallback.onAulaDeleted(mAulaSelecionada);
		}

		mAulaSelecionada = null;
	}

	void toggleConcludedAula() {
		if(mDetalhesCallback != null) {
			mDetalhesCallback.onAulaConcluidaToggle(mAulaSelecionada);
		}
	}

	public void setDetalhesDisciplinaCallback(DetalhesDisciplinaCallback detalhesCallback) {
		mDetalhesCallback = detalhesCallback;
	}
}
