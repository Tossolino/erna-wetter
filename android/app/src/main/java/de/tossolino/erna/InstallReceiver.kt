package de.tossolino.erna

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.widget.Toast

/** Rückmeldung der Installation. Muss der Nutzer bestätigen, wird der Android-Dialog geöffnet. */
class InstallReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirm: Intent? = if (Build.VERSION.SDK_INT >= 33)
                    intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                else @Suppress("DEPRECATION") intent.getParcelableExtra(Intent.EXTRA_INTENT)
                // Klappt nur, wenn die App gerade offen ist; sonst beim nächsten Öffnen erneut
                try { confirm?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); ctx.startActivity(confirm) } catch (_: Exception) {}
            }
            PackageInstaller.STATUS_SUCCESS -> {}
            PackageInstaller.STATUS_FAILURE_ABORTED -> {
                // "Abbrechen" getippt: bis morgen nicht wieder fragen
                Prefs.markUpdateDeclined(ctx, intent.getStringExtra("version") ?: "")
            }
            else -> {
                val msg = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE) ?: ""
                try { Toast.makeText(ctx, "Erna-Update nicht installiert $msg".trim(), Toast.LENGTH_LONG).show() } catch (_: Exception) {}
            }
        }
    }
}
