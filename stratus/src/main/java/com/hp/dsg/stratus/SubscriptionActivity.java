package com.hp.dsg.stratus;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.hp.dsg.stratus.rest.entities.MppInstance;
import com.hp.dsg.stratus.rest.entities.MppSubscription;

import java.util.Date;

import static com.hp.dsg.stratus.rest.Mpp.M_STRATUS;

/**
 * Created by panuska on 7.1.2015.
 */
public class SubscriptionActivity extends ActionBarActivity {
    public static final String TAG = SubscriptionActivity.class.getSimpleName();

    public static final String SUBSCRIPTION_EXTRA_KEY = "subscription";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        String json = getIntent().getStringExtra(SUBSCRIPTION_EXTRA_KEY);
        final MppSubscription subscription= new MppSubscription(json);
        new GetSubscriptionDetails().execute(subscription);

        ((TextView) findViewById(R.id.subscriptionName2)).setText(subscription.getProperty("name"));
        ((TextView) findViewById(R.id.subscriptionStatus)).setText(subscription.getProperty("status"));
        Date finishTime = subscription.getDateProperty("subscriptionTerm.endDate");
        TextView expiresIn = (TextView) findViewById(R.id.expiresIn);
        if (finishTime == null) {
            expiresIn.setText("Never");
        } else {
            long now = System.currentTimeMillis();
            long diff = finishTime.getTime() - now;
            if (diff <= 1000) {       // less than a second or already expired
                expiresIn.setText("Expired");
            } else {
                diff = diff / 1000;
                if (diff < 60) {
                    expiresIn.setText(diff+" seconds");
                } else {
                    diff = diff / 60;
                    if (diff < 60) {
                        expiresIn.setText(diff+" minutes");
                    } else {
                        diff = diff / 60;
                        if (diff < 24) {
                            expiresIn.setText(diff+" hours");
                        } else {
                            diff = diff / 24;
                            if (diff < 7) {
                                expiresIn.setText(diff+ " days");
                            } else {
                                diff = diff / 7;
                                expiresIn.setText(diff+ " weeks");
                            }
                        }
                    }
                }
            }
        }

        final TextView expandTextView = (TextView) findViewById(R.id.expandComponentProperties);
        expandTextView.setOnClickListener(new View.OnClickListener() {
            private boolean expanded = false;

            @Override
            public void onClick(View v) {
                expanded = !expanded;
                View properties = findViewById(R.id.subscriptionProperties);
                if (expanded) {
                    expand(properties);
                    expandTextView.setText("Collapse servers");
                } else {
                    collapse(properties);
                    expandTextView.setText("Expand servers");
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about : {
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            }
            default : return super.onOptionsItemSelected(item);
        }
    }

    private class GetSubscriptionDetails extends AsyncTask<MppSubscription, Void, Boolean> {

        @Override
        protected Boolean doInBackground(MppSubscription... params) {
            final MppInstance instance = M_STRATUS.getInstance(params[0]);
            final String[] demoNames = instance.getComponentProperties("DEMONAME");
            final Boolean[] actives = instance.getBooleanComponentProperties("ACTIVATED");
            final String[] publicIps = instance.getComponentProperties("PublicIPAddress");
            final String[] privateIps = instance.getComponentProperties("PRIVATEIP");
            final String[] vpnInfos = instance.getComponentProperties("vpninfo.txt");

            final LinearLayout properties = (LinearLayout) findViewById(R.id.subscriptionProperties);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < demoNames.length; i++) {
                        View row = View.inflate(SubscriptionActivity.this, R.layout.subscription_property_list_item, null);
                        ((TextView)row.findViewById(R.id.demoNameValue)).setText(demoNames[i]);
                        if (actives.length != demoNames.length) {
                            row.findViewById(R.id.activatedValue).setVisibility(View.GONE);
                        } else {
                            ((TextView) row.findViewById(R.id.activatedValue)).setText(actives[i] ? "ACTIVE" : "HALT");
                        }
                        if (privateIps.length != demoNames.length) {
                            row.findViewById(R.id.privateIpTitle).setVisibility(View.GONE);
                            row.findViewById(R.id.privateIpValue).setVisibility(View.GONE);
                        } else {
                            ((TextView)row.findViewById(R.id.privateIpValue)).setText(privateIps[i]);
                        }
                        if (publicIps.length != demoNames.length) {
                            row.findViewById(R.id.publicIpTitle).setVisibility(View.GONE);
                            row.findViewById(R.id.publicIpValue).setVisibility(View.GONE);
                        } else {
                            ((TextView)row.findViewById(R.id.publicIpValue)).setText(publicIps[i]);
                        }
                        if (vpnInfos.length != demoNames.length) {
                            row.findViewById(R.id.vpnInfoTitle).setVisibility(View.GONE);
                            row.findViewById(R.id.vpnInfoValue).setVisibility(View.GONE);
                        } else {
                            ((TextView)row.findViewById(R.id.vpnInfoValue)).setText(vpnInfos[i]);
                        }
                        properties.addView(row, i);
                    }
                }
            });
            return true;
        }
    }

    // copied from https://stackoverflow.com/questions/4946295
    public static void expand(final View v) {
        v.measure(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? RadioGroup.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }
}
