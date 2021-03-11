package cz.lhoracek.databinding


import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.map
import cz.lhoracek.databinding.binding.WithIdCallback
import cz.lhoracek.databinding.databinding.ActivityMainBinding
import cz.lhoracek.databinding.databinding.ItemRowBinding
import cz.lhoracek.databinding.model.Odd
import cz.lhoracek.databinding.model.Opportunity
import cz.lhoracek.databinding.model.WithId
import cz.quanti.quase.loremkotlinum.Lorem
import kotlinx.coroutines.delay
import me.tatarka.bindingcollectionadapter2.ItemBinding
import me.tatarka.bindingcollectionadapter2.collections.AsyncDiffObservableList
import org.koin.androidx.viewmodel.dsl.viewModel
import timber.log.Timber
import java.util.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.dsl.module
import java.lang.IllegalArgumentException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ActivityViewModel by viewModel()

    init {
        lifecycleScope.launchWhenStarted {
            while (true) {
                viewModel.ping()
                delay(200)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

val mainActivityModule = module {
    viewModel {
        ActivityViewModel()
    }
}

class ItemViewModel(item: Opportunity) : WithId {
    val value = MutableLiveData(item)
    override val id: String = item.id
    val odds = MutableLiveData(item.odds)
}

class ActivityViewModel : ViewModel() {
    private val oddHandler: (String) -> Unit = { Timber.d("Click on ODD: $it") }
    private val deleteHandler: (String) -> Unit = { items.update(items.toMutableList().also { list -> list.removeIf { item -> item.id == it } }) }
    val items = AsyncDiffObservableList(WithIdCallback<ItemViewModel>())
    val itemBinding = ItemBinding.of<ItemViewModel> { itemBinding, position, item ->
        itemBinding.set(
            BR.item, when (item) {
                is ItemViewModel -> R.layout.item_row
                else -> throw IllegalArgumentException("Unhabdled")
            }
        )
        itemBinding.bindExtra(BR.oddHandler, oddHandler)
        itemBinding.bindExtra(BR.deleteHandler, deleteHandler)
    }

    val menuClick: (Int) -> Unit = {
        when (it) {
            R.id.add_item -> items.update(
                items.toMutableList().also {
                    it.add(

                        Math.min(1, items.size), ItemViewModel(
                            Opportunity(
                                Lorem.words(3),
                                UUID.randomUUID().toString(),
                                (1..(2..4).random()).map { Odd(it / 4.0f, UUID.randomUUID().toString()) }.toList()
                            )
                        )
                    )
                })
        }
    }

    fun ping() {
        items.forEach { it.odds.value = it.odds.value?.map {  Odd((2..6).random() / 4.0f, it.id) } }
    }
}
