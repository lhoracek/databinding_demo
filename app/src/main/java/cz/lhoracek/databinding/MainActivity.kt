package cz.lhoracek.databinding


import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cz.lhoracek.databinding.binding.WithIdCallback
import cz.lhoracek.databinding.databinding.ActivityMainBinding
import cz.lhoracek.databinding.model.Odd
import cz.lhoracek.databinding.model.Opportunity
import cz.quanti.quase.loremkotlinum.Lorem
import me.tatarka.bindingcollectionadapter2.ItemBinding
import me.tatarka.bindingcollectionadapter2.collections.AsyncDiffObservableList
import timber.log.Timber
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var viewModel: ActivityViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        viewModel.menuClick.invoke(item.itemId)
        return true
    }
}

class ActivityViewModel : ViewModel() {
    private val oddHandler: (String) -> Unit = { Timber.d("Click on ODD: $it") }
    private val deleteHandler: (String) -> Unit = { items.update(items.toMutableList().also { list -> list.removeIf { item -> item.id == it } }) }
    val items = AsyncDiffObservableList(WithIdCallback<Opportunity>())
    val itemBinding = ItemBinding.of<Opportunity> { itemBinding, position, item ->
        itemBinding.set(BR.item, if (position == 0) R.layout.item_row_special else R.layout.item_row)
        itemBinding.bindExtra(BR.oddHandler, oddHandler)
        itemBinding.bindExtra(BR.deleteHandler, deleteHandler)
    }

    val menuClick: (Int) -> Unit = {
        when (it) {
            R.id.add_item -> items.update(
                items.toMutableList().also { it.add(Math.min(1, items.size), Opportunity(
                            Lorem.words(3),
                            UUID.randomUUID().toString(),
                            (1..(2..6).random()).map { Odd(it / 2.0f, UUID.randomUUID().toString()) }.toList()
                        ))})
        }
    }
}