package br.com.wakim.weekcalendarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import hirondelle.date4j.DateTime;

/**
 * Created by wakim on 02/09/14.
 */
public class WeekCalendarHeaderView extends View {

	float mCellWidth = 0f, mCellHeight = 0f, mOffsetX = 0f;
	float mTextWidthLimit = 0f;

	int mDays = 0;

	boolean mTwoLineHeader = false, mAbbreviateDays = false;

	DateTime mStartDate;

	String[] mDayLabels;

	Typeface mTypeface;

	TextPaint mTextPaint = new TextPaint();

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		mStartDate = null;
		mTextPaint = null;
		mTypeface = null;
		mDayLabels = null;
	}

	public WeekCalendarHeaderView(Context context) {
		super(context);
		init(context, null, 0);

		// Not drawing now, waiting for dates
		setWillNotDraw(true);
	}

	public WeekCalendarHeaderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
		// Not drawing now, waiting for dates
		setWillNotDraw(true);
	}

	public WeekCalendarHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr);

		// Not drawing now, waiting for dates
		setWillNotDraw(true);
	}

	void init(Context context, AttributeSet attrs, int defStyle) {

		if(attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WeekCalendarView, defStyle, 0);

			mTwoLineHeader = a.getBoolean(R.styleable.WeekCalendarHeaderView_two_line_header, true);
			mAbbreviateDays = a.getBoolean(R.styleable.WeekCalendarHeaderView_abreviate_days, true);

			a.recycle();
		}

		mTextPaint.setAntiAlias(true);

		mDayLabels = context.getResources().getStringArray(mAbbreviateDays ? R.array.wc__abreviatted_days : R.array.wc__days);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension((int) (mOffsetX + (mCellWidth * (mDays + 1))), (int) mCellHeight);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		drawVerticalDates(canvas, mOffsetX, mDays, mCellWidth, mCellHeight);
	}

	void drawVerticalDates(Canvas canvas, float firstColumnWidth, int verticalCellsCount, float cellWidth, float cellHeight) {
		DateTime date = mStartDate.plusDays(0);
		float originalTextSize = mTextPaint.getTextSize();
		float textCellHeight = mTwoLineHeader ? cellHeight / 2f : cellHeight;
		float textCellWidth = cellWidth;
		float x = firstColumnWidth; // Ignorando a primeira celula

		if(textCellWidth > mTextWidthLimit) {
			textCellWidth = mTextWidthLimit;
		}

		TextHelper.configureMaxTextSizeForBounds(mTwoLineHeader ? "MMM" : "MMM MM", mTextPaint, textCellWidth, textCellHeight);

		for(int i = 0; i < verticalCellsCount; ++i, x += cellWidth, date = date.plusDays(1)) {
			TextHelper.drawText(getHeaderString(date), canvas, mTextPaint, x, 0, cellWidth, cellHeight);
		}

		mTextPaint.setTextSize(originalTextSize);
	}

	String getHeaderString(DateTime date) {
		return mDayLabels[date.getWeekDay() - 1]
				.concat(mTwoLineHeader ? "\n" : " ")
				.concat(date.getDay() < 10 ? "0" : "")
				.concat(Integer.toString(date.getDay()));
	}

	void setStartDate(DateTime startDate) {
		mStartDate = startDate;
		verifyIfWillDraw();
	}

	void setTextWidthLimit(float textWidthLimit) {
		mTextWidthLimit = textWidthLimit;
	}

	void setCellWidth(float cellWidth) {
		mCellWidth = cellWidth;
		verifyIfWillDraw();
	}

	void setCellHeight(float cellHeight) {
		mCellHeight = cellHeight;
		verifyIfWillDraw();
	}

	void setOffsetX(float offsetX) {
		mOffsetX = offsetX;
		verifyIfWillDraw();
	}

	void setTypeface(Typeface typeface) {
		mTypeface = typeface;

		if(mTypeface != null) {
			mTextPaint.setTypeface(mTypeface);
		}
	}

	void setDays(int days) {
		mDays = days;
		verifyIfWillDraw();
	}

	void setTextSize(float textSize) {
		mTextPaint.setTextSize(textSize);
	}

	void verifyIfWillDraw() {
		if(mCellHeight != 0 && mCellWidth != 0 && mDays != 0 && mStartDate != null) {
			setWillNotDraw(false);
		}
	}

	public void setAbbreviateDays(boolean abbreviateDays) {
		mAbbreviateDays = abbreviateDays;
		mDayLabels = getContext().getResources().getStringArray(mAbbreviateDays ? R.array.wc__abreviatted_days : R.array.wc__days);

		invalidate();
	}

	public void setTwoLineHeader(boolean twoLineHeader) {
		mTwoLineHeader = twoLineHeader;

		invalidate();
	}
}
