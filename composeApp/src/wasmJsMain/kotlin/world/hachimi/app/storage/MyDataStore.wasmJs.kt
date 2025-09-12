package world.hachimi.app.storage

actual class PreferenceKey<T> {
    actual val name: String

    constructor(name: String) {
        this.name = name
    }
}

actual object PreferencesKeys {
    actual val USER_UID: PreferenceKey<Long> = PreferenceKey("user_uid")
    actual val USER_NAME: PreferenceKey<String> = PreferenceKey("user_name")
    actual val USER_AVATAR: PreferenceKey<String> = PreferenceKey("user_avatar")
    actual val AUTH_ACCESS_TOKEN: PreferenceKey<String> = PreferenceKey("auth_access_token")
    actual val AUTH_REFRESH_TOKEN: PreferenceKey<String> = PreferenceKey("auth_refresh_token")
    actual val SETTINGS_DARK_MODE: PreferenceKey<Boolean> = PreferenceKey("settings_dark_mode")
}