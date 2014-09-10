package br.com.wakim.weekcalendarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import br.com.wakim.weekcalendarview.listener.OnDateClickListener;
import br.com.wakim.weekcalendarview.listener.OnDateLongClickListener;
import br.com.wakim.weekcalendarview.utils.ColorHelper;
import br.com.wakim.weekcalendarview.utils.TextHelper;
import br.com.wakim.weekcalendarview.utils.WidthHeight;
import br.com.wakim.weekcalendarview.utils.XY;
import hirondelle.date4j.DateTime;

/**
 * Created by wakim on 01/09/14.
 */
public class WeekCalendarView extends View implements GestureDetector.OnGestureListener, DraggableManager.OnDropAnimationListener {

	static final int CELL_HEIGHT_MULTIPLIER = 5;
	static final float FIRST_CELL_WIDTH_MULTIPLIER = 1.5f;

	DateTime mHighlightedDate, mBaseDate, mStartDate, mEndDate;

	Map<DateTime, Event> mEvents;

	int mStartHour, mEndHour;
	int mHoursCount = -1, mDayCount= - 1;

	boolean mHeaderRequestedLayout = false;
	boolean mDragEnabled = false;

	boolean mIsDragging = false;

	TextPaint mTextPaint = new TextPaint();
	TextPaint mHoursPaint = new TextPaint();

	Paint mStripePaint = new Paint();
	Paint mLinePaint = new Paint();

	float mHoursTextWidth, mHoursTextHeight;
	float mFirstCellWidth, mCellWidth, mCellHeight;

	Typeface mTypeFace;

	OnDateClickListener mClickListener;
	OnDateLongClickListener mLongClickListener;
	br.com.wakim.weekcalendarview.listener.OnDragListener mDragListener;

	GestureDetectorCompat mGestureDetector;

	DraggableManager mDraggableManager = new DraggableManager();

	WeekCalendarHeaderView mHeader;

	private static final String PARENT_STATE = "WC_PARENT_STATE",
								START_HOUR = "WC_START_HOUR",
								END_HOUR = "WC_END_HOUR",
								BASE_DATE = "WC_BASE_DATE",
								START_DATE = "WC_START_DATE",
								END_DATE = "WC_END_DATE",
								STRIPE_COLOR = "WC_STRIPE_COLOR",
								LINE_COLOR = "WC_LINE_COLOR",
								TEXT_SIZE = "WC_TEXT_SIZE",
								EVENTS = "WC_EVENTS";

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		TextHelper.sSizeCache.clear();
		mDraggableManager.destroy();

		mHoursPaint = mTextPaint = null;
		mStripePaint = mLinePaint = null;

		mEvents = null;
		mHighlightedDate = mBaseDate = mStartDate = null;
		mTypeFace = null;

		mClickListener = null;
		mLongClickListener = null;
		mDragListener = null;

		mGestureDetector = null;

		mHeader = null;
		mDraggableManager = null;
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();

		Bundle state = new Bundle();
		TimeZone tz = TimeZone.getDefault();

		state.putParcelable(PARENT_STATE, superState);
		state.putInt(START_HOUR, mStartHour);
		state.putInt(END_HOUR, mEndHour);
		state.putLong(BASE_DATE, mBaseDate.getMilliseconds(tz));
		state.putLong(START_DATE, mStartDate.getMilliseconds(tz));
		state.putLong(END_DATE, mEndDate.getMilliseconds(tz));
		state.putInt(STRIPE_COLOR, mStripePaint.getColor());
		state.putInt(LINE_COLOR, mLinePaint.getColor());
		state.putFloat(TEXT_SIZE, mHoursPaint.getTextSize());

		Event e = mDraggableManager.popEvent();

		if(e != null) {
			mEvents.put(e.getDate(), e);
		}

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

		mBaseDate = DateTime.forInstant(savedState.getLong(BASE_DATE), tz);
		mStartDate = DateTime.forInstant(savedState.getLong(START_DATE), tz);
		mEndDate = DateTime.forInstant(savedState.getLong(END_DATE), tz);

		mStripePaint.setColor(savedState.getInt(STRIPE_COLOR));
		mLinePaint.setColor(savedState.getInt(LINE_COLOR));

		mHoursPaint.setTextSize(savedState.getFloat(TEXT_SIZE));
		mTextPaint.setTextSize(mHoursPaint.getTextSize() * 0.75f);

