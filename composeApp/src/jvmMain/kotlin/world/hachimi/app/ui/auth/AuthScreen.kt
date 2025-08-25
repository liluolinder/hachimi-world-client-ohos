package world.hachimi.app.ui.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.AuthViewModel

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
    var isLogin by remember(displayLoginAsInitial) { mutableStateOf(displayLoginAsInitial) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("欢迎回家")
            Row {
                if (isLogin) {
                    Button(onClick = {}) {
                        Text("登录")
                    }
                    TextButton(onClick = {
                        isLogin = false
                    }) {
                        Text("注册")
                    }
                } else {
                    TextButton(onClick = {
                        isLogin = true
                    }) {
                        Text("登录")
                    }
                    Button(onClick = {}) {
                        Text("注册")
                    }
                }
            }

            if (isLogin) {
                TextField(vm.email, { vm.email = it }, placeholder = {
                    Text("邮箱")
                })
                TextField(vm.password, { vm.password = it }, placeholder = {
                    Text("密码")
                }, visualTransformation = PasswordVisualTransformation())

                Button(
                    onClick = { vm.login() },
                    enabled = !vm.isOperating && vm.email.isNotBlank() && vm.password.isNotBlank(),
                ) {
                    Text("登录")
                }
            } else {
                if (vm.regStep == 0) {
                    Text("成为神人")

                    TextField(vm.regEmail, { vm.regEmail = it }, placeholder = {
                        Text("邮箱")
                    }, singleLine = true)
                    TextField(vm.regPassword, { vm.regPassword = it }, placeholder = {
                        Text("密码")
                    }, singleLine = true, visualTransformation = PasswordVisualTransformation())
                    TextField(vm.regPasswordRepeat, { vm.regPasswordRepeat = it }, placeholder = {
                        Text("确认密码")
                    }, singleLine = true, visualTransformation = PasswordVisualTransformation())

                    val enabled by derivedStateOf {
                        vm.regEmail.isNotBlank() && vm.regPassword.isNotBlank()
                                && vm.regPassword == vm.regPasswordRepeat
                    }
                    Button(
                        onClick = { vm.regNextStep() },
                        enabled = enabled && !vm.isOperating,
                    ) {
                        Text("下一步")
                    }
                } else if (vm.regStep == 1) {
                    Text("离神人很近了")
                    Text("确认你的邮箱")

                    Text("已将验证码发送到您的邮箱")

                    Row {
                        TextField(vm.regCode, { vm.regCode = it }, placeholder = {
                            Text("验证码")
                        }, singleLine = true)
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
                        onClick = { vm.regNextStep() },
                        enabled = vm.regCode.isNotBlank() && !vm.isOperating
                    ) {
                        Text("下一步")
                    }
                } else if (vm.regStep == 2) {
                    Text("只差一步")
                    Text("完善你的资料")


                    TextField(vm.name, { vm.name = it }, placeholder = {
                        Text("昵称")
                    }, singleLine = true)
                    TextField(vm.intro, { vm.intro = it }, placeholder = {
                        Text("介绍一下")
                    })

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = vm.gender == 0, onClick = { vm.gender = 0 })
                        Text("男")

                        RadioButton(selected = vm.gender == 1, onClick = { vm.gender = 1 })
                        Text("女")

                        RadioButton(selected = vm.gender == 2, onClick = { vm.gender = 2 })
                        Text("神没有性别")
                    }
                    Button(
                        onClick = { vm.finishRegister() },
                        enabled = vm.name.isNotBlank() && vm.intro.isNotBlank() && vm.gender != null && !vm.isOperating
                    ) {
                        Text("完成")
                    }

                    Button(onClick = { vm.skipProfile() }) {
                        Text("跳过")
                    }
                }
            }

            vm.error?.let {
                Text(it)
            }
        }
    }
}