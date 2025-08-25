package com.example.homeal_app.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify

/**
 * Unit tests for HomeViewModel
 */
class HomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var textObserver: Observer<String>

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = HomeViewModel()
    }


    @Test
    fun `text LiveData is not null`() {
        // When
        val textValue = viewModel.text.value

        // Then
        assertThat(textValue).isNotNull()
    }
}
