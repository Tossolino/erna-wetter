package de.tossolino.erna

import android.annotation.SuppressLint
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView

/**
 * Einstellungen eines Widgets: Transparenz des Hintergrunds mit Live-Vorschau.
 * Öffnet sich beim Hinzufügen (ältere Android-Versionen) bzw. über
 * Widget lange drücken > Einstellungen (Android 12 und neuer).
 */
class ConfigActivity : Activity() {
    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var preview: WebView
    private var kachel = "5x2"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        widgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }

        // Sofort "OK" melden: auch beim Zurück-Wischen bleibt das Widget mit den bisherigen Einstellungen erhalten
        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))

        val info = AppWidgetManager.getInstance(this).getAppWidgetInfo(widgetId)
        if (info?.provider?.className?.endsWith("5x3") == true) kachel = "5x3"

        setContentView(R.layout.activity_config)
        preview = findViewById(R.id.preview)
        preview.setBackgroundColor(Color.TRANSPARENT)
        with(preview.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = false
            textZoom = 100
        }
        preview.layoutParams = preview.layoutParams.apply {
            height = (resources.displayMetrics.density * (if (kachel == "5x3") 230 else 150)).toInt()
        }

        val label = findViewById<TextView>(R.id.label)
        val seek = findViewById<SeekBar>(R.id.seek)
        seek.progress = Prefs.transparency(this, widgetId)
        label.text = getString(R.string.config_transparency, seek.progress)
        loadPreview(seek.progress)

        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar, value: Int, fromUser: Boolean) {
                label.text = getString(R.string.config_transparency, value)
            }
            override fun onStartTrackingTouch(s: SeekBar) {}
            override fun onStopTrackingTouch(s: SeekBar) { loadPreview(s.progress) }
        })

        findViewById<Button>(R.id.done).setOnClickListener {
            Prefs.setTransparency(this, widgetId, seek.progress)
            RefreshWorker.refreshNow(this)
            finish()
        }
    }

    private fun loadPreview(transparency: Int) {
        preview.loadUrl(Config.BASE_URL + "?kachel=" + kachel + "&transparenz=" + transparency + "&t=" + System.currentTimeMillis())
    }

    override fun onDestroy() {
        if (::preview.isInitialized) preview.destroy()
        super.onDestroy()
    }
}
