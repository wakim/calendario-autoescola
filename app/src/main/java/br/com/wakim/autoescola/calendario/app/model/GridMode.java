package br.com.wakim.autoescola.calendario.app.model;

import hirondelle.date4j.DateTime;

/**
 * Created by wakim on 31/08/14.
 */
public enum GridMode {
	DAY, WEEK;

	public DateTime getStartDate(DateTime base) {
		switch(this) {
			case DAY:
				return base.getStartOfDay();
			case WEEK:
				return base.plusDays(1 - base.getWeekDay()).getStartOfDay();
			default:
				return null;
		}
	}

	public DateTime getEndDate(DateTime base) {
		switch(this) {
			case DAY:
				return base.getEndOfDay();
			case WEEK:
				return base.plusDays(7 - base.getWeekDay()).getEndOfDay();
			default:
				return null;
		}
	}

	public int getDays() {
		switch(this) {
			case DAY: return 1;
			case WEEK: return 7;
			default: return 0;
		}
	}
}
