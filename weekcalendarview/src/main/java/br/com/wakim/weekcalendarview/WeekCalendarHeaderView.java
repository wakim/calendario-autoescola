package br.com.wakim.weekcalendarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.TimeZone;

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

	private static final String PARENT_STATE = "WC_PARENT_STATE",
								TWO_LINE_HEADER = "TWO_LINE_HEADER",
								ABREVIATTED_DAYS = "ABREVIATTED_DAYS",
								DAYS = "DAYS",
								TEXT_SIZE = "TEXT_SIZE",
								START_DATE = "START_DATE";

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();

		Bundle state = new Bundle();
		TimeZone tz = TimeZone.getDefault();

		state.putParcelable(PARENT_STATE, superState);
		state.putBoolean(TWO_LINE_HEADER, mTwoLineHeader);
		state.putBoolean(ABREVIATTED_DAYS, mAbbreviateDays);
		state.putInt(DAYS, mDays);
		state.putLong(START_DATE, mStartDate.getMilliseconds(tz));
		state.putFloat(TEXT_SIZE, mTextPaint.getTextSize());

		return state;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle savedState = (Bundle) state;

		Parcelable superState = savedState.getParcelable(PARENT_STATE);
		super.onRestoreInstanceState(superState);

		TimeZone tz = TimeZone.getDefault();

		mTwoLineHeader = savedState.getBoolean(TWO_LINE_HEADER);
		mAbbreviateDays = savedState.getBoolean(ABREVIATTED_DAYS);
		mDays = savedState.getInt(DAYS);
		mStartDate = DateTime.forInstant(savedState.getLong(START_DATE), tz);
		mTextPaint.setTextSize(savedState.getFloat(TEXT_SIZE));

		mDayLabels = getResources().getStringArray(mAbbreviateDays ? R.array.wc__abreviatted_days : R.array.wc__days);
	}

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

			int textSize = a.getDimensionPixelSize(R.styleable.WeekCalendarHeaderView_preferred_font_size, 14);

			mTextPaint.setTextSize(textSize * 0.75f);

			a.recycle();
		}

		mTextPaint.setAntiAlias(true);

		mDayLabels = context.getResources().getStringArray(mAbbreviateDays ? R.array.wc__abreviatted_days : R.array.wc__days);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if(mCellWidth != 0 && mCellHeight != 0 && mDays != 0) {
			setMeasuredDimension((int) (mOffsetX + (mCellWidth * (mDays + 1))), (int) mCellHeight);
			return;
		}

		int width, height;

		int suggestedWidth = MeasureSpec.getSize(widthMeasureSpec);
		int suggestedHeight = MeasureSpec.getSize(heightMeasureSpec);

		int heightMode = MeasureSpec.getMode(heightMeasureSpec);

		// Not matter, i want the maximum width
		if(Build.VERSION.SDK_INT >= 16) {
			width = Math.max(getMinimumWidth(), suggestedWidth);
		} else {
			width = suggestedWidth;
		}

		if(heightMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.AT_MOST) {
			if(Build.VERSION.SDK_INT >= 16) {
				height = Math.max(getMinimumHeight(), suggestedHeight);
			} else {
				height = suggestedHeight;
			}
		} else {
			WidthHeight textBounds = TextHelper.measureText("MM", mTextPaint); // Mede o maior texto de duas letras

			float hoursTextHeight = textBounds.height;

			height = (int) (hoursTextHeight * WeekCalendarView.CELL_HEIGHT_MULTIPLIER);
		}

		setMeasuredDimension(width, height);
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
