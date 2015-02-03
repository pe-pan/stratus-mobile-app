package com.hp.dsg.stratus;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hp.dsg.stratus.entities.Entity;

import java.util.Date;
import java.util.List;

import static com.hp.dsg.stratus.rest.Mpp.M_STRATUS;


public class SubscriptionListActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_list);

        new GetSubscriptions().execute(false);

        ListView list = (ListView) findViewById(R.id.subscriptionList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Entity subscription = (Entity)parent.getItemAtPosition(position);
                Intent i = new Intent(SubscriptionListActivity.this, SubscriptionActivity.class);
                i.putExtra(SubscriptionActivity.SUBSCRIPTION_EXTRA_KEY, subscription.toJson());
                startActivity(i);
            }
        });
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
    }

    private class GetSubscriptions extends AsyncTask<Boolean, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... params) {
            final List<Entity> subscriptions = M_STRATUS.getSubscriptions(params[0]);
            final ListView listview = (ListView) findViewById(R.id.subscriptionList);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final ListAdapter adapter = new ArrayAdapter<Entity>(SubscriptionListActivity.this,
                            android.R.layout.simple_list_item_1, subscriptions) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View row = convertView;
                            if (row == null) {

                                LayoutInflater inflater = (SubscriptionListActivity.this).getLayoutInflater();
                                row = inflater.inflate(R.layout.subscription_list_item, parent, false);
                            } else {
                                row = convertView;
                            }
                            Entity subscription = subscriptions.get(position);

                            ((TextView)row.findViewById(R.id.subscriptionNameList)).setText(subscription.getProperty("name"));

                            Date endDate = subscription.getDateProperty("subscriptionTerm.endDate");
                            int color;
                            if (endDate == null) {
                                color = Color.MAGENTA;  // no end date => purple background
                            } else {
                                long now = System.currentTimeMillis();
                                long diff = endDate.getTime() - now;
                                if (diff <= 0) {
                                    color = Color.GRAY;   // gray       // expired => gray background
                                } else if (diff > 16 * 24 * 60 * 60 * 1000) { // more than 16 days
                                    color = Color.GREEN;   // pure green
                                } else {
                                    int offs = (int) ((diff - 1) * 256 / (16 * 24 * 60 * 60 * 1000));
                                    int red = 0xff - offs;
                                    int green = offs;
                                    int blue = 0x00;
                                    color = 0xff * 256*256*256 + red * 256 * 256 + green * 256 + blue;
                                }
                            }
                            row.findViewById(R.id.subscriptionListItem).setBackgroundColor(color);
                            return row;
                        }
                    };
                    listview.setAdapter(adapter);
                    final ProgressBar progressBar = (ProgressBar) findViewById(R.id.getSubscriptionsProgress);
                    progressBar.setVisibility(View.GONE);
                }
            });
            return true;
        }
    }
}
