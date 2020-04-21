package mf.asciitext.app

import android.app.Application
import mf.asciitext.fonts.AvailableFonts
import mf.asciitext.gallery.AvailableArt

class App : Application() {


    override fun onCreate() {
        super.onCreate()

        /* Initialize application fonts & art */
        AvailableFonts.init(applicationContext.resources)
        // AvailableArt.init(applicationContext.resources)

    }
}
