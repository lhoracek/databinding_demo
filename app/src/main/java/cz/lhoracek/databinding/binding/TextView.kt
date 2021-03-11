package cz.lhoracek.databinding.binding

import android.text.TextUtils
import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("fading")
fun TextView.setFading(fading: Boolean = false) {
    maxLines = 5
    ellipsize = TextUtils.TruncateAt.END
    isVerticalFadingEdgeEnabled = true
    this.isSelected = true
}