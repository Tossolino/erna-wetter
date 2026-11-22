package de.tossolino.erna

import android.content.Context

/** Einstellungen je Widget (gespeichert auf dem Handy). */
object Prefs {
    private fun sp(ctx: Context) = ctx.getSharedPreferences("erna", Context.MODE_PRIVATE)

    /** Transparenz des Widget-Hintergrunds in Prozent: 0 = deckend, 100 = ganz durchsichtig */
    fun transparency(ctx: Context, widgetId: Int): Int = sp(ctx).getInt("transparenz_$widgetId", 0)

    fun setTransparency(ctx: Context, widgetId: Int, value: Int) =
        sp(ctx).edit().putInt("transparenz_$widgetId", value.coerceIn(0, 100)).apply()

    fun remove(ctx: Context, widgetId: Int) = sp(ctx).edit().remove("transparenz_$widgetId").apply()

    // ----- Updater: nicht in einer Schleife immer wieder fragen -----
    private const val RETRY_MS = 30 * 60 * 1000L          // nach einem Versuch 30 Minuten Ruhe
    private const val DECLINED_MS = 24 * 60 * 60 * 1000L  // nach "Abbrechen" einen Tag Ruhe

    /** Darf jetzt ein Update-Versuch für diese Version starten? */
    fun mayTryUpdate(ctx: Context, version: String): Boolean {
        val p = sp(ctx); val now = System.currentTimeMillis()
        if (now - p.getLong("update_try_at", 0) < RETRY_MS) return false
        if (p.getString("update_declined_version", "") == version && now - p.getLong("update_declined_at", 0) < DECLINED_MS) return false
        return true
    }
    fun markUpdateTried(ctx: Context) = sp(ctx).edit().putLong("update_try_at", System.currentTimeMillis()).apply()
    fun markUpdateDeclined(ctx: Context, version: String) = sp(ctx).edit()
        .putString("update_declined_version", version).putLong("update_declined_at", System.currentTimeMillis()).apply()
}
