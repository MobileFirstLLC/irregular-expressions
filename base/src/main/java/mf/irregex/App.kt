package mf.irregex

import android.app.Application
import mf.irregex.styles.AvailableStyles

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        /* Initialize text styles */
        AvailableStyles.init(applicationContext)
    }
}
