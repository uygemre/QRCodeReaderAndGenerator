package com.uygemre.qrcode.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.SystemClock
import android.text.Html
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uygemre.qrcode.R
import com.uygemre.qrcode.extensions.DateExtensions
import com.uygemre.qrcode.extensions.isNetworkConnected
import com.uygemre.qrcode.extensions.showNoInternetDialog
import com.uygemre.qrcode.extensions.visibile
import kotlinx.android.synthetic.main.item_contact.*
import kotlinx.android.synthetic.main.item_document.*
import kotlinx.android.synthetic.main.item_email.*
import kotlinx.android.synthetic.main.item_location.*
import kotlinx.android.synthetic.main.item_phone.*
import kotlinx.android.synthetic.main.item_sms.*
import kotlinx.android.synthetic.main.item_web_url.*
import kotlinx.android.synthetic.main.item_wifi.*
import kotlinx.android.synthetic.main.layout_dialog_result_scan_qr.*
import net.glxn.qrgen.android.QRCode

class DialogResultScanQR : BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme
    var sharedLastClickTime = 0L

    private var intent = Intent()
    private var text: String? = ""

    /////////////////////////////////////////////
    private var contactName: String? = null
    private var contactCompany: String? = null
    private var contactAddress: String? = null
    private var contactTelephone: String? = null
    private var contactEMail: String? = null
    private var contactNotes: String? = null

    /////////////////////////////////////////////
    private var sendEmail: String? = ""
    private var sendEmailSubject: String? = ""
    private var sendEmailMessage: String? = ""

    /////////////////////////////////////////////
    private var smsPhone: String? = ""
    private var smsMessage: String? = ""

    /////////////////////////////////////////////
    private var latitude: String? = ""
    private var longitude: String? = ""

    /////////////////////////////////////////////
    private var phone: String? = ""

    /////////////////////////////////////////////
    private var wifiSsid: String? = ""
    private var wifiPassword: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_dialog_result_scan_qr, container, false)
    }

    @SuppressLint("ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
        openBrowserOnClick()
        searchInBrowserOnClick()
        sendEmailOnClick()
        sendSmsOnClick()
        showMapOnClick()
        getDirectionsOnClick()
        callOnClick()
        copyOnClick()
        //connectOnClick()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog?
            setupHalfHeight(bottomSheetDialog)
        }

        return dialog
    }

    private fun setupHalfHeight(bottomSheetDialog: BottomSheetDialog?) {
        val bottomSheet =
            bottomSheetDialog?.findViewById<View?>(R.id.design_bottom_sheet) as FrameLayout?
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet!!)
        val layoutParams = bottomSheet.layoutParams
        val windowHeight = getWindowHeight()

        if (layoutParams != null) {
            layoutParams.height = windowHeight
        }

        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun getWindowHeight(): Int {
        val displayMetrics = DisplayMetrics()
        (context as Activity?)?.windowManager?.defaultDisplay
            ?.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    @SuppressLint("SetTextI18n")
    private fun setupView() {
        text = arguments?.getString("text")
        val barcodeFormat = arguments?.getString("barcodeFormat")
        img_scan_qr.setImageBitmap(QRCode.from(text).withSize(500, 500).bitmap())

        when {
            text?.startsWith("https://") == true || text?.startsWith("http://") == true -> {
                tv_result.text = Html.fromHtml(
                    "<b>" + "WEB URL" + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, $barcodeFormat"
                )
                layout_web_url.visibile()
                tv_description.text = text
            }
            text?.startsWith("MECARD") == true || text?.startsWith("BEGIN:VCARD") == true -> {
                tv_result.text = Html.fromHtml(
                    "<b>" + "CONTACT" + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, $barcodeFormat"
                )
                layout_contact.visibile()
                if (text?.startsWith("MECARD") == true)
                    tv_description.text = setMeCardDescription(text)
                else if (text?.startsWith("BEGIN:VCARD") == true)
                    tv_description.text = text

                //separateContact(text)
            }
            text?.startsWith("mailto") == true || text?.startsWith("MAILTO") == true -> {
                tv_result.text = Html.fromHtml(
                    "<b>" + "E-MAIL" + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, $barcodeFormat"
                )

                tv_description.text = setupEMailDescription(text)
                layout_email.visibile()
                separateEMail(text)
            }
            text?.startsWith("SMSTO:") == true || text?.startsWith("sms:") == true -> {
                tv_result.text = Html.fromHtml(
                    "<b>" + "SMS" + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, $barcodeFormat"
                )
                layout_sms.visibile()
                tv_description.text = setupSmsDescription(text)
                separateSms(text)
            }
            text?.startsWith("geo") == true || text?.startsWith("GEO") == true -> {
                tv_result.text = Html.fromHtml(
                    "<b>" + "LOCATION" + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, $barcodeFormat"
                )
                tv_description.text = setupLocationDescription(text)
                separateLocation(text)
                layout_location.visibile()
            }
            text?.startsWith("tel:") == true -> {
                tv_result.text = Html.fromHtml(
                    "<b>" + "PHONE" + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, $barcodeFormat"
                )
                layout_phone.visibile()
                phone = text?.replace("tel:", "")
                tv_description.text = text?.replace("tel:", "")
            }
            text?.startsWith("WIFI") == true -> {
                tv_result.text = Html.fromHtml(
                    "<b>" + "WI-FI" + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, $barcodeFormat"
                )
                tv_description.text = setupWifiDescription(text)
                separateWifi(text)
                layout_wifi.visibile()
            }
            else -> {
                tv_result.text = Html.fromHtml(
                    "<b>" + "DOCUMENT" + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, $barcodeFormat"
                )
                layout_document.visibile()
                tv_description.text = "$text"
            }
        }
    }

    // BEGIN:VCARD
    //VERSION:3.0
    //N:emre uygun
    //ORG:Demirören
    //TITLE:emre uygun
    //TEL:05356424624
    //EMAIL:uygemre@gmail.com
    //ADR:Gökalp Mahallesi 35. Sokak No:6/5 Zeytinburnu /?STANBUL
    //NOTE:Notes
    //END:VCARD

    private fun separateContact(text: String?) {
        if (text?.contains("N:") == true) {
            contactName = text.removeRange(
                text.getOrNull(0)?.toInt()?.rangeTo(text.indexOf("N:"))!!
            )
            Log.d("contactName", contactName ?: "empty")
        }
    }

    private fun separateEMail(text: String?) {
        if (text?.startsWith("mailto") == true || text?.startsWith("MAILTO") == true) {
            if (text.contains("subject") && text.contains("body")) {
                val str = text.replace("subject=", "")
                    .replace("body=", "")
                    .replace("mailto:", "")
                    .replace("MAILTO:", "")

                sendEmail = str.substring(0, str.indexOf("?"))
                sendEmailSubject = str.substring(str.indexOf("?") + 1, str.indexOf("&"))
                sendEmailMessage = str.substring(str.indexOf("&") + 1, str.lastIndex + 1)
            } else if (text.contains("subject") && !text.contains("body")) {
                val str = text.replace("subject=", "")
                    .replace("body=", "")
                    .replace("mailto:", "")
                    .replace("MAILTO:", "")

                sendEmail = str.substring(0, str.indexOf("?"))
                sendEmailSubject = str.substring(str.indexOf("?") + 1, str.lastIndex + 1)
                sendEmailMessage = ""
            } else if (!text.contains("subject") && text.contains("body")) {
                val str = text.replace("subject=", "")
                    .replace("body=", "")
                    .replace("mailto:", "")
                    .replace("MAILTO:", "")

                sendEmail = str.substring(0, str.indexOf("?"))
                sendEmailSubject = ""
                sendEmailMessage = str.substring(str.indexOf("?") + 1, str.lastIndex + 1)
            } else {
                val str = text.replace("mailto:", "")
                    .replace("MAILTO:", "")

                sendEmail = str
                sendEmailSubject = ""
                sendEmailMessage = ""
            }
        }
    }

    private fun separateSms(text: String?) {
        when {
            text?.startsWith("sms:") == true -> {
                val str = text.replace("sms:", "")
                    .replace("body=", "")

                if (text.contains("?body")) {
                    smsPhone = str.substring(0, text.indexOf("?"))
                    smsMessage = str.substring(text.indexOf("?") + 1, str.lastIndex + 1)
                } else {
                    smsPhone = str
                    smsMessage = ""
                }
            }
            text?.startsWith("SMSTO:") == true -> {
                val str = text.replace("SMSTO:", "")
                if (str.contains(":")) {
                    smsPhone = str.substring(0, str.indexOf(":"))
                    smsMessage = str.substring(str.indexOf(":") + 1, str.lastIndex + 1)
                } else {
                    smsPhone = str
                    smsMessage = ""
                }
            }
        }
    }

    private fun separateLocation(text: String?) {
        val str = text?.replace("geo:", "")
            ?.replace("GEO:", "")
        if (str?.contains(",") == true) {
            latitude = str.substring(0, str.indexOf(","))
            longitude = str.substring(str.indexOf(",") + 1, str.lastIndex + 1)
        } else {
            latitude = str
            longitude = 0.toString()
        }
    }

    private fun separateWifi(text: String?) {
        if (text?.startsWith("WIFI:S:") == true && text.contains(";;")) {
            val str = text.replace("WIFI:S:", "")
                .replace(";;", "")
            wifiSsid = str.substring(0, str.indexOf(";"))
            wifiPassword = str.substring(str.indexOf("P:") + 2, str.lastIndex + 1)
        } else if (text?.startsWith("WIFI:S:") == true && !text.contains(";;")) {
            var str = text.replace("WIFI:S:", "")
            wifiSsid = str.substring(0, str.indexOf(";"))
            if (text.contains("H:false") || text.contains("H:true")) {
                str = str.replace("H:false;", "")
                    .replace("H:true;", "")
                wifiPassword = str.substring(str.indexOf("P:"), str.lastIndex)
                wifiPassword = wifiPassword?.replace("P:", "")
            } else {
                wifiPassword = str.substring(str.indexOf("P:"), str.lastIndex + 1)
                wifiPassword = wifiPassword?.replace("P:", "")
            }
        } else if (text?.startsWith("WIFI:T:") == true) {
            var str = text.replace("WIFI:T:", "")
            val t = str.substring(0, str.indexOf("S:"))
            str = str.replace(t, "").replace("S:", "")
            wifiSsid = str.substring(0, str.indexOf(";"))
            if (text.contains("H:false") || text.contains("H:true")) {
                str = str.replace("H:false;", "")
                    .replace("H:true;", "")
                wifiPassword = str.substring(str.indexOf("P:"), str.lastIndex)
                wifiPassword = wifiPassword?.replace("P:", "")
            } else {
                wifiPassword = str.substring(str.indexOf("P:"), str.lastIndex + 1)
                wifiPassword = wifiPassword?.replace("P:", "")
            }
        }
    }

    private fun setupLocationDescription(text: String?): String {
        return text?.replace("geo:", "")
            ?.replace("GEO:", "")
            ?.replace(",", "\n") ?: ""
    }

    private fun setupWifiDescription(text: String?): String {
        return text?.replace("WIFI:S:", "")
            ?.replace(";T:", "\n")
            ?.replace(";P:", "\n")
            ?.replace(";H:", "")
            ?.replace(";;", "")?: ""
    }

    private fun setupSmsDescription(text: String?): String {
        when {
            text?.startsWith("sms:") == true -> {
                val str = text.replace("sms:", "")
                    .replace("body=", "")

                return if (str.contains("?")) {
                    smsPhone = str.substring(0, str.indexOf("?"))
                    smsMessage = str.substring(str.indexOf("?") + 1, str.lastIndex + 1)
                    "$smsPhone\n$smsMessage"
                } else {
                    smsPhone = str
                    smsMessage = ""
                    "$smsPhone"
                }
            }
            text?.startsWith("SMSTO:") == true -> {
                val str = text.replace("SMSTO:", "")
                return if (str.contains(":")) {
                    smsPhone = str.substring(0, str.indexOf(":"))
                    smsMessage = str.substring(str.indexOf(":") + 1, str.lastIndex + 1)
                    "$smsPhone\n$smsMessage"
                } else {
                    smsPhone = str
                    smsMessage = ""
                    "$smsPhone"
                }
            }
            else -> {
                return text ?: ""
            }
        }
    }

    private fun setMeCardDescription(text: String?): String {
        return text?.replace("MECARD:", "")
            ?.replace("N:", "")
            ?.replace("ORG:", "")
            ?.replace("ADR:", "")
            ?.replace("EMAIL:", "")
            ?.replace("TEL:", "")
            ?.replace("NOTE:", "")
            ?.replace(";;", "")
            ?.replace(";", "\n") ?: ""
    }

    private fun setupEMailDescription(text: String?): String {
        return text?.replace("mailto:", "")
            ?.replace("MAILTO:", "")
            ?.replace("?subject=", "\n")
            ?.replace("&subject=", "\n")
            ?.replace("?body=", "\n")
            ?.replace("&body=", "\n") ?: ""
    }

    private fun copyToClipboard(text: String?) {
        val clipboard  = ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
        val clip = ClipData.newPlainText("Copy", text)
        clipboard?.setPrimaryClip(clip)
    }

    private fun shareDate(text: String?) {
        if (requireContext().isNetworkConnected()) {
            if (SystemClock.elapsedRealtime() - sharedLastClickTime < 1000) {
                return
            }
            sharedLastClickTime = SystemClock.elapsedRealtime()

            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "$text")
                type = "text/plain"
            }
            ContextCompat.startActivity(
                requireContext(),
                Intent.createChooser(sendIntent, "Seçiniz"),
                null
            )
        } else {
            requireContext().showNoInternetDialog()
        }
    }

    private fun openBrowserOnClick() {
        ll_open_browser.setOnClickListener {
            intent = Intent(Intent.ACTION_VIEW, Uri.parse(text))
            startActivity(intent)
        }
    }

    private fun searchInBrowserOnClick() {
        ll_search.setOnClickListener {
            intent = Intent(Intent.ACTION_WEB_SEARCH)
            intent.putExtra(SearchManager.QUERY, text)
            startActivity(intent)
        }
    }

    private fun sendEmailOnClick() {
        ll_send_email.setOnClickListener {
            intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(text)
            startActivity(intent)
        }
    }

    private fun sendSmsOnClick() {
        ll_send_sms.setOnClickListener {
            intent = Intent(Intent.ACTION_VIEW, Uri.parse("smsto:$smsPhone"))
            intent.putExtra("sms_body", smsMessage)
            startActivity(intent)
        }
    }

    private fun showMapOnClick() {
        ll_show_map.setOnClickListener {
            val uri = "http://maps.google.com/maps?q=loc:$latitude,$longitude"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))

            intent.setClassName(
                "com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity"
            )

            startActivity(intent)
        }
    }

    private fun getDirectionsOnClick() {
        ll_get_directions.setOnClickListener {
            val gmmIntentUri = Uri.parse("google.navigation:q=$latitude,$longitude&mode=d")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }
    }

    private fun callOnClick() {
        ll_call.setOnClickListener {
            intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            startActivity(intent)
        }
    }

    private fun connectOnClick() {
        ll_wifi_connect.setOnClickListener {
            val wifiConfiguration = WifiConfiguration()
            wifiConfiguration.SSID = "\"" + wifiSsid + "\""

            wifiConfiguration.wepKeys[0] = "\"" + wifiPassword + "\""
            wifiConfiguration.wepTxKeyIndex = 0
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)

            wifiConfiguration.preSharedKey = "\""+ wifiPassword +"\""
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)

            val wifiManager = context?.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val id = wifiManager.addNetwork(wifiConfiguration)
            wifiManager.disconnect()
            wifiManager.enableNetwork(id, true)
            wifiManager.reconnect()
        }
    }

    private fun copyOnClick() {
        ll_copy_web_url.setOnClickListener {
            copyToClipboard(text)
        }
        ll_copy_document.setOnClickListener {
            copyToClipboard(text)
        }
        ll_copy_email.setOnClickListener {
            copyToClipboard(setupEMailDescription(text))
        }
        ll_copy_sms.setOnClickListener {
            copyToClipboard(setupSmsDescription(text))
        }
        ll_copy_location.setOnClickListener {
            copyToClipboard(setupLocationDescription(text))
        }
        ll_copy_phone.setOnClickListener {
            copyToClipboard(text?.replace("tel:", ""))
        }
        ll_copy_wifi.setOnClickListener {
            copyToClipboard(setupWifiDescription(text))
        }
        ll_copy_contact.setOnClickListener {
        }
    }
}

// web url √
// document √
// contact
// email √
// sms √
// location √
// phone √
// wifi