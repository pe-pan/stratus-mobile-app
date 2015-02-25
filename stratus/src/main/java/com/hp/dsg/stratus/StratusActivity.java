package com.hp.dsg.stratus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ViewConfiguration;

import com.hp.dsg.rest.AuthenticatedClient;
import com.hp.dsg.rest.IllegalRestStateException;

import java.lang.reflect.Field;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreAuthenticationToken();
        registerAuthenticationListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreAuthenticationToken();
        registerAuthenticationListener();
        enableStatusBar();
    }

    private void restoreAuthenticationToken() {
        if (!M_STRATUS.isAuthenticated()) {
            SharedPreferences authenticationTokenFile = getSharedPreferences(AUTHENTICATION_FILE, MODE_PRIVATE);
            String token = authenticationTokenFile.getString(AUTHENTICATION_TOKEN_PKEY, null);
            M_STRATUS.setAuthenticationHeader(token);
            M_STRATUS.setUsername(authenticationTokenFile.getString(USERNAME_PKEY, null));
            M_STRATUS.setPassword(authenticationTokenFile.getString(PASSWORD_PKEY, null));
            Log.d(TAG, "Not authenticated; restoring token ..."+token.substring(token.length()-30));
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
                    Intent i = new Intent(StratusActivity.this, LoginActivity.class);
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
}
