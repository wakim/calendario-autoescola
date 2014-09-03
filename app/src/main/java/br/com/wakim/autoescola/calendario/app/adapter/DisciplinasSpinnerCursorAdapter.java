package br.com.wakim.autoescola.calendario.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.provider.BaseColumns;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.model.Disciplina;

/**
 * Created by wakim on 11/08/14.
 */
public class DisciplinasSpinnerCursorAdapter extends SimpleCursorAdapter {

	private int mDotSize;

	String[] mProjection = {
			BaseColumns._ID,
			Disciplina.NOME,
			Disciplina.COR,
			Disciplina.SIMBOLO
	};

	public DisciplinasSpinnerCursorAdapter(Context context) {
		super(context, R.layout.list_item_spinner_disciplina, null, new String[] {
				BaseColumns._ID,
				Disciplina.NOME,
				Disciplina.COR,
				Disciplina.SIMBOLO
		}, null, 0);

		mDotSize = context.getResources().getDimensionPixelSize(R.dimen.tag_color_dot_size);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		if (mDataValid) {
			mCursor.moveToPosition(position);
			View v;

			if (convertView == null) {
				v = newDropDownView(mContext, mCursor, parent);
			} else {
				v = convertView;
			}

			bindView(v, mContext, mCursor, true);

			return v;
		} else {
			return null;
		}
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		bindView(view, context, cursor, false);
	}

	void bindView(View view, Context context, Cursor cursor, boolean isDropDown) {
		Holder holder = (Holder) view.getTag(R.layout.list_item_spinner_disciplina);

		if(holder == null) {
			holder = new Holder();

			holder.text1 = (TextView) view.findViewById(android.R.id.text1);

			view.setTag(R.layout.list_item_spinner_disciplina, holder);
		}

		int nameIndex = cursor.getColumnIndex(Disciplina.NOME),
				symbolIndex = cursor.getColumnIndex(Disciplina.SIMBOLO),
				colorIndex = cursor.getColumnIndex(Disciplina.COR);

		String name = cursor.getString(nameIndex);
		String symbol = cursor.isNull(symbolIndex) ? null : cursor.getString(symbolIndex);
		Integer color = cursor.getInt(colorIndex);

		if(isDropDown) {
			holder.text1.setTextColor(context.getResources().getColor(R.color.black_87p));
			setupColor(holder.text1, color);
			holder.text1.setText(name + " - " + symbol);
		} else {
			holder.text1.setTextColor(context.getResources().getColor(R.color.white));
			holder.text1.setText(name);
		}
	}


	private void setupColor(TextView textView, int color) {
		ShapeDrawable colorDrawable = (ShapeDrawable) textView.getCompoundDrawables()[2];

		if (color == 0) {
			if (colorDrawable != null) {
				textView.setCompoundDrawables(null, null, null, null);
			}
		} else if (colorDrawable == null) {
			colorDrawable = new ShapeDrawable(new OvalShape());
			colorDrawable.setIntrinsicWidth(mDotSize);
			colorDrawable.setIntrinsicHeight(mDotSize);
			colorDrawable.getPaint().setStyle(Paint.Style.FILL);

			textView.setCompoundDrawablesWithIntrinsicBounds(null, null, colorDrawable, null);
		}

		colorDrawable.getPaint().setColor(color);
	}

	public Disciplina getDisciplina(int position) {
		Disciplina d = new Disciplina();

		Cursor cursor = (Cursor) getItem(position);

		int	idIndex = cursor.getColumnIndex(BaseColumns._ID),
			nameIndex = cursor.getColumnIndex(Disciplina.NOME),
			symbolIndex = cursor.getColumnIndex(Disciplina.SIMBOLO),
			colorIndex = cursor.getColumnIndex(Disciplina.COR);

		Long id = cursor.getLong(idIndex);
		String name = cursor.getString(nameIndex);
		String symbol = cursor.isNull(symbolIndex) ? null : cursor.getString(symbolIndex);
		Integer color = cursor.isNull(colorIndex) ? null : cursor.getInt(colorIndex);

		d.setId(id);
		d.setCor(color);
		d.setNome(name);
		d.setSimbolo(symbol);

		return d;
	}

	public void destroy() {
		mProjection = null;
	}

	public String[] getProjection() {
		return mProjection;
	}

	public static class Holder {
		TextView text1;
	}
}
