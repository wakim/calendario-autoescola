package br.com.wakim.autoescola.calendario.app.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidGridAdapter;

import java.util.HashMap;

import hirondelle.date4j.DateTime;

/**
 * Created by wakim on 25/08/14.
 */
public class CustomCaldroidGridAdapter extends CaldroidGridAdapter {

	public CustomCaldroidGridAdapter(Context context, int month, int year, HashMap<String, Object> caldroidData, HashMap<String, Object> extraData) {
		super(context, month, year, caldroidData, extraData);
	}

	@SuppressWarnings("unchecked")
	protected void setCustomResources(DateTime dateTime, View backgroundView,
									  TextView textView) {
		// Set custom background resource
		HashMap<DateTime, Integer> backgroundForDateTimeMap = (HashMap<DateTime, Integer>) caldroidData.get(CaldroidFragment._BACKGROUND_FOR_DATETIME_MAP);

		// Set custom text color
		HashMap<DateTime, Integer> textColorForDateTimeMap = (HashMap<DateTime, Integer>) caldroidData.get(CaldroidFragment._TEXT_COLOR_FOR_DATETIME_MAP);

		if (backgroundForDateTimeMap != null) {
			// Get background resource for the dateTime
			Integer backgroundResource = backgroundForDateTimeMap.get(dateTime);

			// Set it
			if (backgroundResource != null) {
				backgroundView.setBackgroundColor(backgroundResource.intValue());
			}
		}

		if (textColorForDateTimeMap != null) {
			// Get textColor for the dateTime
			Integer textColorResource = textColorForDateTimeMap.get(dateTime);

			// Set it
			if (textColorResource != null) {
				textView.setTextColor(textColorResource.intValue());
			}
		}
	}
}
