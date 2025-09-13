package world.hachimi.app.font

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import kotlinx.browser.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.withContext
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.fetch.Response
import org.w3c.files.Blob
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator


@Composable
fun WithFont(
    content: @Composable () -> Unit
) {
    val fontsLoaded = remember { mutableStateOf(false) }
    val fontFamilyResolver = LocalFontFamilyResolver.current

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            val fontFamily = loadFonts()
            fontFamilyResolver.preload(fontFamily)
            fontsLoaded.value = true
        }
    }
    if (fontsLoaded.value) {
        content()
    } else {
        Box(Modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }
}

@Composable
internal fun returnsNullable(): Any? = null

suspend fun loadRes(url: String): ArrayBuffer {
    return window.fetch(url).await<Response>().arrayBuffer().await()
}

fun ArrayBuffer.toByteArray(): ByteArray {
    val source = Int8Array(this, 0, byteLength)
    return jsInt8ArrayToKotlinByteArray(source)
}

internal fun jsExportInt8ArrayToWasm(src: Int8Array, size: Int, dstAddr: Int): Unit = js(
    """{
    const mem8 = new Int8Array(wasmExports.memory.buffer, dstAddr, size);
    mem8.set(src);
}"""
)

internal fun jsInt8ArrayToKotlinByteArray(x: Int8Array): ByteArray {
    val size = x.length

    @OptIn(UnsafeWasmMemoryApi::class)
    return withScopedMemoryAllocator { allocator ->
        val memBuffer = allocator.allocate(size)
        val dstAddress = memBuffer.address.toInt()
        jsExportInt8ArrayToWasm(x, size, dstAddress)
        ByteArray(size) { i -> (memBuffer + i).loadByte() }
    }
}

external class FontData : JsAny {
    val postscriptName: String
    val fullName: String
    val family: String
    val style: String

    fun blob(): Promise<Blob>
}

@JsFun(
    """ () => {
        return window.queryLocalFonts()
    }
"""
)
external fun queryLocalFonts(): Promise<JsArray<FontData>>

private val preferredCJKFontFamilies = linkedSetOf("Microsoft YaHei", "PingFang SC", "Noto Sans SC", "Noto Sans CJK");
private val preferredEmojiFontFamilies = linkedSetOf("Segoe UI Emoji", "Apple Color Emoji", "Noto Color Emoji");

// Demibold Italic
private fun parseFontStyle(styleString: String): Pair<FontWeight, FontStyle> {
    val part = styleString.split(" ");
    val weight = when (part[0].lowercase()) {
        "thin", "hairline" -> FontWeight.Thin
        "extralight", "ultralight" -> FontWeight.ExtraLight
        "light" -> FontWeight.Light
        "normal", "regular" -> FontWeight.Normal
        "medium" -> FontWeight.Medium
        "semibold", "demibold" -> FontWeight.SemiBold
        "bold" -> FontWeight.Bold
        "extrabold", "ultrabold" -> FontWeight.ExtraBold
        "black", "heavy" -> FontWeight.Black
        else -> FontWeight.Normal
    }

    val style = part.getOrNull(2)?.let {
        when (it.lowercase()) {
            "italic", "oblique" -> FontStyle.Italic
            else -> FontStyle.Normal
        }
    } ?: FontStyle.Normal

    return weight to style
}

data class LoadedLocalFont(
    val family: String,
    val weight: FontWeight,
    val style: FontStyle,
    val data: ArrayBuffer,
)

suspend fun loadLocalFonts(): List<LoadedLocalFont> {
    val fonts = try {
        queryLocalFonts().await<JsArray<FontData>>().toList()
    } catch (e: Exception) {
        error("Can't load fonts")
    }
    val fontMap = fonts.groupBy { it.family }

    val firstFont = preferredCJKFontFamilies.firstNotNullOfOrNull {
        fontMap[it]
    } ?: error("Cant find CJK fonts in computer")

    println("CJK font was found: ${firstFont.first().family}")

    val loaded = firstFont.map { fontData ->
        println("Reading font: ${fontData.family}, ${fontData.style}, ${fontData.postscriptName}")
        val (weight, style) = parseFontStyle(fontData.style)

        val blob = fontData.blob().await<Blob>()

        val reader = FileReader()
        reader.readAsArrayBuffer(blob)
        suspendCoroutine<Unit> { cont ->
            reader.addEventListener("loadend") {
                cont.resume(Unit)
            }
        }

        val buffer = reader.result as ArrayBuffer
        LoadedLocalFont(fontData.family, weight, style, buffer)
    }

    return loaded
}

suspend fun loadFonts(): FontFamily {
    val fonts = loadLocalFonts()
    val composeFonts = fonts.map { font ->
        Font(
            "${font.family} ${font.weight.weight} ${font.style}",
            font.data.toByteArray(),
            font.weight,
            font.style
        )
    }
    val fontFamily = FontFamily(composeFonts)
    return fontFamily
}