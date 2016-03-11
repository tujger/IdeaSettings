package com.pantheon_inc.odyssey.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alirezaafkar.json.requester.Requester;
import com.alirezaafkar.json.requester.interfaces.Methods;
import com.alirezaafkar.json.requester.interfaces.Response;
import com.alirezaafkar.json.requester.requesters.JsonObjectRequester;
import com.alirezaafkar.json.requester.requesters.RequestBuilder;
import com.android.volley.VolleyError;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.pantheon_inc.odyssey.R;
import com.pantheon_inc.odyssey.android.helpers.Account;
import com.pantheon_inc.odyssey.android.helpers.ServerVersion;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/*
GCM
Server API Key
AIzaSyAma31pStK-KAvtMS38j7WW8ReVTlpq7eU
Sender ID
351458721539
 */


public class AddAccountActivity extends AppCompatActivity {

    private static final int REQUEST_CHECK_LOGIN = 0;

    // UI references.
    private JsonObjectRequester mRequester;
    private EditText mUrlView, mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private AlertDialog dialog;
    private View content;
    private TextView mWarning;
    private Button mOk;
    private String token;

    // GCM Sender_id
    private String SENDER_ID = "351458721539";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prepareAndShowDialog();

        //rest request init
        Map<String, String> header = new HashMap<>();
        header.put("charset", "utf-8");
        Requester.Config config = new Requester.Config(getApplicationContext());
        config.setHeader(header);
        Requester.init(config);
    }

    private void prepareAndShowDialog() {
        dialog = new AlertDialog.Builder(AddAccountActivity.this).create();
        content = getLayoutInflater().inflate(R.layout.activity_options, null);
        mWarning = (TextView) content.findViewById(R.id.tvWarning);

        OnClickHolder x = new OnClickHolder();
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.login), x);
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), x);

        dialog.setTitle(R.string.add_odyssey_account);
        dialog.setOnCancelListener(new OnCancel());

        dialog.setView(content);
        dialog.show();
        mOk = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        mWarning.setVisibility(View.GONE);

        mUrlView = (EditText) content.findViewById(R.id.etServer);
        mUrlView.setText("http://");
        mUrlView.setSelection("http://".length());

        mUsernameView = (EditText) content.findViewById(R.id.etUserid);

        mPasswordView = (EditText) content.findViewById(R.id.etPassword);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id,
                                          KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_ACTION_DONE) {
                    new OnLoginConfirm().onClick(mPasswordView);
                    return true;
                }
                return false;
            }
        });

        mOk.setOnClickListener(new OnLoginConfirm());

        mLoginFormView = content.findViewById(R.id.layoutForm);

        mProgressView = content.findViewById(R.id.pbLogin);
        mProgressView.setVisibility(View.GONE);

        content.findViewById(R.id.vTitle).setVisibility(View.GONE);
        content.findViewById(R.id.swRememberPassword).setVisibility(View.GONE);
    }

    private class OnClickHolder implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    }

    private class OnCancel implements android.content.DialogInterface.OnCancelListener {
        @Override
        public void onCancel(DialogInterface dialog) {
            dialog.dismiss();
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    }

    private class OnGCMRegistration implements OnClickListener {
        @Override
        public void onClick(View v) {
            new AsyncTask<Void, Void, Boolean>() {
                String message;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    mProgressView.setVisibility(View.VISIBLE);
                    mLoginFormView.setVisibility(View.INVISIBLE);
                    mOk.setEnabled(false);
                }

                @Override
                protected Boolean doInBackground(Void... params) {
                    Boolean res = false;
                    token = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("token", "");

                    if (TextUtils.isEmpty(token)) {
                        InstanceID instanceID = InstanceID.getInstance(getApplicationContext());
                        try {
                            token = instanceID.getToken(SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                            if (!TextUtils.isEmpty(token)) {
                                if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("token", token).commit()) {
                                    res = true;
                                } else {
                                    message = "Error tokenize. Try again later";
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            message = e.getMessage();
                        }
                        System.out.println("GOT TOKEN = " + token);
                    } else {
                        System.out.println("TOKEN EXISTS = " + token);
                        res = true;
                    }
                    return res;
                }

                @Override
                protected void onPostExecute(Boolean b) {
                    super.onPostExecute(b);
                    if (b) {
                        System.out.println("TOKEN = " + token);
                        new OnLoginConfirm().onClick(mOk);
                    } else {
                        mProgressView.setVisibility(View.GONE);
                        mLoginFormView.setVisibility(View.VISIBLE);
                        mUrlView.setError(message);
                    }
                }
            }.execute();
        }
    }

    private class OnLoginConfirm implements OnClickListener {
        @Override
        public void onClick(View v) {

            // Reset errors.
            mUrlView.setError(null);
            mUsernameView.setError(null);
            mPasswordView.setError(null);

            // Store values at the time of the login attempt.
            String url = mUrlView.getText().toString();
            url = url.replaceAll("\\\\/+$", "");
            String username = mUsernameView.getText().toString();
            String password = mPasswordView.getText().toString();

            boolean cancel = false;
            View focusView = null;

            if (!url.startsWith("http")) {
                mUrlView.setError(getString(R.string.url_invalid));
                focusView = mUrlView;
                cancel = true;
            } else if (url.length() < 11) {
                mUrlView.setError(getString(R.string.url_too_short));
                focusView = mUrlView;
                cancel = true;
            } else if (url.matches("^https?:\\\\/\\\\/")) {
                mUrlView.setError(getString(R.string.url_invalid));
                focusView = mUrlView;
                cancel = true;
            } else if (TextUtils.isEmpty(username)) {
                mUsernameView.setError(getString(R.string.userid_too_short));
                focusView = mUsernameView;
                cancel = true;
            } else {
                for (int i = 0; i <= Account.getLastId(); i++) {
                    Account s = new Account();
                    if (s.load(i)) {
                        if (s.getUrl().toString().equals(url)
                                && s.getUsername().equals(username)) {
                            focusView = mUrlView;
                            mUrlView.setError(String.format(getString(R.string.account_already_exists), username, url));
                            cancel = true;
                        }
                    }
                }
            }

            if (cancel) {
                mProgressView.setVisibility(View.GONE);
                mLoginFormView.setVisibility(View.VISIBLE);
                mOk.setEnabled(true);
                focusView.requestFocus();
            } else {
                mProgressView.setVisibility(View.VISIBLE);
                mLoginFormView.setVisibility(View.INVISIBLE);
                mOk.setEnabled(false);

                String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                String deviceName = android.os.Build.MODEL;

                mRequester = new RequestBuilder(getApplicationContext())
                        .requestCode(REQUEST_CHECK_LOGIN)
                        .timeOut(3000)
                        .showError(true) //Show error with toast on Network or Server error
                        .shouldCache(true)
                        .addToHeader("deviceid", deviceId)
                        .addToHeader("platformtype", deviceName)
                        .addToHeader("platformid", "android")
                        .buildObjectRequester(new RestRequester());

                String link = url + "/odyssey/rhaps.ody?";
                ArrayList<String> pars = new ArrayList<>();
                pars.add("action=getVersion");
                if (!TextUtils.isEmpty(username)) {
                    pars.add("action=checkUserid");
                    pars.add("userid=" + username);
                }
                if (!TextUtils.isEmpty(password)) {
                    pars.add("password=" + password);
                }

                link += TextUtils.join("&", pars);
                mRequester.request(Methods.GET, link);
            }
        }
    }

    private class RestRequester implements Response.ObjectResponse {
        @Override
        public void onResponse(int requestCode, @Nullable JSONObject jsonObject) {
            System.out.println("ONRESPONCE " + requestCode + ":" + jsonObject);

            mProgressView.setVisibility(View.GONE);
            mLoginFormView.setVisibility(View.VISIBLE);
            mOk.setEnabled(true);

            if (jsonObject == null) {
                mUrlView.requestFocus();
                mUrlView.setError(getString(R.string.connection_error));
                return;
            }

            try {
                ServerVersion version = new ServerVersion(jsonObject.has("Version") ? jsonObject.getString("Version") : "");

                PackageInfo a = null;
                try {
                    a = getApplication().getPackageManager().getPackageInfo(getApplication().getPackageName(), 0);
                    String clientVersion = a.versionName + "." + a.versionCode;
                    if (!version.isEarlierThanMinor(clientVersion)) {
                        mUrlView.requestFocus();
                        mUrlView.setError(getString(R.string.odyssey_server_too_old));
                        return;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    mUrlView.requestFocus();
                    mUrlView.setError(getString(R.string.your_device_is_incompatible_with_odyssey_server));
                    return;
                }

                String errorField = jsonObject.has("ErrorField") ? jsonObject.getString("ErrorField") : "";
                String errorMessage = jsonObject.has("ErrorMessage") ? jsonObject.getString("ErrorMessage") : "";
                if (errorField.length() > 0) {
                    switch (errorField) {
                        case "userid":
                            mUsernameView.requestFocus();
                            mUsernameView.setError(errorMessage);
                            break;
                        case "password":
                            mPasswordView.requestFocus();
                            mPasswordView.setError(errorMessage);
                            break;
                        default:
                            mUrlView.requestFocus();
                            mUrlView.setError(errorMessage);
                            break;
                    }
                    return;
                }

                String passed = jsonObject.has("Passed") ? jsonObject.getString("Passed") : "";
                if (passed.length() == 0) {
                    mUrlView.requestFocus();
                    mUrlView.setError(getString(R.string.unknown_error));
                    return;
                }

                String url = mUrlView.getText().toString();
                url = url.replaceAll("\\\\/+$", "");
                String username = mUsernameView.getText().toString();
                String password = mPasswordView.getText().toString();

                try {
                    new Account().setUrl(url).setUsername(username).setPassword(password).add();

                    Intent intent = new Intent(AddAccountActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    Toast.makeText(getApplicationContext(), R.string.odyssey_account_added, Toast.LENGTH_SHORT).show();

                    finish();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    mUrlView.requestFocus();
                    mUrlView.setError(getString(R.string.error_adding_account));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mUrlView.setError(getString(R.string.unknown_error));
                mPasswordView.requestFocus();
            }
        }

        @Override
        public void onErrorResponse(int requestCode, VolleyError volleyError, @Nullable JSONObject errorObject) {
            System.out.println("ONERRORRESPONCE " + requestCode + ":" + errorObject + ":" + volleyError);
        }

        @Override
        public void onFinishResponse(int requestCode, @Nullable VolleyError volleyError, String message) {
            System.out.println("ONFINISHRESPONCE " + requestCode + ":" + message + ":" + volleyError);
            mProgressView.setVisibility(View.GONE);
            mLoginFormView.setVisibility(View.VISIBLE);
            mUrlView.setError(message);
            mUrlView.requestFocus();
            mOk.setEnabled(true);
        }

        @Override
        public void onRequestStart(int requestCode) {
//                        mProgressView.setVisibility(View.VISIBLE);
            System.out.println("ONREQUESTSTART " + requestCode);
        }

        @Override
        public void onRequestFinish(int requestCode) {
            System.out.println("ONREQUESTFINISH " + requestCode);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing() && mRequester != null) {
            mRequester.setCallback(null);
        }
    }
}
