package mf.irregex

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        window.insetsController?.hide(WindowInsets.Type.statusBars())
        val mainActivityClass = MainActivity::class.java
        val intent = Intent(this, mainActivityClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val options = ActivityOptionsCompat.makeCustomAnimation(
            this,
            R.anim.fade_in,
            R.anim.fade_out
        ).toBundle()
        startActivity(intent, options)
        finish()
    }
}
