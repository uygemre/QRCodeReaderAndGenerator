package com.uygemre.qrcode.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.uygemre.qrcode.R
import com.uygemre.qrcode.database.AppDatabase
import com.uygemre.qrcode.database.QRCodeDTO
import com.uygemre.qrcode.dialog.DialogResultScanQR
import com.uygemre.qrcode.extensions.DateExtensions
import com.uygemre.qrcode.extensions.visibile
import kotlinx.android.synthetic.main.fragment_scan_qr.*
import kotlinx.android.synthetic.main.layout_dialog_result_scan_qr.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ScanQRFragment : Fragment() {

    private var codeScanner: CodeScanner? = null
    lateinit var dialogResultScanQR: DialogResultScanQR
    private val dialog = DialogResultScanQR()
    private val bundle = Bundle()
    private var mPermissionGranted = false
    private var appDatabase: AppDatabase? = null
    private var qrFormat: String? = ""
    private var qrDescription: String? = ""
    private var qrImage: Int? = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutInflater.inflate(R.layout.fragment_scan_qr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appDatabase = AppDatabase.getInstance(requireContext().applicationContext)
        dialogResultScanQR = DialogResultScanQR()

        codeScanner = CodeScanner(requireContext(), scanner_view)
        codeScanner?.decodeCallback = DecodeCallback {
            requireActivity().runOnUiThread {
                bundle.putString("text", it.text)
                bundle.putString("barcodeFormat", it.barcodeFormat.toString())
                dialog.arguments = bundle
                dialog.show(childFragmentManager, "dialog")
            }
            setQrFormat(it.text)
            GlobalScope.launch {
                appDatabase?.qrCodeDao()?.insert(QRCodeDTO(
                    id = 0,
                    description = qrDescription,
                    date = DateExtensions.dateDiff8(),
                    format = qrFormat,
                    image = qrImage,
                    text = it.text,
                    barcodeFormat = it.barcodeFormat.toString()
                ))
            }
        }
        checkPermission()
        scanner_view?.setOnClickListener {
            if (mPermissionGranted) {
                codeScanner?.startPreview()
            } else {
                checkPermission()
            }
        }
    }

    private fun setQrFormat(text: String) {
        when {
            text.startsWith("https://") || text.startsWith("http://") -> {
                qrFormat = resources.getString(R.string.webUrl)
                qrImage = R.drawable.ic_web_url
                qrDescription = text
            }
            text.startsWith("MECARD") || text.startsWith("BEGIN:VCARD") -> {
                qrFormat = resources.getString(R.string.contact)
                qrImage = R.drawable.contact
                qrDescription = when {
                    text.startsWith("MECARD") -> {
                        dialogResultScanQR.setMeCardDescription(text)
                    }
                    text.startsWith("BEGIN:VCARD") -> {
                        dialogResultScanQR.setupVCardDescription(text)
                    }
                    else -> text
                }
            }
            text.startsWith("mailto") || text.startsWith("MAILTO") -> {
                qrFormat = resources.getString(R.string.email)
                qrImage = R.drawable.email
                qrDescription = dialogResultScanQR.setupEMailDescription(text)
            }
            text.startsWith("SMSTO:") || text.startsWith("sms:") -> {
                qrFormat = resources.getString(R.string.sms)
                qrImage = R.drawable.ic_sms
                qrDescription = dialogResultScanQR.setupSmsDescription(text)
            }
            text.startsWith("geo") || text.startsWith("GEO") -> {
                qrFormat = resources.getString(R.string.location)
                qrImage = R.drawable.ic_location
                qrDescription = dialogResultScanQR.setupLocationDescription(text)
            }
            text.startsWith("tel:") -> {
                qrFormat = resources.getString(R.string.phone)
                qrImage = R.drawable.ic_phone
                qrDescription = text.replace("tel:", "")
            }
            text.startsWith("WIFI") -> {
                qrFormat = resources.getString(R.string.wifi)
                qrImage = R.drawable.ic_wifi
                qrDescription = dialogResultScanQR.setupWifiDescription(text)
            }
            else -> {
                qrFormat = resources.getString(R.string.document)
                qrImage = R.drawable.ic_document
                qrDescription = text
            }
        }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED -> {
                    mPermissionGranted = false
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), 100)
                }
                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
                }
                else -> {
                    mPermissionGranted = true
                }
            }
        } else {
            mPermissionGranted = true
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
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPermissionGranted = true
                    codeScanner?.startPreview()
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA))
                        showDialog()
                    else
                        showSettingsDialog()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (mPermissionGranted) {
            codeScanner?.startPreview()
        }
    }

    override fun onPause() {
        codeScanner?.releaseResources()
        super.onPause()
    }

    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(requireContext())
            .setMessage("Lütfen ayarlardan kamera izni veriniz.")
            .setCancelable(false)
            .setNegativeButton(
                "Cancel"
            ) { dialog, _ ->
                mPermissionGranted = false
                dialog?.cancel()
            }
            .setPositiveButton(
                "Settings"
            ) { _, _ ->
                val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", context?.packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        builder.show()
    }

    private fun showDialog() {
        val builder = AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setMessage("Kamera izni vermeniz gerekmektedir.")
            .setNegativeButton(
                "Cancel"
            ) { dialog, _ ->
                mPermissionGranted = false
                dialog?.cancel()
            }
            .setPositiveButton(
                "Accept"
            ) { _, _ ->
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 100)
            }
        builder.show()
    }
}