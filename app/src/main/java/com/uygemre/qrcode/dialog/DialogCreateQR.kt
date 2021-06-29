package com.uygemre.qrcode.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.text.Html
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.MailTo
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uygemre.qrcode.R
import com.uygemre.qrcode.constants.PrefConstants
import com.uygemre.qrcode.enums.IntentBundleKeyEnum
import com.uygemre.qrcode.extensions.DateExtensions
import com.uygemre.qrcode.extensions.isNetworkConnected
import com.uygemre.qrcode.extensions.showNoInternetDialog
import kotlinx.android.synthetic.main.layout_dialog_create_qr.*
import net.glxn.qrgen.android.QRCode
import net.glxn.qrgen.core.scheme.*
import java.io.ByteArrayOutputStream

class DialogCreateQR : BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme
    var sharedLastClickTime = 0L
    private var contentView: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_dialog_create_qr, container, false)
    }

    @SuppressLint("ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments.let {
            contentView = it?.getString(PrefConstants.PREF_CONTENT_VIEW)
        }

        setupView()
        saveQRCode()
        btn_share.setOnClickListener {
            shareDate(img_scan_create_qr)
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
        val qrgEncoder: QRGEncoder?
        when (contentView) {
            IntentBundleKeyEnum.DETAIL_WEB_URL.toString() -> {
                tv_format.text = Html.fromHtml(
                    "<b>" + "WEB URL" + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, QR_CODE"
                )
                val webUrl = arguments?.getString(PrefConstants.PREF_WEB_URL)
                img_scan_create_qr.setImageBitmap(QRCode.from(webUrl).withSize(500, 500).bitmap())
            }
            IntentBundleKeyEnum.DETAIL_DOCUMENT.toString() -> {
                tv_format.text = Html.fromHtml(
                    "<b>" + "DOCUMENT" + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, QR_CODE"
                )
                val text = arguments?.getString(PrefConstants.PREF_DOCUMENT)
                img_scan_create_qr.setImageBitmap(QRCode.from(text).withSize(500, 500).bitmap())
            }
            IntentBundleKeyEnum.DETAIL_EMAIL.toString() -> {
                tv_format.text = Html.fromHtml(
                    "<b>" + "E-MAIL" + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, QR_CODE"
                )
                val email = arguments?.getString(PrefConstants.PREF_EMAIL)
                val subject = arguments?.getString(PrefConstants.PREF_EMAIL_SUBJECT)
                val message = arguments?.getString(PrefConstants.PREF_EMAIL_MESSAGE)
                val text = "mailto:${email}?subject=${subject}&body=${message}"
                img_scan_create_qr.setImageBitmap(
                    QRCode.from(text).withSize(500, 500).bitmap()
                )
            }
            IntentBundleKeyEnum.DETAIL_CONTACT.toString() -> {
                tv_format.text = Html.fromHtml(
                    "<b>" + "CONTACTL" + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, QR_CODE"
                )
                val name = arguments?.getString(PrefConstants.PREF_CONTACT_NAME)
                val phone = arguments?.getString(PrefConstants.PREF_CONTACT_TELEPHONE)
                val email = arguments?.getString(PrefConstants.PREF_CONTACT_EMAIL)
                val address = arguments?.getString(PrefConstants.PREF_CONTACT_ADDRESS)
                val company = arguments?.getString(PrefConstants.PREF_CONTACT_COMPANY)
                val note = arguments?.getString(PrefConstants.PREF_CONTACT_NOTES)
                val vCard = VCard()
                    .setEmail(email)
                    .setTitle(name)
                    .setPhoneNumber(phone)
                    .setCompany(company)
                    .setAddress(address)
                    .setName(name)

                vCard.note = note
                img_scan_create_qr.setImageBitmap(QRCode.from(vCard).withSize(500, 500).bitmap())
            }
            IntentBundleKeyEnum.DETAIL_SMS.toString() -> {
                tv_format.text = Html.fromHtml(
                    "<b>" + "SMS" + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, QR_CODE"
                )
                val subject = arguments?.getString(PrefConstants.PREF_SMS_SUBJECT)
                val number = arguments?.getString(PrefConstants.PREF_SMS_PHONE)
                val text = "SMSTO:${number}:${subject}"
                img_scan_create_qr.setImageBitmap(
                    QRCode.from(text).withSize(500, 500).bitmap()
                )
            }
            IntentBundleKeyEnum.DETAIL_LOCATION.toString() -> {
                tv_format.text = Html.fromHtml(
                    "<b>" + "LOCATION" + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, QR_CODE"
                )
                val latitude = arguments?.getString(PrefConstants.PREF_LOCATION_LATITUDE)
                val longitude = arguments?.getString(PrefConstants.PREF_LOCATION_LONGITUDE)
                val bundle = Bundle()
                latitude?.toFloatOrNull()?.let { bundle.putFloat("LAT", it) }
                longitude?.toFloatOrNull()?.let { bundle.putFloat("LONG", it) }
                qrgEncoder = QRGEncoder(
                    QRGContents.Type.LOCATION,
                    bundle,
                    QRGContents.Type.LOCATION,
                    500
                )
                img_scan_create_qr.setImageBitmap(qrgEncoder.bitmap)
            }
            IntentBundleKeyEnum.DETAIL_PHONE.toString() -> {
                tv_format.text = Html.fromHtml(
                    "<b>" + "PHONE" + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, QR_CODE"
                )
                val phone = arguments?.getString(PrefConstants.PREF_PHONE)
                val telephone = Telephone()
                telephone.telephone = phone
                img_scan_create_qr.setImageBitmap(
                    QRCode.from(telephone).withSize(500, 500).bitmap()
                )
            }
            IntentBundleKeyEnum.DETAIL_WIFI.toString() -> {
                tv_format.text = Html.fromHtml(
                    "<b>" + "Wi-Fi" + "</b>" + "<br>" + "</br>" +
                            "${DateExtensions.dateDiff8()}, QR_CODE"
                )
                val username = arguments?.getString(PrefConstants.PREF_WIFI_NETWORK_NAME)
                val password = arguments?.getString(PrefConstants.PREF_WIFI_PASSWORD)
                val authentication = arguments?.getString(PrefConstants.PREF_WIFI_AUTHENTICATION)
                val isHidden = arguments?.getBoolean(PrefConstants.PREF_WIFI_IS_HIDDEN)
                val wifi = Wifi()
                wifi.ssid = username
                wifi.psk = password
                when (authentication) {
                    "WPA" -> wifi.setAuthentication(Wifi.Authentication.WPA)
                    "WEP" -> wifi.setAuthentication(Wifi.Authentication.WEP)
                    "nopass" -> wifi.setAuthentication(Wifi.Authentication.nopass)
                    else -> wifi.setAuthentication(Wifi.Authentication.WPA)
                }
                wifi.isHidden = isHidden ?: false
                img_scan_create_qr.setImageBitmap(QRCode.from(wifi).withSize(500, 500).bitmap())
            }
        }
    }

    private fun shareDate(image: ImageView?) {
        if (requireContext().isNetworkConnected()) {
            if (SystemClock.elapsedRealtime() - sharedLastClickTime < 1000) {
                return
            }
            sharedLastClickTime = SystemClock.elapsedRealtime()

            val intent = Intent(Intent.ACTION_SEND).setType("image/*")
            val bitmap = image?.drawable?.toBitmap()
            val bytes = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val path = MediaStore.Images.Media.insertImage(
                requireContext().contentResolver,
                bitmap,
                "MY_QR_CODE",
                null
            )
            val uri = Uri.parse(path)
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            startActivity(intent)
        } else {
            requireContext().showNoInternetDialog()
        }
    }

    private fun saveQRCode() {
        btn_save.setOnClickListener {
            MediaStore.Images.Media.insertImage(
                context?.contentResolver,
                img_scan_create_qr.drawable.toBitmap(),
                "MY_QR_CODE",
                "qwerty"
            )
        }
    }
}