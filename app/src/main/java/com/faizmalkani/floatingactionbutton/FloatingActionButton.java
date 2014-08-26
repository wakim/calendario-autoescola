package com.faizmalkani.floatingactionbutton;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;

import br.com.wakim.autoescola.calendario.R;

public class FloatingActionButton extends View {

	private Paint mButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint mDrawablePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Bitmap mBitmap;
	private int mScreenHeight;
	private int mColor;
	private boolean mHidden = false;

	TranslateAnimation mBottomUpAnimation;
	LinearInterpolator mInterpolator = new LinearInterpolator();
	ReverseLinearInterpolator mReverseIntepolator = new ReverseLinearInterpolator();

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		mBottomUpAnimation = null;
		mInterpolator = null;
		mReverseIntepolator = null;

		mDrawablePaint = mButtonPaint = null;
	}

	public FloatingActionButton(Context context) {
		this(context, null);
	}

	public FloatingActionButton(Context context, AttributeSet attributeSet) {
		this(context, attributeSet, 0);
	}

	public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FloatingActionButton);
		float radius, dx, dy;
		int color = a.getInteger(R.styleable.FloatingActionButton_shadowColor, Color.argb(100, 0, 0, 0));
		Point size = getWindowSize(context);

		dx = a.getFloat(R.styleable.FloatingActionButton_shadowDx, 0.0f);
		dy = a.getFloat(R.styleable.FloatingActionButton_shadowDy, 3.5f);
		radius = a.getFloat(R.styleable.FloatingActionButton_shadowRadius, 10.0f);

		mButtonPaint.setShadowLayer(radius, dx, dy, color);
		mColor = a.getColor(R.styleable.FloatingActionButton_color, Color.WHITE);
		mButtonPaint.setStyle(Paint.Style.FILL);
		mButtonPaint.setColor(mColor);
		mScreenHeight = size.y;

		Drawable drawable = a.getDrawable(R.styleable.FloatingActionButton_drawable);

		if (null != drawable) {
			mBitmap = ((BitmapDrawable) drawable).getBitmap();
		}

		mBottomUpAnimation = new TranslateAnimation(0, 0, mScreenHeight, 0);

		mBottomUpAnimation.setFillAfter(true);
		mBottomUpAnimation.setDuration(500l);

		setWillNotDraw(false);

		if(Build.VERSION.SDK_INT >= 11) {
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
	}

	@SuppressWarnings("deprecation")
	@TargetApi(13)
	public static Point getWindowSize(Context context) {

		WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

		Display display = mWindowManager.getDefaultDisplay();

		if(Build.VERSION.SDK_INT >= 13) {
			Point p = new Point();
			display.getSize(p);

			return p;
		} else {
			return new Point(display.getWidth(), display.getHeight());
		}
	}

	public void setColor(int color) {
		mColor = color;
		mButtonPaint.setColor(mColor);

		invalidate();
	}

	public void setDrawable(Drawable drawable) {
		mBitmap = ((BitmapDrawable) drawable).getBitmap();
		invalidate();
	}


	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawCircle(getWidth() / 2, getHeight() / 2, (float) (getWidth() / 2.6), mButtonPaint);

		if (null != mBitmap && ! mBitmap.isRecycled()) {
			canvas.drawBitmap(mBitmap, (getWidth() - mBitmap.getWidth()) / 2,
				(getHeight() - mBitmap.getHeight()) / 2, mDrawablePaint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int color;

		if(! isClickable()) {
			return super.onTouchEvent(event);
		}

		if (event.getAction() == MotionEvent.ACTION_UP) {
			color = mColor;
			invalidate();
			mButtonPaint.setColor(color);
		} else if(event.getAction() == MotionEvent.ACTION_DOWN) {
			color = darkenColor(mColor);
			invalidate();
			mButtonPaint.setColor(color);
		}

		return super.onTouchEvent(event);
	}

	public void hide(boolean hide) {
		if (mHidden != hide) {

			mHidden = hide;

			mBottomUpAnimation.setInterpolator(mHidden ? mReverseIntepolator : mInterpolator);
			startAnimation(mBottomUpAnimation);
		}
	}

	public void listenTo(AbsListView listView) {
		if (null != listView) {
			listView.setOnScrollListener(new DirectionScrollListener(this));
		}
	}

	public static int darkenColor(int color) {
		float[] hsv = new float[3];

		Color.colorToHSV(color, hsv);
		hsv[2] *= 0.8f;

		return Color.HSVToColor(hsv);
	}
}
