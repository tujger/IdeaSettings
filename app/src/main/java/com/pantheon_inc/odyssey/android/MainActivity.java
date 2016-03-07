package com.pantheon_inc.odyssey.android;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.context.IconicsContextWrapper;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;
import com.pantheon_inc.odyssey.R;
import com.pantheon_inc.odyssey.android.helpers.Account;
import com.pantheon_inc.odyssey.android.helpers.OdysseyWebView;
import com.pantheon_inc.odyssey.android.helpers.Utils;
import com.pantheon_inc.odyssey.android.helpers.WebAppInterface;

import java.util.HashMap;

import static com.pantheon_inc.odyssey.R.drawable;
import static com.pantheon_inc.odyssey.R.id;
import static com.pantheon_inc.odyssey.R.layout;
import static com.pantheon_inc.odyssey.R.string;


public class MainActivity extends AppCompatActivity {
    private static final int MENU_ADD_ACCOUNT = 100000;
    public static final int MENU_APPS = 100001;
    public static final int MENU_MESSAGES = 100002;
    public static final int MENU_INBOX = 100003;
    public static final int MENU_PROFILE = 100004;
    public static final int MENU_REFRESH = 100005;
    public static final int MENU_INFO = 100006;
    private static final int MENU_ACCOUNT_OPTIONS = 100100;
    private static final int MENU_ACCOUNT_HELP = 100101;
    private static final int MENU_HELP = 100103;
    private static final int MENU_SETTINGS = 100104;

    private static final int REQUEST_OPTIONS = 1;

    //save our header or drawer
    private AccountHeader headerResult = null;
    private DrawerBuilder resultNormal = null, resultError = null;
    private Drawer drawer = null;
    private HashMap<Integer, Account> accounts;
    private int selectedAccount;
    private OdysseyWebView wv;
    private static Handler uiHandler;


