package com.hp.dsg.stratus;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import static com.hp.dsg.stratus.Mpp.M_STRATUS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends StratusActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView homeLink = (TextView) findViewById(R.id.registerLink);
        homeLink.setMovementMethod(LinkMovementMethod.getInstance());

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mEmailView.setOnFocusChangeListener(ViewUtils.SELECT_LOCAL_PART_OF_EMAIL_ADDRESS);

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
        mEmailSignInButton.getBackground().setColorFilter(getResources().getColor(android.R.color.holo_blue_dark), PorterDuff.Mode.MULTIPLY);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        SharedPreferences credentials = getSharedPreferences(StratusActivity.AUTHENTICATION_FILE, MODE_PRIVATE);
        String username = credentials.getString(StratusActivity.USERNAME_PKEY, "");
        String password = credentials.getString(StratusActivity.PASSWORD_PKEY, "");

        mEmailView.setText(username);
        mPasswordView.setText(password);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return true;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        mEmailView.setError(null);
        mPasswordView.setError(null);

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); //hide keyboard if there is one shown
        imm.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);

        showProgress(true);
        M_STRATUS.setUsername(email);
        M_STRATUS.setPassword(password);

        mAuthTask = new UserLoginTask(email, password);
        mAuthTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            try {
                M_STRATUS.authenticate();
            } catch (Exception e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                SharedPreferences credentials = getSharedPreferences(StratusActivity.AUTHENTICATION_FILE, MODE_PRIVATE);
                SharedPreferences.Editor editor = credentials.edit();
                editor.putString(StratusActivity.USERNAME_PKEY, username);
                editor.putString(StratusActivity.PASSWORD_PKEY, password);
                editor.apply();
                synchronized (M_STRATUS) {
                    M_STRATUS.notifyAll();
                }

                finish();
            } else {
                showProgress(false);
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); //hide keyboard if there is one shown
                imm.showSoftInput(mPasswordView, 0);
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
