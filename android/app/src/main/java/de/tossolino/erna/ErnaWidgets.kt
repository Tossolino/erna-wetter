package de.tossolino.erna

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle

/** Gemeinsame Basis der beiden Widget-Größen. Das eigentliche Zeichnen macht der RefreshWorker. */
abstract class ErnaWidgetBase : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        RefreshWorker.refreshNow(context)
    }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, newOptions: Bundle) {
        RefreshWorker.refreshNow(context)   // Größe geändert -> neu zeichnen
    }

    override fun onDeleted(context: Context, ids: IntArray) {
        ids.forEach { Prefs.remove(context, it) }
    }

    override fun onEnabled(context: Context) {
        RefreshWorker.schedulePeriodic(context)
    }

    companion object {
        /** Antippen des Widgets öffnet die App */
        fun openAppIntent(context: Context): PendingIntent =
            PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
    }
}

class ErnaWidget5x2 : ErnaWidgetBase()
class ErnaWidget5x3 : ErnaWidgetBase()
