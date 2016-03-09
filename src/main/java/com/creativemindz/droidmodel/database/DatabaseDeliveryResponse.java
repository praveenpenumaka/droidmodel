package com.creativemindz.droidmodel.database;

import com.creativemindz.droidmodels.DroidModelException;

/**
 * Created by praveen on 05/03/16.
 */
public class DatabaseDeliveryResponse implements DatabaseDelivery{
    @Override
    public void postResponse(DatabaseRequest request, DatabaseResponse response) {

    }

    @Override
    public void postResponse(DatabaseRequest request, DatabaseResponse response, Runnable runnable) {

    }

    @Override
    public void postError(DatabaseRequest request, DroidModelException error) {

    }

    private class ResponseDeliveryRunnable implements Runnable {


        private final DatabaseRequest mRequest;

        private final DatabaseResponse mResponse;

        private final Runnable mRunnable;

        public ResponseDeliveryRunnable(DatabaseRequest request, DatabaseResponse response, Runnable runnable) {
            mRequest = request;
            mResponse = response;
            mRunnable = runnable;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {

        }
    }
}
