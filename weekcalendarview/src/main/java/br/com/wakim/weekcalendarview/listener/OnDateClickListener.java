package br.com.wakim.weekcalendarview.listener;

import br.com.wakim.weekcalendarview.model.Event;
import hirondelle.date4j.DateTime;

public interface OnDateClickListener {
	public void onDateClicked(DateTime date);
	public void onDateClicked(DateTime date, Event event);
}