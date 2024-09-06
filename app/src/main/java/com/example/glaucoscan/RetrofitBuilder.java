package com.example.glaucoscan;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitBuilder {
    public static Retrofit retrofit = null;

    public static Retrofit getClient(){
//        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .readTimeout(60, TimeUnit.SECONDS)
//                .connectTimeout(60, TimeUnit.SECONDS)
//                .build();
        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://0gqqj9x945.execute-api.ap-south-1.amazonaws.com")
                    .addConverterFactory(GsonConverterFactory.create())
//                    .client(okHttpClient)
                    .build();
        }
        return retrofit;
    }
}
