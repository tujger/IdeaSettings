package com.pantheon_inc.odyssey.android;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

/**
 * Created by eduardm on 024, 2/24/2016.
 */
public class BackupAgent extends BackupAgentHelper {
    static String fileName;
    static final String PREFS_BACKUP_KEY = "prefs";
    static final String PREFS_BACKUP_KEY_ACCOUNTS = "account_";

    @Override
    public void onCreate() {
if(true)return;

        System.out.println("BACKUP DATA STARTS");
//TODO

        fileName = this.getPackageName() + "_preferences";

        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this,
                fileName);
        addHelper(PREFS_BACKUP_KEY, helper);

     /*   fileName = "preferences";

        helper = new SharedPreferencesBackupHelper(this,
                fileName);
        addHelper(PREFS_BACKUP_KEY, helper);
*/
/*
        *//* backup accounts */
      /*  for (int i = 1; i <= Account.getLastId(); i++) {
            fileName = "account_" + i;
            SharedPreferences sp = getApplicationContext().getSharedPreferences(fileName, Context.MODE_PRIVATE);

            if (sp.contains(Account.ACCOUNT_URL)) {
                helper = new SharedPreferencesBackupHelper(this,fileName);
                addHelper(PREFS_BACKUP_KEY_ACCOUNTS, helper);
            }
        }*/
    }

}