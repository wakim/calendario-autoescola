package br.com.wakim.autoescola.calendario.app.model.task;

import android.os.AsyncTask;

import br.com.wakim.autoescola.calendario.app.model.Aula;

/**
 * Created by wakim on 13/09/14.
 */
public class AulaOperationAsyncTask extends AbstractOperationAsyncTask<Void, Void, Void> {

	Long mAulaId;
	Aula mAula;

	long mDate;

	public AulaOperationAsyncTask(Aula aula, Operation op) {
		super(op);
		mAula = aula;
	}

	public AulaOperationAsyncTask(long aulaId, Operation op) {
		super(op);
		mAulaId = aulaId;
	}

	public AulaOperationAsyncTask setAula(Aula aula) {
		mAula = aula;
		return this;
	}

	public AulaOperationAsyncTask setOperation(Operation op) {
		mOp = op;
		return this;
	}

	public AulaOperationAsyncTask setDate(long date) {
		mDate = date;
		return this;
	}

	@Override
	protected Void doInBackground(Void... params) {
		super.doInBackground(params);

		if(mAula == null && mAulaId != null) {
			mAula = Aula.load(Aula.class, mAulaId);
		}

		switch(mOp) {
			case DELETE:
				delete();
				break;
			case CONCLUIDA_TOGGLE:
				concluidaToggle();
				break;
			case UPDATE_DATE:
				updateDate();
				break;
		}

		return null;
	}

	void updateDate() {
		mAula.setData(mDate);
		mAula.save();
	}

	void delete() {
		mAula.delete();
		mAula.getDisciplina().saveAndCalculate();
	}

	void concluidaToggle() {
		mAula.setConcluida(! mAula.isConcluida());
		mAula.save();

		mAula.getDisciplina().saveAndCalculate();
	}

	@Override
	protected void onPostExecute(Void aVoid) {
		super.onPostExecute(aVoid);

		mPostOperation = null;
		mAula = null;
	}
}
