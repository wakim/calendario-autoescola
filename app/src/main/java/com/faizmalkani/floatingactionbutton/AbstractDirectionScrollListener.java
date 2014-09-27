package com.faizmalkani.floatingactionbutton;

import java.lang.ref.WeakReference;

/**
 * Created by wakim on 14/09/14.
 */
public class AbstractDirectionScrollListener {

	protected static final int DIRECTION_CHANGE_THRESHOLD = 1;

	private final WeakReference<FloatingActionButton> mFloatingActionButton;

	private boolean mUpdated = false;
	protected int mPrevTop;

	AbstractDirectionScrollListener(FloatingActionButton floatingActionButton) {
		mFloatingActionButton = new WeakReference<FloatingActionButton>(floatingActionButton);
	}

	protected void hide(boolean changed, boolean goingDown) {

		if (changed && mUpdated && mFloatingActionButton.get() != null) {
			mFloatingActionButton.get().hide(goingDown);
		}

		mUpdated = true;
	}
}
