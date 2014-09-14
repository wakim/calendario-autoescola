package br.com.wakim.autoescola.calendario.app.fragment;

import android.app.Activity;
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

import com.mrengineer13.snackbar.SnackBar;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.model.Disciplina;
import br.com.wakim.autoescola.calendario.app.utils.ColorHelper;
import br.com.wakim.autoescola.calendario.app.utils.Params;
import br.com.wakim.autoescola.calendario.app.view.RobotoTextView;

/**
 * Created by wakim on 12/08/14.
 */
public class FragmentEditDisciplina extends Fragment implements View.OnClickListener, FragmentDialogColorPicker.DialogListener {

	DisciplinaCallback mCallback;
	Disciplina mDisciplina;

	Integer mCor;

	EditText mName, mSymbol, mLimit;
	RobotoTextView mColor;

	ViewGroup mDataLayout;

	SnackBar mSnackBar;

	FragmentDialogColorPicker mColorPicker;

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		mSnackBar.destroy();

		if(mColorPicker != null) {
			mColorPicker.setDialogListener(null);
		}

		mName = mSymbol = mLimit = null;
		mColor = null;
		mDataLayout = null;
		mColorPicker = null;
		mSnackBar = null;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mCallback = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDisciplina = getArguments() != null && getArguments().containsKey(Params.DISCIPLINA) ? getArguments().<Disciplina>getParcelable(Params.DISCIPLINA) : mDisciplina;

		if(savedInstanceState != null) {
			mDisciplina = savedInstanceState.<Disciplina>getParcelable(Params.DISCIPLINA);

			mCor = savedInstanceState.getInt(Params.COLOR);
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

		outState.putInt(Params.COLOR, mCor);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_edit_disciplina, null);

		setHasOptionsMenu(true);

		mDataLayout = (ViewGroup) view.findViewById(R.id.fnd_data_body);

		mColor = (RobotoTextView) mDataLayout.findViewById(R.id.fnd_color);
		mName = (EditText) mDataLayout.findViewById(R.id.fnd_nome);
		mSymbol = (EditText) mDataLayout.findViewById(R.id.fnd_simbolo);
		mLimit = (EditText) mDataLayout.findViewById(R.id.fnd_limite);

		mColor.setOnClickListener(this);

		if(mCor == null) {
			if(mDisciplina != null) {
				mCor = mDisciplina.getCor();
			} else {
				mCor = getResources().getColor(R.color.primary);
			}
		}

		mColorPicker = (FragmentDialogColorPicker) getChildFragmentManager().findFragmentByTag(getString(R.string.alert_dialog_tag));

		if(mColorPicker != null && mColorPicker.isVisible()) {
			mColorPicker.setDialogListener(this);
		}

		if(mDisciplina != null) {
			mName.append(mDisciplina.getNome());
			mSymbol.append(mDisciplina.getSimbolo());

			if (mDisciplina.getLimite() != null) {
				mLimit.append(Integer.toString(mDisciplina.getLimite()));
			}
		}

		ColorHelper.configureColor(mColor, mCor);

		mSnackBar = new SnackBar(getActivity());

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
			Integer message = gatherData();

			if(message != null) {
				mSnackBar.show(getString(message), SnackBar.MED_SNACK);
			}
		}

		return super.onOptionsItemSelected(item);
	}

	boolean isColorLayoutVisible() {
		return mColorPicker != null && mColorPicker.isVisible();
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

	Integer gatherData() {
		String name = mName.getText().toString();
		String simbolo = mSymbol.getText().toString();
		String sLimite = mLimit.getText().toString();

		int limite;

		if(name == null || name.trim().isEmpty()) {
			return R.string.nome_vazio;
		}

		if(sLimite == null || sLimite.trim().isEmpty()) {
			return R.string.limite_vazio;
		}

		try {
			limite = Integer.parseInt(sLimite);
		} catch(NumberFormatException e) {
			return R.string.limite_vazio;
		}

		if(mCallback != null) {
			mCallback.onEditDisciplinaAccept(name, simbolo, mCor, limite);
		}

		return null;
	}

	void openColorPickerDialog() {
		if(mColorPicker == null) {
			mColorPicker = new FragmentDialogColorPicker(mCor);

			mColorPicker.setShowsDialog(true);
			mColorPicker.setCancelable(true);
		}

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
		mCor = cor;
		ColorHelper.configureColor(mColor, mCor);
	}

	public static interface DisciplinaCallback {
		public void onEditDisciplinaAccept(String nome, String simbolo, int cor, int limiteAulas);
		public void onEditDisciplinaCancel();
	}
}
