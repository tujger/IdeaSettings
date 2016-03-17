package com.pantheon_inc.odyssey.android.helpers;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class WebAppInterface {


    public static final String ACTION = "action";
    public static final int ACTION_SHOW_WEBVIEW = 1;
    public static final int ACTION_HIDE_WEBVIEW = 2;
    public static final int ACTION_SHOW_PROGRESS = 4;
    public static final int ACTION_HIDE_PROGRESS = 8;
    public static final int ACTION_SHOW_ERROR = 16;
    public static final int ACTION_HIDE_ERROR = 32;
    public static final int ACTION_LOGIN_SUCCESS = 64;
    public static final int ACTION_HIDE_ALL = 128;
    public static final int ACTION_LOGIN = 256;
    public static final int ACTION_SHOW_REFRESH = 512;
    public static final int ACTION_REFRESH = 1024;
    public static final int ACTION_SHOW_REGULAR_DRAWER = 2048;
    public static final String ACTION_COMMENT = "comment";

    private Handler uiHandler;
    private final Context mContext;
    private int requestsCounter = 0;
    private boolean mainScreen = false;

    /**
     * Instantiate the interface and set the context
     */
    WebAppInterface(Context c) {
        mContext = c;
    }

    /**
     * Show a toast from the web page
     */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void println(String str) {
        System.out.println(str);
    }

    @JavascriptInterface
    public void error(String message) {
        System.out.println("SEND ERROR TO UI :" + message);
        Message m = uiHandler.obtainMessage();
        Bundle uB = m.getData();
        uB.putInt(ACTION, ACTION_HIDE_ALL | ACTION_SHOW_ERROR);
        uB.putString(ACTION_COMMENT, message);
        m.setData(uB);
        uiHandler.sendMessage(m);
    }

    @JavascriptInterface
    public void show() {
        System.out.println("SEND MESSAGE TO UI FOR SHOW");
        Message m = uiHandler.obtainMessage();
        Bundle uB = m.getData();
        uB.putInt(ACTION, ACTION_HIDE_ALL | ACTION_SHOW_WEBVIEW | ACTION_SHOW_REGULAR_DRAWER);
        m.setData(uB);
        uiHandler.sendMessage(m);
    }

    @JavascriptInterface
    public void showLoginScreen() {
        System.out.println("SEND MESSAGE TO UI FOR SHOW");
        Message m = uiHandler.obtainMessage();
        Bundle uB = m.getData();
        uB.putInt(ACTION, ACTION_HIDE_ALL | ACTION_SHOW_WEBVIEW);
        m.setData(uB);
        uiHandler.sendMessage(m);
    }

    @JavascriptInterface
    public void login() {
        System.out.println("SEND MESSAGE TO UI FOR LOGIN");
        Message m = uiHandler.obtainMessage();
        Bundle uB = m.getData();
        uB.putInt(ACTION, ACTION_HIDE_ALL | ACTION_LOGIN);
        m.setData(uB);
        uiHandler.sendMessage(m);
    }

    @JavascriptInterface
    public void loginSuccess() {
        System.out.println("SEND MESSAGE TO UI FOR LOGIN SUCCESS");
        Message m = uiHandler.obtainMessage();
        Bundle uB = m.getData();
        uB.putInt(ACTION, ACTION_HIDE_ALL | ACTION_LOGIN_SUCCESS | ACTION_SHOW_REFRESH);
        m.setData(uB);
        uiHandler.sendMessage(m);
    }

    @JavascriptInterface
    public void hide() {
        System.out.println("SEND MESSAGE TO UI FOR HIDE");
        Message m = uiHandler.obtainMessage();
        Bundle uB = m.getData();
        uB.putInt(ACTION, ACTION_HIDE_ALL | ACTION_SHOW_PROGRESS);
        m.setData(uB);
        uiHandler.sendMessage(m);
    }

    @JavascriptInterface
    public void refresh() {
        System.out.println("SEND MESSAGE TO UI FOR REFRESH");
        Message m = uiHandler.obtainMessage();
        Bundle uB = m.getData();
        uB.putInt(ACTION, ACTION_HIDE_ALL | ACTION_REFRESH);
        m.setData(uB);
        uiHandler.sendMessage(m);
    }

    @JavascriptInterface
    public void increaseRequestsCounter() {
        requestsCounter++;
    }

    @JavascriptInterface
    public void resetRequestsCounter() {
        requestsCounter = 0;
    }

    public int getRequestsCounter() {
        return requestsCounter;
    }

    public Handler getUiHandler() {
        return uiHandler;
    }

    public void setUiHandler(Handler uiHandler) {
        this.uiHandler = uiHandler;
    }

    @JavascriptInterface
    public boolean isMainScreen() {
        boolean a = mainScreen;
        mainScreen=false;
        return a;
    }

    @JavascriptInterface
    public void setMainScreen(boolean mainScreen) {
        this.mainScreen = mainScreen;
    }
}