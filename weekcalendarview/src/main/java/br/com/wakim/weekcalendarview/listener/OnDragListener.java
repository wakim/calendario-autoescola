package br.com.wakim.weekcalendarview.listener;

import br.com.wakim.weekcalendarview.model.DropAction;
import br.com.wakim.weekcalendarview.model.Event;
import hirondelle.date4j.DateTime;

public interface OnDragListener {
	public boolean onStartDrag(Event event);
	public boolean onStopDrag(Event event, DateTime date);
	public DropAction onStopDrag(Event draggedEvent, Event targetEvent);
}