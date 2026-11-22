package de.tossolino.erna

import android.app.Application
import android.webkit.WebView

class ErnaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Muss vor dem ersten WebView passieren: erlaubt das Zeichnen der ganzen Seite in ein Bild
        WebView.enableSlowWholeDocumentDraw()
        RefreshWorker.schedulePeriodic(this)
    }
}
