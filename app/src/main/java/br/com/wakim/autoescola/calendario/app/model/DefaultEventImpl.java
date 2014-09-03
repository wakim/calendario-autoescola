package br.com.wakim.autoescola.calendario.app.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Cache;

import java.util.TimeZone;

import br.com.wakim.weekcalendarview.Event;
import hirondelle.date4j.DateTime;

/**
 * Created by wakim on 02/09/14.
 */
public class DefaultEventImpl implements Event {

	DateTime date;
	String symbol;
	int color;

	public DefaultEventImpl() {}

	public DefaultEventImpl(DateTime date, String symbol, int color) {
		this.date = date;
		this.symbol = symbol;
		this.color = color;
	}

	public DefaultEventImpl(Parcel in) {
		date = DateTime.forInstantNanos(in.readLong(), TimeZone.getDefault());
		symbol = in.readString();
		color = in.readInt();
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

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(date.getMilliseconds(TimeZone.getDefault()));
		dest.writeString(symbol);
		dest.writeInt(color);
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
