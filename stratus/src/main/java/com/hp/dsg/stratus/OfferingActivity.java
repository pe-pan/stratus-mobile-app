package com.hp.dsg.stratus;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hp.dsg.stratus.entities.Entity;
import com.hp.dsg.stratus.rest.Mpp;
import com.hp.dsg.stratus.rest.entities.MppOffering;

import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by panuska on 7.1.2015.
 */
public class OfferingActivity extends Activity {
    public static final String TAG = OfferingActivity.class.getSimpleName();

    public static final String OFFERING_EXTRA_KEY = "offering";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static final SimpleDateFormat sdf2 = new SimpleDateFormat("MMM dd, yyyy hh:mm a");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offering);

        String json = getIntent().getStringExtra(OFFERING_EXTRA_KEY);
        final MppOffering offering = new MppOffering(json);

        int ids[] = {R.id.offeringName, R.id.offeringDescription, R.id.offeringCatalog, R.id.subscriptionName, R.id.offeringUpdateOn };
        String properties[] = {"displayName", "description", "catalogName", "displayName", "publishedDate"};

        TextView text = null;
        String value = null;
        for (int i = 0; i < ids.length; i++) {
            text = (TextView) findViewById(ids[i]);
            value = StringUtils.trimToEmpty(offering.getProperty(properties[i]));
            text.setText(value);
        }

        try {
            text.setText(sdf2.format(sdf.parse(value)));  //todo the value is set second time -> change it
        } catch (ParseException e) {
            Log.d(TAG, e.toString());
        }

        if (offering.getProperty("category.name").equals("EXECUTIVE_DEMOS")) {
            findViewById(R.id.subscriptionParameters).setVisibility(View.GONE);
            ((EditText)findViewById(R.id.emailAddress)).setText(Mpp.M_STRATUS.getLoggedUserName());
        } else {
            findViewById(R.id.executiveParams).setVisibility(View.GONE);
        }

        final EditText oppId = (EditText) findViewById(R.id.opportunityId);

        oppId.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    String text = oppId.getText().toString();
                    if (text.startsWith("OPP-")) {
                        oppId.setSelection("OPP-".length(), text.length());
                    } else {
                        oppId.selectAll();
                    }
                }
            }
        });
        Button subscribeButton = (Button) findViewById(R.id.subscribe);
        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oppId = ((EditText)findViewById(R.id.opportunityId)).getText().toString();
                String days = ((EditText)findViewById(R.id.howManyDays)).getText().toString();
                String subscriptionName = ((EditText)findViewById(R.id.subscriptionName)).getText().toString();
                String emailAddress = ((EditText)findViewById(R.id.emailAddress)).getText().toString();
                ServiceRequestTask requestServiceTask = new ServiceRequestTask(offering, oppId, days, subscriptionName, emailAddress);
                requestServiceTask.execute((Void) null);
                finish();
            }
        });

        Window w = getWindow();
        w.setTitle(offering.getProperty(properties[0]));
        setTitle(offering.getProperty(properties[0]));

    }

    public class ServiceRequestTask extends AsyncTask<Void, Void, String> {

        private final MppOffering offering;
        private final String oppId;
        private final String days;
        private final String subscriptionName;
        private final String emailAddress;

        ServiceRequestTask(MppOffering offering, String oppId, String days, String subscriptionName, String emailAddress) {
            this.offering = offering;
            this.oppId = oppId;
            this.days = days;
            this.subscriptionName = subscriptionName;
            this.emailAddress = emailAddress;
        }

        @Override
        protected String doInBackground(Void... params) {
            int length = Integer.parseInt(days);
            String reqId;
            try {
                reqId = Mpp.M_STRATUS.createSubscription(offering, oppId, length, subscriptionName, emailAddress);
            } catch (Exception e) {
                reqId = null;
            }
            return reqId;
        }

        @Override
        protected void onPostExecute(String s) {
            String statusMessage = s == null ?  getString(R.string.requestFailure) : getString(R.string.requestSuccess);
            Toast.makeText(OfferingActivity.this, statusMessage, Toast.LENGTH_LONG).show();
        }
    }
}
