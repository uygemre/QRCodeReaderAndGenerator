package com.uygemre.qrcode.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.uygemre.qrcode.R
import com.uygemre.qrcode.enums.IntentBundleKeyEnum
import com.uygemre.qrcode.fragments.DetailFragment
import com.uygemre.qrcode.fragments.PremiumSubscribeFragment

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        openRelatedFragment(intent)
    }

    private fun openRelatedFragment(intent: Intent?) {
        intent?.let {
            when (it.getStringExtra(IntentBundleKeyEnum.DETAIL_KEY.toString())) {
                IntentBundleKeyEnum.DETAIL_WEB_URL.toString() -> {
                    navigateToFragment(DetailFragment.newInstance(it.extras))
                }
                IntentBundleKeyEnum.DETAIL_DOCUMENT.toString() -> {
                    navigateToFragment(DetailFragment.newInstance(it.extras))
                }
                IntentBundleKeyEnum.DETAIL_CONTACT.toString() -> {
                    navigateToFragment(DetailFragment.newInstance(it.extras))
                }
                IntentBundleKeyEnum.DETAIL_EMAIL.toString() -> {
                    navigateToFragment(DetailFragment.newInstance(it.extras))
                }
                IntentBundleKeyEnum.DETAIL_SMS.toString() -> {
                    navigateToFragment(DetailFragment.newInstance(it.extras))
                }
                IntentBundleKeyEnum.DETAIL_LOCATION.toString() -> {
                    navigateToFragment(DetailFragment.newInstance(it.extras))
                }
                IntentBundleKeyEnum.DETAIL_PHONE.toString() -> {
                    navigateToFragment(DetailFragment.newInstance(it.extras))
                }
                IntentBundleKeyEnum.DETAIL_WIFI.toString() -> {
                    navigateToFragment(DetailFragment.newInstance(it.extras))
                }
                IntentBundleKeyEnum.DETAIL_TWITTER.toString() -> {
                    navigateToFragment(DetailFragment.newInstance(it.extras))
                }
                IntentBundleKeyEnum.DETAIL_WHATSAPP.toString() -> {
                    navigateToFragment(DetailFragment.newInstance(it.extras))
                }
                IntentBundleKeyEnum.DETAIL_LINKEDIN.toString() -> {
                    navigateToFragment(DetailFragment.newInstance(it.extras))
                }
                IntentBundleKeyEnum.DETAIL_YOUTUBE.toString() -> {
                    navigateToFragment(DetailFragment.newInstance(it.extras))
                }
                IntentBundleKeyEnum.DETAIL_INSTAGRAM.toString() -> {
                    navigateToFragment(DetailFragment.newInstance(it.extras))
                }
                IntentBundleKeyEnum.DETAIL_PREMIUM_SUBSCRIBE.toString() -> {
                    navigateToFragment(PremiumSubscribeFragment())
                }
                /*
                IntentBundleKeyEnum.DETAIL_CALENDAR.toString() -> {
                    navigateToFragment(DetailFragment.newInstance(it.extras))
                }

                */
            }
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.detail_container, fragment)
        transaction.commit()
    }
}