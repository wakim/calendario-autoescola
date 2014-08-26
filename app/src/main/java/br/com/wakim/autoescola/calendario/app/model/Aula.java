package br.com.wakim.autoescola.calendario.app.model;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.activeandroid.query.Sqlable;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by wakim on 10/08/14.
 */
@Table(name = "aula", id = BaseColumns._ID)
public class Aula extends Model {

	public static final String DATA = "data",
							   CONCLUIDA = "concluida",
							   DISCIPLINA = "disciplina";

	@Column(name = DISCIPLINA, onDelete = Column.ForeignKeyAction.CASCADE)
	Disciplina disciplina;

	@Column(name = DATA, notNull = true)
	long data;

	@Column(name = CONCLUIDA)
	boolean concluida;

	public void setId(Long id) {
		try {

			if(id == -123l) {
				id = null;
			}

			Field mId = Model.class.getDeclaredField("mId");

			mId.setAccessible(true);
			mId.set(this, id);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void setDisciplina(Disciplina disciplina) {
		this.disciplina = disciplina;
	}

	public Calendar getDataAsCalendar() {
		Calendar cal = Calendar.getInstance();

		cal.setTimeInMillis(data);

		return cal;
	}

	public Date getDataAsDate() {
		return getDataAsCalendar().getTime();
	}

	public Long getData() {
		return data;
	}

	public void setData(long data) {
		this.data = data;
	}

	public boolean isConcluida() {
		return concluida;
	}

	public void setConcluida(boolean concluida) {
		this.concluida = concluida;
	}

	public static List<Aula> recuperarApos(Date date, Disciplina... disciplinas) {
		return recuperar(date, null, disciplinas);
	}

	public static List<Aula> recuperarAntes(Date date, Disciplina... disciplinas) {
		return recuperar(null, date, disciplinas);
	}

	static List<Aula> recuperar(Date dataInicial, Date dataFinal, Disciplina... disciplinas) {
		From from = new Select()
			.from(Aula.class);

		from.where("1=1");

		if(dataInicial != null) {
			from.and("data >= ?", dataInicial.getTime());
		}

		if(dataFinal != null) {
			from.and("data <= ?", dataFinal.getTime());
		}

		if(disciplinas != null && disciplinas.length > 0) {
			from.and("disciplina IN ?", serialize(disciplinas));
		}

		return from.execute();
	}

	static String serialize(Disciplina[] disciplinas) {
		StringBuilder builder = new StringBuilder("(");

		for(Disciplina disciplina : disciplinas) {
			builder.append(disciplina.getId().toString()).append(",");
		}

		builder.deleteCharAt(builder.length() - 1);
		builder.append(")");

		return builder.toString();
	}
}
