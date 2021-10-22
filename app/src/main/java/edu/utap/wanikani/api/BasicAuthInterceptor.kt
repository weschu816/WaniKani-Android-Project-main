package edu.utap.wanikani.api
import okhttp3.Credentials
import okhttp3.Interceptor

class BasicAuthInterceptor (): Interceptor {
    //private var credentials: String = Credentials.basic()

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var request = chain.request()
        request = request.newBuilder()//.header("Authorization: Bearer ffef2121-13e6-409a-bd8d-78437dc4338").build()
            .addHeader("Authorization", "Bearer ffef2121-13e6-409a-bd8d-78437dc4338")
            .build();
        return chain.proceed(request)
    }
}