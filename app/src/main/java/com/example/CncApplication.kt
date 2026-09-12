package com.example

import android.app.Application
import android.util.Log
import kotlin.system.exitProcess

class CncApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupGlobalCrashHandler()
    }

    private fun setupGlobalCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // In a real industrial app, we'd log this to a persistent file or remote telemetry
            Log.e("CncApplication", "FATAL CRASH on thread ${thread.name}: ${throwable.message}", throwable)
            
            // Note: This is best-effort. The process is already unstable.
            // We'd ideally use a more robust persistent logging solution.

            defaultHandler?.uncaughtException(thread, throwable) ?: exitProcess(1)
        }
    }
}
