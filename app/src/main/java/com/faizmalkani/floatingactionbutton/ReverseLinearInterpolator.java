package com.faizmalkani.floatingactionbutton;

import android.view.animation.Interpolator;

/**
 * Created by wakim on 26/07/14.
 */
public class ReverseLinearInterpolator implements Interpolator {
	@Override
	public float getInterpolation(float input) {
		return Math.abs(input - 1f);
	}
}
