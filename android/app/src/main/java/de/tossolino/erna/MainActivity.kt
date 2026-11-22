package de.tossolino.erna

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
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
    private var fileCallback: ValueCallback<Array<Uri>>? = null

    /** Tablet (kürzere Seite ab 600 dp): Querformat, Vollbild, Bildschirm bleibt an. Handy: Hochformat. */
    private val isTablet get() = resources.configuration.smallestScreenWidthDp >= 600

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isTablet) {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
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
        // Dateiauswahl für die Einstellungsseite (Foto oder Video aus der Galerie)
        web.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(view: WebView, callback: ValueCallback<Array<Uri>>, params: FileChooserParams): Boolean {
                fileCallback?.onReceiveValue(null)
                fileCallback = callback
                val pick = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
                }
                return try {
                    @Suppress("DEPRECATION")
                    startActivityForResult(Intent.createChooser(pick, "Foto oder Video wählen"), PICK_FILE)
                    true
                } catch (_: Exception) { fileCallback = null; false }
            }
        }
        // Android 15: Inhalt nicht unter Statusleiste/Navigationsleiste zeichnen
        val frame = android.widget.FrameLayout(this)
        frame.fitsSystemWindows = true
        frame.setBackgroundColor(0xFF12161D.toInt())
        frame.addView(web)
        setContentView(frame)
        // Wiederherstellen nur, wenn wirklich eine geladene Seite gespeichert war – sonst neu laden
        val restored = savedInstanceState?.let { web.restoreState(it) }
        if (restored == null || restored.size == 0) web.loadUrl(Config.BASE_URL + "?app=1")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        web.saveState(outState)
    }

    override fun onResume() {
        super.onResume()
        if (isTablet) hideSystemBars()
        web.onResume()
        RefreshWorker.refreshNow(this)   // Widgets beim Öffnen der App gleich mit aktualisieren
        if (BuildConfig.SELF_UPDATE) checkAppUpdate()   // Play-Version: Updates über den Play Store
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && isTablet) hideSystemBars()
    }

    /** Vollbild auf dem Tablet: Status- und Navigationsleiste ausblenden (Wischen vom Rand holt sie kurz zurück). */
    @Suppress("DEPRECATION")
    private fun hideSystemBars() {
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(android.view.WindowInsets.Type.systemBars())
                it.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = (android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
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
                if (!Prefs.mayTryUpdate(this@MainActivity, latest)) return@launch
                if (!Updater.canInstall(this@MainActivity)) {
                    if (!isFinishing) askInstallPermission(latest)
                    return@launch
                }
                Toast.makeText(this@MainActivity, "Erna wird auf Version $latest aktualisiert …", Toast.LENGTH_LONG).show()
                if (!Updater.downloadAndInstall(this@MainActivity, latest))
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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_FILE) {
            fileCallback?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data))
            fileCallback = null
        } else {
            @Suppress("DEPRECATION")
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object { private const val PICK_FILE = 42 }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
