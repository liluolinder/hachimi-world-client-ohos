package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.CommonError
import world.hachimi.app.api.module.AuthModule
import world.hachimi.app.api.module.UserModule
import world.hachimi.app.logging.Logger
import world.hachimi.app.nav.RootContent
import world.hachimi.app.nav.Route
import world.hachimi.app.storage.MyDataStore
import world.hachimi.app.storage.PreferencesKeys

class AuthViewModel(
    private val api: ApiClient,
    private val dataStore: MyDataStore
): ViewModel() {
    var isOperating by mutableStateOf(false)
        private set

    var email by mutableStateOf("")
    var password by mutableStateOf("")

    var regStep by mutableStateOf(0)
    var regEmail by mutableStateOf("")
    var regPassword by mutableStateOf("")
    var regPasswordRepeat by mutableStateOf("")
    var regCode by mutableStateOf("")

    var regCodeRemainSecs by mutableStateOf(-1)
        private set


    var uid by mutableStateOf("")
        private set
    var name by mutableStateOf("")
    var intro by  mutableStateOf("")
    var gender by  mutableStateOf<Int?>(null)

    var error by mutableStateOf<String?>(null)

    fun mounted() {
        startCountdownJob()
    }

    fun unmount() {
        countdownJob?.cancel()
        countdownJob = null
    }

    private var countdownJob: Job? = null

    private fun startCountdownJob() {
        viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                regCodeRemainSecs -= 1
                delay(1000)
            }
        }
    }

    fun regSendEmailCode() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                isOperating = true
                clearErrorMessage()

                val result = api.authModule.sendEmailCode(AuthModule.SendEmailCodeReq(email = regEmail))
                if (!result.ok) {
                    val errData = result.errData<CommonError>()
                    error = errData.msg
                    return@launch
                }
                regCodeRemainSecs = 60
            } catch (e: Exception) {
                Logger.e("auth", "Failed to send email code", e)
                error = e.localizedMessage
            } finally {
                isOperating = false
            }
        }
    }

    fun regNextStep() {
        if (regStep == 0) {
            regSendEmailCode()
            regStep = 1
        } else if (regStep == 1) {
            viewModelScope.launch(Dispatchers.IO) {
                mRegister()
            }
        }
    }

    fun finishRegister() {
        viewModelScope.launch {
            isOperating = true
            try {
                val resp = api.userModule.updateProfile(
                    UserModule.UpdateProfileReq(
                        username = name,
                        bio = intro,
                        gender = gender.takeUnless { it == 2 }, // Map 2 to null
                    )
                )
                if (resp.ok) {
                    dataStore.set(PreferencesKeys.USER_NAME, name)
                    GlobalStore.setLoginUser(name, null)
                    GlobalStore.nav.replace(Route.Root(RootContent.Home))
                } else {
                    error = resp.errData<CommonError>().msg
                }
            } catch (e: Exception) {
                error = e.localizedMessage
            } finally {
                isOperating = false
            }
        }

    }

    fun skipProfile() {
        GlobalStore.nav.replace(Route.Root(RootContent.Home))
    }

    fun clearErrorMessage() {
        error = null
    }

    private suspend fun mRegister() = withContext(Dispatchers.IO) {
        try {
            isOperating = true
            val resp = api.authModule.registerEmail(AuthModule.RegisterReq(
                email = regEmail,
                password = regPassword,
                code = regCode,
                deviceInfo = "Desktop Client" // TODO
            ))
            if (resp.ok) {
                val data: AuthModule.RegisterResp = resp.okData()
                uid = data.uid.toString()
                name = data.generatedUsername

                regStep = 2

                // Set token to the api client
                api.setToken(data.token.accessToken, data.token.refreshToken)

                // Save token
                dataStore.set(PreferencesKeys.USER_UID, data.uid)
                dataStore.set(PreferencesKeys.USER_NAME, data.generatedUsername)
                dataStore.set(PreferencesKeys.AUTH_ACCESS_TOKEN, data.token.accessToken)
                dataStore.set(PreferencesKeys.AUTH_REFRESH_TOKEN, data.token.refreshToken)
            } else {
                error = resp.errData<CommonError>().msg
            }
        } catch (e: Exception) {
            error = e.localizedMessage
        } finally {
            isOperating = false
        }
    }
}