package com.uygemre.qrcode.activities

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.uygemre.qrcode.R
import com.uygemre.qrcode.constants.PrefConstants
import com.uygemre.qrcode.fragments.CreateQRFragment
import com.uygemre.qrcode.fragments.HistoryFragment
import com.uygemre.qrcode.fragments.ScanQRFragment
import com.uygemre.qrcode.fragments.SettingsFragment
import com.uygemre.qrcode.helpers.LocalPrefManager
import com.uygemre.qrcode.helpers.MyContextWrapper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var localPrefManager: LocalPrefManager
    private var languageCode = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupBottomBarView()
    }

    private fun setupLanguageCode() {
        languageCode = when (localPrefManager.pull(PrefConstants.PREF_LANGUAGE, "")) {
            "Turkish" -> "tr"
            "English" -> "en"
            "Spanish" -> "es"
            "Italian" -> "it"
            "Chinese (Simplified)" -> "zh"
            "Hindi" -> "hi"
            else -> "en"
        }
    }

    private fun setupBottomBarView() {
        var fragment: Fragment? = ScanQRFragment()
        fragment?.let {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_container, it)
                .commit()
        }

        val bottomNavBarClickListener = BottomNavigationView.OnNavigationItemSelectedListener { p0 ->
                when (p0.itemId) {
                    R.id.navigation_scan_qr -> {
                        fragment = ScanQRFragment()
                    }
                    R.id.navigation_create_qr -> {
                        fragment = CreateQRFragment()
                    }
                    R.id.navigation_settings -> {
                        fragment = SettingsFragment()
                    }
                    R.id.navigation_history -> {
                        fragment = HistoryFragment()
                    }
                }

                if (fragment != null) {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.main_container, fragment!!)
                        .commit()
                }

                true
            }

        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavBarClickListener)
    }

    override fun attachBaseContext(newBase: Context?) {
        localPrefManager = newBase?.let { LocalPrefManager(it) }!!
        setupLanguageCode()
        super.attachBaseContext(newBase.let { MyContextWrapper.wrap(it, languageCode) })
    }

    override fun onStop() {
        super.onStop()
        localPrefManager.push(PrefConstants.PREF_SCAN_AD, true)
    }
}