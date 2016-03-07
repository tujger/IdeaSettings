package com.pantheon_inc.odyssey.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.pantheon_inc.odyssey.R;
import com.pantheon_inc.odyssey.android.helpers.Account;

import java.net.MalformedURLException;

public class OptionsActivity extends AppCompatActivity {

    private EditText etTitle,etServer,etLogin,etPassword;
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
//        setContentView(R.layout.activity_options);

        account = Account.getCurrentAccount();

        etTitle = (EditText) content.findViewById(R.id.etTitle);
        etServer = (EditText) content.findViewById(R.id.etServer);
        etLogin = (EditText) content.findViewById(R.id.etUserid);
        etPassword = (EditText) content.findViewById(R.id.etPassword);
        content.findViewById(R.id.cbRememberPassword).setVisibility(View.GONE);
        content.findViewById(R.id.tvWarning).setVisibility(View.GONE);
        content.findViewById(R.id.pbLogin).setVisibility(View.GONE);

        etTitle.setText(account.getTitle());

        etServer.setText(account.getUrl().toString());
        etServer.setEnabled(false);

        etLogin.setText(account.getUsername());
        etLogin.setEnabled(false);

        etPassword.setText(account.getPassword());

        mNeutral.setVisibility(View.GONE);
        mOk.setOnClickListener(onConfirm);

    }

    private void prepareAndShowDialog() {

        dialog = new AlertDialog.Builder(OptionsActivity.this).create();
        content = getLayoutInflater().inflate(R.layout.activity_options, null);
        mWarning = (TextView) content.findViewById(R.id.tvWarning);

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok), onClickHolder);
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), onClickHolder);
        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.password), onClickHolder);

        dialog.setTitle(R.string.account_options);
        dialog.setOnCancelListener(onCancelListener);

        dialog.setView(content);
        dialog.show();
        mOk = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        mNeutral = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
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

    View.OnClickListener onConfirm = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            System.out.println("CONFIRM");

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
            setResult(RESULT_OK,intent);
            finish();
        }
    };

}
