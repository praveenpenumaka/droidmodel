package com.creativemindz.droidmodel.database;

import java.util.concurrent.PriorityBlockingQueue;

/**
 *
 * Request queue for both network and database
 * Created by praveen on 19/02/16.
 */
public class DatabaseRequestQueue {

    private static DatabaseRequestQueue _instance;

    private final PriorityBlockingQueue<DatabaseRequest> databaseQueue =
            new PriorityBlockingQueue<DatabaseRequest>();

    private static final int DEFAULT_DB_THREAD_POOL_SIZE = 4;

    private DatabaseDispatcher[] dDispatchers;

    private DatabaseHandler db;

    private DatabaseDelivery mDelivery;

    private DatabaseRequestQueue(int threadPoolSize, DatabaseHandler db) {
        dDispatchers = new DatabaseDispatcher[threadPoolSize];
        this.db = db;
        //this.mDelivery = delivery;
    }

    public static DatabaseRequestQueue getInstance(){
        if( _instance == null ){
            _instance = new DatabaseRequestQueue(DEFAULT_DB_THREAD_POOL_SIZE,DatabaseHandler.getInstance());
        }
        return _instance;
    }

    public DatabaseRequestQueue(){
        dDispatchers = new DatabaseDispatcher[DEFAULT_DB_THREAD_POOL_SIZE];
    }

    public void start() {
        stop();  // Make sure any currently running dispatchers are stopped.

        // Create database dispatchers (and corresponding threads) up to the pool size.
        for (int i = 0; i < dDispatchers.length; i++) {
            DatabaseDispatcher databaseDispatcher = new DatabaseDispatcher(databaseQueue,db, mDelivery);
            dDispatchers[i] = databaseDispatcher;
            databaseDispatcher.start();
        }
    }

    public void stop(){
        for (int i = 0; i < dDispatchers.length; i++) {
            if (dDispatchers[i] != null) {
                dDispatchers[i].quit();
            }
        }
    }

    public DatabaseRequest addRequest(DatabaseRequest request){

        databaseQueue.add(request);

        return request;

    }

}