		mDraggableManager.setTextSize(mTextPaint.getTextSize());

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

			mDragEnabled = a.getBoolean(R.styleable.WeekCalendarView_drag_enabled, false);

			calculateHoursCount();

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
		checkIfWillNotDraw();
	}

	public void requestLayout() {
		// If some setter call requestLayout before its get measured, so this call can be rejected.
		if(getMeasuredWidth() != 0) {
			super.requestLayout();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = 0, height = 0;

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
			WidthHeight textBounds = TextHelper.measureText("MM", mHoursPaint); // Mede o maior texto de duas letras

			mHoursTextWidth = textBounds.width;
			mHoursTextHeight = textBounds.height;

			height = (int) ((mHoursTextHeight * CELL_HEIGHT_MULTIPLIER) * (mHoursCount + 1)); // +1 para o cabecalho
		}

		calculateCellSizes(width, height);
		setMeasuredDimension(width, height);

		// Dimensions already measured, configure Header Cell Width, and Cell Height.
		configureHeaderValues();
		checkIfWillNotDraw();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		setHapticFeedbackEnabled(true);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		Rect rect = new Rect();

		getGlobalVisibleRect(rect);

		mDraggableManager
				.setTop(top)
				.setBottom(bottom)
				.setVisibleHeight(rect.height());
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

		mDraggableManager.onDraw(canvas);
	}

	void drawHighlightedCell(Canvas canvas, DateTime date, float xOffset, float yOffset, float cellWidth, float cellHeight) {
		XY position = getPositionForDate(date, xOffset, yOffset, cellWidth, cellHeight);
		int hourOffset = date.getHour() - mStartHour;
		int originalColor = mStripePaint.getColor();

		// Should stripe?
		if(hourOffset % 2 == 1) {
			mStripePaint.setColor(ColorHelper.darkenColor(originalColor, 0.5f));
		} else {
			mStripePaint.setColor(ColorHelper.darkenColor(0xFFFFFFFF, 0.75f));
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

			if(i % 2 == 1) {
				canvas.drawRect(0, y + 1, canvasWidth, y + cellHeight - 1, mStripePaint);
			}

			canvas.drawLine(0, y, canvasWidth, y, mLinePaint);
			canvas.drawText(hourText, textOffsetX, y + textOffsetY, mHoursPaint);
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

			String cacheKey = TextHelper.getCacheKey(text, cellWidth, cellHeight);

			if(TextHelper.sSizeCache.containsKey(cacheKey)) {
				eventTextPaint.setTextSize(TextHelper.sSizeCache.get(cacheKey));
			} else {
				TextHelper.configureMaxTextSizeForBounds(text, eventTextPaint, cellWidth, cellHeight);
				TextHelper.sSizeCache.put(cacheKey, eventTextPaint.getTextSize());
			}

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
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		if(mClickListener != null) {
			DateTime date = getDateForPosition(e.getX(), e.getY());
			Event event = mEvents.get(date);

			if(event == null) {
				mClickListener.onDateClicked(date);
			} else {
				mClickListener.onDateClicked(date, event);
			}
		}

		// Apenas para eventos de acessibilidade
		performClickAccessibilityAndFeedback();

		return true;
	}

	void performClickAccessibilityAndFeedback() {
		sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);
		playSoundEffect(SoundEffectConstants.CLICK);
	}

	void startDrag(DateTime date, float x, float y) {
		Event event;

		if((event = mEvents.get(date)) == null) {
			return;
		}

		if(mDragListener != null && ! mDragListener.onStartDrag(event)) {
			return;
		}

		mIsDragging = true;
		getParent().requestDisallowInterceptTouchEvent(true);

		event = mEvents.remove(date);
		XY cellPosition = getPositionForDate(date, mFirstCellWidth, getYOffset(), mCellWidth, mCellHeight);

		// Possible previous event (probably from DropAction.WAIT without future action)
		if(mDraggableManager.hasEvent()) {
			Event previousEvent = mDraggableManager.popEvent();
			mEvents.put(previousEvent.getDate(), previousEvent);
		}

		mDraggableManager
			.setEvent(event)
			.setX(x, cellPosition.x)
			.setY(y, cellPosition.y);

		mHighlightedDate = null;

		invalidate();
	}

	@Override
	public void onLongPress(MotionEvent e) {
		DateTime date = getDateForPosition(e.getX(), e.getY());

		if(! mDragEnabled) {
			if(mLongClickListener != null) {
				Event event = mEvents.get(date);

				if(event == null) {
					mLongClickListener.onDateLongClicked(date);
				} else {
					mLongClickListener.onDateLongClicked(date, event);
				}
			}
		} else {
			startDrag(date, e.getX(), e.getY());
		}

		performLongClickAccessibilityAndFeedback();
	}

	void performLongClickAccessibilityAndFeedback() {
		sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_LONG_CLICKED);
		performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING | HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		// IGNORED
		return true;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		// IGNORED
		return true;
	}

	void revertDropEvent(Event event) {
		DateTime dropTarget = event.getDate();
		mDraggableManager.animateDrop(this, this, getPositionForDate(dropTarget, mFirstCellWidth, getYOffset(), mCellWidth, mCellHeight));
	}

	void startDrop(float x, float y) {
		if(mIsDragging && mDraggableManager.hasEvent()) {
			DateTime dropTarget = getDateForPosition(x, y);
			Event event = mDraggableManager.getEvent();

			mIsDragging = false;
			getParent().requestDisallowInterceptTouchEvent(false);

			// Ok, invalid drop (first column or out of bounds)
			if(dropTarget == null) {
				revertDropEvent(event);
				return;
			}

			// We have a listener?
			if(mDragListener != null) {
				Event dropEvent = mEvents.get(dropTarget);

				// Dropping in a empty cell and the listener refuse...
				// Immediate revert
				if(dropEvent == null && ! mDragListener.onStopDrag(event, dropTarget)) {
					revertDropEvent(event);
					return;
				} else if(dropEvent != null) {
					DropAction action = mDragListener.onStopDrag(event, dropEvent);

					// If the listener wants an immediate revert, lets do it
					if(action == DropAction.REVERT) {
						revertDropEvent(event);
					}

					// If the listener wants to wait for revert or commit an drop in future.
					// nothing is done now.

					return;
				}
			}

			// No Listener, so we can commit the drop
			// Or the listener accepted to replace an existing event
			event = mDraggableManager.popEvent();
			event.setDate(dropTarget);

			mEvents.put(dropTarget, event);
		}
	}

	public void revertEventDrop(Event event) {
		DateTime dropTarget = event.getDate();
		mDraggableManager.animateDrop(this, this, getPositionForDate(dropTarget, mFirstCellWidth, getYOffset(), mCellWidth, mCellHeight));
	}

	public void commitEventDrop(Event event) {
		DateTime dropTarget = event.getDate();

		mDraggableManager.popEvent();
		mEvents.put(dropTarget, event);

		invalidate();
	}

	@Override
	public void onAnimationEnd(Event event) {
		mEvents.put(event.getDate(), event);
		invalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int actionEvent = event.getAction();

		switch (actionEvent) {
			case MotionEvent.ACTION_DOWN:

				DateTime date = getDateForPosition(event.getX(), event.getY());

				if(date != null) {
					mHighlightedDate = date;
					invalidate();
				}

				break;
			case MotionEvent.ACTION_UP:

				startDrop(event.getX(), event.getY());

				mHighlightedDate = null;

				// Redraw View
				invalidate();

				break;
			case MotionEvent.ACTION_CANCEL:

				startDrop(event.getX(), event.getY());

				mHighlightedDate = null;

				// Redraw View
				invalidate();

				break;
			case MotionEvent.ACTION_MOVE:

				if (mIsDragging) {

					mDraggableManager
						.setX(event.getX())
						.setY(event.getY());

					mDraggableManager.scrollIfNeeded((android.view.ViewGroup) getParent());
				}

				if (mHighlightedDate != null) {
					mHighlightedDate = null;
				}

				// Redraw View
				invalidate();

				break;
		}

		return mGestureDetector.onTouchEvent(event);
	}

	public XY getDatePosition(DateTime date) {
		return getPositionForDate(date, mFirstCellWidth, mCellHeight, mCellWidth, mCellHeight);
	}

	//
	// Getters and Setters
	//

	public void setDragEnabled(boolean dragEnabled) {
		mDragEnabled = dragEnabled;
	}

	public void setBaseDate(DateTime baseDate) {
		mBaseDate = baseDate;

		// Not need to recalculate size, just redraw
		invalidate();
	}

	public void setDates(DateTime baseDate, DateTime startDate, DateTime endDate) {

		if(mBaseDate != null && mBaseDate.equals(baseDate) &&
			mStartDate != null && mStartDate.equals(startDate) &&
			mEndDate != null && mEndDate.equals(endDate)) {

			return;
		}

		mBaseDate = baseDate;
		mStartDate = startDate;
		mEndDate = endDate;

		calculateDaysCount();

		resetHeaderRequestFlag();
		configureHeaderValues();

		requestLayout();
	}

	public void setStartDate(DateTime startDate) {
		mStartDate = startDate;

		calculateDaysCount();

		resetHeaderRequestFlag();
		configureHeaderValues();

		requestLayout();
	}

	public void setEndDate(DateTime endDate) {
		mEndDate = endDate;

		calculateDaysCount();

		resetHeaderRequestFlag();
		configureHeaderValues();

		requestLayout();
	}

	public void setStartHour(int startHour) {
		mStartHour = startHour;

		calculateHoursCount();

		resetHeaderRequestFlag();

		// Need to recalculate height
		requestLayout();
	}

	public void setEndHour(int endHour) {
		mEndHour = endHour;

		calculateHoursCount();

		resetHeaderRequestFlag();

		// Need to recalculate height
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

	public void addEvent(Event event) {
		mEvents.put(event.getDate(), event);
		invalidate();
	}

	public void setOnDateClickListener(OnDateClickListener onDateClickListener) {
		mClickListener = onDateClickListener;
	}

	public void setOnDateLongClickListener(OnDateLongClickListener onDateLongClickListener) {
		mLongClickListener = onDateLongClickListener;
	}

	public void setOnDragListener(br.com.wakim.weekcalendarview.listener.OnDragListener onDragListener) {
		mDragListener = onDragListener;
	}

	public void setTypeface(Typeface typeface) {
		mTypeFace = typeface;

		mTextPaint.setTypeface(mTypeFace);
		mHoursPaint.setTypeface(mTypeFace);

		mDraggableManager.setTypeface(mTypeFace);

		resetHeaderRequestFlag();
		configureHeaderValues();

		requestLayout();
	}

	public void setHeader(WeekCalendarHeaderView header) {
		mHeader = header;

		configureHeaderValues();
	}

	void configureHeaderValues() {

		if(mHeader == null || mStartDate == null) {
			return;
		}

		mHeader.setStartDate(mStartDate);
		mHeader.setDays(getColumnsCount());
		mHeader.setTextSize(mHoursPaint.getTextSize());
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

	@Override
	public void setOnClickListener(OnClickListener l) {
		throw new RuntimeException("Use setOnDateClickListener instead.");
	}

	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		throw new RuntimeException("Use setOnDateLongClickListener instead.");
	}

	void resetHeaderRequestFlag() {
		mHeaderRequestedLayout = false;
	}

	public void clearEvents() {
		if(mEvents != null) {
			mEvents = new HashMap<DateTime, Event>();
		}
	}

	void calculateDaysCount() {
		if(mStartDate == null || mEndDate == null) {
			return;
		}

		if(mEndDate.lt(mStartDate)) {
			throw new RuntimeException("End date must be greater than start date! (" + mStartDate.toString() + ", " + mEndDate.toString() + ")");
		}

		mDayCount = mStartDate.numDaysFrom(mEndDate) + 1;
		checkIfWillNotDraw();
	}

	void checkIfWillNotDraw() {
		boolean willNotDraw = mStartDate == null || mBaseDate == null || mEndDate == null;

		setWillNotDraw(willNotDraw);
	}

	void calculateHoursCount() {
		mHoursCount = (mEndHour - mStartHour) + 1;
	}

	void calculateCellSizes(float width, float height) {
		mFirstCellWidth = (mHoursTextWidth * FIRST_CELL_WIDTH_MULTIPLIER);
		mCellHeight = height / (mHoursCount + 1); // Cabecalho
		mCellWidth = (width - mFirstCellWidth) / getColumnsCount();

		mDraggableManager
			.setCellWidth(mCellWidth)
			.setCellHeight(mCellHeight);
	}

	int getColumnsCount() {
		return mDayCount;
	}

	int getRowsCount() {
		return mHoursCount;
	}
}
