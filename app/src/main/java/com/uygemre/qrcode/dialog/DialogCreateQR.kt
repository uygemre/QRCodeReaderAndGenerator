package com.uygemre.qrcode.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.text.Html
import android.text.Spanned
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uygemre.qrcode.R
import com.uygemre.qrcode.constants.PrefConstants
import com.uygemre.qrcode.constants.PrefConstants.PREF_INSTAGRAM
import com.uygemre.qrcode.constants.PrefConstants.PREF_LINKEDIN
import com.uygemre.qrcode.constants.PrefConstants.PREF_YOUTUBE
import com.uygemre.qrcode.constants.PrefConstants.PREF_YOUTUBE_VIDEO_ID
import com.uygemre.qrcode.enums.IntentBundleKeyEnum
import com.uygemre.qrcode.extensions.DateExtensions
import com.uygemre.qrcode.extensions.isNetworkConnected
import com.uygemre.qrcode.extensions.showNoInternetDialog
import kotlinx.android.synthetic.main.layout_dialog_create_qr.*
import kotlinx.android.synthetic.main.layout_dialog_create_qr.adView
import net.glxn.qrgen.android.QRCode
import net.glxn.qrgen.core.scheme.*
import java.io.*
import androidx.core.content.FileProvider
import com.uygemre.qrcode.helpers.LocalPrefManager

