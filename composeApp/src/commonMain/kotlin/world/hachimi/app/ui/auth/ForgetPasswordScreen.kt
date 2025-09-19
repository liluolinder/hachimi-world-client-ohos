package world.hachimi.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.ForgetPasswordViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.auth.components.CaptchaDialog
import world.hachimi.app.ui.auth.components.FormCard
import world.hachimi.app.ui.insets.currentSafeAreaInsets
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun ForgetPasswordScreen(vm: ForgetPasswordViewModel = koinViewModel()) {
    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.dispose() }
    }
    val global = koinInject<GlobalStore>()
    Box(Modifier.fillMaxSize().padding(top = currentSafeAreaInsets().top)) {
        IconButton(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 24.dp).align(Alignment.TopStart),
            onClick = { global.nav.back() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        FormCard(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .widthIn(max = 512.dp)
                .fillMaxWidth()
                .align(Alignment.Center),
            title = { Text("重置密码") },
            subtitle = { Text("神也会忘记密码") },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = vm.email,
                    onValueChange = { vm.email = it },
                    label = { Text("邮箱") },
                    singleLine = true
                )
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = vm.password,
                    onValueChange = { vm.password = it },
                    label = { Text("新密码") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = vm.passwordRepeat,
                    onValueChange = { vm.passwordRepeat = it },
                    label = { Text("确认密码") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                val enabled by remember {
                    derivedStateOf {
                        vm.email.isNotBlank() && vm.password.isNotBlank()
                                && vm.password == vm.passwordRepeat
                                && vm.verifyCode.isNotBlank()
                    }
                }

                Column {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = vm.verifyCode,
                        onValueChange = { vm.verifyCode = it },
                        label = { Text("验证码") },
                        singleLine = true
                    )

                    val sendCodeEnabled by remember {
                        derivedStateOf { vm.codeRemainSecs < 0 }
                    }
                    TextButton(
                        modifier = Modifier.align(Alignment.End).padding(top = 8.dp),
                        onClick = { vm.sendVerifyCode() },
                        enabled = sendCodeEnabled && !vm.operating && vm.email.isNotBlank()
                    ) {
                        Text(
                            if (sendCodeEnabled) "发送"
                            else "重新发送 (${vm.codeRemainSecs} 秒)"
                        )
                    }
                }
                Row(Modifier.fillMaxWidth()) {
                    TextButton(modifier = Modifier, onClick = { global.nav.back() }) {
                        Text("返回")
                    }

                    Spacer(Modifier.weight(1f))

                    Button(
                        modifier = Modifier.width(110.dp),
                        onClick = { vm.submit() },
                        enabled = enabled && !vm.operating
                    ) {
                        Text("登录")
                    }
                }
            }

            if (vm.showSuccessDialog) AlertDialog(
                onDismissRequest = { vm.closeDialog() },
                confirmButton = {
                    TextButton(onClick = { vm.closeDialog() }) {
                        Text("确定")
                    }
                },
                title = { Text("成功") },
                text = { Text("已成功将您的密码重置") }
            )

            if (vm.showCaptchaDialog) CaptchaDialog(onConfirm = {
                vm.captchaContinue()
            })
        }
    }
}

@Composable
@Preview
private fun Preview() {
    PreviewTheme(background = true) {
        ForgetPasswordScreen()
    }
}