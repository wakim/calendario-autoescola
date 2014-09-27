package br.com.wakim.autoescola.calendario.app.model.task;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.activeandroid.Model;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import br.com.wakim.autoescola.calendario.app.model.Aula;

/**
 * Created by wakim on 15/06/14.
 */
public class ProximasAulasAsyncTaskLoader extends AsyncTaskLoader<List<Aula>> {

	List<Aula> mProximasAulas;

	private static Integer sId = 0;

	public ProximasAulasAsyncTaskLoader(Context context) {
		super(context);
	}

	/****************************************************/
	/** (1) A task that performs the asynchronous load **/
	/****************************************************/
	@Override
	public List<Aula> loadInBackground() {

		Calendar today = Calendar.getInstance();
		Calendar tomorrow = Calendar.getInstance();

		today.clear(Calendar.MINUTE);
		today.clear(Calendar.SECOND);
		today.clear(Calendar.MILLISECOND);

		tomorrow.add(Calendar.DAY_OF_YEAR, 1);

		tomorrow.clear(Calendar.HOUR);
		tomorrow.clear(Calendar.MINUTE);
		tomorrow.clear(Calendar.SECOND);
		tomorrow.clear(Calendar.MILLISECOND);


		tomorrow.add(Calendar.MINUTE, -1);

		return Aula.recuperarNoPeriodo(today.getTimeInMillis(), tomorrow.getTimeInMillis());
	}

	/********************************************************/
	/** (2) Deliver the results to the registered listener **/
	/********************************************************/
	@Override
	public void deliverResult(List<Aula> data) {
		if(isReset()) {
			releaseResources(data);
			return;
		}

		List<Aula> oldData = mProximasAulas;
		mProximasAulas = data;

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
		if(mProximasAulas != null) {
			deliverResult(mProximasAulas);
		}

		if (takeContentChanged() || mProximasAulas == null) {
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
	public void onCanceled(List<Aula> data) {
		releaseResources(data);
	}

	@Override
	protected void onReset() {
		super.onReset();

		onStopLoading();

		releaseResources(mProximasAulas);
		mProximasAulas = null;
	}

	@Override
	protected void onStopLoading() {
		cancelLoad();
	}

	protected void releaseResources(List<Aula> data) {
		// For a simple List, there is nothing to do. For something like a Cursor, we
		// would close it in this method. All resources associated with the Loader
		// should be released here.
	}

	public void refresh() {
		onContentChanged();
	}
}
