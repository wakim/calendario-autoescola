package br.com.wakim.autoescola.calendario.app.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SharedPreferencesWrapper {
	private static Context mContext;
	private static SharedPreferences sRealSharedPreferences;
	
	public static void setContext(Context context) {
		mContext = context;
		sRealSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
	}
	
	private static SharedPreferences getSharedPreferences() {
		
		if(sRealSharedPreferences != null) {
			return sRealSharedPreferences;
		}
		
		if(mContext != null) {
			sRealSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		} else {
			sRealSharedPreferences = new NullSharedPreferences();
		}
		
		return sRealSharedPreferences;
	}
	
	public static Boolean getBoolean(String key) {
		return getSharedPreferences().getBoolean(key, false);
	}
	
	public static Boolean getBoolean(Object key) {
		return getSharedPreferences().getBoolean(key.toString(), false);
	}

	public static Boolean getBoolean(String key, boolean defaultValue) {
		return getSharedPreferences().getBoolean(key, defaultValue);
	}

	public static Boolean getBoolean(Object key, boolean defaultValue) {
		return getSharedPreferences().getBoolean(key.toString(), defaultValue);
	}
	
	public static boolean contains(String key) {
		return getSharedPreferences().contains(key);
	}
	
	public static boolean contains(Object key) {
		return getSharedPreferences().contains(key.toString());
	}
	
	public static String getString(String key) {
		return getSharedPreferences().getString(key, null);
	}
	
	public static String getString(Object key) {
		return getSharedPreferences().getString(key.toString(), null);
	}

	public static String getString(String key, String defaultValue) {
		return getSharedPreferences().getString(key, defaultValue);
	}

	public static String getString(Object key, String defaultValue) {
		return getSharedPreferences().getString(key.toString(), defaultValue);
	}
	
	public static Integer getInt(String key) {
		return getSharedPreferences().getInt(key, -1);
	}

	public static Integer getInt(String key, Integer defaultValue) {
		return getSharedPreferences().getInt(key, defaultValue);
	}
	
	public static Integer getInt(Object key) {
		return getSharedPreferences().getInt(key.toString(), -1);
	}

	public static Integer getInt(Object key, Integer defaultValue) {
		return getSharedPreferences().getInt(key.toString(), defaultValue);
	}
	
	public static Editor getEditor() {
		return getSharedPreferences().edit();
	}
	
	public static class NullSharedPreferences implements SharedPreferences {
		
		@Override
		public Map<String, ?> getAll() {
			return new HashMap<String, Object>();
		}

		@Override
		public String getString(String key, String defValue) {
			return defValue;
		}

		public Set<String> getStringSet(String key, Set<String> defValues) {
			return defValues;
		}

		@Override
		public int getInt(String key, int defValue) {
			return defValue;
		}

		@Override
		public long getLong(String key, long defValue) {
			return defValue;
		}

		@Override
		public float getFloat(String key, float defValue) {
			return defValue;
		}

		@Override
		public boolean getBoolean(String key, boolean defValue) {
			return defValue;
		}

		@Override
		public boolean contains(String key) {
			return false;
		}

		@Override
		public Editor edit() {
			return new Editor() {

				@Override
				public Editor putString(String key, String value) {
					return this;
				}


				public Editor putStringSet(String key, Set<String> values) {
					return this;
				}

				@Override
				public Editor putInt(String key, int value) {
					return this;
				}

				@Override
				public Editor putLong(String key, long value) {
					return this;
				}

				@Override
				public Editor putFloat(String key, float value) {
					return this;
				}

				@Override
				public Editor putBoolean(String key, boolean value) {
					return this;
				}

				@Override
				public Editor remove(String key) {
					return this;
				}

				@Override
				public Editor clear() {
					return this;
				}

				@Override
				public boolean commit() {
					return false;
				}

				@Override
				public void apply() {
				}
			
			};
		}

		@Override
		public void registerOnSharedPreferenceChangeListener(
				OnSharedPreferenceChangeListener listener) {}

		@Override
		public void unregisterOnSharedPreferenceChangeListener(
				OnSharedPreferenceChangeListener listener) {}
		
	}
}