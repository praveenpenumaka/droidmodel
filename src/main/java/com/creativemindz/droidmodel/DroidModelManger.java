package com.creativemindz.droidmodel;

import android.content.Context;

import com.creativemindz.database.DatabaseHandler;
import com.creativemindz.database.DatabaseRequestQueue;

/**
 * Created by praveen on 04/03/16.
 */
public class DroidModelManger {

    protected static String databaseName;
    protected static Integer databaseVersion;

    public static void initAll(Context context){
        DatabaseHandler db = DatabaseHandler.getInstance(context,databaseName,databaseVersion);
        DatabaseRequestQueue dbQueue = DatabaseRequestQueue.getInstance();
        dbQueue.start();
    }
}
