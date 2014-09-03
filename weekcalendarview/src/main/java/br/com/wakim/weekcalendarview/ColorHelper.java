package br.com.wakim.weekcalendarview;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;

/**
 * Created by wakim on 31/08/14.
 */
public abstract class ColorHelper {

	public static void configureColor(View view, int newColor) {
		Drawable drawable = view.getBackground();
		if(drawable instanceof GradientDrawable) {
			GradientDrawable gDrawable = (GradientDrawable) drawable;
			gDrawable.setColor(newColor);
		} else if(drawable instanceof StateListDrawable) {
			StateListDrawable listDrawable = (StateListDrawable) drawable;

			DrawableContainer.DrawableContainerState drawableContainerState =
					(DrawableContainer.DrawableContainerState) listDrawable.getConstantState();

			int c = drawableContainerState.getChildCount();
			Drawable[] children = drawableContainerState.getChildren();

			for(int i = 0; i < c; ++i) {
				Drawable child = children[i];
				int[] states = child.getState();

				if(child instanceof GradientDrawable) {
					GradientDrawable gChild = (GradientDrawable) child;

					if (containsState(states, android.R.attr.state_pressed)) {
						gChild.setColor(darkenColor(newColor));
					} else {
						gChild.setColor(newColor);
					}
				} else if(child instanceof ColorDrawable) {}
			}
		}
	}

	static boolean containsState(int[] states, int desiredState) {
		for(int state : states) {
			if(state == desiredState) {
				return true;
			}
		}

		return false;
	}

	public static int darkenColor(int color) {
		return darkenColor(color, 0.8f);
	}

	public static int darkenColor(int color, float amount) {
		float[] hsv = new float[3];

		Color.colorToHSV(color, hsv);
		hsv[2] *= amount;

		return Color.HSVToColor(hsv);
	}
}
