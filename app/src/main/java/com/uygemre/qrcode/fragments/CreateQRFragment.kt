package com.uygemre.qrcode.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.uygemre.qrcode.activities.DetailActivity
import com.uygemre.qrcode.R
import com.uygemre.qrcode.constants.PrefConstants
import com.uygemre.qrcode.enums.IntentBundleKeyEnum
import com.uygemre.qrcode.extensions.openActivity
import com.uygemre.qrcode.helpers.LocalPrefManager
import kotlinx.android.synthetic.main.fragment_create_qr.*

class CreateQRFragment : Fragment() {

    private val bundle: Bundle = Bundle()
    lateinit var localPrefManager: LocalPrefManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutInflater.inflate(R.layout.fragment_create_qr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        localPrefManager = LocalPrefManager(requireContext())

        rl_web_url.setOnClickListener {
            gotoDetailPage(IntentBundleKeyEnum.DETAIL_WEB_URL.toString())
        }
        rl_document.setOnClickListener {
            gotoDetailPage(IntentBundleKeyEnum.DETAIL_DOCUMENT.toString())
        }
        rl_contact.setOnClickListener {
            gotoDetailPage(IntentBundleKeyEnum.DETAIL_CONTACT.toString())
        }
        rl_email.setOnClickListener {
            gotoDetailPage(IntentBundleKeyEnum.DETAIL_EMAIL.toString())
        }
        rl_sms.setOnClickListener {
            gotoDetailPage(IntentBundleKeyEnum.DETAIL_SMS.toString())
        }
        rl_location.setOnClickListener {
            gotoDetailPage(IntentBundleKeyEnum.DETAIL_LOCATION.toString())
        }
        rl_phone.setOnClickListener {
            gotoDetailPage(IntentBundleKeyEnum.DETAIL_PHONE.toString())
        }
        rl_wifi.setOnClickListener {
            gotoDetailPage(IntentBundleKeyEnum.DETAIL_WIFI.toString())
        }
        rl_twitter.setOnClickListener {
            if (localPrefManager.pull("isPremium", false))
                gotoDetailPage(IntentBundleKeyEnum.DETAIL_TWITTER.toString())
            else
                gotoDetailPage(IntentBundleKeyEnum.DETAIL_PREMIUM_SUBSCRIBE.toString())
        }
        rl_whatsapp.setOnClickListener {
            if (localPrefManager.pull("isPremium", false))
                gotoDetailPage(IntentBundleKeyEnum.DETAIL_WHATSAPP.toString())
            else
                gotoDetailPage(IntentBundleKeyEnum.DETAIL_PREMIUM_SUBSCRIBE.toString())
        }
        rl_linkedin.setOnClickListener {
            if (localPrefManager.pull("isPremium", false))
                gotoDetailPage(IntentBundleKeyEnum.DETAIL_LINKEDIN.toString())
            else
                gotoDetailPage(IntentBundleKeyEnum.DETAIL_PREMIUM_SUBSCRIBE.toString())
        }
        rl_youtube.setOnClickListener {
            if (localPrefManager.pull("isPremium", false))
                gotoDetailPage(IntentBundleKeyEnum.DETAIL_YOUTUBE.toString())
            else
                gotoDetailPage(IntentBundleKeyEnum.DETAIL_PREMIUM_SUBSCRIBE.toString())
        }
        rl_instagram.setOnClickListener {
            if (localPrefManager.pull("isPremium", false))
                gotoDetailPage(IntentBundleKeyEnum.DETAIL_INSTAGRAM.toString())
            else
                gotoDetailPage(IntentBundleKeyEnum.DETAIL_PREMIUM_SUBSCRIBE.toString())
        }
        /*

rv_calendar.setOnClickListener {
gotoDetailPage(IntentBundleKeyEnum.DETAIL_CALENDAR.toString(), IntentBundleKeyEnum.DETAIL_CALENDAR.toString())
}


*/
    }

    private fun gotoDetailPage(detailPage: String?) {
        bundle.putString(
            IntentBundleKeyEnum.DETAIL_KEY.toString(),
            detailPage
        )
        bundle.putString(PrefConstants.PREF_CONTENT_VIEW, detailPage)
        activity?.openActivity(DetailActivity::class.java, bundle)
    }
}