package com.hp.dsg.stratus;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import java.util.List;

import static com.hp.dsg.stratus.Mpp.M_STRATUS;

public class OfferingListActivity extends StratusActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offering_list);

        new GetOfferings().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false);

        ListView list = (ListView) findViewById(R.id.offeringList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Entity offering = (Entity)parent.getItemAtPosition(position);
                Intent i = new Intent(OfferingListActivity.this, OfferingActivity.class);
                i.putExtra(OfferingActivity.OFFERING_EXTRA_KEY, offering.toJson());
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_offering, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.offerings) {
            new GetOfferings().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, true); //refresh;
            return true;
        } else return super.onOptionsItemSelected(item);
    }

    private class GetOfferings extends AsyncTask<Boolean, Void, List<Entity>> {

        @Override
        protected List<Entity> doInBackground(Boolean... params) {
            try {
                final List<Entity> offerings = M_STRATUS.getOfferings(params[0]);
                return offerings;
            } catch (Exception e) {
                showSendErrorDialog(e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Entity> offerings) {
            final ListView listview = (ListView) findViewById(R.id.offeringList);
            final ArrayAdapter adapter = new ArrayAdapter<>(OfferingListActivity.this,
                    android.R.layout.simple_list_item_1, offerings);
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
    }
}
