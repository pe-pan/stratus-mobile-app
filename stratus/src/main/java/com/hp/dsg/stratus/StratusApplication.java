package com.hp.dsg.stratus;

import android.app.Application;
import android.preference.PreferenceManager;

/**
 * Created by panuska on 3.4.2015.
 */
public class StratusApplication extends Application {
    @Override
    public void onCreate() {
        SubscriptionListActivity.enableHints(isEnabledPreference(SettingsActivity.KEY_PREF_SHOW_SWIPE_HINTS));
    }

    public boolean isEnabledPreference(String key) { // todo copy of StratusActivity.isEnabledPreference()
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(key, true); //this default value must correspond to default value in settings.xml
    }

}
