package com.andela.currencyapp

import com.andela.currencyapp.data.database.CurrencyDatabase
import com.andela.currencyapp.data.netowork.model.Currency
import com.andela.currencyapp.data.netowork.model.CurrencyResponse
import com.andela.currencyapp.data.netowork.model.ErrorResponse
import com.andela.currencyapp.data.netowork.service.CurrencyService
import com.andela.currencyapp.data.netowork.service.CurrencyState
import com.andela.currencyapp.data.repository.CurrencyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock


@RunWith(JUnit4::class)
class CurrencyTest {

    var currencyDatabase: CurrencyDatabase = mock()
    var service : CurrencyService = mock()
    val coroutineContext = Dispatchers.IO

    lateinit var repository : CurrencyRepository

    @Before
     fun init() {
        repository =  Mockito.spy(CurrencyRepository(service, currencyDatabase))
    }

    @Test
    fun testEmptyResponse() {
        var testEmptyResponse: Flow<CurrencyState>?

        runBlocking {

            `when`(repository.getAllCurrencies()).doReturn(emptyFlow())
             testEmptyResponse = repository.getAllCurrencies()

        }
        Assert.assertEquals(emptyFlow<Currency>(), testEmptyResponse)
    }


    @Test
    fun testCurrencyApi() {
        val response = runBlocking { repository.getAllCurrencies() }

        var state : CurrencyState? = null
        val s =  response.map {
            state = it
            it
        }.flowOn(coroutineContext)

        Assert.assertNull(state)
    }

    @Test
    fun testLatestApiNull() {
        val response = runBlocking { repository.getLatestRates("USD") }
        Assert.assertNotNull(response)
    }

    @Test
    fun testHistoricApiNotNull() {
        var result: CurrencyResponse? = null

        runBlocking {
            `when`(repository.getHistoricData("USD", arrayListOf())).doReturn(getDummySuccessFlow())

            val response = repository.getHistoricData("USD", arrayListOf())

            response.collect{
                result = (it as CurrencyState.Success).response
            }

            Assert.assertNotNull(response)
        }
    }


    private fun getDummySuccessFlow(): Flow<CurrencyState> {
        val flow = flow<CurrencyState> {
            CurrencyState.Success(
                CurrencyResponse(
                    success = true,
                    symbols = mapOf("USD" to "United States", "INR" to "India"),
                    errorResponse = ErrorResponse("", ""),
                    rates = Unit
                )
            )
        }
        return flow
    }


}