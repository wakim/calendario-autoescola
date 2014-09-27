package br.com.wakim.autoescola.calendario.app.model.task;

import android.os.AsyncTask;

/**
 * Created by wakim on 13/09/14.
 */
public abstract class AbstractOperationAsyncTask<Resource, Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

	Resource mResource;
	Operation mOp;
	OperationRunnable<Resource> mPostOperation;

	public AbstractOperationAsyncTask(Resource resource, Operation op) {
		mOp = op;
		mResource = resource;
	}

	public AbstractOperationAsyncTask setPostOperation(OperationRunnable<Resource> postOperation) {
		mPostOperation = postOperation;
		return this;
	}

	@Override
	protected void onPostExecute(Result result) {
		super.onPostExecute(result);

		if(mPostOperation != null) {
			mPostOperation.run(mResource);
		}

		mPostOperation = null;
		mResource = null;
	}

	@Override
	protected Result doInBackground(Params... params) {
		return null;
	}

	public static enum Operation {
		PERSIST, UPDATE_DATE, DELETE, CONCLUIDA_TOGGLE;
	}

	public static interface OperationRunnable<T> {
		public void run(T t);
	}
}
