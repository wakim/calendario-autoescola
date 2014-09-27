package br.com.wakim.autoescola.calendario.app.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.adapter.OnOptionClickListener;
import br.com.wakim.autoescola.calendario.app.adapter.ProximasAulasAdapter;
import br.com.wakim.autoescola.calendario.app.adapter.TestBaseAdapter;
import br.com.wakim.autoescola.calendario.app.model.Aula;
import br.com.wakim.autoescola.calendario.app.model.task.AbstractOperationAsyncTask;
import br.com.wakim.autoescola.calendario.app.model.task.AulaOperationAsyncTask;
import br.com.wakim.autoescola.calendario.app.model.task.ProximasAulasAsyncTaskLoader;
import br.com.wakim.autoescola.calendario.app.utils.ColorHelper;
import br.com.wakim.autoescola.calendario.app.utils.Params;

/**
 * Created by wakim on 20/09/14.
 */
public class FragmentProximasAulas extends Fragment
	implements LoaderManager.LoaderCallbacks<List<Aula>>,
	View.OnClickListener, OnOptionClickListener {

	ProximasAulasAsyncTaskLoader mLoader;

	SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
	ProximasAulasAdapter mAdapter;
	//TestBaseAdapter mAdapter;
	Aula mProximaAula;

	TextView mProximaAulaData, mProximaAulaNome;
	ToggleButton mProximaAulaCheck;
	View mProximaAulaBackground;
	ViewGroup mProximaAulaContent;

	AulaOperationAsyncTask mDeleteTask;

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		if(mDeleteTask != null) {
			mDeleteTask.setPostOperation(null);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_proximas_aulas, null);

		ListView lv = ((ListView) view.findViewById(R.id.fpa_list));

		lv.setAdapter(new PerguntaAdapter(getActivity(), Arrays.asList(new Pergunta(), new Pergunta(), new Pergunta(), new Pergunta())));
		mAdapter = new ProximasAulasAdapter(getActivity(), this);

		/*
		lv.setAdapter(mAdapter = new ProximasAulasAdapter(getActivity(), this));

		((ActionBarActivity) getActivity()).setSupportProgressBarIndeterminateVisibility(true);
		mLoader = (ProximasAulasAsyncTaskLoader) getLoaderManager().initLoader(Params.AULAS_LOADER_ID, null, this);

		mProximaAulaData = (TextView) view.findViewById(R.id.fpa_proxima_aula_data);
		mProximaAulaNome = (TextView) view.findViewById(R.id.fpa_proxima_aula_nome);
		mProximaAulaBackground = view.findViewById(R.id.fpa_proxima_aula_background);
		mProximaAulaCheck = (ToggleButton) view.findViewById(R.id.fpa_check);
		mProximaAulaContent = (ViewGroup) view.findViewById(R.id.fpa_proxima_aula_content);

		mProximaAulaCheck.setOnClickListener(this);
		view.findViewById(R.id.fpa_delete).setOnClickListener(this);

		*/

		return view;
	}

	void populateFirstAula() {
		mProximaAulaData.setText(mAdapter.getPeriodoFormatado(mDateFormat, mProximaAula.getDataAsCalendar(), 1));
		mProximaAulaNome.setText(mProximaAula.getDisciplina().getNome());

		if(mAdapter.isDelayed(mProximaAula)) {
			mProximaAulaCheck.setBackgroundResource(R.drawable.custom_late_check_toggle);
		} else {
			mProximaAulaCheck.setBackgroundResource(R.drawable.custom_check_toggle);
		}

		mProximaAulaCheck.setChecked(mProximaAula.isConcluida());

		ColorHelper.configureColor(mProximaAulaBackground, mProximaAula.getDisciplina().getCor());

		animateProximaAula(false);
	}

	@Override
	public Loader<List<Aula>> onCreateLoader(int id, Bundle args) {
		return mLoader = new ProximasAulasAsyncTaskLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<List<Aula>> loader, List<Aula> data) {
		ArrayList<Aula> copy = new ArrayList<Aula>(data);

		((ActionBarActivity) getActivity()).setSupportProgressBarIndeterminateVisibility(false);

		mLoader = (ProximasAulasAsyncTaskLoader) loader;

		if(copy != null && ! copy.isEmpty()) {
			mProximaAula = copy.remove(0);
			populateFirstAula();
		}

		if(copy == null || copy.isEmpty()) {
			getView().findViewById(R.id.fpa_header).setVisibility(View.GONE);
		}

		mAdapter.setAulas(copy);
	}

	@Override
	public void onLoaderReset(Loader<List<Aula>> loader) {
		mLoader = (ProximasAulasAsyncTaskLoader) loader;
		mAdapter.clear();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.fpa_check:
				toggleProximaAulaConcluida();
			break;
			case R.id.fpa_delete:
				deleteProximaAula();
			break;
		}
	}

	void animateProximaAulaRemoval() {
		animateProximaAula(true);
	}

	void animateProximaAula(final boolean removal) {
		ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) mProximaAulaContent.getLayoutParams();
		ObjectAnimator oa = ObjectAnimator.ofFloat(mProximaAulaContent, "x", removal ? -mProximaAulaContent.getWidth() : mlp.leftMargin);

		oa.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				if(! removal) {
					mProximaAulaContent.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if(removal) {
					mProximaAulaContent.setVisibility(View.INVISIBLE);
					mLoader.refresh();
				}
			}
		});

		oa.start();
	}

	void toggleProximaAulaConcluida() {
		new AulaOperationAsyncTask(mProximaAula, AbstractOperationAsyncTask.Operation.CONCLUIDA_TOGGLE).execute();
	}

	void deleteProximaAula() {
		mDeleteTask = (AulaOperationAsyncTask) new AulaOperationAsyncTask(mProximaAula, AbstractOperationAsyncTask.Operation.DELETE).setPostOperation(new AbstractOperationAsyncTask.OperationRunnable<Aula>() {
			@Override
			public void run(Aula aula) {
				mProximaAula = null;

				animateProximaAulaRemoval();
				((ActionBarActivity) getActivity()).setSupportProgressBarIndeterminateVisibility(true);
			}
		});

		mDeleteTask.execute();
	}

	@Override
	public void onOptionClick(int position, @IdRes int optionId) {
		Aula aula = (Aula) mAdapter.getItem(position);

		if(optionId == R.id.lipa_check) {
			new AulaOperationAsyncTask(aula, AbstractOperationAsyncTask.Operation.CONCLUIDA_TOGGLE).execute();
		}
	}

	public class Pergunta {

		Boolean mResposta = false;

		public String getPergunta() {
			return "LOREM IPSUM DOLOR ASI AMET";
		}

		public void setResposta(boolean resposta) {
			mResposta = resposta;
		}
	}

	public class PerguntaAdapter extends BaseAdapter implements View.OnClickListener {

		private List<Pergunta> lista;
		private Context context;

		public PerguntaAdapter(Context context, List<Pergunta> lista) {
			this.context = context;
			this.lista = lista;
		}

		@Override
		public int getCount() {
			return lista.size();
		}

		@Override
		public Object getItem(int position) {
			return lista.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public class ViewHolder {
			TextView tvPergunta;
			Button btnSim, btnNao;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			try {
				Pergunta pergunta = lista.get(position);

				if (convertView == null) {
					LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = inflater.inflate(R.layout.modelo_questionario, null);

					viewHolder = new ViewHolder();
					viewHolder.tvPergunta = (TextView) convertView.findViewById(R.id.textPerguntas);
					viewHolder.btnSim = (Button) convertView.findViewById(R.id.btnSim);
					viewHolder.btnNao = (Button) convertView.findViewById(R.id.btnNao);

					viewHolder.btnSim.setOnClickListener(this);
					viewHolder.btnNao.setOnClickListener(this);
				} else {
					viewHolder = (ViewHolder) convertView.getTag();
				}

				convertView.setTag(viewHolder);

				viewHolder.btnSim.setTag(R.layout.modelo_questionario, position);
				viewHolder.btnNao.setTag(R.layout.modelo_questionario, position);

				viewHolder.tvPergunta.setText(pergunta.getPergunta());
			} catch (Exception erro) {
				erro.printStackTrace();
			}

			return convertView;
		}

		public void onClick(View v) {
			// Com essa posicao eh possivel saber qual pergunta ele respondeu

			int position = (Integer) v.getTag(R.layout.modelo_questionario);

			Pergunta pergunta = lista.get(position);

			// Comparo o ID da View que foi clicada com o ID do botao SIM,
			// gerando um booleano

			boolean resposta = v.getId() == R.id.btnSim;

			// Criar um campo na pergunta para armazenar a resposta
			// Ou usar um ArrayList ou SparseArray para armezar.

			pergunta.setResposta(resposta);
		}

		public List<Pergunta> getPerguntas() {
			return lista;
		}
	}
}