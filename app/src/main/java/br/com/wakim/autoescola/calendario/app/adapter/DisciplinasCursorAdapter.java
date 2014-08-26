package br.com.wakim.autoescola.calendario.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.provider.BaseColumns;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.TextView;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.model.Disciplina;

/**
 * Created by wakim on 11/08/14.
 */
public class DisciplinasCursorAdapter extends SimpleCursorAdapter {

	String[] mProjection = {
			BaseColumns._ID,
			Disciplina.NOME,
			Disciplina.COR,
			Disciplina.SIMBOLO,
			Disciplina.LIMITE,
			Disciplina.TOTAL_AULAS_CONCLUIDAS,
			Disciplina.TOTAL_AULAS_RESTANTES
	};

	public DisciplinasCursorAdapter(Context context) {
		super(context, R.layout.list_item_disciplina, null, new String[] {
				BaseColumns._ID,
				Disciplina.NOME,
				Disciplina.COR,
				Disciplina.SIMBOLO,
				Disciplina.LIMITE,
				Disciplina.TOTAL_AULAS_CONCLUIDAS,
				Disciplina.TOTAL_AULAS_RESTANTES
		}, null, 0);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Holder holder = (Holder) view.getTag(R.layout.list_item_disciplina);

		if(holder == null) {
			holder = new Holder();

			holder.symbol = (TextView) view.findViewById(R.id.lid_symbol);
			holder.title = (TextView) view.findViewById(R.id.lid_title);
			holder.concluded = (TextView) view.findViewById(R.id.lid_description_1);
			holder.remaining = (TextView) view.findViewById(R.id.lid_description_2);

			view.setTag(R.layout.list_item_disciplina, holder);
		}

		int nameIndex = cursor.getColumnIndex(Disciplina.NOME),
			symbolIndex = cursor.getColumnIndex(Disciplina.SIMBOLO),
			concludedIndex = cursor.getColumnIndex(Disciplina.TOTAL_AULAS_CONCLUIDAS),
			remainingIndex = cursor.getColumnIndex(Disciplina.TOTAL_AULAS_RESTANTES),
			colorIndex = cursor.getColumnIndex(Disciplina.COR);

		String name = cursor.getString(nameIndex);
		String symbol = cursor.isNull(symbolIndex) ? null : cursor.getString(symbolIndex);
		Integer concluded = cursor.getInt(concludedIndex);
		Integer remaining = cursor.getInt(remainingIndex);
		Integer color = cursor.isNull(colorIndex) ? null : cursor.getInt(colorIndex);

		holder.title.setText(name);
		holder.concluded.setText(context.getString(R.string.concluidas, concluded));
		holder.remaining.setText(context.getString(R.string.restantes, remaining));

		if(symbol == null) {
			holder.symbol.setVisibility(View.GONE);
		} else {
			holder.symbol.setVisibility(View.VISIBLE);
			holder.symbol.setText(symbol);

			if(color != null) {
				changeColor(color, holder.symbol.getBackground());
			} else {
				changeColor(context.getResources().getColor(R.color.black_87p), holder.symbol.getBackground());
			}
		}
	}

	public void changeColor(int newColor, Drawable drawable) {
		if(drawable instanceof GradientDrawable) {
			GradientDrawable gDrawable = (GradientDrawable) drawable;
			gDrawable.setColor(newColor);
		}
	}

	public Disciplina getDisciplina(int position) {
		Disciplina d = new Disciplina();

		Cursor cursor = (Cursor) getItem(position);

		int	idIndex = cursor.getColumnIndex(BaseColumns._ID),
			nameIndex = cursor.getColumnIndex(Disciplina.NOME),
			symbolIndex = cursor.getColumnIndex(Disciplina.SIMBOLO),
			concludedIndex = cursor.getColumnIndex(Disciplina.TOTAL_AULAS_CONCLUIDAS),
			remainingIndex = cursor.getColumnIndex(Disciplina.TOTAL_AULAS_RESTANTES),
			colorIndex = cursor.getColumnIndex(Disciplina.COR),
			limiteIndex = cursor.getColumnIndex(Disciplina.LIMITE);

		Long id = cursor.getLong(idIndex);
		String name = cursor.getString(nameIndex);
		String symbol = cursor.isNull(symbolIndex) ? null : cursor.getString(symbolIndex);
		Integer concluded = cursor.getInt(concludedIndex);
		Integer remaining = cursor.getInt(remainingIndex);
		Integer color = cursor.isNull(colorIndex) ? null : cursor.getInt(colorIndex);
		Integer limite = cursor.getInt(limiteIndex);

		d.setId(id);
		d.setCor(color);
		d.setNome(name);
		d.setSimbolo(symbol);
		d.setLimite(limite);
		d.setTotalAulasConcluidas(concluded);
		d.setTotalAulasRestantes(remaining);

		return d;
	}

	public String[] getProjection() {
		return mProjection;
	}

	public static class Holder {
		TextView symbol,
				 title,
				 concluded,
				 remaining;
	}
}
