package br.com.wakim.autoescola.calendario.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.model.Aula;
import br.com.wakim.autoescola.calendario.app.model.task.AulaAsyncTaskLoader;
import br.com.wakim.autoescola.calendario.app.utils.Params;

/**
 * Created by wakim on 10/09/14.
 */
public class FragmentDetalhesAula extends FragmentMaterialDialog implements LoaderManager.LoaderCallbacks<Aula> {

	long mAulaId = -1;
	Aula mAula;

	DateFormat mDateFormat = SimpleDateFormat.getDateTimeInstance();
	Calendar mNow = Calendar.getInstance();

	ToggleButton mToggle;

	TextView mNome, mData, mAulasConcluidas, mAulasRestantes;

	DetalhesAulaCallback mCallback;

	AulaAsyncTaskLoader mLoader;

	public FragmentDetalhesAula() {}

	public FragmentDetalhesAula(Context context, long idAula) {
		super(context.getResources().getString(R.string.detalhes_da_aula),
			  context.getString(R.string.fechar_caps),
			  null
		);

		getArguments().putLong(Params.AULA, idAula);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mCallback = null;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		mAula = null;
		mToggle = null;
		mNome = mData = mAulasConcluidas = mAulasRestantes = null;
		mLoader = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAulaId = getArguments().getLong(Params.AULA);

		if(savedInstanceState != null) {
			mAulaId = savedInstanceState.getLong(Params.AULA, mAulaId);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putLong(Params.AULA, mAulaId);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		mNome = (TextView) view.findViewById(R.id.fda_nome);
		mData = (TextView) view.findViewById(R.id.fda_data);
		mAulasConcluidas = (TextView) view.findViewById(R.id.fda_aulas_concluidas);
		mAulasRestantes = (TextView) view.findViewById(R.id.fda_aulas_restantes);

		mToggle = (ToggleButton) view.findViewById(R.id.fda_check);

		view.findViewById(R.id.fda_delete).setOnClickListener(this);

		mLoader = (AulaAsyncTaskLoader) getLoaderManager().initLoader(Params.AULA_LOADER_ID, null, this);

		return view;
	}

	void loadAula() {
		Calendar data = mAula.getDataAsCalendar();

		mNome.setText(mAula.getDisciplina().getNome());
		mData.setText(mDateFormat.format(data.getTime()));

		mAulasConcluidas.setText(Integer.toString(mAula.getDisciplina().getTotalAulasConcluidas()));
		mAulasRestantes.setText(Integer.toString(mAula.getDisciplina().getTotalAulasRestantes()));

		if(mNow.after(data)) {
			mToggle.setBackgroundResource(R.drawable.custom_late_check_toggle);
		} else {
			mToggle.setBackgroundResource(R.drawable.custom_check_toggle);
		}

		mToggle.setChecked(mAula.isConcluida());
		mToggle.setOnClickListener(this);
	}

	public void updateAula() {
		getLoaderManager().restartLoader(Params.AULA_LOADER_ID, null, this);
	}

	@Override
	public void onClick(View v) {

		int itemId = v.getId();

		if(itemId == R.id.fda_check) {
			if(mCallback != null) {
				mCallback.onAulaConcluidaToggle(mAula);

			}
		} else if(itemId == R.id.fda_delete) {
			if(mCallback != null) {
				mCallback.onAulaDeleted(mAula);
			}

			dismiss();
		}

		super.onClick(v);
	}

	@Override
	public boolean onConfirm() {
		return true;
	}

	@Override
	public boolean onCancel() {
		return true;
	}

	@Override
	public int getLayoutResourceId() {
		return R.layout.fragment_detalhes_aula;
	}

	@Override
	public Loader<Aula> onCreateLoader(int id, Bundle args) {
		return mLoader = new AulaAsyncTaskLoader(getActivity(), mAulaId);
	}

	@Override
	public void onLoadFinished(Loader<Aula> loader, Aula data) {
		mLoader = (AulaAsyncTaskLoader) loader;

		mAula = data;
		loadAula();
	}

	@Override
	public void onLoaderReset(Loader<Aula> loader) {
		mAula = null;
	}

	public void setDetalhesAulaCallback(DetalhesAulaCallback callback) {
		mCallback = callback;
	}
}
