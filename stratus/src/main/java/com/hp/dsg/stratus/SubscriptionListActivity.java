package com.hp.dsg.stratus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hp.dsg.stratus.entities.Entity;
import com.hp.dsg.stratus.entities.EntityHandler;
import com.hp.dsg.stratus.entities.MppInstance;
import com.hp.dsg.stratus.entities.MppRequest;
import com.hp.dsg.stratus.entities.MppRequestHandler;
import com.hp.dsg.stratus.entities.MppSubscription;
import com.hp.dsg.utils.TimeUtils;

import java.util.Date;
import java.util.List;

import static com.hp.dsg.stratus.entities.MppRequest.FIELD_P;
import static com.hp.dsg.stratus.entities.ServerProperty.SHARE_EMAIL;

public class SubscriptionListActivity extends StratusActivity {
    private static final String TAG = SubscriptionListActivity.class.getSimpleName();

    private ListView listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_list);
        listview = (ListView) findViewById(R.id.subscriptionList);

        listview.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                SubscriptionListActivity.this.scrollState = scrollState;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        new GetSubscriptions().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_subscription, menu);
        return true;
    }

    private static int swipesLeft;
    private static int swipesRight;
    private long startDelay = 0;
    private int scrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

    public static void enableHints(boolean enable) {
        if (enable) {
            swipesLeft = 1;
            swipesRight = 1;
        } else {
            swipesLeft = 0;
            swipesRight = 0;
        }
    }

    private void decreaseSwipesLeft() {
        Log.d(TAG, "Swipes left / right: "+swipesLeft+"/"+swipesRight);
        switch (swipesLeft) {
            case 0 :
                return;
            case 1 :
                if (swipesRight <= 0 && isEnabledPreference(SettingsActivity.KEY_PREF_SHOW_SWIPE_HINTS)) {
                    enablePreference(SettingsActivity.KEY_PREF_SHOW_SWIPE_HINTS, false);
                }
            default: swipesLeft--;
        }
    }

    private void decreaseSwipesRight() {
        Log.d(TAG, "Swipes left / right: "+swipesLeft+"/"+swipesRight);
        switch (swipesRight) {
            case 0 :
                return;
            case 1 :
                if (swipesLeft <= 0 && isEnabledPreference(SettingsActivity.KEY_PREF_SHOW_SWIPE_HINTS)) {
                    enablePreference(SettingsActivity.KEY_PREF_SHOW_SWIPE_HINTS, false);
                }
            default: swipesRight--;
        }
    }

    private void setHintAnimation(View item, boolean delay) {
        if (swipesLeft <= 0 && swipesRight <= 0)  // no hint shown
            return;

        if (!delay) {
            startDelay = 0;
        } else {
            startDelay += (200);
        }
        Log.d(TAG, "Delaying animation by "+startDelay);
        if (swipesRight <= swipesLeft) {
            onSwipeLeftStart(item, - getDisplayWidth() / 4);
        } else {
            onSwipeRightStart(item, getDisplayWidth() / 4);
        }
        animateViewTo(item, 0, bounceInterpolator, startDelay);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.subscriptions) {
            new GetSubscriptions().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, true); //refresh subscriptions
            return true;
        } else return super.onOptionsItemSelected(item);
    }

    private float difference = 0;
    private float initialX = 0;
    private float currentX = 0;
    private View animatedView = null;

    public static final int CLICK = 10;  // everything below this value is considered not to be a swipe

    private static final TimeInterpolator bounceInterpolator = new BounceInterpolator();
    private static final TimeInterpolator decelerateInterpolator = new DecelerateInterpolator();

    private ObjectAnimator animateViewTo(View v, int where, TimeInterpolator interpolator) {
        return animateViewTo(v, where, interpolator, 0);
    }

    private ObjectAnimator animateViewTo(View v, int where, TimeInterpolator interpolator, long startDelay) {
        ObjectAnimator oa = ObjectAnimator.ofFloat(v, "translationX", where);
        oa.setDuration(600);
        oa.setInterpolator(interpolator);
        oa.setStartDelay(startDelay);
        oa.start();
        return oa;
    }

    private ObjectAnimator onSwipeCancel(final View v) {
        EditText editText = (EditText) ((View)v.getParent()).findViewById(R.id.shareEmail);
        hideKeyboard(editText);

        animatedView = null;
        return animateViewTo(v, 0, bounceInterpolator);
    }

    private void onSwipeRightStart(final View v, float difference) {
        v.setTranslationX(difference);
        ((View)v.getParent()).findViewById(R.id.subscriptionButtons).setVisibility(View.VISIBLE);
        ((View)v.getParent()).findViewById(R.id.subscriptionParameters).setVisibility(View.GONE);
        ((View)v.getParent()).findViewById(R.id.subscriptionShareButtons).setVisibility(View.GONE);
    }

    private void onSwipeLeftStart(final View v, float difference) {
        v.setTranslationX(difference);
        ((View)v.getParent()).findViewById(R.id.subscriptionButtons).setVisibility(View.GONE);
        ((View)v.getParent()).findViewById(R.id.subscriptionShareButtons).setVisibility(View.GONE);
        ((View)v.getParent()).findViewById(R.id.subscriptionParameters).setVisibility(View.VISIBLE);
    }

    private void onSwipeRightFinish(final View v) {
        decreaseSwipesRight();
        animateViewTo(v, v.getWidth(), decelerateInterpolator);
        animatedView = v;
    }

    private void onSwipeLeftFinish(final View v) {
        decreaseSwipesLeft();
        ObjectAnimator oa = animateViewTo(v, -v.getWidth(), decelerateInterpolator);
        animatedView = v;
        Entity subscription = ((ViewHolder) v.getTag()).subscription;
        final Intent i = new Intent(SubscriptionListActivity.this, SubscriptionActivity.class);
        i.putExtra(SubscriptionActivity.SUBSCRIPTION_EXTRA_KEY, subscription.toJson());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final Bundle options = ActivityOptions.makeScaleUpAnimation(v, 0, v.getHeight()/2, v.getWidth(), 0).toBundle();
            oa.addListener(new AnimatorListenerAdapter() {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onAnimationEnd(Animator animation) {
                    startActivity(i, options);
                }
            });
        } else {
            startActivity(i);
        }
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
            try {
                startDelay = 0;  // when running subscription list refresh and there was already some delay set, this needs to be reset
                final List<Entity> subscriptions = getSubscriptions(params[0]);
                if (subscriptions == null || subscriptions.size() == 0) return false; // no internet connection
                final View.OnTouchListener gestureListener = new View.OnTouchListener() {
                    public boolean onTouch(final View v, MotionEvent event) {
                        try {
                            if (animatedView != null && animatedView != v) { // something has been animated but now I'm clicking somewhere else
                                onSwipeCancel(animatedView);
                            }
                            if (animatedView == null) {                      // nothing has been animated yet
                                switch (event.getAction()) {
                                    case MotionEvent.ACTION_DOWN:
                                        difference = 0;
                                        initialX = event.getRawX();
                                        currentX = event.getRawX();
                                        Log.d(TAG, "Motion down " + initialX);
                                        return true;
                                    case MotionEvent.ACTION_MOVE:
                                        currentX = event.getRawX();
                                        difference = currentX - initialX;
                                        if (difference > CLICK)
                                            onSwipeRightStart(v, difference);
                                        if (difference > v.getWidth() / 4) {
                                            onSwipeRightFinish(v);
                                        }
                                        if (difference < -CLICK)
                                            onSwipeLeftStart(v, difference);
                                        if (difference < -v.getWidth() / 4)
                                            onSwipeLeftFinish(v);

                                        Log.d(TAG, "Motion left/right " + difference);
                                        return true;
                                    case MotionEvent.ACTION_UP:
                                    case MotionEvent.ACTION_CANCEL:
                                        onSwipeCancel(v);
                                        return false;
                                }
                            }
                            return false;
                        } catch (Throwable e) {
                            showSendErrorDialog(e);
                            return false;
                        }
                    }
                };

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final ListAdapter adapter = new ArrayAdapter<Entity>(SubscriptionListActivity.this,
                                    android.R.layout.simple_list_item_1, subscriptions) {
                                @Override
                                public View getView(int position, View convertView, ViewGroup parent) {
                                    try {
                                        final View row;
                                        if (convertView == null) {
                                            LayoutInflater inflater = (SubscriptionListActivity.this).getLayoutInflater();
                                            row = inflater.inflate(R.layout.subscription_list_item, parent, false);
                                        } else {
                                            row = convertView;
                                        }
                                        final MppSubscription subscription = (MppSubscription) subscriptions.get(position);

                                        ((TextView) row.findViewById(R.id.subscriptionNameList)).setText(subscription.getProperty("name"));

                                        ((TextView) row.findViewById(R.id.subscriptionStatus)).setText(subscription.getProperty("status"));
                                        Date endDate = subscription.getDateProperty("subscriptionTerm.endDate");
                                        ((TextView) row.findViewById(R.id.expiresIn)).setText(endDate == null ? "Never" : TimeUtils.getPeriod(endDate.getTime()));

                                        View item = row.findViewById(R.id.subscriptionListItem);
                                        ImageView expirationLine = (ImageView) item.findViewById(R.id.expirationLine);
                                        ImageView redLine = (ImageView) item.findViewById(R.id.redLine);
                                        long length;
                                        int maxWidth = parent.getMeasuredWidth(); //todo not correct; we should take the part of the row where the line lies, not the whole row length
                                        int maxPeriod = 7 * 24 * 60 * 60 * 1000; // 7 days in in millis
                                        if (!"ACTIVE".equals(subscription.getProperty("status"))) { // if not active
                                            length = 0;
                                            redLine.getLayoutParams().width = 0;  // hiding red line will reveal grey line
                                        } else if (endDate == null) {                               // if no end date
                                            length = maxWidth;
                                        } else {
                                            long now = System.currentTimeMillis();
                                            long diff = endDate.getTime() - now;
                                            if (diff <= 0) {
                                                length = 0;
                                            } else if (diff > maxPeriod) { // more than 7 days
                                                length = maxWidth;
                                            } else {
                                                length = diff * maxWidth / maxPeriod;
                                            }
                                        }
                                        expirationLine.getLayoutParams().width = (int) length;
                                        final ViewHolder holder = new ViewHolder(subscription, row);
                                        item.setTag(holder);
                                        item.setOnTouchListener(gestureListener);
                                        if (subscription.getBooleanProperty("deleted") != null) {
                                            item.setEnabled(false);
                                            item.findViewById(R.id.removedIcon).setVisibility(View.VISIBLE);
                                        } else {
                                            item.setEnabled(true);
                                            item.findViewById(R.id.removedIcon).setVisibility(View.GONE);
                                        }
                                        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) { // first render
                                            setHintAnimation(item, true); //delay the hints on the first render
                                        } else { // scrolling
                                            setHintAnimation(item, false);
                                        }

                                        ImageView image = (ImageView) row.findViewById(R.id.subscriptionIconList);
                                        setIcon(image, subscription);

                                        View button = row.findViewById(R.id.extendButton);
                                        button.getBackground().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.MULTIPLY);
                                        button.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (subscription.getProperty("subscriptionTerm.endDate") == null) {
                                                    Toast.makeText(SubscriptionListActivity.this, getResources().getString(R.string.extensionRequestMissing), Toast.LENGTH_LONG).show();
                                                } else {
                                                    v.setEnabled(false);
                                                    new ExtendSubscription().executeOnExecutor(THREAD_POOL_EXECUTOR, holder);
                                                }
                                            }
                                        });
                                        button = row.findViewById(R.id.shareButton);
                                        button.getBackground().setColorFilter(getResources().getColor(R.color.yellow), PorterDuff.Mode.MULTIPLY);
                                        button.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(final View v) {
                                                View shareButtons = row.findViewById(R.id.subscriptionShareButtons);
                                                shareButtons.setTranslationX(-row.getWidth() * 2 / 3);
                                                shareButtons.setVisibility(View.VISIBLE);

                                                final EditText editText = (EditText) shareButtons.findViewById(R.id.shareEmail);
                                                editText.setText(receivePreviousShareEmail());
                                                editText.setOnFocusChangeListener(ViewUtils.SELECT_LOCAL_PART_OF_EMAIL_ADDRESS);
                                                animateViewTo(shareButtons, 0, decelerateInterpolator);
                                            }
                                        });
                                        final Button cancelButton = (Button) row.findViewById(R.id.cancelButton);
                                        boolean deletable = subscription.getBooleanProperty("deletable");
                                        boolean cancelable = subscription.getBooleanProperty("cancelable");
                                        cancelButton.setText(getString(deletable ? R.string.deleteButton : R.string.cancelButton));
                                        cancelButton.setEnabled(cancelable || deletable);

                                        cancelButton.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.MULTIPLY);
                                        cancelButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (holder.cancelTask != null) {
                                                    holder.cancelTask.cancel(true);
                                                    holder.cancelTask = null;
                                                    cancelButton.setText(getString(R.string.cancelButton));
                                                } else {
                                                    if (subscription.getBooleanProperty("cancelable")) {
                                                        cancelButton.setText("" + 10);  // set the text now so it's visible in UI immediately
                                                        holder.cancelTask = new CancelSubscription();
                                                        holder.cancelTask.executeOnExecutor(THREAD_POOL_EXECUTOR, holder);
                                                    } else {
                                                        if (subscription.getBooleanProperty("deletable")) {
                                                            cancelButton.setEnabled(false);
                                                            new DeleteSubscription().executeOnExecutor(THREAD_POOL_EXECUTOR, holder);
                                                        } else {
                                                            //todo this should never happen
                                                            Toast.makeText(SubscriptionListActivity.this, "This demo cannot be canceled nor deleted", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }
                                            }
                                        });
                                        final View sendShareButton = row.findViewById(R.id.sendShareRequestButton);
                                        sendShareButton.getBackground().setColorFilter(getResources().getColor(R.color.yellow), PorterDuff.Mode.MULTIPLY);
                                        sendShareButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                row.findViewById(R.id.shareButton).setEnabled(false);
                                                sendShareButton.setEnabled(false);
                                                final View shareButtons = row.findViewById(R.id.subscriptionShareButtons);
                                                ObjectAnimator oa = animateViewTo(shareButtons, -row.getWidth() * 2 / 3, decelerateInterpolator);
                                                oa.addListener(new AnimatorListenerAdapter() {
                                                    @Override
                                                    public void onAnimationEnd(Animator animation) {
                                                        shareButtons.setVisibility(View.GONE);
                                                        sendShareButton.setEnabled(true);  // enable it for next use
                                                    }
                                                });
                                                new ShareSubscription().executeOnExecutor(THREAD_POOL_EXECUTOR, holder);
                                                storeShareEmail(((EditText) row.findViewById(R.id.shareEmail)).getText().toString());
                                            }
                                        });
                                        return row;
                                    } catch (Throwable e) {
                                        showSendErrorDialog(e);
                                        return null;
                                    }
                                }

                                //todo hack; we should be re-using views for performance reasons; learnt from http://stackoverflow.com/questions/6921462
                                @Override
                                public int getViewTypeCount() {
                                    return getCount();
                                }

                                @Override
                                public int getItemViewType(int position) {
                                    return position;
                                }
                            };
                            listview.setAdapter(adapter);
                            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.getSubscriptionsProgress);
                            progressBar.setVisibility(View.GONE);
                        } catch (Throwable e) {
                            showSendErrorDialog(e);
                        }
                    }
                });
                return true;
            } catch (Throwable e) {
                showSendErrorDialog(e);
                return null;
            }
        }
    }

    private class ViewHolder {
        private MppSubscription subscription;
        private View topView;

        private ViewHolder(MppSubscription subscription, View topView) {
            this.subscription = subscription;
            this.topView = topView;
        }
        private CancelSubscription cancelTask;
    }

    public static final int DEFAULT_EXTENSION_PERIOD = 3;

    private class ExtendSubscription extends AsyncTask<ViewHolder, Void, String> {
        private View extendButton;
        @Override
        protected String doInBackground(ViewHolder... params) {
            try {
                Entity subscription = params[0].subscription;
                extendButton = params[0].topView.findViewById(R.id.extendButton);

                MppRequest req = new MppRequest(null);
                req.setProperty("action", "MODIFY_SUBSCRIPTION");
                req.setProperty("subscriptionId", subscription.getProperty("serviceId"));
                req.setProperty(MppRequestHandler.CATALOG_ID_KEY, subscription.getProperty(MppRequestHandler.CATALOG_ID_KEY));
                req.setProperty(MppRequestHandler.SERVICE_ID_KEY, subscription.getId());
                long endDate = subscription.getDateProperty("subscriptionTerm.endDate").getTime();
                long newEndDate = endDate + DEFAULT_EXTENSION_PERIOD * 24 * 60 * 60 * 1000;  // add 3 more days
                req.setObjectProperty("endDate", new Date(newEndDate));
                EntityHandler handler = MppRequestHandler.INSTANCE;
                try {
                    return handler.create(req);
                } catch (Exception e) {
                    return null;  //todo upon IllegalRestStateException, check the error stream, it may contain the reason of the failure
                }
            } catch (Throwable e) {
                showSendErrorDialog(e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            String statusMessage = s == null ? getString(R.string.extensionRequestFailure) : String.format(getString(R.string.extensionRequestSuccess), DEFAULT_EXTENSION_PERIOD);
            Toast.makeText(SubscriptionListActivity.this, statusMessage, Toast.LENGTH_LONG).show();
            extendButton.setEnabled(true);
        }
    }

    private class ShareSubscription extends AsyncTask<ViewHolder, Void, String> {
        private View shareButton;
        @Override
        protected String doInBackground(ViewHolder... params) {
            try {
                shareButton = params[0].topView.findViewById(R.id.shareButton);
                MppSubscription subscription = params[0].subscription;
                MppInstance instance = subscription.getInstance();
                if (instance == null) return null; // no internet connection

                String serviceId = instance.getShareServiceId();
                if (serviceId == null) {
                    return "";
                }

                MppRequest req = new MppRequest(null);
                req.setProperty("action", instance.getShareActionName());
                req.setProperty("subscriptionId", subscription.getId());
                req.setProperty(MppRequestHandler.CATALOG_ID_KEY, subscription.getProperty(MppRequestHandler.CATALOG_ID_KEY));
                req.setProperty(MppRequestHandler.SERVICE_ID_KEY, serviceId);

                EditText shareEmail = (EditText) params[0].topView.findViewById(R.id.shareEmail);
                req.setProperty(FIELD_P+SHARE_EMAIL, shareEmail.getText().toString().trim());

                EntityHandler handler = MppRequestHandler.INSTANCE;
                try {
                    return handler.create(req);
                } catch (Exception e) {
                    return null;  //todo upon IllegalRestStateException, check the error stream, it may contain the reason of the failure
                }
            } catch (Throwable e) {
                showSendErrorDialog(e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            String statusMessage = s == null ? getString(R.string.shareRequestFailure) : s.length() == 0 ? getString(R.string.shareRequestMissing) : getString(R.string.shareRequestSuccess);
            Toast.makeText(SubscriptionListActivity.this, statusMessage, Toast.LENGTH_LONG).show();
            shareButton.setEnabled(true);
        }
    }

    private class CancelSubscription extends AsyncTask<ViewHolder, Void, String> {
        private Button cancelButton;
        private ViewHolder holder;

        private void setCancelButtonText(final String text, final boolean enabled) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cancelButton.setText(text);
                    cancelButton.setEnabled(enabled);
                }
            });
        }

        private void taskCanceled() {
            setCancelButtonText(getString(R.string.cancelButton), true);
        }

        @Override
        protected String doInBackground(ViewHolder... params) {
            try {
                cancelButton = (Button) params[0].topView.findViewById(R.id.cancelButton);
                holder = params[0];

                for (int i = Integer.parseInt(cancelButton.getText().toString()); i >= 0; i--) {
                    setCancelButtonText("" + i, true);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        taskCanceled();
                        return null; // onPostExecute will not be executed
                    }
                }
                if (isCancelled()) { // give the last chance to abort the cancellation
                    taskCanceled();
                    return null; // onPostExecute will not be executed
                }
                setCancelButtonText(getString(R.string.cancelButton), false);
                holder.subscription.setObjectProperty("cancelable", false); // keep cancel button even upon UI refresh (till REST API call)

                MppRequest req = new MppRequest(null);
                req.setProperty("action", "CANCEL_SUBSCRIPTION");
                req.setProperty("subscriptionId", holder.subscription.getId());
                req.setProperty(MppRequestHandler.CATALOG_ID_KEY, holder.subscription.getProperty(MppRequestHandler.CATALOG_ID_KEY));
                req.setProperty(MppRequestHandler.SERVICE_ID_KEY, holder.subscription.getId());

                EntityHandler handler = MppRequestHandler.INSTANCE;
                try {
                    return handler.create(req);
                } catch (Exception e) {
                    Log.e(TAG, "Exception when cancelling " + holder.subscription, e);
                    return null;//todo upon IllegalRestStateException, check the error stream, it may contain the reason of the failure
                }
            } catch (Throwable e) {
                showSendErrorDialog(e);
                return null;
            }
        }
        @Override
        protected void onPostExecute(String s) {
            String statusMessage;
            if (s == null) {
                statusMessage = getString(R.string.cancelRequestFailure);
                setCancelButtonText(getString(R.string.cancelButton), true); // enable the button to try cancel later
                holder.subscription.setObjectProperty("cancelable", true); // get button back to be cancelable
            } else {
                statusMessage = getString(R.string.cancelRequestSuccess);
                holder.subscription.setObjectProperty("cancelable", false); // keep cancel button even upon UI refresh (till
                // do not enable the button to make the user to refresh the view
            }
            Toast.makeText(SubscriptionListActivity.this, statusMessage, Toast.LENGTH_LONG).show();
            holder.cancelTask = null;
        }
    }

    private class DeleteSubscription extends AsyncTask<ViewHolder, Void, String> {
        private View cancelButton;
        private ViewHolder holder;

        @Override
        protected String doInBackground(ViewHolder... params) {
            try {
                MppSubscription subscription = params[0].subscription;
                cancelButton = params[0].topView.findViewById(R.id.cancelButton);
                holder = params[0];

                try {
                    return subscription.delete();
                } catch (Exception e) {
                    Log.e(TAG, "Exception when deleting "+subscription, e);
                    return null;
                }
            } catch (Throwable e) {
                showSendErrorDialog(e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            String statusMessage;
            if (s == null) {
                statusMessage = getString(R.string.deleteRequestFailure);
            } else {
                statusMessage = getString(R.string.deleteRequestSuccess);
                holder.subscription.setObjectProperty("deleted", true);
                holder.topView.findViewById(R.id.subscriptionListItem).setEnabled(false);
                holder.topView.findViewById(R.id.removedIcon).setVisibility(View.VISIBLE);
                if (holder.topView.findViewById(R.id.subscriptionListItem) == animatedView) { // the line being removed is animated
                    onSwipeCancel(animatedView);
                }
            }
            Toast.makeText(SubscriptionListActivity.this, statusMessage, Toast.LENGTH_LONG).show();
            cancelButton.setEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (animatedView != null) {
            onSwipeCancel(animatedView);
        } else {
            super.onBackPressed();
        }
    }
}
