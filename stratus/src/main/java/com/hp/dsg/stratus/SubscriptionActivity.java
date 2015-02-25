package com.hp.dsg.stratus;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.support.v7.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hp.dsg.stratus.entities.EntityHandler;
import com.hp.dsg.stratus.entities.MppInstance;
import com.hp.dsg.stratus.entities.MppRequest;
import com.hp.dsg.stratus.entities.MppRequestHandler;
import com.hp.dsg.stratus.entities.MppSubscription;
import com.hp.dsg.stratus.entities.Server;
import com.hp.dsg.stratus.entities.ServerProperty;
import com.hp.dsg.stratus.entities.ServiceAction;
import com.hp.dsg.utils.TimeUtils;

import java.util.Date;

/**
 * Created by panuska on 7.1.2015.
 */
public class SubscriptionActivity extends StratusActivity {
    public static final String TAG = SubscriptionActivity.class.getSimpleName();

    public static final String SUBSCRIPTION_EXTRA_KEY = "subscription";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        String json = getIntent().getStringExtra(SUBSCRIPTION_EXTRA_KEY);
        final MppSubscription subscription= new MppSubscription(json);
        new GetSubscriptionDetails().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, subscription);

        ((TextView) findViewById(R.id.subscriptionName2)).setText(subscription.getProperty("name"));
        ((TextView) findViewById(R.id.subscriptionStatus)).setText(subscription.getProperty("status"));
        Date finishTime = subscription.getDateProperty("subscriptionTerm.endDate");
        ((TextView) findViewById(R.id.expiresIn)).setText(finishTime == null ? "Never" : TimeUtils.getPeriod(finishTime.getTime()));
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
        protected Boolean doInBackground(final MppSubscription... params) {
            final MppInstance instance = params[0].getInstance();
            final Server[] servers = instance.getServers();

            if (servers == null) {
                return false;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final TextView expandTextView = (TextView) findViewById(R.id.expandComponentProperties);
                    expandTextView.setVisibility(View.VISIBLE);
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

                    LinearLayout properties = (LinearLayout) findViewById(R.id.subscriptionProperties);
                    for (final Server server : servers) {
                        View row = View.inflate(SubscriptionActivity.this, R.layout.subscription_property_list_item, null);
                        for (ServerProperty property : server.properties) {

                            switch (property.name) {
                                case "DEMONAME" :
                                    ((TextView)row.findViewById(R.id.demoNameValue)).setText((String) property.value);
                                    break;
                                case "ACTIVATED" :
                                    ((TextView) row.findViewById(R.id.activatedValue)).setText((Boolean)property.value ? "ACTIVE" : "HALT");
                                    break;
                                default:
                                    LinearLayout propertyList = (LinearLayout) row.findViewById(R.id.subscriptionPropertyList);
                                    View pair = View.inflate(SubscriptionActivity.this, R.layout.subscription_property_pair, null);

                                    TextView title = (TextView)pair.findViewById(R.id.propertyTitle);
                                    title.setText(property.displayName);

                                    TextView value = (TextView)pair.findViewById(R.id.propertyValue);
                                    value.setText(property.value.toString());

                                    propertyList.addView(pair);
                            }
                        }
                        row.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(final View v) {
                                if (server.serviceSubscriptionId == null) {
                                    Toast.makeText(SubscriptionActivity.this, getString(R.string.noOpsDefined), Toast.LENGTH_LONG).show();
                                } else {
                                    PopupMenu menu = new PopupMenu(SubscriptionActivity.this, v);
                                    for (int i = 0; i < server.actions.length; i++) {
                                        ServiceAction action = server.actions[i];
                                        menu.getMenu().add(Menu.NONE, i, Menu.NONE, action.displayName);
                                    }
                                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                        @Override
                                        public boolean onMenuItemClick(MenuItem item) {
                                            ServiceAction action = server.actions[item.getItemId()];
                                            MppRequest req = new MppRequest(null);
                                            req.setProperty("action", action.name);
                                            if (action.emailProperty != null) {
                                                req.setProperty("field_EMAIL_CONF", action.emailProperty);
                                            }
                                            req.setProperty("subscriptionId", instance.getProperty("subscriptionId"));
                                            req.setProperty(MppRequestHandler.CATALOG_ID_KEY, instance.getProperty(MppRequestHandler.CATALOG_ID_KEY));
                                            req.setProperty(MppRequestHandler.SERVICE_ID_KEY, server.serviceSubscriptionId);
                                            new SendServiceAction().executeOnExecutor(THREAD_POOL_EXECUTOR, req);
                                            return true;
                                        }
                                    });
                                    final int color = v.getDrawingCacheBackgroundColor();
                                    menu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                                        @Override
                                        public void onDismiss(PopupMenu popupMenu) {
                                            v.setBackgroundColor(color);
                                        }
                                    });
                                    v.setBackgroundColor(Color.GRAY);
                                    menu.show();
                                }
                                return true;
                            }
                        });
                        properties.addView(row);
                    }
                }
            });
            return true;
        }
    }

    private class SendServiceAction extends AsyncTask<MppRequest, Void, String> {
        @Override
        protected String doInBackground(final MppRequest... params) {
            EntityHandler reqHandler = MppRequestHandler.INSTANCE;
            try {
                return reqHandler.create(params[0]);
            } catch (Exception e) {
                return null;  //todo upon IllegalRestStateException, check the error stream, it may contain the reason of the failure
            }
        }
        @Override
        protected void onPostExecute(String s) {
            String statusMessage = s == null ?  getString(R.string.serviceActionRequestFailure) : getString(R.string.serviceActionRequestSuccess);
            Toast.makeText(SubscriptionActivity.this, statusMessage, Toast.LENGTH_LONG).show();
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
