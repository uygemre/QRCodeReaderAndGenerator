package com.uygemre.qrcode.recyclerview

import com.uygemre.qrcode.database.QRCodeDTO

interface OnItemClickListener {
    fun deleteItemOnClicked(qrCodeDTO: QRCodeDTO, position: Int)
    fun goDetailItemOnClicked(qrCodeDTO: QRCodeDTO)
    //fun onItemClicked(qrCodeDTO: QRCodeDTO)
}