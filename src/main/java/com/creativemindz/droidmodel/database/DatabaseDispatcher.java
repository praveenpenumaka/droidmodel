package com.creativemindz.droidmodel.database;

import android.os.Process;

import java.util.concurrent.BlockingQueue;

/**
 * Created by praveen on 19/02/16.
 */
public class DatabaseDispatcher extends Thread{

    private final BlockingQueue<DatabaseRequest> databaseRequestQueue;

    private DatabaseHandler databaseHandler;

    private final DatabaseDelivery mDelivery;

    private boolean mQuit = false;

    public DatabaseDispatcher(BlockingQueue<DatabaseRequest> queue, DatabaseHandler db, DatabaseDelivery delivery){
        this.databaseRequestQueue = queue;
        this.databaseHandler = db;
        this.mDelivery = delivery;
    }

    /**
     * Forces this dispatcher to quit immediately.  If any requests are still in
     * the queue, they are not guaranteed to be processed.
     */
    public void quit() {
        mQuit = true;
        interrupt();
    }

    @Override
    public void run(){
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while (true){
            DatabaseRequest request;
            try {
                // Take a request from the queue.
                request = databaseRequestQueue.take();
            } catch (InterruptedException e) {
                // We may have been interrupted because it was time to quit.
                if (mQuit) {
                    return;
                }
                continue;
            }


            //HashMap<String,String> results = databaseHandler.processRequest(request.getRW(),request.getRawQuery());
            //DatabaseResponse response = request.parseResults(results);
            //mDelivery.postResponse(request, response);
            // TODO: Convert into Database Response and invoke onResponse

        }
    }
}
