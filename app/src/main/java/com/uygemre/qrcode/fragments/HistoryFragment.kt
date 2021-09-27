package com.uygemre.qrcode.fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.uygemre.qrcode.R
import com.uygemre.qrcode.constants.PrefConstants
import com.uygemre.qrcode.database.AppDatabase
import com.uygemre.qrcode.database.QRCodeDTO
import com.uygemre.qrcode.database.QRCodeDao
import com.uygemre.qrcode.dialog.DialogResultScanQR
import com.uygemre.qrcode.helpers.LocalPrefManager
import com.uygemre.qrcode.recyclerview.OnItemClickListener
import com.uygemre.qrcode.recyclerview.RecyclerViewAdapter
import kotlinx.android.synthetic.main.fragment_history.*

class HistoryFragment : Fragment(), OnItemClickListener {

    private lateinit var qrCodeDao: QRCodeDao
    private lateinit var recyclerViewAdapter: RecyclerViewAdapter
    private lateinit var localPrefManager: LocalPrefManager
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        localPrefManager = LocalPrefManager(requireContext())
        qrCodeDao = AppDatabase.getInstance(requireContext())?.qrCodeDao()!!

        if (!localPrefManager.isPremium())
            loadInterstitialAd()

        recyclerViewAdapter = RecyclerViewAdapter(qrCodeDao.getAll().reversed(), this)
        rv_history.layoutManager = LinearLayoutManager(requireContext())
        rv_history.adapter = recyclerViewAdapter
    }

    override fun deleteItemOnClicked(qrCodeDTO: QRCodeDTO, position: Int) {
        qrCodeDao.delete(qrCodeDTO)
        Handler().postDelayed({
            recyclerViewAdapter.update(qrCodeDao.getAll().reversed())
        }, 1000)
    }

    override fun goDetailItemOnClicked(qrCodeDTO: QRCodeDTO) {
        if (!localPrefManager.isPremium()) {
            mInterstitialAd?.show(requireActivity())
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    openDialog(
                        text = qrCodeDTO.text ?: "",
                        barcodeFormat = qrCodeDTO.barcodeFormat ?: ""
                    )
                }
            }
        } else {
            openDialog(text = qrCodeDTO.text ?: "", barcodeFormat = qrCodeDTO.barcodeFormat ?: "")
        }
    }

    private fun openDialog(text: String, barcodeFormat: String) {
        val dialog = DialogResultScanQR()
        dialog.arguments = Bundle().apply {
            putString("text", text)
            putString("barcodeFormat", barcodeFormat)
        }
        dialog.show(childFragmentManager, "dialog")
    }

    private fun loadInterstitialAd() {
        MobileAds.initialize(requireContext())
        InterstitialAd.load(
            requireContext(),
            PrefConstants.INTERSTITIAL_AD_PRODUCT_KEY,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(p0: InterstitialAd) {
                    mInterstitialAd = p0
                    mInterstitialAd?.show(requireActivity())
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    mInterstitialAd = null
                }
            })
    }
}