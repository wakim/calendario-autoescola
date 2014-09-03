package br.com.wakim.weekcalendarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;

/**
 * Created by wakim on 01/09/14.
 */
public class WeekCalendarView extends View implements GestureDetector.OnGestureListener {

	static final int CELL_HEIGHT_MULTIPLIER = 5;
	static final float FIRST_CELL_WIDTH_MULTIPLIER = 1.5f;

	DateTime mHighlightedDate, mBaseDate, mStartDate, mEndDate;
	Map<DateTime, Event> mEvents;

	int mStartHour, mEndHour;
	int mHoursCount = -1, mDayCount= - 1;

	boolean inRequestLayout = false, mHeaderRequestedLayout = false;

	TextPaint mTextPaint = new TextPaint();
	TextPaint mHoursPaint = new TextPaint();
	TextPaint mStripePaint = new TextPaint();

	Paint mLinePaint = new TextPaint();

	float mHoursTextWidth, mHoursTextHeight;
	float mFirstCellWidth, mCellWidth, mCellHeight;

	Typeface mTypeFace;

	OnDateClickListener mClickListener;
	OnDateLongClickListener mLongClickListener;

	GestureDetectorCompat mGestureDetector;

	WeekCalendarHeaderView mHeader;

	private static final String PARENT_STATE = "WC_PARENT_STATE",
								START_HOUR = "START_HOUR",
								END_HOUR = "END_HOUR",
								START_DATE = "START_DATE",
								END_DATE = "END_DATE",
								STRIPE_COLOR = "STRIPE_COLOR",
								LINE_COLOR = "LINE_COLOR",
								TEXT_SIZE = "TEXT_SIZE",
								EVENTS = "EVENTS";


	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		mHoursPaint = mTextPaint = mStripePaint = null;
		mEvents = null;
		mHighlightedDate = mBaseDate = mStartDate = null;
		mTypeFace = null;

		mClickListener = null;
		mLongClickListener = null;
		mGestureDetector = null;

		mHeader = null;
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();

		Bundle state = new Bundle();
		TimeZone tz = TimeZone.getDefault();

		state.putParcelable(PARENT_STATE, superState);
		state.putInt(START_HOUR, mStartHour);
		state.putInt(END_HOUR, mEndHour);
		state.putLong(START_DATE, mStartDate.getMilliseconds(tz));
		state.putLong(END_DATE, mEndDate.getMilliseconds(tz));
		state.putInt(STRIPE_COLOR, mStripePaint.getColor());
		state.putInt(LINE_COLOR, mLinePaint.getColor());
		state.putFloat(TEXT_SIZE, mHoursPaint.getTextSize());

		state.putParcelableArrayList(EVENTS, new ArrayList<Event>(mEvents.values()));

		return state;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle savedState = (Bundle) state;

		Parcelable superState = savedState.getParcelable(PARENT_STATE);
		super.onRestoreInstanceState(superState);

		TimeZone tz = TimeZone.getDefault();

		mStartHour = savedState.getInt(START_HOUR);
		mEndHour = savedState.getInt(END_HOUR);

		mHoursCount = (mEndHour - mStartHour) + 1;

		calculateDaysCount();

		mStartDate = DateTime.forInstant(savedState.getLong(START_DATE), tz);
		mEndDate = DateTime.forInstant(savedState.getLong(END_DATE), tz);

		mStripePaint.setColor(savedState.getInt(STRIPE_COLOR));
		mLinePaint.setColor(savedState.getInt(LINE_COLOR));

		mHoursPaint.setTextSize(savedState.getFloat(TEXT_SIZE));
		mTextPaint.setTextSize(mHoursPaint.getTextSize() * 0.75f);

		ArrayList<Event> events = savedState.getParcelableArrayList(EVENTS);

		mEvents = new HashMap<DateTime, Event>();

