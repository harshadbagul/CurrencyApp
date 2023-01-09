package com.andela.currencyapp.data.database

import androidx.room.*
import com.andela.currencyapp.data.netowork.model.Currency

@Dao
interface CurrencyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(currencies: List<Currency>)

    @Query("SELECT * FROM Currency")
    suspend fun getAllDbCurrencies(): List<Currency>

    @Update
    suspend fun updateDbCurrencies(currencies: List<Currency>)

}

@Database(entities = [Currency::class], version = 1, exportSchema = false)
abstract class CurrencyDatabase : RoomDatabase() {
    abstract val currencyDao: CurrencyDao
}