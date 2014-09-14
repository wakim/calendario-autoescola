package br.com.wakim.weekcalendarview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.view.View;
import android.view.ViewGroup;

import br.com.wakim.weekcalendarview.model.Event;
import br.com.wakim.weekcalendarview.utils.TextHelper;
import br.com.wakim.weekcalendarview.utils.XY;

/**
 * Created by wakim on 03/09/14.
 */
public class DraggableManager {

	private static final float SHADOW_RADIUS = 5f, DX = 7.5f, DY = 7.5f;
	private static final float SCROLL_OFFSET = 80f;
	private static final int SCROLL_AMOUNT = 40;

	private static final float MOVEMENT_INCREMENT = 30f;

	Event mEvent;

	float mX, mY;
	float mOffsetX = 0f, mOffsetY = 0f;
	float mVisibleHeight = 0f;

	float mTop = Float.MIN_VALUE, mBottom = Float.MAX_VALUE;

	float mCellWidth, mCellHeight;

	String mText;

	Paint mBackgroundPaint = new Paint();
	Paint mShadowPaint = new Paint();
	TextPaint mTextPaint = new TextPaint();

	AnimationRunnable mAnimationRunnable;

	void destroy() {
		mEvent = null;
		mBackgroundPaint = mShadowPaint = mTextPaint = null;
		mAnimationRunnable = null;
	}

	public DraggableManager() {
		mTextPaint.setColor(0xFFFFFFFF);
		mTextPaint.setAntiAlias(true);

		mShadowPaint.setColor(0x99999999);
	}

	Event popEvent() {
		Event e = mEvent;

		mEvent = null;

		return e;
	}

	Event getEvent() {
		return mEvent;
	}

	DraggableManager setTop(float top) {
		mTop = top;
		return this;
	}

	DraggableManager setBottom(float bottom) {
		mBottom = bottom;
		return this;
	}

	DraggableManager setVisibleHeight(float visibleHeight) {
		mVisibleHeight = visibleHeight;
		return this;
	}

	DraggableManager setCellWidth(float cellWidth) {
		mCellWidth = cellWidth;
		return this;
	}

	DraggableManager setCellHeight(float cellHeight) {
		mCellHeight = cellHeight;
		return this;
	}

	DraggableManager setEvent(Event event) {
		mEvent = event;

		mBackgroundPaint.setColor(mEvent.getColor());
		mText = event.getSymbol();

		return this;
	}

	DraggableManager setTypeface(Typeface typeface) {
		mTextPaint.setTypeface(typeface);
		return this;
	}

	DraggableManager setX(float x, float xCell) {
		mX = x;

		mOffsetX = xCell - x;

		return this;
	}

	DraggableManager setY(float y, float yCell) {
		mY = y;

		mOffsetY = yCell - y;

		return this;
	}

	DraggableManager setX(float x) {
		mX = x;
		return this;
	}

	DraggableManager setY(float y) {
		mY = y;
		return this;
	}

	DraggableManager setTextSize(float textSize) {
		mTextPaint.setTextSize(textSize);
		return this;
	}

	public void onDraw(Canvas canvas) {
		// No event to draw...
		if(mEvent == null) {
			return;
		}

		String cacheKey = TextHelper.getCacheKey(mText, mCellWidth, mCellHeight);

		if(TextHelper.sSizeCache.containsKey(cacheKey)) {
			mTextPaint.setTextSize(TextHelper.sSizeCache.get(cacheKey));
		} else {
			TextHelper.configureMaxTextSizeForBounds(mText, mTextPaint, mCellWidth, mCellHeight);
			TextHelper.sSizeCache.put(cacheKey, mTextPaint.getTextSize());
		}

		float x = mX + mOffsetX, y = mY + mOffsetY;
		float x2 = x + mCellWidth;
		float y2 = y + (mCellHeight * mEvent.getDuration());

		RectF shadowRect = new RectF(x - DX, y + DY, x2 - DX, y2 + DY);

		canvas.drawRoundRect(shadowRect, SHADOW_RADIUS, SHADOW_RADIUS, mShadowPaint);
		canvas.drawRect(x, y, x2, y2, mBackgroundPaint);

		TextHelper.drawText(mText, canvas, mTextPaint, x, y, mCellWidth, mCellHeight * mEvent.getDuration());

		if(mAnimationRunnable != null) {
			mAnimationRunnable.postDelayed();
		}
	}

	public boolean hasEvent() {
		return mEvent != null;
	}

	public void scrollIfNeeded(ViewGroup parent) {
		int offset;

		float scrollY = parent.getScrollY();
		float relativeY = mY - scrollY;

		if(relativeY >= (mVisibleHeight - SCROLL_OFFSET) && mY <= (mBottom - SCROLL_OFFSET)) {
			offset = SCROLL_AMOUNT;
		} else if(relativeY <= (SCROLL_OFFSET) && mY >= SCROLL_OFFSET) {
			offset = - SCROLL_AMOUNT;
		} else {
			return;
		}

		parent.scrollBy(0, offset);
	}

	public void animateDrop(OnDropAnimationListener listener, View view, XY targetPosition) {
		mAnimationRunnable = new AnimationRunnable(view, listener, targetPosition.x, targetPosition.y);

		mX += mOffsetX;
		mY += mOffsetY;

		mOffsetX = mOffsetY = 0f;

		view.postDelayed(mAnimationRunnable, 16l);
	}

	public class AnimationRunnable implements Runnable {

		OnDropAnimationListener mListener;
		View mView;
		float mTargetX, mTargetY;

		public AnimationRunnable(View view, OnDropAnimationListener listener, float targetX, float targetY) {
			mListener = listener;
			mView = view;
			mTargetX = targetX;
			mTargetY = targetY;
		}

		@Override
		public void run() {

			if(mEvent == null) {
				return;
			}

			if(mX == mTargetX && mY == mTargetY) {
				mListener.onAnimationEnd(mEvent);
				mEvent = null;
				mAnimationRunnable = null;
				return;
			}

			float xIncrement = MOVEMENT_INCREMENT, yIncrement = MOVEMENT_INCREMENT;

			if(mX > mTargetX) {
				xIncrement = - xIncrement;
			}

			if(mY > mTargetY) {
				yIncrement = - yIncrement;
			}

			float x = mX + xIncrement;
			float y = mY + yIncrement;

			if(Math.abs(x - mTargetX) <= MOVEMENT_INCREMENT) {
				x = mTargetX;
			}

			if(Math.abs(y - mTargetY) <= MOVEMENT_INCREMENT) {
				y = mTargetY;
			}

			setX(x).setY(y);

			mView.invalidate();
		}

		void postDelayed() {
			mView.postDelayed(this, 16l);
		}
	}

	public static interface OnDropAnimationListener {
		public void onAnimationEnd(Event event);
	}
}
