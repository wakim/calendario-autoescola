package br.com.wakim.autoescola.calendario.app.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Cache;

import java.util.TimeZone;

import br.com.wakim.weekcalendarview.model.Event;
import hirondelle.date4j.DateTime;

/**
 * Created by wakim on 02/09/14.
 */
public class DefaultEventImpl implements Event {

	DateTime date;
	String symbol;
	int color;

	// Custom Data
	long idAula;
	long idDisciplina;
	String nomeDisciplina;

	public DefaultEventImpl() {}

	public DefaultEventImpl(DateTime date, String symbol, int color, long idAula, long idDisciplina, String nomeDisciplina) {
		this.date = date;
		this.symbol = symbol;
		this.color = color;

		this.idAula = idAula;
		this.idDisciplina = idDisciplina;
		this.nomeDisciplina = nomeDisciplina;
	}

	public DefaultEventImpl(Parcel in) {
		date = DateTime.forInstantNanos(in.readLong(), TimeZone.getDefault());
		symbol = in.readString();
		color = in.readInt();

		idAula = in.readLong();
		idDisciplina = in.readLong();
		nomeDisciplina = in.readString();
	}

	@Override
	public int getColor() {
		return color;
	}

	@Override
	public String getSymbol() {
		return symbol;
	}

	@Override
	public DateTime getDate() {
		return date;
	}

	// Now, every event is 1 hour
	@Override
	public float getDuration() {
		return 1f;
	}

	@Override
	public void setDate(DateTime date) {
		this.date = date;
	}

	public long getIdAula() {
		return idAula;
	}

	public long getIdDisciplina() {
		return idDisciplina;
	}

	public String getNomeDisciplina() {
		return nomeDisciplina;
	}

	@Override
	public long getType() {
		return idDisciplina;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(date.getMilliseconds(TimeZone.getDefault()));
		dest.writeString(symbol);
		dest.writeInt(color);

		dest.writeLong(idAula);
		dest.writeLong(idDisciplina);
		dest.writeString(nomeDisciplina);
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public DefaultEventImpl createFromParcel(Parcel in) {
			if(! Cache.isInitialized()) {
				return null;
			}

			return new DefaultEventImpl(in);
		}

		public DefaultEventImpl[] newArray(int size) {
			return new DefaultEventImpl[size];
		}
	};
}
