package com.uygemre.qrcode.fragments

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.uygemre.qrcode.R
import com.uygemre.qrcode.constants.PrefConstants
import com.uygemre.qrcode.extensions.DateExtensions
import com.uygemre.qrcode.helpers.LocalPrefManager
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : Fragment() {

    lateinit var localPrefManager: LocalPrefManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutInflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        localPrefManager = LocalPrefManager(requireContext())
        switchOnCheckedChanged()
        switchIsChecked()
        setupSwitchText()
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
            Html.fromHtml("<b>" + "Vibration" + "</b>" + "<br>" + "</br>" + "Turn on/off mobile vibration")
        switch_sound.text =
            Html.fromHtml("<b>" + "Sound" + "</b>" + "<br>" + "</br>" + "Turn on/off mobile sound")
        switch_auto_copy_clipboard.text =
            Html.fromHtml("<b>" + "Auto copy to clipboard" + "</b>" + "<br>" + "</br>" + "Turn on/off auto copt yo clipboard")
        switch_auto_search_on_web.text =
            Html.fromHtml("<b>" + "Auto web search" + "</b>" + "<br>" + "</br>" + "Turn on/off auto web search")
        switch_open_browser.text =
            Html.fromHtml("<b>" + "Auto open url" + "</b>" + "<br>" + "</br>" + "Turn on/off auto open url")
    }
}