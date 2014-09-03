package br.com.wakim.autoescola.calendario.app.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import hirondelle.date4j.DateTime;

/**
 * Created by wakim on 10/08/14.
 */
@Table(name = "aula", id = BaseColumns._ID)
public class Aula extends Model implements Parcelable {

	public static final String DATA = "data",
							   CONCLUIDA = "concluida",
							   DISCIPLINA = "disciplina";

	@Column(name = DISCIPLINA, onDelete = Column.ForeignKeyAction.CASCADE)
	Disciplina disciplina;

	@Column(name = DATA, notNull = true)
	long data;

	@Column(name = CONCLUIDA)
	boolean concluida;

	public Aula() {}

	public Aula(Parcel in) {
		setId(in.readLong());
		setDisciplina(in.<Disciplina>readParcelable(Disciplina.class.getClassLoader()));
		setData(in.readLong());
		setConcluida(in.readInt() > 0);
	}

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

	public DateTime getDataAsDateTime() {
		Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(data);

		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1; // 0-based
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY); // 0..23
		int minute = calendar.get(Calendar.MINUTE);

		return new DateTime(year, month, day, hour, minute, 0, 0);
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
		return recuperarNoPeriodo(date.getTime(), null, disciplinas);
	}

	public static List<Aula> recuperarAntes(Date date, Disciplina... disciplinas) {
		return recuperarNoPeriodo(null, date.getTime(), disciplinas);
	}

	static List<Aula> recuperarNoPeriodo(Long dataInicial, Long dataFinal, Disciplina... disciplinas) {
		From from = new Select()
			.from(Aula.class);

		from.where("1=1");

		if(dataInicial != null) {
			from.and("data >= ?", dataInicial);
		}

		if(dataFinal != null) {
			from.and("data <= ?", dataFinal);
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

	public Disciplina getDisciplina() {
		return disciplina;
	}

	// Metodos da interface/protocolo Parcelable

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		Long id = getId();

		if(id == null) {
			id = -123l;
		}

		dest.writeLong(id);
		dest.writeParcelable(getDisciplina(), 0);
		dest.writeLong(getData());
		dest.writeInt(isConcluida() ? 1 : 0);
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public Aula createFromParcel(Parcel in) {
			if(! Cache.isInitialized()) {
				return null;
			}

			return new Aula(in);
		}

		public Aula[] newArray(int size) {
			return new Aula[size];
		}
	};
}
