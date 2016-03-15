package com.pantheon_inc.odyssey.android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.pantheon_inc.odyssey.R;
import com.pantheon_inc.odyssey.android.helpers.Account;

import java.util.Calendar;

public class SplashScreenActivity extends AppCompatActivity {

    private static final int REQUEST_ADD_ACCOUNT = 1;
    private static final int REQUEST_LOCK = 2;

    private View mContentView;
    Handler mHandler = new Handler();
    Runnable mStartMain;
    private boolean showSplashScreen = false;
    private String securityMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Account.initialize(getApplicationContext());

        securityMethod = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(LockActivity.PREF_SECURITY_METHOD, null);

        if (showSplashScreen = checkSplashScreen()) {
            setContentView(R.layout.activity_splash_screen);
            mContentView = findViewById(R.id.iv_splash_background);
        }
    }

    @SuppressLint("InlinedApi")
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (showSplashScreen) {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }

        mStartMain = new Runnable() {
            @Override
            public void run() {
                Log.d("SPLASH", String.valueOf(Account.getCount()));
                if (Account.getCount() == 0) {
                    Intent intent = new Intent(getBaseContext(), AddAccountActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        };

//        if (getIntent().hasExtra(DO_NOT_LOCK)) {//set specific action
//            Integer action = getIntent().getExtras().getInt(ACTION);
//

        if (Account.getCount() == 0) {
            findViewById(R.id.pb_splash).setVisibility(View.GONE);
            findViewById(R.id.tv_splash).setVisibility(View.GONE);
            Intent intent = new Intent(getBaseContext(), AddAccountActivity.class);
            startActivityForResult(intent, REQUEST_ADD_ACCOUNT);
        } else if (securityMethod != null && !"".equals(securityMethod)) {
            findViewById(R.id.pb_splash).setVisibility(View.GONE);
            findViewById(R.id.tv_splash).setVisibility(View.GONE);
            Intent intent = new Intent(getBaseContext(), LockActivity.class);
            startActivityForResult(intent, REQUEST_LOCK);
        } else {
            mHandler.postDelayed(mStartMain, showSplashScreen ? 2000 : 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_LOCK:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                finish();
                break;
            case REQUEST_ADD_ACCOUNT:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mStartMain);
    }

    /**
     * Check for necessary of showing SplashScreen
     */
    private boolean checkSplashScreen() {
        boolean res = false;

        //if lock defined then show splash anyway
        if (securityMethod != null && !"".equals(securityMethod))
            return true;

        if (Account.getCount() == 0)
            return true;

        Calendar d = Calendar.getInstance();
        d.set(d.get(Calendar.YEAR), d.get(Calendar.MONTH), d.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        long currentDate = (long) Math.floor(d.getTimeInMillis() / 1000);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (currentDate != sp.getLong("lastSplashScreenDateShow", 0)) {
            res = true;
            sp.edit().putLong("lastSplashScreenDateShow", currentDate).apply();
        }
        return res;
    }
}
