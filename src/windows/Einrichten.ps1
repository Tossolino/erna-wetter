#Requires -Version 5.1
<#
  Erna-Wetter – Widget für Windows einrichten
  Das Widget ist ein kleines Fenster (Microsoft Edge im App-Modus), das die
  Online-Anzeige https://tossolino.github.io/erna-wetter/ zeigt.
  Updates kommen dadurch automatisch – genau wie auf dem Tablet.
  Dieses Skript muss nur einmal ausgeführt werden.
#>
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName PresentationFramework

$Url     = 'https://tossolino.github.io/erna-wetter/?widget=1'
$AppDir  = Join-Path $env:LOCALAPPDATA 'ErnaWidget'
$EdgeProfile = Join-Path $AppDir 'edge'
$Icon    = Join-Path $AppDir 'erna.ico'

function Say($text, $buttons = 'OK', $icon = 'Information') {
    [System.Windows.MessageBox]::Show($text, 'Erna-Wetter Widget', $buttons, $icon)
}

try {
    # Microsoft Edge finden (ist auf Windows 10/11 vorinstalliert)
    $edge = @(
        (Join-Path ${env:ProgramFiles(x86)} 'Microsoft\Edge\Application\msedge.exe'),
        (Join-Path $env:ProgramFiles 'Microsoft\Edge\Application\msedge.exe'),
        (Join-Path $env:LOCALAPPDATA 'Microsoft\Edge\Application\msedge.exe')
    ) | Where-Object { Test-Path $_ } | Select-Object -First 1
    if (-not $edge) { Say 'Microsoft Edge wurde nicht gefunden. Bitte Edge installieren und erneut starten.' 'OK' 'Error' | Out-Null; exit 1 }

    New-Item -ItemType Directory -Force -Path $AppDir | Out-Null
    Copy-Item (Join-Path $PSScriptRoot 'erna.ico') $Icon -Force

    $edgeArgs = "--app=`"$Url`" --user-data-dir=`"$EdgeProfile`" --window-size=900,563 " +
                "--no-first-run --no-default-browser-check"
    $sh = New-Object -ComObject WScript.Shell
    function New-Link([string]$path) {
        $l = $sh.CreateShortcut($path)
        $l.TargetPath = $edge
        $l.Arguments = $edgeArgs
        $l.IconLocation = $Icon
        $l.WorkingDirectory = $AppDir
        $l.Description = 'Erna-Wetter: Frostwarnung, Gassi-Vorschläge und Regen'
        $l.Save()
    }

    # Desktop-Verknüpfung
    $desktopLnk = Join-Path ([Environment]::GetFolderPath('Desktop')) 'Erna-Wetter.lnk'
    New-Link $desktopLnk

    # Autostart
    $startupLnk = Join-Path ([Environment]::GetFolderPath('Startup')) 'Erna-Wetter.lnk'
    $a = Say "Soll das Erna-Wetter-Widget automatisch mit Windows starten?" 'YesNo' 'Question'
    if ($a -eq 'Yes') { New-Link $startupLnk } elseif (Test-Path $startupLnk) { Remove-Item $startupLnk -Force }

    # altes PowerShell-Widget (Frost & Regen) aus dem Autostart entfernen
    $oldLnk = Join-Path ([Environment]::GetFolderPath('Startup')) 'Frost & Regen.lnk'
    if (Test-Path $oldLnk) {
        $b = Say "Das alte Widget 'Frost & Regen' startet noch mit Windows.`nSoll es aus dem Autostart entfernt werden?" 'YesNo' 'Question'
        if ($b -eq 'Yes') { Remove-Item $oldLnk -Force }
    }

    Start-Process -FilePath $edge -ArgumentList $edgeArgs
    Say ("Fertig! Das Widget startet jetzt.`n`n" +
         "Es liegt als 'Erna-Wetter' auf dem Desktop.`n" +
         "Fenstergröße und Position kannst du frei ändern – Edge merkt sie sich.`n" +
         "Neue Versionen werden automatisch geladen.") | Out-Null
} catch {
    Say ("Einrichtung fehlgeschlagen:`n" + $_.Exception.Message) 'OK' 'Error' | Out-Null
    exit 1
}
