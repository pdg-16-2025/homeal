package com.example.homeal_app.ui.Fridge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.homeal_app.databinding.FragmentFridgeBinding
import com.example.homeal_app.ui.Calendar.CalendarScreen
import com.example.homeal_app.ui.Calendar.CalendarViewModel

class FridgeFragment : Fragment() {

    private var _binding: FragmentFridgeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fridgeViewModel =
            ViewModelProvider(this).get(FridgeViewModel::class.java)

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    FridgeScreen(viewModel = fridgeViewModel)
                }
            }
        }
    }
}

fun FridgeScreen(viewModel: FridgeViewModel) {

}