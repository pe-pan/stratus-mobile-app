package com.hp.dsg.stratus;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.hp.dsg.stratus.entities.Entity;

import java.util.ArrayList;
import java.util.List;

import static com.hp.dsg.stratus.rest.Mpp.M_STRATUS;


public class SubscriptionListActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_list);

        new GetSubscriptions().execute(false);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_subscription, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case  R.id.offerings : {
                startActivity(new Intent(this, OfferingListActivity.class));
                finish();
                return true;
//                startActivity
            }
            case R.id.subscriptions : {
                new GetSubscriptions().execute(true); //refresh subscriptions
                return true;
            }
            case R.id.about : {
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            }
            default : return super.onOptionsItemSelected(item);

        }
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
    }

    private class GetSubscriptions extends AsyncTask<Boolean, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... params) {
            List<Entity> subscriptions = M_STRATUS.getSubscriptions(params[0]);
            final ListView listview = (ListView) findViewById(R.id.subscriptionList);
            final List<String> list = new ArrayList<>();
            for (Entity subscription : subscriptions) {
                list.add(subscription.getProperty("name"));
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final ListAdapter adapter = new ArrayAdapter(SubscriptionListActivity.this,
                            android.R.layout.simple_list_item_1, list);
                    listview.setAdapter(adapter);
                    final ProgressBar progressBar = (ProgressBar) findViewById(R.id.getSubscriptionsProgress);
                    progressBar.setVisibility(View.GONE);

                }
            });
            return true;
        }
    }


}
