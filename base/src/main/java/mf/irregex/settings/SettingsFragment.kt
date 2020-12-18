package mf.irregex.settings

import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceFragmentCompat
import mf.irregex.R


class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}