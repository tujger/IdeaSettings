package com.pantheon_inc.odyssey.android;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.pantheon_inc.odyssey.R;
import com.pantheon_inc.odyssey.android.helpers.Account;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class AddAccountActivity extends AppCompatActivity {

    public static final String ACTION = "action";

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world", "eduardm:Eduardm12#", "user1:Richard30061"};
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mUrlView, mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private AlertDialog dialog;
    private View content;
    private TextView mWarning;
    private Button mOk;
    private Button mNeutral;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prepareAndShowDialog();
//        setContentView(R.layout.activity_options);

        // Set up the login form.
        mUrlView = (EditText) content.findViewById(R.id.etServer);
        mUrlView.setText("http://");

        mUsernameView = (EditText) content.findViewById(R.id.etUserid);
//		populateAutoComplete();

        mPasswordView = (EditText) content.findViewById(R.id.etPassword);
        mPasswordView
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int id,
                                                  KeyEvent keyEvent) {
                        if (id == R.id.login || id == EditorInfo.IME_NULL) {
                            attemptLogin();
                            return true;
                        }
                        return false;
                    }
                });

        mOk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = content.findViewById(R.id.layoutForm);
        mProgressView = content.findViewById(R.id.pbLogin);

        content.findViewById(R.id.vTitle).setVisibility(View.GONE);
        content.findViewById(R.id.cbRememberPassword).setVisibility(View.GONE);
        mProgressView.setVisibility(View.GONE);

        populateExisting();

    }

    private void prepareAndShowDialog() {

        dialog = new AlertDialog.Builder(AddAccountActivity.this).create();
        content = getLayoutInflater().inflate(R.layout.activity_options, null);
        mWarning = (TextView) content.findViewById(R.id.tvWarning);

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok), onClickHolder);
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), onClickHolder);
//        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.password), onClickHolder);

        dialog.setTitle(R.string.add_odyssey_account);
        dialog.setOnCancelListener(onCancelListener);

        dialog.setView(content);
        dialog.show();
        mOk = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        mNeutral = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        mWarning.setVisibility(View.GONE);
    }

    DialogInterface.OnClickListener onClickHolder = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    };

    DialogInterface.OnCancelListener onCancelListener = new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            dialog.dismiss();
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    };

    private void populateExisting() {

        Bundle bundle = getIntent().getExtras();

        if (bundle != null && Account.ACCOUNT_CURRENT_ID.equals(bundle.getString(ACTION, ""))) {
            Account s = Account.getCurrentAccount();
            mUrlView.setText(s.getUrl().toString());
            mUsernameView.setText(s.getUsername());
            mPasswordView.setText(s.getPassword());
        }

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

        // Reset errors.
        mUrlView.setError(null);
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String url = mUrlView.getText().toString();
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.password_too_short));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid server address.
        if (TextUtils.isEmpty(url)) {
            mUrlView.setError(getString(R.string.field_is_required));
            focusView = mUrlView;
            cancel = true;
        } else if (!isUrlValid(url)) {
            mUrlView.setError(getString(R.string.url_adrress_invalid));
            focusView = mUrlView;
            cancel = true;
            // Check for a valid email address.
        } else if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.field_is_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.userid_too_short));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(url, username, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isUsernameValid(String username) {
        // TODO: Replace this with your own logic
        return username.length() > 3;
    }

    private boolean isUrlValid(String url) {
        // TODO: Replace this with your own logic
        return true;// url.matches("^https?:\\\\/\\\\/");
    }

    private boolean isPasswordValid(String password) {
        // TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.INVISIBLE
                                    : View.VISIBLE);
                        }
                    });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mProgressView.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUrl;
        private final String mUsername;
        private final String mPassword;
        private TextView errorView;
        private String errorText;

        UserLoginTask(String url, String username, String password) {
            mUrl = url;
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            URL u = null;
            try {
                u = new URL(mUrl);
                u = new URL(u.getProtocol(), u.getHost(), u.getPort(), "");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }


            for (int i = 0; i <= Account.getLastId(); i++) {
                Account s = new Account();
                if (s.load(i)) {
                    if (s.getUrl().toString().equals(u.toString())
                            && s.getUsername().equals(mUsername)) {
                        errorView = mUrlView;
                        errorText = String.format("The account for %s on %s already exists.", mUsername, u.toString());
                        return false;
                    }
                }
            }

            String str;
            try {
                if (!Account.checkServer(u.toString())) {
                    errorView = mUrlView;
                    errorText = "This is not an Odyssey server.";
                    return false;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                errorView = mUrlView;
                errorText = e.getLocalizedMessage();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                errorView = mUrlView;
                errorText = e.getLocalizedMessage();
                return false;
            }


            boolean res = false;
            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mUsername)) {
                    // Account exists, return true if the password matches.
                    if (pieces[1].equals(mPassword)) {
                        res = true;
                        break;
                    }
                    errorView = mPasswordView;
                    errorText = getString(R.string.password_invalid);
                    return false;
                }
            }

            if (!res) {

                errorView = mUsernameView;
                errorText = getString(R.string.userid_invalid);
                return false;
            }

            // TODO: register the new account here.
            new Account().setUrl(u).setUsername(mUsername).setPassword(mPassword).add();

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Intent intent = new Intent(AddAccountActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                Toast.makeText(getApplicationContext(), "Odyssey account added", Toast.LENGTH_SHORT).show();

                finish();
            } else {
                errorView.setError(errorText);
                errorView.requestFocus();

                mWarning.setText(errorText);
                mWarning.setVisibility(View.VISIBLE);

            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
