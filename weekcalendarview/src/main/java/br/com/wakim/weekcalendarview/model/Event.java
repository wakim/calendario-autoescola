package br.com.wakim.weekcalendarview.model;

import android.os.Parcelable;

import hirondelle.date4j.DateTime;

/**
 * Created by wakim on 02/09/14.
 */
public interface Event extends Parcelable {

	public long getType();
	public int getColor();
	public String getSymbol();
	public DateTime getDate();

	// Duration in hours
	public float getDuration();

	public void setDate(DateTime date);
}
