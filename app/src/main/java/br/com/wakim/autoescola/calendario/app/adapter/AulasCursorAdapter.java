package br.com.wakim.autoescola.calendario.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.media.Image;
import android.provider.BaseColumns;
import android.support.annotation.IdRes;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.model.Aula;
import br.com.wakim.autoescola.calendario.app.model.Disciplina;

/**
 * Created by wakim on 15/08/14.
 */
public class AulasCursorAdapter extends SimpleCursorAdapter {

	DateFormat mDateFormat;
	Calendar mNow = Calendar.getInstance();

	String[] mProjection = {
		BaseColumns._ID,
		Aula.DATA,
		Aula.CONCLUIDA,
		Aula.DISCIPLINA
	};

	OnSwipeOptionClickListener mOptionClickListener;
	View.OnClickListener mClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Integer position = (Integer) v.getTag(R.layout.list_item_aula);

			if(position != null && mOptionClickListener != null) {
				mOptionClickListener.onOptionClick(position, v.getId());
			}
		}
	};

	public AulasCursorAdapter(Context context, OnSwipeOptionClickListener optionClickListener) {
		super(context, R.layout.list_item_aula, null,
			new String[] {
					BaseColumns._ID,
					Aula.DATA,
					Aula.CONCLUIDA,
					Aula.DISCIPLINA
			}, null, 0);

		mDateFormat = DateFormat.getDateTimeInstance();
		mOptionClickListener = optionClickListener;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Holder holder = (Holder) view.getTag(R.layout.list_item_aula);

		if(holder == null) {
			holder = new Holder();

			holder.data = (TextView) view.findViewById(R.id.lia_data);
			holder.check = (ImageView) view.findViewById(R.id.lia_check);
			holder.edit = (ImageView) view.findViewById(R.id.lia_edit);
			holder.delete = (ImageView) view.findViewById(R.id.lia_delete);

			holder.edit.setOnClickListener(mClickListener);
			holder.delete.setOnClickListener(mClickListener);

			view.setTag(R.layout.list_item_disciplina, holder);
		}

		int dataIndex = cursor.getColumnIndex(Aula.DATA),
			concluidaIndex = cursor.getColumnIndex(Aula.CONCLUIDA);

		Long data = cursor.getLong(dataIndex);
		Boolean concluida = cursor.isNull(concluidaIndex) ? null : cursor.getInt(concluidaIndex) > 0;
		Calendar cal = Calendar.getInstance();

		cal.setTimeInMillis(data);

		holder.data.setText(mDateFormat.format(cal.getTime()));
		holder.edit.setTag(R.layout.list_item_aula, cursor.getPosition());
		holder.delete.setTag(R.layout.list_item_aula, cursor.getPosition());

		if (concluida) {
			holder.check.setImageResource(R.drawable.ic_check_circle_green_600);
		} else {
			if(mNow.after(cal)) {
				holder.check.setImageResource(R.drawable.ic_warning_red_600);
			} else {
				holder.check.setImageResource(R.drawable.ic_check_circle_grey_600);
			}
		}
	}

	public String[] getProjection() {
		return mProjection;
	}

	public Aula getAula(int position) {
		Aula a = new Aula();

		Cursor cursor = (Cursor) getItem(position);

		int idIndex = cursor.getColumnIndex(BaseColumns._ID),
			dataIndex = cursor.getColumnIndex(Aula.DATA),
			concluidaIndex = cursor.getColumnIndex(Aula.CONCLUIDA);

		Long id = cursor.getLong(idIndex);
		Long data = cursor.getLong(dataIndex);
		boolean concluida = cursor.getInt(concluidaIndex) > 0;

		a.setId(id);
		a.setData(data);
		a.setConcluida(concluida);

		return a;
	}

	public static class Holder {
		TextView data;
		ImageView check, edit, delete;
	}

	public static interface OnSwipeOptionClickListener {
		public void onOptionClick(int position, @IdRes int optionId);
	}
}
