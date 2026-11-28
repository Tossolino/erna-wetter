#Requires -Version 5.1
<#
  Erna-Wetter – Widget starten (wird von den Verknüpfungen "Erna-Wetter" aufgerufen)
  Öffnet die Anzeige als Edge-Fenster und schiebt es in die rechte obere Ecke des Hauptmonitors.
  Die zuletzt eingestellte Fenstergröße bleibt erhalten.
#>
$ErrorActionPreference = 'SilentlyContinue'
$AppDir      = Join-Path $env:LOCALAPPDATA 'ErnaWidget'
$EdgeProfile = Join-Path $AppDir 'edge'
$Url         = 'https://tossolino.github.io/erna-wetter/?widget=1'
$Abstand     = 12   # Pixel Abstand zum Bildschirmrand

$edge = @(
    (Join-Path ${env:ProgramFiles(x86)} 'Microsoft\Edge\Application\msedge.exe'),
    (Join-Path $env:ProgramFiles 'Microsoft\Edge\Application\msedge.exe'),
    (Join-Path $env:LOCALAPPDATA 'Microsoft\Edge\Application\msedge.exe')
) | Where-Object { Test-Path $_ } | Select-Object -First 1
if (-not $edge) { exit 1 }

Add-Type -AssemblyName System.Windows.Forms
Add-Type -Namespace ErnaWin -Name Api -MemberDefinition @'
[DllImport("user32.dll")] public static extern bool SetWindowPos(System.IntPtr h, System.IntPtr after, int x, int y, int cx, int cy, uint flags);
[DllImport("user32.dll")] public static extern bool GetWindowRect(System.IntPtr h, out RECT r);
[DllImport("user32.dll")] public static extern bool ShowWindow(System.IntPtr h, int cmd);
public struct RECT { public int Left, Top, Right, Bottom; }
'@

# Läuft das Widget schon? Dann nur nach vorne holen und in die Ecke schieben.
function Find-Window {
    Get-Process msedge -ErrorAction SilentlyContinue |
        Where-Object { $_.MainWindowHandle -ne 0 -and $_.MainWindowTitle -like 'Erna Wetter*' } |
        Select-Object -First 1
}
$p = Find-Window
if (-not $p) {
    Start-Process -FilePath $edge -ArgumentList "--app=`"$Url`" --user-data-dir=`"$EdgeProfile`" --window-size=900,563 --no-first-run --no-default-browser-check"
    for ($i = 0; $i -lt 60 -and -not $p; $i++) { Start-Sleep -Milliseconds 500; $p = Find-Window }
}
if (-not $p) { exit 0 }

$h = $p.MainWindowHandle
[ErnaWin.Api]::ShowWindow($h, 9) | Out-Null          # falls minimiert: wiederherstellen
Start-Sleep -Milliseconds 300
$r = New-Object ErnaWin.Api+RECT
[ErnaWin.Api]::GetWindowRect($h, [ref]$r) | Out-Null
$w = $r.Right - $r.Left
$wa = [System.Windows.Forms.Screen]::PrimaryScreen.WorkingArea
$x = $wa.Right - $w - $Abstand
$y = $wa.Top + $Abstand
# SWP_NOSIZE (1) | SWP_NOZORDER (4) | SWP_SHOWWINDOW (0x40)
[ErnaWin.Api]::SetWindowPos($h, [IntPtr]::Zero, $x, $y, 0, 0, 0x45) | Out-Null
