package com.lawyercasediary.api

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

/**
 * Enterprise CookieJar for maintaining session state across Vercel/Next.js endpoints.
 * Automatically handles the 'token' and 'refreshToken' cookies sent by the backend.
 */
class SessionCookieJar : CookieJar {
    private val cookieStore = ConcurrentHashMap<String, MutableList<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore[url.host] = cookies.toMutableList()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore[url.host] ?: emptyList()
    }
    
    fun clear() {
        cookieStore.clear()
    }
}
