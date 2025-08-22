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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.nav.RootContent
import world.hachimi.app.nav.Route

@Composable
fun AuthScreen(init: Boolean) {
    var isLogin by remember(init) { mutableStateOf(init) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.align(Alignment.Center)) {
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
                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }

                TextField(email, { email = it }, placeholder = {
                    Text("邮箱")
                })
                TextField(password, { password = it }, placeholder = {
                    Text("密码")
                }, visualTransformation = PasswordVisualTransformation())

                Button(onClick = {}) {
                    Text("登录")
                }
            } else {
                var step by remember { mutableStateOf(0) }
                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                var passwordRepeat by remember { mutableStateOf("") }
                var code by remember { mutableStateOf("") }
                var codeRemainSecs by remember { mutableStateOf(0) }

                LaunchedEffect(Unit) {
                    while (isActive) {
                        delay(1000)
                        codeRemainSecs -= 1
                    }
                }

                if (step == 0) {
                    Text("成为神人")

                    TextField(email, { email = it }, placeholder = {
                        Text("邮箱")
                    }, singleLine = true)
                    TextField(password, { password = it }, placeholder = {
                        Text("密码")
                    }, singleLine = true, visualTransformation = PasswordVisualTransformation())
                    TextField(passwordRepeat, { passwordRepeat = it }, placeholder = {
                        Text("确认密码")
                    }, singleLine = true, visualTransformation = PasswordVisualTransformation())

                    Button(onClick = {
                        step = 1
                        codeRemainSecs = 60
                    }) {
                        Text("下一步")
                    }
                } else if (step == 1) {
                    Text("离神人很近了")
                    Text("确认你的邮箱")

                    Text("已将验证码发送到您的邮箱")

                    Row {
                        TextField(code, { code = it }, placeholder = {
                            Text("验证码")
                        }, singleLine = true)
                        val sendCodeEnabled = codeRemainSecs < 0

                        Button(
                            onClick = {
                                if (sendCodeEnabled) codeRemainSecs = 60
                            },
                            enabled = sendCodeEnabled
                        ) {
                            Text("重新发送 (${codeRemainSecs} 秒)")
                        }
                    }
                    Button(onClick = {
                        step = 2
                    }) {
                        Text("下一步")
                    }
                } else if (step == 2) {
                    Text("只差一步")
                    Text("完善你的资料")

                    var name by remember { mutableStateOf("") }
                    var intro by remember { mutableStateOf("") }
                    var gender by remember { mutableStateOf<Int?>(null) }

                    TextField(name, { name = it }, placeholder = {
                        Text("昵称")
                    }, singleLine = true)
                    TextField(intro, { intro = it }, placeholder = {
                        Text("介绍一下")
                    })

                    Row {
                        RadioButton(selected = gender == 0, onClick = { gender = 0 })
                        Text("男")

                        RadioButton(selected = gender == 1, onClick = { gender = 1 })
                        Text("女")

                        RadioButton(selected = gender == 2, onClick = { gender = 2 })
                        Text("神没有性别")
                    }
                    Button(onClick = {
                        GlobalStore.setLoginUser("Test User", null)
                        GlobalStore.nav.replace(Route.Root(RootContent.Home))
                    }) {
                        Text("完成")
                    }
                }
            }
        }
    }
}