    private boolean regularDrawer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.activity_main);

        // Handle Toolbar
        Toolbar toolbar = (Toolbar) findViewById(id.toolbar);
        setSupportActionBar(toolbar);

        uiHandler = new Handler() {
            // this will handle the notification gets from worker thead
            @Override
            public void handleMessage(Message msg) {
                Bundle b = msg.getData();

                int res = b.getInt(WebAppInterface.ACTION);

                if ((res & WebAppInterface.ACTION_HIDE_ALL) == WebAppInterface.ACTION_HIDE_ALL) {
                    findViewById(id.progressBar).setVisibility(View.GONE);
                    findViewById(id.layout_fatal_error).setVisibility(View.GONE);
                    wv.setVisibility(View.GONE);
                }

                if ((res & WebAppInterface.ACTION_SHOW_PROGRESS) == WebAppInterface.ACTION_SHOW_PROGRESS) {
                    findViewById(id.progressBar).setVisibility(View.VISIBLE);
                } else if ((res & WebAppInterface.ACTION_HIDE_PROGRESS) == WebAppInterface.ACTION_HIDE_PROGRESS) {
                    findViewById(id.progressBar).setVisibility(View.GONE);
                }

                if ((res & WebAppInterface.ACTION_SHOW_WEBVIEW) == WebAppInterface.ACTION_SHOW_WEBVIEW) {
                    if (!isRegularDrawer()) {
                        setRegularDrawer();
                    }
                    wv.setVisibility(View.VISIBLE);
                } else if ((res & WebAppInterface.ACTION_HIDE_WEBVIEW) == WebAppInterface.ACTION_HIDE_WEBVIEW) {
                    wv.setVisibility(View.GONE);
                }

                if ((res & WebAppInterface.ACTION_SHOW_ERROR) == WebAppInterface.ACTION_SHOW_ERROR) {
                    ((TextView) findViewById(id.tv_fatal_error)).setText(b.getString(WebAppInterface.ACTION_COMMENT));
                    findViewById(id.layout_fatal_error).setVisibility(View.VISIBLE);

                    accounts.get(selectedAccount).setErrorCode(b.getString(WebAppInterface.ACTION_COMMENT));
                    accounts.get(selectedAccount).setErrorState(true);
                    updateActiveProfileIcon();
                    headerResult.setActiveProfile(selectedAccount);

                    if (isRegularDrawer()) {
                        setRefreshDrawer();
                    }
                } else if ((res & WebAppInterface.ACTION_HIDE_ERROR) == WebAppInterface.ACTION_HIDE_ERROR) {
                    findViewById(id.layout_fatal_error).setVisibility(View.GONE);

                    accounts.get(selectedAccount).setErrorState(false);
                    updateActiveProfileIcon();
                    headerResult.setActiveProfile(selectedAccount);

                    if (!isRegularDrawer()) {
                        setRegularDrawer();
                    }
                }

                if ((res & WebAppInterface.ACTION_LOGIN_SUCCESS) == WebAppInterface.ACTION_LOGIN_SUCCESS) {
                    accounts.get(selectedAccount).setErrorState(false);
                    updateActiveProfileIcon();
                    wv.loginSuccess();
                }
                if ((res & WebAppInterface.ACTION_LOGIN) == WebAppInterface.ACTION_LOGIN) {
                    wv.login();
                }
                if ((res & WebAppInterface.ACTION_REFRESH) == WebAppInterface.ACTION_REFRESH) {
                    setRefreshDrawer();
                    new SwitchToAccount(selectedAccount).go();
//                  wv.login();
                }
                if ((res & WebAppInterface.ACTION_SHOW_REFRESH) == WebAppInterface.ACTION_SHOW_REFRESH) {
                    setRefreshDrawer();
//                  wv.login();
                }
            }
        };

        wv = (OdysseyWebView) findViewById(id.wv_odyssey);
        if (savedInstanceState != null) {
            wv.restoreState(savedInstanceState);
        }

        wv.setUiHandler(uiHandler);

        // Create the AccountHeader
        AccountHeaderBuilder ahb = new AccountHeaderBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withHeaderBackgroundScaleType(ImageView.ScaleType.FIT_XY)
                .withHeaderBackground(drawable.drawer_bg)
                .withTranslucentStatusBar(true)
                .withNameTypeface(Typeface.DEFAULT_BOLD)
                .withTextColor(Color.BLACK);

        accounts = new HashMap<>();
        int x = 0;
        for (int i = 1; i <= Account.getLastId(); i++) {
            Account s = new Account();

            if (s.load(i)) {
                System.out.println(s);

                IProfile p = new ProfileDrawerItem()
                        .withIdentifier(i)
                        .withName(s.hasTitle() ? s.getTitle().toUpperCase() : s.getUsername().toUpperCase())
                        .withEmail(s.hasTitle() ? "as " + (s.getUsername()) : s.getUrl().toString())
                        .withNameShown(true)
                        .withIcon(getRelatedIcon(s));

                accounts.put(i, s);
                x++;
                ahb.addProfiles((IProfile) p);
                if (Account.getCurrentId() == i) {
                    selectedAccount = x;
                }
            }
        }

        ahb.addProfiles(
                new ProfileSettingDrawerItem().withName(getString(string.add_account)).withDescription(getString(string.add_odyssey_account)).withIcon(GoogleMaterial.Icon.gmd_add).withIdentifier(MENU_ADD_ACCOUNT),
                new ProfileSettingDrawerItem().withName(getString(string.settings)).withIcon(GoogleMaterial.Icon.gmd_settings).withIdentifier(MENU_SETTINGS),
                new ProfileSettingDrawerItem().withName(getString(string.help)).withIcon(GoogleMaterial.Icon.gmd_help).withIdentifier(MENU_HELP)
        )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        //sample usage of the onProfileChanged listener
                        //if the clicked item has the identifier 1 add a new profile ;)
                        if (profile instanceof IDrawerItem) {
                            int id = (int) profile.getIdentifier();

                            switch (id) {
                                case MENU_ADD_ACCOUNT:
                                    Intent intent = new Intent(MainActivity.this, AddAccountActivity.class);
                                    startActivity(intent);
                                    break;
                                case MENU_SETTINGS:
                                    intent = new Intent(MainActivity.this, SettingsActivity.class);
                                    startActivity(intent);
                                    break;
                                case MENU_HELP:
                                    System.out.println("MAIN HELP");
                                    Toast.makeText(getApplicationContext(), "SHOW MAIN HELP", Toast.LENGTH_SHORT).show();
//                                    intent = new Intent(MainActivity.this, SettingsActivity.class);
//                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//                                    startActivity(intent);
                                    break;
                                default:
                                    selectedAccount = id;
                                    setRefreshDrawer();

                                    Message m = uiHandler.obtainMessage();
                                    Bundle uB = m.getData();
                                    uB.putInt(WebAppInterface.ACTION, WebAppInterface.ACTION_HIDE_ALL);
                                    m.setData(uB);
                                    uiHandler.sendMessage(m);

                                    new SwitchToAccount(id).go();
                            }

                            //false if you have not consumed the event and it should close the drawer
                            return false;
                        }
                        //false if you have not consumed the event and it should close the drawer
                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState);
        headerResult = ahb.build();

        //Create the drawer
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
//                .withItemAnimator(new AlphaCrossFadeAnimator())
                .withAccountHeader(headerResult) //set the AccountHeader we created earlier for the header
                .addDrawerItems(
//                        new PrimaryDrawerItem().withName(string.refresh).withIcon(GoogleMaterial.Icon.gmd_refresh).withIdentifier(MENU_REFRESH),
//                        new SectionDrawerItem().withName(string.general),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName(string.options).withIcon(GoogleMaterial.Icon.gmd_mode_edit).withIdentifier(MENU_ACCOUNT_OPTIONS).withSelectable(false),
                        new PrimaryDrawerItem().withName(string.help).withIcon(GoogleMaterial.Icon.gmd_help_outline).withIdentifier(MENU_ACCOUNT_HELP).withSelectable(false)
//                        new SecondaryDrawerItem().withName(string.help).withIcon(getResources().getDrawable(drawable.ic_help_outline_black_24dp)).withIdentifier(MENU_HELP),
                ) // add the items we want to use with our Drawer
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        //check if the drawerItem is set.
                        //there are different reasons for the drawerItem to be null
                        //--> click on the header
                        //--> click on the footer
                        //those items don't contain a drawerItem

                        if (drawerItem != null) {
                            Intent intent = null;

                            //http://piusvs023:8080/odyssey/index.ody?hlk=eac04999&M.URL_PARAM=true&M.URL_HEADLESS=true&M.AUTO_DEPLOY=true&M.USER_ID=reepaa&M.PASSWORD=Test12
                            switch ((int) drawerItem.getIdentifier()) {
                                case MENU_APPS:
                                    setTitle(string.apps);
                                    wv.switchToApps();
                                    break;
                                case MENU_MESSAGES:
                                    setTitle(string.messages);
                                    wv.switchToMessages();
                                    break;
                                case MENU_INBOX:
                                    setTitle(string.inbox);
                                    wv.switchToInbox();
                                    break;
                                case MENU_ACCOUNT_OPTIONS:
                                    intent = new Intent(MainActivity.this, OptionsActivity.class);
                                    break;
                                case MENU_INFO:
                                    setTitle(string.info);
                                    wv.switchToInfo();
                                    break;
                                case MENU_PROFILE:
                                    setTitle(string.info);
                                    wv.switchToUserProfile();
                                    break;
                                case MENU_ACCOUNT_HELP:
                                    String url = wv.getAccount().getUrl() + "/odyssey/help/odysseyhelp.ody";
                                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                    System.out.println("ACCOUNT HELP " + url);
                                    break;
                                case MENU_REFRESH:
                                    System.out.println("REFRESH");
                                    setRefreshDrawer();
                                    new SwitchToAccount(selectedAccount).go();
//                                    wv.login();
                                    break;
                            }

                            if (intent != null) {
                                MainActivity.this.startActivityForResult(intent, REQUEST_OPTIONS);
                            }
                        }

                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(true)
                .build();
        setRefreshDrawer();

        findViewById(id.btn_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SwitchToAccount(selectedAccount).go();
//                wv.login();
            }
        });


        if (savedInstanceState == null) {
            // set the selection to the item with the identifier 11
//            drawer.setSelection(21, true);
            //set the active profile
            if (accounts.get(selectedAccount) == null) {
                selectedAccount = Account.getCurrentId();
            }
            if (accounts.get(selectedAccount) == null) {
                selectedAccount = Account.getLastId();
            }
            System.out.println("ACCOUNT =" + selectedAccount);

            headerResult.setActiveProfile(selectedAccount);
            new SwitchToAccount(selectedAccount).go();
        }

