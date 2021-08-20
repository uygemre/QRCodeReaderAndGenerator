package com.uygemre.qrcode.activities

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
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var localPrefManager: LocalPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        localPrefManager = LocalPrefManager(this)
        setupBottomBarView()
    }

    private fun setLocales() {
        val locale: Locale = when (localPrefManager.pull(PrefConstants.PREF_LANGUAGE, "")) {
            "English" -> {
                Locale.ENGLISH
            }
            "Turkish" -> {
                Locale("tr", "TR")
            }
            "German" -> {
                Locale.GERMAN
            }
            "Italian" -> {
                Locale.ITALIAN
            }
            "Spanish" -> {
                Locale("ES", "es")
            }
            "Chinese (Simplified)" -> {
                Locale.SIMPLIFIED_CHINESE
            }
            "Hindi" -> {
                Locale("hi", "IN")
            }
            "" -> {
                Locale.getDefault()
            }
            else -> Locale.getDefault()
        }

        Locale.setDefault(locale)
        val configuration = resources.configuration
        configuration.locale = locale
        configuration.setLayoutDirection(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
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

    override fun onResume() {
        super.onResume()
        setLocales()
        bottomNavigationView.menu.clear()
        bottomNavigationView.inflateMenu(R.menu.bottom_main_menu)
    }

    override fun onStop() {
        super.onStop()
        localPrefManager.push(PrefConstants.PREF_SCAN_AD, true)
    }
}