package cz.lhoracek.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.lhoracek.bottomsheet.databinding.FragmentBottomSheetBinding

class ExpandingBottomSheetDialogFragment : BottomSheetDialogFragment() {

    val viewModel = ExpandingBottomSheetViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentBottomSheetBinding.inflate(inflater)
        binding.viewModel = viewModel
        return binding.root
    }


}

class ExpandingBottomSheetViewModel : ViewModel() {


}