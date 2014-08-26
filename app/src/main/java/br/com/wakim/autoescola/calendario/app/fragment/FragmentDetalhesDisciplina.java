package br.com.wakim.autoescola.calendario.app.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewPropertyAnimator;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.model.Disciplina;
import br.com.wakim.autoescola.calendario.app.utils.Params;

/**
 * Created by wakim on 14/08/14.
 */
public class FragmentDetalhesDisciplina extends Fragment implements View.OnClickListener {

	ViewGroup mHeader, mHeaderDivider;
	TextView mNomeDisciplina, mConcluidas, mRestantes, mSimbolo;
	View mFab, mSpacer;

	int mHeaderMinHeight;
	int mHeaderPadding = 0;

	boolean mPushedToTop = false;

	Disciplina mDisciplina;
	DetalhesDisciplinaCallback mDetalhesCallback;

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		mHeader = mHeaderDivider = null;
		mNomeDisciplina = mConcluidas = mRestantes = mSimbolo = null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mDisciplina = null;
		mDetalhesCallback = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_detalhes_disciplina, null);

		mSpacer = view.findViewById(R.id.fdd_spacer);
		mHeader = (ViewGroup) view.findViewById(R.id.fdd_header);
		mHeaderDivider = (ViewGroup) view.findViewById(R.id.fdd_header_divider);

		mNomeDisciplina = (TextView) view.findViewById(R.id.fdd_nome_disciplina);
		mConcluidas = (TextView) view.findViewById(R.id.fdd_concluidas);
		mRestantes = (TextView) view.findViewById(R.id.fdd_restantes);
		mSimbolo = (TextView) view.findViewById(R.id.fdd_simbolo_disciplina);

		view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				view.getViewTreeObserver().removeOnPreDrawListener(this);
				resize();

				if(savedInstanceState != null && savedInstanceState.getBoolean(Params.PUSHED_TO_TOP, false)) {
					internalPushToTop(null, 0l);
				}

				return true;
			}
		});

		mFab = view.findViewById(R.id.fdd_fab);

		mFab.setOnClickListener(this);

		if(savedInstanceState != null) {
			mDisciplina = savedInstanceState.<Disciplina>getParcelable(Params.DISCIPLINA);
		}

		preencheDisciplinaSePossivel();

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

		outState.putBoolean(Params.PUSHED_TO_TOP, isPushedToTop());
		outState.putParcelable(Params.DISCIPLINA, mDisciplina);
	}

	void setHeaderPadding() {
		if(mHeader != null) {
			int[] position = new int[2];

			mNomeDisciplina.getLocationOnScreen(position);

			if(position[0] == 0) {
				position[0] = mNomeDisciplina.getLeft();
			}

			if(position[1] == 0) {
				position[1] = mNomeDisciplina.getTop();
			}

			if(position[1] < mHeaderPadding) {
				mHeader.setPadding(0, mHeaderPadding - position[1], 0, 0);
				mSpacer.setPadding(0, mHeaderPadding - position[1], 0, 0);
			}
		}
	}

	public void setHeaderPadding(int headerPadding) {
		mHeaderPadding = headerPadding;
		setHeaderPadding();
	}

	void preencheDisciplinaSePossivel() {
		if(mDisciplina != null && mNomeDisciplina != null) {
			if(mDisciplina.getCor() != null) {
				int cor = mDisciplina.getCor();

				mSpacer.setBackgroundColor(cor);
				mHeaderDivider.setBackgroundColor(darkenColor(cor));
			}

			mNomeDisciplina.setText(mDisciplina.getNome());
			mConcluidas.setText(getString(R.string.concluidas, mDisciplina.getTotalAulasConcluidas()));
			mRestantes.setText(getString(R.string.restantes, mDisciplina.getTotalAulasRestantes()));

			if(mDisciplina.getSimbolo() != null) {
				mSimbolo.setText(mDisciplina.getSimbolo());
			} else {
				mSimbolo.setVisibility(View.GONE);
			}
		}
	}

	public int darkenColor(int color) {
		float[] hsv = new float[3];

		Color.colorToHSV(color, hsv);
		hsv[2] *= 0.8f;

		return Color.HSVToColor(hsv);
	}

	public void setDisciplina(Disciplina disciplina) {
		mDisciplina = disciplina;
		preencheDisciplinaSePossivel();
	}

	void resize() {
		float adjustedHeight = getView().getHeight() * 0.3f;
		int actualHeight = mHeader.getHeight();

		mHeaderMinHeight = (int) (adjustedHeight < actualHeight ? actualHeight : adjustedHeight);

		mHeader.setMinimumHeight(mHeaderMinHeight);

		mSpacer.setMinimumHeight(mHeaderMinHeight);
		setSpacerHeight(mHeaderMinHeight);

		setHeaderPadding();
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.fdd_fab && mDetalhesCallback != null) {
			mDetalhesCallback.onNovaAula();
		}
	}

	public void pushToTop(Runnable runnable) {
		internalPushToTop(runnable, 300l);
	}

	void internalPushToTop(final Runnable runnable, long duration) {
		final View view = getView();

		AnimatorSet set = new AnimatorSet();

		set.playTogether(
			ObjectAnimator.ofFloat(mHeader, "y", - mHeader.getHeight()).setDuration(duration),
			ObjectAnimator.ofFloat(mHeaderDivider, "y", mHeaderPadding).setDuration(duration),
			ObjectAnimator.ofFloat(mFab, "y", - mFab.getHeight()).setDuration(duration)
		);

		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);

				mHeader.setVisibility(View.GONE);
				mFab.setVisibility(View.GONE);

				if (runnable != null) {
					view.post(runnable);
				}
			}
		});

		set.start();

		setSpacerHeight(mHeaderPadding);

		mPushedToTop = true;
	}

	public void pushBack(Runnable runnable) {
		internalPushBack(runnable, 300l);
	}

	void internalPushBack(final Runnable runnable, long duration) {
		final View view = getView();

		AnimatorSet set = new AnimatorSet();

		int headerHeight = mHeader.getHeight();

		setSpacerHeight(headerHeight);

		mHeader.setVisibility(View.VISIBLE);
		mFab.setVisibility(View.VISIBLE);

		set.playTogether(
			ObjectAnimator.ofFloat(mHeader, "y", 0).setDuration(duration),
			ObjectAnimator.ofFloat(mHeaderDivider, "y", headerHeight).setDuration(duration),
			ObjectAnimator.ofFloat(mFab, "y", headerHeight - (mFab.getHeight() / 2)).setDuration(duration)
		);

		if (runnable != null) {
			set.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);

					view.post(runnable);
				}
			});
		}

		set.start();

		mPushedToTop = false;
	}

	void setSpacerHeight(int height) {
		ViewGroup.LayoutParams lp = mSpacer.getLayoutParams();

		lp.height = height;

		mSpacer.requestLayout();
	}

	public boolean isPushedToTop() {
		return mPushedToTop;
	}

	public void setDetalhesDisciplinaCallback(DetalhesDisciplinaCallback detalhesCallback) {
		mDetalhesCallback = detalhesCallback;
	}
}
