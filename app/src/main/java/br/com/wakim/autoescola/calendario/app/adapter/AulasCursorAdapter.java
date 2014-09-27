package br.com.wakim.autoescola.calendario.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.model.Aula;

/**
 * Created by wakim on 15/08/14.
 */
public class AulasCursorAdapter extends SimpleCursorAdapter {

	SimpleDateFormat mDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
	Calendar mNow = Calendar.getInstance();

	public static final String[] sProjection = {
		BaseColumns._ID,
		Aula.DATA,
		Aula.CONCLUIDA,
		Aula.DISCIPLINA
	};

	OnOptionClickListener mOptionClickListener;

	View.OnClickListener mClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Integer position = (Integer) v.getTag(R.layout.list_item_aula);

			if(position != null && mOptionClickListener != null) {
				mOptionClickListener.onOptionClick(position, v.getId());
			}
		}
	};

	public AulasCursorAdapter(Context context, OnOptionClickListener optionClickListener) {
		this(context, R.layout.list_item_aula, optionClickListener);
	}

	AulasCursorAdapter(Context context, int layoutResId, OnOptionClickListener optionClickListener) {
		this(context, sProjection, layoutResId, optionClickListener);
	}

	AulasCursorAdapter(Context context, String[] projection, int layoutResId, OnOptionClickListener optionClickListener) {
		super(context, layoutResId, null, projection, null, 0);
		mOptionClickListener = optionClickListener;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Holder holder = (Holder) view.getTag(R.layout.list_item_aula);

		if(holder == null) {
			holder = new Holder();

			holder.data = (TextView) view.findViewById(R.id.lia_data);
			holder.check = (ToggleButton) view.findViewById(R.id.lia_check);

			holder.check.setOnClickListener(mClickListener);

			view.setTag(R.layout.list_item_aula, holder);
		}

		int dataIndex = cursor.getColumnIndex(Aula.DATA),
			concluidaIndex = cursor.getColumnIndex(Aula.CONCLUIDA);

		Long data = cursor.getLong(dataIndex);
		Boolean concluida = cursor.getInt(concluidaIndex) > 0;

		Calendar cal = Calendar.getInstance();

		cal.setTimeInMillis(data);

		holder.data.setText(mDateFormat.format(cal.getTime()));

		holder.check.setTag(R.layout.list_item_aula, cursor.getPosition());

		if(mNow.after(cal)) {
			holder.check.setBackgroundResource(R.drawable.custom_late_check_toggle);
		} else {
			holder.check.setBackgroundResource(R.drawable.custom_check_toggle);
		}

		holder.check.setChecked(concluida);
	}

	public String[] getProjection() {
		return sProjection;
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

	public void destroy() {
		mOptionClickListener = null;
		mDateFormat = null;
		mClickListener = null;
		mNow = null;
	}

	public static class Holder {
		TextView data;
		ToggleButton check;
	}
}
