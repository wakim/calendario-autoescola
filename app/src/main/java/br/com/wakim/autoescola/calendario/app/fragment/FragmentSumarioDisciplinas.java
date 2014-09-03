package br.com.wakim.autoescola.calendario.app.fragment;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.activeandroid.content.ContentProvider;
import com.faizmalkani.floatingactionbutton.FloatingActionButton;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.adapter.DisciplinasCursorAdapter;
import br.com.wakim.autoescola.calendario.app.model.Disciplina;
import br.com.wakim.autoescola.calendario.app.utils.Params;

/**
 * Created by wakim on 11/08/14.
 */
public class FragmentSumarioDisciplinas extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

	DisciplinasCursorAdapter mCursorAdapter;

	SumarioDisciplinasCallback mCallback;

	@Override
	public void onDetach() {
		super.onDetach();

		mCallback = null;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		mCursorAdapter.destroy();
		mCursorAdapter = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_sumario_disciplinas, null);
		AbsListView listView = (AbsListView) view.findViewById(android.R.id.list);

		mCursorAdapter = new DisciplinasCursorAdapter(getActivity());
		getLoaderManager().initLoader(Params.DISCIPLINA_LOADER_ID, null, this);

		setListAdapter(mCursorAdapter);

		FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fd_fabbutton);

		fab.setOnClickListener(this);
		fab.listenTo(listView);

		View emptyView = inflater.inflate(R.layout.list_empty_disciplina, null);

		((ViewGroup) view).addView(emptyView);
		listView.setEmptyView(emptyView);

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if(activity instanceof SumarioDisciplinasCallback) {
			mCallback = (SumarioDisciplinasCallback) activity;
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		return new CursorLoader(
				getActivity(), // Parent activity context
				ContentProvider.createUri(Disciplina.class, null), // Table to query
				mCursorAdapter.getProjection(), // Projection to return
				null, // No selection clause
				null, // No selection arguments
				Disciplina.NOME + " ASC" // Default sort order
		);
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
	public void onClick(View v) {
		if(R.id.fd_fabbutton == v.getId()) {
			showDialogDisciplina();
		}
	}

	void showDialogDisciplina() {
		if(mCallback != null) {
			mCallback.onNovaDisciplinaClicked();
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Disciplina disciplina = mCursorAdapter.getDisciplina(position);

		if(mCallback != null) {
			mCallback.onDisciplinaClicked(disciplina);
		}
	}

	public static interface SumarioDisciplinasCallback {
		public void onNovaDisciplinaClicked();
		public void onDisciplinaClicked(Disciplina disciplina);
	}
}
