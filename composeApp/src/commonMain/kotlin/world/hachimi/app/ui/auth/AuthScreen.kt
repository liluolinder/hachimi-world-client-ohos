package world.hachimi.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.AuthViewModel
import world.hachimi.app.model.GlobalStore

@Composable
fun AuthScreen(
    displayLoginAsInitial: Boolean,
    vm: AuthViewModel = koinViewModel()
) {
    DisposableEffect(vm) {
        vm.mounted()
        onDispose {
            vm.unmount()
        }
    }
    val global = koinInject<GlobalStore>()
    var isLogin by remember(displayLoginAsInitial) { mutableStateOf(displayLoginAsInitial) }

    Box(Modifier.fillMaxSize()) {
        IconButton(
            modifier = Modifier.padding(24.dp).align(Alignment.TopStart),
            onClick = { global.nav.back() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Column(
            modifier = Modifier.width(360.dp).align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Title
            Text(
                text = when {
                    isLogin -> "欢迎回家"
                    vm.regStep == 0 -> "成为神人"
                    vm.regStep == 1 -> "离神人很近了"
                    vm.regStep == 2 -> "只差一步"
                    else -> error("unreachable")
                },
                style = MaterialTheme.typography.displayLarge
            )

            // Subtitle
            if (!isLogin && vm.regStep != 0) Text(
                text = when {
                    vm.regStep == 1 -> "确认你的邮箱"
                    vm.regStep == 2 -> "完善你的资料"
                    else -> error("unreachable")
                },
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (isLogin) {
                        Button(modifier = Modifier.weight(1f), onClick = {}) {
                            Text("登录")
                        }
                        TextButton(modifier = Modifier.weight(1f), onClick = {
                            isLogin = false
                        }) {
                            Text("注册")
                        }
                    } else {
                        TextButton(modifier = Modifier.weight(1f), onClick = {
                            isLogin = true
                        }) {
                            Text("登录")
                        }
                        Button(modifier = Modifier.weight(1f), onClick = {}) {
                            Text("注册")
                        }
                    }
                }
            }

            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    if (isLogin) {
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
                            label = { Text("密码") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )

                        Column {
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { vm.startLogin() },
                                enabled = !vm.isOperating && vm.email.isNotBlank() && vm.password.isNotBlank(),
                            ) {
                                Text("登录")
                            }
                            Spacer(Modifier.height(8.dp))
                            TextButton(onClick = { vm.forgetPassword() }) {
                                Text("忘记密码")
                            }
                        }
                    } else {
                        if (vm.regStep == 0) {
                            TextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = vm.regEmail,
                                onValueChange = { vm.regEmail = it },
                                label = { Text("邮箱") },
                                singleLine = true
                            )
                            TextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = vm.regPassword,
                                onValueChange = { vm.regPassword = it },
                                label = { Text("密码") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation()
                            )
                            TextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = vm.regPasswordRepeat,
                                onValueChange = { vm.regPasswordRepeat = it },
                                label = { Text("确认密码") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation()
                            )

                            val enabled by remember {
                                derivedStateOf {
                                    vm.regEmail.isNotBlank() && vm.regPassword.isNotBlank()
                                            && vm.regPassword == vm.regPasswordRepeat
                                }
                            }
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { vm.regNextStep() },
                                enabled = enabled && !vm.isOperating,
                            ) {
                                Text("下一步")
                            }
                        } else if (vm.regStep == 1) {
                            Text("已将验证码发送到您的邮箱", style = MaterialTheme.typography.labelMedium)

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                TextField(
                                    modifier = Modifier.weight(1f),
                                    value = vm.regCode,
                                    onValueChange = { vm.regCode = it },
                                    label = { Text("验证码") },
                                    singleLine = true
                                )
                                val sendCodeEnabled = vm.regCodeRemainSecs < 0

                                Button(
                                    onClick = { vm.regSendEmailCode() },
                                    enabled = sendCodeEnabled && !vm.isOperating
                                ) {
                                    Text(
                                        if (sendCodeEnabled) "重新发送"
                                        else "重新发送 (${vm.regCodeRemainSecs} 秒)"
                                    )
                                }
                            }
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { vm.regNextStep() },
                                enabled = vm.regCode.isNotBlank() && !vm.isOperating
                            ) {
                                Text("下一步")
                            }
                        } else if (vm.regStep == 2) {
                            TextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = vm.name,
                                onValueChange = { vm.name = it },
                                label = { Text("昵称") },
                                singleLine = true
                            )
                            TextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = vm.intro,
                                onValueChange = { vm.intro = it },
                                label = { Text("介绍一下") },
                                maxLines = 4,
                                minLines = 4
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = vm.gender == 0, onClick = { vm.gender = 0 })
                                Text("男")

                                RadioButton(selected = vm.gender == 1, onClick = { vm.gender = 1 })
                                Text("女")

                                RadioButton(selected = vm.gender == 2, onClick = { vm.gender = 2 })
                                Text("神没有性别")
                            }

                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { vm.finishRegister() },
                                enabled = vm.name.isNotBlank() && vm.intro.isNotBlank() && vm.gender != null && !vm.isOperating
                            ) {
                                Text("完成")
                            }

                            TextButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { vm.skipProfile() }
                            ) {
                                Text("跳过")
                            }
                        }
                    }
                }
            }
        }

        if (vm.showCaptchaDialog) AlertDialog(
            onDismissRequest = {},
            title = {
                Text("请完成人机验证")
            },
            text = {
                Text("请在打开的浏览器页面中完成人机验证")
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.finishCaptcha()
                }) {
                    Text("我已完成，继续")
                }
            }
        )
    }
}