//        drawer.updateBadge("5", 0);
//        drawer.updateBadge("10", 1);
    }

    private Drawable getRelatedIcon(Account account) {
        //https://github.com/amulyakhare/TextDrawable
//        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
//        int color = generator.getColor(account.getUrl().toString() + account.getUsername());

        String str;
        if (account.hasTitle()) str = account.getTitle();
        else str = account.getUsername();

        if (account.isErrorState()) {
            return new IconicsDrawable(this, GoogleMaterial.Icon.gmd_warning).actionBarSize().paddingDp(3).colorRes(R.color.material_red_500);
        }
        return TextDrawable.builder()
                .beginConfig()
                .width(60)
                .height(60)
                .fontSize(30)
                .textColor(Color.WHITE)
                .toUpperCase()
                .bold()
                .endConfig()
                .buildRect(str.substring(0, 1),
                        ColorGenerator.MATERIAL.getColor(account.getUrl().toString() + account.getUsername())
                );
    }

    private void updateActiveProfileIcon() {

        Account account = accounts.get(selectedAccount);
        account.load();

        IProfile p = headerResult.getActiveProfile();

        p.withName(account.hasTitle() ? account.getTitle().toUpperCase() : account.getUsername().toUpperCase());
        p.withEmail(account.hasTitle() ? "as " + (account.getUsername()) : account.getUrl().toString());
        p.withIcon(getRelatedIcon(account));

        headerResult.updateProfile(p);
    }

    private void setRegularDrawer() {
        regularDrawer = true;
        if (drawer.switchedDrawerContent()) drawer.resetDrawerContent();
        drawer.removeItems(MENU_REFRESH, MENU_APPS, MENU_MESSAGES, MENU_INBOX, MENU_INFO, MENU_PROFILE);
        drawer.addItemsAtPosition(1,
                new PrimaryDrawerItem().withName(string.apps).withIcon(GoogleMaterial.Icon.gmd_view_headline).withIdentifier(MENU_APPS),
                new PrimaryDrawerItem().withName(string.messages).withIcon(GoogleMaterial.Icon.gmd_mail_outline).withIdentifier(MENU_MESSAGES),
                new PrimaryDrawerItem().withName(string.inbox).withIcon(GoogleMaterial.Icon.gmd_view_list).withIdentifier(MENU_INBOX),
                new PrimaryDrawerItem().withName(string.profile).withIcon(GoogleMaterial.Icon.gmd_account_circle).withIdentifier(MENU_PROFILE)
        );
        drawer.addItemAtPosition(
                new PrimaryDrawerItem().withName(string.info).withIcon(GoogleMaterial.Icon.gmd_info_outline).withIdentifier(MENU_INFO)
                , 7);
        drawer.getDrawerLayout();
    }

    private void setRefreshDrawer() {
        regularDrawer = false;
        if (drawer.switchedDrawerContent()) drawer.resetDrawerContent();
        drawer.removeItems(MENU_REFRESH, MENU_APPS, MENU_MESSAGES, MENU_INBOX, MENU_INFO, MENU_PROFILE);
        drawer.addItemAtPosition(new PrimaryDrawerItem().withName(string.refresh).withIcon(GoogleMaterial.Icon.gmd_refresh).withIdentifier(MENU_REFRESH), 1);
    }

    public boolean isRegularDrawer() {
        return regularDrawer;
    }

    class SwitchToAccount {
        private int id;
        private Account account;
        SwitchToAccount sta;

        public SwitchToAccount(int accountId) {
            this.id = accountId;
            sta = this;
            System.out.println("TRYING TO SET ACTIVE PROFILE " + accountId);
            setAccount(accounts.get(accountId));
            wv.setAccount(accounts.get(accountId));
        }

        public void go() {
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt(Account.ACCOUNT_CURRENT_ID, id).apply();

            if (accounts.get(id).hasPassword()) {
                doLogin();
            } else {

                final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
                final View content = getLayoutInflater().inflate(layout.activity_options, null);
                final TextView mWarning = (TextView) content.findViewById(R.id.tvWarning);
                final EditText etPassword = (EditText) content.findViewById(R.id.etPassword);
                final CheckBox cbRememberPassword = (CheckBox) content.findViewById(R.id.cbRememberPassword);

                dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        cancelClicked(dialog);
                    }
                });

                dialog.setTitle(string.enter_odyssey_password);

                dialog.setView(content);
                content.findViewById(R.id.vServer).setVisibility(View.GONE);
                content.findViewById(R.id.vUserid).setVisibility(View.GONE);
                content.findViewById(R.id.vTitle).setVisibility(View.GONE);
                mWarning.setVisibility(View.GONE);
                content.findViewById(R.id.pbLogin).setVisibility(View.GONE);

                dialog.show();
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface d) {
                        cancelClicked(dialog);
                    }
                });
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String str = etPassword.getText().toString();

                        System.out.println("CLICKE DOK "+str);

                        if (str.length() > 0) {
                            sta.getAccount().setRememberPassword(false);
                            sta.getAccount().setSessionId("");
                            sta.getAccount().setPassword(str);
                            if (cbRememberPassword.isChecked()) {
                                sta.getAccount().setRememberPassword(true);
                            }
                            sta.getAccount().save();
                            sta.doLogin();
                            dialog.dismiss();
                        } else {
                            mWarning.setText(R.string.password_too_short);
                            mWarning.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }

        private void cancelClicked(AlertDialog v){
            Utils.sendHandlerMessage(uiHandler, WebAppInterface.ACTION_HIDE_ALL | WebAppInterface.ACTION_SHOW_ERROR, getString(string.password_required));
            v.dismiss();
        };

        private void doLogin() {

            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            findViewById(R.id.sv_login_form).setVisibility(View.GONE);
            findViewById(R.id.layout_fatal_error).setVisibility(View.GONE);
            wv.setVisibility(View.INVISIBLE);

            wv.login();
        }

        public Account getAccount() {
            return account;
        }

        public void setAccount(Account account) {
            this.account = account;
        }
    }


    private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(IDrawerItem drawerItem, CompoundButton buttonView, boolean isChecked) {
            if (drawerItem instanceof Nameable) {
                Log.i("material-drawer", "DrawerItem: " + ((Nameable) drawerItem).getName() + " - toggleChecked: " + isChecked);
            } else {
                Log.i("material-drawer", "toggleChecked: " + isChecked);
            }
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = drawer.saveInstanceState(outState);
        //add the values which need to be saved from the accountHeader to the bundle
        outState = headerResult.saveInstanceState(outState);
        //add the values which need to be saved from the accountHeader to the bundle
        wv.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (drawer != null && drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_OPTIONS && resultCode == RESULT_OK) {
            updateActiveProfileIcon();
            new SwitchToAccount(selectedAccount).go();

        } else if (requestCode == REQUEST_OPTIONS && resultCode == RESULT_CANCELED) {
            System.out.println("OPTIONS CANCELLED");
        }
    }
}
