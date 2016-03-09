package com.creativemindz.droidmodel.database;

import android.util.Log;

import com.creativemindz.Utils.DroidUtils;
import com.creativemindz.droidmodels.DroidModel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by praveen on 29/02/16.
 */
public class DatabaseRequest implements Comparable<DatabaseRequest>{


    private static final String TAG = "DatabaseRequest";
    private String RW;

    @Override
    public int compareTo(DatabaseRequest another) {
        return this.getPriority().compareTo(another.getPriority());
    }

    public String getRW() {
        return RW;
    }

    public DatabaseResponse parseResults(HashMap<String, String> results) {
        // TODO
        return null;
    }

    public void setResponse(){

    }

    public interface Action {
        int CREATE_TABLE = 0;
        int CLEAR_TABLE = 1;
        int ADD_COLUMN = 2;
        int REMOVE_COLUMN = 3;
        int INSERT = 4;
        int SELECT = 5;
        int UPDATE = 6;
        int DELETE = 7;
        int BULK_INSERT = 8;
        int BULK_UPDATE = 9;
        int BULK_DELETE = 10;
        int DROP_TABLE = 11;
        int INSERT_OR_UPDATE = 12;
        int CREATE_TABLE_IF_NOTEXISTS = 13;
        int SELECT_ALL = 14;
    }

    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        IMMEDIATE
    }

    private int dAction;

    private String tableName;

    private DroidModel model;

    private Map<String,?> values;

    private String conditions;

    private Priority dPriority;

    public void setPriority(Priority priority) {
        this.dPriority =  priority;
    }

    public Priority getPriority() {
        return this.dPriority;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName){
        this.tableName = tableName;
    }

    public void setModel(DroidModel model) {

        this.model = model;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public void setActionType(int action){
        this.dAction = action;
        switch (action){
            case Action.CREATE_TABLE: RW = "Writable";break;
            case Action.CLEAR_TABLE: RW = "Writable";break;
            case Action.ADD_COLUMN: RW = "Writable";break;
            case Action.REMOVE_COLUMN: RW = "Writable";break;
            case Action.INSERT: RW = "Writable";break;
            case Action.SELECT: RW = "";break;
            case Action.UPDATE: RW = "Writable";break;
            case Action.DELETE: RW = "Writable";break;
            case Action.BULK_INSERT: RW = "Writable";break;
            case Action.BULK_UPDATE: RW = "Writable";break;
            case Action.BULK_DELETE: RW = "Writable";break;
            case Action.DROP_TABLE: RW = "Writable";break;
            case Action.INSERT_OR_UPDATE: RW = "Writable";break;
            case Action.CREATE_TABLE_IF_NOTEXISTS: RW = "Writable";break;
            case Action.SELECT_ALL: RW = "";break;
            default: this.dAction = -1;RW="";
        }
        return;
    }

    private String getSelectAllQuery(){
        StringBuffer sb = new StringBuffer("SELECT * FROM ");
        sb.append(this.tableName);
        return sb.toString();
    }

    private String getSelectQuery(){
        StringBuffer sb = new StringBuffer(" SELECT ");
        if(this.values.size() > 0 ){
            Iterator<String> i = this.values.keySet().iterator();

            while (i.hasNext()){
                String column = i.next();
                sb.append(column);
                if(i.hasNext()){
                    sb.append(",");
                }else{
                    sb.append(" ");
                }
            }
        }else{
            sb.append(" * ");
        }
        sb.append(" FROM " +this.tableName);
        if(DroidUtils.isStringValid(conditions)){
            sb.append(" WHERE ");
            sb.append(conditions);
        }
        return sb.toString();
    }

    private String getInsertUpdateQuery(){
        if(!DroidUtils.isStringValid(this.tableName)){
            Log.e(TAG,"Bad database request");
            return "";
        }
        if( this.model == null ){
            Log.e(TAG,"Bad database Request");
            return "";
        }
        StringBuffer sb = new StringBuffer(" INSERT OR REPLACE INTO ");
        StringBuffer valuesString = new StringBuffer();
        sb.append(" "+this.tableName+" ");


        sb.append("(");
        valuesString.append("(");
        Field[] fields = this.model.getClass().getFields();
        boolean first = true;
        for (int i=0;i<fields.length;i++){
            Annotation[] a = fields[i].getDeclaredAnnotations();
            if(a != null && a.length > 0){
                String fieldName = fields[i].getName();
                try {
                    Object fieldValue = fields[i].get(this.model);
                    if(fieldValue == null){
                        Log.w(TAG,"Field value "+fieldName +" is null");
                    }else {
                        if(first){
                            first = false;
                        }else {
                            sb.append(",");
                            valuesString.append(",");
                        }
                        String value = fieldValue.toString();
                        sb.append(fieldName);
                        valuesString.append(DroidUtils.enclose(value));
                    }
                }catch(IllegalAccessException iae){
                    iae.printStackTrace();
                    Log.e(TAG,"Something gone wrong, not able to generate query");
                    return "";
                }

            }
        }
        sb.append(")");
        valuesString.append(")");

        sb.append(" VALUES ");
        sb.append(valuesString);

        /*if(DroidUtils.isStringValid(conditions)){
            sb.append(" WHERE ");
            sb.append(conditions);
        }*/
        return sb.toString();
    }

    public String getRawQuery() {
        switch (this.dAction){
            case Action.CREATE_TABLE:
                return getCreateQuery();
            case Action.SELECT : return getSelectQuery();
            case Action.DROP_TABLE: return getDropTableQuery();
            case Action.INSERT_OR_UPDATE: return getInsertUpdateQuery();
            case Action.DELETE: break;//TODO
            case Action.UPDATE: break;//TODO
            case Action.INSERT: break;//TODO
            case Action.ADD_COLUMN: break;//TODO
            case Action.REMOVE_COLUMN:break;//TODO
            case Action.BULK_INSERT: break;//TODO
            case Action.BULK_UPDATE: break;//TODO
            case Action.BULK_DELETE: break;//TODO
            case Action.CLEAR_TABLE:break;//TODO
            case Action.SELECT_ALL: return getSelectAllQuery();
        }
        return getSelectQuery();
    }

    private String getUpdateQuery(){
        /*StringBuffer sb = new StringBuffer("UPDATE "+ this.tableName + " (");
        HashMap<String,String> columns = request.getModel().getColumns();
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
        sb.append(" WHERE "+ conditions);*/
        return "";
    }

    private String getDropTableQuery(){
        return "DROP TABLE IF EXISTS " + this.tableName;
    }
    private String getCreateQuery(){
        if(this.tableName == null || "".equals(tableName)){
            return "";
        }
        if( model == null ){
            return "";
        }

        /*
           CREATE TABLE "usertest" (
        `rowid`	INT UNIQUE,
        `name`	TEXT,
        `email`	TEXT,
                PRIMARY KEY(rowid)
        )*/
        StringBuffer query = new StringBuffer("CREATE TABLE IF NOT EXISTS " + this.model.getModelName() + "(");
        Field[] fields = this.model.getClass().getFields();
        query.append("`rowid` INT UNIQUE,");

        for (int i=0;i<fields.length;i++){
            Annotation[] a = fields[i].getDeclaredAnnotations();
            if(a != null && a.length > 0){
                String fieldName = fields[i].getName();
                DroidModel.Key k = (DroidModel.Key)a[0];
                String type = k.type().toString();
                query.append("`" + fieldName + "` " + type + ",");
            }
        }
        query.append("PRIMARY KEY("+this.model.getPrimaryKey()+")");
        query.append(")");
        return query.toString();
    }

    private String getTableExistsQuery(){
        return "SELECT name FROM sqlite_master WHERE type='table' AND name='" + this.tableName +"'";
    }
}
