package com.pantheon_inc.odyssey.android;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.digitus.Digitus;
import com.afollestad.digitus.DigitusCallback;
import com.afollestad.digitus.DigitusErrorType;
import com.eftimoff.patternview.PatternView;
import com.pantheon_inc.odyssey.R;
import com.pantheon_inc.odyssey.android.helpers.Utils;

import java.util.ArrayList;


public class LockActivity extends AppCompatActivity implements DigitusCallback {


    public static final String ACTION = "action";
    public static final int SETUP_PASSWORD = 1;
    public static final int SETUP_PATTERN = 2;
    public static final int SETUP_PIN = 4;
    public static final int SETUP_FINGERPRINT = 8;

    public static final String PATTERN = "pattern";
    public static final String PASSWORD = "password";
    public static final String PIN = "pin";
    public static final String FINGERPRINT = "fingerprint";

    public static final String PREF_SECURITY_METHOD = "securityMethod";
    public static final String PREF_SECURITY_TRIES_BEFORE_DELAY = "triesBeforeDelay";
    public static final String PREF_SECURITY_SECURED_LOCK_TIME = "securedLockTime";

    private static final String PREF_PIN_MAX_TRIES = "pinMaxTries";
    private static final String PREF_PIN_LENGTH = "pinLength";
    private static final String PREF_PATTERN_LENGTH = "patternLength";
    private static final String PREF_PATTERN_HIDE_TRAIL = "patternHideTrail";
    private static final String PREF_PASSWORD_MIN_LENGTH = "passwordMinLength";

    private static final String SECURITY_TRIES = "tries";
    private static final String PIN_TRIES = "pinTries";

    private static final int OPTION_PASSWORD_MIN_LENGTH_DEFAULT = 4;
    private static final int OPTION_PIN_LENGTH_DEFAULT = 4;
    private static final int OPTION_PIN_MAX_TRIES_DEFAULT = 10;
    private static final int OPTION_PATTERN_LENGTH_DEFAULT = 3;
    private static final boolean OPTION_PATTERN_HIDE_TRAIL_DEFAULT = false;


    private String password;
    private int passwordInitialMethod, passwordMinLength, currentLength, currentTries, currentPinTries;
    private String preSet;
    private PinView mPinView;
    private PatternView mPatternView;
    private AlertDialog dialog;
    private View content;
    private TextView mWarning;
    private Button mOk, mNeutral;

    private EditText mPasswordView, mConfirmPasswordView, mPinLength, mPinMaxTries, mPatternLength;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String method = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(PREF_SECURITY_METHOD, null);

        if (getIntent().hasExtra(ACTION)) {//set specific action
            Integer action = getIntent().getExtras().getInt(ACTION);
            if (action != 0) {
                prepareAndShowDialog();
                if (action == SETUP_PASSWORD) {
                    setupPassword(SETUP_PASSWORD);
                    return;
                } else if (action == SETUP_PATTERN) {
                    setCurrentLength(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(PREF_PATTERN_LENGTH, OPTION_PATTERN_LENGTH_DEFAULT));
                    setupPattern();
                    return;
                } else if (action == SETUP_PIN) {
                    setCurrentLength(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(PREF_PIN_LENGTH, OPTION_PIN_LENGTH_DEFAULT));
                    setCurrentTries(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(PREF_PIN_MAX_TRIES, OPTION_PIN_MAX_TRIES_DEFAULT));
                    setupPin();
                    return;
                } else if (action == SETUP_FINGERPRINT) {
                    setupFingerprint();
                    return;
                }
            }
        }

