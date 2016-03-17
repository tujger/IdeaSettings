package com.pantheon_inc.odyssey.android;

import android.annotation.SuppressLint;
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
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.pantheon_inc.odyssey.R;
import com.pantheon_inc.odyssey.android.helpers.Account;

import java.net.MalformedURLException;

public class OptionsActivity extends AppCompatActivity {

    private EditText etTitle, etServer, etLogin, etPassword;
    private Account account;
    private View content;
//    private TextView mWarning;
    private Button mOk;
//    private Button mNeutral;

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

        mOk.setOnClickListener(new OnConfirm());
//        mNeutral.setVisibility(View.GONE);
    }

    @SuppressLint("InflateParams")
    private void prepareAndShowDialog() {
        AlertDialog dialog = new AlertDialog.Builder(OptionsActivity.this).create();
        content = getLayoutInflater().inflate(R.layout.activity_options, null);
//        mWarning = (TextView) content.findViewById(R.id.tvWarning);

        OnCancelListener x = new OnCancelListener();
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok), x);
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), x);
        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.delete), new OnDeleteListener());

        dialog.setTitle(R.string.account_options);
        dialog.setOnCancelListener(x);

        dialog.setView(content);
        dialog.show();
        mOk = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
//        mNeutral = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
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

    private class OnDeleteListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();

            AlertDialog deleteDialog = new AlertDialog.Builder(OptionsActivity.this).create();

            deleteDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok), new OnDeleteConfirm());
            deleteDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), new OnCancelListener());

            deleteDialog.setIcon(new IconicsDrawable(OptionsActivity.this, GoogleMaterial.Icon.gmd_warning).actionBar());
            deleteDialog.setTitle(getString(R.string.delete_account_question));
            deleteDialog.setMessage(getString(R.string.delete_account_message));
            deleteDialog.show();
        }
    }

    private class OnDeleteConfirm implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Intent intent;
            if(account.delete()) {
                Toast.makeText(getApplicationContext(), R.string.odyssey_account_deleted,Toast.LENGTH_SHORT).show();

                if(Account.getCount()>0) {
                    intent = new Intent(OptionsActivity.this, MainActivity.class);
                }else{
                    intent = new Intent(OptionsActivity.this, SplashScreenActivity.class);
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }else{
                Toast.makeText(getApplicationContext(), R.string.error_deleting_odyssey_account,Toast.LENGTH_SHORT).show();

                intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
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
