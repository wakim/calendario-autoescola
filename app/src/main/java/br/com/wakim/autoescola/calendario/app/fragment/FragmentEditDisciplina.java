package br.com.wakim.autoescola.calendario.app.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.model.Disciplina;
import br.com.wakim.autoescola.calendario.app.utils.Params;

/**
 * Created by wakim on 12/08/14.
 */
public class FragmentEditDisciplina extends Fragment implements View.OnClickListener, FragmentDialogColorPicker.DialogListener {

	DisciplinaCallback mCallback;
	Disciplina mDisciplina;

	EditText mName, mSymbol, mLimit;
	View mColor;

	ViewGroup mDataLayout;

	FragmentDialogColorPicker mColorPicker;

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		mName = mSymbol = mLimit = null;
		mColor = null;
		mDataLayout = null;
		mColorPicker = null;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mCallback = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(savedInstanceState != null) {
			mDisciplina = savedInstanceState.<Disciplina>getParcelable(Params.DISCIPLINA);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if(mDisciplina != null) {
			outState.putParcelable(Params.DISCIPLINA, mDisciplina);
		}

		if(isColorLayoutVisible()) {
			outState.putBoolean(Params.COLOR_LAYOUT_VISIBLE, true);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_edit_disciplina, null);

		setHasOptionsMenu(true);

		mDataLayout = (ViewGroup) view.findViewById(R.id.fnd_data_body);

		mColor = mDataLayout.findViewById(R.id.fnd_color);
		mName = (EditText) mDataLayout.findViewById(R.id.fnd_nome);
		mSymbol = (EditText) mDataLayout.findViewById(R.id.fnd_simbolo);
		mLimit = (EditText) mDataLayout.findViewById(R.id.fnd_limite);

		mColor.setOnClickListener(this);

		if(mDisciplina != null) {
			mName.append(mDisciplina.getNome());
			mSymbol.append(mDisciplina.getSimbolo());

			if(mDisciplina.getLimite() != null) {
				mLimit.append(Integer.toString(mDisciplina.getLimite()));
			}

			if(mDisciplina.getCor() != null) {
				configureColor(mDisciplina.getCor());
			} else {
				configureColor(getResources().getColor(R.color.primary));
			}

		} else {
			configureColor(getResources().getColor(R.color.primary));
		}

		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.edit_disciplina, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if(mCallback == null) {
			return super.onOptionsItemSelected(item);
		}

		if(id == R.id.ed_cancel) {
			mCallback.onEditDisciplinaCancel();
			return true;
		} else if(id == R.id.ed_save) {
			gatherData();
		}

		return super.onOptionsItemSelected(item);
	}

	boolean isColorLayoutVisible() {
		return mColorPicker != null && mColorPicker.isVisible();
	}

	void configureColor(int newColor) {

		Drawable drawable = mColor.getBackground();

		if(drawable instanceof GradientDrawable) {
			GradientDrawable gDrawable = (GradientDrawable) drawable;
			gDrawable.setColor(newColor);
		} else if(drawable instanceof StateListDrawable) {
			StateListDrawable listDrawable = (StateListDrawable) drawable;

			DrawableContainer.DrawableContainerState drawableContainerState =
					(DrawableContainer.DrawableContainerState) listDrawable.getConstantState();

			int c = drawableContainerState.getChildCount();
			Drawable[] children = drawableContainerState.getChildren();

			for(int i = 0; i < c; ++i) {
				GradientDrawable child = (GradientDrawable) children[i];
				int[] states = child.getState();

				// Esta invertido e nao sei porque.
				if(states.length == 0) {
					child.setColor(darkenColor(newColor));
				} else {
					child.setColor(newColor);
				}
			}
		}

		mColor.setTag(newColor);
	}

	public int darkenColor(int color) {
		float[] hsv = new float[3];

		Color.colorToHSV(color, hsv);
		hsv[2] *= 0.8f;

		return Color.HSVToColor(hsv);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if(activity instanceof DisciplinaCallback) {
			mCallback = (DisciplinaCallback) activity;
		}
	}

	@Override
	public void onClick(View v) {

		int id = v.getId();

		switch (id) {
			case R.id.fnd_color:
				openColorPickerDialog();
			break;
		}
	}

	boolean gatherData() {
		String name = mName.getText().toString();
		String simbolo = mSymbol.getText().toString();
		String sLimite = mLimit.getText().toString();

		int limite;
		int cor = (Integer) mColor.getTag();

		if(name == null || name.trim().isEmpty()) {
			return false;
		}

		if(sLimite == null || sLimite.trim().isEmpty()) {
			return false;
		}

		try {
			limite = Integer.parseInt(sLimite);
		} catch(NumberFormatException e) {
			return false;
		}

		if(mCallback != null) {
			mCallback.onEditDisciplinaAccept(name, simbolo, cor, limite);
		}

		return true;
	}

	void openColorPickerDialog() {
		mColorPicker = new FragmentDialogColorPicker((Integer) mColor.getTag());

		mColorPicker.setShowsDialog(true);
		mColorPicker.setCancelable(true);
		mColorPicker.setDialogListener(this);

		mColorPicker.show(getChildFragmentManager(), getString(R.string.color_picker_dialog_tag));
	}

	public void setDisciplina(Disciplina disciplina) {
		mDisciplina = disciplina;
	}

	@Override
	public void onCancel() {}

	@Override
	public void onConfirm(int cor) {
		configureColor(cor);
	}

	public static interface DisciplinaCallback {
		public void onEditDisciplinaAccept(String nome, String simbolo, int cor, int limiteAulas);
		public void onEditDisciplinaCancel();
	}
}
