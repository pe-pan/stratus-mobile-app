package com.hp.dsg.stratus;

import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hp.dsg.stratus.entities.MppOffering;
import com.hp.dsg.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by panuska on 7.1.2015.
 */
public class OfferingActivity extends StratusActivity {
    public static final String TAG = OfferingActivity.class.getSimpleName();

    public static final String OFFERING_EXTRA_KEY = "offering";
    private static final SimpleDateFormat sdf2 = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.ENGLISH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_offering);

            String json = getIntent().getStringExtra(OFFERING_EXTRA_KEY);
            final MppOffering offering = new MppOffering(json);

            ImageView image = (ImageView) findViewById(R.id.offeringIcon);
            setIcon(image, offering);

            int ids[] = {R.id.offeringName, R.id.offeringDescription, R.id.offeringCatalog, R.id.subscriptionName, R.id.offeringUpdateOn};
            String properties[] = {"displayName", "description", "catalogName", "displayName", "publishedDate"};

            TextView text = null;
            String value;
            for (int i = 0; i < ids.length; i++) {
                text = (TextView) findViewById(ids[i]);
                value = StringUtils.trimToEmpty(offering.getProperty(properties[i]));
                text.setText(value);
            }

            text.setText(sdf2.format(offering.getDateProperty(properties[ids.length - 1])));  //todo the value is set second time -> change it

            if ("EXECUTIVE_DEMOS".equals(offering.getProperty("category.name"))) {
                findViewById(R.id.offeringParameters).setVisibility(View.GONE);
                EditText email = (EditText) findViewById(R.id.emailAddress);
                email.setText(Mpp.M_STRATUS.getLoggedUserName());
                email.setOnFocusChangeListener(ViewUtils.SELECT_LOCAL_PART_OF_EMAIL_ADDRESS);
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
            subscribeButton.getBackground().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.MULTIPLY);
            subscribeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String oppId = ((EditText) findViewById(R.id.opportunityId)).getText().toString();
                    String days = ((EditText) findViewById(R.id.howManyDays)).getText().toString();
                    String subscriptionName = ((EditText) findViewById(R.id.subscriptionName)).getText().toString();
                    String emailAddress = ((EditText) findViewById(R.id.emailAddress)).getText().toString();
                    ServiceRequestTask requestServiceTask = new ServiceRequestTask(offering, oppId, days, subscriptionName, emailAddress);
                    requestServiceTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
                    finish();
                }
            });
        } catch (Exception e) {
            showSendErrorDialog(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_full, menu);
        return true;
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
            String statusMessage = s == null ?  getString(R.string.subscriptionRequestFailure) : getString(R.string.subscriptionRequestSuccess);
            Toast.makeText(OfferingActivity.this, statusMessage, Toast.LENGTH_LONG).show();
        }
    }
}
