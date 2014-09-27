package br.com.wakim.autoescola.calendario.app.model.task;

import br.com.wakim.autoescola.calendario.app.model.Disciplina;

/**
 * Created by wakim on 13/09/14.
 */
public class DisciplinaOperationAsyncTask extends AbstractOperationAsyncTask<Disciplina, Void, Void, Void> {

	public DisciplinaOperationAsyncTask(Disciplina disciplina, Operation op) {
		super(disciplina, op);
	}

	@Override
	protected Void doInBackground(Void... params) {
		super.doInBackground(params);

		switch (mOp) {
			case PERSIST:
				persist();
				break;
		}

		return null;
	}

	void persist() {
		mResource.saveAndCalculate();
	}
}
