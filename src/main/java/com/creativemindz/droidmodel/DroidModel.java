package com.creativemindz.droidmodel;

import android.util.Log;

import com.creativemindz.droidmodel.DroidUtils;
import com.creativemindz.droidmodel.database.DatabaseHandler;
import com.creativemindz.droidmodel.database.DatabaseRequest;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by praveen on 28/02/16.
 */
public class DroidModel {

    public final static String TAG = "DroidModel";

    public final static String EQUALS = " = ";

    public final static String ROWID = "rowid";

    private String remoteUrl;

    private String modelName;

    private String primaryKey;

    private Integer rowid = -1;

    @Key(type = "remoteId")
    public String remoteKey;
    //private HashMap<String, String> modelValues;

    public String getPrimaryKeyValue() {
        Field[] fields = this.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            String key = fields[i].getName();
            Annotation[] a = fields[i].getDeclaredAnnotations();
            Key p = (Key)a[0];
            if(p.type().equals("PrimaryKey")){
                try {
                    return fields[i].get(this).toString();
                }catch (IllegalAccessException iae) {
                    iae.printStackTrace();
                }
                return "";
            }
        }
        return "";
    }

    public void setModelValues(HashMap<String,String> modelValues) throws IllegalAccessException{
        Field[] fields = this.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            String key = fields[i].getName();
            fields[i].set(this,modelValues.get(key));
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Key{
        String type();
    };

    /**
     * Gets the name of model/table
     * @return modelname
     */
    public String getModelName(){
        return this.modelName;
    }

    /**
     * Sets the name of model/table
     * @param name
     */
    private void setModelName(String name){
        this.modelName = name;
    }

    /**
     * Get primary key from quick access
     * @return
     */
    public Object getPrimaryKey(){
        return primaryKey;
    }

    /**
     * Iterate over all keys and set primary key
     *
     * Note: This method is expensive
     */
    private void setPrimaryKey(){
        Field[] fields = this.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            String key = fields[i].getName();
            Annotation[] a = fields[i].getDeclaredAnnotations();
            if(a != null) {
                if( a.length >0 ) {
                    Key p = (Key) a[0];
                    if (p.type().equals("PrimaryKey")) {
                        this.primaryKey = key;
                        return;
                    }
                }
            }
        }
    }

    private static String sanitizeCanonicalName(String canonicalName){
        String[] split = canonicalName.split("\\.");
        if(split.length>0){
            return split[split.length-1];
        }
        return canonicalName;
    }

    /**
     * Initialize database tables
     * @param definedClass
     */
    public DroidModel(Class<?> definedClass){

        setModelName(sanitizeCanonicalName(definedClass.getName()));
        setPrimaryKey();
        DatabaseRequest createRequest = new DatabaseRequest();
        createRequest.setActionType(DatabaseRequest.Action.CREATE_TABLE);
        createRequest.setTableName(this.getModelName());
        createRequest.setModel(this);

        ArrayList<HashMap<String,String>> response = DatabaseHandler.getInstance().processRequest(createRequest.getRW(), createRequest.getRawQuery());
        // Note: Nothing to do with response here? Saving in static value? Handle model update?

        // TODO: Handle model update scenario
    }

    /**
     * Iterate over all variables and get values
     * @return
     * @throws IllegalAccessException
     */
    public HashMap<String,String> getValues(){
        HashMap<String,String> columns = new HashMap<>();
        Field[] fields = this.getClass().getDeclaredFields();
        try{
            for (int i = 0; i < fields.length; i++) {
                String key = fields[i].getName();
                String value = fields[i].get(this).toString();
                columns.put(key,value);
            }
        }catch (IllegalAccessException iae){
            iae.printStackTrace();
        }
        return columns;
    }

    /**
     * Returns all the models present in database
     *
     * @return all models
     * @throws DroidModelException
     */
    public static ArrayList<Object> findAll(Class<?> definedClass) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        DatabaseRequest dbRequest = new DatabaseRequest();
        String modelName = sanitizeCanonicalName(definedClass.getName());
        dbRequest.setTableName(modelName);
        dbRequest.setActionType(DatabaseRequest.Action.SELECT_ALL);
        ArrayList<HashMap<String,String>> response
                = DatabaseHandler.getInstance().
                    processRequest(dbRequest.getRW(), dbRequest.getRawQuery());

        ArrayList<Object> returning = new ArrayList<>();

        Iterator<HashMap<String,String>> i = response.iterator();
        while (i.hasNext()){
            HashMap<String,String> values = i.next();
            Object dm = definedClass.getConstructor().newInstance();
            Method m = definedClass.getMethod("setModelValues", HashMap.class);
            m.invoke(dm,values);
            returning.add(dm);
        }
        return returning;
    }

    /**
     * Returns all models based on given model
     * @param model
     * @return
     * @throws DroidModelException
     */
    private static ArrayList<Object> find(DroidModel model) throws DroidModelException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        DatabaseRequest dbRequest = new DatabaseRequest();
        String modelName = sanitizeCanonicalName(model.getClass().getName());
        dbRequest.setTableName(modelName);
        dbRequest.setActionType(DatabaseRequest.Action.SELECT);

        StringBuffer conditionBuffer = new StringBuffer();

        HashMap<String,String> values = model.getValues();
        Iterator<String> keys = values.keySet().iterator();
        boolean first = true;
        while (keys.hasNext()){
            String key = keys.next();
            String value = values.get(key);
            if(DroidUtils.isStringValid(value) && DroidUtils.isStringValid(key)){
                String condition = key + EQUALS + DroidUtils.enclose(value);
                if(first){
                    first = false;
                }else {
                    conditionBuffer.append(" AND ");
                }
                //TODO: Validate condition before appending
                conditionBuffer.append(condition);
            }
        }

        dbRequest.setConditions(conditionBuffer.toString());
        ArrayList<HashMap<String,String>> response
                = DatabaseHandler.getInstance().
                processRequest(dbRequest.getRW(), dbRequest.getRawQuery());

        ArrayList<Object> returning = new ArrayList<>();

        Iterator<HashMap<String,String>> i = response.iterator();
        while (i.hasNext()){
            HashMap<String,String> rvalues = i.next();
            Object dm = model.getClass().getConstructor().newInstance();
            Method m = model.getClass().getMethod("setModelValues", HashMap.class);
            m.invoke(dm,rvalues);
            returning.add(dm);
        }
        return returning;
    }

    /**
     * Return one model based on given model
     * @param model
     * @return
     * @throws DroidModelException
     */
    private static DroidModel findOne(DroidModel model) throws DroidModelException{
        //TODO
        return null;
    }

    /**
     * Save the model to persistent data store
     * @throws DroidModelException
     */
    public void save() throws DroidModelException,IllegalAccessException{
        if(!DroidUtils.isStringValid(getModelName())
                || !DroidUtils.isStringValid(getPrimaryKey().toString()) ){
            Log.e(TAG,"Model is not initialized properly, not saving");
            return;
        }

        String primaryKeyvalue = getPrimaryKeyValue();
        if( this.rowid == -1 && !DroidUtils.isStringValid(primaryKeyvalue)){
            Log.e(TAG,"No primary key is set, not saving");
            return;
        }

        DatabaseRequest dbRequest = new DatabaseRequest();
        dbRequest.setTableName(getModelName());
        dbRequest.setActionType(DatabaseRequest.Action.INSERT_OR_UPDATE);
        dbRequest.setModel(this);
        if(DroidUtils.isStringValid(primaryKeyvalue)){
            dbRequest.setConditions(this.getPrimaryKey().toString() + EQUALS + DroidUtils.enclose(primaryKeyvalue));
        }else{
            dbRequest.setConditions(ROWID + EQUALS + DroidUtils.enclose(this.rowid.toString()));
        }
        ArrayList<HashMap<String,String>> dbResponse
                = DatabaseHandler.getInstance().processRequest(dbRequest.getRW(), dbRequest.getRawQuery());

        HashMap<String,String> modelValues = new HashMap<>();
        if(dbResponse.size() == 1){
            modelValues = dbResponse.get(0);
        }else{
            // TODO: Throw appropriate Error
            return;
        }
        //TODO : Validate DB response

        // For the scenario when save is called multiple times
        // assert if both of them are same or not
        if( modelValues == null || modelValues.size() == 0 ) {
            return;
        }
        if (this.rowid != -1 && modelValues.containsKey(ROWID)) {
            if (this.rowid != new Integer(modelValues.get(ROWID)))
                throw new DroidModelException();
        }
        if(modelValues.containsKey(ROWID)) {
            this.rowid = new Integer(modelValues.get(ROWID));
        }

    }

    /**
     * Retrieves data from persistant data
     *
     * @throws DroidModelException
     */
    public void load() throws DroidModelException {
        if (!DroidUtils.isStringValid(getModelName())
                || !DroidUtils.isStringValid(getPrimaryKey().toString())) {
            Log.e(TAG, "Model is not initialized properly, not saving");
            return;
        }

        String primaryKeyvalue = getPrimaryKeyValue();
        if (this.rowid == -1 && !DroidUtils.isStringValid(primaryKeyvalue)) {
            Log.e(TAG, "No primary key is set, not saving");
            return;
        }

        DatabaseRequest dbRequest = new DatabaseRequest();
        dbRequest.setTableName(getModelName());
        dbRequest.setActionType(DatabaseRequest.Action.SELECT_ALL);
        dbRequest.setModel(this);
        if (DroidUtils.isStringValid(primaryKeyvalue)) {
            dbRequest.setConditions(this.getPrimaryKey().toString() + EQUALS + DroidUtils.enclose(primaryKeyvalue));
        } else {
            dbRequest.setConditions(ROWID + EQUALS + DroidUtils.enclose(this.rowid.toString()));
        }

        ArrayList<HashMap<String, String>> dbResponse = DatabaseHandler.getInstance().processRequest(dbRequest.getRW(), dbRequest.getRawQuery());
        if(dbResponse.size() != 1){
            Log.e(TAG,"Something gone wrong, not loading");
            return;
        }
        HashMap<String,String> values = dbResponse.get(0);

        // For the scenario when save is called multiple times
        // assert if both of them are same or not
        if (this.rowid != -1) {
            if (this.rowid != new Integer(values.get(ROWID))) throw new DroidModelException();
        }
        try {
            setModelValues(values);
        }catch (IllegalAccessException iae){
            iae.printStackTrace();
            Log.e(TAG,"Unable to set values, something gone wrong");
        }
        if(values.containsKey(ROWID) && values.get(ROWID) != null){
            this.rowid = new Integer(values.get(ROWID));
        }
    }

    /**
     * Set remote URL for sync functionality
     * @param remoteUrl
     */
    public void setBaseUrl(String remoteUrl){
        this.remoteUrl = remoteUrl;
    }

    /**
     * Sync model with remote server
     * @throws DroidModelException
     */
    public void sync() throws DroidModelException{
        // TODO
    }

    /**
     * Sync all models with remote server
     * @throws DroidModelException
     */
    public static void syncAll() throws DroidModelException{
        // TODO
    }

}
