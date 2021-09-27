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
import android.text.Spanned
import androidx.annotation.RequiresApi
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.uygemre.qrcode.database.AppDatabase
import com.uygemre.qrcode.database.QRCodeDTO
import com.uygemre.qrcode.extensions.*
import com.uygemre.qrcode.fragments.ScanQRFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DialogResultScanQR : BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme
    private lateinit var localPrefManager: LocalPrefManager
    private var sharedLastClickTime = 0L
    private var appDatabase: AppDatabase? = null

    private var qrFormat: String? = ""
    private var qrDescription: String? = ""
    private var qrImage: Int? = 0

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
    private var wifiEncryption: String? = ""
    private var wifiIsHidden: String? = ""

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
        if (!localPrefManager.isPremium())
            adView.loadAd(AdRequest.Builder().build())
        appDatabase = AppDatabase.getInstance(requireContext().applicationContext)

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

    private fun setQrFormat(text: String) {
        when {
            text.startsWith("https://") || text.startsWith("http://") -> {
                qrFormat = resources.getString(R.string.webUrl)
                qrImage = R.drawable.ic_web_url_white
                qrDescription = text
            }
            text.startsWith("MECARD") -> {
                qrFormat = resources.getString(R.string.contact)
                qrImage = R.drawable.ic_contact_black
                qrDescription = setMeCardDescription(text)
            }
            text.startsWith("BEGIN:VCARD") -> {
                qrFormat = resources.getString(R.string.contact)
                qrImage = R.drawable.ic_contact_black
                qrDescription = setupVCardDescription(text)
            }
            text.startsWith("mailto") || text.startsWith("MAILTO") -> {
                qrFormat = resources.getString(R.string.email)
                qrImage = R.drawable.ic_mail_white
                qrDescription = setupEMailDescription(text)
            }
            text.startsWith("MATMSG") -> {
                qrFormat = getString(R.string.email)
                qrImage = R.drawable.ic_mail_white
                qrDescription = setupEmailWithSubjectAndMessage(text)
            }
            text.startsWith("SMSTO:") || text.startsWith("sms:") -> {
                qrFormat = resources.getString(R.string.sms)
                qrImage = R.drawable.ic_sms_new
                qrDescription = setupSmsDescription(text)
            }
            text.startsWith("geo") || text.startsWith("GEO") -> {
                qrFormat = resources.getString(R.string.location)
                qrImage = R.drawable.ic_location_new
                qrDescription = setupLocationDescription(text)
            }
            text.startsWith("tel:") -> {
                qrFormat = resources.getString(R.string.phone)
                qrImage = R.drawable.ic_phone_new
                qrDescription = text.replace("tel:", "")
            }
            text.startsWith("WIFI") -> {
                qrFormat = resources.getString(R.string.wifi)
                qrImage = R.drawable.ic_wifi_new
                qrDescription = setupWifiDescription(text)
            }
            else -> {
                qrFormat = resources.getString(R.string.document)
                qrImage = R.drawable.ic_document_white
                qrDescription = text
            }
        }
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
        text?.let { setQrFormat(it) }
        GlobalScope.launch {
            appDatabase?.qrCodeDao()?.insert(
                QRCodeDTO(
                    id = 0,
                    description = qrDescription,
                    date = DateExtensions.dateDiff8(),
                    format = qrFormat,
                    image = qrImage,
                    text = text,
                    barcodeFormat = barcodeFormat.toString()
                )
            )
        }

        val isOpenBrowserAuto = localPrefManager.pull(
            PrefConstants.PREF_IS_OPEN_BROWSER_AUTO,
            false
        )
        val isAutoCopyClipboard = localPrefManager.pull(
            PrefConstants.PREF_AUTO_COPY_CLIPBOARD,
            false
        )
        val isAutoSearchOnWeb = localPrefManager.pull(PrefConstants.PREF_AUTO_SEARCH_ON_WEB, false)
        img_scan_qr.setImageBitmap(QRCode.from(text).withSize(600, 600).bitmap())

        when {
            text?.startsWith("https://") == true || text?.startsWith("http://") == true -> {
                tv_result.text = Html.fromHtml(
                    "<b>" + resources.getString(R.string.result_web_url) + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, $barcodeFormat"
                )

                layout_web_url.visible()
                tv_description.text = text
                if (isOpenBrowserAuto) {
                    intent = Intent(Intent.ACTION_VIEW, Uri.parse(text))
                    startActivity(intent)
                }
            }
            text?.startsWith("MECARD") == true -> {
                tv_result.text = Html.fromHtml(
                    "<b>" + resources.getString(R.string.result_contact) + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, $barcodeFormat"
                )
                tv_description.text = Html.fromHtml(setMeCardDescription(text))
                layout_contact.visible()
            }
            text?.startsWith("BEGIN:VCARD") == true -> {
                tv_result.text = Html.fromHtml(
                    "<b>" + resources.getString(R.string.result_contact) + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, $barcodeFormat"
                )
                tv_description.text = Html.fromHtml(setupVCardDescription(text))
                layout_contact.visible()
            }
            text?.startsWith("mailto") == true || text?.startsWith("MAILTO") == true -> {
                tv_result.text = Html.fromHtml(
                    "<b>" + resources.getString(R.string.result_e_mail) + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, $barcodeFormat"
                )
                tv_description.text = Html.fromHtml(setupEMailDescription(text))
                layout_email.visible()
            }
            text?.startsWith("MATMSG") == true -> {
                tv_result.text = Html.fromHtml(
                    "<b>" + resources.getString(R.string.result_e_mail) + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, $barcodeFormat"
                )
                tv_description.text = Html.fromHtml(setupEmailWithSubjectAndMessage(text))
                layout_email.visible()
            }
            text?.startsWith("SMSTO:") == true || text?.startsWith("sms:") == true -> {
                tv_result.text = Html.fromHtml(
                    "<b>" + resources.getString(R.string.result_sms) + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, $barcodeFormat"
                )
                tv_description.text = Html.fromHtml(setupSmsDescription(text))
                layout_sms.visible()
            }
            text?.startsWith("geo") == true || text?.startsWith("GEO") == true -> {
                tv_result.text = Html.fromHtml(
                    "<b>" + resources.getString(R.string.result_location) + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, $barcodeFormat"
                )
                tv_description.text = Html.fromHtml(setupLocationDescription(text))
                layout_location.visible()
            }
            text?.startsWith("tel:") == true -> {
                tv_result.text = Html.fromHtml(
                    "<b>" + resources.getString(R.string.result_phone) + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, $barcodeFormat"
                )
                layout_phone.visible()
                phone = text?.replace("tel:", "")
                tv_description.text = phone
            }
            text?.startsWith("WIFI") == true -> {
                tv_result.text = Html.fromHtml(
                    "<b>" + resources.getString(R.string.result_wifi) + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, $barcodeFormat"
                )
                tv_description.text = Html.fromHtml(setupWifiDescription(text))
                layout_wifi.visible()
            }
            else -> {
                tv_result.text = Html.fromHtml(
                    "<b>" + resources.getString(R.string.result_document) + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, $barcodeFormat"
                )
                tv_description.text = text
                layout_document.visible()
                if (isAutoSearchOnWeb) {
                    intent = Intent(Intent.ACTION_WEB_SEARCH)
                    intent.putExtra(SearchManager.QUERY, text)
                    startActivity(intent)
                }
            }
        }

        if (isAutoCopyClipboard) {
            copyToClipboard(text)
            Toast.makeText(
                requireContext(),
                "${getString(R.string.copied_clipboard)}\n$text",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    private fun setupVCardDescription(text: String?): String {
        text?.let { _text -> separateVCard(_text) }
        return (if (contactName?.isNotEmpty() == true) boldString(getString(R.string.name), contactName) else "") +
                (if (contactCompany?.isNotEmpty() == true) boldString(getString(R.string.company), contactCompany) else "") +
                (if (contactTelephone?.isNotEmpty() == true) boldString(getString(R.string.telephone), contactTelephone) else "") +
                (if (contactEMail?.isNotEmpty() == true) boldString(getString(R.string.email), contactEMail) else "") +
                (if (contactAddress?.isNotEmpty() == true) boldString(getString(R.string.address), contactAddress) else "") +
                (if (contactNotes?.isNotEmpty() == true) boldString(getString(R.string.notes), contactNotes) else "")
    }

    private fun boldString(first: String, second: String?): String {
        return "<b>$first: </b>$second<br></br>"
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

    private fun separateWifiWithParameters(text: String?) {
        val parameters: Map<String, String> =
            getParameters(text?.replaceFirst("WIFI:", "").toString(), ";", ":")

        wifiSsid = if (parameters.containsKey("S"))
            parameters["S"]
        else ""

        wifiPassword = if (parameters.containsKey("P"))
            parameters["P"]
        else ""

        wifiEncryption = if (parameters.containsKey("T"))
            parameters["T"]
        else ""

        wifiIsHidden = if (parameters.containsKey("H"))
            parameters["H"]
        else ""
    }

    private fun separateEmailWithParameters(text: String?) {
        val parameters: Map<String, String> =
            getParameters(text?.replaceFirst("MATMSG:", "").toString(), ";", ":")

        sendEmail = if (parameters.containsKey("TO"))
            parameters["TO"]
        else ""

        sendEmailSubject = if (parameters.containsKey("SUB"))
            parameters["SUB"]
        else ""

        sendEmailMessage = if (parameters.containsKey("BODY"))
            parameters["BODY"]
        else ""
    }

    private fun setupLocationDescription(text: String?): String {
        text?.let { _text -> separateLocation(_text) }
        return (if (latitude?.isNotEmpty() == true) boldString(getString(R.string.latitude), latitude) else "") +
                (if (longitude?.isNotEmpty() == true) boldString(getString(R.string.longitude), longitude) else "")
    }

    private fun setupWifiDescription(text: String?): String {
        separateWifiWithParameters(text)
        return (if (wifiSsid?.isNotEmpty() == true) boldString(getString(R.string.ssid_network_name), smsPhone) else "") +
                (if (wifiPassword?.isNotEmpty() == true) boldString(getString(R.string.password), smsPhone) else "") +
                (if (wifiEncryption?.isNotEmpty() == true) boldString(getString(R.string.encryption), smsPhone) else "") +
                (if (wifiIsHidden?.isNotEmpty() == true) boldString(getString(R.string.hidden), smsPhone) else "")
    }

    private fun setupSmsDescription(text: String?): String {
        separateSms(text)
        return (if (smsPhone?.isNotEmpty() == true) boldString(getString(R.string.phone), smsPhone) else "") +
                (if (smsMessage?.isNotEmpty() == true) boldString(getString(R.string.message), smsMessage) else "")
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

    private fun setMeCardDescription(text: String?): String {
        text?.let { separateMeCard(it) }
        return (if (contactName?.isNotEmpty() == true) boldString(getString(R.string.name), contactName) else "") +
                (if (contactTelephone?.isNotEmpty() == true) boldString(getString(R.string.telephone), contactTelephone) else "") +
                (if (contactAddress?.isNotEmpty() == true) boldString(getString(R.string.address), contactAddress) else "") +
                (if (contactUrl?.isNotEmpty() == true) boldString(getString(R.string.webUrl), contactUrl) else "") +
                (if (contactCompany?.isNotEmpty() == true) boldString(getString(R.string.company), contactCompany) else "") +
                (if (contactNotes?.isNotEmpty() == true) boldString(getString(R.string.notes), contactNotes) else "") +
                (if (contactEMail?.isNotEmpty() == true) boldString(getString(R.string.email), contactEMail) else "")
    }

    private fun setupEMailDescription(text: String?): String {
        separateEMail(text)
        return (if (sendEmail?.isNotEmpty() == true) boldString(getString(R.string.email), sendEmail) else "") +
                (if (sendEmailSubject?.isNotEmpty() == true) boldString(getString(R.string.subject), sendEmailSubject) else "") +
                (if (sendEmailMessage?.isNotEmpty() == true) boldString(getString(R.string.message), sendEmailMessage) else "")
    }

    private fun setupEmailWithSubjectAndMessage(text: String?): String {
        separateEmailWithParameters(text)
        return (if (sendEmail?.isNotEmpty() == true) boldString(getString(R.string.email), sendEmail) else "") +
                (if (sendEmailSubject?.isNotEmpty() == true) boldString(getString(R.string.subject), sendEmailSubject) else "") +
                (if (sendEmailMessage?.isNotEmpty() == true) boldString(getString(R.string.message), sendEmailMessage) else "")
    }

    private fun copyToClipboard(text: String?) {
        val clipboard = ContextCompat.getSystemService(
            requireContext(),
            ClipboardManager::class.java
        )
        val clip = ClipData.newPlainText("Copy", text)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(requireContext(), getString(R.string.copied_clipboard), Toast.LENGTH_SHORT)
            .show()
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
                Intent.createChooser(intent, getString(R.string.select)),
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