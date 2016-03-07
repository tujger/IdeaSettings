package com.pantheon_inc.odyssey.android.helpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;

public class Utils {

    public static final int FINGERPRINT_HARDWARE_NOT_DETECTED = 1;
    public static final int FINGERPRINT_HARDWARE_DETECTED = 2;
    public static final int FINGERPRINT_HAS_ENROLLED_FINGERPRINTS = 4;
    public static final int FINGERPRINT_NOT_ALLOWED = 8;



    private static int fingerprintMode;

    public static String getUrl(String url) throws MalformedURLException, IOException {
        return Utils.getUrl(url, "UTF-8");
    }

    public static String getUrl(String url, String urlCharset) throws MalformedURLException, IOException {
        String line = null;
        StringBuilder sb = new StringBuilder();
        InputStream in = null;
        URLConnection feedUrl = null;
        feedUrl = new URL(url).openConnection();
        feedUrl.setConnectTimeout(5000);
        feedUrl.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows; U; Windows NT 5.1; ru; rv:1.8.1.12) Gecko/20080201 Firefox");

        in = feedUrl.getInputStream();
        BufferedReader reader;
        reader = new BufferedReader(new InputStreamReader(in, urlCharset));
        while ((line = reader.readLine()) != null) {
            sb.append(new String(line.getBytes("UTF-8")) + "\n");
        }
        in.close();

        return sb.toString();
    }

    /**
     * Reads the text of an asset. Should not be run on the UI thread.
     *
     * @param path The path to the asset.
     * @return The plain text of the asset
     */
    public static String readAsset(AssetManager mgr, String path) {
        String contents = "";
        InputStream is = null;
        BufferedReader reader = null;
        try {
            is = mgr.open(path);
            reader = new BufferedReader(new InputStreamReader(is));
            contents = reader.readLine();
            String line = null;
            while ((line = reader.readLine()) != null) {
                contents += '\n' + line;
            }
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
        return contents;
    }


    public static String getEncryptedHash(String str) {
        return getEncryptedHash(str, 5);
    }

    public static String getEncryptedHash(String str, int type) {

        String sType;
        switch (type) {
            case 1:
                sType = "SHA-1";
                break;
            case 2:
                sType = "MD2";
                break;
            case 5:
                sType = "MD5";
                break;
            case 256:
                sType = "SHA-256";
                break;
            case 384:
                sType = "SHA-256";
                break;
            case 512:
                sType = "SHA-512";
                break;
            default:
                sType = "SHA-512";
        }

//        System.out.println("GET HASH (" + sType + ") FOR: " + str);

        try {
            MessageDigest messageDigest = MessageDigest.getInstance(sType);
            messageDigest.update(str.getBytes("UTF-8"));
            byte[] bytes = messageDigest.digest();
            StringBuilder buffer = new StringBuilder();
            for (byte b : bytes) {
                buffer.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            System.out.println(buffer.toString());
            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getIdResourceByName(Context context, String aString)
    {
        String packageName = context.getPackageName();
        int resId = context.getResources().getIdentifier(aString, "id", packageName);
        return resId;
    }


    public static int getFingerprintMode() {
        return fingerprintMode;
    }

    public static void setFingerprintMode(int fingerprintMode) {
        Utils.fingerprintMode = fingerprintMode;
    }

    public static void checkFingerprintHardware(Context context){
        System.out.println("A");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        System.out.println("B");
            FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
        System.out.println("C");
                setFingerprintMode(FINGERPRINT_NOT_ALLOWED);
            }else {
                if (!fingerprintManager.isHardwareDetected()) {// Device doesn't support fingerprint authentication
        System.out.println("D");
                    setFingerprintMode(FINGERPRINT_NOT_ALLOWED | FINGERPRINT_HARDWARE_NOT_DETECTED);
                } else if (!fingerprintManager.hasEnrolledFingerprints()) {// User hasn't enrolled any fingerprints to authenticate with
        System.out.println("E");
                    setFingerprintMode(FINGERPRINT_HARDWARE_DETECTED);
                } else {// Everything is ready for fingerprint authentication
        System.out.println("F");
                    setFingerprintMode(FINGERPRINT_HARDWARE_DETECTED | FINGERPRINT_HAS_ENROLLED_FINGERPRINTS);
                }
            }
        }else{
        System.out.println("g");
            setFingerprintMode(FINGERPRINT_NOT_ALLOWED | FINGERPRINT_HARDWARE_NOT_DETECTED);
        }
    }

    public static void sendHandlerMessage(Handler uiHandler, int flags, String message){
        Message m = uiHandler.obtainMessage();
        Bundle uB = m.getData();
        uB.putInt(WebAppInterface.ACTION, flags);
        if(message!= null && !"".equals(message)) {
            uB.putString(WebAppInterface.ACTION_COMMENT, message);
        }
        m.setData(uB);
        uiHandler.sendMessage(m);
    }

}
