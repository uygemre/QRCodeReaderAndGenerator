package com.uygemre.qrcode.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.uygemre.qrcode.R
import com.uygemre.qrcode.dialog.DialogResultScanQR
import kotlinx.android.synthetic.main.fragment_scan_qr.*

class ScanQRFragment : Fragment() {

    private var codeScanner: CodeScanner? = null
    private val dialog = DialogResultScanQR()
    private val bundle = Bundle()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutInflater.inflate(R.layout.fragment_scan_qr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        codeScanner = CodeScanner(requireContext(), scanner_view)
        codeScanner?.decodeCallback = DecodeCallback {
            requireActivity().runOnUiThread {
                bundle.putString("text", it.text)
                bundle.putString("barcodeFormat", it.barcodeFormat.toString())
                dialog.arguments = bundle
                dialog.show(childFragmentManager, "dialog")
            }
        }

        scanner_view?.setOnClickListener {
            codeScanner?.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner?.startPreview()
    }

    override fun onPause() {
        codeScanner?.releaseResources()
        super.onPause()
    }
}