        if (PASSWORD.equals(method)) {
            System.out.println("CHECK PASSWORD");
            prepareAndShowDialog();
            checkPassword("");
        } else if (PATTERN.equals(method)) {
            System.out.println("CHECK PATTERN");
            prepareAndShowDialog();
            setCurrentLength(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(PREF_PATTERN_LENGTH, OPTION_PATTERN_LENGTH_DEFAULT));
            checkPattern();
        } else if (PIN.equals(method)) {
            System.out.println("CHECK PIN");
            prepareAndShowDialog();
            setCurrentLength(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(PREF_PIN_LENGTH, OPTION_PIN_LENGTH_DEFAULT));
            checkPin();
        } else if (FINGERPRINT.equals(method)) {
            System.out.println("CHECK FINGERPRINT");
            prepareAndShowDialog();
            checkFingerprint();
        } else {
            System.out.println("NO CHECKS");
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private void prepareAndShowDialog() {

        dialog = new AlertDialog.Builder(LockActivity.this).create();
        content = getLayoutInflater().inflate(R.layout.activity_lock, null);
        mWarning = (TextView) content.findViewById(R.id.tvWarning);

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok), onClickHolder);
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), onClickHolder);
        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.password), onClickHolder);

        dialog.setTitle(R.string.lock);
        dialog.setOnCancelListener(onCancelListener);

        dialog.setView(content);
        dialog.show();
        mOk = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        mNeutral = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
    }

    private void setupPattern() {

        if (getPassword() == null) {
            setupPassword(SETUP_PATTERN);
            return;
        }

        dialog.setTitle(getString(R.string.set_new_pattern));
        hideAll();

        content.findViewById(R.id.layoutPattern).setVisibility(View.VISIBLE);
        mOk.setVisibility(View.VISIBLE);
        mOk.setEnabled(false);
        mOk.setText(R.string.continue_string);
        mOk.setOnClickListener(onPatternContinue);

        content.findViewById(R.id.layoutPattern).setVisibility(View.VISIBLE);

        mPatternView = (PatternView) content.findViewById(R.id.vPattern);

        mNeutral.setText(R.string.options);
        mNeutral.setVisibility(View.VISIBLE);
        mNeutral.setOnClickListener(onPatternOptions);

        mPatternView.setOnPatternDetectedListener(new PatternView.OnPatternDetectedListener() {
            @Override
            public void onPatternDetected() {
                if (mPatternView.getPattern().size() > OPTION_PATTERN_LENGTH_DEFAULT) {
                    mOk.setEnabled(true);
                    mWarning.setVisibility(View.GONE);
                } else {
                    mOk.setEnabled(false);
                    mWarning.setText(R.string.draw_over_at_least_dots);
                    mWarning.setVisibility(View.VISIBLE);
                }
            }
        });
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }


    View.OnClickListener onPatternContinue = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            dialog.setTitle(getString(R.string.confirm_pattern));

            mOk.setText(R.string.confirm);
            mOk.setOnClickListener(onPatternConfirm);
            mOk.setEnabled(false);

            setPreSet(mPatternView.getPatternString());
            mPatternView.clearPattern();

        }
    };

    View.OnClickListener onPatternConfirm = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mPatternView.getPatternString().equals(getPreSet())) {

                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                        .putString(PATTERN, Utils.getEncryptedHash(getPreSet(), 512)).apply();

                Toast.makeText(getApplicationContext(), R.string.pattern_defined, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                return;
            }

            mPatternView.clearPattern();
            mWarning.setVisibility(View.VISIBLE);
            mWarning.setText(R.string.pattern_not_confirmed);
            mOk.setEnabled(false);

        }
    };

    View.OnClickListener onPatternOptions = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LockActivity.this);
            builder.setTitle(getString(R.string.pattern_lock_options));

            LayoutInflater inflater = LayoutInflater.from(LockActivity.this);
            final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_lock_options, null);

            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    boolean b = ((CheckBox) layout.findViewById(R.id.cbPatternHide)).isChecked();
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(PREF_PATTERN_HIDE_TRAIL, b).apply();
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
//                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
//                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    dialog.dismiss();
                }
            });


            layout.findViewById(R.id.layoutPinTries).setVisibility(View.GONE);
            layout.findViewById(R.id.layoutPinLength).setVisibility(View.GONE);
            layout.findViewById(R.id.layoutPatternLength).setVisibility(View.GONE);
            layout.findViewById(R.id.layoutPatternHide).setVisibility(View.VISIBLE);
