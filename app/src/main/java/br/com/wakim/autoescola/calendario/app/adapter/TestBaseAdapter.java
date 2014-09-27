package br.com.wakim.autoescola.calendario.app.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.com.wakim.autoescola.calendario.R;

/**
 * Created by wakim on 21/09/14.
 */
public class TestBaseAdapter extends SimpleAdapter {

	// Armazena a quantidade de clicks em uma determinada linha e sua posicao
	SparseArray<Boolean> mCliques = new SparseArray<Boolean>();

	// Usado para adicionar um valor a uma View, no metodo setTag
	int mResId;

	public TestBaseAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(context, data, resource, from, to);
		mResId = resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);

		updateBackground(view, position);

		// Guardo na View, sua posicao
		// Facilita ao fazer a limpeza manual do background caso seja necessario
		view.setTag(mResId, position);

		return view;
	}

	/***
	 * Atualiza o background da View conforme a regra de cliques
	 * @param view
	 * @param position
	 */
	void updateBackground(View view, int position) {
		if(mCliques.get(position, false)) {
			view.setBackgroundResource(0);
		} else {
			view.setBackgroundResource(R.color.primary);
		}
	}

	/***
	 * Atualiza a ultima posicao clicada e o numero de cliques.
	 * E atualiza o Background da view conforme o resultado
	 * @param adapterView
	 * @param view
	 * @param position
	 */
	public void updateClick(AdapterView adapterView, View view, int position) {
		if(mCliques.get(position, false)) {
			mCliques.remove(position);
		} else {
			mCliques.put(position, true);
		}

		// Podemos usar o "notifyDataSetChanged", com isso todas as Views
		// que estao visiveis serao reconstruidas
		// notifyDataSetChanged();

		// Ou podemos atualizar manualmente as visiveis, as demais
		// serao construídas pelo Adapter
		updateVisibleViews(adapterView);
	}

	void updateVisibleViews(AdapterView adapterView) {
		for(int i = 0, childCount = adapterView.getChildCount(); i < childCount; ++i) {
			View view = adapterView.getChildAt(i);
			int position = (Integer) view.getTag(mResId);

			updateBackground(view, position);
		}
	}

	/***
	 * Retorna a lista dos indices dos itens selecionados
	 * @return
	 */
	public ArrayList<Integer> getSelectedItems() {
		ArrayList<Integer> selecao = new ArrayList<Integer>(mCliques.size());

		for(int i = 0, size = mCliques.size(); i < size; ++i) {
			selecao.add(mCliques.keyAt(i));
		}

		return selecao;
	}
}
