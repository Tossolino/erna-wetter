package de.tossolino.erna

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.webkit.WebResourceRequest
import android.widget.Toast
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/** Die App zeigt die Online-Anzeige im Hochformat (Handy-Ansicht der gemeinsamen Webseite). */
class MainActivity : Activity() {
    private lateinit var web: WebView
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

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
        scope.cancel()
        web.destroy()
        super.onDestroy()
    }

    /** Neue App-Version? -> selbst herunterladen und installieren (Inhalte aktualisieren sich ohnehin selbst). */
    private var updateRunning = false
    private fun checkAppUpdate() {
        if (updateRunning) return
        updateRunning = true
        scope.launch {
            try {
                val latest = Updater.latestNewerVersion() ?: return@launch
                if (!Updater.canInstall(this@MainActivity)) {
                    if (!isFinishing) askInstallPermission(latest)
                    return@launch
                }
                Toast.makeText(this@MainActivity, "Erna wird auf Version $latest aktualisiert …", Toast.LENGTH_LONG).show()
                if (!Updater.downloadAndInstall(this@MainActivity))
                    Toast.makeText(this@MainActivity, "Update konnte nicht geladen werden – später erneut.", Toast.LENGTH_LONG).show()
            } finally { updateRunning = false }
        }
    }

    /** Einmalig: Erlaubnis, dass Erna sich selbst aktualisieren darf. */
    private fun askInstallPermission(latest: String) {
        AlertDialog.Builder(this)
            .setTitle("Neue Erna-App $latest")
            .setMessage("Damit sich Erna künftig selbst aktualisieren kann, bitte einmalig erlauben:\n\n" +
                    "Im nächsten Fenster „Aus dieser Quelle zulassen“ einschalten und zurück zur App.")
            .setPositiveButton("Erlauben") { _, _ ->
                startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:$packageName")))
            }
            .setNegativeButton("Später", null)
            .show()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
