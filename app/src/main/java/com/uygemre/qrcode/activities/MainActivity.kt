package com.uygemre.qrcode.activities

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.uygemre.qrcode.R
import com.uygemre.qrcode.fragments.CreateQRFragment
import com.uygemre.qrcode.fragments.ScanQRFragment
import com.uygemre.qrcode.fragments.SettingsFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkCameraPermission()
        setupBottomBarView()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.CAMERA
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.CAMERA),
                    1
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.CAMERA),
                    1
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    if ((ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED)
                    ) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return

            }
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

        val bottomNavBarClickListener =
            BottomNavigationView.OnNavigationItemSelectedListener { p0 ->
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
}