package cz.lhoracek.bottomsheet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import cz.lhoracek.bottomsheet.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val behavior = BottomSheetBehavior.from(binding.sheet)

        // TODO move to inherited behavior
        behavior.isFitToContents = true
        //behavior.halfExpandedRatio = 0.1f
        behavior.isHideable = true
        behavior.state = STATE_HIDDEN

        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                Log.d("Translate", "State changed to $newState")
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                Log.d("Translate", "Slide update $slideOffset")
                lastOffset = slideOffset
                updatePistions(lastOffset)
            }

        })

        viewModel.displaySheet.observe(this) {
            when (it) {
                1 -> BottomSheetBehavior.from(binding.sheet)
                    .apply {
                        state = if (state != STATE_HIDDEN && state != STATE_COLLAPSED) STATE_HIDDEN else STATE_COLLAPSED
                    }
            }
        }
    }

    var lastOffset = 0f
    fun updatePistions(offset: Float) {
        val behavior = BottomSheetBehavior.from(binding.sheet)
        val sheetHeight = binding.sheet.height
        val peekHeight = binding.below.height + binding.above.height
        if (offset >= 0) {
            val translate = -1 * ((sheetHeight - peekHeight) * (1 - offset))
            Log.d("Translate", "Current offset off: $offset sheetHeight: $sheetHeight peekHeight: $peekHeight trans: $translate")
            behavior.peekHeight = peekHeight
            binding.below.translationY = translate
        } else {
            binding.below.translationY = -1f * (sheetHeight - peekHeight)
        }
    }
}

class ActivityViewModel : ViewModel() {

    val displaySheet = LiveEvent<Int>()

    val coordinatorSheetContentVisible = MutableLiveData(false)

    val buttonClick: (Int) -> Unit = {
        displaySheet.value = it
    }

    val toggleCoordinatorSheetContent =
        { coordinatorSheetContentVisible.apply { value = value?.not() ?: false } }
}