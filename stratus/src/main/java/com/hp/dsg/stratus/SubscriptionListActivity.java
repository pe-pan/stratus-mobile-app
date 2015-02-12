package com.hp.dsg.stratus;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hp.dsg.stratus.entities.Entity;
import com.hp.dsg.utils.TimeUtils;

import java.util.Date;
import java.util.List;

import static com.hp.dsg.stratus.Mpp.M_STRATUS;


public class SubscriptionListActivity extends ActionBarActivity {
    private static final String TAG = SubscriptionListActivity.class.getSimpleName();

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
    }

    private float difference = 0;
    private float initialx = 0;
    private float currentx = 0;
    private View animatedView = null;

    public static final int CLICK = 10;  // everything below this value is considered not to be a swipe

    private ObjectAnimator animateViewTo(View v, int where) {
        ObjectAnimator oa = ObjectAnimator.ofFloat(v, "translationX", where);
        oa.setDuration(400);
        oa.start();
        return oa;
    }

    private void onSwipeCancel(final View v) {
        animateViewTo(v, 0);
        animatedView = null;
    }

    private void onSwipeRightStart(final View v) {
        v.setTranslationX(difference);
        ((View)v.getParent()).findViewById(R.id.subscriptionButtons).setVisibility(View.VISIBLE);
        ((View)v.getParent()).findViewById(R.id.subscriptionParameters).setVisibility(View.GONE);
    }

    private void onSwipeLeftStart(final View v) {
        v.setTranslationX(difference);
        ((View)v.getParent()).findViewById(R.id.subscriptionButtons).setVisibility(View.GONE);
        ((View)v.getParent()).findViewById(R.id.subscriptionParameters).setVisibility(View.VISIBLE);
    }

    private void onSwipeRightFinish(final View v) {
        animateViewTo(v, v.getWidth());
        animatedView = v;
    }

    private void onSwipeLeftFinish(final View v) {
        ObjectAnimator oa = animateViewTo(v, -v.getWidth());
        animatedView = v;
        Entity subscription = (Entity) v.getTag();
        final Intent i = new Intent(SubscriptionListActivity.this, SubscriptionActivity.class);
        i.putExtra(SubscriptionActivity.SUBSCRIPTION_EXTRA_KEY, subscription.toJson());
        final Bundle options = ActivityOptions.makeScaleUpAnimation(v, 0, v.getHeight()/2, v.getWidth(), 0).toBundle();

        oa.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startActivity(i, options);
            }

            @Override
            public void onAnimationStart(Animator animation) { }

            @Override
            public void onAnimationCancel(Animator animation) { }

            @Override
            public void onAnimationRepeat(Animator animation) { }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) { // when returning from next activity
            if (animatedView != null) {
                onSwipeCancel(animatedView);
            }
        }
    }

    private class GetSubscriptions extends AsyncTask<Boolean, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... params) {
            final List<Entity> subscriptions = M_STRATUS.getSubscriptions(params[0]);
            final ListView listview = (ListView) findViewById(R.id.subscriptionList);
            final View.OnTouchListener gestureListener = new View.OnTouchListener() {
                public boolean onTouch(final View v, MotionEvent event) {
                    if (animatedView != null && animatedView != v) { // something has been animated but now I'm clicking somewhere else
                        onSwipeCancel(animatedView);
                    }
                    if (animatedView == null) {                      // nothing has been animated yet
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN :
                                difference = 0;
                                initialx = event.getRawX();
                                currentx = event.getRawX();
                                Log.d(TAG, "Motion down " + initialx);
                                return true;
                            case MotionEvent.ACTION_MOVE :
                                currentx = event.getRawX();
                                difference = currentx - initialx;
                                if (difference > CLICK)
                                    onSwipeRightStart(v);
                                if (difference > v.getWidth() / 4) {
                                    onSwipeRightFinish(v);
                                }
                                if (difference < -CLICK)
                                    onSwipeLeftStart(v);
                                if (difference < -v.getWidth() / 4)
                                    onSwipeLeftFinish(v);

                                Log.d(TAG, "Motion left/right " + difference);
                                return true;
                            case MotionEvent.ACTION_UP :
                            case MotionEvent.ACTION_CANCEL :
                                onSwipeCancel(v);
                                return false;
                        }
                    }
                    return false;
                }
            };

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final ListAdapter adapter = new ArrayAdapter<Entity>(SubscriptionListActivity.this,
                            android.R.layout.simple_list_item_1, subscriptions) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View row;
                            if (convertView == null) {
                                LayoutInflater inflater = (SubscriptionListActivity.this).getLayoutInflater();
                                row = inflater.inflate(R.layout.subscription_list_item, parent, false);
                            } else {
                                row = convertView;
                            }
                            final Entity subscription = subscriptions.get(position);

                            ((TextView)row.findViewById(R.id.subscriptionNameList)).setText(subscription.getProperty("name"));

                            ((TextView) row.findViewById(R.id.subscriptionStatus)).setText(subscription.getProperty("status"));
                            Date endDate = subscription.getDateProperty("subscriptionTerm.endDate");
                            ((TextView) row.findViewById(R.id.expiresIn)).setText(endDate == null ? "Never" : TimeUtils.getPeriod(endDate.getTime()));

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
                            View item = row.findViewById(R.id.subscriptionListItem);
                            item.setBackgroundColor(color);
                            item.setTag(subscription);
                            item.setOnTouchListener(gestureListener);

                            View button = row.findViewById(R.id.extendButton);
                            button.getBackground().setColorFilter(0xFF00FF00, PorterDuff.Mode.MULTIPLY);
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.d(TAG, "Extend button pressed");
                                    Toast.makeText(SubscriptionListActivity.this, "Extend button pressed", Toast.LENGTH_SHORT).show();
                                }
                            });
                            button = row.findViewById(R.id.shareButton);
                            button.getBackground().setColorFilter(0xFFFFCC00, PorterDuff.Mode.MULTIPLY);
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.d(TAG, "Share button pressed");
                                    Toast.makeText(SubscriptionListActivity.this, "Share button pressed", Toast.LENGTH_SHORT).show();
                                }
                            });
                            button = row.findViewById(R.id.cancelButton);
                            button.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.d(TAG, "Cancel button pressed");
                                    Toast.makeText(SubscriptionListActivity.this, "Cancel button pressed", Toast.LENGTH_SHORT).show();
                                }
                            });
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
