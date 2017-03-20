/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.v4.preference;

import android.annotation.SuppressLint;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

@SuppressWarnings({
		"JavadocReference", "unused", "UnnecessaryLocalVariable", "UnnecessaryBoxing", "JavaDoc"
		, "WeakerAccess"
})
@SuppressLint("PrivateApi")
public class PreferenceManagerCompat {

	private static final String TAG = PreferenceManagerCompat.class.getSimpleName();

	/**
	 * Interface definition for a callback to be invoked when a {@link Preference} in the hierarchy rooted at this
	 * {@link PreferenceScreen} is clicked.
	 */
	interface OnPreferenceTreeClickListener {
		/**
		 * Called when a preference in the tree rooted at this {@link PreferenceScreen} has been clicked.
		 * 
		 * @param preferenceScreen
		 *            The {@link PreferenceScreen} that the preference is located in.
		 * @param preference
		 *            The preference that was clicked.
		 * @return Whether the click was handled.
		 */
		boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference);
	}

	static PreferenceManager newInstance(final Activity activity, final int firstRequestCode) {
		try {
			Constructor<PreferenceManager> c = PreferenceManager.class.getDeclaredConstructor(Activity.class, int.class);
			c.setAccessible(true);
			return c.newInstance(activity, firstRequestCode);
		} catch (Exception e) {
			Log.w(TAG, "Couldn't call constructor PreferenceManager by reflection", e);
		}
		return null;
	}

	/**
	 * Sets the owning preference fragment
	 */
	static void setFragment(final PreferenceManager manager, final PreferenceFragment fragment) {
		// stub
	}

	/**
	 * Sets the callback to be invoked when a {@link Preference} in the hierarchy rooted at this {@link PreferenceManager} is
	 * clicked.
	 * 
	 * @param listener
	 *            The callback to be invoked.
	 */
	static void setOnPreferenceTreeClickListener(final PreferenceManager manager, final OnPreferenceTreeClickListener listener) {
		try {
			Field onPreferenceTreeClickListener = PreferenceManager.class.getDeclaredField("mOnPreferenceTreeClickListener");
			onPreferenceTreeClickListener.setAccessible(true);
			if (listener != null) {
				Object proxy = Proxy.newProxyInstance(onPreferenceTreeClickListener.getType().getClassLoader(),
						new Class[] { onPreferenceTreeClickListener.getType() }, new InvocationHandler() {
							@Override
							public Object invoke(final Object proxy, final Method method, final Object[] args) {
								if (method.getName().equals("onPreferenceTreeClick")) {
									return Boolean.valueOf(listener.onPreferenceTreeClick((PreferenceScreen) args[0],
											(Preference) args[1]));
								} else {
									return null;
								}
							}
						});
				onPreferenceTreeClickListener.set(manager, proxy);
			} else {
				onPreferenceTreeClickListener.set(manager, null);
			}
		} catch (Exception e) {
			Log.w(TAG, "Couldn't set PreferenceManager.mOnPreferenceTreeClickListener by reflection", e);
		}
	}

	/**
	 * Inflates a preference hierarchy from the preference hierarchies of {@link Activity Activities} that match the given
	 * {@link Intent}. An {@link Activity} defines its preference hierarchy with meta-data using the
	 * {@link #METADATA_KEY_PREFERENCES} key.
	 * <p>
	 * If a preference hierarchy is given, the new preference hierarchies will be merged in.
	 * 
	 * @param queryIntent
	 *            The intent to match activities.
	 * @param rootPreferences
	 *            Optional existing hierarchy to merge the new hierarchies into.
	 * @return The root hierarchy (if one was not provided, the new hierarchy's root).
	 */
	static PreferenceScreen inflateFromIntent(final PreferenceManager manager, final Intent intent, final PreferenceScreen screen) {
		try {
			Method m = PreferenceManager.class.getDeclaredMethod("inflateFromIntent", Intent.class, PreferenceScreen.class);
			m.setAccessible(true);
			PreferenceScreen prefScreen = (PreferenceScreen) m.invoke(manager, intent, screen);
			return prefScreen;
		} catch (Exception e) {
			Log.w(TAG, "Couldn't call PreferenceManager.inflateFromIntent by reflection", e);
		}
		return null;
	}

	/**
	 * Inflates a preference hierarchy from XML. If a preference hierarchy is given, the new preference hierarchies will be merged
	 * in.
	 * 
	 * @param context
	 *            The context of the resource.
	 * @param resId
	 *            The resource ID of the XML to inflate.
	 * @param rootPreferences
	 *            Optional existing hierarchy to merge the new hierarchies into.
	 * @return The root hierarchy (if one was not provided, the new hierarchy's root).
	 * @hide
	 */
	static PreferenceScreen inflateFromResource(final PreferenceManager manager, final Activity activity, final int resId,
			final PreferenceScreen screen) {
		try {
			Method m = PreferenceManager.class.getDeclaredMethod("inflateFromResource", Context.class, int.class,
					PreferenceScreen.class);
			m.setAccessible(true);
			PreferenceScreen prefScreen = (PreferenceScreen) m.invoke(manager, activity, resId, screen);
			return prefScreen;
		} catch (Exception e) {
			Log.w(TAG, "Couldn't call PreferenceManager.inflateFromResource by reflection", e);
		}
		return null;
	}

	/**
	 * Returns the root of the preference hierarchy managed by this class.
	 * 
	 * @return The {@link PreferenceScreen} object that is at the root of the hierarchy.
	 */
	static PreferenceScreen getPreferenceScreen(final PreferenceManager manager) {
		try {
			Method m = PreferenceManager.class.getDeclaredMethod("getPreferenceScreen");
			m.setAccessible(true);
			return (PreferenceScreen) m.invoke(manager);
		} catch (Exception e) {
			Log.w(TAG, "Couldn't call PreferenceManager.getPreferenceScreen by reflection", e);
		}
		return null;
	}

	/**
	 * Called by the {@link PreferenceManager} to dispatch a subactivity result.
	 */
	static void dispatchActivityResult(final PreferenceManager manager, final int requestCode, final int resultCode,
			final Intent data) {
		try {
			Method m = PreferenceManager.class.getDeclaredMethod("dispatchActivityResult", int.class, int.class, Intent.class);
			m.setAccessible(true);
			m.invoke(manager, requestCode, resultCode, data);
		} catch (Exception e) {
			Log.w(TAG, "Couldn't call PreferenceManager.dispatchActivityResult by reflection", e);
		}
	}

	/**
	 * Called by the {@link PreferenceManager} to dispatch the activity stop event.
	 */
	static void dispatchActivityStop(final PreferenceManager manager) {
		try {
			Method m = PreferenceManager.class.getDeclaredMethod("dispatchActivityStop");
			m.setAccessible(true);
			m.invoke(manager);
		} catch (Exception e) {
			Log.w(TAG, "Couldn't call PreferenceManager.dispatchActivityStop by reflection", e);
		}
	}

	/**
	 * Called by the {@link PreferenceManager} to dispatch the activity destroy event.
	 */
	static void dispatchActivityDestroy(final PreferenceManager manager) {
		try {
			Method m = PreferenceManager.class.getDeclaredMethod("dispatchActivityDestroy");
			m.setAccessible(true);
			m.invoke(manager);
		} catch (Exception e) {
			Log.w(TAG, "Couldn't call PreferenceManager.dispatchActivityDestroy by reflection", e);
		}
	}

	/**
	 * Sets the root of the preference hierarchy.
	 * 
	 * @param preferenceScreen
	 *            The root {@link PreferenceScreen} of the preference hierarchy.
	 * @return Whether the {@link PreferenceScreen} given is different than the previous.
	 */
	static boolean setPreferences(final PreferenceManager manager, final PreferenceScreen screen) {
		try {
			Method m = PreferenceManager.class.getDeclaredMethod("setPreferences", PreferenceScreen.class);
			m.setAccessible(true);
			return (Boolean) m.invoke(manager, screen);
		} catch (Exception e) {
			Log.w(TAG, "Couldn't call PreferenceManager.setPreferences by reflection", e);
		}
		return false;
	}

}
