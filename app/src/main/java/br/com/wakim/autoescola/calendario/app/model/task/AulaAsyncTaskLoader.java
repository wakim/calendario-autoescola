package br.com.wakim.autoescola.calendario.app.model.task;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.activeandroid.Model;

import br.com.wakim.autoescola.calendario.app.model.Aula;

/**
 * Created by wakim on 15/06/14.
 */
public class AulaAsyncTaskLoader extends AsyncTaskLoader<Aula> {

	long mAulaId;
	Aula mAula;

	private static Integer sId = 0;

	public AulaAsyncTaskLoader(Context context, long aulaId) {
		super(context);

		mAulaId = aulaId;
	}

	/****************************************************/
	/** (1) A task that performs the asynchronous load **/
	/****************************************************/
	@Override
	public Aula loadInBackground() {
		return Model.load(Aula.class, mAulaId);
	}

	/********************************************************/
	/** (2) Deliver the results to the registered listener **/
	/********************************************************/
	@Override
	public void deliverResult(Aula data) {
		if(isReset()) {
			releaseResources(data);
			return;
		}

		Aula oldData = mAula;
		mAula = data;

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
		if(mAula != null) {
			deliverResult(mAula);
		}

		if (takeContentChanged() || mAula == null) {
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
	public void onCanceled(Aula data) {
		releaseResources(data);
	}

	@Override
	protected void onReset() {
		super.onReset();

		onStopLoading();

		releaseResources(mAula);
		mAula = null;
	}

	@Override
	protected void onStopLoading() {
		cancelLoad();
	}

	protected void releaseResources(Aula data) {
		// For a simple List, there is nothing to do. For something like a Cursor, we
		// would close it in this method. All resources associated with the Loader
		// should be released here.
	}

	public void updateId(long aulaId) {
		mAulaId = aulaId;
		onContentChanged();
	}

	public void refresh() {
		mAula = null;
		onContentChanged();
	}
}
