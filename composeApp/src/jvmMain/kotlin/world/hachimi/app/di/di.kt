package world.hachimi.app.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toOkioPath
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import world.hachimi.app.BuildKonfig
import world.hachimi.app.JVMPlatform
import world.hachimi.app.api.ApiClient
import world.hachimi.app.model.AuthViewModel
import world.hachimi.app.model.MainViewModel
import world.hachimi.app.storage.MyDataStore

val appModule = module {
    single {
        ApiClient(BuildKonfig.API_BASE_URL)
    }
    single { getPreferencesDataStore() }
    single { MyDataStore(get()) }

    viewModelOf(::MainViewModel)
    viewModelOf(::AuthViewModel)
}

private fun getPreferencesDataStore(): DataStore<Preferences> {
    val file = JVMPlatform().getDataDir().resolve("settings.preferences_pb")

    return PreferenceDataStoreFactory.createWithPath { file.toOkioPath() }
}