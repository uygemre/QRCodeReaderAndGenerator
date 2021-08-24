package com.uygemre.qrcode.fragments

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.uygemre.qrcode.R
import com.uygemre.qrcode.constants.PrefConstants
import com.uygemre.qrcode.helpers.LocalPrefManager
import kotlinx.android.synthetic.main.fragment_settings.*
import java.util.*

class SettingsFragment : Fragment() {

    lateinit var localPrefManager: LocalPrefManager
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutInflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MobileAds.initialize(requireContext())
        InterstitialAd.load(requireContext(), "ca-app-pub-7295215165419770/5915515669", AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(p0: InterstitialAd) {
                mInterstitialAd = p0
                mInterstitialAd?.show(requireActivity())
            }
            override fun onAdFailedToLoad(p0: LoadAdError) {
                mInterstitialAd = null
            }
        })

        localPrefManager = LocalPrefManager(requireContext())
        switchOnCheckedChanged()
        switchIsChecked()
        setupSwitchText()

        val language = resources.getStringArray(R.array.Languages)
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.layout_spinner,
            language
        )

        spinners_language.adapter = adapter
        spinners_language.setSelection(setupSpinner(language))

        spinners_language.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != language.indexOf(
                        localPrefManager.pull(
                            PrefConstants.PREF_LANGUAGE,
                            ""
                        )
                    )
                ) {
                    val intent = requireActivity().baseContext.packageManager
                        .getLaunchIntentForPackage(requireActivity().baseContext.packageName)

                    localPrefManager.push(PrefConstants.PREF_LANGUAGE, language[position])
                    requireActivity().finish()
                    startActivity(intent)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun switchOnCheckedChanged() {
        switch_open_browser.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                localPrefManager.push(PrefConstants.PREF_IS_OPEN_BROWSER_AUTO, true)
            } else {
                localPrefManager.push(PrefConstants.PREF_IS_OPEN_BROWSER_AUTO, false)
            }
        }
        switch_auto_copy_clipboard.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                localPrefManager.push(PrefConstants.PREF_AUTO_COPY_CLIPBOARD, true)
            else
                localPrefManager.push(PrefConstants.PREF_AUTO_COPY_CLIPBOARD, false)
        }
        switch_auto_search_on_web.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                localPrefManager.push(PrefConstants.PREF_AUTO_SEARCH_ON_WEB, true)
            else
                localPrefManager.push(PrefConstants.PREF_AUTO_SEARCH_ON_WEB, false)
        }
        switch_vibration.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                localPrefManager.push(PrefConstants.PREF_VIBRATION, true)
            else
                localPrefManager.push(PrefConstants.PREF_VIBRATION, false)
        }
        switch_sound.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                localPrefManager.push(PrefConstants.PREF_SOUND, true)
            else
                localPrefManager.push(PrefConstants.PREF_SOUND, false)
        }
    }

    private fun switchIsChecked() {
        switch_open_browser.isChecked =
            localPrefManager.pull(PrefConstants.PREF_IS_OPEN_BROWSER_AUTO, false)
        switch_auto_copy_clipboard.isChecked =
            localPrefManager.pull(PrefConstants.PREF_AUTO_COPY_CLIPBOARD, false)
        switch_auto_search_on_web.isChecked =
            localPrefManager.pull(PrefConstants.PREF_AUTO_SEARCH_ON_WEB, false)
        switch_vibration.isChecked =
            localPrefManager.pull(PrefConstants.PREF_VIBRATION, false)
        switch_sound.isChecked =
            localPrefManager.pull(PrefConstants.PREF_SOUND, false)
    }

    private fun setupSwitchText() {
        switch_vibration.text =
            Html.fromHtml(
                "<b>" + resources.getString(R.string.vibration) + "</b>" + "<br>" + "</br>" + resources.getString(
                    R.string.description_vibration
                )
            )
        switch_sound.text =
            Html.fromHtml(
                "<b>" + resources.getString(R.string.sound) + "</b>" + "<br>" + "</br>" + resources.getString(
                    R.string.description_sound
                )
            )
        switch_auto_copy_clipboard.text =
            Html.fromHtml(
                "<b>" + resources.getString(R.string.auto_copy_to_clipboard) + "</b>" + "<br>" + "</br>" + resources.getString(
                    R.string.description_auto_copy_to_clipboard
                )
            )
        switch_auto_search_on_web.text =
            Html.fromHtml(
                "<b>" + resources.getString(R.string.auto_web_search) + "</b>" + "<br>" + "</br>" + resources.getString(
                    R.string.description_auto_web_search
                )
            )
        switch_open_browser.text =
            Html.fromHtml(
                "<b>" + resources.getString(R.string.auto_open_url) + "</b>" + "<br>" + "</br>" + resources.getString(
                    R.string.description_auto_open_url
                )
            )
    }

    private fun setupSpinner(language: Array<String>): Int {
        return when {
            localPrefManager.pull(PrefConstants.PREF_LANGUAGE, "") == "Turkish" -> {
                language.indexOf("Turkish")
            }
            localPrefManager.pull(PrefConstants.PREF_LANGUAGE, "") == "English" -> {
                language.indexOf("English")
            }
            localPrefManager.pull(PrefConstants.PREF_LANGUAGE, "") == "Italian" -> {
                language.indexOf("Italian")
            }
            localPrefManager.pull(PrefConstants.PREF_LANGUAGE, "") == "German" -> {
                language.indexOf("German")
            }
            localPrefManager.pull(PrefConstants.PREF_LANGUAGE, "") == "Spanish" -> {
                language.indexOf("Spanish")
            }
            localPrefManager.pull(PrefConstants.PREF_LANGUAGE, "") == "Chinese (Simplified)" -> {
                language.indexOf("Chinese (Simplified)")
            }
            localPrefManager.pull(PrefConstants.PREF_LANGUAGE, "") == "Hindi" -> {
                language.indexOf("Hindi")
            }
            else -> {
                when (Locale.getDefault().language) {
                    "tr" -> {
                        localPrefManager.push(PrefConstants.PREF_LANGUAGE, "Turkish")
                        language.indexOf("Turkish")
                    }
                    "it" -> {
                        localPrefManager.push(PrefConstants.PREF_LANGUAGE, "Italian")
                        language.indexOf("Italian")
                    }
                    "en" -> {
                        localPrefManager.push(PrefConstants.PREF_LANGUAGE, "English")
                        language.indexOf("English")
                    }
                    "es" -> {
                        localPrefManager.push(PrefConstants.PREF_LANGUAGE, "Spanish")
                        language.indexOf("Spanish")
                    }
                    "de" -> {
                        localPrefManager.push(PrefConstants.PREF_LANGUAGE, "German")
                        language.indexOf("German")
                    }
                    "zh" -> {
                        localPrefManager.push(PrefConstants.PREF_LANGUAGE, "Chinese (Simplified)")
                        language.indexOf("Chinese (Simplified)")
                    }
                    "hi" -> {
                        localPrefManager.push(PrefConstants.PREF_LANGUAGE, "Hindi")
                        language.indexOf("Hindi")
                    }
                    else -> {
                        localPrefManager.push(PrefConstants.PREF_LANGUAGE, "English")
                        language.indexOf("English")
                    }
                }
            }
        }
    }
}