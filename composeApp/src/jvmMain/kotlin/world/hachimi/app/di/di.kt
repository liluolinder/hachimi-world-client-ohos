package world.hachimi.app.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import world.hachimi.app.BuildKonfig
import world.hachimi.app.api.ApiClient
import world.hachimi.app.model.MainViewModel

val appModule = module {
    single {
        ApiClient(BuildKonfig.API_BASE_URL)
    }
    viewModelOf(::MainViewModel)
}