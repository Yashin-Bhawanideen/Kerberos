package com.example.kerberos.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Emulator talking to a locally-run API: use http://10.0.2.2:5000/ http://192.168.10.110:5000/
    // Real device / deployed API: use your actual https:// domain
    private const val BASE_URL = "https://kerberos-gqcae4hrdgatfta5.southafricanorth-01.azurewebsites.net"

    // Logs full request/response bodies; useful in dev, but leaking
    // credential payloads (including passwords) to Logcat is risky in
    // a build that could ship to users
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Attaches auth (e.g. bearer token) to every request, plus logging
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
//References
//Geek4geeks, 2025. How to Post Data to API using Retrofit in Android?. [Online]
//Available at: https://www.geeksforgeeks.org/android/how-to-post-data-to-api-using-retrofit-in-android/
//man, S., 2014. Using Retrofit in Android. [Online]
//Available at: https://stackoverflow.com/questions/26500036/using-retrofit-in-android