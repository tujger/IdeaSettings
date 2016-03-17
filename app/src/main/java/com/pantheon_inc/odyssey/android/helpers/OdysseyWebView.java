package com.pantheon_inc.odyssey.android.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.pantheon_inc.odyssey.R;
import com.pantheon_inc.odyssey.android.MainActivity;

import java.io.IOException;

/**
 * Odyssey specific WebView with implemented Javascript API.
 */
public class OdysseyWebView extends WebView {

    private static final String API_TYPE_LOGIN = "OdysseyMobileLogin.js";
    private static final String API_TYPE_UPDATE = "OdysseyMobileUpdateView.js";

    private Handler uiHandler;

    private final Context context;
    private Account account;
    private String api = "";
    private WebAppInterface wai;
    private String apiType = API_TYPE_LOGIN;


    public OdysseyWebView(final Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        if (!isInEditMode()) {

            /*setDownloadListener(new DownloadListener() {
                public void onDownloadStart(String url, String userAgent,
                                            String contentDisposition, String mimetype,
                                            long contentLength) {

                    System.out.println("DOWNLOAD START");

                    DownloadManager.Request request = new DownloadManager.Request(
                            Uri.parse(url));
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "download");
                    DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                    dm.enqueue(request);

                }
            });*/



            WebSettings webSettings = getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setSupportZoom(false);
            webSettings.setSupportMultipleWindows(false);

            wai = new WebAppInterface(context);
//                wai.setTaskCompleted(taskCompleted);

            addJavascriptInterface(wai, "OdysseyMobileAPI");
            setWebViewClient(new WebViewClient() {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    System.out.println("SHOULD OVERRIDE " + url);
                    view.loadUrl(url);
                    return false;
                }

                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    super.onReceivedError(view, request, error);
                    System.out.println("ERROR RECEIVER " + error.toString());

                    Message m = uiHandler.obtainMessage();
                    Bundle uB = m.getData();
                    uB.putInt(WebAppInterface.ACTION, WebAppInterface.ACTION_HIDE_ALL | WebAppInterface.ACTION_SHOW_ERROR);
                    uB.putString(WebAppInterface.ACTION_COMMENT, context.getString(R.string.network_error));
                    m.setData(uB);
                    uiHandler.sendMessage(m);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    System.out.println("PAGE FINISHED = " + url);

                    if (wai.getRequestsCounter() > 5) {
                        Message m = uiHandler.obtainMessage();
                        Bundle uB = m.getData();
                        uB.putInt(WebAppInterface.ACTION, WebAppInterface.ACTION_HIDE_ALL | WebAppInterface.ACTION_SHOW_ERROR);
                        uB.putString(WebAppInterface.ACTION_COMMENT, context.getString(R.string.network_error));
                        m.setData(uB);
                        uiHandler.sendMessage(m);
                    } else {
                        String a = Utils.readAsset(context.getAssets(), getApiType());
                        view.loadUrl("javascript:" + a + getApi());
                    }
                }
            });
            clearCache(true);

            setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int progress) {
//                        System.out.println("PROGRESS " + progress);
//                        if (progress == 100) {
//
//                            System.out.println("FINISHED");
//                        }
                }
            });
        }
    }

    private String getSessionId() {
        String res = "";

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        try {
            String[] temp = cookieManager.getCookie(account.getUrl().toString() + "/odyssey/").split(";");
            System.out.print("COOKIES ");
            for (String s : temp) {
                System.out.print(s + " ");
            }
            System.out.println("");

            for (String ar1 : temp) {
                if (ar1.contains("JSESSIONID")) {
                    String[] temp1 = ar1.split("=");
                    res = temp1[1];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return res;
    }


    public void setUiHandler(Handler uiHandler) {
        this.uiHandler = uiHandler;
        wai.setUiHandler(uiHandler);
    }

    @Override
    public void loadUrl(String url) {
        String x = url;
        if (x.startsWith("http")) {
            x += "/odyssey/index.ody";
            System.out.println("LOAD LOCATION " + x);
        } else {
            System.out.println("LOAD JAVASCRIPT ");
        }
        super.loadUrl(x);
    }

    public void login() {
        System.out.println("DO LOGIN. SESSIONID " + account.getSessionId());

        wai.hide();
        clearCache(true);//FIXME can be removed at the release stage

        new AsyncTask<Object, Boolean, Boolean>() {
            @Override
            protected Boolean doInBackground(Object... params) {
                boolean error;
                String errorText = "";
                try {
                    error = !account.checkServer();
                } catch (IOException e) {
                    e.printStackTrace();
                    error = true;
                    errorText = e.getLocalizedMessage();
                }
                if (error) {
                    Message m = uiHandler.obtainMessage();
                    Bundle uB = m.getData();
                    uB.putInt(WebAppInterface.ACTION, WebAppInterface.ACTION_HIDE_ALL | WebAppInterface.ACTION_SHOW_ERROR);
                    uB.putString(WebAppInterface.ACTION_COMMENT, errorText);
                    m.setData(uB);
                    uiHandler.sendMessage(m);
                    return false;
                }
                return true;
            }

            @SuppressLint("NewApi")
            @Override
            protected void onPostExecute(Boolean o) {
                if (o) {
                    CookieManager cookieManager = CookieManager.getInstance();
                    cookieManager.setAcceptCookie(true);
                    cookieManager.removeAllCookie();
                    if (account.getSessionId().length() > 0) {
                        cookieManager.setCookie(account.getUrl().toString() + "/odyssey/", "JSESSIONID=" + account.getSessionId());
                    }

                    setApiType(API_TYPE_LOGIN);
                    setApi(context.getString(R.string.api_login, account.getUsername(), account.getPassword()));
                    loadUrl(account.getUrl().toString());
                }
            }
        }.execute();

    }

    public void loginSuccess() {
        wai.hide();

        boolean sId = account.getSessionId().length() > 0;
        account.setSessionId(getSessionId());

        System.out.println("LOGIN COMPLETE, SESSION ID " + account.getSessionId());
        setApiType(API_TYPE_UPDATE);
        setApi(context.getString(R.string.api_view, MainActivity.MENU_INBOX));
//        loadUrl("javascript:MobileUpdateView("+MainActivity.MENU_INBOX+");");
        if (sId) {
//            System.out.println("REGULAR CLICK");
            loadUrl(account.getUrl().toString());
        } else {
//            System.out.println("CLICK TO APP BUTTON");
            loadUrl("javascript:if(!document.getElementById('dbAppPanel'))document.getElementById('dbAppButton').children[0].onclick();else window.location.pathname = '/odyssey/index.ody';");
        }
    }

    public void switchToApps() {
        wai.hide();
        System.out.println("SWITCH TO APPS");
        setApi(context.getString(R.string.api_view, MainActivity.MENU_APPS));
        loadRequestedMode(true);
    }

    private void loadRequestedMode(boolean cancelModalWindow){
        if(wai.isMainScreen()) {
            if(cancelModalWindow) {
                loadUrl("javascript:cancelModalWindow();" + getApi());
            }else{
                loadUrl("javascript:" + getApi());
            }
        }else{
            loadUrl("javascript:window.name=\"\";");
            loadUrl(account.getUrl().toString());
        }
    }

    public void switchToMessages() {
        wai.hide();
        System.out.println("SWITCH TO MESSAGES");
        setApi(context.getString(R.string.api_view, MainActivity.MENU_MESSAGES));
        loadRequestedMode(true);
    }

    public void switchToInbox() {
        wai.hide();
        System.out.println("SWITCH TO INBOX");
        setApi(context.getString(R.string.api_view, MainActivity.MENU_INBOX));
        loadRequestedMode(true);
    }

    public void switchToUserProfile() {
        wai.hide();
        System.out.println("USER PROFILE");
        setApi(context.getString(R.string.api_view, MainActivity.MENU_PROFILE));
        loadRequestedMode(false);
    }

    public void switchToInfo() {
        wai.hide();
        System.out.println("ACCOUNT INFO");
        setApi(context.getString(R.string.api_view, MainActivity.MENU_INFO));
        loadRequestedMode(false);
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    private String getApi() {
        return api;
    }

    private void setApi(String api) {
        this.api = api;
    }

    private String getApiType() {
        return apiType;
    }

    private void setApiType(String apiType) {
        this.apiType = apiType;
    }

    /*@Override
    public WebBackForwardList saveState(Bundle outState) {
        return super.saveState(outState);
    }*/

    public void resetRequestsCounter() {
        wai.resetRequestsCounter();
    }
}

