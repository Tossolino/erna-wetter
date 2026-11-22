package de.tossolino.erna

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/** Die App zeigt die Online-Anzeige im Hochformat (Handy-Ansicht der gemeinsamen Webseite). */
class MainActivity : Activity() {
    private lateinit var web: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        web.setBackgroundColor(0xFF12161D.toInt())
        with(web.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false   // Gassi-Video startet von selbst
        }
        web.webViewClient = object : WebViewClient() {
            // Links nach außen (z. B. Download) im Browser öffnen, die Anzeige selbst in der App
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                if (url.startsWith(Config.BASE_URL)) return false
                startActivity(Intent(Intent.ACTION_VIEW, request.url))
                return true
            }
        }
        setContentView(web)
        if (savedInstanceState != null) web.restoreState(savedInstanceState)
        else web.loadUrl(Config.BASE_URL + "?app=1")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        web.saveState(outState)
    }

    override fun onResume() {
        super.onResume()
        web.onResume()
        RefreshWorker.refreshNow(this)   // Widgets beim Öffnen der App gleich mit aktualisieren
        checkAppUpdate()
    }

    override fun onPause() {
        web.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        web.destroy()
        super.onDestroy()
    }

    /** Neue App-Version? -> Hinweis mit Download. (Inhalte der Anzeige aktualisieren sich ohnehin selbst.) */
    private fun checkAppUpdate() {
        thread {
            try {
                val con = URL(Config.APP_VERSION_URL + "?t=" + System.currentTimeMillis()).openConnection() as HttpURLConnection
                con.connectTimeout = 8000; con.readTimeout = 8000
                val latest = JSONObject(con.inputStream.bufferedReader().readText()).getString("version")
                if (isNewer(latest, BuildConfig.VERSION_NAME)) runOnUiThread {
                    if (isFinishing) return@runOnUiThread
                    AlertDialog.Builder(this)
                        .setTitle("Neue Erna-App")
                        .setMessage("Version $latest ist verfügbar (installiert: ${BuildConfig.VERSION_NAME}).\n\nJetzt herunterladen und installieren?")
                        .setPositiveButton("Herunterladen") { _, _ ->
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Config.APK_URL)))
                        }
                        .setNegativeButton("Später", null)
                        .show()
                }
            } catch (_: Exception) { /* offline – beim nächsten Öffnen erneut */ }
        }
    }

    private fun isNewer(a: String, b: String): Boolean {
        val x = a.split(".").map { it.toIntOrNull() ?: 0 }
        val y = b.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until 3) {
            val d = x.getOrElse(i) { 0 } - y.getOrElse(i) { 0 }
            if (d != 0) return d > 0
        }
        return false
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
