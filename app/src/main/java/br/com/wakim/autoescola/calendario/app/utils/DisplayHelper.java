package br.com.wakim.autoescola.calendario.app.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;

public class DisplayHelper {

	public static Point getAdjustedPortraitWindowSize(Activity activity) {
		Point windowSize = getPortraitWindowSize(activity);

		if(Build.VERSION.SDK_INT >= 19) {
			windowSize.y += getStatusBarHeight(activity);
		}

		return windowSize;
	}

	public static Point getPortraitWindowSize(Activity activity) {
		Point cloneSize = getWindowSize(activity);
		int tmp = cloneSize.x;
		
		if(activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			cloneSize.x = cloneSize.y;
			cloneSize.y = tmp;
		}
		
		return cloneSize;
	}
	
	public static Point scaleWithAspectRatio(double sourceWidth, double sourceHeight, double destWidth, double destHeight) {
		Point size = new Point();

		double scaleHeight = destHeight / sourceHeight;
		double scaleWidth = destWidth / sourceWidth;
		double scale = scaleHeight > scaleWidth ? scaleWidth : scaleHeight;
		
		size.set((int) (sourceWidth * scale), (int) (sourceHeight * scale));
		
		return size;
	}

	@SuppressWarnings("deprecation")
    @TargetApi(13)
	public static Point getWindowSize(Activity context) {
		Display display = context.getWindowManager().getDefaultDisplay();

		if(Build.VERSION.SDK_INT >= 13) {
			Point p = new Point();
			display.getSize(p);

			return p;
		} else {
			return new Point(display.getWidth(), display.getHeight());
		}
	}

	public static int getNavigationBarHeight(Context context) {
		Resources resources = context.getResources();
		int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");

		if (resourceId > 0) {
			return resources.getDimensionPixelSize(resourceId);
		}

		return 0;
	}

	public static int getStatusBarHeight(Context context) {
		int result = 0;
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");

		if (resourceId > 0) {
			result = context.getResources().getDimensionPixelSize(resourceId);
		}

		return result;
	}

	public static int getActionBarHeight(Context context) {
		int mActionBarSize = 0;

		final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(new int[]{android.R.attr.actionBarSize});
		mActionBarSize = (int) styledAttributes.getDimension(0, 0);

		styledAttributes.recycle();

		return mActionBarSize;
	}


}
