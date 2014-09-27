package br.com.wakim.autoescola.calendario.app.model.task;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

/**
 * Created by wakim on 20/09/14.
 */
public class NotObserverCursorLoader extends CursorLoader {

	public NotObserverCursorLoader(Context context) {
		super(context);
	}

	public NotObserverCursorLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		super(context, uri, projection, selection, selectionArgs, sortOrder);
	}

	@Override
	public Cursor loadInBackground() {
		Cursor cursor = getContext().getContentResolver().query(getUri(), getProjection(), getSelection(), getSelectionArgs(), getSortOrder());

		if (cursor != null) {
			// Ensure the cursor window is filled
			cursor.getCount();
		}

		return cursor;
	}
}
