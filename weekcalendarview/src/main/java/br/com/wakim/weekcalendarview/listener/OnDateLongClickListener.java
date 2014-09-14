package br.com.wakim.weekcalendarview.listener;

import br.com.wakim.weekcalendarview.model.Event;
import hirondelle.date4j.DateTime;

public interface OnDateLongClickListener {
	public void onDateLongClicked(DateTime date);
	public void onDateLongClicked(DateTime date, Event event);
}