//            mPatternLength = (EditText) layout.findViewById(R.id.etPatternLength);
//            mPatternLength.setText(String.valueOf(getCurrentLength()));

            boolean b = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(PREF_PATTERN_HIDE_TRAIL, OPTION_PATTERN_HIDE_TRAIL_DEFAULT);
            ((CheckBox) layout.findViewById(R.id.cbPatternHide)).setChecked(b);

            builder.setView(layout);

            AlertDialog dialog = builder.create();
            dialog.show();

        }
    };


    View.OnClickListener onPatternCheck = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String defined = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(PATTERN, "");

            String enc = Utils.getEncryptedHash(mPatternView.getPatternString(), 512);

            if (defined.equals(enc)) {
                successUnlock();
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                return;
            }

            mPatternView.clearPattern();
            Toast.makeText(getApplicationContext(), getString(R.string.pattern_not_accepted), Toast.LENGTH_SHORT).show();

            setCurrentTries(getCurrentTries() + 1);

            if (getCurrentTries() >= Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(PREF_SECURITY_TRIES_BEFORE_DELAY, "5"))) {
                delayBeforeNextTry(PATTERN);
            }
        }
    };


    private void setupPin() {

        if (getPassword() == null) {
            setupPassword(SETUP_PIN);
            return;
        }

        dialog.setTitle(getString(R.string.set_new_pin));
        hideAll();

        mOk.setVisibility(View.VISIBLE);
        mOk.setEnabled(false);
        mOk.setText(R.string.continue_string);
        mOk.setOnClickListener(onPinContinue);

        mNeutral.setVisibility(View.VISIBLE);
        content.findViewById(R.id.layoutPin).setVisibility(View.VISIBLE);

        if (mPinView == null) mPinView = new PinView();
        mPinView.setDots((LinearLayout) content.findViewById(R.id.layoutPinDots));
        mPinView.getDots().setLength(getCurrentLength());
        mPinView.setKeypad((GridLayout) content.findViewById(R.id.layoutPinKeypad));

        mNeutral.setOnClickListener(onPinOptions);
        mNeutral.setText(R.string.options);
        mNeutral.setVisibility(View.VISIBLE);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    View.OnClickListener onPinOptions = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LockActivity.this);

            builder.setTitle(getString(R.string.pin_lock_options));

            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    int oldLength = getCurrentLength();
                    setCurrentLength(Integer.parseInt(mPinLength.getText().toString()));
                    setCurrentTries(Integer.parseInt(mPinMaxTries.getText().toString()));

                    if (getCurrentLength() < 4) {
                        setCurrentLength(4);
                    } else if (getCurrentLength() > 10) {
                        setCurrentLength(10);
                    }
                    if (getCurrentTries() < 5) {
                        setCurrentTries(5);
                    } else if (getCurrentTries() > 20) {
                        setCurrentTries(20);
                    }

                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    dialog.dismiss();

                    if (getCurrentLength() != oldLength) {
                        setPreSet(null);
                        mPinView.getDots().resetState();
                        setupPin();
                        Toast.makeText(getApplicationContext(), R.string.pin_length_was_changed, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    dialog.dismiss();
                }
            });


            LayoutInflater inflater = LayoutInflater.from(LockActivity.this);
            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_lock_options, null);

            layout.findViewById(R.id.layoutPinTries).setVisibility(View.VISIBLE);
            layout.findViewById(R.id.layoutPinLength).setVisibility(View.VISIBLE);
            layout.findViewById(R.id.layoutPatternLength).setVisibility(View.GONE);
            layout.findViewById(R.id.layoutPatternHide).setVisibility(View.GONE);

            mPinLength = (EditText) layout.findViewById(R.id.etPinNumber);
            mPinMaxTries = (EditText) layout.findViewById(R.id.etPinTries);
            mPinLength.setText(String.valueOf(getCurrentLength()));
            mPinMaxTries.setText(String.valueOf(getCurrentTries()));

            builder.setView(layout);

            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        }
    };

    public int getCurrentPinTries() {
        if (currentPinTries == 0)
            currentPinTries = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(PIN_TRIES, 0);

        return currentPinTries;
    }

    public void setCurrentPinTries(int currentPinTries) {
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                .putInt(PIN_TRIES, currentPinTries).apply();
        this.currentPinTries = currentPinTries;
    }

    class PinView {
        private PinViewDots dots = new PinViewDots();

        private PinViewKeypad keypad = new PinViewKeypad();

        public PinView setDots(LinearLayout layout) {
            this.dots.setLayout(layout);
            return this;
        }

        public PinViewDots getDots() {
            return dots;
        }

        public PinViewKeypad getKeypad() {
            return keypad;
        }

        public PinView setKeypad(GridLayout layout) {
            this.keypad.setLayout(layout);
            return this;
        }

        class PinViewDots {
            private LinearLayout layout;
            private int length, current;
            private boolean autoSubmit = false;
            private ArrayList<RadioButton> dots = new ArrayList<>();

            public PinViewDots() {
                current = 0;
            }

            public String toString() {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < length; i++) {
                    int k = (int) dots.get(i).getTag();
                    sb.append(k);
                }
                return sb.toString();
            }

            /**
             * Sets or changes the amount of dots
             */
            public void setLength(int length) {
                if (length < 4) length = 4;
                if (length > 10) length = 10;
                this.length = length;

                for (int i = 0; i < 10; i++) {
                    RadioButton b = (RadioButton) layout.findViewById(Utils.getIdResourceByName(getApplicationContext(), "pinDot" + i));
                    if (i < length) {
                        layout.findViewById(Utils.getIdResourceByName(getApplicationContext(), "pinDotLayout" + i)).setVisibility(View.VISIBLE);
                        dots.add(i, b);
                    } else {
                        layout.findViewById(Utils.getIdResourceByName(getApplicationContext(), "pinDotLayout" + i)).setVisibility(View.GONE);
                    }
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((RadioButton) v).setChecked(!((RadioButton) v).isChecked());

                        }
                    });
                }
            }

            /**
             * Sets the value to current position and goes to next dot
             */
            public void setNext(int value) {
                if (current >= length) return;
                RadioButton b = dots.get(current);
                b.setChecked(true);
                b.setTag(value);
                current++;
                if (current >= length) {
                    mOk.setEnabled(true);
                    if (isAutoSubmit())
                        mOk.callOnClick();
                }
            }

            public void setLayout(LinearLayout layout) {
                this.layout = layout;
            }

            /**
             * Resets the state of current dots
             */
            public void resetState() {
                for (int i = 0; i < length; i++) {
                    dots.get(i).setChecked(false);
                    dots.get(i).setTag(null);
                }
                current = 0;
                mOk.setEnabled(false);
            }

            /**
             * Clears the value of current position and goes back to previous
             */
            public void back() {
                if (current <= 0) return;
                current--;
                RadioButton b = dots.get(current);
                b.setChecked(false);
                b.setTag(null);
                if (current < length) {
                    mOk.setEnabled(false);
                }
            }

            public boolean isAutoSubmit() {
                return autoSubmit;
            }

            public void setAutoSubmit(boolean autoSubmit) {
                this.autoSubmit = autoSubmit;
            }
        }

        class PinViewKeypad {
            GridLayout layout;
            private ArrayList<Button> buttons = new ArrayList<>();

            public PinViewKeypad() {
            }

            public GridLayout getLayout() {
                return layout;
            }

            public void setEnabled(boolean enabled) {
                for (Button x : buttons) {
                    x.setEnabled(enabled);
                }
            }

            public void setLayout(GridLayout layout) {
                this.layout = layout;

                for (int i = 0; i < 10; i++) {
                    Button b = (Button) layout.findViewById(Utils.getIdResourceByName(getApplicationContext(), "pinButton" + i));
                    b.setTag(i);
                    b.setOnClickListener(onPinKeypadButtonListener);
                    buttons.add(i, b);
                }
                Button b = (Button) layout.findViewById(R.id.pinButtonStar);
                b.setOnClickListener(onPinKeypadButtonStarListener);
                buttons.add(10, b);

                b = (Button) layout.findViewById(R.id.pinButtonBack);
                b.setOnClickListener(onPinKeypadButtonBackListener);
                buttons.add(11, b);

            }

            View.OnClickListener onPinKeypadButtonListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dots.setNext((Integer) v.getTag());
                }
            };
            View.OnClickListener onPinKeypadButtonStarListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dots.resetState();
                }
            };
            View.OnClickListener onPinKeypadButtonBackListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dots.back();
                }
            };
        }
    }


    View.OnClickListener onPinContinue = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            dialog.setTitle(getString(R.string.confirm_new_pin));

            mOk.setText(R.string.confirm);
            mOk.setOnClickListener(onPinConfirm);
            mOk.setEnabled(false);

            setPreSet(mPinView.getDots().toString());

            mPinView.getDots().resetState();

        }
    };


    View.OnClickListener onPinConfirm = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mPinView.getDots().toString().equals(getPreSet())) {

                int sol = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(PREF_PIN_MAX_TRIES, OPTION_PIN_MAX_TRIES_DEFAULT);

                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt(PREF_PIN_LENGTH, getCurrentLength()).apply();
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt(PREF_PIN_MAX_TRIES, getCurrentTries()).apply();
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                        .putString(PIN, Utils.getEncryptedHash("" + getCurrentLength() + sol + mPinView.getDots().toString(), 512)).apply();

                Toast.makeText(getApplicationContext(), R.string.pin_defined, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                return;
            }

            mPinView.getDots().resetState();
            mWarning.setVisibility(View.VISIBLE);
            mWarning.setText(R.string.pin_not_confirmed);

        }
    };

    View.OnClickListener onPinCheck = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String defined = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(PIN, "");

            int sol1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(PREF_PIN_LENGTH, OPTION_PIN_LENGTH_DEFAULT);
            int sol2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(PREF_PIN_MAX_TRIES, OPTION_PIN_MAX_TRIES_DEFAULT);

            String enc = Utils.getEncryptedHash("" + sol1 + sol2 + mPinView.getDots().toString(), 512);

            if (defined.equals(enc)) {
                successUnlock();
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                return;
            }

            mPinView.getDots().resetState();
            Toast.makeText(getApplicationContext(), getString(R.string.pin_not_accepted), Toast.LENGTH_SHORT).show();

            setCurrentPinTries(getCurrentPinTries() + 1);
            setCurrentTries(getCurrentTries() + 1);

            if (getCurrentPinTries() >= PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(PREF_PIN_MAX_TRIES, OPTION_PIN_MAX_TRIES_DEFAULT)) {
                Toast.makeText(getApplicationContext(), getString(R.string.pin_locked_enter_password), Toast.LENGTH_SHORT).show();
                checkPassword(PIN);
            } else {
                checkPinTries();
            }

            if (getCurrentTries() >= Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(PREF_SECURITY_TRIES_BEFORE_DELAY, "5"))) {
                delayBeforeNextTry(PIN);
            }

        }
    };

    private void delayBeforeNextTry(final String method) {

        new AsyncTask<Void, Void, Void>() {
            private int current = 0;
            private int max = 3;
            String message;
            int visibility;
            boolean confirmState;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (PIN.equals(method)) {
                    mPinView.getKeypad().setEnabled(false);
                } else if (PATTERN.equals(method)) {
                    mPatternView.setEnabled(false);
                } else if (PASSWORD.equals(method)) {
                    mPasswordView.setEnabled(false);
                }
                message = (String) mWarning.getText();
                visibility = mWarning.getVisibility();
                confirmState = mOk.isEnabled();
                mWarning.setVisibility(View.VISIBLE);
                mOk.setEnabled(false);

                mWarning.setText(getResources().getString(R.string.you_can_try_after_seconds, max - current));
            }

            @Override
            protected Void doInBackground(Void... params) {
                for (int i = 1; i <= max; i++) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    current = max - i;
                    publishProgress();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void o) {
                super.onPostExecute(o);
                mOk.setEnabled(confirmState);
                mWarning.setVisibility(visibility);
                mWarning.setText(message);
                if (PIN.equals(method)) {
                    mPinView.getKeypad().setEnabled(true);
                } else if (PATTERN.equals(method)) {
                    mPatternView.setEnabled(true);
                } else if (PASSWORD.equals(method)) {
                    mPasswordView.setEnabled(true);
                    mPasswordView.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                }

                setCurrentTries(0);
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
                mWarning.setText(getResources().getString(R.string.you_can_try_after_seconds, current));
            }
        }.execute();

    }


    private void hideAll() {

        content.findViewById(R.id.layoutPin).setVisibility(View.GONE);
        content.findViewById(R.id.layoutPattern).setVisibility(View.GONE);
        content.findViewById(R.id.layoutPassword).setVisibility(View.GONE);
        content.findViewById(R.id.layoutFingerprint).setVisibility(View.GONE);

        mNeutral.setVisibility(View.GONE);
        mOk.setVisibility(View.GONE);
        mWarning.setVisibility(View.GONE);

    }


    /**
     * Set password dialog
     */
    private void setupPassword(int initialMethod) {


        dialog.setTitle(getString(R.string.set_new_password));
        hideAll();

        passwordMinLength = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(PREF_PASSWORD_MIN_LENGTH, OPTION_PASSWORD_MIN_LENGTH_DEFAULT);

        mOk.setVisibility(View.VISIBLE);
        content.findViewById(R.id.vConfirmPassword).setVisibility(View.VISIBLE);
        content.findViewById(R.id.layoutPassword).setVisibility(View.VISIBLE);

        mPasswordView = (EditText) content.findViewById(R.id.etPassword);
        mConfirmPasswordView = (EditText) content.findViewById(R.id.etConfirmPassword);
        checkPasswordFieldsLength.onTextChanged("", 0, 0, 0);
        mPasswordView.addTextChangedListener(checkPasswordFieldsLength);
        mConfirmPasswordView.addTextChangedListener(checkPasswordFieldsLength);

        mOk.setText(R.string.confirm);
        mOk.setOnClickListener(onPasswordConfirm);

        setPasswordInitialMethod(initialMethod);
        if (initialMethod != SETUP_PASSWORD) {
            Toast.makeText(this, R.string.you_must_set_up_password_first, Toast.LENGTH_SHORT).show();
            mWarning.setText(R.string.you_must_set_up_password_first);
            mWarning.setVisibility(View.VISIBLE);
        }
    }

    TextWatcher checkPasswordFieldsLength = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mPasswordView.getText().toString().length() >= passwordMinLength
                    && (mConfirmPasswordView == null || mConfirmPasswordView.getText().toString().length() >= passwordMinLength)) {
                mOk.setEnabled(true);
            } else {
                mOk.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    View.OnClickListener onPasswordConfirm = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mPasswordView.getText().toString().length() < 4) {
                mWarning.setText(R.string.password_too_short);
                mWarning.setVisibility(View.VISIBLE);

                mPasswordView.requestFocus();
                return;
            }

            if (!mPasswordView.getText().toString().equals(mConfirmPasswordView.getText().toString())) {
                mWarning.setText(R.string.password_not_confirmed);
                mWarning.setVisibility(View.VISIBLE);

                mConfirmPasswordView.requestFocus();
                return;
            }

            mWarning.setVisibility(View.GONE);
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                    .putString(PASSWORD, Utils.getEncryptedHash(mPasswordView.getText().toString(), 512)).apply();

            Toast.makeText(getApplicationContext(), "Password defined.", Toast.LENGTH_SHORT).show();

            if (getPasswordInitialMethod() == SETUP_PASSWORD) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            } else if (getPasswordInitialMethod() == SETUP_PATTERN) {
                setupPattern();
            } else if (getPasswordInitialMethod() == SETUP_PIN) {
                setupPin();
            } else if (getPasswordInitialMethod() == SETUP_FINGERPRINT) {
                setupFingerprint();
            }
        }
    };


    private void successUnlock() {
        System.out.println("UNLOCK SUCCESS");

        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt(PIN_TRIES, 0).apply();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt(SECURITY_TRIES, 0).apply();

    }

    /**
     * Check password dialog
     */
    private void checkPassword(String initialMethod) {
        System.out.println("CHECK PASSWORD");

        if (getPassword() == null) {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }

        dialog.setTitle(getString(R.string.check_password));
        hideAll();

        content.findViewById(R.id.vConfirmPassword).setVisibility(View.GONE);
        content.findViewById(R.id.layoutPassword).setVisibility(View.VISIBLE);
        mWarning.setText("");

        passwordMinLength = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(PREF_PASSWORD_MIN_LENGTH, OPTION_PASSWORD_MIN_LENGTH_DEFAULT);
        mPasswordView = (EditText) content.findViewById(R.id.etPassword);

        if (PIN.equals(initialMethod)) {
            mWarning.setText(R.string.pin_locked_enter_password);
            mWarning.setVisibility(View.VISIBLE);
        }

        mOk.setOnClickListener(onPasswordCheck);
        mOk.setVisibility(View.VISIBLE);
        mNeutral.setText(R.string.clear);
        mNeutral.setVisibility(View.GONE);

        checkPasswordFieldsLength.onTextChanged("", 0, 0, 0);
        mPasswordView.addTextChangedListener(checkPasswordFieldsLength);

        mPasswordView.requestFocusFromTouch();
        mPasswordView.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    /**
     * Check pattern
     */
    private void checkPattern() {

        dialog.setTitle(getString(R.string.check_pattern));
        hideAll();

        content.findViewById(R.id.layoutPattern).setVisibility(View.VISIBLE);

        mOk.setEnabled(false);
        mOk.setOnClickListener(onPatternCheck);

        mNeutral.setVisibility(View.VISIBLE);
        mNeutral.setText(R.string.password);
        mNeutral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPassword("");
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mPatternView = (PatternView) content.findViewById(R.id.vPattern);
        mPatternView.setOnPatternDetectedListener(new PatternView.OnPatternDetectedListener() {
            @Override
            public void onPatternDetected() {
                if (mPatternView.getPattern().size() > OPTION_PATTERN_LENGTH_DEFAULT) {
                    mWarning.setVisibility(View.GONE);
                    mOk.setEnabled(true);
                    mOk.callOnClick();
                } else {
                    mOk.setEnabled(false);
                    mWarning.setText(R.string.draw_over_at_least_dots);
                    mWarning.setVisibility(View.VISIBLE);
                }
            }
        });

        mPatternView.setInStealthMode(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(PREF_PATTERN_HIDE_TRAIL, OPTION_PATTERN_HIDE_TRAIL_DEFAULT));

    }

    /**
     * Check pin
     */
    private void checkPin() {

        if (getCurrentPinTries() >= PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(PREF_PIN_MAX_TRIES, OPTION_PIN_MAX_TRIES_DEFAULT)) {
            Toast.makeText(getApplicationContext(), R.string.pin_locked_enter_password, Toast.LENGTH_SHORT).show();
            checkPassword(PIN);
            return;
        }

        dialog.setTitle(getString(R.string.check_pin));
        hideAll();

        content.findViewById(R.id.layoutPin).setVisibility(View.VISIBLE);

        if (mPinView == null) mPinView = new PinView();
        mPinView.setDots((LinearLayout) content.findViewById(R.id.layoutPinDots));
        mPinView.getDots().setLength(getCurrentLength());
        mPinView.getDots().setAutoSubmit(true);
        mPinView.setKeypad((GridLayout) content.findViewById(R.id.layoutPinKeypad));

        mOk.setOnClickListener(onPinCheck);
        mNeutral.setVisibility(View.VISIBLE);
        mNeutral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPassword("");
            }
        });

        checkPinTries();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    }

    private void checkPinTries() {
        int max = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(PREF_PIN_MAX_TRIES, OPTION_PIN_MAX_TRIES_DEFAULT);

        if (getCurrentPinTries() >= max / 2) {
            mWarning.setText(getResources().getString(R.string.attempts_remaining, max - getCurrentPinTries()));
            mWarning.setVisibility(View.VISIBLE);
        }
    }


    /**
     * Check fingerprint
     */
    private void checkFingerprint() {

        Utils.checkFingerprintHardware(getApplicationContext());

        if ((Utils.getFingerprintMode() & Utils.FINGERPRINT_NOT_ALLOWED) == Utils.FINGERPRINT_NOT_ALLOWED) {
            Toast.makeText(getApplicationContext(), R.string.fingerprint_hardware_not_found, Toast.LENGTH_SHORT).show();
            checkPassword(FINGERPRINT);
            return;
        }

        if ((Utils.getFingerprintMode() & Utils.FINGERPRINT_HAS_ENROLLED_FINGERPRINTS) == Utils.FINGERPRINT_HAS_ENROLLED_FINGERPRINTS) {

            dialog.setTitle(getString(R.string.check_fingerprint));
            hideAll();

            content.findViewById(R.id.layoutFingerprint).setVisibility(View.VISIBLE);
            mNeutral.setVisibility(View.VISIBLE);
            mNeutral.setText(R.string.password);
            mNeutral.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkPassword("");
                }
            });

            ((TextView) content.findViewById(R.id.tvFingerprint)).setText(R.string.touch_sensor);
            try {
                Digitus.init(this, getString(R.string.app_name), 69, this);

            } catch (Exception e) {//TODO improve behaviour
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), R.string.error_with_fingerprint, Toast.LENGTH_SHORT).show();
                checkPassword(FINGERPRINT);
            }

