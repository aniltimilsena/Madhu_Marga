package com.smarthive.manager.data

import android.app.Application
import com.smarthive.manager.data.Hive
import com.smarthive.manager.data.HiveRepository
import com.smarthive.manager.data.HiveViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class HiveViewModelTest {

    private lateinit var viewModel: HiveViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var repository: HiveRepository

    @Mock
    private lateinit var application: Application

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = HiveViewModel(repository, application)
    }

    @Test
    fun `calculateStatus returns Healthy for ideal temperature`() {
        val status = viewModel.calculateStatus("33.5°C", "")
        assertEquals("Healthy", status)
    }

    @Test
    fun `calculateStatus returns Warning for temperature outside ideal range`() {
        val status = viewModel.calculateStatus("37.5°C", "")
        assertEquals("Warning", status)
    }

    @Test
    fun `calculateStatus returns Alert for critical high temperature`() {
        val status = viewModel.calculateStatus("39.0°C", "")
        assertEquals("Alert", status)
    }

    @Test
    fun `calculateStatus returns Alert for critical low temperature`() {
        val status = viewModel.calculateStatus("25.0°C", "")
        assertEquals("Alert", status)
    }

    @Test
    fun `calculateStatus returns Warning when pest issues are present even with healthy temp`() {
        val status = viewModel.calculateStatus("34.0°C", "Varroa")
        assertEquals("Warning", status)
    }

    @Test
    fun `calculateStatus prioritizes Alert temp over pest Warning`() {
        val status = viewModel.calculateStatus("39.0°C", "Varroa")
        assertEquals("Alert", status)
    }
}
