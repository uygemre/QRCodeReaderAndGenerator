package com.uygemre.qrcode.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.uygemre.qrcode.R
import com.uygemre.qrcode.constants.PrefConstants
import com.uygemre.qrcode.dialog.DialogCreateQR
import com.uygemre.qrcode.enums.IntentBundleKeyEnum
import com.uygemre.qrcode.extensions.checkNull
import com.uygemre.qrcode.extensions.multipleInputEditText
import kotlinx.android.synthetic.main.layout_contact.*
import kotlinx.android.synthetic.main.layout_document.*
import kotlinx.android.synthetic.main.layout_email.*
import kotlinx.android.synthetic.main.layout_location.*
import kotlinx.android.synthetic.main.layout_phone.*
import kotlinx.android.synthetic.main.layout_sms.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import kotlinx.android.synthetic.main.layout_web_url.*
import kotlinx.android.synthetic.main.layout_wifi.*
import kotlinx.android.synthetic.main.layout_wifi.view.*
import net.glxn.qrgen.core.scheme.Wifi
import java.util.*

class DetailFragment : Fragment() {

    private var contentView: String? = ""
    private var isHidden: Boolean? = false
    private var wifiAuthentication: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments.let {
            contentView = it?.getString(PrefConstants.PREF_CONTENT_VIEW)
        }