//        Digitus.get().

        } else {
            Toast.makeText(getApplicationContext(), R.string.fingerprints_not_found, Toast.LENGTH_SHORT).show();
            checkPassword(FINGERPRINT);
        }
    }


    View.OnClickListener onPasswordCheck = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //password checking
            mWarning.setVisibility(View.GONE);
            mWarning.setText("");

            if (getPassword().equals(Utils.getEncryptedHash(mPasswordView.getText().toString(), 512))) {
                successUnlock();
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                return;
            }
            Toast.makeText(getApplicationContext(), getString(R.string.password_not_accepted), Toast.LENGTH_SHORT).show();

            mNeutral.setVisibility(View.VISIBLE);
            mNeutral.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPasswordView.setText("");
                    mWarning.setVisibility(View.GONE);
                }
            });

            mPasswordView.requestFocus();
            setCurrentTries(getCurrentTries() + 1);

            if (getCurrentTries() >= Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(PREF_SECURITY_TRIES_BEFORE_DELAY, "5"))) {
                delayBeforeNextTry(PASSWORD);
            }

        }
    };

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

    public String getPassword() {
        if (password == null) {
            password = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(PASSWORD, null);
        }
        return password;
    }


    private void setupFingerprint() {

        if (getPassword() == null) {
            setupPassword(SETUP_FINGERPRINT);
            return;
        }
        dialog.setTitle(getString(R.string.set_new_pattern));

        Utils.checkFingerprintHardware(getApplicationContext());

        if ((Utils.getFingerprintMode() & Utils.FINGERPRINT_NOT_ALLOWED) == Utils.FINGERPRINT_NOT_ALLOWED) {
            Toast.makeText(getApplicationContext(), R.string.fingerprint_hardware_not_found, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
            return;
        }

        try {
//            Digitus.init(this, getString(R.string.app_name), 69, this);
            startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), SETUP_FINGERPRINT);
            Intent intent1 = new Intent();
            setResult(RESULT_OK, intent1);
            finish();
            if (Digitus.get().openSecuritySettings()) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), R.string.error_setting_up_fingerprint, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        } catch (Exception e) {//TODO improve behaviour
            e.printStackTrace();
            setupPassword(SETUP_FINGERPRINT);
        }
