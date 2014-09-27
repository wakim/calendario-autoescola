package br.com.wakim.autoescola.calendario.app.utils;

import android.util.Pair;

import java.io.Serializable;

/**
 * Created by wakim on 20/09/14.
 */
public class SerializablePair<F, S> extends Pair<F, S> implements Serializable {
	/**
	 * Constructor for a Pair.
	 *
	 * @param first  the first object in the Pair
	 * @param second the second object in the pair
	 */
	public SerializablePair(F first, S second) {
		super(first, second);
	}
}
