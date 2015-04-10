package com.hp.dsg.stratus;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import static com.hp.dsg.stratus.Mpp.M_STRATUS;

/**
 * Created by panuska on 19.3.2015.
 */
public class MainActivity extends StratusActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new GetOfferings().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(getString(R.string.sureToExit))
                .setTitle(getString(R.string.exitConfirmation))
                .setPositiveButton(getString(R.string.okButton), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.super.onBackPressed();
                    }
                }).
                setNegativeButton(getString(R.string.cancelButton), null).
                create().show();
    }

    public void subscriptionsClicked(View view) {
        startActivity(new Intent(MainActivity.this, SubscriptionListActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
    }

    public void offeringsClicked(View view) {
        startActivity(new Intent(MainActivity.this, OfferingListActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
    }

    public void settingsClicked(View view) {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
    }

    public void aboutClicked(View view) {
        startActivity(new Intent(MainActivity.this, AboutActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
    }

    private class GetOfferings extends AsyncTask<Boolean, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Boolean... params) {
            try {
                M_STRATUS.getOfferings(false); // cache the results so the sub-sequent call will not take so long
            } catch (Throwable e) {
                showSendErrorDialog(e);
                return false;
            }
            return true;
        }
    }
}
