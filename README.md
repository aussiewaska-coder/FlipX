![FlipX — Debian desktop on a folded Flip, external monitor, keyboard and mouse](flipx.jpeg)

# FlipX

**Your Samsung never got DeX? Good — you don't need it.**

FlipX turns a Samsung Galaxy Z Flip — and every other Galaxy phone Samsung left
behind — into a complete Debian XFCE desktop workstation. On the phone itself,
on the little cover screen, or on a full-size external monitor with your phone
doubling as a precision trackpad.

If you've spent years watching other phones get desktop modes while yours got
nothing, stop waiting. This is that desktop — and paired with an external
display, it's better than DeX in the way that counts: a real Linux desktop out
front, and a giant trackpad already in your hand.

## Why FlipX

- **No DeX required.** Built for the long-suffering Samsung users whose phones
  never got a desktop mode — using Termux, Termux:X11 and proot Debian, all
  free software, no root, no hacks to the phone itself.
- **Flip-first.** The Z Flip's cover screen becomes a proper miniature desktop
  (correct aspect, never squashed), and folded shut the phone sips power while
  your session keeps running.
- **Your phone is the trackpad.** Plug into an external monitor and the phone's
  own screen becomes a large, precise trackpad for the desktop (Termux:X11
  Preferences → Touch Mode → Trackpad). Monitor, keyboard, mouse, trackpad — a
  full workstation from one phone and one cable.
- **Plays nice with Android.** The phone's resolution switches to 16:9 only
  while the desktop is in the foreground; hop to any Android app and the phone
  snaps back to native. Log out and everything is restored — the phone is never
  left in a weird state.

## Compatibility

| Device | Status |
|---|---|
| Galaxy Z Flip6 | ✅ Tested — main display, cover screen, external monitor |
| Galaxy Z Flip5 / Flip4 | ✅ Expected — same stack (cover screen via Good Lock MultiStar) |
| Other Galaxy phones without DeX | ✅ Expected — main display + external monitor flows |
| Fold, S series, tablets, any modern Android | ✅ Should work — the core setup is device-independent |

The core (Termux + Termux:X11 + proot Debian) runs anywhere. The Flip-specific
polish — clamshell cover screen, tall-screen handling — is where FlipX shines.

Feature requirements: cover screen needs Good Lock → MultiStar; external
monitor needs USB-C video out (or Smart View wireless); the automatic
resolution switching needs wireless ADB to localhost.

## Quick start

Prerequisites (Termux side): Termux + the Termux:X11 app, `proot-distro` with a
Debian container running XFCE, the `android-tools` package
(`pkg install android-tools`) with **Wireless debugging** enabled.

```bash
cp guistart restoggle ~/
chmod +x ~/guistart ~/restoggle
./guistart
```

## What's in here

| File | What it does |
|---|---|
| `guistart` | One-shot desktop launcher: kills stale sessions, disables Android's phantom-process killer, forces 16:9 while the desktop runs, applies Termux:X11 prefs, starts the X server + XFCE, and restores native phone resolution on exit |
| `restoggle` | Background watcher: forces `1080x1920` only while Termux:X11 is in the foreground, resets to native for every other Android app |
| `debian/google-chrome.desktop` | Chrome launcher with the flags proot needs (`--no-sandbox --test-type --disable-gpu`) |

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
