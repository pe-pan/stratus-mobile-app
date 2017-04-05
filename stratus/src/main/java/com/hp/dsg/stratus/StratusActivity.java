package com.hp.dsg.stratus;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.Toast;

import com.hp.dsg.rest.AuthenticatedClient;
import com.hp.dsg.rest.IllegalRestStateException;
import com.hp.dsg.stratus.entities.Entity;
import com.hp.dsg.stratus.entities.MppCategory;
import com.hp.dsg.stratus.entities.MppCategoryHandler;
import com.hp.dsg.stratus.entities.MppSubscriptionHandler;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang.exception.ExceptionUtils;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLHandshakeException;

import static com.hp.dsg.stratus.Mpp.M_STRATUS;
/**
 * Created by panuska on 24.2.2015.
 */
public class StratusActivity extends ActionBarActivity {
    private static final String TAG = StratusActivity.class.getSimpleName();
    public static final String AUTHENTICATION_FILE = "authentication.xml";
    public static final String AUTHENTICATION_TOKEN_PKEY = "authenticationToken";
    public static final String USERNAME_PKEY = "username";
    public static final String PASSWORD_PKEY = "password";

    private static final NetworkStateReceiver stateReceiver = new NetworkStateReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreAuthenticationToken();
        registerAuthenticationListener();
        calculateDisplaySize();
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreAuthenticationToken();
        registerAuthenticationListener();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(stateReceiver, filter);
        enableStatusBar();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(stateReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case  R.id.offerings : {
                startActivity(new Intent(this, OfferingListActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                return true;
            }
            case R.id.subscriptions : {
                startActivity(new Intent(this, SubscriptionListActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                return true;
            }
            case R.id.about : {
                startActivity(new Intent(this, AboutActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                return true;
            }
            case R.id.settings: {
                startActivity(new Intent(this, SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                return true;
            }
            default : return super.onOptionsItemSelected(item);
        }
    }

    private void restoreAuthenticationToken() {
        if (!M_STRATUS.isAuthenticated()) {
            SharedPreferences authenticationTokenFile = getSharedPreferences(AUTHENTICATION_FILE, MODE_PRIVATE);
            String token = authenticationTokenFile.getString(AUTHENTICATION_TOKEN_PKEY, null);
            M_STRATUS.setAuthenticationHeader(token);
            M_STRATUS.setUsername(authenticationTokenFile.getString(USERNAME_PKEY, null));
            M_STRATUS.setPassword(authenticationTokenFile.getString(PASSWORD_PKEY, null));
            Log.d(TAG, "Not authenticated; restoring token: "+token);
        }
    }

    private void registerAuthenticationListener() {
        M_STRATUS.setAuthenticationListener(new AuthenticatedClient.AuthenticationListener() {

            @Override
            public boolean onNoAuthentication() {
                SharedPreferences credentials = getSharedPreferences(AUTHENTICATION_FILE, MODE_PRIVATE);
                String username = credentials.getString(USERNAME_PKEY, "");
                String password = credentials.getString(PASSWORD_PKEY, "");

                M_STRATUS.setUsername(username);
                M_STRATUS.setPassword(password);
                try {
                    Log.d(TAG, "Not authenticated; let's try now");
                    String token = M_STRATUS.authenticate();
                    Log.d(TAG, "Authenticated!; new token is "+token);
                    SharedPreferences authenticationTokenFile = getSharedPreferences(AUTHENTICATION_FILE, MODE_PRIVATE);
                    SharedPreferences.Editor e = authenticationTokenFile.edit();
                    e.putString(AUTHENTICATION_TOKEN_PKEY, token);
                    e.apply();
                    return true;
                } catch (IllegalRestStateException e) { // most probably, incorrect authentication
                    Log.d(TAG, "Authentication did not go well; starting Login screen");
                    M_STRATUS.setAuthenticationHeader(null);
                    Intent i = new Intent(StratusActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(i);
                    synchronized (M_STRATUS) {
                        while (!M_STRATUS.isAuthenticated()) {
                            try {
                                M_STRATUS.wait();
                            } catch (InterruptedException e1) {
                                Log.e(TAG, "Exception when waiting for authentication", e);
                                return false;
                            }
                        }
                    }
                    return true;
                }
            }
        });
    }

    private void enableStatusBar() {
        try { // to show the action bar, switch permanent menu key off
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }

    }
    public void showSendErrorDialog(final Throwable e) {
        Log.e(TAG, "Exception thrown", e);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(StratusActivity.this);

                if (e.getCause() instanceof SSLHandshakeException) { // SSLHandshakeException thrown if Stratus service not available
                    builder.setMessage(getString(R.string.noStratus))
                            .setTitle(getString(R.string.systemMaintenance))
                            .setPositiveButton(getString(R.string.okButton), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                    System.exit(0);
                                }
                            }).create().show();
                } else {
                    builder.setMessage(getString(R.string.criticalError))
                            .setTitle(getString(R.string.errorTitle))
                            .setPositiveButton(getString(R.string.okButton), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent i = new Intent(Intent.ACTION_SEND);
                                    i.setType("message/rfc822");
                                    i.putExtra(Intent.EXTRA_EMAIL  , new String[]{getString(R.string.support_mail_to)});
                                    i.putExtra(Intent.EXTRA_SUBJECT, String.format(getString(R.string.support_mail_subj), BuildConfig.VERSION_NAME));
                                    i.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.support_mail_body), ExceptionUtils.getFullStackTrace(e),
                                            Build.VERSION.SDK_INT, Build.MODEL, getDisplayWidth(), getDisplayHeight(), new Date().toString()));
                                    try {
                                        startActivity(Intent.createChooser(i, getString(R.string.mail_client_title)));
                                    } catch (ActivityNotFoundException ex) {
                                        Toast.makeText(StratusActivity.this, getString(R.string.no_mail_clients), Toast.LENGTH_LONG).show();
                                    }
                                    finish();
                                    System.exit(0);
                                }
                            }).setNegativeButton(getString(R.string.cancelButton), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            System.exit(0);
                        }
                    }).create().show();
                }
            }
        });
    }

    /**
     * Returns true if settings is not found (if setting was never persisted [preference screen was never open], will return true).
     */
    public boolean isEnabledPreference(String key) {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(key, true); //this default value must correspond to default value in settings.xml
    }

    public void enablePreference(String key, boolean value) {
        SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(this).edit();
        e.putBoolean(key, value);
        e.apply();
    }

    protected void setIcon(ImageView view, Entity subscriptionOrOffering) {
        if (isEnabledPreference(SettingsActivity.KEY_PREF_LOAD_IMAGES)) {
            if (isEnabledPreference(SettingsActivity.KEY_PREF_CACHE_FILES)) {
                Picasso.with(this).load(Mpp.STRATUS_HOSTNAME + subscriptionOrOffering.getProperty("image")).into(view);
            } else {
                Picasso.with(this).load(Mpp.STRATUS_HOSTNAME + subscriptionOrOffering.getProperty("image")).
                        networkPolicy(NetworkPolicy.NO_STORE).
                        memoryPolicy(MemoryPolicy.NO_STORE).
                        into(view);
            }
        } else {
            view.setImageDrawable(getResources().getDrawable(R.drawable.no_icon));  // if someone switches off pictures when already loaded
        }
    }

    protected List<Entity> getSubscriptions(boolean enforce) {
        String filter = isEnabledPreference(SettingsActivity.KEY_PREF_FILTER_ACTIVE) ? MppSubscriptionHandler.ACTIVE_ONLY_FILTER : null;
        MppSubscriptionHandler.INSTANCE.setFilter(filter);
        return M_STRATUS.getSubscriptions(enforce);
    }

    private static volatile List<Entity> cachedCategories;    //todo hack; should be handled better way
    private static Boolean cachedCategoriesInitialized = false;
    protected List<Entity> getCategories(boolean enforce) {
        synchronized (cachedCategoriesInitialized) {
            List<Entity> categories = MppCategoryHandler.INSTANCE.list(enforce);
            if (categories == null || categories.size() == 0) return null;  // no internet connection
            if (categories != cachedCategories) { // new instance of the list
                categories.add(0, new MppCategory("{\"displayName\":\""+getString(R.string.allCategories)+"\"}"));  // 'name' property is null
                cachedCategories = categories;
                cachedCategoriesInitialized = false;
            }
            return categories;
        }
    }

    protected void initCategories(List<Entity> offerings) {
        synchronized (cachedCategoriesInitialized) {
            if (cachedCategoriesInitialized) return;
            Map<String, Integer> quantities = new HashMap<>(cachedCategories.size() * 2);
            for (Entity offering : offerings) {        // calculate quantities
                String categoryName = offering.getProperty("category.name");
                if (categoryName != null) { // filter out "All Categories" option
                    Integer quantity = quantities.get(categoryName);
                    if (quantity == null) {
                        quantities.put(categoryName, 1);          // first time
                    } else {
                        quantities.put(categoryName, quantity + 1); // increase the stored quantity
                    }
                }
            }
            for (Iterator<Entity> iterator = cachedCategories.iterator(); iterator.hasNext(); ) { // update displayName properties and remove empty ones
                Entity category = iterator.next();
                String categoryName = category.getProperty("name");
                Integer quantity = categoryName == null ? (Integer) offerings.size() : quantities.get(categoryName); // for "all categories" put number of offerings
                if (quantity == null) {
                    iterator.remove();
                } else {
                    String displayName = category.getProperty("displayName");
                    category.setProperty("displayName", displayName + " (" + quantity + ")");
                }
            }
            cachedCategoriesInitialized = true;
        }
    }

    private static int displayWidth, displayHeight;

    private void calculateDisplaySize() {
        Display display = getWindowManager().getDefaultDisplay();
        displayWidth = display.getWidth();
        displayHeight = display.getHeight();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        calculateDisplaySize();
    }

    public static int getDisplayWidth() {
        return displayWidth;
    }

    public static int getDisplayHeight() {
        return displayHeight;
    }
}