        when (contentView) {
            IntentBundleKeyEnum.DETAIL_WEB_URL.toString() -> {
                return inflater.inflate(R.layout.layout_web_url, container, false)
            }
            IntentBundleKeyEnum.DETAIL_DOCUMENT.toString() -> {
                return inflater.inflate(R.layout.layout_document, container, false)
            }
            IntentBundleKeyEnum.DETAIL_CONTACT.toString() -> {
                return inflater.inflate(R.layout.layout_contact, container, false)
            }
            IntentBundleKeyEnum.DETAIL_EMAIL.toString() -> {
                return inflater.inflate(R.layout.layout_email, container, false)
            }
            IntentBundleKeyEnum.DETAIL_SMS.toString() -> {
                return inflater.inflate(R.layout.layout_sms, container, false)
            }
            IntentBundleKeyEnum.DETAIL_LOCATION.toString() -> {
                return inflater.inflate(R.layout.layout_location, container, false)
            }
            IntentBundleKeyEnum.DETAIL_PHONE.toString() -> {
                return inflater.inflate(R.layout.layout_phone, container, false)
            }
            IntentBundleKeyEnum.DETAIL_WIFI.toString() -> {
                return inflater.inflate(R.layout.layout_wifi, container, false)
            }

            /*
IntentBundleKeyEnum.DETAIL_CALENDAR.toString() -> {
return inflater.inflate(R.layout.layout_calendar, container, false)
}
*/
            else -> return layoutInflater.inflate(R.layout.fragment_detail, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbarTitleAndImage()

        view.btn_create?.setOnClickListener {
            val dialog = DialogCreateQR()
            val bundle = Bundle()
            var isNull = false
            val list = mutableListOf<TextInputEditText>()

            when (contentView) {
                IntentBundleKeyEnum.DETAIL_WEB_URL.toString() -> {
                    bundle.putString(
                        PrefConstants.PREF_WEB_URL,
                        edt_web_url.text.toString().toLowerCase(Locale.ROOT)
                    )
                    bundle.putString(
                        PrefConstants.PREF_CONTENT_VIEW,
                        IntentBundleKeyEnum.DETAIL_WEB_URL.toString()
                    )

                    isNull = edt_web_url.checkNull(til_web_url)
                }
                IntentBundleKeyEnum.DETAIL_DOCUMENT.toString() -> {
                    bundle.putString(
                        PrefConstants.PREF_DOCUMENT,
                        edt_document.text.toString().toLowerCase()
                    )
                    bundle.putString(
                        PrefConstants.PREF_CONTENT_VIEW,
                        IntentBundleKeyEnum.DETAIL_DOCUMENT.toString()
                    )

                    isNull = edt_document.checkNull(til_document)
                }
                IntentBundleKeyEnum.DETAIL_EMAIL.toString() -> {
                    bundle.putString(
                        PrefConstants.PREF_EMAIL,
                        edt_email.text.toString().toLowerCase(Locale.getDefault())
                    )
                    bundle.putString(
                        PrefConstants.PREF_EMAIL_SUBJECT,
                        edt_subject.text.toString()
                    )
                    bundle.putString(
                        PrefConstants.PREF_EMAIL_MESSAGE,
                        edt_message.text.toString()
                    )
                    bundle.putString(
                        PrefConstants.PREF_CONTENT_VIEW,
                        IntentBundleKeyEnum.DETAIL_EMAIL.toString()
                    )

                    isNull = edt_email.checkNull(til_email)
                }
                IntentBundleKeyEnum.DETAIL_CONTACT.toString() -> {
                    bundle.putString(
                        PrefConstants.PREF_CONTACT_NOTES,
                        edt_contact_notes.text.toString()
                    )
                    bundle.putString(
                        PrefConstants.PREF_CONTACT_ADDRESS,
                        edt_contact_address.text.toString()
                    )
                    bundle.putString(
                        PrefConstants.PREF_CONTACT_COMPANY,
                        edt_contact_company.text.toString()
                    )
                    bundle.putString(
                        PrefConstants.PREF_CONTACT_NAME,
                        edt_contact_name.text.toString().toLowerCase(Locale.getDefault())
                    )
                    bundle.putString(
                        PrefConstants.PREF_CONTACT_EMAIL,
                        edt_contact_email.text.toString().toLowerCase(Locale.getDefault())
                    )
                    bundle.putString(
                        PrefConstants.PREF_CONTACT_TELEPHONE,
                        edt_contact_telephone.text.toString().toLowerCase(Locale.getDefault())
                    )
                    bundle.putString(
                        PrefConstants.PREF_CONTENT_VIEW,
                        IntentBundleKeyEnum.DETAIL_CONTACT.toString()
                    )

                    isNull = edt_contact_name.checkNull(til_contact_name)
                }
                IntentBundleKeyEnum.DETAIL_SMS.toString() -> {
                    bundle.putString(
                        PrefConstants.PREF_CONTENT_VIEW,
                        IntentBundleKeyEnum.DETAIL_SMS.toString()
                    )
                    bundle.putString(
                        PrefConstants.PREF_SMS_SUBJECT,
                        edt_sms.text.toString()
                    )
                    bundle.putString(
                        PrefConstants.PREF_SMS_PHONE,
                        edt_sms_phone.text.toString()
                    )

                    isNull = edt_sms_phone.checkNull(til_sms_phone)
                }
                IntentBundleKeyEnum.DETAIL_LOCATION.toString() -> {
                    bundle.putString(
                        PrefConstants.PREF_CONTENT_VIEW,
                        IntentBundleKeyEnum.DETAIL_LOCATION.toString()
                    )
                    bundle.putString(
                        PrefConstants.PREF_LOCATION_LATITUDE,
                        edt_latitude.text.toString()
                    )
                    bundle.putString(
                        PrefConstants.PREF_LOCATION_LONGITUDE,
                        edt_longitude.text.toString()
                    )

                    list.add(edt_latitude)
                    list.add(edt_longitude)

                    isNull = list.multipleInputEditText(til_latitude, til_longitude)
                }
                IntentBundleKeyEnum.DETAIL_PHONE.toString() -> {
                    bundle.putString(
                        PrefConstants.PREF_CONTENT_VIEW,
                        IntentBundleKeyEnum.DETAIL_PHONE.toString()
                    )
                    bundle.putString(
                        PrefConstants.PREF_PHONE,
                        edt_phone.text.toString().toLowerCase(Locale.getDefault())
                    )

                    isNull = edt_phone.checkNull(til_phone)
                }
                IntentBundleKeyEnum.DETAIL_WIFI.toString() -> {
                    setWifiAuthenticationAndHidden()

                    bundle.putString(
                        PrefConstants.PREF_CONTENT_VIEW,
                        IntentBundleKeyEnum.DETAIL_WIFI.toString()
                    )
                    bundle.putString(
                        PrefConstants.PREF_WIFI_NETWORK_NAME,
                        edt_ssid.text.toString()
                    )
                    bundle.putString(
                        PrefConstants.PREF_WIFI_PASSWORD,
                        edt_password.text.toString()
                    )
                    bundle.putString(
                        PrefConstants.PREF_WIFI_AUTHENTICATION,
                        wifiAuthentication
                    )
                    bundle.putBoolean(
                        PrefConstants.PREF_WIFI_IS_HIDDEN,
                        isHidden ?: false
                    )

                    list.add(edt_ssid)
                    list.add(edt_password)
                    isNull = list.multipleInputEditText(til_ssid, til_password)
                }
            }

            if (isNull) {
                dialog.arguments = bundle
                dialog.show(childFragmentManager, "dialog")
                list.clear()
            }
        }

        btn_back.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun setWifiAuthenticationAndHidden() {
        when (rg_authentication?.checkedRadioButtonId) {
            R.id.rb_wpa -> wifiAuthentication = Wifi.Authentication.WPA.toString()
            R.id.rb_wep -> wifiAuthentication = Wifi.Authentication.WEP.toString()
            R.id.rb_no_pass -> wifiAuthentication = Wifi.Authentication.nopass.toString()
        }
        isHidden = cb_is_hidden?.isChecked
    }

    private fun setToolbarTitleAndImage() {
        when (contentView) {
            IntentBundleKeyEnum.DETAIL_WEB_URL.toString() -> {
                tv_title.text = resources.getString(R.string.webUrl)
                img_toolbar.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_web_url
                    )
                )
            }
            IntentBundleKeyEnum.DETAIL_DOCUMENT.toString() -> {
                tv_title.text = resources.getString(R.string.document)
                img_toolbar.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_document
                    )
                )
            }
            IntentBundleKeyEnum.DETAIL_CONTACT.toString() -> {
                tv_title.text = resources.getString(R.string.contact)
                img_toolbar.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_contact
                    )
                )
            }
            IntentBundleKeyEnum.DETAIL_EMAIL.toString() -> {
                tv_title.text = resources.getString(R.string.email)
                img_toolbar.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_email
                    )
                )
            }
            IntentBundleKeyEnum.DETAIL_SMS.toString() -> {
                tv_title.text = resources.getString(R.string.sms)
                img_toolbar.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_sms
                    )
                )
            }
            IntentBundleKeyEnum.DETAIL_LOCATION.toString() -> {
                tv_title.text = resources.getString(R.string.location)
                img_toolbar.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_location
                    )
                )
            }
            IntentBundleKeyEnum.DETAIL_PHONE.toString() -> {
                tv_title.text = resources.getString(R.string.phone)
                img_toolbar.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_phone
                    )
                )
            }
            IntentBundleKeyEnum.DETAIL_WIFI.toString() -> {
                tv_title.text = resources.getString(R.string.wifi)
                img_toolbar.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_wifi
                    )
                )
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle?) =
            DetailFragment().apply {
                arguments = bundle
            }
    }
}