package com.example.base.network.net

import com.example.base.network.config.AppConfig
import com.example.base.network.config.DirConfig
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * create by libo
 * create on 2018/11/13
 * description 单例的Retrofit和Okhttp管理类
 */
object ApiManager {
    private var mOkHttpClient: OkHttpClient? = null
    lateinit var mApiService: ApiService
    private val TIMEOUT = 10

    private fun initOkhttp() {
        val builder = OkHttpClient.Builder()
                .connectTimeout(TIMEOUT.toLong(), TimeUnit.SECONDS) //连接超时设置
                .readTimeout(TIMEOUT.toLong(), TimeUnit.SECONDS) //写入缓存超时10s
                .writeTimeout(TIMEOUT.toLong(), TimeUnit.SECONDS) //读取缓存超时10s
                .retryOnConnectionFailure(true) //失败重连
                .addInterceptor(HeaderInterceptor()) //添加header
                .addInterceptor(NetCacheInterceptor()) //添加网络缓存
        addLogIntercepter(builder) //日志拦截器
        setCacheFile(builder) //网络缓存
        mOkHttpClient = builder.build()
    }

    private fun initRetrofit() {
        val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Api.Companion.SERVER_ADDRESS)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(mOkHttpClient)
                .build()
        mApiService = retrofit.create(ApiService::class.java)
    }

    /**
     * 设置缓存文件路径
     */
    private fun setCacheFile(builder: OkHttpClient.Builder) {
        //设置缓存文件
        val cacheFile = File(DirConfig.HTTP_CACHE)
        //缓存大小为10M
        val cacheSize = 10 * 1024 * 1024
        val cache = Cache(cacheFile, cacheSize.toLong())
        builder.cache(cache)
    }

    /**
     * 调试模式下加入日志拦截器
     * @param builder
     */
    private fun addLogIntercepter(builder: OkHttpClient.Builder) {
        if (AppConfig.isDebug) {
            builder.addInterceptor(LoggingInterceptor())
        }
    }

    init {
        initOkhttp()
        initRetrofit()
    }
}