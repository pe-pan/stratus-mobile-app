package com.hp.dsg.stratus;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hp.dsg.stratus.entities.EntityHandler;
import com.hp.dsg.stratus.rest.entities.CsaEntityHandler;

import static com.hp.dsg.stratus.rest.Mpp.M_STRATUS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity /*implements LoaderCallbacks<Cursor>*/ {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

//    private Csa csa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView homeLink = (TextView) findViewById(R.id.registerLink);
        homeLink.setMovementMethod(LinkMovementMethod.getInstance());

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
//        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        SharedPreferences credentials = getPreferences(MODE_PRIVATE);
        String username = credentials.getString("username", "");
        String password = credentials.getString("password", "");

        ((AutoCompleteTextView) findViewById(R.id.email)).setText(username);
        ((EditText) findViewById(R.id.password)).setText(password);


    }

//    private void populateAutoComplete() {
//        if (Build.VERSION.SDK_INT >= 14) {
//            // Use ContactsContract.Profile (API 14+)
//            getLoaderManager().initLoader(0, null, this);
//        } else if (Build.VERSION.SDK_INT >= 8) {
////            Use AccountManager (API 8+)
//            new SetupEmailAutoCompleteTask().execute(null, null);
//        }
//    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


//        // Check for a valid password, if the user entered one.
//        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
//            mPasswordView.setError(getString(R.string.error_invalid_password));
//            focusView = mPasswordView;
//            cancel = true;
//        }
//
//        // Check for a valid email address.
//        if (TextUtils.isEmpty(email)) {
//            mEmailView.setError(getString(R.string.error_field_required));
//            focusView = mEmailView;
//            cancel = true;
//        } else if (!isEmailValid(email)) {
//            mEmailView.setError(getString(R.string.error_invalid_email));
//            focusView = mEmailView;
//            cancel = true;
//        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            M_STRATUS.setUsername(email);
            M_STRATUS.setPassword(password);
//            csa = new Csa(email, password);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
//        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
//        // for very easy animations. If available, use these APIs to fade-in
//        // the progress spinner.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
//
//            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//                }
//            });
//
//            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mProgressView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//                }
//            });
//        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean > {

        private final String username;
        private final String password;

        UserLoginTask(String email, String password) {
            username = email;
            this.password = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
//            System.setProperty("java.net.useSystemProxies", "false");
//            System.setProperty("https.proxyHost", "proxy.bbn.hp.com");
//            System.setProperty("https.proxyPort", "8080");
//            System.setProperty("http.proxyHost", "proxy.bbn.hp.com");
//            System.setProperty("http.proxyPort", "8080");
            String errorReason = null;
            try {
                M_STRATUS.authenticate();
//            } catch (IllegalRestStateException e) {
//
//                //if (e.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
//                    errorReason = e.getErrorStream();
//                    if (errorReason == null) {
//                        errorReason = e.getMessage();
//                    }
//                    if (errorReason == null) {
//                        errorReason = getString(R.string.error_returned_http_code) + e.getResponseCode();
//                    }
////                    return errorReason;
////                }
            } catch (Exception e) {
                return false;
//                errorReason = e.getLocalizedMessage();
//                if (errorReason == null) {
//                    errorReason = getString(R.string.error_exception_thrown) + e.toString();
//                }

//                return errorReason;
            }
//            if (errorReason != null) {
//                final Button signInButton = (Button) findViewById(R.id.email_sign_in_button);
//                final String message = errorReason;
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        signInButton.setError(message);
//
//                    }
//                });
//                return false;
//            }
            CsaEntityHandler.setClient(M_STRATUS);  //todo move this code to more appropriate place
            EntityHandler.initHandlers();


//            csa.authenticate();
//            try {
//                // Simulate network access.
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                return false;
//            }
//
//            for (String credential : DUMMY_CREDENTIALS) {
//                String[] pieces = credential.split(":");
//                if (pieces[0].equals(username)) {
//                    // Account exists, return true if the password matches.
//                    return pieces[1].equals(password);
//                }
//            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
//            showProgress(false);

            if (success) {

                startActivity(new Intent(LoginActivity.this, SubscriptionListActivity.class));

                SharedPreferences credentials = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor editor = credentials.edit();
                editor.putString("username", username);
                editor.putString("password", password);
                editor.apply();

                finish();
            } else {
                showProgress(false);
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}



