package com.hp.dsg.stratus;

import android.animation.ObjectAnimator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.ImageView;
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

        ImageView image = (ImageView) findViewById(R.id.subscriptionIcon);
        setIcon(image, subscription);

        new GetSubscriptionDetails().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, subscription);

        ((TextView) findViewById(R.id.subscriptionName2)).setText(subscription.getProperty("name"));
        ((TextView) findViewById(R.id.subscriptionStatus)).setText(subscription.getProperty("status"));
        Date finishTime = subscription.getDateProperty("subscriptionTerm.endDate");
        ((TextView) findViewById(R.id.expiresIn)).setText(finishTime == null ? "Never" : TimeUtils.getPeriod(finishTime.getTime()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_full, menu);
        return true;
    }

    private class GetSubscriptionDetails extends AsyncTask<MppSubscription, Void, Server[]> {
        private MppInstance instance;
        private ImageView expandTriangle;

        private GetSubscriptionDetails() {
            expandTriangle = (ImageView) findViewById(R.id.expandComponentProperties);
            RotateAnimation a = (RotateAnimation) AnimationUtils.loadAnimation(SubscriptionActivity.this, R.anim.rotation);
            expandTriangle.startAnimation(a);
        }

        @Override
        protected Server[] doInBackground(final MppSubscription... params) {
            try {
                instance = params[0].getInstance();
                return instance.getServers();
            } catch (Exception e) {
                showSendErrorDialog(e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Server[] servers) {
            try {
                Animation a = expandTriangle.getAnimation();
                if (servers == null) {
                    final Animation rotateScaleOut = AnimationUtils.loadAnimation(SubscriptionActivity.this, R.anim.rotation_scale);
                    // (2) and once it's scaled out, the icon is gone forever
                    rotateScaleOut.setAnimationListener(new ViewUtils.AnimationListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            expandTriangle.setVisibility(View.GONE);
                        }
                    });
                    final Animation scaleOut = AnimationUtils.loadAnimation(SubscriptionActivity.this, R.anim.scale_out);
                    scaleOut.setAnimationListener(new ViewUtils.AnimationListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            findViewById(R.id.leftLine).setVisibility(View.GONE);
                            findViewById(R.id.rightLine).setVisibility(View.GONE);
                        }
                    });
                    // (1) once the animation finishes the cycle, start a new animation that scales the icon out
                    if (a != null) { // if there is an animation running, stop it once it finishes a cycle
                        a.setAnimationListener(new ViewUtils.AnimationListenerAdapter() {
                            @Override
                            public void onAnimationRepeat(Animation animation) {
                                expandTriangle.startAnimation(rotateScaleOut);
                                findViewById(R.id.leftLine).startAnimation(scaleOut);
                                findViewById(R.id.rightLine).startAnimation(scaleOut);
                            }
                        });
                    } else {
                        expandTriangle.startAnimation(rotateScaleOut);
                        findViewById(R.id.leftLine).startAnimation(scaleOut);
                        findViewById(R.id.rightLine).startAnimation(scaleOut);
                    }

                } else {
                    if (a != null) { // if there is an animation running, stop it once it finishes a cycle
                        a.setAnimationListener(new ViewUtils.AnimationListenerAdapter() {
                            @Override
                            public void onAnimationRepeat(Animation animation) {
                                expandTriangle.clearAnimation();
                            }
                        });
                    }
                    expandTriangle.setPivotX(1f / 2f * expandTriangle.getWidth());
                    expandTriangle.setPivotY(2f / 3f * expandTriangle.getHeight());
                    final ObjectAnimator rotationUp = ObjectAnimator.ofFloat(expandTriangle, "rotation", 0);
                    rotationUp.setDuration(600);
                    final ObjectAnimator rotationDown = ObjectAnimator.ofFloat(expandTriangle, "rotation", 180);
                    rotationDown.setDuration(600);

                    expandTriangle.setOnClickListener(new View.OnClickListener() {
                        private boolean expanded = false;

                        @Override
                        public void onClick(View v) {
                            expanded = !expanded;
                            View properties = findViewById(R.id.subscriptionProperties);
                            if (expanded) {
                                rotationDown.start();
                                expand(properties);
                            } else {
                                rotationUp.start();
                                collapse(properties);
                            }
                        }
                    });

                    LinearLayout properties = (LinearLayout) findViewById(R.id.subscriptionProperties);
                    for (final Server server : servers) {
                        View row = View.inflate(SubscriptionActivity.this, R.layout.subscription_property_list_item, null);
                        for (ServerProperty property : server.properties) {

                            switch (property.name) {
                                case "DEMONAME":
                                    ((TextView) row.findViewById(R.id.demoNameValue)).setText((String) property.value);
                                    break;
                                case "ACTIVATED":
                                    ((TextView) row.findViewById(R.id.activatedValue)).setText((Boolean) property.value ? "ACTIVE" : "HALT");
                                    break;
                                default:
                                    LinearLayout propertyList = (LinearLayout) row.findViewById(R.id.subscriptionPropertyList);
                                    View pair = View.inflate(SubscriptionActivity.this, R.layout.subscription_property_pair, null);

                                    TextView title = (TextView) pair.findViewById(R.id.propertyTitle);
                                    title.setText(property.displayName);

                                    TextView value = (TextView) pair.findViewById(R.id.propertyValue);
                                    value.setText(property.value.toString());

                                    propertyList.addView(pair);
                            }
                        }
                        row.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(final View v) {
                                try {
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
                                                try {
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
                                                } catch (Exception e) {
                                                    showSendErrorDialog(e);
                                                    return false;
                                                }
                                            }
                                        });
                                        final int color = v.getDrawingCacheBackgroundColor();
                                        menu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                                            @Override
                                            public void onDismiss(PopupMenu popupMenu) {
                                                v.setBackgroundColor(color);
                                            }
                                        });
                                        v.setBackgroundColor(getResources().getColor(R.color.darker_gray));
                                        menu.show();
                                    }
                                    return true;
                                } catch (Exception e) {
                                    showSendErrorDialog(e);
                                    return false;
                                }
                            }
                        });
                        properties.addView(row);
                    }
                }
            } catch (Exception e) {
                showSendErrorDialog(e);
            }
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
