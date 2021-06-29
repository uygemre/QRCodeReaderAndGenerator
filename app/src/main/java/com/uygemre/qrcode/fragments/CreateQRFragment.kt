package com.uygemre.qrcode.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.uygemre.qrcode.activities.DetailActivity
import com.uygemre.qrcode.R
import com.uygemre.qrcode.constants.PrefConstants
import com.uygemre.qrcode.enums.IntentBundleKeyEnum
import com.uygemre.qrcode.extensions.openActivity
import kotlinx.android.synthetic.main.fragment_create_qr.*

class CreateQRFragment : Fragment() {

    private val bundle: Bundle = Bundle()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutInflater.inflate(R.layout.fragment_create_qr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv_web_url.setOnClickListener {
            gotoDetailPage(IntentBundleKeyEnum.DETAIL_WEB_URL.toString())
        }
        rv_document.setOnClickListener {
            gotoDetailPage(IntentBundleKeyEnum.DETAIL_DOCUMENT.toString())
        }
        rv_contact.setOnClickListener {
            gotoDetailPage(IntentBundleKeyEnum.DETAIL_CONTACT.toString())
        }
        rv_email.setOnClickListener {
            gotoDetailPage(IntentBundleKeyEnum.DETAIL_EMAIL.toString())
        }
        rv_sms.setOnClickListener {
            gotoDetailPage(IntentBundleKeyEnum.DETAIL_SMS.toString())
        }
        rv_location.setOnClickListener {
            gotoDetailPage(IntentBundleKeyEnum.DETAIL_LOCATION.toString())
        }
        rv_phone.setOnClickListener {
            gotoDetailPage(IntentBundleKeyEnum.DETAIL_PHONE.toString())
        }
        rv_wifi.setOnClickListener {
            gotoDetailPage(IntentBundleKeyEnum.DETAIL_WIFI.toString())
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