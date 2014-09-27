package br.com.wakim.autoescola.calendario.app.utils;

import java.util.Calendar;
import java.util.Date;

import hirondelle.date4j.DateTime;

/**
 * Created by wakim on 31/08/14.
 */
public class CalendarHelper {
	public static DateTime convertDateToDateTime(Calendar calendar) {
		int year = calendar.get(Calendar.YEAR);
		int javaMonth = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DATE);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);

		// javaMonth start at 0. Need to plus 1 to get datetimeMonth
		return new DateTime(year, javaMonth + 1, day, hour, 0, 0, 0);
	}

	public static DateTime convertDateToDateTime(Date date) {
		// Get year, javaMonth, date
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.setTime(date);

		return convertDateToDateTime(calendar);
	}
}
