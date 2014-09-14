package br.com.wakim.autoescola.calendario.app.model.task;

import br.com.wakim.autoescola.calendario.app.model.Disciplina;

/**
 * Created by wakim on 13/09/14.
 */
public class DisciplinaOperationAsyncTask extends AbstractOperationAsyncTask<Void, Void, Void> {

	Disciplina mDisciplina;

	public DisciplinaOperationAsyncTask(Disciplina disciplina, Operation op) {
		super(op);
		mDisciplina = disciplina;
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
		mDisciplina.saveAndCalculate();
	}
}
