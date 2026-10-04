$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

$assets = $PSScriptRoot
$repo = Split-Path -Parent (Split-Path -Parent $assets)
$sourcePath = Join-Path $repo 'ios/FrecuenciasCurativasApp/FrecuenciasCurativasApp/Assets.xcassets/AppIcon.appiconset/AppIcon-1024.png'
$source = [System.Drawing.Image]::FromFile($sourcePath)

function New-Brush([System.Drawing.Color] $color) {
    return New-Object System.Drawing.SolidBrush($color)
}

function New-Font([string] $family, [float] $size, [System.Drawing.FontStyle] $style) {
    return New-Object System.Drawing.Font($family, $size, $style)
}

function New-Canvas([int] $width, [int] $height, [System.Drawing.Color] $color) {
    $bitmap = New-Object System.Drawing.Bitmap($width, $height)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.Clear($color)
    $graphics.Dispose()
    return $bitmap
}

function Invoke-Canvas([System.Drawing.Bitmap] $bitmap, [scriptblock] $draw) {
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    & $draw $graphics
    $graphics.Dispose()
}

function Save-Png([System.Drawing.Bitmap] $bitmap, [string] $path) {
    $bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bitmap.Dispose()
}

function Draw-Text($graphics, [string] $text, [float] $x, [float] $y, [float] $size, [System.Drawing.Color] $color, [System.Drawing.FontStyle] $style = [System.Drawing.FontStyle]::Regular) {
    $font = New-Font 'Segoe UI' $size $style
    $brush = New-Brush $color
    $graphics.DrawString($text, $font, $brush, $x, $y)
    $font.Dispose()
    $brush.Dispose()
}

function Draw-Copy($graphics, [string] $text, [float] $x, [float] $y, [float] $size, [float] $width, [System.Drawing.Color] $color) {
    $font = New-Font 'Segoe UI' $size ([System.Drawing.FontStyle]::Regular)
    $brush = New-Brush $color
    $graphics.DrawString($text, $font, $brush, [System.Drawing.RectangleF]::new($x, $y, $width, 160))
    $font.Dispose()
    $brush.Dispose()
}

function Draw-Card($graphics, [float] $x, [float] $y, [float] $width, [float] $height, [System.Drawing.Color] $color) {
    $brush = New-Brush $color
    $graphics.FillRectangle($brush, $x, $y, $width, $height)
    $brush.Dispose()
}

function Draw-Line($graphics, [float] $x1, [float] $y1, [float] $x2, [float] $y2, [float] $width, [System.Drawing.Color] $color) {
    $pen = New-Object System.Drawing.Pen($color, $width)
    $graphics.DrawLine($pen, $x1, $y1, $x2, $y2)
    $pen.Dispose()
}

$black = [System.Drawing.Color]::FromArgb(255, 3, 1, 8)
$panel = [System.Drawing.Color]::FromArgb(255, 18, 9, 29)
$violet = [System.Drawing.Color]::FromArgb(255, 176, 38, 255)
$cyan = [System.Drawing.Color]::FromArgb(255, 0, 240, 255)
$pink = [System.Drawing.Color]::FromArgb(255, 245, 20, 125)
$orange = [System.Drawing.Color]::FromArgb(255, 255, 133, 39)
$white = [System.Drawing.Color]::White
$muted = [System.Drawing.Color]::FromArgb(255, 190, 184, 203)

$logo = New-Canvas 1024 1024 $black
Invoke-Canvas $logo {
    param($graphics)
    $graphics.DrawImage($source, [System.Drawing.Rectangle]::new(170, 70, 684, 684))
    Draw-Text $graphics 'CURATIVE' 220 810 78 $white ([System.Drawing.FontStyle]::Bold)
}
Save-Png $logo (Join-Path $assets 'curative-app-logo-en.png')

$feature = New-Canvas 1024 500 $black
Invoke-Canvas $feature {
    param($graphics)
    Draw-Line $graphics 32 466 992 466 5 $pink
    $graphics.DrawImage($source, [System.Drawing.Rectangle]::new(55, 62, 330, 330))
    Draw-Text $graphics 'CURATIVE APP' 438 92 52 $white ([System.Drawing.FontStyle]::Bold)
    Draw-Copy $graphics 'Daily frequencies for your practice' 440 174 25 500 $muted
    Draw-Text $graphics 'ONE-TIME PURCHASE' 440 282 25 $cyan ([System.Drawing.FontStyle]::Bold)
    Draw-Text $graphics 'US$7.77 permanent access' 440 326 22 $white
}
Save-Png $feature (Join-Path $assets 'curative-feature-graphic-en.png')

function New-PhoneScreen([string] $path, [string] $kicker, [string] $title, [string] $copy, [string[]] $labels, [System.Drawing.Color] $accent) {
    $screen = New-Canvas 1080 1920 $black
    Invoke-Canvas $screen {
        param($graphics)
        Draw-Text $graphics 'CURATIVE APP' 72 82 28 $accent ([System.Drawing.FontStyle]::Bold)
        Draw-Line $graphics 72 140 1008 140 2 ([System.Drawing.Color]::FromArgb(255, 58, 42, 73))
        Draw-Text $graphics $kicker 72 212 22 $accent ([System.Drawing.FontStyle]::Bold)
        Draw-Text $graphics $title 72 260 52 $white ([System.Drawing.FontStyle]::Bold)
        Draw-Copy $graphics $copy 72 350 25 850 $muted
        $top = 590
        $index = 0
        foreach ($label in $labels) {
            $y = $top + ($index * 180)
            Draw-Card $graphics 72 $y 936 140 $panel
            Draw-Text $graphics $label 112 ($y + 34) 30 $white ([System.Drawing.FontStyle]::Bold)
            Draw-Line $graphics 112 ($y + 102) 760 ($y + 102) 7 $accent
            Draw-Text $graphics 'Ready to play' 800 ($y + 43) 17 $muted
            $index++
        }
        Draw-Line $graphics 72 1770 1008 1770 2 ([System.Drawing.Color]::FromArgb(255, 58, 42, 73))
        Draw-Text $graphics 'Infiniti IA / W Studio' 72 1810 20 $muted
    }
    Save-Png $screen $path
}

New-PhoneScreen (Join-Path $assets 'curative-phone-01-purchase-en.png') 'ONE-TIME PURCHASE' 'Your sound space' 'Buy permanent access for US$7.77 through Google Play. No renewal or further charges.' @('US$7.77 one-time', 'Localized price', 'Permanent access') $cyan
New-PhoneScreen (Join-Path $assets 'curative-phone-02-frequencies-en.png') 'FREQUENCIES' 'Choose your environment' 'Explore tones and shape your experience for yoga, massage, or meditation.' @('Solfeggio 528 Hz', 'Theta 6 Hz', 'Calm 432 Hz') $pink
New-PhoneScreen (Join-Path $assets 'curative-phone-03-playlist-en.png') 'AUTOMATION' 'Program your session' 'Combine frequencies, set durations, and let the session run on its own.' @('Start: now', 'Duration: 20 min', 'Next: 528 Hz') $orange

$source.Dispose()
Write-Output "Assets generated in $assets"
