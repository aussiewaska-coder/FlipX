# FlipX Control (companion app)

Control-center app for the FlipX phone-as-desktop setup. No root — privileged
display commands run through [Shizuku](https://shizuku.rikka.app/).

## What it does

* **Control center UI** — one-tap buttons: Desktop Mode, Reset Screen,
  Linux Desktop, Kill Linux (keeps layout), OpenCode single-click.
* **Auto desktop layout** — foreground service watches displays; when a monitor
  appears it applies phone `1080x1920/190` + monitor `1920x1080/190`.
  Disconnects never reset (manual Reset only).
* **Linux/OpenCode buttons** fire Termux `RUN_COMMAND` intents at the FlipX
  home scripts (needs Termux → Settings → “Allow external apps” ON).

## Requirements

* Shizuku app installed, started, permission granted to FlipX Control.
* Termux with the FlipX scripts (`guistart`, `guikill`) for the Linux buttons.

## Build

Push to `main` → GitHub Actions builds the debug APK → download from
Actions artifacts (or `gh run download`). Install with `adb install`.
