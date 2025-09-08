package world.hachimi.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import world.hachimi.app.BuildKonfig
import world.hachimi.app.model.GlobalStore

@Composable
fun SettingsScreen() {
    val globalStore = koinInject<GlobalStore>()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("设置", style = MaterialTheme.typography.titleLarge)

        PropertyItem(label = {
            Text("深色模式", style = MaterialTheme.typography.bodyLarge)
        }) {
            var expanded by remember { mutableStateOf(false) }
            Box {
                TextButton(onClick = { expanded = true }) {
                    Text(
                        when (globalStore.darkMode) {
                            true -> "始终开启"
                            false -> "始终关闭"
                            null -> "跟随系统"
                        }
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                }
                DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(onClick = {
                        globalStore.updateDarkMode(null)
                        expanded = false
                    }, text = {
                        Text("跟随系统")
                    })
                    DropdownMenuItem(onClick = {
                        globalStore.updateDarkMode(true)
                        expanded = false
                    }, text = {
                        Text("始终开启")
                    })
                    DropdownMenuItem(onClick = {
                        globalStore.updateDarkMode(false)
                        expanded = false
                    }, text = {
                        Text("始终关闭")
                    })
                }
            }
        }

        PropertyItem(label = { Text("版本名") }) {
            Text(BuildKonfig.VERSION_NAME)
        }
        PropertyItem(label = { Text("版本号") }) {
            Text(BuildKonfig.VERSION_CODE.toString())
        }
    }
}

@Composable
private fun PropertyItem(
    label: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        label()
        Spacer(Modifier.weight(1f))
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
            content()
        }
    }
}