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
import android.view.ViewTreeObserver;

import com.activeandroid.content.ContentProvider;
import com.fortysevendeg.swipelistview.SwipeListView;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.adapter.AulasCursorAdapter;
import br.com.wakim.autoescola.calendario.app.model.Aula;
import br.com.wakim.autoescola.calendario.app.model.Disciplina;
import br.com.wakim.autoescola.calendario.app.utils.Params;

/**
 * Created by wakim on 17/08/14.
 */
public class FragmentSumarioAulas extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>,
	AulasCursorAdapter.OnSwipeOptionClickListener, FragmentDialogAlert.DialogListener {

	CursorLoader mLoader;
	AulasCursorAdapter mCursorAdapter;

	SwipeListView mList;

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

		mLoader = null;
		mCursorAdapter = null;
		mList = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(savedInstanceState != null) {
			mDisciplina = savedInstanceState.<Disciplina>getParcelable(Params.DISCIPLINA);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_sumario_aulas, null);
		mList = (SwipeListView) view.findViewById(android.R.id.list);

		mCursorAdapter = new AulasCursorAdapter(getActivity(), this);

		setListAdapter(mCursorAdapter);

		preencheDisciplinaSePossivel();

		view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				view.getViewTreeObserver().removeOnPreDrawListener(this);
				setListViewOffsetRight();

				return true;
			}
		});

		View emptyView = inflater.inflate(R.layout.list_empty_aula, null);

		((ViewGroup) view).addView(emptyView);
		mList.setEmptyView(emptyView);

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

	void preencheDisciplinaSePossivel() {
		if(mDisciplina != null && mList != null) {
			if(mLoader == null) {
				getLoaderManager().initLoader(Params.AULAS_LOADER_ID, null, this);
			} else {
				mLoader.setSelectionArgs(new String[] { Long.toString(mDisciplina.getId()) });
			}
		}
	}

	void setListViewOffsetRight() {
		float offset = mList.getWidth();

		// Lista ainda nao foi carrega pelo Loader, entao nao foi medida.
		if(offset == 0f) {
			offset = ((View) mList.getParent()).getWidth();
		}

		// Desconta os dois icones
		offset -= (getResources().getDimensionPixelSize(R.dimen.width_list_icon) * 2);
		// Desconta as margins
		offset -= (getResources().getDimensionPixelSize(R.dimen.margin_list_icon) * 3);
		offset -= getResources().getDimensionPixelSize(R.dimen.swipe_shadow);

		mList.setOffsetRight(offset);
	}

	public void setDisciplina(Disciplina disciplina) {
		mDisciplina = disciplina;
		preencheDisciplinaSePossivel();
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

		if(R.id.lia_delete == optionId) {
			if (mAulaSelecionada.isConcluida()) {
				FragmentDialogAlert alert =
					new FragmentDialogAlert(getActivity(), R.string.excluir_aula_title, R.string.excluir_aula_message, R.string.sim_caps, R.string.nao_caps);

				alert.setShowsDialog(true);
				alert.setDialogListener(this);

				alert.show(getChildFragmentManager(), getString(R.string.alert_dialog_tag));
			} else {
				deleteAula();
			}
		} else if(R.id.lia_edit == optionId) {
			if(mDetalhesCallback != null) {
				mDetalhesCallback.onEditAula(mAulaSelecionada);
			}
		}
	}

	@Override
	public void onCancel() {}

	@Override
	public void onConfirm() {
		deleteAula();
	}

	void deleteAula() {
		mAulaSelecionada.delete();
		mAulaSelecionada = null;

		// TODO Fazer isso assincronamente.

		mDisciplina.saveAndCalculate();

		preencheDisciplinaSePossivel();

		mList.closeAnimate(mPosicaoAulaSelecionada);
	}

	public void setDetalhesDisciplinaCallback(DetalhesDisciplinaCallback detalhesCallback) {
		mDetalhesCallback = detalhesCallback;
	}
}
