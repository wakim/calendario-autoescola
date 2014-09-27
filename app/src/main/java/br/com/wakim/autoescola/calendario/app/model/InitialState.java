package br.com.wakim.autoescola.calendario.app.model;

import android.content.Context;
import android.graphics.Color;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Random;

import br.com.wakim.autoescola.calendario.R;

/**
 * Created by wakim on 10/08/14.
 */
public abstract class InitialState {

	static Random r = new Random();
	final static int MAX_AULAS = 15;
	final static int MAX_DIAS_ANTES_DEPOIS = 10;

	public static void persistIfNeeded(Context context) {

		if(! Disciplina.tem()) {
			for(String disciplina : context.getResources().getStringArray(R.array.disciplinas)) {
				try {
					persistDisciplina(disciplina);
				} catch (JSONException e) {}
			}
		}
	}

	static void persistDisciplina(String jsonString) throws JSONException {
		Disciplina d = new Disciplina();
		JSONObject json = new JSONObject(jsonString);

		d.setNome(json.getString("nome"));
		d.setLimite(json.getInt("limite"));
		d.setSimbolo(json.getString("simbolo"));
		d.setCor(Color.parseColor(json.getString("cor")));
		d.setTotalAulasConcluidas(0);
		d.setTotalAulasRestantes(d.getLimite());

		d.save();

		persistSomeAulas(d);

		d.saveAndCalculate();
	}

	static void persistSomeAulas(Disciplina disciplina) {
		Calendar now = Calendar.getInstance();
		Calendar data;
		int aulas = r.nextInt(MAX_AULAS);

		for(int i = 0; i < aulas; ++i) {
			int offsetDias = r.nextBoolean() ? r.nextInt(MAX_DIAS_ANTES_DEPOIS) : - r.nextInt(MAX_DIAS_ANTES_DEPOIS);
			int hora = r.nextInt(24);

			data = generateDate(now, offsetDias, hora);

			Aula aula = new Aula();

			aula.setDisciplina(disciplina);
			aula.setData(data.getTimeInMillis());
			aula.setConcluida(data.before(now) && r.nextGaussian() > 0.5d);

			aula.save();
		}
	}

	static Calendar generateDate(Calendar now, int offsetDays, int hour) {
		Calendar other = Calendar.getInstance();

		other.setTimeInMillis(now.getTimeInMillis());

		other.add(Calendar.DAY_OF_YEAR, offsetDays);
		other.set(Calendar.HOUR_OF_DAY, hour);
		other.set(Calendar.MINUTE, 0);
		other.set(Calendar.SECOND, 0);
		other.set(Calendar.MILLISECOND, 0);

		return other;
	}
}
