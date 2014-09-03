package br.com.wakim.weekcalendarview;

import android.os.Parcelable;

import hirondelle.date4j.DateTime;

/**
 * Created by wakim on 02/09/14.
 */
public interface Event extends Parcelable {
	public int getColor();
	public String getSymbol();
	public DateTime getDate();
}
