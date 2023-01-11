package com.andela.currencyapp

import androidx.lifecycle.MutableLiveData
import com.andela.currencyapp.data.netowork.model.CurrencyResponse
import com.andela.currencyapp.data.netowork.service.CurrencyService
import com.andela.currencyapp.data.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnit


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @get:Rule
    var mockitoRule = MockitoJUnit.rule()

    @InjectMocks
    var service: CurrencyService? = null

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun getAllSymbols() = runBlocking{
        var response: CurrencyResponse? = null
        try {
            val headers = mapOf("API_KEY" to "Xhjdhkhdwd")
            response = service?.getAllCurrencies(headers)
            assertEquals(response?.success, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}