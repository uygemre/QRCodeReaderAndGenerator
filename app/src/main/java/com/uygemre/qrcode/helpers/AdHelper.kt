package com.uygemre.qrcode.helpers

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.uygemre.qrcode.constants.PrefConstants

class AdHelper {

    companion object {
        fun loadAndShowInterstitialAd(
            context: Context,
            activity: FragmentActivity,
            isPremium: Boolean
        ) {
            InterstitialAd.load(
                context,
                PrefConstants.INTERSTITIAL_AD_PRODUCT_KEY,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        if (!isPremium)
                            interstitialAd.show(activity)
                    }
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                    }
                })
        }
    }
}