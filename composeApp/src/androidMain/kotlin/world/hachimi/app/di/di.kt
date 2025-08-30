package world.hachimi.app.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toOkioPath
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import world.hachimi.app.BuildKonfig
import world.hachimi.app.api.ApiClient
import world.hachimi.app.getPlatform
import world.hachimi.app.model.AuthViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.MainViewModel
import world.hachimi.app.model.MyArtworkViewModel
import world.hachimi.app.model.PlaylistDetailViewModel
import world.hachimi.app.model.PlaylistViewModel
import world.hachimi.app.model.PublishViewModel
import world.hachimi.app.model.SearchViewModel
import world.hachimi.app.model.UserSpaceViewModel
import world.hachimi.app.player.AndroidPlayer
import world.hachimi.app.player.Player
import world.hachimi.app.storage.MyDataStore

val appModule = module {
    single {
        ApiClient(BuildKonfig.API_BASE_URL)
    }
    single { getPreferencesDataStore() }
    single { MyDataStore(get()) }
    single<Player> { AndroidPlayer() }

    singleOf(::GlobalStore)

    viewModelOf(::MainViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::PublishViewModel)
    viewModelOf(::MyArtworkViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::UserSpaceViewModel)
    viewModelOf(::PlaylistViewModel)
    viewModelOf(::PlaylistDetailViewModel)
}

private fun getPreferencesDataStore(): DataStore<Preferences> {
    val file = getPlatform().getDataDir().resolve("settings.preferences_pb")

    return PreferenceDataStoreFactory.createWithPath { file.toOkioPath() }
}