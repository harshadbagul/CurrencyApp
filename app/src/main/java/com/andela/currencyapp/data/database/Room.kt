package com.andela.currencyapp.data.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.RoomDatabase
import com.andela.currencyapp.data.netowork.model.CurrencyResponse

@Dao
interface CurrencyDao {

    //write down dao operation

}

@Database(entities = [CurrencyResponse::class], version = 1, exportSchema = false)
abstract class CurrencyDatabase : RoomDatabase() {
    abstract val currencyDao: CurrencyDao
}