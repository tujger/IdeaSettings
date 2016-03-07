package com.pantheon_inc.odyssey.android;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.view.MenuItem;

import com.pantheon_inc.odyssey.R;
import com.pantheon_inc.odyssey.android.helpers.AppCompatPreferenceActivity;
import com.pantheon_inc.odyssey.android.helpers.Utils;

import java.util.Arrays;
import java.util.List;

import static android.preference.Preference.OnPreferenceChangeListener;
import static android.preference.Preference.OnPreferenceClickListener;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    private static final String[] SECURITY_METHODS = new String[]{"security_method_none", "security_method_pattern", "security_method_pin", "security_method_password", "security_method_fingerprint"};
    private static final int SECURITY_METHOD_NONE = 0;
    private static final int SECURITY_METHOD_PATTERN = 1;
    private static final int SECURITY_METHOD_PIN = 2;
    private static final int SECURITY_METHOD_PASSWORD = 3;
    private static final int SECURITY_METHOD_FINGERPRINT = 4;

    protected static String password;
    private static Context context;
    private static AppCompatPreferenceActivity activity;
    private static PreferenceFragment fragment;
    private static ListPreference lpMethod;
    private static Preference pPassword, pPin, pPattern, pFingerprint;
    private static String previousMethod;


    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();


            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }

    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        //preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();

        activity = this;

        setupActionBar();


    }


    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || SecurityPreferenceFragment.class.getName().equals(fragmentName)
                || AboutPreferenceFragment.class.getName().equals(fragmentName);
    }


    public static String getPassword() {
        if (password == null) {
            password = PreferenceManager.getDefaultSharedPreferences(context).getString(LockActivity.PASSWORD, null);
        }
        return password;
    }

    public String getPreviousMethod() {
        return previousMethod;
    }

    public static void setPreviousMethod(String previousMethod) {
        System.out.println("PREVIOUS METHOD "+previousMethod);
        SettingsActivity.previousMethod = previousMethod;
    }

    /**
     * This fragment shows security preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SecurityPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_security);
            setHasOptionsMenu(true);

            fragment = this;
            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.

            lpMethod = (ListPreference) findPreference(LockActivity.PREF_SECURITY_METHOD);
            lpMethod.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    setPreviousMethod(lpMethod.getValue());
                    clickSelectedPreference((String) newValue);
                    return false;
                }
            });
            setPreviousMethod(lpMethod.getValue());

            pPattern = findPreference(SECURITY_METHODS[SECURITY_METHOD_PATTERN]);
            String pattern = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(LockActivity.PATTERN, null);
            if (pattern != null) pPattern.setSummary(R.string.defined);

            pPattern.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    defineLock(LockActivity.SETUP_PATTERN);
                    return false;
                }
            });

            pPin = findPreference(SECURITY_METHODS[SECURITY_METHOD_PIN]);
            String pin = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(LockActivity.PIN, null);
            if (pin != null) pPin.setSummary(R.string.defined);
            pPin.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    defineLock(LockActivity.SETUP_PIN);
                    return false;
                }
            });

            pPassword = findPreference(SECURITY_METHODS[SECURITY_METHOD_PASSWORD]);
            if (getPassword() != null)
                pPassword.setSummary(R.string.defined);
            pPassword.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    defineLock(LockActivity.SETUP_PASSWORD);
                    return false;
                }
            });

            pFingerprint = findPreference(SECURITY_METHODS[SECURITY_METHOD_FINGERPRINT]);
            Utils.checkFingerprintHardware(getActivity().getApplicationContext());
            if ((Utils.getFingerprintMode() & Utils.FINGERPRINT_HARDWARE_DETECTED) == Utils.FINGERPRINT_HARDWARE_DETECTED) {
                pFingerprint.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        defineLock(LockActivity.SETUP_FINGERPRINT);
                        return false;
                    }
                });
            } else {
                PreferenceScreen preferenceScreen = getPreferenceScreen();
                preferenceScreen.removePreference(pFingerprint);

                CharSequence[] ent = lpMethod.getEntries();
                CharSequence[] val = lpMethod.getEntryValues();

                ent = Arrays.copyOf(ent, ent.length - 1);
                val = Arrays.copyOf(val, val.length - 1);

                lpMethod.setEntries(ent);
                lpMethod.setEntryValues(val);

            }

            setSummariesToMethods();

/*
            final ListPreference lpLockTime = (ListPreference) findPreference(LockActivity.PREF_SECURITY_SECURED_LOCK_TIME);
            lpLockTime.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    lpLockTime.setValue((String) newValue);
                    bindPreferenceSummaryToValue(lpLockTime);
                    return false;
                }
            });
            bindPreferenceSummaryToValue(lpLockTime);
*/

            final EditTextPreference etTries = (EditTextPreference) findPreference(LockActivity.PREF_SECURITY_TRIES_BEFORE_DELAY);
            etTries.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    etTries.setText(String.valueOf(newValue));
                    bindPreferenceSummaryToValue(etTries);
                    return false;
                }
            });
            bindPreferenceSummaryToValue(etTries);
        }

        private void clickSelectedPreference(String method) {
            if (!method.equals("")) {

                Preference p = findPreference("security_method_" + method);
                if (p.getSummary() != null && p.getSummary().length() > 0) {
                    lpMethod.setValue(method);
                    setPreviousMethod(method);
                    setSummariesToMethods();
                } else {
                    p.setEnabled(true);
                    int pos = p.getOrder();
                    getPreferenceScreen().onItemClick(null, null, pos, 0);
                }
            } else {
                lpMethod.setValue("");
                setPreviousMethod("");
                setSummariesToMethods();
            }
        }


        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            return (item.getItemId() == android.R.id.home) || super.onOptionsItemSelected(item);
        }

    }


    private static void defineLock(int method) {

        Intent intent = new Intent(activity, LockActivity.class);
        intent.putExtra(LockActivity.ACTION, method);
        activity.startActivityForResult(intent, method);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String method = getPreviousMethod();

        switch (requestCode) {
            case LockActivity.SETUP_PATTERN: {
                System.out.println("PATTERN RESULT " + resultCode);
                if (resultCode == RESULT_OK) {
                    method = LockActivity.PATTERN;
                }else{
                    method=getPreviousMethod();
                }
                break;
            }
            case LockActivity.SETUP_PIN: {
                System.out.println("PIN RESULT " + resultCode);
                if (resultCode == RESULT_OK) {
                    method = LockActivity.PIN;
                }else{
                    method=getPreviousMethod();
                }
                break;
            }
            case LockActivity.SETUP_PASSWORD: {
                System.out.println("PASSWORD RESULT " + resultCode);
                if (resultCode == RESULT_OK) {
                    method = LockActivity.PASSWORD;
                }else{
                    method=getPreviousMethod();
                }
                break;
            }
            case LockActivity.SETUP_FINGERPRINT: {
                System.out.println("FINGERPRINT RESULT " + resultCode);
                if (resultCode == RESULT_OK) {
                    method = LockActivity.FINGERPRINT;
                }else{
                    method=getPreviousMethod();
                }
                break;
            }// SECURITY_CREATE_PASSWORD
        }

        lpMethod.setValue(method);
        setPreviousMethod(method);
        setSummariesToMethods();
    }

    private static void setSummariesToMethods() {

        String str = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext()).getString(LockActivity.PATTERN, null);
        pPattern.setSummary(str == null ? "" : fragment.getResources().getString(R.string.defined));
        pPattern.setEnabled(false);

        str = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext()).getString(LockActivity.PIN, null);
        pPin.setSummary(str == null ? "" : fragment.getResources().getString(R.string.defined));
        pPin.setEnabled(false);

        str = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext()).getString(LockActivity.PASSWORD, null);
        System.out.println("PASSWORDSUMMARY "+str);
        pPassword.setSummary(str == null ? "" : fragment.getResources().getString(R.string.defined));
        pPassword.setEnabled(false);

        Utils.checkFingerprintHardware(fragment.getActivity().getApplicationContext());
        if ((Utils.getFingerprintMode() & Utils.FINGERPRINT_HARDWARE_DETECTED) == Utils.FINGERPRINT_HARDWARE_DETECTED) {
            boolean b = (Utils.getFingerprintMode() & Utils.FINGERPRINT_HAS_ENROLLED_FINGERPRINTS) == Utils.FINGERPRINT_HAS_ENROLLED_FINGERPRINTS;
            pFingerprint.setSummary(b ? fragment.getResources().getString(R.string.defined) : "");
            pFingerprint.setEnabled(false);
        }

        str = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext()).getString(LockActivity.PREF_SECURITY_METHOD, null);
        if (str != null && !str.equals("-1")) {
            System.out.println("METHOD " + str);
            Preference p = fragment.findPreference("security_method_" + str);
            if (p != null) {
                p.setEnabled(true);
                pPassword.setEnabled(true);
            }
        }
        bindPreferenceSummaryToValue(lpMethod);

    }

    /**
     * This fragment shows about only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AboutPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_about);
            setHasOptionsMenu(true);

            PackageInfo a = null;
            try {
                 a = getActivity().getApplication().getPackageManager().getPackageInfo(getActivity().getApplication().getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if(a!=null){
                Preference p = findPreference("about");
                p.setSummary("Version "+a.versionName+" ("+a.versionCode+")");

                p = findPreference("info");
                p.setSummary(
                        Html.fromHtml(getString(R.string.odyssey_legal_information_summary)));


            }

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
//            bindPreferenceSummaryToValue(findPreference("about"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            return (item.getItemId() == android.R.id.home) || super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("RESUME SETTINGS");

        if(lpMethod!=null){
            Utils.checkFingerprintHardware(getApplicationContext());
            if(lpMethod.getValue().equals(LockActivity.FINGERPRINT)){
                if ((Utils.getFingerprintMode() & Utils.FINGERPRINT_HAS_ENROLLED_FINGERPRINTS) != Utils.FINGERPRINT_HAS_ENROLLED_FINGERPRINTS) {
                    lpMethod.setValue("");
                    bindPreferenceSummaryToValue(lpMethod);
                    setSummariesToMethods();
                }
            }


        }

    }
}
