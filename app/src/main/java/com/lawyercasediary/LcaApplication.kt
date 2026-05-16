package com.lawyercasediary

import android.app.Application
import com.lawyercasediary.di.AppContainer

/**
 * Enterprise Application class for Lawyer Case Diary.
 * Initializes the AppContainer for dependency resolution.
 */
class LcaApplication : Application() {
    
    // Instance of AppContainer to be used by the rest of the app
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
