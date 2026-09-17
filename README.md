# FlipX

Full Debian XFCE desktop on a Samsung Galaxy Z Flip6 — via Termux, Termux:X11 and
proot-distro. No DeX required. Correct fullscreen on the main display, the cover
screen (clamshell), and automatic resolution switching when you hop between the
desktop and Android apps.

## What's in here

| File | What it does |
|---|---|
| `guistart` | One-shot desktop launcher: kills stale sessions, disables Android's phantom-process killer, forces 16:9 while the desktop runs, applies Termux:X11 prefs, starts the X server + XFCE, and restores native phone resolution on exit |
| `restoggle` | Background watcher: forces `1080x1920` only while Termux:X11 is in the foreground, resets to native for every other Android app |
| `debian/google-chrome.desktop` | Chrome launcher with the flags proot needs (`--no-sandbox --test-type --disable-gpu`) |

## Prerequisites (Termux side)

- Termux + Termux:X11 app
- `proot-distro` with a Debian container running XFCE (`startxfce4`)
- `android-tools` package (`pkg install android-tools`) + **Wireless debugging** enabled, so `adb connect 127.0.0.1:5555` works
- Optional, for the cover screen: Samsung Good Lock → MultiStar → allow Termux:X11 on the FlexWindow

## Install

```bash
cp guistart restoggle ~/
chmod +x ~/guistart ~/restoggle
```

Then start the desktop:

```bash
./guistart
```

## How it works

- `guistart` sets `displayResolutionMode:native`, so the X server adopts whatever
  screen it's on: full widescreen when open, proper square 720×748 on the cover
  screen when closed — never squashed.
- While the desktop runs, `wm size 1080x1920` keeps the X session at an exact
  1920×1080. `restoggle` watches the foreground app and drops the phone back to
  native resolution whenever you switch to a regular Android app.
- Logging out of XFCE stops the watcher and resets the resolution, so the phone
  is never left in a forced mode.

## Extras

**Chrome on ARM Debian** (inside the proot):

```bash
sudo apt install ./google-chrome-stable_current_arm64.deb   # from Google's site
cp debian/google-chrome.desktop ~/.local/share/applications/
cp debian/google-chrome.desktop ~/Desktop/   # optional desktop icon
```

Plain Chrome won't show a window under proot (no user namespaces, crashing GPU
process) — the bundled launcher bakes in the working flags.

**Mouse too twitchy?** The Termux:X11 pointer is raw 1:1 by default. Scale it
down live (tune `0.7` to taste):

```bash
DISPLAY=:0 xinput set-prop "Lorie mouse" "Coordinate Transformation Matrix" \
  0.7 0 0  0 0.7 0  0 0 1
```

(Make it permanent with an XFCE autostart entry once you're happy with the value.)
