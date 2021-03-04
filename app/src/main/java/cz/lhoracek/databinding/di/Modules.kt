import cz.lhoracek.databinding.ActivityViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainActivityModule = module {
    viewModel {
        ActivityViewModel()
    }
}