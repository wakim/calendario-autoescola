package br.com.wakim.autoescola.calendario.app.application;

import android.util.Log;

import com.activeandroid.ActiveAndroid;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import br.com.wakim.autoescola.calendario.app.model.InitialState;

/**
 * Created by wakim on 10/08/14.
 */
public class Application extends android.app.Application {

	@Override
	public void onCreate() {
		super.onCreate();

		ActiveAndroid.initialize(this);
		InitialState.persistIfNeeded(this);

		int[] styles = new int[] {
			DateFormat.LONG, DateFormat.MEDIUM, DateFormat.SHORT, DateFormat.FULL, DateFormat.DEFAULT
		};

		Date date = Calendar.getInstance().getTime();

		for(int style : styles) {
			printFormatted(date, style);
		}
	}

	void printFormatted(Date date, int style) {
		Log.d("AAAAA", SimpleDateFormat.getTimeInstance(style).format(date));
	}
}
