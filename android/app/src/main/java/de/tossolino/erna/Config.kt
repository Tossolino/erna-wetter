package de.tossolino.erna

object Config {
    /** Online-Anzeige (gemeinsame Quelle für Tablet, Windows-Widget, Handy und Android-Widgets) */
    const val BASE_URL = "https://tossolino.github.io/erna-wetter/"
    /** Hier steht die aktuelle Versionsnummer der App (android/VERSION) */
    const val APP_VERSION_URL = BASE_URL + "app-version.json"
    /** Download der neuesten App */
    const val APK_URL = "https://github.com/Tossolino/erna-wetter/releases/latest/download/erna-app.apk"
    /** Widgets werden so oft neu gezeichnet (Minuten, Android erlaubt mindestens 15) */
    const val REFRESH_MINUTES = 30L
}
