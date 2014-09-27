package br.com.wakim.autoescola.calendario.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.model.Aula;
import br.com.wakim.autoescola.calendario.app.model.Disciplina;

/**
 * Created by wakim on 15/08/14.
 */
public class ProximasAulasAdapter extends BaseAdapter {

	List<Aula> mAulas = new ArrayList<Aula>();

	SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
	Calendar mNow = Calendar.getInstance();

	LayoutInflater mInflater;

	View.OnClickListener mClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Integer position = (Integer) v.getTag(R.layout.list_item_proxima_aula);

			if(position != null && mOptionClickListener != null) {
				mOptionClickListener.onOptionClick(position, v.getId());
			}
		}
	};

	OnOptionClickListener mOptionClickListener;

	public ProximasAulasAdapter(Context context, OnOptionClickListener optionClickListener) {
		mInflater = LayoutInflater.from(context);
		mOptionClickListener = optionClickListener;
	}

	public void setAulas(List<Aula> aulas) {
		mAulas = aulas;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mAulas.size();
	}

	@Override
	public Object getItem(int position) {
		return mAulas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		Holder holder;

		if(convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item_proxima_aula, null);

			holder = new Holder();

			holder.data = (TextView) convertView.findViewById(R.id.lipa_data);
			holder.check = (ToggleButton) convertView.findViewById(R.id.lipa_check);
			holder.name = (TextView) convertView.findViewById(R.id.lipa_name);

			holder.check.setOnClickListener(mClickListener);

			convertView.setTag(R.layout.list_item_proxima_aula, holder);
		} else {
			holder = (Holder) convertView.getTag(R.layout.list_item_proxima_aula);
		}

		Aula aula = mAulas.get(position);

		Calendar start = aula.getDataAsCalendar();

		holder.data.setText(getPeriodoFormatado(mDateFormat, start, 1));
		holder.name.setText(aula.getDisciplina().getNome());
		holder.check.setTag(R.layout.list_item_proxima_aula, position);

		if(mNow.after(start)) {
			holder.check.setBackgroundResource(R.drawable.custom_late_check_toggle);
		} else {
			holder.check.setBackgroundResource(R.drawable.custom_check_toggle);
		}

		holder.check.setChecked(aula.isConcluida());

		return convertView;
	}

	public boolean isDelayed(Aula aula) {
		return mNow.after(aula.getDataAsCalendar());
	}

	public String getPeriodoFormatado(DateFormat df, Calendar startDate, int hours) {
		Calendar end = Calendar.getInstance();

		end.setTimeInMillis(startDate.getTimeInMillis());
		end.add(Calendar.HOUR, hours);

		return df.format(startDate.getTime()).concat(" - ").concat(df.format(end.getTime()));
	}

	public void clear() {
		mAulas.clear();
		mAulas = null;
		mInflater = null;
		mOptionClickListener = null;

		notifyDataSetChanged();
	}

	public static class Holder {
		TextView data, name;
		ToggleButton check;
	}
}
