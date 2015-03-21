package com.hp.dsg.stratus;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import static com.hp.dsg.stratus.Mpp.M_STRATUS;

public class SplashActivity extends StratusActivity {
    private static final String TAG = StratusActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_splash);

        ImageView splashScreen = (ImageView) findViewById(R.id.splashscreen);
        Animation a = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.rotation_splash);
        splashScreen.startAnimation(a);

        new GetSubscriptions().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private class GetSubscriptions extends AsyncTask<Boolean, Void, Boolean> {

        private Throwable e;
        @Override
        protected Boolean doInBackground(Boolean... params) {
            try {
                M_STRATUS.getSubscriptions(params[0]);  // just to cache the subscriptions while splash screen is being shown
                return true;
            } catch (Throwable e) {
                Log.d(TAG, "Exception when getting subscriptions", e);
                this.e = e;
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                finish();
            } else {
                showSendErrorDialog(e);
            }
        }
    }
}