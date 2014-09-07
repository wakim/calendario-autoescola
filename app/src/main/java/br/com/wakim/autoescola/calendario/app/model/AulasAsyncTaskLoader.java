package br.com.wakim.autoescola.calendario.app.model;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import br.com.wakim.weekcalendarview.Event;
import hirondelle.date4j.DateTime;

/**
 * Created by wakim on 15/06/14.
 */
public class AulasAsyncTaskLoader extends AsyncTaskLoader<Map<DateTime, Event>> {

	Map<DateTime, Event> mData;
	DateTime mStartDate, mEndDate;

	private static Integer sId = 0;

	public AulasAsyncTaskLoader(Context context, DateTime startDate, DateTime endDate) {
		super(context);

		mStartDate = startDate;
		mEndDate = endDate;
	}

	/****************************************************/
	/** (1) A task that performs the asynchronous load **/
	/****************************************************/
	@Override
	public Map<DateTime, Event> loadInBackground() {

		Map<DateTime, Event> aulas = new HashMap<DateTime, Event>();

		if(mStartDate == null || mEndDate == null) {
			return aulas;
		}

		TimeZone tz = TimeZone.getDefault();

		DateTime startDate = mStartDate.getStartOfDay();
		DateTime endDate = mEndDate.getEndOfDay();

		List<Aula> listaAulas = Aula.recuperarNoPeriodo(startDate.getMilliseconds(tz), endDate.getMilliseconds(tz));
		Calendar calendar = Calendar.getInstance();

		for(Aula aula : listaAulas) {
			calendar.setTimeInMillis(aula.getData());

			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH) + 1;
			int day = calendar.get(Calendar.DATE);
			int hour = calendar.get(Calendar.HOUR);

			DateTime dt = new DateTime(year, month, day, hour, 0, 0, 0);

			aulas.put(dt, new DefaultEventImpl(dt, aula.getDisciplina().getSimbolo(), aula.getDisciplina().getCor(), aula.getId(), aula.getDisciplina().getId(), aula.getDisciplina().getNome()));
		}

		return aulas;
	}

	/********************************************************/
	/** (2) Deliver the results to the registered listener **/
	/********************************************************/
	@Override
	public void deliverResult(Map<DateTime, Event> data) {
		if(isReset()) {
			releaseResources(data);
			return;
		}

		Map<DateTime, Event> oldData = mData;
		mData = data;

		if(isStarted()) {
			super.deliverResult(data);
		}

		if(oldData != null && oldData != data) {
			releaseResources(oldData);
		}
	}

	/*********************************************************/
	/** (3) Implement the Loader’s state-dependent behavior **/
	/*********************************************************/
	@Override
	protected void onStartLoading() {
		if(mData != null) {
			deliverResult(mData);
		}

		if (takeContentChanged() || mData == null) {
			// When the observer detects a change, it should call onContentChanged()
			// on the Loader, which will cause the next call to takeContentChanged()
			// to return true. If this is ever the case (or if the current data is
			// null), we force a new load.
			forceLoad();
		}
	}

	@Override
	public void stopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	@Override
	public void onCanceled(Map<DateTime, Event> data) {
		releaseResources(data);
	}

	@Override
	protected void onReset() {
		super.onReset();

		onStopLoading();

		releaseResources(mData);
		mData = null;
	}

	@Override
	protected void onStopLoading() {
		cancelLoad();
	}

	protected void releaseResources(Map<DateTime, Event> data) {
		// For a simple List, there is nothing to do. For something like a Cursor, we
		// would close it in this method. All resources associated with the Loader
		// should be released here.

		if(data != null) {
			data.clear();
		}
	}

	public void updateDates(DateTime startDate, DateTime endDate) {
		mStartDate = startDate;
		mEndDate = endDate;

		onContentChanged();
	}
}
