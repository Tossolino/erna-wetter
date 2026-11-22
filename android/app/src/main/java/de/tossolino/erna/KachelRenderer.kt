package de.tossolino.erna

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import kotlin.coroutines.resume

/**
 * Lädt die Kachel-Ansicht der Webseite (?kachel=5x2 / 5x3) unsichtbar in einem WebView
 * und zeichnet sie in ein Bild. So kommt das Widget aus derselben Quelle wie alles andere.
 */
object KachelRenderer {

    class Result(val bitmap: Bitmap?, val summary: JSONObject?)

    suspend fun render(ctx: Context, kachel: String, widthDp: Int, heightDp: Int, transparency: Int = 0): Result =
        withContext(Dispatchers.Main) {
            withTimeoutOrNull(45_000) { renderInternal(ctx, kachel, widthDp, heightDp, transparency) } ?: Result(null, null)
        }

    @SuppressLint("SetJavaScriptEnabled")
    private suspend fun renderInternal(ctx: Context, kachel: String, widthDp: Int, heightDp: Int, transparency: Int): Result =
        suspendCancellableCoroutine { cont ->
            val density = ctx.resources.displayMetrics.density
            val w = (widthDp * density).toInt().coerceAtLeast(100)
            val h = (heightDp * density).toInt().coerceAtLeast(60)
            val handler = Handler(Looper.getMainLooper())
            val web = WebView(ctx.applicationContext)
            var finished = false

            fun finish(result: Result) {
                if (finished) return
                finished = true
                handler.removeCallbacksAndMessages(null)
                try { web.stopLoading(); web.destroy() } catch (_: Exception) {}
                if (cont.isActive) cont.resume(result)
            }
            cont.invokeOnCancellation { handler.post { finish(Result(null, null)) } }

            web.setBackgroundColor(Color.TRANSPARENT)
            web.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            with(web.settings) {
                javaScriptEnabled = true
                domStorageEnabled = true
                useWideViewPort = false          // 1 CSS-Pixel = 1 dp -> Kachel in Widget-Größe
                loadWithOverviewMode = false
                textZoom = 100                   // System-Schriftgröße ignorieren: Widget hat feste Größe
            }
            web.measure(
                View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY)
            )
            web.layout(0, 0, w, h)

            fun capture() {
                web.evaluateJavascript("JSON.stringify(window.ERNA_SUMMARY || null)") { raw ->
                    val summary = try {
                        // evaluateJavascript liefert einen JSON-String in Anführungszeichen
                        val inner = JSONObject("{\"v\":$raw}").optString("v", "null")
                        if (inner == "null") null else JSONObject(inner)
                    } catch (_: Exception) { null }
                    var bmp: Bitmap? = null
                    try {
                        web.measure(
                            View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY)
                        )
                        web.layout(0, 0, w, h)
                        bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                        web.draw(Canvas(bmp))
                        if (looksEmpty(bmp)) { bmp.recycle(); bmp = null }
                    } catch (_: Exception) { bmp = null }
                    finish(Result(bmp, summary))
                }
            }

            var polls = 0
            val poll = object : Runnable {
                override fun run() {
                    if (finished) return
                    web.evaluateJavascript("window.ERNA_READY === true") { ready ->
                        when {
                            ready == "true" -> handler.postDelayed({ capture() }, 600)  // Bilder fertig dekodieren lassen
                            ++polls > 100 -> capture()                                 // nach ~30 s trotzdem versuchen
                            else -> handler.postDelayed(this, 300)
                        }
                    }
                }
            }
            web.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    handler.removeCallbacks(poll)
                    handler.postDelayed(poll, 300)
                }
            }
            web.loadUrl(Config.BASE_URL + "?kachel=" + kachel + "&transparenz=" + transparency + "&t=" + System.currentTimeMillis())
        }

    /** Prüft, ob das WebView tatsächlich etwas gezeichnet hat (Kachel-Hintergrund ist dunkel und deckend). */
    private fun looksEmpty(b: Bitmap): Boolean {
        // Stichproben über die ganze Fläche: ist irgendwo etwas Sichtbares gezeichnet?
        for (iy in 1..9) for (ix in 1..9) {
            if (Color.alpha(b.getPixel(b.width * ix / 10, b.height * iy / 10)) > 20) return false
        }
        return true
    }
}
