package br.com.wakim.autoescola.calendario.app.application;

import android.util.Log;

import com.activeandroid.ActiveAndroid;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
	}
}
