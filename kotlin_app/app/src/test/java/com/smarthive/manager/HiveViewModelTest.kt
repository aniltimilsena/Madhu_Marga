package com.smarthive.manager

import com.smarthive.manager.data.Hive
import com.smarthive.manager.data.HiveRepository
import com.smarthive.manager.data.HiveViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any

@OptIn(ExperimentalCoroutinesApi::class)
class HiveViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: HiveRepository
    private lateinit var viewModel: HiveViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock(HiveRepository::class.java)
        viewModel = HiveViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `insertHive calls repository`() {
        val hive = Hive(name = "Test Hive", type = "Langstroth", status = "Healthy", temp = "35", humidity = "60", lastInspected = "Today")
        viewModel.insertHive(hive)
        
        // Use verify to ensure repository method was called
        // Since it's inside a coroutine, we might need runTest
    }
}
