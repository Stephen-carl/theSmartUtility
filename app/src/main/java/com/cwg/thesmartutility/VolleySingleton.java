package com.cwg.thesmartutility;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleySingleton {

    // Static variable to hold the singleton instance
    private static VolleySingleton instance;

    // Volley request queue
    private RequestQueue requestQueue;

    // Application context
    private static Context ctx;

    // Private constructor to prevent direct instantiation
    private VolleySingleton(Context context) {
        // Use the application context to avoid memory leaks
        ctx = context.getApplicationContext();
        requestQueue = getRequestQueue();
    }

    // Singleton pattern: get the single instance of VolleySingleton
    public static synchronized VolleySingleton getInstance(Context context) {
        if (instance == null) {
            instance = new VolleySingleton(context);
        }
        return instance;
    }

    // Get the request queue (create it if null)
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // Instantiate the request queue
            requestQueue = Volley.newRequestQueue(ctx);
        }
        return requestQueue;
    }

    // Method to add requests to the queue
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
//    private RequestQueue requestQueue;
//    public static  VolleySingleton mInstance;
//
//    private VolleySingleton(Context context) {
//        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
//    }
//    static synchronized VolleySingleton getmInstance(Context context) {
//        //check if the instance is null
//        if (mInstance == null) {
//            mInstance = new VolleySingleton(context);
//        }
//        return mInstance;
//    }
//
//    //another method
//    public RequestQueue getRequestQueue () {
//        return requestQueue;
//    }
}


