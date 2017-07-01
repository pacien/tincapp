package org.pacien.tincapp.context

import android.app.Application
import android.content.Context

/**
 * @author pacien
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

    companion object {
        private var appContext: Context? = null
        fun getContext() = appContext!!
        fun getResources() = getContext().resources!!
    }

}
