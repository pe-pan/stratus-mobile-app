package com.hp.dsg.stratus;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;

/**
 * Created by panuska on 11.3.2015.
 */
public class SettingsActivity extends PreferenceActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    public static final String KEY_PREF_FILTER_ACTIVE = "filter_active_subs";
    public static final String KEY_PREF_LOAD_IMAGES = "load_images";
    public static final String KEY_PREF_CACHE_FILES = "cache_files";
    public static final String KEY_PREF_CLEAR_CACHE = "clear_cache";
    public static final String KEY_PREF_KEEP_PASSWORD = "keep_password";
    public static final String KEY_PREF_LOGOUT_NOW = "logout_now";
    public static final String KEY_PREF_SHOW_SWIPE_HINTS = "show_swipe_hints";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment())
                .commit();

    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.settings);

            Preference clearCacheNow = findPreference(KEY_PREF_CLEAR_CACHE);
            updateCacheDirSize(clearCacheNow);
            clearCacheNow.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    PicassoTools.clearCache(Picasso.with(getActivity()));
                    updateCacheDirSize(preference);
                    return true;
                }
            });

            Preference logoutNow = findPreference(KEY_PREF_LOGOUT_NOW);
            logoutNow.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    SharedPreferences credentials = getActivity().getSharedPreferences(StratusActivity.AUTHENTICATION_FILE, MODE_PRIVATE);
                    SharedPreferences.Editor editor = credentials.edit();
                    editor.remove(StratusActivity.PASSWORD_PKEY);               // remove it for next app run
                    editor.remove(StratusActivity.AUTHENTICATION_TOKEN_PKEY);
                    editor.apply();
                    Mpp.M_STRATUS.setPassword(null);                            // and remove it also for this run
                    Mpp.M_STRATUS.setAuthenticationHeader(null);
                    Log.d(TAG, "Removing password and authentication token");
                    getActivity().finish();  // when logging out, close the settings activity
                    return true;
                }
            });

            Preference keepLoggedIn = findPreference(KEY_PREF_KEEP_PASSWORD);
            keepLoggedIn.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SharedPreferences credentials = getActivity().getSharedPreferences(StratusActivity.AUTHENTICATION_FILE, MODE_PRIVATE);
                    SharedPreferences.Editor editor = credentials.edit();
                    if ((Boolean)newValue) {
                        editor.putString(StratusActivity.PASSWORD_PKEY, Mpp.M_STRATUS.getPassword());
                    } else {
                        editor.remove(StratusActivity.PASSWORD_PKEY);   // does not remove the authentication token; it will be possible to re-use it for some time without logging in
                    }
                    editor.apply();
                    return true;
                }
            });

            Preference showHints = findPreference(KEY_PREF_SHOW_SWIPE_HINTS);
            showHints.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object enable) {
                    SubscriptionListActivity.enableHints((Boolean)enable);
                    return true;
                }
            });
        }

        private void updateCacheDirSize(Preference clearCacheNow) {
            clearCacheNow.setSummary(String.format(getString(R.string.clearCacheSummary), PicassoTools.getMemoryCacheSize(Picasso.with(getActivity()))/1024, PicassoTools.getDiskCacheSize(getActivity())/1024));
        }
    }
}