class DialogCreateQR : BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme
    private lateinit var localPrefManager: LocalPrefManager
    private var sharedLastClickTime = 0L
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

        localPrefManager = LocalPrefManager(requireContext())
        MobileAds.initialize(requireContext())
        if (!localPrefManager.isPremium())
            adView.loadAd(AdRequest.Builder().build())
        setupView()
        saveQRCode()
        btn_share.setOnClickListener {
            shareQR(img_scan_create_qr)
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
        when (contentView) {
            IntentBundleKeyEnum.DETAIL_WEB_URL.toString() -> {
                tv_format.text = setupFormatText(resources.getString(R.string.webUrl))
                val webUrl = arguments?.getString(PrefConstants.PREF_WEB_URL)
                img_scan_create_qr.setImageBitmap(QRCode.from(webUrl).withSize(500, 500).bitmap())
            }
            IntentBundleKeyEnum.DETAIL_DOCUMENT.toString() -> {
                tv_format.text = setupFormatText(resources.getString(R.string.document))
                val text = arguments?.getString(PrefConstants.PREF_DOCUMENT)
                img_scan_create_qr.setImageBitmap(QRCode.from(text).withSize(500, 500).bitmap())
            }
            IntentBundleKeyEnum.DETAIL_EMAIL.toString() -> {
                tv_format.text = setupFormatText(resources.getString(R.string.email))
                val email = arguments?.getString(PrefConstants.PREF_EMAIL)
                val subject = arguments?.getString(PrefConstants.PREF_EMAIL_SUBJECT)
                val message = arguments?.getString(PrefConstants.PREF_EMAIL_MESSAGE)
                val text = "mailto:${email}?subject=${subject}&body=${message}"
                img_scan_create_qr.setImageBitmap(
                    QRCode.from(text).withSize(500, 500).bitmap()
                )
            }
            IntentBundleKeyEnum.DETAIL_CONTACT.toString() -> {
                tv_format.text = setupFormatText(resources.getString(R.string.contact))
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
                tv_format.text = setupFormatText(resources.getString(R.string.sms))
                val subject = arguments?.getString(PrefConstants.PREF_SMS_SUBJECT)
                val number = arguments?.getString(PrefConstants.PREF_SMS_PHONE)
                val text = "SMSTO:${number}:${subject}"
                img_scan_create_qr.setImageBitmap(
                    QRCode.from(text).withSize(500, 500).bitmap()
                )
            }
            IntentBundleKeyEnum.DETAIL_LOCATION.toString() -> {
                tv_format.text = setupFormatText(resources.getString(R.string.location))
                val latitude = arguments?.getString(PrefConstants.PREF_LOCATION_LATITUDE)
                val longitude = arguments?.getString(PrefConstants.PREF_LOCATION_LONGITUDE)
                img_scan_create_qr.setImageBitmap(
                    QRCode.from("geo:${latitude?.toFloatOrNull()},${longitude?.toFloatOrNull()}")
                        .withSize(500, 500).bitmap()
                )
            }
            IntentBundleKeyEnum.DETAIL_PHONE.toString() -> {
                tv_format.text = setupFormatText(resources.getString(R.string.phone))
                val phone = arguments?.getString(PrefConstants.PREF_PHONE)
                val telephone = Telephone()
                telephone.telephone = phone
                img_scan_create_qr.setImageBitmap(
                    QRCode.from(telephone).withSize(500, 500).bitmap()
                )
            }
            IntentBundleKeyEnum.DETAIL_WIFI.toString() -> {
                tv_format.text = setupFormatText(resources.getString(R.string.wifi))
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
            IntentBundleKeyEnum.DETAIL_TWITTER.toString() -> {
                tv_format.text = setupFormatText(resources.getString(R.string.twitter))
                val twitter = arguments?.getString(PrefConstants.PREF_TWITTER)
                val isProfile = arguments?.getBoolean(PrefConstants.PREF_IS_PROFILE)
                if (isProfile == true)
                    img_scan_create_qr.setImageBitmap(
                        QRCode.from("https://www.twitter.com/$twitter").withSize(500, 500).bitmap()
                    )
                else
                    img_scan_create_qr.setImageBitmap(
                        QRCode.from("https://www.twitter.com/intent/tweet?text=$twitter")
                            .withSize(500, 500).bitmap()
                    )
            }
            IntentBundleKeyEnum.DETAIL_WHATSAPP.toString() -> {
                tv_format.text = setupFormatText(resources.getString(R.string.whatsapp))
                val phone = arguments?.getString(PrefConstants.PREF_WHATSAPP_PHONE)
                val message = arguments?.getString(PrefConstants.PREF_WHATSAPP_MESSAGE)

                img_scan_create_qr.setImageBitmap(
                    QRCode.from("https://wa.me/$phone/?text=$message")
                        .withSize(500, 500).bitmap()
                )
            }
            IntentBundleKeyEnum.DETAIL_LINKEDIN.toString() -> {
                tv_format.text = setupFormatText(resources.getString(R.string.linkedin))
                val linkedIn = arguments?.getString(PREF_LINKEDIN)
                if (linkedIn?.startsWith("https://") == true || linkedIn?.startsWith("http://") == true) {
                    img_scan_create_qr.setImageBitmap(
                        QRCode.from(linkedIn).withSize(500, 500).bitmap()
                    )
                } else {
                    img_scan_create_qr.setImageBitmap(
                        QRCode.from("https://linkedin.com/in/$linkedIn/").withSize(500, 500)
                            .bitmap()
                    )
                }
            }
            IntentBundleKeyEnum.DETAIL_YOUTUBE.toString() -> {
                tv_format.text = setupFormatText(resources.getString(R.string.youtube))
                val isVideoId = arguments?.getBoolean(PREF_YOUTUBE_VIDEO_ID)
                val youtube = arguments?.getString(PREF_YOUTUBE)
                if (isVideoId == true) {
                    img_scan_create_qr.setImageBitmap(
                        QRCode.from("https://www.youtube.com/watch?v=$youtube").withSize(500, 500)
                            .bitmap()
                    )
                } else {
                    img_scan_create_qr.setImageBitmap(
                        QRCode.from(youtube).withSize(500, 500).bitmap()
                    )
                }
            }
            IntentBundleKeyEnum.DETAIL_INSTAGRAM.toString() -> {
                tv_format.text = setupFormatText(resources.getString(R.string.instagram))
                val instagram = arguments?.getString(PREF_INSTAGRAM)
                img_scan_create_qr.setImageBitmap(
                    QRCode.from("https://instagram.com/$instagram/").withSize(500, 500).bitmap()
                )
            }
        }
    }

    private fun setupFormatText(text: String): Spanned {
        return Html.fromHtml(
            "<b>" + text + "</b>" + "<br>" + "</br>" +
                    "${DateExtensions.dateDiff8()}, QR_CODE"
        )
    }

    private fun shareQR(image: ImageView?) {
        if (requireContext().isNetworkConnected()) {
            if (SystemClock.elapsedRealtime() - sharedLastClickTime < 1000) {
                return
            }
            sharedLastClickTime = SystemClock.elapsedRealtime()

            val bitmap = image?.drawable?.toBitmap()

            try {
                val cachePath = File(requireContext().cacheDir, "images")
                cachePath.mkdirs()
                val stream = FileOutputStream("$cachePath/image.png")
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.close()

            } catch (e: IOException) {
                e.printStackTrace()
            }

            val imagePath = File(requireContext().cacheDir, "images")
            val newFile = File(imagePath, "image.png")
            val contentUri: Uri? =
                FileProvider.getUriForFile(requireContext(), "com.uygemre.qrcode.fileprovider", newFile)

            if (contentUri != null) {
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                shareIntent.setDataAndType(contentUri, requireContext().contentResolver.getType(contentUri))
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
                startActivity(shareIntent)
            }
        } else {
            requireContext().showNoInternetDialog()
        }
    }

    private fun saveQRCode() {
        btn_save.setOnClickListener {
            if (requireActivity().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Handler().postDelayed({
                    MediaStore.Images.Media.insertImage(
                        requireContext().contentResolver,
                        img_scan_create_qr.drawable.toBitmap(),
                        "MY_QR_CODE",
                        null
                    )
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.save_qr_code_toast),
                        Toast.LENGTH_LONG
                    ).show()
                }, 1000)
            } else {
                requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            100 -> {
                if (grantResults.getOrNull(0) == PackageManager.PERMISSION_GRANTED) {
                    Handler().postDelayed({
                        MediaStore.Images.Media.insertImage(
                            requireContext().contentResolver,
                            img_scan_create_qr.drawable.toBitmap(),
                            "MY_QR_CODE",
                            null
                        )
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.save_qr_code_toast),
                            Toast.LENGTH_LONG
                        ).show()
                    }, 1000)
                }
            }
        }
    }
}