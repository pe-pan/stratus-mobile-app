package com.hp.dsg.stratus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

import static com.hp.dsg.stratus.Mpp.M_STRATUS;


public class SubscriptionListActivity extends ActionBarActivity {
    private static final String TAG = SubscriptionListActivity.class.getSimpleName();

    private ListView listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_list);
        listview = (ListView) findViewById(R.id.subscriptionList);

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

    private static final TimeInterpolator bounceInterpolator = new BounceInterpolator();
    private static final TimeInterpolator decelerateInterpolator = new DecelerateInterpolator();

    private ObjectAnimator animateViewTo(View v, int where, TimeInterpolator interpolator) {
        ObjectAnimator oa = ObjectAnimator.ofFloat(v, "translationX", where);
        oa.setDuration(600);
        oa.setInterpolator(interpolator);
        oa.start();
        return oa;
    }

    private ObjectAnimator onSwipeCancel(final View v) {
        InputMethodManager imm = (InputMethodManager)getSystemService(  //hide keyboard if there is one shown
                Context.INPUT_METHOD_SERVICE);
        EditText editText = (EditText) ((View)v.getParent()).findViewById(R.id.shareEmail);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        animatedView = null;
        return animateViewTo(v, 0, bounceInterpolator);
    }

    private void onSwipeRightStart(final View v) {
        v.setTranslationX(difference);
        ((View)v.getParent()).findViewById(R.id.subscriptionButtons).setVisibility(View.VISIBLE);
        ((View)v.getParent()).findViewById(R.id.subscriptionParameters).setVisibility(View.GONE);
        ((View)v.getParent()).findViewById(R.id.subscriptionShareButtons).setVisibility(View.GONE);
    }

    private void onSwipeLeftStart(final View v) {
        v.setTranslationX(difference);
        ((View)v.getParent()).findViewById(R.id.subscriptionButtons).setVisibility(View.GONE);
        ((View)v.getParent()).findViewById(R.id.subscriptionShareButtons).setVisibility(View.GONE);
        ((View)v.getParent()).findViewById(R.id.subscriptionParameters).setVisibility(View.VISIBLE);
    }

    private void onSwipeRightFinish(final View v) {
        animateViewTo(v, v.getWidth(), decelerateInterpolator);
        animatedView = v;
    }

    private void onSwipeLeftFinish(final View v) {
        ObjectAnimator oa = animateViewTo(v, -v.getWidth(), decelerateInterpolator);
        animatedView = v;
        Entity subscription = ((ViewHolder) v.getTag()).subscription;
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
                            final View row;
                            if (convertView == null) {
                                LayoutInflater inflater = (SubscriptionListActivity.this).getLayoutInflater();
                                row = inflater.inflate(R.layout.subscription_list_item, parent, false);
                            } else {
                                row = convertView;
                            }
                            final MppSubscription subscription = (MppSubscription) subscriptions.get(position);

                            ((TextView)row.findViewById(R.id.subscriptionNameList)).setText(subscription.getProperty("name"));

                            ((TextView) row.findViewById(R.id.subscriptionStatus)).setText(subscription.getProperty("status"));
                            Date endDate = subscription.getDateProperty("subscriptionTerm.endDate");
                            ((TextView) row.findViewById(R.id.expiresIn)).setText(endDate == null ? "Never" : TimeUtils.getPeriod(endDate.getTime()));

                            long length;
                            int maxWidth = parent.getMeasuredWidth(); //todo not correct; we should take the part of the row where the line lies, not the whole row length
                            int maxPeriod = 7 * 24 * 60 * 60 * 1000; // 7 days in in millis
                            if (endDate == null) {
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
                            View item = row.findViewById(R.id.subscriptionListItem);
                            row.findViewById(R.id.expirationLine).getLayoutParams().width = (int)length;
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

                            View button = row.findViewById(R.id.extendButton);
                            button.getBackground().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.MULTIPLY);
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (subscription.getProperty("subscriptionTerm.endDate") == null) {
                                        Toast.makeText(SubscriptionListActivity.this, getResources().getString(R.string.extensionRequestMissing), Toast.LENGTH_LONG).show();
                                    } else {
                                        v.setEnabled(false);
                                        new ExtendSubscription().execute(holder);
                                    }
                                }
                            });
                            button = row.findViewById(R.id.shareButton);
                            button.getBackground().setColorFilter(getResources().getColor(R.color.yellow), PorterDuff.Mode.MULTIPLY);
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(final View v) {
                                    View shareButtons = row.findViewById(R.id.subscriptionShareButtons);
                                    shareButtons.setTranslationX(-row.getWidth()*2/3);
                                    shareButtons.setVisibility(View.VISIBLE);

                                    final EditText editText = (EditText) shareButtons.findViewById(R.id.shareEmail);
                                    String oldValue = getPreferences(MODE_PRIVATE).getString("shareEmail", getString(R.string.defaultShareEmail));
                                    editText.setText(oldValue);
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
                                            cancelButton.setText(""+10);  // set the text now so it's visible in UI immediately
                                            holder.cancelTask = new CancelSubscription();
                                            holder.cancelTask.executeOnExecutor(THREAD_POOL_EXECUTOR, holder);
                                        } else {
                                            if (subscription.getBooleanProperty("deletable")) {
                                                cancelButton.setEnabled(false);
                                                new DeleteSubscription().execute(holder);
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
                                    ObjectAnimator oa = animateViewTo(shareButtons, -row.getWidth()*2/3, decelerateInterpolator);
                                    oa.addListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            shareButtons.setVisibility(View.GONE);
                                            sendShareButton.setEnabled(true);  // enable it for next use
                                        }
                                    });
                                    new ShareSubscription().execute(holder);

                                    SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                                    editor.putString("shareEmail", ((EditText) row.findViewById(R.id.shareEmail)).getText().toString());
                                    editor.apply();
                                }
                            });
                            return row;
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
                }
            });
            return true;
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
            MppSubscription subscription = params[0].subscription;
            MppInstance instance = subscription.getInstance();
            shareButton= params[0].topView.findViewById(R.id.shareButton);

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
            req.setProperty("field_shareEmail", shareEmail.getText().toString().trim());

            EntityHandler handler = MppRequestHandler.INSTANCE;
            try {
                return handler.create(req);
            } catch (Exception e) {
                return null;  //todo upon IllegalRestStateException, check the error stream, it may contain the reason of the failure
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
                Log.e(TAG, "Exception when cancelling "+holder.subscription, e);
                return null;//todo upon IllegalRestStateException, check the error stream, it may contain the reason of the failure
            }
        }
        @Override
        protected void onPostExecute(String s) {
            String statusMessage;
            if (s == null) {
                statusMessage = getString(R.string.cancelRequestFailure);
                setCancelButtonText(getString(R.string.cancelButton), true); // enable the button to try cancel later
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
            MppSubscription subscription = params[0].subscription;
            cancelButton = params[0].topView.findViewById(R.id.cancelButton);
            holder = params[0];

            try {
                return subscription.delete();
            } catch (Exception e) {
                Log.e(TAG, "Exception when deleting "+subscription, e);
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
}
