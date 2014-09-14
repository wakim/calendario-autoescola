package br.com.wakim.autoescola.calendario.app.model.task;

import android.os.AsyncTask;

/**
 * Created by wakim on 13/09/14.
 */
public abstract class AbstractOperationAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

	Operation mOp;
	Runnable mPostOperation;

	public AbstractOperationAsyncTask(Operation op) {
		mOp = op;
	}

	public void setPostOperation(Runnable postOperation) {
		mPostOperation = postOperation;
	}

	@Override
	protected void onPostExecute(Result result) {
		super.onPostExecute(result);

		if(mPostOperation != null) {
			mPostOperation.run();
		}
	}

	@Override
	protected Result doInBackground(Params... params) {
		return null;
	}

	public static enum Operation {
		PERSIST, UPDATE_DATE, DELETE, CONCLUIDA_TOGGLE;
	}
}
