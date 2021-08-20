package com.uygemre.qrcode.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qrcodedatabase")
data class QRCodeDTO(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "description") var description: String? = "",
    @ColumnInfo(name = "date") var date: String? = "",
    @ColumnInfo(name = "format") var format: String? = "",
    @ColumnInfo(name = "image") var image: Int? = 0,
    @ColumnInfo(name = "text") var text: String? = "",
    @ColumnInfo(name = "barcodeFormat") var barcodeFormat: String? = ""
)