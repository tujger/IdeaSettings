package com.pantheon_inc.odyssey.android.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Account {

    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "user1:Richard300611", "wtiger:richard"};

    private static final String ACCOUNT_LAST_ID = "accountLastId";
    public static final String ACCOUNT_CURRENT_ID = "accountCurrentId";
    public static final String ACCOUNT_URL = "url";
    private static final String ACCOUNT_TITLE = "title";
    private static final String ACCOUNT_USERNAME = "username";
    private static final String ACCOUNT_PASSWORD = "password";
    private static final String ACCOUNT_REMEMBER_PASSWORD = "rememberPassword";
    private static final String ACCOUNT_ERROR_STATE = "errorState";
    private static final String ACCOUNT_ERROR_CODE = "errorCode";
    private static final String ACCOUNT_SESSION_ID = "sessionId";

    public static final int ERROR_NOERROR = 0;
    public static final int ERROR_USERID_INVALID = 1;
    public static final int ERROR_PASSWORD_INVALID = 2;
    public static final int ERROR_OLD_VERSION = 3;
    public static final int ERROR_NETWORK_NOT_ALLOWED = 4;
    public static final int ERROR_ANOTHER_ERROR = 100;


    private static Context context;

    /*
     * Unique integer identifier in SharedPreferences
     */
    private int id;
    private URL url;
    private String title = "";
    private String username;
    private String password = "";
    private boolean errorState;
    private int errorCode = 0;
    private String sessionId = "";
    private boolean rememberPassword = true;

    /**
     * Initialize account part. Required for using this class. Use it with
     * getApplicationContext().
     */
    public static void initialize(Context context) {
//        if (Account.context != null) {
//			throw new IllegalAccessError("Trying to change context is illegal.");
//        }
        Account.context = context;
    }

    /**
     * Creates, loads and returns object for the last active account
     */
    public static Account getCurrentAccount() {
        Account s = new Account();
        if (s.load(Account.getCurrentId()))
            return s;
        else
            return null;
    }

    /**
     * Returns the ID of current account.
     */
    public static int getCurrentId() {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sp.getInt(ACCOUNT_CURRENT_ID, 0);
    }

    /**
     * Returns the ID of last registered account.
     */
    public static int getLastId() {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sp.getInt(ACCOUNT_LAST_ID, 0);
    }

    /**
     * Returns the counter of accounts registered.
     */
    public static int getCount() {
        int count = 0;

        int lastId = Account.getLastId();
        SharedPreferences sp;

        for (int i = 1; i <= lastId; i++) {
            sp = context.getSharedPreferences("account_" + i,
                    Context.MODE_PRIVATE);

            if (sp.contains(ACCOUNT_URL))
                ++count;
        }

        return count;
    }

    public Account() {
    }

    public Account(String url, String username, String password) throws MalformedURLException {
        this(new URL(url), username, password);
    }

    public Account(URL url, String username, String password) {
        setUrl(url);
        setUsername(username);
        setPassword(password);
    }

    public boolean load() {
        return load(getId());
    }

    /**
     * Loads properties of account with ID.
     *
     * @return <code>true</code> if success, <code>false</code> otherwise
     */
    public boolean load(int id) {

        SharedPreferences sp = context.getSharedPreferences("account_" + id,
                Context.MODE_PRIVATE);

        if (!sp.contains(ACCOUNT_URL)) {
            return false;
        }

        setId(id);
        try {
            setUrl(sp.getString(ACCOUNT_URL, ""));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        setTitle(sp.getString(ACCOUNT_TITLE, ""));
        setUsername(sp.getString(ACCOUNT_USERNAME, ""));
        setPassword(sp.getString(ACCOUNT_PASSWORD, ""));
        this.errorState = sp.getBoolean(ACCOUNT_ERROR_STATE, false);
        this.sessionId = sp.getString(ACCOUNT_SESSION_ID, "");
        return true;

    }

    /**
     * Adds new account ID, sets it as default and saves the account.
     */
    public boolean add() {

        int lastId = Account.getLastId();

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);

        Editor editor = sp.edit();
        editor.putInt(ACCOUNT_LAST_ID, ++lastId);
        editor.putInt(ACCOUNT_CURRENT_ID, lastId);

        setId(lastId);

        boolean res = save();
        if (res)
            editor.apply();

        return res;
    }

    /**
     * Saves account properties to the preferences file.
     */
    public boolean save() {

        if (!(id > 0)) {
            try {
                throw new Exception("Identifier not defined.");
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        SharedPreferences sp = context.getSharedPreferences("account_" + id,
                Context.MODE_PRIVATE);
        Editor editor = sp.edit();

        editor.putString(ACCOUNT_URL, url.toString());
        editor.putString(ACCOUNT_TITLE, title);
        editor.putString(ACCOUNT_USERNAME, username);
        editor.putBoolean(ACCOUNT_REMEMBER_PASSWORD, rememberPassword);
        editor.putString(ACCOUNT_PASSWORD, rememberPassword ? password : "");

        return editor.commit();
    }

    /**
     * Check credentials for the account
     */
    public static void checkCredentials(final String url, final String username, final String password) {

        new AsyncTask() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Object doInBackground(Object[] params) {
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
            }
        }.execute();

    }


    /**
     * Checks credentials for account.
     */
    public void checkCredentials() {
        Account.checkCredentials(url.toString(), username, password);
    }

    /**
     * Checks accessibility of the server and its Odyssey relation.
     *
     * @throws IOException
     */
    public static boolean checkServer(String url) throws IOException {
        return Utils.getUrl(url + "/odyssey/index.ody").contains("Odyssey");
    }

    /**
     * Checks accessibility of the server and its Odyssey relation.
     */
    public boolean checkServer() throws IOException {
        return checkServer(getUrl().toString());
    }

    public URL getUrl() {
        System.out.println("URI: " + url.toString());
        return url;
    }

    public Account setUrl(URL url) {
        try {
            this.url = new URL(url.getProtocol(), url.getHost(), url.getPort(), "");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        System.out.println("URL: " + this.url);
        return this;
    }

    public Account setUrl(String url) throws MalformedURLException {
        setUrl(new URL(url));
        return this;
    }

    public String getUsername() {
        return username;
    }

    public Account setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public boolean hasPassword() {
        return password.length() > 0;
    }

    public Account setPassword(String password) {
        this.password = password;
        return this;
    }

    public int getId() {
        return id;
    }

    public Account setId(int id) {
        this.id = id;
        return this;
    }

    public String toString() {
        return "Id:" + id
                + (hasTitle() ? ", Title: " + title : "")
                + ", Uri:" + url + " Username:" + username;
    }

    public boolean isErrorState() {
        return errorState;
    }

    public Account setErrorState(boolean errorState) {
        this.errorState = errorState;
        context.getSharedPreferences("account_" + id, Context.MODE_PRIVATE).edit().putBoolean(ACCOUNT_ERROR_STATE, errorState).apply();
        if (!errorState) setErrorCode(ERROR_NOERROR);
        return this;
    }

    public String getSessionId() {
        if (sessionId == null) sessionId = "";
        return sessionId;
    }

    public boolean hasSessionId() {
        return sessionId.length() > 0;
    }

    public Account setSessionId(String sessionId) {
        this.sessionId = sessionId;
        if (sessionId != null) {
            context.getSharedPreferences("account_" + id, Context.MODE_PRIVATE).edit().putString(ACCOUNT_SESSION_ID, sessionId).apply();
            System.out.println("SAVE SESSION ID " + sessionId);
        }
        return this;
    }

    public String getTitle() {
        return title;
    }

    public boolean hasTitle() {
        return title.length() > 0;
    }

    public Account setTitle(String title) {
        this.title = title;
        return this;
    }

    public boolean isRememberPassword() {
        return rememberPassword;
    }

    public Account setRememberPassword(boolean rememberPassword) {
        this.rememberPassword = rememberPassword;
        return this;
    }

    public int getErrorCode() {
        return context.getSharedPreferences("account_" + id, Context.MODE_PRIVATE).getInt(ACCOUNT_ERROR_CODE, ERROR_NOERROR);
    }

    public Account setErrorCode(int errorCode) {
        this.errorCode = errorCode;
        context.getSharedPreferences("account_" + id, Context.MODE_PRIVATE).edit().putInt(ACCOUNT_ERROR_CODE, errorCode).apply();
        return this;
    }

    public Account setErrorCode(String errorMessage) {

        if(errorMessage.contains("old version of Odyssey server")){
            setErrorCode(ERROR_OLD_VERSION);
        }else{
            setErrorCode(ERROR_ANOTHER_ERROR);
        }

        return this;
    }



}
