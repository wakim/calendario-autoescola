package br.com.wakim.autoescola.calendario.app.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.EditText;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.utils.FontHelper;

/**
 * Created by wakim on 20/08/14.
 */
public class RobotoEditText extends EditText {

	public RobotoEditText(Context context) {
		super(context);
	}

	public RobotoEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public RobotoEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	void init(Context context, AttributeSet attrs) {
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RobotoTextView);

		int typefaceIndex = a.getInteger(R.styleable.RobotoTextView_typeface, -1);

		if(typefaceIndex != -1) {
			setTypeface(FontHelper.loadTypeface(context, typefaceIndex));
		}

		a.recycle();
	}
}
