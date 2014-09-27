package br.com.wakim.autoescola.calendario.app.model.task;

import br.com.wakim.autoescola.calendario.app.model.Aula;

/**
 * Created by wakim on 13/09/14.
 */
public class AulaOperationAsyncTask extends AbstractOperationAsyncTask<Aula, Void, Void, Void> {

	Long mAulaId;
	long mDate;

	public AulaOperationAsyncTask(Aula aula, Operation op) {
		super(aula, op);
	}

	public AulaOperationAsyncTask(long aulaId, Operation op) {
		super(null, op);
		mAulaId = aulaId;
	}

	public AulaOperationAsyncTask setAula(Aula aula) {
		mResource = aula;
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

		if(mResource == null && mAulaId != null) {
			mResource = Aula.load(Aula.class, mAulaId);
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
			case PERSIST:
				persist();
		}

		return null;
	}

	void updateDate() {
		mResource.setData(mDate);
		mResource.save();
	}

	void delete() {
		mResource.delete();
		mResource.getDisciplina().saveAndCalculate();
	}

	void concluidaToggle() {
		mResource.setConcluida(! mResource.isConcluida());
		mResource.save();

		mResource.getDisciplina().saveAndCalculate();
	}

	void persist() {
		mResource.save();
		mResource.getDisciplina().saveAndCalculate();
	}

	public Aula getAula() {
		return mResource;
	}
}
