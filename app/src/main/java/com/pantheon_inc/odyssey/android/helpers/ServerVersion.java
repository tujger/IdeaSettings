package com.pantheon_inc.odyssey.android.helpers;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by eduardm on 010, 3/10/2016.
 */
public class ServerVersion {
    private ArrayList<Integer> version = new ArrayList<>(Arrays.asList(new Integer[]{0, 0, 0, 0}));

    public ServerVersion(String version) {
        if (version == null) version = "";
        String[] x = version.split("\\.");
        for (int i = 0; i < x.length; i++) {
            this.version.set(i, Integer.parseInt("0" + x[i]));
        }
    }

    public int getMajor() {
        return version.get(0);
    }

    public int getMinor() {
        return version.get(1);
    }

    public int getBuild() {
        return version.get(2);
    }

    public int getRevision() {
        return version.get(3);
    }

    public String toString() {
        return String.format("%d.%d.%d.%d", getMajor(), getMinor(), getBuild(), getRevision());
    }

    public boolean isEarlierThanMinor(String version) {
        ServerVersion v = new ServerVersion(version);
        boolean res = true;

        if (getMajor() > v.getMajor()) {
            res = false;
        } else if (getMinor() > v.getMinor()) {
            res = false;
        }
        return res;
    }

    public boolean isEarlierThanBuild(String version) {
        ServerVersion v = new ServerVersion(version);
        boolean res = true;

        if (getMajor() > v.getMajor()) {
            res = false;
        } else if (getMinor() > v.getMinor()) {
            res = false;
        } else if (getBuild() > v.getBuild()) {
            res = false;
        }
        return res;
    }

    public boolean isEarlierThan(String version) {
        ServerVersion v = new ServerVersion(version);
        boolean res = true;

        if (getMajor() > v.getMajor()) {
            res = false;
        } else if (getMinor() > v.getMinor()) {
            res = false;
        } else if (getBuild() > v.getBuild()) {
            res = false;
        } else if (getRevision() > v.getRevision()) {
            res = false;
        }
        return res;
    }
}
