package vcmsa.projects.prog7314.data.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/*
    Code Attribution for: Using a Retrofit Client

    GeeksforGeeks, 2017. Introduction to Retrofit in Android (Version unknown) [Source code].
    Available at: <https://www.geeksforgeeks.org/android/introduction-retofit-2-android-set-1/>
    [Accessed 17 November 2025].
*/


// Singleton object to manage Retrofit network calls
object RetrofitClient {

    private const val TAG = "RetrofitClient"

    // Base URL for the API; our custom built API deployed on render
    private const val BASE_URL = "https://memory-match-maddness-api.onrender.com"

    // Logging interceptor for debugging network requests and responses
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d(TAG, message)
    }.apply {
        // Show full request and response body in logs
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Interceptor to add common headers to every request
    private val headerInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Content-Type", "application/json") // Ensure server receives JSON
            .addHeader("Accept", "application/json")       // Expect JSON responses
            .build()
        chain.proceed(request)
    }

    // OkHttp client configuration with interceptors and timeouts
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // Add logging for debugging
        .addInterceptor(headerInterceptor)  // Add headers to all requests
        .connectTimeout(30, TimeUnit.SECONDS) // Max time to establish a connection
        .readTimeout(30, TimeUnit.SECONDS)    // Max time to wait for response
        .writeTimeout(30, TimeUnit.SECONDS)   // Max time to send request body
        .retryOnConnectionFailure(true)       // Retry if connection fails
        .build()

    // Lazy-initialized Retrofit instance
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)                     // Set the API base URL
            .client(okHttpClient)                  // Use the configured OkHttp client
            .addConverterFactory(GsonConverterFactory.create()) // Convert JSON to Kotlin objects
            .build()
    }

    // Single instance of the API service
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java) // Create implementation of ApiService interface
    }

    /**
     * Create a Retrofit API service with a custom base URL.
     * Useful for testing or switching environments (e.g., dev, staging, production)
     */
    fun createApiService(baseUrl: String): ApiService {
        val customRetrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return customRetrofit.create(ApiService::class.java)
    }
}
