package world.hachimi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import org.koin.compose.koinInject
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        FileKit.init(this)

        setContent {
            val global = koinInject<GlobalStore>()

            BackHandler(enabled = global.nav.backStack.isNotEmpty()) {
                global.nav.back()
            }
            App()
        }
    }
}