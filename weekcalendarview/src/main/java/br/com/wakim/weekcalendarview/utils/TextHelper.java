package br.com.wakim.weekcalendarview.utils;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wakim on 02/09/14.
 */
public abstract class TextHelper {

	public static Map<String, Float> sSizeCache = new HashMap<String, Float>();

	public static String getCacheKey(String text, float cellWidth, float cellHeight) {
		return text.concat("w").concat(Float.toString(cellWidth)).concat("h").concat(Float.toString(cellHeight));
	}

	public static WidthHeight configureMaxTextSizeForBounds(String text, TextPaint textPaint, float width, float height) {
		float textWidth = 0f, textHeight = 0f; // Our calculated text bounds
		float textSize = textPaint.getTextSize();

		Rect textBounds = new Rect();

		height = height * 0.85f;

		while(textWidth <= width && textHeight <= height) {
			textPaint.setTextSize(textSize);

			// Now lets calculate the size of the text
			textPaint.getTextBounds(text, 0, text.length(), textBounds);

			textWidth = textPaint.measureText(text); // Use measureText to calculate width
			textHeight = textBounds.height(); // Use height from getTextBounds()

			textSize++;
		}

		textPaint.setTextSize(textSize * 0.75f);

		return measureText(text, textPaint);
	}

	public static WidthHeight measureText(String text, TextPaint textPaint) {
		float textWidth, textHeight; // Our calculated text bounds

		// Now lets calculate the size of the text
		Rect textBounds = new Rect();
		textPaint.getTextBounds(text, 0, text.length(), textBounds);

		textWidth = textPaint.measureText(text); // Use measureText to calculate width
		textHeight = textBounds.height(); // Use height from getTextBounds()

		WidthHeight wh = new WidthHeight(textWidth, textHeight);

		return wh;
	}

	public static void drawText(String text, Canvas canvas, TextPaint textPaint, float offsetX, float offsetY, float cellWidth, float cellHeight) {
		drawText(text, canvas, textPaint, offsetX, offsetY, cellWidth, cellHeight, Layout.Alignment.ALIGN_CENTER);
	}

	public static void drawText(String text, Canvas canvas, TextPaint textPaint, float offsetX, float offsetY, float cellWidth, float cellHeight, Layout.Alignment alignment) {
		StaticLayout staticLayout = new StaticLayout(text, textPaint, (int) cellWidth, alignment, 1.0f, 0.0f, false);

		canvas.save();
		canvas.translate(offsetX + (cellWidth - staticLayout.getWidth()) / 2f, offsetY + (cellHeight - staticLayout.getHeight()) / 2f);
		staticLayout.draw(canvas);
		canvas.restore();
	}
}
