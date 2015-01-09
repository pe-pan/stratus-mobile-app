package com.hp.dsg.stratus;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.hp.dsg.stratus.entities.Entity;

import java.util.ArrayList;
import java.util.List;

import static com.hp.dsg.stratus.rest.Mpp.M_STRATUS;


public class OfferingListActivity extends ActionBarActivity {

    private List<Entity> offerings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offering_list);

        new GetOfferings().execute(false);

        ListView list = (ListView) findViewById(R.id.offeringList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Entity offering = offerings.get(position);
                Intent i = new Intent(OfferingListActivity.this, OfferingActivity.class);
                i.putExtra(OfferingActivity.OFFERING_EXTRA_KEY, offering.toJson());
                startActivity(i);

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_offering, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case  R.id.offerings : {
                new GetOfferings().execute(true); //refresh;
                return true;
            }
            case R.id.subscriptions : {
                startActivity(new Intent(this, SubscriptionListActivity.class));
                finish();
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

    private class GetOfferings extends AsyncTask<Boolean, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... params) {

            offerings = M_STRATUS.getOfferings(params[0]);
            final ListView listview = (ListView) findViewById(R.id.offeringList);
            final List<String> list = new ArrayList<>();
            for (Entity subscription : offerings) {
                list.add(subscription.getProperty("displayName"));
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final ArrayAdapter adapter = new ArrayAdapter(OfferingListActivity.this,
                            android.R.layout.simple_list_item_1, list);
                    listview.setAdapter(adapter);
                    final ProgressBar progressBar = (ProgressBar) findViewById(R.id.getSubscriptionsProgress);
                    progressBar.setVisibility(View.GONE);

                    EditText searchBox = (EditText) findViewById(R.id.searchBox);
                    searchBox.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            adapter.getFilter().filter(s);
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });


                }
            });
            return true;
        }
    }


}
