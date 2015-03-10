package com.hp.dsg.stratus;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.Toast;

import com.hp.dsg.rest.AuthenticatedClient;
import com.hp.dsg.rest.CacheListener;
import com.hp.dsg.rest.ContentType;
import com.hp.dsg.rest.IllegalRestStateException;
import com.hp.dsg.stratus.cache.ImageCacheListener;
import com.hp.dsg.stratus.entities.Entity;

import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.InputStream;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case  R.id.offerings : {
                startActivity(new Intent(this, OfferingListActivity.class));
                return true;
            }
            case R.id.subscriptions : {
                startActivity(new Intent(this, SubscriptionListActivity.class));
                return true;
            }
            case R.id.about : {
                startActivity(new Intent(this, AboutActivity.class));
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
    public void showSendErrorDialog(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(StratusActivity.this);

                builder.setMessage(getString(R.string.criticalError))
                        .setTitle(getString(R.string.errorTitle))
                        .setPositiveButton(getString(R.string.okButton), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(Intent.ACTION_SEND);
                                i.setType("message/rfc822");
                                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{getString(R.string.support_mail_to)});
                                i.putExtra(Intent.EXTRA_SUBJECT, String.format(getString(R.string.support_mail_subj), getString(R.string.app_version)));
                                Display display = getWindowManager().getDefaultDisplay();
                                i.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.support_mail_body), ExceptionUtils.getFullStackTrace(e), Build.VERSION.SDK_INT, Build.MODEL, display.getWidth(), display.getHeight()));
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
        });
    }

    private CacheListener imageCacheListener;
    protected CacheListener getImageCacheListener() {
        if (imageCacheListener == null) {
            imageCacheListener = new ImageCacheListener(this);
        }
        return imageCacheListener;
    }

    protected void setIcon(ImageView image, Entity subscriptionOrOffering) {
        String url = subscriptionOrOffering.getProperty("image");
        if (url == null) {
            image.setImageDrawable(getResources().getDrawable(R.drawable.no_icon));
            return;
        }
        new DownloadImage(image, url).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class DownloadImage extends AsyncTask<Void, Void, Bitmap> {
        private ImageView image;
        private String url;

        private DownloadImage(ImageView image, String url) {
            this.image = image;
            this.url = url;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                InputStream stream = M_STRATUS.doGet(url, ContentType.NONE, getImageCacheListener());
                if (stream == null) return null;
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                stream.close();
                return bitmap;
            } catch (Exception e) {
                Log.e(TAG, "Exception when reading image from " + url, e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap == null) {
                image.setImageDrawable(getResources().getDrawable(R.drawable.no_icon));
            } else {
                image.setImageBitmap(bitmap);
            }
        }
    }
}
