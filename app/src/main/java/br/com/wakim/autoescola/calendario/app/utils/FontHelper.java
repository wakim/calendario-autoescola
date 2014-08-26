package br.com.wakim.autoescola.calendario.app.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.SparseArray;

/**
 * Created by wakim on 20/08/14.
 */
public abstract class FontHelper {
	private static final SparseArray<Typeface> mCache = new SparseArray<Typeface>();

	public static Typeface loadTypeface(Context context, int typefaceIndex) {
		Typeface typeface = null;

		typeface = mCache.get(typefaceIndex, null);

		if(typeface == null) {

			String typefaceName = null;

			switch(typefaceIndex) {
				case 0:
					typefaceName = "Roboto-Regular.ttf";
					break;
				case 1:
					typefaceName = "Roboto-Medium.ttf";
					break;
				case 2:
					typefaceName = "Roboto-Light.ttf";
					break;
			}

			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/".concat(typefaceName));

			mCache.setValueAt(typefaceIndex, typeface);
		}

		return typeface;
	}
}
