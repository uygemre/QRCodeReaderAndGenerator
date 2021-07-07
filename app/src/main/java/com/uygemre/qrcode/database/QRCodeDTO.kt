package com.uygemre.qrcode.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

// Created by Emre UYGUN on 7/1/21
// Copyriht © Demiroren Teknoloji. All rights reserved.

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