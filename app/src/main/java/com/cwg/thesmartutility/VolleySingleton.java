package com.cwg.thesmartutility;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleySingleton {
    private RequestQueue requestQueue;
    public static  VolleySingleton mInstance;

    private VolleySingleton(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }
    static synchronized VolleySingleton getmInstance(Context context) {
        //check if the instance is null
        if (mInstance == null) {
            mInstance = new VolleySingleton(context);
        }
        return mInstance;
    }

    //another method
    public RequestQueue getRequestQueue () {
        return requestQueue;
    }
}
