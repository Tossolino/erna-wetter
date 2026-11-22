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
}
