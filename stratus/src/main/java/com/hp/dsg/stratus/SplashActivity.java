package com.hp.dsg.stratus;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang.exception.ExceptionUtils;

import static com.hp.dsg.stratus.Mpp.M_STRATUS;

public class SplashActivity extends StratusActivity {
    private static final String TAG = StratusActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_splash);

        new GetSubscriptions().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private class GetSubscriptions extends AsyncTask<Boolean, Void, Boolean> {

        private Exception e;
        @Override
        protected Boolean doInBackground(Boolean... params) {
            try {
                M_STRATUS.getSubscriptions(params[0]);  // just to cache the subscriptions while splash screen is being shown
                return true;
            } catch (Exception e) {
                Log.d(TAG, "Exception when getting subscriptions", e);
                this.e = e;
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Intent mainIntent = new Intent(SplashActivity.this, SubscriptionListActivity.class);
                SplashActivity.this.startActivity(mainIntent);
            } else {
                TextView initText = (TextView) findViewById(R.id.splashInitText);
                initText.setText(getString(R.string.error));
                initText.setTextColor(getResources().getColor(R.color.red));

                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{getString(R.string.support_mail_to)});
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_mail_subj));
                i.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.support_mail_body), ExceptionUtils.getFullStackTrace(e)));
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.mail_client_title)));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(SplashActivity.this, getString(R.string.no_mail_clients), Toast.LENGTH_LONG).show();
                }
            }
            SplashActivity.this.finish();
        }
    }
}