package com.pantheon_inc.odyssey.android.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;

import com.alirezaafkar.json.requester.Requester;
import com.alirezaafkar.json.requester.interfaces.Methods;
import com.alirezaafkar.json.requester.interfaces.Response;
import com.alirezaafkar.json.requester.requesters.JsonObjectRequester;
import com.alirezaafkar.json.requester.requesters.RequestBuilder;
import com.android.volley.VolleyError;
import com.pantheon_inc.odyssey.R;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Account {
    public static final String ACCOUNT_LAST_ID = "accountLastId";
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

    private static final int REQUEST_CHECK_LOGIN = 0;

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
//    private int errorCode = 0;
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
        File file;

        for (int i = 1; i <= lastId; i++) {
            file = new File(context.getFilesDir().getParent() + "/shared_prefs", "account_" + i + ".xml");
            if (!file.exists()) continue;

            sp = context.getSharedPreferences("account_" + i,
                    Context.MODE_PRIVATE);
            if (!sp.contains(ACCOUNT_URL)) continue;
            ++count;
        }

        return count;
    }

    /**
     * Returns the ID of first registered account.
     */
    public static int getFirstId() {
        int lastId = Account.getLastId();
        SharedPreferences sp;

        for (int i = 1; i <= lastId; i++) {
            sp = context.getSharedPreferences("account_" + i,
                    Context.MODE_PRIVATE);

            if (sp.contains(ACCOUNT_URL))
                return i;
        }
        return 0;
    }

    public Account() {
    }

/*
    public Account(String url, String username, String password) throws MalformedURLException {
        this(new URL(url), username, password);
    }
*/

/*
    public Account(URL url, String username, String password) {
        setUrl(url);
        setUsername(username);
        setPassword(password);
    }
*/

    public boolean load() {
        return load(getId());
    }

    /**
     * Loads properties of account with ID.
     *
     * @return <code>true</code> if success, <code>false</code> otherwise
     */
    public boolean load(int id) {

        File file = new File(context.getFilesDir().getParent() + "/shared_prefs", "account_" + id + ".xml");
        if (!file.exists()) return false;

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
                throw new Exception(context.getString(R.string.identifier_not_defined));
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        SharedPreferences sp = context.getSharedPreferences("account_" + id, Context.MODE_PRIVATE);
        Editor editor = sp.edit();

        editor.putString(ACCOUNT_URL, url.toString());
        editor.putString(ACCOUNT_TITLE, title);
        editor.putString(ACCOUNT_USERNAME, username);
        editor.putBoolean(ACCOUNT_REMEMBER_PASSWORD, rememberPassword);
        editor.putString(ACCOUNT_PASSWORD, rememberPassword ? password : "");

        return editor.commit();
    }

    public boolean delete() {
        boolean res = false;
        File file = new File(context.getFilesDir().getParent() + "/shared_prefs", "account_" + id + ".xml");
        if (file.exists() && file.delete()) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(ACCOUNT_CURRENT_ID, 0).commit();
            res = true;
        }
        return res;
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

    /**
     * First stage of server checking. Tries to connect server and get the page. Checks content of this page for
     * present of some substrings and throws an error if found. Else in success calls the second stage of checking.
     */
    public void tryServer(Response.ObjectResponse restRequester){
        tryServer(getUrl().toString(),username,password,restRequester);
    }

    /**
     * First stage of server checking. Tries to connect server and get the page. Checks content of this page for
     * present of some substrings and throws an error if found. Else in success calls the second stage of checking.
     * @param restRequester - callbacks for result
     */
    public static void tryServer(final String url, final String username, final String password, final Response.ObjectResponse restRequester) {
        new AsyncTask<Void,Void,Boolean>() {
            String message = "";
            @Override
            protected Boolean doInBackground(Void... params) {
                boolean res=true;
                String content = "";
                try {
                    content = Utils.getUrl(url + "/odyssey/index.ody");
                } catch (IOException e) {
                    if(e.getClass().getSimpleName().equals("FileNotFoundException")){
                        message = context.getString(R.string.not_an_odyssey_server);
                    }else{
                        message = e.getMessage();
                    }
                    e.printStackTrace();
                    res=false;
                }

                if(res && !(content.indexOf("Odyssey")>0)){
                    res=false;
                    message = context.getString(R.string.not_an_odyssey_server);
                }else if(content.indexOf("has been accessed via an unlicensed")>0){
                    res=false;
                    message = context.getString(R.string.trying_to_access_via_unlicensed_ip);
                }else if(res) {
                    try {
                        Utils.getUrl(url + "/odyssey/rhaps.ody?action=getVersion");
                    } catch (IOException e) {
                        if (e.getClass().getSimpleName().equals("FileNotFoundException")) {
                            message = context.getString(R.string.odyssey_server_too_old);
                        } else {
                            message = e.getMessage();
                        }
                        e.printStackTrace();
                        res = false;
                    }
                }
                return res;
            }

            @Override
            protected void onPostExecute(Boolean res) {
                super.onPostExecute(res);
                if(res){
                    tryServerSecondStage(url,username,password,restRequester);
                }else{
                    restRequester.onFinishResponse(0, new VolleyError("ERR"), message);
                }
            }
        }.execute();
    }

    public static void tryServerSecondStage(String url, String username, String password,Response.ObjectResponse restRequester){
        //rest request init
        Map<String, String> header = new HashMap<>();
        header.put("charset", "utf-8");
        Requester.Config config = new Requester.Config(context);
        config.setHeader(header);
        Requester.init(config);

        String deviceId = Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ANDROID_ID);
        String deviceName = android.os.Build.MODEL;

        JsonObjectRequester mRequester = new RequestBuilder(context)
                .requestCode(REQUEST_CHECK_LOGIN)
                .timeOut(15000)
                .shouldCache(true)
                .addToHeader("deviceid", deviceId)//FIXME remove
                .addToHeader("platformtype", deviceName)//FIXME remove
                .addToHeader("platformid", "android")//FIXME remove
                .buildObjectRequester(restRequester);

        String link = url + "/odyssey/rhaps.ody?";
        ArrayList<String> pars = new ArrayList<>();
        pars.add("action=getVersion");

        try {
            if (!TextUtils.isEmpty(username)) {
                pars.add("action=checkUserid");
                pars.add("deviceid=" + URLEncoder.encode(deviceId, "UTF-8"));
                pars.add("platformtype=" + URLEncoder.encode(deviceName, "UTF-8"));
                pars.add("platformid=android");
                pars.add("userid=" + URLEncoder.encode(username, "UTF-8"));
            }
            if (!TextUtils.isEmpty(password)) {
                pars.add("password=" + URLEncoder.encode(password, "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        link += TextUtils.join("&", pars);

        mRequester.request(Methods.GET, link);
    }

    public URL getUrl() {
        return url;
    }

    public Account setUrl(URL url) {
        try {
            this.url = new URL(url.getProtocol(), url.getHost(), url.getPort(), "");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
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
                + ", Url:" + url + " Username:" + username
                + (hasSessionId() ? ", SessionID: " + sessionId : "");
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
        context.getSharedPreferences("account_" + id, Context.MODE_PRIVATE).edit().putInt(ACCOUNT_ERROR_CODE, errorCode).apply();
        return this;
    }

    public Account setErrorCode(String errorMessage) {
        if (errorMessage.contains("old version of Odyssey server")) {
            setErrorCode(ERROR_OLD_VERSION);
        } else {
            setErrorCode(ERROR_ANOTHER_ERROR);
        }
        return this;
    }
}
