package cz.lhoracek.bottomsheet

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
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

        initSheet()
        initSheet2()

        viewModel.displaySheet.observe(this) {
            when (it) {
                1 -> from(binding.sheet)
                    .apply {
                        state = if (state != STATE_HIDDEN && state != STATE_EXPANDED) STATE_HIDDEN else STATE_EXPANDED
                    }
                2 -> from(binding.sheet2)
                    .apply {
                        state = if (state != STATE_HIDDEN && state != STATE_EXPANDED) STATE_HIDDEN else STATE_EXPANDED
                        binding.webView.loadUrl("https://www.seznam.cz/")
                    }
            }
        }
        binding.sheet.doOnLayout { updatePositions(0f) }
    }


    private fun initSheet() {
        val behavior = from(binding.sheet)

        behavior.isFitToContents = true
        //behavior.halfExpandedRatio = 0.1f
        behavior.isHideable = true
        behavior.peekHeight = 0

        behavior.state = STATE_HIDDEN

        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                Log.d("Translate", "State changed to $newState")
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                Log.d("Translate", "Slide update $slideOffset")
                lastOffset = slideOffset
                updatePositions(lastOffset)
            }

        })

    }

    private fun initSheet2() {
        val behavior = from(binding.sheet2)

        behavior.isFitToContents = true
        behavior.isHideable = true
        behavior.peekHeight = 0
        behavior.state = STATE_HIDDEN
        //behavior.halfExpandedRatio  = 0f

        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
        }


        behavior.let { behaviour ->
            behaviour.addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {

                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_DRAGGING && binding.webView.scrollY > 0) {
                        // this is where we check if webview can scroll up or not and based on that we let BottomSheet close on scroll down
                        behaviour.setState(BottomSheetBehavior.STATE_EXPANDED)
                    } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        // close()
                    }
                }
            })
        }
    }

    var lastOffset = 0f
    fun updatePositions(offset: Float) {
        val behavior = from(binding.sheet)
        val sheetHeight = binding.sheet.height
//        val peekHeight = binding.below.height + binding.above.height
//        if (offset >= 0) {
//            val translate = -1 * ((sheetHeight - peekHeight) * (1 - offset))
//            Log.d("Translate", "Current offset off: $offset sheetHeight: $sheetHeight peekHeight: $peekHeight trans: $translate")
//            behavior.peekHeight = peekHeight
//            binding.below.translationY = translate
//        } else {
//            binding.below.translationY = -1f * (sheetHeight - peekHeight)
//        }
    }
}

class ActivityViewModel : ViewModel() {

    val displaySheet = LiveEvent<Int>()

    val coordinatorSheetContentVisible = MutableLiveData(false)
    val contentText = MutableLiveData(R.string.lorem)

    val buttonClick: (Int) -> Unit = {
        displaySheet.value = it
    }

    val toggleCoordinatorSheetContent = { coordinatorSheetContentVisible.apply { value = value?.not() ?: false } }
}