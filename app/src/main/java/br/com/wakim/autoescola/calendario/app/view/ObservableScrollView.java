package br.com.wakim.autoescola.calendario.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by wakim on 14/09/14.
 */
public class ObservableScrollView extends ScrollView {

	OnScrollListener mScrollListener;

	public ObservableScrollView(Context context) {
		super(context);
	}

	public ObservableScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ObservableScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);

		if(mScrollListener != null) {
			mScrollListener.onScroll(l, t, oldl, oldt);
		}
	}

	public void setOnScrollListener(OnScrollListener scrollListener) {
		mScrollListener = scrollListener;
	}

	public static interface OnScrollListener {
		public void onScroll(int x, int y, int oldX, int oldY);
	}
}
