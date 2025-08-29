package com.example.homeal_app.ui.Shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kotlin.getValue


class ShoppingFragment : Fragment() {

    private val shoppingViewModel: ShoppingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ShoppingScreen(viewModel = shoppingViewModel)
                }
            }
        }
    }
}

@Composable
fun ShoppingScreen(viewModel: ShoppingViewModel) {

}


















