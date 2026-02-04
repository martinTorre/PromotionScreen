package com.dermy.pharma.promotionscreen.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android

object KtorClient {

    fun create(): HttpClient {
        return HttpClient(Android)
    }
}
