package de.tossolino.erna

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Eingebauter Updater: prüft app-version.json, lädt die neue APK und installiert sie selbst.
 * - Erstes Mal: Android fragt nach der Erlaubnis "Apps aus dieser Quelle" und nach einer Bestätigung.
 * - Ab Android 12: Hat sich die App einmal selbst installiert, laufen weitere Updates ohne Rückfrage.
 */
object Updater {

    /** Neuere Version verfügbar? Liefert die Versionsnummer oder null. */
    suspend fun latestNewerVersion(): String? = withContext(Dispatchers.IO) {
        try {
            val con = URL(Config.APP_VERSION_URL + "?t=" + System.currentTimeMillis()).openConnection() as HttpURLConnection
            con.connectTimeout = 8000; con.readTimeout = 8000
            val latest = JSONObject(con.inputStream.bufferedReader().readText()).getString("version")
            if (isNewer(latest, BuildConfig.VERSION_NAME)) latest else null
        } catch (_: Exception) { null }
    }

    /** Darf die App Pakete installieren? (Einstellung "Apps aus dieser Quelle zulassen") */
    fun canInstall(ctx: Context): Boolean = ctx.packageManager.canRequestPackageInstalls()

    /** Lädt die APK und startet die Installation. Gibt false zurück, wenn etwas schiefging. */
    suspend fun downloadAndInstall(ctx: Context): Boolean {
        val apk = withContext(Dispatchers.IO) { download(ctx) } ?: return false
        return withContext(Dispatchers.IO) {
            try {
                val installer = ctx.packageManager.packageInstaller
                val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
                params.setAppPackageName(ctx.packageName)
                if (Build.VERSION.SDK_INT >= 31) {
                    // Ohne Rückfrage, wenn Android es erlaubt (App hat sich selbst installiert)
                    params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
                }
                val id = installer.createSession(params)
                installer.openSession(id).use { session ->
                    session.openWrite("erna.apk", 0, apk.length()).use { out ->
                        apk.inputStream().use { it.copyTo(out) }
                        session.fsync(out)
                    }
                    val flags = if (Build.VERSION.SDK_INT >= 31)
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    else PendingIntent.FLAG_UPDATE_CURRENT
                    val pi = PendingIntent.getBroadcast(ctx, 7, Intent(ctx, InstallReceiver::class.java), flags)
                    session.commit(pi.intentSender)
                }
                true
            } catch (_: Exception) { false }
        }
    }

    private fun download(ctx: Context): File? = try {
        var url = URL(Config.APK_URL)
        var con: HttpURLConnection
        var hops = 0
        while (true) {   // GitHub leitet auf einen Download-Server weiter
            con = url.openConnection() as HttpURLConnection
            con.instanceFollowRedirects = false
            con.connectTimeout = 15000; con.readTimeout = 60000
            val code = con.responseCode
            if (code in 300..399 && hops++ < 5) { url = URL(url, con.getHeaderField("Location")); con.disconnect(); continue }
            if (code != 200) { con.disconnect(); throw IllegalStateException("HTTP $code") }
            break
        }
        val f = File(ctx.cacheDir, "erna-update.apk")
        con.inputStream.use { input -> f.outputStream().use { input.copyTo(it) } }
        con.disconnect()
        if (f.length() < 100_000) null else f
    } catch (_: Exception) { null }

    fun isNewer(a: String, b: String): Boolean {
        val x = a.split(".").map { it.toIntOrNull() ?: 0 }
        val y = b.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until 3) {
            val d = x.getOrElse(i) { 0 } - y.getOrElse(i) { 0 }
            if (d != 0) return d > 0
        }
        return false
    }
}