		for(Event event : events) {
			mEvents.put(event.getDate(), event);
		}
	}

	public WeekCalendarView(Context context) {
		super(context);
		init(context, null, 0);
	}

	public WeekCalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public WeekCalendarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	void init(Context context, AttributeSet attrs, int defStyle) {

		if(attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WeekCalendarView, defStyle, 0);

			mStartHour = a.getInt(R.styleable.WeekCalendarView_startHour, 0);
			mEndHour = a.getInt(R.styleable.WeekCalendarView_endHour, 23);

			mStripePaint.setColor(a.getColor(R.styleable.WeekCalendarView_stripe_color, 0xFFEEEEEE));
			mLinePaint.setColor(a.getColor(R.styleable.WeekCalendarView_line_color, 0xFF000000));

			mHoursCount = (mEndHour - mStartHour) + 1;

			float textSize = a.getDimensionPixelSize(R.styleable.WeekCalendarView_font_size, 14);

			mTextPaint.setTextSize(textSize * 0.75f);
			mHoursPaint.setTextSize(textSize);

			a.recycle();
		}

		mHoursPaint.setTextAlign(Paint.Align.RIGHT);

		mHoursPaint.setAntiAlias(true);
		mTextPaint.setAntiAlias(true);

		mGestureDetector = new GestureDetectorCompat(context, this);

		configureHeaderValues();

		// Not drawing now, waiting for dates
		setWillNotDraw(true);
	}

	@Override
	public void requestLayout() {
		inRequestLayout = true;
		super.requestLayout();
		inRequestLayout = false;
	}

	public void setBaseDate(DateTime baseDate) {
		mBaseDate = baseDate;

		// Not need to recalculate size, just redraw
		invalidate();
	}

	public void setStartDate(DateTime startDate) {
		mStartDate = startDate;
		calculateDaysCount();

		requestLayout();
	}

	public void setEndDate(DateTime endDate) {
		mEndDate = endDate;
		calculateDaysCount();

		requestLayout();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int mWidth = 0, mHeight = 0;

		int suggestedWidth = MeasureSpec.getSize(widthMeasureSpec);
		int suggestedHeight = MeasureSpec.getSize(heightMeasureSpec);

		int heightMode = MeasureSpec.getMode(heightMeasureSpec);

		// Not matter, i want the maximum width
		if(Build.VERSION.SDK_INT >= 16) {
			mWidth = Math.max(getMinimumWidth(), suggestedWidth);
		} else {
			mWidth = suggestedWidth;
		}

		if(heightMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.AT_MOST) {
			if(Build.VERSION.SDK_INT >= 16) {
				mHeight = Math.max(getMinimumHeight(), suggestedHeight);
			} else {
				mHeight = suggestedHeight;
			}
		} else {
			WidthHeight textBounds = TextHelper.measureText("MM", mHoursPaint); // Mede o maior texto de duas letras

			mHoursTextWidth = textBounds.width;
			mHoursTextHeight = textBounds.height;

			mHeight = (int) ((mHoursTextHeight * CELL_HEIGHT_MULTIPLIER) * (mHoursCount + 1)); // +1 para o cabecalho
		}

		calculateCellSizes(mWidth, mHeight);
		setMeasuredDimension(mWidth, mHeight);

		// As dimensoes ja foram medidas...
		configureHeaderValues();
	}

	void calculateDaysCount() {
		if(mStartDate == null || mEndDate == null) {
			return;
		}

		mDayCount = mStartDate.numDaysFrom(mEndDate) + 1;
		setWillNotDraw(false);
	}

	void calculateCellSizes(float width, float height) {
		mFirstCellWidth = (mHoursTextWidth * FIRST_CELL_WIDTH_MULTIPLIER);
		mCellHeight = height / mHoursCount;
		mCellWidth = (width - mFirstCellWidth) / getColumnsCount();
	}

	int getColumnsCount() {
		return mDayCount;
	}

	int getRowsCount() {
		return mHoursCount;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if(mBaseDate == null || mStartDate == null || mEndDate == null) {
			throw new RuntimeException("Suply all dates to calendar");
		}

		float width = canvas.getWidth();
		float height = canvas.getHeight();

		// Has an highlighted cell to draw?
		boolean hasEventForHighlightedDate = mHighlightedDate != null && mEvents != null && mEvents.containsKey(mHighlightedDate);

		drawHorizontalLinesWithHours(canvas, getRowsCount(), mCellHeight, mFirstCellWidth, width);
		drawVerticalLinesWithDates(canvas, mFirstCellWidth, getColumnsCount(), mCellWidth, mCellHeight, height);

		//Event is highlighted?
		if(hasEventForHighlightedDate) {
			drawEvents(canvas, mFirstCellWidth, getYOffset(), mCellWidth, mCellHeight, true);
		} else {
			//Draw normal event
			drawEvents(canvas, mFirstCellWidth, getYOffset(), mCellWidth, mCellHeight, false);

			// Is an empty cell highlighted?
			if(mHighlightedDate != null) {
				drawHighlightedCell(canvas, mHighlightedDate, mFirstCellWidth, mCellHeight, mCellWidth, mCellHeight);
			}
		}
	}

	void drawHighlightedCell(Canvas canvas, DateTime date, float xOffset, float yOffset, float cellWidth, float cellHeight) {
		XY position = getPositionForDate(date, xOffset, yOffset, cellWidth, cellHeight);
		int hourOffset = date.getHour() - mStartHour;
		int originalColor = mStripePaint.getColor();

		// Should stripe?
		if(hourOffset % 2 == 1) {
			mStripePaint.setColor(ColorHelper.darkenColor(originalColor, 0.5f));
		} else {
			mStripePaint.setColor(ColorHelper.darkenColor(0xFFFFFFFF, 0.5f));
		}

		canvas.drawRect(position.x, position.y, position.x + cellWidth, position.y + cellHeight, mStripePaint);

		mStripePaint.setColor(originalColor);
	}

	float getYOffset() {
		return 0;
	}

	void drawHorizontalLinesWithHours(Canvas canvas, int horizontalCellsCount, float cellHeight, float firstColumnWidth, float canvasWidth) {
		String hourText;
		int hour = mStartHour;

		float y = getYOffset();
		float textOffsetY = cellHeight * 0.25f;
		float textOffsetX = firstColumnWidth * 0.9f;

		for(int i = 0; i < horizontalCellsCount; ++i, y+= cellHeight, ++hour) {
			hourText = (hour < 10 ? "0" : "") + hour;

			canvas.drawLine(0, y, canvasWidth, y, mLinePaint);
			canvas.drawText(hourText, textOffsetX, y + textOffsetY, mHoursPaint);

			if(i % 2 == 1) {
				canvas.drawRect(firstColumnWidth, y + 1, canvasWidth, y + cellHeight - 1, mStripePaint);
			}
		}
	}

	boolean drawEvents(Canvas canvas, float xOffset, float yOffset, float cellWidth, float cellHeight, boolean hasHighlitedEvent) {

		if(mEvents == null || mEvents.isEmpty()) {
			return false;
		}

		TextPaint eventBackgroundPaint = new TextPaint();
		TextPaint eventTextPaint = new TextPaint();

		float originalTextSize = mTextPaint.getTextSize();

		boolean foundedEvent = false;

		eventTextPaint.setColor(0xFFFFFFFF);
		eventTextPaint.setAntiAlias(true);
		eventTextPaint.setTextSize(originalTextSize);

		if(mTypeFace != null) {
			eventTextPaint.setTypeface(mTypeFace);
		}

		for(DateTime date : mEvents.keySet()) {
			if(! date.gteq(mStartDate) || ! date.lteq(mEndDate)) {
				continue;
			}

			Event event = mEvents.get(date);
			String text = event.getSymbol();

			XY position = getPositionForDate(date, xOffset, yOffset, cellWidth, cellHeight);

			if(hasHighlitedEvent && date.equals(mHighlightedDate)) {
				foundedEvent = true;
				hasHighlitedEvent = false;
				eventBackgroundPaint.setColor(ColorHelper.darkenColor(event.getColor(), 0.5f));
			} else {
				eventBackgroundPaint.setColor(event.getColor());
			}

			TextHelper.configureMaxTextSizeForBounds(text, eventTextPaint, cellWidth, cellHeight);

			canvas.drawRect(position.x, position.y, position.x + cellWidth, position.y + cellHeight, eventBackgroundPaint);
			TextHelper.drawText(text, canvas, eventTextPaint, position.x, position.y, cellWidth, cellHeight);

			eventTextPaint.setTextSize(originalTextSize);
		}

		return foundedEvent;
	}

	XY getPositionForDate(DateTime date, float xOffset, float yOffset, float cellWidth, float cellHeight) {
		XY position = new XY();

		int hourOffset = date.getHour() - mStartHour;
		int dayOffset = mStartDate.numDaysFrom(date);

		position.x = xOffset + (dayOffset * cellWidth);
		position.y = (hourOffset * cellHeight);

		return position;
	}

	void drawVerticalLinesWithDates(Canvas canvas, float firstColumnWidth, int verticalCellsCount, float cellWidth, float cellHeight, float canvasHeight) {
		float originalTextSize = mTextPaint.getTextSize();
		float x = firstColumnWidth; // Ignorando a primeira celula

		for(int i = 0; i < verticalCellsCount; ++i, x += cellWidth) {
			canvas.drawLine(x, 0, x, canvasHeight, mLinePaint);//cellHeight, x, canvasHeight, mLinePaint);
		}

		mTextPaint.setTextSize(originalTextSize);
	}

	DateTime getDateForPosition(float x, float y) {
		float yOffset = getYOffset();

		if(x < mFirstCellWidth || y < yOffset) {
			return null;
		}

		x -= mFirstCellWidth;
		y -= yOffset;

		int dayOffset = (int) (x / mCellWidth), hourOffset = (int) (y / mCellHeight);

		return mStartDate.plus(0, 0, dayOffset, hourOffset, 0, 0, 0, DateTime.DayOverflow.Spillover);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// IGNORED
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		if(mClickListener != null) {
			DateTime date = getDateForPosition(e.getX(), e.getY());
			mClickListener.onDateClicked(date);
		}

		return true;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		// IGNORED
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		if(mLongClickListener != null) {
			DateTime date = getDateForPosition(e.getX(), e.getY());
			mLongClickListener.onDateLongClicked(date);
		}
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		// IGNORED
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		DateTime date = getDateForPosition(event.getX(), event.getY());

		if(date == null) {
			return super.onTouchEvent(event);
		}

		int actionEvent = event.getAction();

		switch (actionEvent) {
			case MotionEvent.ACTION_DOWN:
				mHighlightedDate = date;
				invalidate();
				break;
			case MotionEvent.ACTION_UP:
				mHighlightedDate = null;
				invalidate();
				break;
			case MotionEvent.ACTION_CANCEL:
				mHighlightedDate = null;
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				if(mHighlightedDate != null) {
					mHighlightedDate = null;
					invalidate();
				}
				break;
		}

		return mGestureDetector.onTouchEvent(event);
	}

	public XY getDatePosition(DateTime date) {
		return getPositionForDate(date, mFirstCellWidth, mCellHeight, mCellWidth, mCellHeight);
	}

	public void setStartHour(int startHour) {
		mStartHour = startHour;

		mHoursCount = (mEndHour - mStartHour) + 1;

		resetHeaderRequestFlag();
		configureHeaderValues();

		// Need to recalculate height
		requestLayout();
	}

	public void setEndHour(int endHour) {
		mEndHour = endHour;

		mHoursCount = (mEndHour - mStartHour) + 1;

		resetHeaderRequestFlag();
		configureHeaderValues();

		requestLayout();
	}

	public void setLineColor(int color) {
		mLinePaint.setColor(color);

		invalidate();
	}

	public void setStripeColor(int color) {
		mStripePaint.setColor(color);

		invalidate();
	}

	public void setEvents(Map<DateTime, Event> events) {
		mEvents = events;

		invalidate();
	}

	public void setOnDateClickListener(OnDateClickListener onDateClickListener) {
		mClickListener = onDateClickListener;
	}

	public void setOnDateLongClickListener(OnDateLongClickListener onDateLongClickListener) {
		mLongClickListener = onDateLongClickListener;
	}

	public void setTypeface(Typeface typeface) {
		mTypeFace = typeface;

		mTextPaint.setTypeface(mTypeFace);
		mHoursPaint.setTypeface(mTypeFace);

		resetHeaderRequestFlag();
		configureHeaderValues();

		requestLayout();
	}

	public void setHeader(WeekCalendarHeaderView header) {
		mHeader = header;

		configureHeaderValues();
	}

	void configureHeaderValues() {

		if(mHeader == null) {
			return;
		}

		mHeader.setStartDate(mStartDate);
		mHeader.setDays(getColumnsCount());
		mHeader.setTextSize(mTextPaint.getTextSize());
		mHeader.setTypeface(mTypeFace);
		mHeader.setCellHeight(mCellHeight);
		mHeader.setCellWidth(mCellWidth);
		mHeader.setOffsetX(mFirstCellWidth);
		mHeader.setTextWidthLimit(mFirstCellWidth * 3f);

		if(! mHeaderRequestedLayout) {
			mHeader.requestLayout();

			mHeaderRequestedLayout = true;
		}
	}

	void resetHeaderRequestFlag() {
		mHeaderRequestedLayout = false;
	}

	public void clearEvents() {
		if(mEvents != null) {
			mEvents = new HashMap<DateTime, Event>();
		}
	}

	public static interface OnDateClickListener {
		public void onDateClicked(DateTime date);
	}

	public static interface OnDateLongClickListener {
		public void onDateLongClicked(DateTime date);
	}
}
