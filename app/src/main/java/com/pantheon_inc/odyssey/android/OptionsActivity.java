package com.pantheon_inc.odyssey.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.pantheon_inc.odyssey.R;
import com.pantheon_inc.odyssey.android.helpers.Account;

import java.net.MalformedURLException;

public class OptionsActivity extends AppCompatActivity {

    private EditText etTitle, etServer, etLogin, etPassword;
    private Account account;
    private AlertDialog dialog;
    private View content;
    private TextView mWarning;
    private Button mOk;
    private Button mNeutral;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prepareAndShowDialog();

        account = Account.getCurrentAccount();

        etTitle = (EditText) content.findViewById(R.id.etTitle);
        etServer = (EditText) content.findViewById(R.id.etServer);
        etLogin = (EditText) content.findViewById(R.id.etUserid);
        etPassword = (EditText) content.findViewById(R.id.etPassword);
        content.findViewById(R.id.swRememberPassword).setVisibility(View.GONE);
        content.findViewById(R.id.tvWarning).setVisibility(View.GONE);
        content.findViewById(R.id.pbLogin).setVisibility(View.GONE);

        etTitle.setText(account.getTitle());

        etServer.setText(account.getUrl().toString());
        etServer.setEnabled(false);

        etLogin.setText(account.getUsername());
        etLogin.setEnabled(false);

        etPassword.setText(account.getPassword());
        etPassword.addTextChangedListener(new OnPasswordFieldLengthChanged());

        mNeutral.setVisibility(View.GONE);
        mOk.setOnClickListener(new OnConfirm());
    }

    private void prepareAndShowDialog() {
        dialog = new AlertDialog.Builder(OptionsActivity.this).create();
        content = getLayoutInflater().inflate(R.layout.activity_options, null);
        mWarning = (TextView) content.findViewById(R.id.tvWarning);

        OnCancelListener x = new OnCancelListener();
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok), x);
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), x);
        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.password), x);

        dialog.setTitle(R.string.account_options);
        dialog.setOnCancelListener(x);

        dialog.setView(content);
        dialog.show();
        mOk = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        mNeutral = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
    }

    private class OnPasswordFieldLengthChanged implements TextWatcher {
        public OnPasswordFieldLengthChanged() {
            this.onTextChanged("", 0, 0, 0);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (etPassword.getText().toString().length() == 0 || etPassword.getText().toString().length() >= 6) {
                mOk.setEnabled(true);
            } else {
                mOk.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    private class OnCancelListener implements DialogInterface.OnCancelListener, DialogInterface.OnClickListener {
        @Override
        public void onCancel(DialogInterface dialog) {
            dialog.dismiss();
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    }

    private class OnConfirm implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            account.setTitle(etTitle.getText().toString());
            try {
                account.setUrl(etServer.getText().toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            account.setUsername(etLogin.getText().toString());
            account.setPassword(etPassword.getText().toString());
            account.setSessionId("");
            account.save();

            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
