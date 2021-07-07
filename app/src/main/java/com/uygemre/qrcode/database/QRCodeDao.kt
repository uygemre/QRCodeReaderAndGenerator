package com.uygemre.qrcode.database

import androidx.room.*

@Dao
interface QRCodeDao : BaseDao<QRCodeDTO> {

    @Query("SELECT * FROM qrcodedatabase")
    fun getAll(): List<QRCodeDTO>

    @Query("DELETE FROM qrcodedatabase")
    fun deleteAll()

}