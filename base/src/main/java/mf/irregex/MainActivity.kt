package mf.irregex

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton


class MainActivity : AppCompatActivity() {

    private val enableStep = 1
    private val selectStep = 2

    private lateinit var step1: MaterialButton
    private lateinit var step2: MaterialButton
    private var activeStep = enableStep

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        clearStatusBar()

        step1 = findViewById(R.id.step_1)
        step2 = findViewById(R.id.step_2)

        step1.setOnClickListener(enableKeyboard(this))
        step2.setOnClickListener(selectKeyboard())

        determineActiveStep()
    }

    override fun onResume() {
        super.onResume()
        determineActiveStep()
    }

    @Suppress("DEPRECATION")
    private fun clearStatusBar(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    private fun determineActiveStep() {
        activeStep = if (!isCodeBoardEnabled())
            enableStep
        else
            selectStep

        step1.isEnabled = activeStep == enableStep
        step2.isEnabled = activeStep == selectStep
    }

    private fun isCodeBoardEnabled(): Boolean {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return imm.enabledInputMethodList.any { it.packageName == packageName }
    }

    private fun enableKeyboard(a: Activity): View.OnClickListener {
        return View.OnClickListener {
            val enableIntent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
            enableIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            a.startActivity(enableIntent)
        }
    }

    private fun selectKeyboard(): View.OnClickListener {
        return View.OnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }
    }
}