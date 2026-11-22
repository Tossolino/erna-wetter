package de.tossolino.erna

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/** Zeichnet alle Erna-Widgets neu (alle 30 Minuten und auf Anstoß). */
class RefreshWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val ctx = applicationContext
        val manager = AppWidgetManager.getInstance(ctx)
        val kinds = listOf(
            Triple(ErnaWidget5x2::class.java, "5x2", Pair(320, 110)),
            Triple(ErnaWidget5x3::class.java, "5x3", Pair(320, 180))
        )
        for ((cls, kachel, defaultSize) in kinds) {
            for (id in manager.getAppWidgetIds(ComponentName(ctx, cls))) {
                val (wDp, hDp) = widgetSize(ctx, manager, id, defaultSize)
                val result = KachelRenderer.render(ctx, kachel, wDp, hDp)
                val views = when {
                    result.bitmap != null -> RemoteViews(ctx.packageName, R.layout.widget_image).apply {
                        setImageViewBitmap(R.id.kachel, result.bitmap)
                    }
                    result.summary != null -> textViews(ctx, result.summary)
                    else -> null   // nichts Neues -> altes Bild stehen lassen
                } ?: continue
                views.setOnClickPendingIntent(R.id.root, ErnaWidgetBase.openAppIntent(ctx))
                manager.updateAppWidget(id, views)
            }
        }
        return Result.success()
    }

    /** Tatsächliche Größe des Widgets in dp (Hochformat: Breite = min, Höhe = max). */
    private fun widgetSize(ctx: Context, m: AppWidgetManager, id: Int, def: Pair<Int, Int>): Pair<Int, Int> {
        val o = m.getAppWidgetOptions(id)
        val portrait = ctx.resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE
        val w = if (portrait) o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) else o.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
        val h = if (portrait) o.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT) else o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        return if (w > 50 && h > 40) Pair(w, h) else def
    }

    /** Ersatzanzeige als Text, falls das Bild nicht gezeichnet werden konnte. */
    private fun textViews(ctx: Context, s: org.json.JSONObject): RemoteViews =
        RemoteViews(ctx.packageName, R.layout.widget_text).apply {
            setTextViewText(R.id.label, s.optString("label"))
            setTextViewText(R.id.next, s.optString("next"))
            setTextViewText(R.id.temp, s.optString("temp"))
            setTextViewText(R.id.stand, s.optString("stand"))
            setTextViewText(R.id.verdict, s.optString("verdict"))
            setTextColor(R.id.verdict, parse(s.optString("verdictColor"), Color.WHITE))
            setTextViewText(R.id.frost, s.optString("frost"))
            setTextColor(R.id.frost, parse(s.optString("frostColor"), Color.WHITE))
            setViewVisibility(R.id.frost, if (s.optString("frost").isEmpty()) View.GONE else View.VISIBLE)
        }

    private fun parse(c: String, def: Int) = try { Color.parseColor(c) } catch (_: Exception) { def }

    companion object {
        private const val PERIODIC = "erna-widgets-periodic"
        private const val NOW = "erna-widgets-now"
        private val network = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        fun schedulePeriodic(ctx: Context) {
            val req = PeriodicWorkRequestBuilder<RefreshWorker>(Config.REFRESH_MINUTES, TimeUnit.MINUTES)
                .setConstraints(network).build()
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(PERIODIC, ExistingPeriodicWorkPolicy.KEEP, req)
        }

        fun refreshNow(ctx: Context) {
            val req = OneTimeWorkRequestBuilder<RefreshWorker>().setConstraints(network).build()
            WorkManager.getInstance(ctx).enqueueUniqueWork(NOW, ExistingWorkPolicy.REPLACE, req)
        }
    }
}
