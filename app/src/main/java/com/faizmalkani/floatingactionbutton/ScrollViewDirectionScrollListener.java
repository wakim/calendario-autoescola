package com.faizmalkani.floatingactionbutton;

import br.com.wakim.autoescola.calendario.app.view.ObservableScrollView;

/**
 * Created by wakim on 14/09/14.
 */
public class ScrollViewDirectionScrollListener extends AbstractDirectionScrollListener implements ObservableScrollView.OnScrollListener {

	protected static final int SCROLL_DIRECTION_CHANGE_THRESHOLD = 3;

	ScrollViewDirectionScrollListener(FloatingActionButton floatingActionButton) {
		super(floatingActionButton);
	}

	@Override
	public void onScroll(int x, int y, int oldX, int oldY) {
		boolean goingDown = y > mPrevTop;
		boolean changed = Math.abs(mPrevTop - y) > SCROLL_DIRECTION_CHANGE_THRESHOLD;

		mPrevTop = y;
		hide(changed, goingDown);
	}
}