//        FingerprintDialog.show(LockActivity.this, getString(R.string.app_name), 69);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    }


    @Override
    protected void onResume() {
        super.onResume();
       /* Digitus.init(this, getString(R.string.app_name), 69, this);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Starts listening for a fingerprint
                Digitus.get().startListening();
            }
        });*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Calling this method automatically makes a call to stopListening() if necessary
//        Digitus.deinit();
    }


    @Override
    public void onDigitusReady(Digitus digitus) {
        System.out.println("DIGITUS READY");
        digitus.startListening();
    }

    @Override
    public void onDigitusListening(boolean b) {
        System.out.println("DIGITUS LISTEN");

    }

    @Override
    public void onDigitusAuthenticated(Digitus digitus) {
        System.out.println("DIGITUS AUTHEN");
        ((ImageView) content.findViewById(R.id.ivFingerprint)).setImageResource(R.drawable.ic_fingerprint_success);
        ((TextView) content.findViewById(R.id.tvFingerprint)).setText(R.string.access_granted);
        Digitus.deinit();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void o) {
                super.onPostExecute(o);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }.execute();

    }

    @Override
    public void onDigitusError(Digitus digitus, DigitusErrorType type, Exception e) {
        switch (type) {
            case FINGERPRINT_NOT_RECOGNIZED:
                ((ImageView) content.findViewById(R.id.ivFingerprint)).setImageResource(R.drawable.ic_fingerprint_error);
                ((TextView) content.findViewById(R.id.tvFingerprint)).setText(R.string.fingerprint_not_recognized);
                digitus.startListening();
                break;
            case FINGERPRINTS_UNSUPPORTED:
                digitus.stopListening();
                checkPassword(FINGERPRINT);
                Toast.makeText(getApplicationContext(), R.string.error_with_fingerprint, Toast.LENGTH_SHORT).show();
                System.out.println("FP_ERRRO " + getString(R.string.status_error, e.getMessage()));
                break;
            case HELP_ERROR:
                digitus.stopListening();
                checkPassword(FINGERPRINT);
                Toast.makeText(getApplicationContext(), R.string.error_with_fingerprint, Toast.LENGTH_SHORT).show();
                System.out.println("FP_ERRRO " + getString(R.string.status_error, e.getMessage()));
                break;
            case PERMISSION_DENIED:
                digitus.stopListening();
                checkPassword(FINGERPRINT);
                Toast.makeText(getApplicationContext(), R.string.fingerprint_denied, Toast.LENGTH_SHORT).show();
                System.out.println("FP_ERRRO " + getString(R.string.status_error, e.getMessage()));
                break;
            case REGISTRATION_NEEDED:
                digitus.stopListening();
                checkPassword(FINGERPRINT);
                Toast.makeText(getApplicationContext(), R.string.fingerprints_not_found, Toast.LENGTH_SHORT).show();
                System.out.println("FP_ERRRO " + getString(R.string.status_error, e.getMessage()));
                break;
            case UNRECOVERABLE_ERROR:
                digitus.stopListening();
                checkPassword(FINGERPRINT);
                Toast.makeText(getApplicationContext(), R.string.error_with_fingerprint, Toast.LENGTH_SHORT).show();
                System.out.println("FP_ERRRO " + getString(R.string.status_error, e.getMessage()));
                break;
        }
    }

    public int getPasswordInitialMethod() {
        return passwordInitialMethod;
    }

    public void setPasswordInitialMethod(int passwordInitialMethod) {
        this.passwordInitialMethod = passwordInitialMethod;
    }

    public String getPreSet() {
        return preSet;
    }

    public void setPreSet(String preSet) {
        this.preSet = preSet;
    }

    public int getCurrentLength() {
        return currentLength;
    }

    public void setCurrentLength(int currentLength) {
        this.currentLength = currentLength;
    }

    public int getCurrentTries() {
        return currentTries;
    }

    public void setCurrentTries(int currentTries) {
        this.currentTries = currentTries;
    }

}

