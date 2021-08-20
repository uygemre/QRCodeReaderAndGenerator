package com.uygemre.qrcode.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.SearchManager
import android.content.*
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.*
import android.provider.Contacts
import android.provider.ContactsContract
import android.provider.Settings
import android.text.Html
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uygemre.qrcode.R
import com.uygemre.qrcode.constants.PrefConstants
import com.uygemre.qrcode.extensions.DateExtensions
import com.uygemre.qrcode.extensions.isNetworkConnected
import com.uygemre.qrcode.extensions.showNoInternetDialog
import com.uygemre.qrcode.extensions.visibile
import com.uygemre.qrcode.helpers.LocalPrefManager
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
import android.content.Intent

class DialogResultScanQR : BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme
    private lateinit var localPrefManager: LocalPrefManager
    var sharedLastClickTime = 0L

    private var intent = Intent()
    private var text: String? = ""

    private var contactName: String? = ""
    private var contactCompany: String? = ""
    private var contactAddress: String? = ""
    private var contactTelephone: String? = ""
    private var contactEMail: String? = ""
    private var contactUrl: String? = ""
    private var contactNotes: String? = ""

    private var sendEmail: String? = ""
    private var sendEmailSubject: String? = ""
    private var sendEmailMessage: String? = ""

    private var smsPhone: String? = ""
    private var smsMessage: String? = ""

    private var latitude: String? = ""
    private var longitude: String? = ""

    private var phone: String? = ""

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

        localPrefManager = LocalPrefManager(requireContext())

        MobileAds.initialize(requireContext())
        adView.loadAd(AdRequest.Builder().build())

        setupView()
        openBrowserOnClick()
        searchInBrowserOnClick()
        sendEmailOnClick()
        sendSmsOnClick()
        showMapOnClick()
        getDirectionsOnClick()
        callOnClick()
        copyOnClick()
        shareOnClick()
        addContactOnClick()
        addPhoneContactOnClick()
        connectOnClick()

        if (localPrefManager.pull(PrefConstants.PREF_VIBRATION, false))
            vibrate()
        if (localPrefManager.pull(PrefConstants.PREF_SOUND, false))
            soundBeep()
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
        val isOpenBrowserAuto = localPrefManager.pull(
            PrefConstants.PREF_IS_OPEN_BROWSER_AUTO,
            false
        )
        val isAutoCopyClipboard = localPrefManager.pull(
            PrefConstants.PREF_AUTO_COPY_CLIPBOARD,
            false
        )
        val isAutoSearchOnWeb = localPrefManager.pull(PrefConstants.PREF_AUTO_SEARCH_ON_WEB, false)
        img_scan_qr.setImageBitmap(QRCode.from(text).withSize(500, 500).bitmap())

        when {
            text?.startsWith("https://") == true || text?.startsWith("http://") == true -> {
                tv_result.text = Html.fromHtml(
                    "<b>" + resources.getString(R.string.result_web_url) + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, $barcodeFormat"
                )

                layout_web_url.visibile()
                tv_description.text = text
                if (isOpenBrowserAuto) {
                    intent = Intent(Intent.ACTION_VIEW, Uri.parse(text))
                    startActivity(intent)
                }
            }
            text?.startsWith("MECARD") == true || text?.startsWith("BEGIN:VCARD") == true -> {
                tv_result.text = Html.fromHtml(
                    "<b>" + resources.getString(R.string.result_contact) + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, $barcodeFormat"
                )
                layout_contact.visibile()

                if (text?.startsWith("MECARD") == true) {
                    tv_description.text = setMeCardDescription(text)
                    separateMeCard(text ?: "")
                } else if (text?.startsWith("BEGIN:VCARD") == true) {
                    tv_description.text = setupVCardDescription(text)
                    separateVCard(text ?: "")
                }
            }
            text?.startsWith("mailto") == true || text?.startsWith("MAILTO") == true -> {
                tv_result.text = Html.fromHtml(
                    "<b>" + resources.getString(R.string.result_e_mail) + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, $barcodeFormat"
                )

                tv_description.text = setupEMailDescription(text)
                layout_email.visibile()
                separateEMail(text)
            }
            text?.startsWith("SMSTO:") == true || text?.startsWith("sms:") == true -> {
                tv_result.text = Html.fromHtml(
                    "<b>" + resources.getString(R.string.result_sms) + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, $barcodeFormat"
                )
                layout_sms.visibile()
                tv_description.text = setupSmsDescription(text)
                separateSms(text)
            }
            text?.startsWith("geo") == true || text?.startsWith("GEO") == true -> {
                tv_result.text = Html.fromHtml(
                    "<b>" + resources.getString(R.string.result_location) + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, $barcodeFormat"
                )
                tv_description.text = setupLocationDescription(text)
                separateLocation(text)
                layout_location.visibile()
            }
            text?.startsWith("tel:") == true -> {
                tv_result.text = Html.fromHtml(
                    "<b>" + resources.getString(R.string.result_phone) + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, $barcodeFormat"
                )
                layout_phone.visibile()
                phone = text?.replace("tel:", "")
                tv_description.text = text?.replace("tel:", "")
            }
            text?.startsWith("WIFI") == true -> {
                tv_result.text = Html.fromHtml(
                    "<b>" + resources.getString(R.string.result_wifi) + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, $barcodeFormat"
                )
                tv_description.text = setupWifiDescription(text)
                separateWifi(text)
                layout_wifi.visibile()
            }
            else -> {
                tv_result.text = Html.fromHtml(
                    "<b>" + resources.getString(R.string.result_document) + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, $barcodeFormat"
                )
                layout_document.visibile()
                tv_description.text = "$text"
                if (isAutoSearchOnWeb) {
                    intent = Intent(Intent.ACTION_WEB_SEARCH)
                    intent.putExtra(SearchManager.QUERY, text)
                    startActivity(intent)
                }

            }
        }

        if (isAutoCopyClipboard) {
            copyToClipboard(text)
            Toast.makeText(requireContext(), "Copied to clipboard\n$text", Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun setupVCardDescription(text: String?): String {
        return if (text?.startsWith("BEGIN:VCARD") == true) {
            return text.replace("BEGIN:VCARD\n", "")
                .replace("VERSION:3.0\n", "")
                .replace("N:", "")
                .replace("N:;", "")
                .replace("FN:", "")
                .replace("TITLE:", "")
                .replace("ORG:", "")
                .replace("URL:", "")
                .replace("ORG:", "")
                .replace("EMAIL;TYPE=INTERNET:", "")
                .replace("TEL:", "")
                .replace("TEL;TYPE=voice,work,pref:", "")
                .replace("TEL;TYPE=voice,home,pref:", "")
                .replace("TEL;TYPE=voice,cell,pref:", "")
                .replace("TEL;TYPE=fax,work,pref:", "")
                .replace("TEL;TYPE=fax,home,pref:", "")
                .replace("ADR:", "")
                .replace("ADR:;;", "")
                .replace(";;", "")
                .replace("END:VCARD", "")
                .replace(";", " ")
                .replace("\n\n", "\n")
            /*
            return text.replace("BEGIN:VCARD\n", "")
                .replace("VERSION:3.0\n", "")
                .replace("N", "")
                .replace("N:;", "")
                .replace("FN", "")
                .replace("URL", "")
                .replace("ORG", "")
                .replace("TITLE", "")
                .replace("NOTE", "")
                .replace("TEL;TYPE=voice,home,pref", "")
                .replace("TEL;TYPE=voice,work,pref", "")
                .replace("TEL;TYPE=voice,cell,pref", "")
                .replace("TEL;TYPE=fax,home,pref", "")
                .replace("TEL;TYPE=fax,work,pref", "")
                .replace("TEL;TYPE=CELL", "")
                .replace("TEL;TYPE=FAX", "")
                .replace("TEL", "")
                .replace("ADR:;;", "")
                .replace("ADR", "")
                .replace("EMAIL;", "")
                .replace("BDAY", "")
                .replace("EMAIL", "")
                .replace("TYPE=INTERNET", "")
                .replace("TEL;TYPE=voice,cell,pref", "")
                .replace("END:VCARD", "")
                .replace(";", " ")
                .replace(":", "")
                .replace("\n\n", "\n")

             */
        } else
            text ?: ""
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

    fun setupLocationDescription(text: String?): String {
        return text?.replace("geo:", "")
            ?.replace("GEO:", "")
            ?.replace(",", "\n") ?: ""
    }

    fun setupWifiDescription(text: String?): String {
        return text?.replace("WIFI:S:", "")
            ?.replace(";T:", "\n")
            ?.replace(";P:", "\n")
            ?.replace(";H:true;", "")
            ?.replace(";H:false;", "")
            ?.replace(";;", "") ?: ""
    }

    fun setupSmsDescription(text: String?): String {
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

    private fun getParameters(
        qrCode: String,
        paramSeparator: String, keyValueSeparator: String
    ): Map<String, String> {
        val result: MutableMap<String, String> = LinkedHashMap()
        val parts = qrCode.split(paramSeparator.toRegex()).toTypedArray()
        for (i in parts.indices) {
            val param = parts[i].split(keyValueSeparator.toRegex()).toTypedArray()
            if (param.size > 1) {
                result[param[0]] = param[1]
            }
        }
        return result
    }

    private fun separateVCard(text: String) {
        val parameters: Map<String, String> =
            getParameters(text.replaceFirst("BEGIN:VCARD\n", ""), "\n", ":")

        contactName = if (parameters.containsKey("N"))
            parameters["N"]
        else ""

        contactCompany = if (parameters.containsKey("ORG"))
            parameters["ORG"]
        else ""

        contactAddress = if (parameters.containsKey("ADR"))
            parameters["ADR"]
        else ""
        contactTelephone = if (parameters.containsKey("TEL"))
            parameters["TEL"]
        else ""

        contactEMail = if (parameters.containsKey("EMAIL"))
            parameters["EMAIL"]
        else ""

        contactNotes = if (parameters.containsKey("NOTE"))
            parameters["NOTE"]
        else ""

        contactUrl = if (parameters.containsKey("URL"))
            parameters["URL"]
        else ""

    }

    private fun separateMeCard(text: String) {
        val parameters: Map<String, String> =
            getParameters(text.replaceFirst("MECARD:", ""), ";", ":")

        contactName = if (parameters.containsKey("N"))
            parameters["N"]
        else ""

        contactCompany = if (parameters.containsKey("ORG"))
            parameters["ORG"]
        else ""

        contactAddress = if (parameters.containsKey("ADR"))
            parameters["ADR"]
        else ""
        contactTelephone = if (parameters.containsKey("TEL"))
            parameters["TEL"]
        else ""

        contactEMail = if (parameters.containsKey("EMAIL"))
            parameters["EMAIL"]
        else ""

        contactNotes = if (parameters.containsKey("NOTE"))
            parameters["NOTE"]
        else ""

        contactUrl = if (parameters.containsKey("URL"))
            parameters["URL"]
        else ""
    }

    fun setMeCardDescription(text: String?): String {
        return text?.replace("MECARD:", "")
            ?.replace("N:", "")
            ?.replace("ORG:", "")
            ?.replace("ADR:", "")
            ?.replace("EMAIL:", "")
            ?.replace("TEL:", "")
            ?.replace("NICKNAME:", "")
            ?.replace("BDAY:", "")
            ?.replace("No:", "")
            ?.replace("NOTE:", "")
            ?.replace(";;", "")
            ?.replace(";", "\n") ?: ""
    }

    fun setupEMailDescription(text: String?): String {
        return text?.replace("mailto:", "")
            ?.replace("MAILTO:", "")
            ?.replace("?subject=", "\n")
            ?.replace("&subject=", "\n")
            ?.replace("?body=", "\n")
            ?.replace("&body=", "\n") ?: ""
    }

    private fun copyToClipboard(text: String?) {
        val clipboard = ContextCompat.getSystemService(
            requireContext(),
            ClipboardManager::class.java
        )
        val clip = ClipData.newPlainText("Copy", text)
        clipboard?.setPrimaryClip(clip)
    }

    private fun share(text: String?) {
        if (requireContext().isNetworkConnected()) {
            if (SystemClock.elapsedRealtime() - sharedLastClickTime < 1000) {
                return
            }
            sharedLastClickTime = SystemClock.elapsedRealtime()

            intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            ContextCompat.startActivity(
                requireContext(),
                Intent.createChooser(intent, "Seçiniz"),
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
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
    }

    private fun copyOnClick() {
        ll_copy_web_url.setOnClickListener {
            copyToClipboard(text)
        }
        ll_copy_document.setOnClickListener {
            copyToClipboard(text)
        }
        ll_copy_contact.setOnClickListener {
            when {
                text?.contains("VCARD") == true -> {
                    copyToClipboard(setupVCardDescription(text))
                }
                text?.contains("MECARD") == true -> {
                    copyToClipboard(setMeCardDescription(text))
                }
                else -> share(text)
            }
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
        tv_copy_password.setOnClickListener {
            copyToClipboard(wifiPassword)
        }
    }

    private fun shareOnClick() {
        ll_share_web_url.setOnClickListener {
            share(text)
        }
        ll_share_document.setOnClickListener {
            share(text)
        }
        ll_share_contact.setOnClickListener {
            when {
                text?.contains("VCARD") == true -> {
                    share(setupVCardDescription(text))
                }
                text?.contains("MECARD") == true -> {
                    share(setMeCardDescription(text))
                }
                else -> share(text)
            }
        }
        ll_share_email.setOnClickListener {
            share(setupEMailDescription(text))
        }
        ll_share_sms.setOnClickListener {
            share(setupSmsDescription(text))
        }
        ll_share_location.setOnClickListener {
            share(setupLocationDescription(text))
        }
        ll_share_phone.setOnClickListener {
            share(text?.replace("tel:", ""))
        }
        ll_share_wifi.setOnClickListener {
            share(setupWifiDescription(text))
        }
    }

    private fun vibrate() {
        val vibrator =
            requireActivity().applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(200)
        }
    }

    private fun soundBeep() {
        val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
    }

    private fun addContactOnClick() {
        ll_add_contact.setOnClickListener {
            intent = Intent(ContactsContract.Intents.Insert.ACTION)
            intent.type = ContactsContract.RawContacts.CONTENT_TYPE
            intent.putExtra(Contacts.Intents.Insert.EMAIL, contactEMail)
            intent.putExtra(Contacts.Intents.Insert.PHONE, contactTelephone)
            intent.putExtra(Contacts.Intents.Insert.NAME, contactName)
            intent.putExtra(Contacts.Intents.Insert.NOTES, contactNotes)
            intent.putExtra(Contacts.Intents.Insert.COMPANY, contactCompany)

            startActivity(intent)
        }
    }

    private fun addPhoneContactOnClick() {
        ll_phone_add_contact.setOnClickListener {
            val phone = text?.replace("tel:", "")
                ?.replace("TEL:", "")
            intent = Intent(ContactsContract.Intents.Insert.ACTION)
            intent.type = ContactsContract.RawContacts.CONTENT_TYPE
            intent.putExtra(Contacts.Intents.Insert.PHONE, phone)

            startActivity(intent)
        }
    }
}