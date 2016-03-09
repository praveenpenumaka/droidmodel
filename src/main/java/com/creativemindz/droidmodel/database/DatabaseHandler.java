package com.creativemindz.droidmodel.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.creativemindz.Utils.DroidUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by praveen on 17/02/16.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHandler";
    private final PriorityBlockingQueue<DatabaseRequest> mDatabaseQueue =
            new PriorityBlockingQueue<DatabaseRequest>();

    private static DatabaseHandler _instance;

    private static Integer DB_VERSION = 1;

    private static String DB_NAME = "testDBName";
    
    private DatabaseHandler(Context context, String dbName, Integer dbVersion) {
        super(context, dbName, null, dbVersion);
    }

    public static DatabaseHandler getInstance(Context context){
        if(context == null){
            return null;
        }
        if(_instance == null){
            _instance = new DatabaseHandler(context, DB_NAME, DB_VERSION);
        }
        return _instance;
    }

    public static DatabaseHandler getInstance(){
        return _instance;
    }

    public static DatabaseHandler getInstance(Context context,String dbName, Integer dbVersion){
        if(context == null){
            return null;
        }
        if(_instance == null){
            _instance = new DatabaseHandler(context, dbName, dbVersion);
        }
        return _instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    private void createTable(String tableName,String query){

        SQLiteDatabase db = this.getWritableDatabase();

        if( !DroidUtils.isStringValid(tableName) ){
            return;
        }

        db.execSQL(query);

    }

    private void dropTable(String query){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
    }

    private void select(DatabaseRequest request){
        SQLiteDatabase db = this.getReadableDatabase();

        db.execSQL(request.getRawQuery());
        db.beginTransaction();
    }

    /**
     * TODO
     * @param request
     */
    private void addColumn(DatabaseRequest request){

    }

    private void delete(String table, String conditions){
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(table, conditions, null);
    }

    private void insert(String table, String request){
        SQLiteDatabase db = this.getWritableDatabase();

        StringBuffer sb = new StringBuffer("INSERT INTO "+table + " (");
/*        HashMap<String,String> columns = request.getModel().getColumns();
        Iterator<String> i = columns.keySet().iterator();
        StringBuffer values = new StringBuffer("(");
        Map<String,?> valuesObj = request.getValues();

        while (i.hasNext()){
            String columnName = i.next();
            sb.append(columnName);
            values.append(valuesObj.get(columnName).toString());
            if(i.hasNext()){
                sb.append(",");
                values.append(",");
            }
        }
        values.append(")");
        sb.append(") VALUES (");
        sb.append(values);
        sb.toString();*/

        db.execSQL(sb.toString());
    }

    private void update(String query){
        SQLiteDatabase db = this.getWritableDatabase();


        db.execSQL(query);
    }

    private void insertOrUpdate(DatabaseRequest request){
        SQLiteDatabase db = this.getWritableDatabase();
        //String id = request.getModel().getId();
        //if("None".equals(id)){
        //    insert(request);
        //}else{
        //    request.setConditions(" id = "+id );
        //    update(request);
        //}

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * Get type of column based on canonical name
     * @param canonicalName
     * @return
     */
    public static String getType(String canonicalName) {
        canonicalName = canonicalName.toLowerCase();
        if("string".indexOf(canonicalName)>=0){
            return "TEXT";
        }else if("integer".indexOf(canonicalName)>=0 || "int".indexOf(canonicalName)>=0 || "float".indexOf(canonicalName)>=0){
            return "NUMBER";
        }
        return "TEXT";
    }

    private SQLiteDatabase getDatabase(String RW){
        if("WRITABLE".equals(RW)){
            return this.getWritableDatabase();
        }
        return this.getReadableDatabase();
    }

    public ArrayList<HashMap<String,String>> processRequest(String RW,String rawQuery) {

        SQLiteDatabase db = getDatabase(RW);
        Cursor cursor = null;
        Log.d(TAG, "Processing rawQuery");
        if(!db.isOpen()) {
            return null;
        }
        cursor = db.rawQuery(rawQuery, null);
        if(cursor == null){
            return null;
        }
        cursor.moveToFirst();
        ArrayList<HashMap<String,String>> results = new ArrayList<>();
        if(cursor.getCount()==0){
            return results;
        }
        String[] columns = cursor.getColumnNames();
        do{
            HashMap<String,String> row = new HashMap<>();
            if(cursor.getColumnCount() == columns.length) {
                for (int i = 0; i < columns.length; i++) {
                    String value = cursor.getString(cursor.getColumnIndex(columns[i]));
                    row.put(columns[i], value);
                }
                results.add(row);
            }
        }while(cursor.moveToNext());
        return results;
    }


}
